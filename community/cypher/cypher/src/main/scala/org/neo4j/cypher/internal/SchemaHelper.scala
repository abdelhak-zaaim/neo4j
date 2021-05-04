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
package org.neo4j.cypher.internal

import org.neo4j.common.EntityType
import org.neo4j.internal.schema.SchemaDescriptor
import org.neo4j.kernel.impl.api.SchemaStateKey
import org.neo4j.kernel.impl.query.TransactionalContext

import java.util.concurrent.atomic.AtomicLong

case class SchemaToken(x: Long) extends AnyVal

class SchemaHelper(val queryCache: QueryCache[_,_,_]) {

  private val schemaToken = new AtomicLong()
  private val schemaStateKey = SchemaStateKey.newKey()
  private val creator =
    new java.util.function.Function[SchemaStateKey, SchemaToken]() {
      def apply(key: SchemaStateKey): SchemaToken = {
        queryCache.clear()
        SchemaToken(schemaToken.incrementAndGet())
      }
    }

  def readSchemaToken(tc: TransactionalContext): SchemaToken = {
    tc.kernelTransaction().schemaRead().schemaStateGetOrCreate(schemaStateKey, creator)
  }

  def lockLabels(schemaTokenBefore: SchemaToken,
                 executionPlan: ExecutableQuery,
                 tc: TransactionalContext): LockedLabels = {
    val lookupTypes: Array[EntityType] = executionPlan.lookupEntityTypes

    // Need to lock lookup index before checking if it exists in order to not have a race with drop index.
    lookupTypes.foreach(lockLookupType(tc, _))
    if (lookupTypes.exists(!hasLookupIndex(tc, _))) {
      lookupTypes.foreach(releaseLookupType(tc, _))
      // lookup index for entity type has been dropped -> need to replan.
      return LockedLabels(successful = false, needsReplan = true)
    }

    val labelIds: Array[Long] = executionPlan.labelIdsOfUsedIndexes
    if (labelIds.nonEmpty) {
      lockPlanLabels(tc, labelIds)
    }

    if (lookupTypes.nonEmpty || labelIds.nonEmpty) {
      val schemaTokenAfter = readSchemaToken(tc)

      // if the schema has changed while taking all locks we release locks and return false
      if (schemaTokenBefore != schemaTokenAfter) {
        releasePlanLabels(tc, labelIds)
        lookupTypes.foreach(releaseLookupType(tc, _))
        return LockedLabels(successful = false, needsReplan = false)
      }
    }
    LockedLabels(successful = true, needsReplan = false)
  }

  private def releasePlanLabels(tc: TransactionalContext, labelIds: Array[Long]): Unit =
    tc.kernelTransaction.locks().releaseSharedLabelLock(labelIds:_*)

  private def lockPlanLabels(tc: TransactionalContext, labelIds: Array[Long]): Unit =
    tc.kernelTransaction.locks().acquireSharedLabelLock(labelIds:_*)

  private def lockLookupType(tc: TransactionalContext, entityType: EntityType): Unit =
    tc.kernelTransaction.locks().acquireSharedLookupLock( entityType )

  private def releaseLookupType(tc: TransactionalContext, entityType: EntityType): Unit =
    tc.kernelTransaction.locks().releaseSharedLookupLock( entityType )

  private def hasLookupIndex(tc: TransactionalContext, entityType: EntityType): Boolean =
    !tc.kernelTransaction().schemaRead.scanStoreAsTokenIndexEnabled || tc.kernelTransaction.schemaRead().indexForSchemaNonTransactional(SchemaDescriptor.forAnyEntityTokens(entityType)).hasNext

  case class LockedLabels(successful: Boolean, needsReplan: Boolean)
}
