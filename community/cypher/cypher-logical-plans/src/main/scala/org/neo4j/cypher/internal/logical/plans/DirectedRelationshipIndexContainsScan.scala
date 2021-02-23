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
package org.neo4j.cypher.internal.logical.plans

import org.neo4j.cypher.internal.expressions.Expression
import org.neo4j.cypher.internal.expressions.RelationshipTypeToken
import org.neo4j.cypher.internal.util.attribution.IdGen
import org.neo4j.cypher.internal.util.attribution.SameId

/**
 * This operator does a full scan of an index, producing rows for all entries that contain a string value.
 *
 * Given each found `relationship`, the rows will have the following structure:
 *
 *  - `{idName: relationship, startNode: relationship.startNode, endNode: relationship.endNode}`
 */
case class DirectedRelationshipIndexContainsScan(idName: String,
                                                 startNode: String,
                                                 endNode: String,
                                                 override val typeToken: RelationshipTypeToken,
                                                 property: IndexedProperty,
                                                 valueExpr: Expression,
                                                 argumentIds: Set[String],
                                                 indexOrder: IndexOrder)
                                                (implicit idGen: IdGen)
  extends RelationshipIndexLeafPlan(idGen) {

  override def properties: Seq[IndexedProperty] = Seq(property)

  val availableSymbols: Set[String] = argumentIds ++ Set(idName, leftNode, rightNode)

  override def usedVariables: Set[String] = valueExpr.dependencies.map(_.name)

  override def withoutArgumentIds(argsToExclude: Set[String]): DirectedRelationshipIndexContainsScan = copy(argumentIds = argumentIds -- argsToExclude)(SameId(this.id))

  override def copyWithoutGettingValues: DirectedRelationshipIndexContainsScan =
    DirectedRelationshipIndexContainsScan(idName, leftNode, rightNode, typeToken, IndexedProperty(property.propertyKeyToken, DoNotGetValue), valueExpr, argumentIds, indexOrder)(SameId(this.id))

  override def withMappedProperties(f: IndexedProperty => IndexedProperty): RelationshipIndexLeafPlan =
    DirectedRelationshipIndexContainsScan(idName, leftNode, rightNode, typeToken, f(property), valueExpr, argumentIds, indexOrder)(SameId(this.id))

  override def leftNode: String = startNode

  override def rightNode: String = endNode
}
