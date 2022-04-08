/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.store;

import org.eclipse.collections.api.set.ImmutableSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.neo4j.configuration.Config;
import org.neo4j.exceptions.UnderlyingStorageException;
import org.neo4j.internal.id.DefaultIdGeneratorFactory;
import org.neo4j.io.fs.EphemeralFileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.pagecache.DelegatingPageCache;
import org.neo4j.io.pagecache.DelegatingPagedFile;
import org.neo4j.io.pagecache.IOController;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.PagedFile;
import org.neo4j.io.pagecache.context.CursorContext;
import org.neo4j.io.pagecache.context.CursorContextFactory;
import org.neo4j.io.pagecache.impl.DelegatingPageCursor;
import org.neo4j.io.pagecache.tracing.DefaultPageCacheTracer;
import org.neo4j.io.pagecache.tracing.PageCacheTracer;
import org.neo4j.io.pagecache.tracing.cursor.PageCursorTracer;
import org.neo4j.kernel.KernelVersion;
import org.neo4j.kernel.impl.store.record.MetaDataRecord;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.transaction.log.EmptyLogTailMetadata;
import org.neo4j.kernel.impl.transaction.log.LogPosition;
import org.neo4j.kernel.impl.transaction.log.LogTailMetadata;
import org.neo4j.logging.InternalLogProvider;
import org.neo4j.logging.NullLogProvider;
import org.neo4j.storageengine.StoreFileClosedException;
import org.neo4j.storageengine.api.ClosedTransactionMetadata;
import org.neo4j.storageengine.api.ExternalStoreId;
import org.neo4j.storageengine.api.StoreId;
import org.neo4j.storageengine.api.TransactionId;
import org.neo4j.storageengine.api.TransactionIdStore;
import org.neo4j.storageengine.api.cursor.StoreCursors;
import org.neo4j.test.Race;
import org.neo4j.test.extension.EphemeralNeo4jLayoutExtension;
import org.neo4j.test.extension.Inject;
import org.neo4j.test.extension.pagecache.PageCacheSupportExtension;

import static java.lang.Math.toIntExact;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.neo4j.dbms.database.readonly.DatabaseReadOnlyChecker.writable;
import static org.neo4j.index.internal.gbptree.RecoveryCleanupWorkCollector.immediate;
import static org.neo4j.io.pagecache.context.CursorContext.NULL_CONTEXT;
import static org.neo4j.io.pagecache.context.EmptyVersionContextSupplier.EMPTY;
import static org.neo4j.kernel.impl.store.MetaDataStore.Position.STORE_VERSION;
import static org.neo4j.kernel.impl.store.format.RecordFormatSelector.defaultFormat;
import static org.neo4j.kernel.impl.transaction.log.LogTailMetadata.EMPTY_LOG_TAIL;
import static org.neo4j.storageengine.api.StoreVersion.versionStringToLong;
import static org.neo4j.storageengine.api.TransactionIdStore.BASE_TX_COMMIT_TIMESTAMP;
import static org.neo4j.test.Race.throwing;
import static org.neo4j.test.utils.PageCacheConfig.config;

@EphemeralNeo4jLayoutExtension
class MetaDataStoreTest
{
    @RegisterExtension
    static PageCacheSupportExtension pageCacheExtension = new PageCacheSupportExtension( config().withInconsistentReads( new AtomicBoolean() ) );
    @Inject
    private EphemeralFileSystemAbstraction fs;
    @Inject
    private DatabaseLayout databaseLayout;

    private PageCache pageCache;
    private boolean fakePageCursorOverflow;
    private PageCache pageCacheWithFakeOverflow;
    private final CursorContextFactory contextFactory = new CursorContextFactory( PageCacheTracer.NULL, EMPTY );

    @BeforeEach
    void setUp()
    {
        pageCache = pageCacheExtension.getPageCache( fs );
        fakePageCursorOverflow = false;
        pageCacheWithFakeOverflow = new DelegatingPageCache( pageCache )
        {
            @Override
            public PagedFile map( Path path, int pageSize, String databaseName,
                    ImmutableSet<OpenOption> openOptions, IOController ioController ) throws IOException
            {
                return new DelegatingPagedFile( super.map( path, pageSize, databaseName, openOptions, ioController ) )
                {
                    @Override
                    public PageCursor io( long pageId, int pf_flags, CursorContext context ) throws IOException
                    {
                        return new DelegatingPageCursor( super.io( pageId, pf_flags, context ) )
                        {
                            @Override
                            public boolean checkAndClearBoundsFlag()
                            {
                                return fakePageCursorOverflow | super.checkAndClearBoundsFlag();
                            }
                        };
                    }
                };
            }
        };
    }

    @AfterEach
    void tearDown()
    {
        pageCache.close();
    }

    @Test
    void getCreationTimeShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::getCreationTime );
    }

    @Test
    void getCurrentLogVersionShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::getCurrentLogVersion );
    }

    @Test
    void getLastClosedTransactionIdShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::getLastClosedTransactionId );
    }

    @Test
    void getLastClosedTransactionShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::getLastClosedTransaction );
    }

    @Test
    void getLastCommittedTransactionShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::getLastCommittedTransaction );
    }

    @Test
    void getLastCommittedTransactionIdShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::getLastCommittedTransactionId );
    }

    @Test
    void getLatestConstraintIntroducingTxShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::getLatestConstraintIntroducingTx );
    }

    @Test
    void getRandomNumberShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::getRandomNumber );
    }

    @Test
    void getStoreVersionShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::getStoreVersion );
    }

    @Test
    void nextCommittingTransactionIdShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, metaDataStore::nextCommittingTransactionId );
    }

    @Test
    void currentCommittingTransactionId()
    {
        try ( MetaDataStore metaDataStore = newMetaDataStore() )
        {
            metaDataStore.nextCommittingTransactionId();
            long lastCommittingTxId = metaDataStore.nextCommittingTransactionId();
            assertEquals( lastCommittingTxId, metaDataStore.committingTransactionId() );

            metaDataStore.nextCommittingTransactionId();
            metaDataStore.nextCommittingTransactionId();

            lastCommittingTxId = metaDataStore.nextCommittingTransactionId();
            assertEquals( lastCommittingTxId, metaDataStore.committingTransactionId() );
        }
    }

    @Test
    void setLastCommittedAndClosedTransactionIdShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class,
                () -> metaDataStore.setLastCommittedAndClosedTransactionId( 1, 2, BASE_TX_COMMIT_TIMESTAMP, 3, 4 ) );
    }

    @Test
    void setLastClosedTransactionFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, () -> metaDataStore.resetLastClosedTransaction( 1, 2, 3, 4, 5 ) );
    }

    @Test
    void setLastClosedTransactionOverridesLastClosedTransactionInformation()
    {
        try ( MetaDataStore metaDataStore = newMetaDataStore() )
        {
            metaDataStore.resetLastClosedTransaction( 3, 4, 5,  6, 7 );

            assertEquals( 3L, metaDataStore.getLastClosedTransactionId() );
            assertEquals( new ClosedTransactionMetadata( 3, new LogPosition( 4, 5 ), 6, 7 ), metaDataStore.getLastClosedTransaction() );
        }
    }

    @Test
    void transactionCommittedShouldFailWhenStoreIsClosed()
    {
        MetaDataStore metaDataStore = newMetaDataStore();
        metaDataStore.close();
        assertThrows( StoreFileClosedException.class, () -> metaDataStore.transactionCommitted( 1, 1, BASE_TX_COMMIT_TIMESTAMP ) );
    }

    @Test
    void testRecordTransactionClosed()
    {
        // GIVEN
        long version = 1L;
        long byteOffset = 777L;
        int checksum = 5252;
        long timestamp = 1234;
        long transactionId;
        try ( MetaDataStore metaDataStore = newMetaDataStore() )
        {
            transactionId = metaDataStore.getLastClosedTransaction().transactionId() + 1;

            // WHEN
            metaDataStore.transactionClosed( transactionId, version, byteOffset, checksum, timestamp );
            // long[] with the highest offered gap-free number and its meta data.
            var closedTransaction = metaDataStore.getLastClosedTransaction();

            //EXPECT
            LogPosition logPosition = closedTransaction.logPosition();
            assertEquals( version, logPosition.getLogVersion() );
            assertEquals( byteOffset, logPosition.getByteOffset() );
        }

        var tail = new EmptyLogTailMetadata()
        {
            @Override
            public LogPosition getLastTransactionLogPosition()
            {
                return new LogPosition( version, byteOffset );
            }

            @Override
            public TransactionId getLastCommittedTransaction()
            {
                return new TransactionId( transactionId, checksum, timestamp );
            }
        };

        try ( MetaDataStore metaDataStore = newMetaDataStore( tail ) )
        {
            var lastClosedTransaction = metaDataStore.getLastClosedTransaction();
            LogPosition logPosition = lastClosedTransaction.logPosition();
            assertEquals( version, logPosition.getLogVersion() );
            assertEquals( byteOffset, logPosition.getByteOffset() );
        }
    }

    @Test
    void incrementAndGetVersionMustBeAtomic() throws Throwable
    {
        try ( MetaDataStore store = newMetaDataStore() )
        {
            long initialVersion = store.incrementAndGetVersion();
            int threads = Runtime.getRuntime().availableProcessors();
            int iterations = 500;
            Race race = new Race();
            race.addContestants( threads, () ->
            {
                for ( int i = 0; i < iterations; i++ )
                {
                    store.incrementAndGetVersion();
                }
            } );
            race.go();
            assertThat( store.incrementAndGetVersion() ).isEqualTo( initialVersion + (threads * iterations) + 1 );
        }
    }

    @Test
    void transactionCommittedMustBeAtomic() throws Throwable
    {
        try ( MetaDataStore store = newMetaDataStore() )
        {
            store.transactionCommitted( 2, 2, 2 );
            AtomicLong writeCount = new AtomicLong();
            AtomicLong fileReadCount = new AtomicLong();
            AtomicLong apiReadCount = new AtomicLong();
            int upperLimit = 10_000;
            int lowerLimit = 100;
            long endTime = currentTimeMillis() + SECONDS.toMillis( 10 );

            Race race = new Race();
            race.withEndCondition( () -> writeCount.get() >= upperLimit &&
                    fileReadCount.get() >= upperLimit && apiReadCount.get() >= upperLimit );
            race.withEndCondition( () -> writeCount.get() >= lowerLimit &&
                    fileReadCount.get() >= lowerLimit && apiReadCount.get() >= lowerLimit &&
                    currentTimeMillis() >= endTime );
            race.addContestants( 3, () ->
            {
                long count = writeCount.incrementAndGet();
                store.transactionCommitted( count, (int) count, count );
            } );

            race.addContestants( 3, throwing( () ->
            {
                TransactionId transactionId = store.getLastCommittedTransaction();
                long id = transactionId.transactionId();
                long checksum = transactionId.checksum();
                assertIdEqualsChecksum( id, checksum, "file" );
                fileReadCount.incrementAndGet();
            } ) );

            race.addContestants( 3, () ->
            {
                TransactionId transaction = store.getLastCommittedTransaction();
                assertIdEqualsChecksum( transaction.transactionId(), transaction.checksum(), "API" );
                apiReadCount.incrementAndGet();
            } );

            race.go();
        }
    }

    @Test
    void transactionClosedMustBeAtomic() throws Throwable
    {
        try ( MetaDataStore store = newMetaDataStore() )
        {
            PagedFile pf = store.pagedFile;
            int initialValue = 2;
            store.transactionClosed( initialValue, initialValue, initialValue, initialValue, initialValue );
            AtomicLong writeCount = new AtomicLong();
            AtomicLong fileReadCount = new AtomicLong();
            AtomicLong apiReadCount = new AtomicLong();
            int upperLimit = 10_000;
            int lowerLimit = 100;
            long endTime = currentTimeMillis() + SECONDS.toMillis( 10 );

            Race race = new Race();
            race.withEndCondition( () -> writeCount.get() >= upperLimit &&
                    fileReadCount.get() >= upperLimit && apiReadCount.get() >= upperLimit );
            race.withEndCondition( () -> writeCount.get() >= lowerLimit &&
                    fileReadCount.get() >= lowerLimit && apiReadCount.get() >= lowerLimit &&
                    currentTimeMillis() >= endTime );
            race.addContestants( 3, () ->
            {
                long count = writeCount.incrementAndGet();
                store.transactionCommitted( count, (int) count, count );
            } );

            race.addContestants( 3, throwing( () ->
            {
                try ( PageCursor cursor = pf.io( 0, PagedFile.PF_SHARED_READ_LOCK, NULL_CONTEXT ) )
                {
                    assertTrue( cursor.next() );
                    LogPosition logPosition = store.getLastClosedTransaction().logPosition();
                    long logVersion = logPosition.getLogVersion();
                    long byteOffset = logPosition.getByteOffset();
                    assertLogVersionEqualsByteOffset( logVersion, byteOffset, "file" );
                    fileReadCount.incrementAndGet();
                }
            } ) );

            race.addContestants( 3, () ->
            {
                var transaction = store.getLastClosedTransaction();
                assertLogVersionEqualsByteOffset( transaction.transactionId(), transaction.logPosition().getLogVersion(), "API" );
                apiReadCount.incrementAndGet();
            } );
            race.go();
        }
    }

    @Test
    void mustSupportScanningAllRecords()
    {
        MetaDataStore.Position[] positions = MetaDataStore.Position.values();
        long storeVersion = versionStringToLong( defaultFormat().storeVersion() );
        try ( MetaDataStore store = newMetaDataStore() )
        {
            writeCorrectMetaDataRecord( store, positions, storeVersion );
            store.flush( NULL_CONTEXT );
        }

        List<Long> actualValues = new ArrayList<>();
        try ( MetaDataStore store = newMetaDataStore();
              var cursor = store.openPageCursorForReading( 0, NULL_CONTEXT ) )
        {
            store.scanAllRecords( record ->
            {
                actualValues.add( record.getValue() );
                return false;
            }, cursor );
        }

        List<Long> expectedValues = Arrays.stream( positions ).map( p ->
        {
            if ( p == STORE_VERSION )
            {
                return storeVersion;
            }
            else
            {
                return p.ordinal() + 1L;
            }
        } ).collect( Collectors.toList() );

        assertThat( actualValues ).isEqualTo( expectedValues );
    }

    private Path createMetaDataFile() throws IOException
    {
        Path file = databaseLayout.metadataStore();
        fs.write( file ).close();
        return file;
    }

    @Test
    void mustSupportScanningAllRecordsWithRecordCursor()
    {
        MetaDataStore.Position[] positions = MetaDataStore.Position.values();
        long storeVersion = versionStringToLong( defaultFormat().storeVersion());
        try ( MetaDataStore store = newMetaDataStore() )
        {
            writeCorrectMetaDataRecord( store, positions, storeVersion );
        }

        List<Long> actualValues = new ArrayList<>();
        try ( MetaDataStore store = newMetaDataStore() )
        {
            MetaDataRecord record = store.newRecord();
            try ( PageCursor cursor = store.openPageCursorForReading( 0, NULL_CONTEXT ) )
            {
                long highId = store.getHighId();
                for ( long id = 0; id < highId; id++ )
                {
                    store.getRecordByCursor( id, record, RecordLoad.NORMAL, cursor );
                    if ( record.inUse() )
                    {
                        actualValues.add( record.getValue() );
                    }
                }
            }
        }

        List<Long> expectedValues = Arrays.stream( positions ).map( p ->
        {
            if ( p == STORE_VERSION )
            {
                return storeVersion;
            }
            else
            {
                return p.ordinal() + 1L;
            }
        } ).collect( Collectors.toList() );

        assertThat( actualValues ).isEqualTo( expectedValues );
    }

    private static void writeCorrectMetaDataRecord( MetaDataStore store, MetaDataStore.Position[] positions, long storeVersion )
    {
        MetaDataRecord record = store.newRecord();
        for ( MetaDataStore.Position position : positions )
        {
            record.setId( position.id() );
            if ( position == STORE_VERSION )
            {
                record.initialize( true, storeVersion );
            }
            else
            {
                record.initialize( true, position.ordinal() + 1 );
            }
            try ( var storeCursor = store.openPageCursorForWriting( 0, NULL_CONTEXT ) )
            {
                store.updateRecord( record, storeCursor, NULL_CONTEXT, StoreCursors.NULL );
            }
        }
    }

    @Test
    void staticSetRecordMustThrowOnPageOverflow()
    {
        fakePageCursorOverflow = true;
        assertThrows( UnderlyingStorageException.class,
                () -> MetaDataStore.setRecord( pageCacheWithFakeOverflow, createMetaDataFile(), STORE_VERSION, 4242, databaseLayout.getDatabaseName(),
                        NULL_CONTEXT ) );
    }

    @Test
    void staticGetRecordMustThrowOnPageOverflow() throws Exception
    {
        Path metaDataFile = createMetaDataFile();
        MetaDataStore.setRecord( pageCacheWithFakeOverflow, metaDataFile, STORE_VERSION, 4242, databaseLayout.getDatabaseName(), NULL_CONTEXT );
        fakePageCursorOverflow = true;
        assertThrows( UnderlyingStorageException.class,
                () -> MetaDataStore.getRecord( pageCacheWithFakeOverflow, metaDataFile, STORE_VERSION, databaseLayout.getDatabaseName(), NULL_CONTEXT ) );

    }

    @Test
    void lastTxCommitTimestampShouldBeBaseInNewStore()
    {
        try ( MetaDataStore metaDataStore = newMetaDataStore() )
        {
            long timestamp = metaDataStore.getLastCommittedTransaction().commitTimestamp();
            assertThat( timestamp ).isEqualTo( TransactionIdStore.BASE_TX_COMMIT_TIMESTAMP );
        }
    }

    @Test
    void generateExternalStoreUUIDOnCreation()
    {
        try ( MetaDataStore metaDataStore = newMetaDataStore() )
        {
            var externalStoreId = metaDataStore.getExternalStoreId();
            var externalUUID = externalStoreId.orElseThrow().getId();
            assertThat( externalUUID.getLeastSignificantBits() ).isNotZero();
            assertThat( externalUUID.getMostSignificantBits() ).isNotZero();
        }
    }

    @Test
    void logRecordsMustIgnorePageOverflow()
    {
        try ( MetaDataStore store = newMetaDataStore() )
        {
            fakePageCursorOverflow = true;
            store.logRecords( s -> {} );
            fakePageCursorOverflow = false;
        }
    }

    @Test
    void throwsWhenClosed()
    {
        MetaDataStore store = newMetaDataStore();
        store.close();
        assertThrows( StoreFileClosedException.class, store::getLastCommittedTransactionId );
    }

    @Test
    void regenerateSetStoreId()
    {
        // given
        int creationTime = 1;
        int randomId = 2;
        long storeVersion = versionStringToLong( defaultFormat().storeVersion() );

        StoreId storeId = new StoreId( creationTime, randomId, storeVersion );
        UUID externalStoreUUID = UUID.randomUUID();
        try ( MetaDataStore store = newMetaDataStore() )
        {
            store.regenerateMetadata( storeId, externalStoreUUID, NULL_CONTEXT );
        }

        // then
        try ( MetaDataStore store = newMetaDataStore() )
        {
            assertEquals( creationTime, store.getCreationTime() );
            assertEquals( randomId, store.getRandomNumber() );
            assertEquals( storeVersion, store.getStoreVersion() );
        }
    }

    @Test
    void regenerateSetExternalStoreId()
    {
        UUID externalStoreId = UUID.randomUUID();
        try ( MetaDataStore store = newMetaDataStore() )
        {
            store.regenerateMetadata( store.getStoreId(), externalStoreId, NULL_CONTEXT );
        }

        try ( MetaDataStore store = newMetaDataStore() )
        {
            Optional<ExternalStoreId> retrievedExternalStoreIdOptional = store.getExternalStoreId();
            assertTrue( retrievedExternalStoreIdOptional.isPresent() );
            ExternalStoreId retrievedExternalStoreId = retrievedExternalStoreIdOptional.get();
            assertEquals( externalStoreId, retrievedExternalStoreId.getId() );
        }
    }

    @Test
    void tracePageCacheAccessOnStoreInitialisation()
    {
        var pageCacheTracer = new DefaultPageCacheTracer();
        CursorContextFactory contextFactory = new CursorContextFactory( pageCacheTracer, EMPTY );
        try ( MetaDataStore ignored = newMetaDataStore( contextFactory ) )
        {
            assertThat( pageCacheTracer.faults() ).isOne();
            assertThat( pageCacheTracer.pins() ).isEqualTo( 2 );
            assertThat( pageCacheTracer.unpins() ).isEqualTo( 2 );
            assertThat( pageCacheTracer.hits() ).isEqualTo( 1 );
        }
    }

    @Test
    void tracePageCacheAccessOnSetRecord() throws IOException
    {
        var contextFactory = new CursorContextFactory( new DefaultPageCacheTracer(), EMPTY );
        var cursorContext = contextFactory.create( "tracePageCacheAccessOnSetRecord" );
        try ( var metaDataStore = newMetaDataStore() )
        {
            MetaDataStore.setRecord( pageCache, metaDataStore.getStorageFile(), MetaDataStore.Position.RANDOM_NUMBER, 3, databaseLayout.getDatabaseName(),
                    cursorContext );

            PageCursorTracer cursorTracer = cursorContext.getCursorTracer();
            assertThat( cursorTracer.pins() ).isOne();
            assertThat( cursorTracer.unpins() ).isOne();
            assertThat( cursorTracer.hits() ).isOne();
        }
    }

    @Test
    void tracePageCacheAccessOnGetRecord() throws IOException
    {
        var contextFactory = new CursorContextFactory( new DefaultPageCacheTracer(), EMPTY );
        var cursorContext = contextFactory.create( "tracePageCacheAccessOnGetRecord" );
        try ( var metaDataStore = newMetaDataStore() )
        {
            MetaDataStore.getRecord( pageCache, metaDataStore.getStorageFile(), MetaDataStore.Position.RANDOM_NUMBER, databaseLayout.getDatabaseName(),
                    cursorContext );

            PageCursorTracer cursorTracer = cursorContext.getCursorTracer();
            assertThat( cursorTracer.pins() ).isOne();
            assertThat( cursorTracer.unpins() ).isOne();
            assertThat( cursorTracer.hits() ).isOne();
        }
    }

    @Test
    void tracePageCacheAssessOnGetStoreId() throws IOException
    {
        var contextFactory = new CursorContextFactory( new DefaultPageCacheTracer(), EMPTY );
        var cursorContext = contextFactory.create( "tracePageCacheAssessOnGetStoreId" );
        try ( var metaDataStore = newMetaDataStore() )
        {
            MetaDataStore.getStoreId( pageCache, metaDataStore.getStorageFile(), databaseLayout.getDatabaseName(), cursorContext );

            PageCursorTracer cursorTracer = cursorContext.getCursorTracer();
            assertThat( cursorTracer.pins() ).isEqualTo( 3 );
            assertThat( cursorTracer.unpins() ).isEqualTo( 3 );
            assertThat( cursorTracer.hits() ).isEqualTo( 3 );
        }
    }

    @Test
    void tracePageCacheAssessOnRegenerate()
    {
        var contextFactory = new CursorContextFactory( new DefaultPageCacheTracer(), EMPTY );
        var cursorContext = contextFactory.create( "tracePageCacheAssessOnSetStoreId" );
        try ( var metaDataStore = newMetaDataStore() )
        {
            var storeId = new StoreId( 1, 2, 3 );
            metaDataStore.regenerateMetadata( storeId, metaDataStore.getExternalStoreId().orElseThrow().getId(), cursorContext );
            PageCursorTracer cursorTracer = cursorContext.getCursorTracer();
            assertThat( cursorTracer.pins() ).isEqualTo( 2 );
            assertThat( cursorTracer.unpins() ).isEqualTo( 2 );
            assertThat( cursorTracer.hits() ).isEqualTo( 2 );
        }
    }

    @Test
    void accessCheckpointLogVersion()
    {
        try ( var dataStore = newMetaDataStore() )
        {
            assertEquals( 0, dataStore.getCheckpointLogVersion() );
            assertEquals( 1, dataStore.incrementAndGetCheckpointLogVersion() );
            assertEquals( 2, dataStore.incrementAndGetCheckpointLogVersion() );
            assertEquals( 3, dataStore.incrementAndGetCheckpointLogVersion() );
            assertEquals( 4, dataStore.incrementAndGetCheckpointLogVersion() );
            assertEquals( 5, dataStore.incrementAndGetCheckpointLogVersion() );
            assertEquals( 5, dataStore.getCheckpointLogVersion() );
            assertEquals( 0, dataStore.getCurrentLogVersion() );
        }
    }

    @Test
    void checkSetCheckpointLogVersion()
    {
        try ( var dataStore = newMetaDataStore() )
        {
            assertEquals( 0, dataStore.getCheckpointLogVersion() );
            dataStore.setCheckpointLogVersion( 123 );
            assertEquals( 123, dataStore.getCheckpointLogVersion() );

            dataStore.setCheckpointLogVersion( 321 );
            assertEquals( 321, dataStore.getCheckpointLogVersion() );
            assertEquals( 0, dataStore.getCurrentLogVersion() );
        }
    }

    @Test
    void shouldBeAbleToReadAndWriteKernelVersion()
    {
        try ( MetaDataStore metaDataStore = newMetaDataStore() )
        {
            assertThat( metaDataStore.kernelVersion() )
                    .isEqualTo( KernelVersion.LATEST ); //new store should have the latest kernel version that is not the same as earliest
            assertThat( metaDataStore.kernelVersion() ).isNotEqualTo( KernelVersion.EARLIEST );
            metaDataStore.setKernelVersion( KernelVersion.EARLIEST ); //so we set it
            assertEquals( KernelVersion.EARLIEST, metaDataStore.kernelVersion() );
        }

        var logTail = new EmptyLogTailMetadata()
        {
            @Override
            public KernelVersion getKernelVersion()
            {
                return KernelVersion.EARLIEST;
            }
        };
        try ( MetaDataStore metaDataStore = newMetaDataStore( logTail ) )
        {
            assertThat( metaDataStore.kernelVersion() ).isEqualTo( KernelVersion.EARLIEST ); //and can read it after a restart
        }
    }

    @Test
    void shouldBeAbleToReadAndWriteDatabaseIdUuid()
    {
        // given
        var databaseIdUuid = UUID.randomUUID();

        // when
        try ( MetaDataStore store = newMetaDataStore() )
        {
            store.setDatabaseIdUuid( databaseIdUuid, NULL_CONTEXT );
        }

        //then
        try ( MetaDataStore store = newMetaDataStore() )
        {
            var storedDatabaseId = store.getDatabaseIdUuid( NULL_CONTEXT );
            assertThat( storedDatabaseId ).hasValue( databaseIdUuid );
        }
    }

    @Test
    void shouldReturnEmptyIfDatabaseIdHasNeverBeenSet()
    {
        // given
        try ( MetaDataStore store = newMetaDataStore() )
        {
            // when
            var storeDatabaseId = store.getDatabaseIdUuid( NULL_CONTEXT );

            // then
            assertThat( storeDatabaseId ).isEmpty();
        }
    }

    @Test
    void shouldLoadAllFieldsOnOpen()
    {
        // given
        var pageCacheTracer = new DefaultPageCacheTracer();
        var contextFactory = new CursorContextFactory( pageCacheTracer, EMPTY );
        try ( var store = newMetaDataStore( contextFactory ) )
        {
            var pinsBefore = pageCacheTracer.pins();
            readAllFields( store );
            var pinsAfter = pageCacheTracer.pins();
            assertThat( pinsAfter ).isEqualTo( pinsBefore );
        }
    }

    private void readAllFields( MetaDataStore store )
    {
        store.getStoreVersion();
        store.getLastCommittedTransaction();
        store.getLastClosedTransaction();
        store.getCreationTime();
        store.getCheckpointLogVersion();
        store.getRandomNumber();
        store.getLatestConstraintIntroducingTx();
        store.getExternalStoreId();
        store.getStoreId();
        // getDatabaseIdUuid actually reads from store, but must not refresh fields
        store.getDatabaseIdUuid( NULL_CONTEXT );
    }

    @Test
    void shouldGenerateCorrectTransactionIdSequenceWhenConcurrentlyReadingOtherFields() throws IOException
    {
        // given an existing meta data store with its external store ID UUID set to uninitialized (as if the db was from an older version)
        try ( var metaDataStore = newMetaDataStore() )
        {
            metaDataStore.regenerateMetadata( metaDataStore.getStoreId(), MetaDataStore.NOT_INITIALIZED_UUID, NULL_CONTEXT );
        }
        try ( var store = newMetaDataStore() )
        {
            var numCommitted = new AtomicLong();
            var race = new Race().withEndCondition( () -> numCommitted.get() >= 10_000 );
            var numCommitters = 4;
            var bitSets = new BitSet[numCommitters];
            for ( int i = 0; i < numCommitters; i++ )
            {
                bitSets[i] = new BitSet();
            }
            race.addContestants( numCommitters, c -> () ->
            {
                var nextTxId = store.nextCommittingTransactionId();
                bitSets[c].set( toIntExact( nextTxId ) );
                numCommitted.incrementAndGet();
            } );
            race.addContestants( 2, () -> readAllFields( store ) );
            race.goUnchecked();

            var combined = new BitSet();
            var expectedCardinality = 0;
            for ( int i = 0; i < numCommitters; i++ )
            {
                combined.or( bitSets[i] );
                expectedCardinality += bitSets[i].cardinality();
            }
            assertThat( combined.cardinality() ).isEqualTo( expectedCardinality );
        }
    }

    private static void assertLogVersionEqualsByteOffset( long logVersion, long byteOffset, String source )
    {
        if ( logVersion != byteOffset )
        {
            throw new AssertionError(
                    "logVersion (" + logVersion + ") and byteOffset (" + byteOffset + ") from " + source +
                            " should be identical" );
        }
    }

    private static void assertIdEqualsChecksum( long id, long checksum, String source )
    {
        if ( id != checksum )
        {
            throw new AssertionError( "id (" + id + ") and checksum (" + checksum + ") from " + source + " should be identical" );
        }
    }

    private MetaDataStore newMetaDataStore()
    {
        return newMetaDataStore( contextFactory );
    }

    private MetaDataStore newMetaDataStore( CursorContextFactory contextFactory )
    {
        return newMetaDataStore( contextFactory, EMPTY_LOG_TAIL );
    }

    private MetaDataStore newMetaDataStore( LogTailMetadata logTail )
    {
        return newMetaDataStore( contextFactory, logTail );
    }

    private MetaDataStore newMetaDataStore( CursorContextFactory contextFactory, LogTailMetadata logTail )
    {
        InternalLogProvider logProvider = NullLogProvider.getInstance();
        StoreFactory storeFactory =
                new StoreFactory( databaseLayout, Config.defaults(), new DefaultIdGeneratorFactory( fs, immediate(), databaseLayout.getDatabaseName() ),
                        pageCacheWithFakeOverflow, fs, logProvider, contextFactory, writable(), logTail );
        return storeFactory.openNeoStores( true, StoreType.META_DATA ).getMetaDataStore();
    }
}
