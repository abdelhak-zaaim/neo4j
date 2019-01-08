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
package org.neo4j.cypher.internal.compiler.v3_3.planner

import org.neo4j.csv.reader.Configuration
import org.neo4j.cypher.internal.compiler.v3_3._
import org.neo4j.cypher.internal.compiler.v3_3.ast.rewriters._
import org.neo4j.cypher.internal.compiler.v3_3.phases._
import org.neo4j.cypher.internal.compiler.v3_3.planner.logical.Metrics._
import org.neo4j.cypher.internal.compiler.v3_3.planner.logical.cardinality.QueryGraphCardinalityModel
import org.neo4j.cypher.internal.compiler.v3_3.planner.logical.idp._
import org.neo4j.cypher.internal.compiler.v3_3.planner.logical.plans.rewriter.unnestApply
import org.neo4j.cypher.internal.compiler.v3_3.planner.logical.steps.LogicalPlanProducer
import org.neo4j.cypher.internal.compiler.v3_3.planner.logical.{LogicalPlanningContext, _}
import org.neo4j.cypher.internal.compiler.v3_3.spi._
import org.neo4j.cypher.internal.compiler.v3_3.test_helpers.ContextHelper
import org.neo4j.cypher.internal.frontend.v3_3.ast._
import org.neo4j.cypher.internal.frontend.v3_3.ast.rewriters._
import org.neo4j.cypher.internal.frontend.v3_3.helpers.fixedPoint
import org.neo4j.cypher.internal.frontend.v3_3.helpers.rewriting.RewriterStepSequencer
import org.neo4j.cypher.internal.frontend.v3_3.helpers.rewriting.RewriterStepSequencer.newPlain
import org.neo4j.cypher.internal.frontend.v3_3.parser.CypherParser
import org.neo4j.cypher.internal.frontend.v3_3.phases._
import org.neo4j.cypher.internal.frontend.v3_3.test_helpers.{CypherFunSuite, CypherTestSupport}
import org.neo4j.cypher.internal.frontend.v3_3.{Foldable, PropertyKeyId, SemanticTable}
import org.neo4j.cypher.internal.ir.v3_3._
import org.neo4j.cypher.internal.v3_3.logical.plans.{LogicalPlan, ProduceResult}
import org.neo4j.helpers.collection.Visitable
import org.neo4j.kernel.impl.util.dbstructure.DbStructureVisitor
import org.scalatest.matchers.{BeMatcher, MatchResult}

import scala.language.reflectiveCalls
import scala.reflect.ClassTag

trait LogicalPlanningTestSupport2 extends CypherTestSupport with AstConstructionTestSupport {
  self: CypherFunSuite =>

  val solved = CardinalityEstimation.lift(PlannerQuery.empty, Cardinality(0))
  var parser = new CypherParser
  val rewriterSequencer = RewriterStepSequencer.newValidating _
  var astRewriter = new ASTRewriter(rewriterSequencer, literalExtraction = Never, getDegreeRewriting = true)
  final var planner = new QueryPlanner() {
    def internalPlan(query: PlannerQuery)(implicit context: LogicalPlanningContext, leafPlan: Option[LogicalPlan] = None): LogicalPlan =
      planSingleQuery(query)
  }
  var queryGraphSolver: QueryGraphSolver = new IDPQueryGraphSolver(SingleComponentPlanner(mock[IDPQueryGraphSolverMonitor]), cartesianProductsOrValueJoins, mock[IDPQueryGraphSolverMonitor])
  val cypherCompilerConfig = CypherCompilerConfiguration(
    queryCacheSize = 100,
    statsDivergenceCalculator = StatsDivergenceCalculator.divergenceNoDecayCalculator(0.5, 1000),
    useErrorsOverWarnings = false,
    idpMaxTableSize = DefaultIDPSolverConfig.maxTableSize,
    idpIterationDuration = DefaultIDPSolverConfig.iterationDurationLimit,
    errorIfShortestPathFallbackUsedAtRuntime = false,
    errorIfShortestPathHasCommonNodesAtRuntime = true,
    legacyCsvQuoteEscaping = false,
    csvBufferSize = Configuration.DEFAULT_BUFFER_SIZE_4MB,
    nonIndexedLabelWarningThreshold = 10000,
    planWithMinimumCardinalityEstimates = false
  )
  val realConfig = new RealLogicalPlanningConfiguration(cypherCompilerConfig)
  def solvedWithEstimation(cardinality: Cardinality) = CardinalityEstimation.lift(PlannerQuery.empty, cardinality)

  implicit class LogicalPlanningEnvironment[C <: LogicalPlanningConfiguration](config: C) {
    lazy val semanticTable = config.updateSemanticTableWithTokens(SemanticTable())

    def metricsFactory = new MetricsFactory {
      def newCostModel(ignore: CypherCompilerConfiguration) =
        (plan: LogicalPlan, input: QueryGraphSolverInput) => config.costModel()(plan -> input)

      def newCardinalityEstimator(queryGraphCardinalityModel: QueryGraphCardinalityModel, evaluator: ExpressionEvaluator) =
        config.cardinalityModel(queryGraphCardinalityModel, mock[ExpressionEvaluator])

      def newQueryGraphCardinalityModel(statistics: GraphStatistics) = QueryGraphCardinalityModel.default(statistics)
    }

    def table = Map.empty[PatternExpression, QueryGraph]

    def planContext = new NotImplementedPlanContext {
      override def statistics: GraphStatistics =
        config.graphStatistics

      override def indexesGetForLabel(labelId: Int): Iterator[IndexDescriptor] = {
        val label = config.labelsById(labelId)
        config.indexes.filter(p => p._1 == label).flatMap(p => indexGet(p._1, p._2)).iterator
      }

      override def uniqueIndexesGetForLabel(labelId: Int): Iterator[IndexDescriptor] = {
        val label = config.labelsById(labelId)
        config.uniqueIndexes.filter(p => p._1 == label).flatMap(p => uniqueIndexGet(p._1, p._2)).iterator
      }

      override def uniqueIndexGet(labelName: String, propertyKeys: Seq[String]): Option[IndexDescriptor] =
        if (config.uniqueIndexes((labelName, propertyKeys)))
          Some(IndexDescriptor(
            semanticTable.resolvedLabelIds(labelName).id,
            propertyKeys.map(semanticTable.resolvedPropertyKeyNames(_).id)
          ))
        else
          None

      override def indexGet(labelName: String, propertyKeys: Seq[String]): Option[IndexDescriptor] =
        if (config.indexes((labelName, propertyKeys)) || config.uniqueIndexes((labelName, propertyKeys)))
          Some(IndexDescriptor(
            semanticTable.resolvedLabelIds(labelName).id,
            propertyKeys.map(semanticTable.resolvedPropertyKeyNames(_).id)
          ))
        else
          None

      override def indexExistsForLabel(labelName: String): Boolean =
        config.indexes.exists(_._1 == labelName) || config.uniqueIndexes.exists(_._1 == labelName)

      override def getOptPropertyKeyId(propertyKeyName: String) =
        semanticTable.resolvedPropertyKeyNames.get(propertyKeyName).map(_.id)

      override def getOptLabelId(labelName: String): Option[Int] =
        semanticTable.resolvedLabelIds.get(labelName).map(_.id)

      override def getOptRelTypeId(relType: String): Option[Int] =
        semanticTable.resolvedRelTypeNames.get(relType).map(_.id)
    }

    val pipeLine =
      Parsing andThen
      PreparatoryRewriting andThen
      SemanticAnalysis(warn = true) andThen
      AstRewriting(newPlain, literalExtraction = Never) andThen
      RewriteProcedureCalls andThen
      Namespacer andThen
      rewriteEqualityToInPredicate andThen
      CNFNormalizer andThen
      LateAstRewriting andThen
      ResolveTokens andThen
      CreatePlannerQuery andThen
      OptionalMatchRemover andThen
      QueryPlanner().adds(CompilationContains[LogicalPlan]) andThen
      Do(removeApply _)

    // This fakes pattern expression naming for testing purposes
    // In the actual code path, this renaming happens as part of planning
    //
    // cf. QueryPlanningStrategy
    //
    private def namePatternPredicates(input: LogicalPlanState, context: CompilerContext): LogicalPlanState = {
      val newStatement = input.statement.endoRewrite(namePatternPredicatePatternElements)
      input.copy(maybeStatement = Some(newStatement))
    }

    private def removeApply(input: LogicalPlanState, context: CompilerContext): LogicalPlanState = {
      val newPlan = input.logicalPlan.endoRewrite(fixedPoint(unnestApply))
      input.copy(maybeLogicalPlan = Some(newPlan))
    }

    def getLogicalPlanFor(queryString: String): (Option[PeriodicCommit], LogicalPlan, SemanticTable) = {
      val mkException = new SyntaxExceptionCreator(queryString, Some(pos))
      val metrics = metricsFactory.newMetrics(planContext.statistics, mock[ExpressionEvaluator], cypherCompilerConfig)
      def context = ContextHelper.create(planContext = planContext,
        exceptionCreator = mkException,
        queryGraphSolver = queryGraphSolver,
        metrics = metrics,
        config = cypherCompilerConfig)

      val state = LogicalPlanState(queryString, None, IDPPlannerName)
      val output = pipeLine.transform(state, context)
      val logicalPlan = output.logicalPlan.asInstanceOf[ProduceResult].inner
      (output.periodicCommit, logicalPlan, output.semanticTable())
    }

    def estimate(qg: QueryGraph, input: QueryGraphSolverInput = QueryGraphSolverInput.empty) =
      metricsFactory.
        newMetrics(config.graphStatistics, mock[ExpressionEvaluator], cypherCompilerConfig).
        queryGraphCardinalityModel(qg, input, semanticTable)

    def withLogicalPlanningContext[T](f: (C, LogicalPlanningContext) => T): T = {
      val metrics = metricsFactory.newMetrics(config.graphStatistics, mock[ExpressionEvaluator], cypherCompilerConfig)
      val logicalPlanProducer = LogicalPlanProducer(metrics.cardinality)
      val ctx = LogicalPlanningContext(
        planContext = planContext,
        logicalPlanProducer = logicalPlanProducer,
        metrics = metrics,
        semanticTable = semanticTable,
        strategy = queryGraphSolver,
        input = QueryGraphSolverInput.empty,
        notificationLogger = devNullLogger
      )
      f(config, ctx)
    }
  }

  def fakeLogicalPlanFor(id: String*): FakePlan = FakePlan(id.toSet)(solved)

  def planFor(queryString: String): (Option[PeriodicCommit], LogicalPlan, SemanticTable) =
    new given().getLogicalPlanFor(queryString)

  class given extends StubbedLogicalPlanningConfiguration(realConfig)

  class givenPlanWithMinimumCardinalityEnabled
    extends StubbedLogicalPlanningConfiguration(RealLogicalPlanningConfiguration(cypherCompilerConfig.copy(planWithMinimumCardinalityEstimates = true)))

  class fromDbStructure(dbStructure: Visitable[DbStructureVisitor])
    extends DelegatingLogicalPlanningConfiguration(DbStructureLogicalPlanningConfiguration(cypherCompilerConfig)(dbStructure))

  implicit def propertyKeyId(label: String)(implicit semanticTable: SemanticTable): PropertyKeyId =
    semanticTable.resolvedPropertyKeyNames(label)

  def using[T <: LogicalPlan](implicit tag: ClassTag[T]): BeMatcher[LogicalPlan] = new BeMatcher[LogicalPlan] {
    import Foldable._
    override def apply(actual: LogicalPlan): MatchResult = {
      val matches = actual.treeFold(false) {
        case lp if tag.runtimeClass.isInstance(lp) => acc => (true, None)
      }
      MatchResult(
        matches = matches,
        rawFailureMessage = s"Plan should use ${tag.runtimeClass.getSimpleName}",
        rawNegatedFailureMessage = s"Plan should not use ${tag.runtimeClass.getSimpleName}")
    }
  }
}
