/*
 * Copyright (c) 2002-2019 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.neo4j.cypher.internal.compatibility.v3_3

import org.neo4j.cypher.internal.frontend.v3_3.phases.CacheCheckResult

trait CacheAccessor[K <: AnyRef, T <: AnyRef] {
  def getOrElseUpdate(cache: LFUCache[K, T])(key: K, f: => T): T
  def remove(cache: LFUCache[K, T])(key: K, userKey: String, secondsSinceReplan: Int)
}

class QueryCache[K <: AnyRef, T <: AnyRef](cacheAccessor: CacheAccessor[K, T], cache: LFUCache[K, T]) {
  def getOrElseUpdate(key: K, userKey: String, isStale: T => CacheCheckResult, produce: => T): (T, Boolean) = {
    if (cache.size == 0)
      (produce, false)
    else {
      var planned = false
      Iterator.continually {
        cacheAccessor.getOrElseUpdate(cache)(key, {
          planned = true
          produce
        })
      }.flatMap { value =>
        if (!planned) {
          val cacheCheck = isStale(value)
          if (cacheCheck.isStale) {
            cacheAccessor.remove(cache)(key, userKey, cacheCheck.secondsSinceReplan)
            None
          } else {
            Some((value, planned))
          }
        }
        else {
          Some((value, planned))
        }
      }.next()
    }
  }
}

class MonitoringCacheAccessor[K <: AnyRef, T <: AnyRef](monitor: CypherCacheHitMonitor[K]) extends CacheAccessor[K, T] {

  override def getOrElseUpdate(cache: LFUCache[K, T])(key: K, f: => T) = {
    var updated = false
    val value = cache(key, {
      updated = true
      f
    })

    if (updated)
      monitor.cacheMiss(key)
    else
      monitor.cacheHit(key)

    value
  }

  def remove(cache: LFUCache[K, T])(key: K, userKey: String, secondsSinceReplan: Int): Unit = {
    cache.remove(key)
    monitor.cacheDiscard(key, userKey, secondsSinceReplan)
  }
}
