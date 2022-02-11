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
package org.neo4j.cypher.internal.logical.builder

import org.neo4j.cypher.internal.ast.UnresolvedCall
import org.neo4j.cypher.internal.ast.factory.neo4j.JavaccRule
import org.neo4j.cypher.internal.expressions.CachedHasProperty
import org.neo4j.cypher.internal.expressions.CachedProperty
import org.neo4j.cypher.internal.expressions.ContainerIndex
import org.neo4j.cypher.internal.expressions.Expression
import org.neo4j.cypher.internal.expressions.NODE_TYPE
import org.neo4j.cypher.internal.expressions.Property
import org.neo4j.cypher.internal.expressions.PropertyKeyName
import org.neo4j.cypher.internal.expressions.RELATIONSHIP_TYPE
import org.neo4j.cypher.internal.expressions.Variable
import org.neo4j.cypher.internal.frontend.phases.rewriting.cnf.flattenBooleanOperators
import org.neo4j.cypher.internal.util.ASTNode
import org.neo4j.cypher.internal.util.Rewriter
import org.neo4j.cypher.internal.util.inSequence
import org.neo4j.cypher.internal.util.topDown

object Parser {
  val injectCachedProperties: Rewriter = topDown(Rewriter.lift {
    case ContainerIndex(Variable(name), Property(v@Variable(node), pkn:PropertyKeyName)) if name == "cache" || name == "cacheN" =>
      CachedProperty(node, v, pkn, NODE_TYPE)(AbstractLogicalPlanBuilder.pos)
    case ContainerIndex(Variable(name), Property(v@Variable(node), pkn:PropertyKeyName)) if name == "cacheFromStore" || name == "cacheNFromStore" =>
      CachedProperty(node, v, pkn, NODE_TYPE, knownToAccessStore = true)(AbstractLogicalPlanBuilder.pos)
    case ContainerIndex(Variable("cacheR"), Property(v@Variable(relationship), pkn:PropertyKeyName)) =>
      CachedProperty(relationship, v, pkn, RELATIONSHIP_TYPE)(AbstractLogicalPlanBuilder.pos)
    case ContainerIndex(Variable("cacheRFromStore"), Property(v@Variable(relationship), pkn:PropertyKeyName)) =>
      CachedProperty(relationship, v, pkn, RELATIONSHIP_TYPE, knownToAccessStore = true)(AbstractLogicalPlanBuilder.pos)
    case ContainerIndex(Variable(name), Property(v@Variable(node), pkn:PropertyKeyName)) if name == "cacheNHasProperty" =>
      CachedHasProperty(node, v, pkn, NODE_TYPE)(AbstractLogicalPlanBuilder.pos)
    case ContainerIndex(Variable(name), Property(v@Variable(rel), pkn:PropertyKeyName)) if name == "cacheRHasProperty" =>
      CachedHasProperty(rel, v, pkn, RELATIONSHIP_TYPE)(AbstractLogicalPlanBuilder.pos)
    case ContainerIndex(Variable(name), Property(v@Variable(node), pkn:PropertyKeyName)) if name == "cacheNHasPropertyFromStore" =>
      CachedHasProperty(node, v, pkn, NODE_TYPE, knownToAccessStore = true)(AbstractLogicalPlanBuilder.pos)
    case ContainerIndex(Variable(name), Property(v@Variable(rel), pkn:PropertyKeyName)) if name == "cacheRHasPropertyFromStore" =>
      CachedHasProperty(rel, v, pkn, RELATIONSHIP_TYPE, knownToAccessStore = true)(AbstractLogicalPlanBuilder.pos)

  })
  val invalidateInputPositions: Rewriter = topDown(Rewriter.lift {
    case a:ASTNode => a.dup(a.treeChildren.toSeq :+ AbstractLogicalPlanBuilder.pos)
  })

  def cleanup[T <: ASTNode](in: T): T = inSequence(
    injectCachedProperties,
    invalidateInputPositions,
    flattenBooleanOperators // It is otherwise impossible to create instances of Ands / Ors
  )(in).asInstanceOf[T]


  private val regex = s"(.+) [Aa][Ss] (.+)".r
  private val parser = new Parser

  def parseProjections(projections: String*): Map[String, Expression] = {
    projections.map {
      case regex(Parser(expression), VariableParser(alias)) => (alias, expression)
      case x => throw new IllegalArgumentException(s"'$x' cannot be parsed as a projection")
    }.toMap
  }

  def parseExpression(text: String): Expression = parser.parseExpression(text)

  def parseProcedureCall(text: String): UnresolvedCall = parser.parseProcedureCall(text)

  def unapply(arg: String): Option[Expression] = Some(parser.parseExpression(arg))
}

private class Parser {

  def parseExpression(text: String): Expression = {
    val expression = JavaccRule.Expression.apply(text)
    Parser.cleanup(expression)
  }

  def parseProcedureCall(text: String): UnresolvedCall = {
    val clause = JavaccRule.CallClause.apply(s"CALL $text")
    clause match {
      case u:UnresolvedCall => Parser.cleanup(u)
      case c => throw new IllegalArgumentException(s"Expected UnresolvedCall but got: $c")
    }
  }
}
