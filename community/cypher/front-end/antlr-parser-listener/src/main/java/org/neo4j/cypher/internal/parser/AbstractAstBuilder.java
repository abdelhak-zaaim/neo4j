/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.parser;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Optimised implementation of CypherParserListener.
 * Generated by org.neo4j.cypher.internal.parser.GenerateListenerMavenPlugin
 */
public abstract class AbstractAstBuilder implements CypherParserListener {
    /**
     *
     * Optimised exit method.
     *
     * This compiles into a blazingly fast tableswitch (jump table)
     * and has been shown to be faster than the generated listeners that antlr provides.
     */
    @Override
    public final void exitEveryRule(ParserRuleContext ctx) {
        switch (ctx.getRuleIndex()) {
            case CypherParser.RULE_statements -> exitStatements((CypherParser.StatementsContext) ctx);
            case CypherParser.RULE_statement -> exitStatement((CypherParser.StatementContext) ctx);
            case CypherParser.RULE_periodicCommitQueryHintFailure -> exitPeriodicCommitQueryHintFailure(
                    (CypherParser.PeriodicCommitQueryHintFailureContext) ctx);
            case CypherParser.RULE_regularQuery -> exitRegularQuery((CypherParser.RegularQueryContext) ctx);
            case CypherParser.RULE_singleQuery -> exitSingleQuery((CypherParser.SingleQueryContext) ctx);
            case CypherParser.RULE_clause -> exitClause((CypherParser.ClauseContext) ctx);
            case CypherParser.RULE_useClause -> exitUseClause((CypherParser.UseClauseContext) ctx);
            case CypherParser.RULE_graphReference -> exitGraphReference((CypherParser.GraphReferenceContext) ctx);
            case CypherParser.RULE_finishClause -> exitFinishClause((CypherParser.FinishClauseContext) ctx);
            case CypherParser.RULE_returnClause -> exitReturnClause((CypherParser.ReturnClauseContext) ctx);
            case CypherParser.RULE_returnBody -> exitReturnBody((CypherParser.ReturnBodyContext) ctx);
            case CypherParser.RULE_returnItem -> exitReturnItem((CypherParser.ReturnItemContext) ctx);
            case CypherParser.RULE_returnItems -> exitReturnItems((CypherParser.ReturnItemsContext) ctx);
            case CypherParser.RULE_orderItem -> exitOrderItem((CypherParser.OrderItemContext) ctx);
            case CypherParser.RULE_skip -> exitSkip((CypherParser.SkipContext) ctx);
            case CypherParser.RULE_limit -> exitLimit((CypherParser.LimitContext) ctx);
            case CypherParser.RULE_whereClause -> exitWhereClause((CypherParser.WhereClauseContext) ctx);
            case CypherParser.RULE_withClause -> exitWithClause((CypherParser.WithClauseContext) ctx);
            case CypherParser.RULE_createClause -> exitCreateClause((CypherParser.CreateClauseContext) ctx);
            case CypherParser.RULE_insertClause -> exitInsertClause((CypherParser.InsertClauseContext) ctx);
            case CypherParser.RULE_setClause -> exitSetClause((CypherParser.SetClauseContext) ctx);
            case CypherParser.RULE_setItem -> exitSetItem((CypherParser.SetItemContext) ctx);
            case CypherParser.RULE_removeClause -> exitRemoveClause((CypherParser.RemoveClauseContext) ctx);
            case CypherParser.RULE_removeItem -> exitRemoveItem((CypherParser.RemoveItemContext) ctx);
            case CypherParser.RULE_deleteClause -> exitDeleteClause((CypherParser.DeleteClauseContext) ctx);
            case CypherParser.RULE_matchClause -> exitMatchClause((CypherParser.MatchClauseContext) ctx);
            case CypherParser.RULE_matchMode -> exitMatchMode((CypherParser.MatchModeContext) ctx);
            case CypherParser.RULE_hint -> exitHint((CypherParser.HintContext) ctx);
            case CypherParser.RULE_mergeClause -> exitMergeClause((CypherParser.MergeClauseContext) ctx);
            case CypherParser.RULE_mergeAction -> exitMergeAction((CypherParser.MergeActionContext) ctx);
            case CypherParser.RULE_unwindClause -> exitUnwindClause((CypherParser.UnwindClauseContext) ctx);
            case CypherParser.RULE_callClause -> exitCallClause((CypherParser.CallClauseContext) ctx);
            case CypherParser.RULE_procedureArgument -> exitProcedureArgument(
                    (CypherParser.ProcedureArgumentContext) ctx);
            case CypherParser.RULE_procedureResultItem -> exitProcedureResultItem(
                    (CypherParser.ProcedureResultItemContext) ctx);
            case CypherParser.RULE_loadCSVClause -> exitLoadCSVClause((CypherParser.LoadCSVClauseContext) ctx);
            case CypherParser.RULE_foreachClause -> exitForeachClause((CypherParser.ForeachClauseContext) ctx);
            case CypherParser.RULE_subqueryClause -> exitSubqueryClause((CypherParser.SubqueryClauseContext) ctx);
            case CypherParser.RULE_subqueryInTransactionsParameters -> exitSubqueryInTransactionsParameters(
                    (CypherParser.SubqueryInTransactionsParametersContext) ctx);
            case CypherParser.RULE_subqueryInTransactionsBatchParameters -> exitSubqueryInTransactionsBatchParameters(
                    (CypherParser.SubqueryInTransactionsBatchParametersContext) ctx);
            case CypherParser.RULE_subqueryInTransactionsErrorParameters -> exitSubqueryInTransactionsErrorParameters(
                    (CypherParser.SubqueryInTransactionsErrorParametersContext) ctx);
            case CypherParser.RULE_subqueryInTransactionsReportParameters -> exitSubqueryInTransactionsReportParameters(
                    (CypherParser.SubqueryInTransactionsReportParametersContext) ctx);
            case CypherParser.RULE_patternList -> exitPatternList((CypherParser.PatternListContext) ctx);
            case CypherParser.RULE_insertPatternList -> exitInsertPatternList(
                    (CypherParser.InsertPatternListContext) ctx);
            case CypherParser.RULE_pattern -> exitPattern((CypherParser.PatternContext) ctx);
            case CypherParser.RULE_insertPattern -> exitInsertPattern((CypherParser.InsertPatternContext) ctx);
            case CypherParser.RULE_quantifier -> exitQuantifier((CypherParser.QuantifierContext) ctx);
            case CypherParser.RULE_anonymousPattern -> exitAnonymousPattern((CypherParser.AnonymousPatternContext) ctx);
            case CypherParser.RULE_shortestPathPattern -> exitShortestPathPattern(
                    (CypherParser.ShortestPathPatternContext) ctx);
            case CypherParser.RULE_patternElement -> exitPatternElement((CypherParser.PatternElementContext) ctx);
            case CypherParser.RULE_selector -> exitSelector((CypherParser.SelectorContext) ctx);
            case CypherParser.RULE_pathPatternNonEmpty -> exitPathPatternNonEmpty(
                    (CypherParser.PathPatternNonEmptyContext) ctx);
            case CypherParser.RULE_nodePattern -> exitNodePattern((CypherParser.NodePatternContext) ctx);
            case CypherParser.RULE_insertNodePattern -> exitInsertNodePattern(
                    (CypherParser.InsertNodePatternContext) ctx);
            case CypherParser.RULE_parenthesizedPath -> exitParenthesizedPath(
                    (CypherParser.ParenthesizedPathContext) ctx);
            case CypherParser.RULE_nodeLabels -> exitNodeLabels((CypherParser.NodeLabelsContext) ctx);
            case CypherParser.RULE_nodeLabelsIs -> exitNodeLabelsIs((CypherParser.NodeLabelsIsContext) ctx);
            case CypherParser.RULE_labelType -> exitLabelType((CypherParser.LabelTypeContext) ctx);
            case CypherParser.RULE_relType -> exitRelType((CypherParser.RelTypeContext) ctx);
            case CypherParser.RULE_labelOrRelType -> exitLabelOrRelType((CypherParser.LabelOrRelTypeContext) ctx);
            case CypherParser.RULE_properties -> exitProperties((CypherParser.PropertiesContext) ctx);
            case CypherParser.RULE_relationshipPattern -> exitRelationshipPattern(
                    (CypherParser.RelationshipPatternContext) ctx);
            case CypherParser.RULE_insertRelationshipPattern -> exitInsertRelationshipPattern(
                    (CypherParser.InsertRelationshipPatternContext) ctx);
            case CypherParser.RULE_leftArrow -> exitLeftArrow((CypherParser.LeftArrowContext) ctx);
            case CypherParser.RULE_arrowLine -> exitArrowLine((CypherParser.ArrowLineContext) ctx);
            case CypherParser.RULE_rightArrow -> exitRightArrow((CypherParser.RightArrowContext) ctx);
            case CypherParser.RULE_pathLength -> exitPathLength((CypherParser.PathLengthContext) ctx);
            case CypherParser.RULE_labelExpression -> exitLabelExpression((CypherParser.LabelExpressionContext) ctx);
            case CypherParser.RULE_labelExpression4 -> exitLabelExpression4((CypherParser.LabelExpression4Context) ctx);
            case CypherParser.RULE_labelExpression4Is -> exitLabelExpression4Is(
                    (CypherParser.LabelExpression4IsContext) ctx);
            case CypherParser.RULE_labelExpression3 -> exitLabelExpression3((CypherParser.LabelExpression3Context) ctx);
            case CypherParser.RULE_labelExpression3Is -> exitLabelExpression3Is(
                    (CypherParser.LabelExpression3IsContext) ctx);
            case CypherParser.RULE_labelExpression2 -> exitLabelExpression2((CypherParser.LabelExpression2Context) ctx);
            case CypherParser.RULE_labelExpression2Is -> exitLabelExpression2Is(
                    (CypherParser.LabelExpression2IsContext) ctx);
            case CypherParser.RULE_labelExpression1 -> exitLabelExpression1((CypherParser.LabelExpression1Context) ctx);
            case CypherParser.RULE_labelExpression1Is -> exitLabelExpression1Is(
                    (CypherParser.LabelExpression1IsContext) ctx);
            case CypherParser.RULE_insertNodeLabelExpression -> exitInsertNodeLabelExpression(
                    (CypherParser.InsertNodeLabelExpressionContext) ctx);
            case CypherParser.RULE_insertRelationshipLabelExpression -> exitInsertRelationshipLabelExpression(
                    (CypherParser.InsertRelationshipLabelExpressionContext) ctx);
            case CypherParser.RULE_expression -> exitExpression((CypherParser.ExpressionContext) ctx);
            case CypherParser.RULE_expression11 -> exitExpression11((CypherParser.Expression11Context) ctx);
            case CypherParser.RULE_expression10 -> exitExpression10((CypherParser.Expression10Context) ctx);
            case CypherParser.RULE_expression9 -> exitExpression9((CypherParser.Expression9Context) ctx);
            case CypherParser.RULE_expression8 -> exitExpression8((CypherParser.Expression8Context) ctx);
            case CypherParser.RULE_expression7 -> exitExpression7((CypherParser.Expression7Context) ctx);
            case CypherParser.RULE_comparisonExpression6 -> exitComparisonExpression6(
                    (CypherParser.ComparisonExpression6Context) ctx);
            case CypherParser.RULE_normalForm -> exitNormalForm((CypherParser.NormalFormContext) ctx);
            case CypherParser.RULE_expression6 -> exitExpression6((CypherParser.Expression6Context) ctx);
            case CypherParser.RULE_expression5 -> exitExpression5((CypherParser.Expression5Context) ctx);
            case CypherParser.RULE_expression4 -> exitExpression4((CypherParser.Expression4Context) ctx);
            case CypherParser.RULE_expression3 -> exitExpression3((CypherParser.Expression3Context) ctx);
            case CypherParser.RULE_expression2 -> exitExpression2((CypherParser.Expression2Context) ctx);
            case CypherParser.RULE_postFix -> exitPostFix((CypherParser.PostFixContext) ctx);
            case CypherParser.RULE_property -> exitProperty((CypherParser.PropertyContext) ctx);
            case CypherParser.RULE_propertyExpression -> exitPropertyExpression(
                    (CypherParser.PropertyExpressionContext) ctx);
            case CypherParser.RULE_expression1 -> exitExpression1((CypherParser.Expression1Context) ctx);
            case CypherParser.RULE_literal -> exitLiteral((CypherParser.LiteralContext) ctx);
            case CypherParser.RULE_caseExpression -> exitCaseExpression((CypherParser.CaseExpressionContext) ctx);
            case CypherParser.RULE_caseAlternative -> exitCaseAlternative((CypherParser.CaseAlternativeContext) ctx);
            case CypherParser.RULE_extendedCaseExpression -> exitExtendedCaseExpression(
                    (CypherParser.ExtendedCaseExpressionContext) ctx);
            case CypherParser.RULE_extendedCaseAlternative -> exitExtendedCaseAlternative(
                    (CypherParser.ExtendedCaseAlternativeContext) ctx);
            case CypherParser.RULE_extendedWhen -> exitExtendedWhen((CypherParser.ExtendedWhenContext) ctx);
            case CypherParser.RULE_listComprehension -> exitListComprehension(
                    (CypherParser.ListComprehensionContext) ctx);
            case CypherParser.RULE_patternComprehension -> exitPatternComprehension(
                    (CypherParser.PatternComprehensionContext) ctx);
            case CypherParser.RULE_reduceExpression -> exitReduceExpression((CypherParser.ReduceExpressionContext) ctx);
            case CypherParser.RULE_listItemsPredicate -> exitListItemsPredicate(
                    (CypherParser.ListItemsPredicateContext) ctx);
            case CypherParser.RULE_normalizeFunction -> exitNormalizeFunction(
                    (CypherParser.NormalizeFunctionContext) ctx);
            case CypherParser.RULE_trimFunction -> exitTrimFunction((CypherParser.TrimFunctionContext) ctx);
            case CypherParser.RULE_patternExpression -> exitPatternExpression(
                    (CypherParser.PatternExpressionContext) ctx);
            case CypherParser.RULE_shortestPathExpression -> exitShortestPathExpression(
                    (CypherParser.ShortestPathExpressionContext) ctx);
            case CypherParser.RULE_parenthesizedExpression -> exitParenthesizedExpression(
                    (CypherParser.ParenthesizedExpressionContext) ctx);
            case CypherParser.RULE_mapProjection -> exitMapProjection((CypherParser.MapProjectionContext) ctx);
            case CypherParser.RULE_mapProjectionElement -> exitMapProjectionElement(
                    (CypherParser.MapProjectionElementContext) ctx);
            case CypherParser.RULE_countStar -> exitCountStar((CypherParser.CountStarContext) ctx);
            case CypherParser.RULE_existsExpression -> exitExistsExpression((CypherParser.ExistsExpressionContext) ctx);
            case CypherParser.RULE_countExpression -> exitCountExpression((CypherParser.CountExpressionContext) ctx);
            case CypherParser.RULE_collectExpression -> exitCollectExpression(
                    (CypherParser.CollectExpressionContext) ctx);
            case CypherParser.RULE_numberLiteral -> exitNumberLiteral((CypherParser.NumberLiteralContext) ctx);
            case CypherParser.RULE_signedIntegerLiteral -> exitSignedIntegerLiteral(
                    (CypherParser.SignedIntegerLiteralContext) ctx);
            case CypherParser.RULE_listLiteral -> exitListLiteral((CypherParser.ListLiteralContext) ctx);
            case CypherParser.RULE_propertyKeyName -> exitPropertyKeyName((CypherParser.PropertyKeyNameContext) ctx);
            case CypherParser.RULE_parameter -> exitParameter((CypherParser.ParameterContext) ctx);
            case CypherParser.RULE_functionInvocation -> exitFunctionInvocation(
                    (CypherParser.FunctionInvocationContext) ctx);
            case CypherParser.RULE_namespace -> exitNamespace((CypherParser.NamespaceContext) ctx);
            case CypherParser.RULE_variable -> exitVariable((CypherParser.VariableContext) ctx);
            case CypherParser.RULE_nonEmptyNameList -> exitNonEmptyNameList((CypherParser.NonEmptyNameListContext) ctx);
            case CypherParser.RULE_command -> exitCommand((CypherParser.CommandContext) ctx);
            case CypherParser.RULE_createCommand -> exitCreateCommand((CypherParser.CreateCommandContext) ctx);
            case CypherParser.RULE_dropCommand -> exitDropCommand((CypherParser.DropCommandContext) ctx);
            case CypherParser.RULE_alterCommand -> exitAlterCommand((CypherParser.AlterCommandContext) ctx);
            case CypherParser.RULE_renameCommand -> exitRenameCommand((CypherParser.RenameCommandContext) ctx);
            case CypherParser.RULE_showCommand -> exitShowCommand((CypherParser.ShowCommandContext) ctx);
            case CypherParser.RULE_showCommandYield -> exitShowCommandYield((CypherParser.ShowCommandYieldContext) ctx);
            case CypherParser.RULE_yieldItem -> exitYieldItem((CypherParser.YieldItemContext) ctx);
            case CypherParser.RULE_yieldSkip -> exitYieldSkip((CypherParser.YieldSkipContext) ctx);
            case CypherParser.RULE_yieldLimit -> exitYieldLimit((CypherParser.YieldLimitContext) ctx);
            case CypherParser.RULE_yieldOrderBy -> exitYieldOrderBy((CypherParser.YieldOrderByContext) ctx);
            case CypherParser.RULE_yieldClause -> exitYieldClause((CypherParser.YieldClauseContext) ctx);
            case CypherParser.RULE_showBriefAndYield -> exitShowBriefAndYield(
                    (CypherParser.ShowBriefAndYieldContext) ctx);
            case CypherParser.RULE_showIndexCommand -> exitShowIndexCommand((CypherParser.ShowIndexCommandContext) ctx);
            case CypherParser.RULE_showIndexesAllowBrief -> exitShowIndexesAllowBrief(
                    (CypherParser.ShowIndexesAllowBriefContext) ctx);
            case CypherParser.RULE_showIndexesNoBrief -> exitShowIndexesNoBrief(
                    (CypherParser.ShowIndexesNoBriefContext) ctx);
            case CypherParser.RULE_showConstraintCommand -> exitShowConstraintCommand(
                    (CypherParser.ShowConstraintCommandContext) ctx);
            case CypherParser.RULE_constraintAllowYieldType -> exitConstraintAllowYieldType(
                    (CypherParser.ConstraintAllowYieldTypeContext) ctx);
            case CypherParser.RULE_constraintExistType -> exitConstraintExistType(
                    (CypherParser.ConstraintExistTypeContext) ctx);
            case CypherParser.RULE_constraintBriefAndYieldType -> exitConstraintBriefAndYieldType(
                    (CypherParser.ConstraintBriefAndYieldTypeContext) ctx);
            case CypherParser.RULE_showConstraintsAllowBriefAndYield -> exitShowConstraintsAllowBriefAndYield(
                    (CypherParser.ShowConstraintsAllowBriefAndYieldContext) ctx);
            case CypherParser.RULE_showConstraintsAllowBrief -> exitShowConstraintsAllowBrief(
                    (CypherParser.ShowConstraintsAllowBriefContext) ctx);
            case CypherParser.RULE_showConstraintsAllowYield -> exitShowConstraintsAllowYield(
                    (CypherParser.ShowConstraintsAllowYieldContext) ctx);
            case CypherParser.RULE_showProcedures -> exitShowProcedures((CypherParser.ShowProceduresContext) ctx);
            case CypherParser.RULE_showFunctions -> exitShowFunctions((CypherParser.ShowFunctionsContext) ctx);
            case CypherParser.RULE_executableBy -> exitExecutableBy((CypherParser.ExecutableByContext) ctx);
            case CypherParser.RULE_showFunctionsType -> exitShowFunctionsType(
                    (CypherParser.ShowFunctionsTypeContext) ctx);
            case CypherParser.RULE_showTransactions -> exitShowTransactions((CypherParser.ShowTransactionsContext) ctx);
            case CypherParser.RULE_terminateCommand -> exitTerminateCommand((CypherParser.TerminateCommandContext) ctx);
            case CypherParser.RULE_terminateTransactions -> exitTerminateTransactions(
                    (CypherParser.TerminateTransactionsContext) ctx);
            case CypherParser.RULE_showSettings -> exitShowSettings((CypherParser.ShowSettingsContext) ctx);
            case CypherParser.RULE_namesAndClauses -> exitNamesAndClauses((CypherParser.NamesAndClausesContext) ctx);
            case CypherParser.RULE_composableCommandClauses -> exitComposableCommandClauses(
                    (CypherParser.ComposableCommandClausesContext) ctx);
            case CypherParser.RULE_composableShowCommandClauses -> exitComposableShowCommandClauses(
                    (CypherParser.ComposableShowCommandClausesContext) ctx);
            case CypherParser.RULE_stringsOrExpression -> exitStringsOrExpression(
                    (CypherParser.StringsOrExpressionContext) ctx);
            case CypherParser.RULE_type -> exitType((CypherParser.TypeContext) ctx);
            case CypherParser.RULE_typePart -> exitTypePart((CypherParser.TypePartContext) ctx);
            case CypherParser.RULE_typeName -> exitTypeName((CypherParser.TypeNameContext) ctx);
            case CypherParser.RULE_typeNullability -> exitTypeNullability((CypherParser.TypeNullabilityContext) ctx);
            case CypherParser.RULE_typeListSuffix -> exitTypeListSuffix((CypherParser.TypeListSuffixContext) ctx);
            case CypherParser.RULE_commandNodePattern -> exitCommandNodePattern(
                    (CypherParser.CommandNodePatternContext) ctx);
            case CypherParser.RULE_commandRelPattern -> exitCommandRelPattern(
                    (CypherParser.CommandRelPatternContext) ctx);
            case CypherParser.RULE_createConstraint -> exitCreateConstraint((CypherParser.CreateConstraintContext) ctx);
            case CypherParser.RULE_constraintType -> exitConstraintType((CypherParser.ConstraintTypeContext) ctx);
            case CypherParser.RULE_dropConstraint -> exitDropConstraint((CypherParser.DropConstraintContext) ctx);
            case CypherParser.RULE_createIndex -> exitCreateIndex((CypherParser.CreateIndexContext) ctx);
            case CypherParser.RULE_oldCreateIndex -> exitOldCreateIndex((CypherParser.OldCreateIndexContext) ctx);
            case CypherParser.RULE_createIndex_ -> exitCreateIndex_((CypherParser.CreateIndex_Context) ctx);
            case CypherParser.RULE_createFulltextIndex -> exitCreateFulltextIndex(
                    (CypherParser.CreateFulltextIndexContext) ctx);
            case CypherParser.RULE_fulltextNodePattern -> exitFulltextNodePattern(
                    (CypherParser.FulltextNodePatternContext) ctx);
            case CypherParser.RULE_fulltextRelPattern -> exitFulltextRelPattern(
                    (CypherParser.FulltextRelPatternContext) ctx);
            case CypherParser.RULE_createLookupIndex -> exitCreateLookupIndex(
                    (CypherParser.CreateLookupIndexContext) ctx);
            case CypherParser.RULE_lookupIndexNodePattern -> exitLookupIndexNodePattern(
                    (CypherParser.LookupIndexNodePatternContext) ctx);
            case CypherParser.RULE_lookupIndexRelPattern -> exitLookupIndexRelPattern(
                    (CypherParser.LookupIndexRelPatternContext) ctx);
            case CypherParser.RULE_dropIndex -> exitDropIndex((CypherParser.DropIndexContext) ctx);
            case CypherParser.RULE_propertyList -> exitPropertyList((CypherParser.PropertyListContext) ctx);
            case CypherParser.RULE_enableServerCommand -> exitEnableServerCommand(
                    (CypherParser.EnableServerCommandContext) ctx);
            case CypherParser.RULE_alterServer -> exitAlterServer((CypherParser.AlterServerContext) ctx);
            case CypherParser.RULE_renameServer -> exitRenameServer((CypherParser.RenameServerContext) ctx);
            case CypherParser.RULE_dropServer -> exitDropServer((CypherParser.DropServerContext) ctx);
            case CypherParser.RULE_showServers -> exitShowServers((CypherParser.ShowServersContext) ctx);
            case CypherParser.RULE_allocationCommand -> exitAllocationCommand(
                    (CypherParser.AllocationCommandContext) ctx);
            case CypherParser.RULE_deallocateDatabaseFromServers -> exitDeallocateDatabaseFromServers(
                    (CypherParser.DeallocateDatabaseFromServersContext) ctx);
            case CypherParser.RULE_reallocateDatabases -> exitReallocateDatabases(
                    (CypherParser.ReallocateDatabasesContext) ctx);
            case CypherParser.RULE_createRole -> exitCreateRole((CypherParser.CreateRoleContext) ctx);
            case CypherParser.RULE_dropRole -> exitDropRole((CypherParser.DropRoleContext) ctx);
            case CypherParser.RULE_renameRole -> exitRenameRole((CypherParser.RenameRoleContext) ctx);
            case CypherParser.RULE_showRoles -> exitShowRoles((CypherParser.ShowRolesContext) ctx);
            case CypherParser.RULE_roleToken -> exitRoleToken((CypherParser.RoleTokenContext) ctx);
            case CypherParser.RULE_createUser -> exitCreateUser((CypherParser.CreateUserContext) ctx);
            case CypherParser.RULE_dropUser -> exitDropUser((CypherParser.DropUserContext) ctx);
            case CypherParser.RULE_renameUser -> exitRenameUser((CypherParser.RenameUserContext) ctx);
            case CypherParser.RULE_alterCurrentUser -> exitAlterCurrentUser((CypherParser.AlterCurrentUserContext) ctx);
            case CypherParser.RULE_alterUser -> exitAlterUser((CypherParser.AlterUserContext) ctx);
            case CypherParser.RULE_password -> exitPassword((CypherParser.PasswordContext) ctx);
            case CypherParser.RULE_passwordExpression -> exitPasswordExpression(
                    (CypherParser.PasswordExpressionContext) ctx);
            case CypherParser.RULE_passwordChangeRequired -> exitPasswordChangeRequired(
                    (CypherParser.PasswordChangeRequiredContext) ctx);
            case CypherParser.RULE_userStatus -> exitUserStatus((CypherParser.UserStatusContext) ctx);
            case CypherParser.RULE_homeDatabase -> exitHomeDatabase((CypherParser.HomeDatabaseContext) ctx);
            case CypherParser.RULE_showUsers -> exitShowUsers((CypherParser.ShowUsersContext) ctx);
            case CypherParser.RULE_showCurrentUser -> exitShowCurrentUser((CypherParser.ShowCurrentUserContext) ctx);
            case CypherParser.RULE_showPrivileges -> exitShowPrivileges((CypherParser.ShowPrivilegesContext) ctx);
            case CypherParser.RULE_showSupportedPrivileges -> exitShowSupportedPrivileges(
                    (CypherParser.ShowSupportedPrivilegesContext) ctx);
            case CypherParser.RULE_showRolePrivileges -> exitShowRolePrivileges(
                    (CypherParser.ShowRolePrivilegesContext) ctx);
            case CypherParser.RULE_showUserPrivileges -> exitShowUserPrivileges(
                    (CypherParser.ShowUserPrivilegesContext) ctx);
            case CypherParser.RULE_privilegeAsCommand -> exitPrivilegeAsCommand(
                    (CypherParser.PrivilegeAsCommandContext) ctx);
            case CypherParser.RULE_privilegeToken -> exitPrivilegeToken((CypherParser.PrivilegeTokenContext) ctx);
            case CypherParser.RULE_grantCommand -> exitGrantCommand((CypherParser.GrantCommandContext) ctx);
            case CypherParser.RULE_grantRole -> exitGrantRole((CypherParser.GrantRoleContext) ctx);
            case CypherParser.RULE_denyCommand -> exitDenyCommand((CypherParser.DenyCommandContext) ctx);
            case CypherParser.RULE_revokeCommand -> exitRevokeCommand((CypherParser.RevokeCommandContext) ctx);
            case CypherParser.RULE_revokeRole -> exitRevokeRole((CypherParser.RevokeRoleContext) ctx);
            case CypherParser.RULE_privilege -> exitPrivilege((CypherParser.PrivilegeContext) ctx);
            case CypherParser.RULE_allPrivilege -> exitAllPrivilege((CypherParser.AllPrivilegeContext) ctx);
            case CypherParser.RULE_allPrivilegeType -> exitAllPrivilegeType((CypherParser.AllPrivilegeTypeContext) ctx);
            case CypherParser.RULE_allPrivilegeTarget -> exitAllPrivilegeTarget(
                    (CypherParser.AllPrivilegeTargetContext) ctx);
            case CypherParser.RULE_createPrivilege -> exitCreatePrivilege((CypherParser.CreatePrivilegeContext) ctx);
            case CypherParser.RULE_createPrivilegeForDatabase -> exitCreatePrivilegeForDatabase(
                    (CypherParser.CreatePrivilegeForDatabaseContext) ctx);
            case CypherParser.RULE_createNodePrivilegeToken -> exitCreateNodePrivilegeToken(
                    (CypherParser.CreateNodePrivilegeTokenContext) ctx);
            case CypherParser.RULE_createRelPrivilegeToken -> exitCreateRelPrivilegeToken(
                    (CypherParser.CreateRelPrivilegeTokenContext) ctx);
            case CypherParser.RULE_createPropertyPrivilegeToken -> exitCreatePropertyPrivilegeToken(
                    (CypherParser.CreatePropertyPrivilegeTokenContext) ctx);
            case CypherParser.RULE_actionForDBMS -> exitActionForDBMS((CypherParser.ActionForDBMSContext) ctx);
            case CypherParser.RULE_dropPrivilege -> exitDropPrivilege((CypherParser.DropPrivilegeContext) ctx);
            case CypherParser.RULE_loadPrivilege -> exitLoadPrivilege((CypherParser.LoadPrivilegeContext) ctx);
            case CypherParser.RULE_showPrivilege -> exitShowPrivilege((CypherParser.ShowPrivilegeContext) ctx);
            case CypherParser.RULE_setPrivilege -> exitSetPrivilege((CypherParser.SetPrivilegeContext) ctx);
            case CypherParser.RULE_passwordToken -> exitPasswordToken((CypherParser.PasswordTokenContext) ctx);
            case CypherParser.RULE_removePrivilege -> exitRemovePrivilege((CypherParser.RemovePrivilegeContext) ctx);
            case CypherParser.RULE_writePrivilege -> exitWritePrivilege((CypherParser.WritePrivilegeContext) ctx);
            case CypherParser.RULE_databasePrivilege -> exitDatabasePrivilege(
                    (CypherParser.DatabasePrivilegeContext) ctx);
            case CypherParser.RULE_dbmsPrivilege -> exitDbmsPrivilege((CypherParser.DbmsPrivilegeContext) ctx);
            case CypherParser.RULE_dbmsPrivilegeExecute -> exitDbmsPrivilegeExecute(
                    (CypherParser.DbmsPrivilegeExecuteContext) ctx);
            case CypherParser.RULE_adminToken -> exitAdminToken((CypherParser.AdminTokenContext) ctx);
            case CypherParser.RULE_procedureToken -> exitProcedureToken((CypherParser.ProcedureTokenContext) ctx);
            case CypherParser.RULE_indexToken -> exitIndexToken((CypherParser.IndexTokenContext) ctx);
            case CypherParser.RULE_constraintToken -> exitConstraintToken((CypherParser.ConstraintTokenContext) ctx);
            case CypherParser.RULE_transactionToken -> exitTransactionToken((CypherParser.TransactionTokenContext) ctx);
            case CypherParser.RULE_userQualifier -> exitUserQualifier((CypherParser.UserQualifierContext) ctx);
            case CypherParser.RULE_executeFunctionQualifier -> exitExecuteFunctionQualifier(
                    (CypherParser.ExecuteFunctionQualifierContext) ctx);
            case CypherParser.RULE_executeProcedureQualifier -> exitExecuteProcedureQualifier(
                    (CypherParser.ExecuteProcedureQualifierContext) ctx);
            case CypherParser.RULE_settingQualifier -> exitSettingQualifier((CypherParser.SettingQualifierContext) ctx);
            case CypherParser.RULE_globs -> exitGlobs((CypherParser.GlobsContext) ctx);
            case CypherParser.RULE_qualifiedGraphPrivilegesWithProperty -> exitQualifiedGraphPrivilegesWithProperty(
                    (CypherParser.QualifiedGraphPrivilegesWithPropertyContext) ctx);
            case CypherParser.RULE_qualifiedGraphPrivileges -> exitQualifiedGraphPrivileges(
                    (CypherParser.QualifiedGraphPrivilegesContext) ctx);
            case CypherParser.RULE_labelsResource -> exitLabelsResource((CypherParser.LabelsResourceContext) ctx);
            case CypherParser.RULE_propertiesResource -> exitPropertiesResource(
                    (CypherParser.PropertiesResourceContext) ctx);
            case CypherParser.RULE_nonEmptyStringList -> exitNonEmptyStringList(
                    (CypherParser.NonEmptyStringListContext) ctx);
            case CypherParser.RULE_graphQualifier -> exitGraphQualifier((CypherParser.GraphQualifierContext) ctx);
            case CypherParser.RULE_graphQualifierToken -> exitGraphQualifierToken(
                    (CypherParser.GraphQualifierTokenContext) ctx);
            case CypherParser.RULE_relToken -> exitRelToken((CypherParser.RelTokenContext) ctx);
            case CypherParser.RULE_elementToken -> exitElementToken((CypherParser.ElementTokenContext) ctx);
            case CypherParser.RULE_nodeToken -> exitNodeToken((CypherParser.NodeTokenContext) ctx);
            case CypherParser.RULE_createCompositeDatabase -> exitCreateCompositeDatabase(
                    (CypherParser.CreateCompositeDatabaseContext) ctx);
            case CypherParser.RULE_createDatabase -> exitCreateDatabase((CypherParser.CreateDatabaseContext) ctx);
            case CypherParser.RULE_primaryTopology -> exitPrimaryTopology((CypherParser.PrimaryTopologyContext) ctx);
            case CypherParser.RULE_secondaryTopology -> exitSecondaryTopology(
                    (CypherParser.SecondaryTopologyContext) ctx);
            case CypherParser.RULE_dropDatabase -> exitDropDatabase((CypherParser.DropDatabaseContext) ctx);
            case CypherParser.RULE_alterDatabase -> exitAlterDatabase((CypherParser.AlterDatabaseContext) ctx);
            case CypherParser.RULE_alterDatabaseAccess -> exitAlterDatabaseAccess(
                    (CypherParser.AlterDatabaseAccessContext) ctx);
            case CypherParser.RULE_alterDatabaseTopology -> exitAlterDatabaseTopology(
                    (CypherParser.AlterDatabaseTopologyContext) ctx);
            case CypherParser.RULE_alterDatabaseOption -> exitAlterDatabaseOption(
                    (CypherParser.AlterDatabaseOptionContext) ctx);
            case CypherParser.RULE_startDatabase -> exitStartDatabase((CypherParser.StartDatabaseContext) ctx);
            case CypherParser.RULE_stopDatabase -> exitStopDatabase((CypherParser.StopDatabaseContext) ctx);
            case CypherParser.RULE_waitClause -> exitWaitClause((CypherParser.WaitClauseContext) ctx);
            case CypherParser.RULE_showDatabase -> exitShowDatabase((CypherParser.ShowDatabaseContext) ctx);
            case CypherParser.RULE_databaseScope -> exitDatabaseScope((CypherParser.DatabaseScopeContext) ctx);
            case CypherParser.RULE_graphScope -> exitGraphScope((CypherParser.GraphScopeContext) ctx);
            case CypherParser.RULE_commandOptions -> exitCommandOptions((CypherParser.CommandOptionsContext) ctx);
            case CypherParser.RULE_commandNameExpression -> exitCommandNameExpression(
                    (CypherParser.CommandNameExpressionContext) ctx);
            case CypherParser.RULE_symbolicNameOrStringParameter -> exitSymbolicNameOrStringParameter(
                    (CypherParser.SymbolicNameOrStringParameterContext) ctx);
            case CypherParser.RULE_createAlias -> exitCreateAlias((CypherParser.CreateAliasContext) ctx);
            case CypherParser.RULE_dropAlias -> exitDropAlias((CypherParser.DropAliasContext) ctx);
            case CypherParser.RULE_alterAlias -> exitAlterAlias((CypherParser.AlterAliasContext) ctx);
            case CypherParser.RULE_alterAliasTarget -> exitAlterAliasTarget((CypherParser.AlterAliasTargetContext) ctx);
            case CypherParser.RULE_alterAliasUser -> exitAlterAliasUser((CypherParser.AlterAliasUserContext) ctx);
            case CypherParser.RULE_alterAliasPassword -> exitAlterAliasPassword(
                    (CypherParser.AlterAliasPasswordContext) ctx);
            case CypherParser.RULE_alterAliasDriver -> exitAlterAliasDriver((CypherParser.AlterAliasDriverContext) ctx);
            case CypherParser.RULE_alterAliasProperties -> exitAlterAliasProperties(
                    (CypherParser.AlterAliasPropertiesContext) ctx);
            case CypherParser.RULE_showAliases -> exitShowAliases((CypherParser.ShowAliasesContext) ctx);
            case CypherParser.RULE_symbolicAliasNameList -> exitSymbolicAliasNameList(
                    (CypherParser.SymbolicAliasNameListContext) ctx);
            case CypherParser.RULE_symbolicAliasNameOrParameter -> exitSymbolicAliasNameOrParameter(
                    (CypherParser.SymbolicAliasNameOrParameterContext) ctx);
            case CypherParser.RULE_symbolicAliasName -> exitSymbolicAliasName(
                    (CypherParser.SymbolicAliasNameContext) ctx);
            case CypherParser.RULE_symbolicNameOrStringParameterList -> exitSymbolicNameOrStringParameterList(
                    (CypherParser.SymbolicNameOrStringParameterListContext) ctx);
            case CypherParser.RULE_glob -> exitGlob((CypherParser.GlobContext) ctx);
            case CypherParser.RULE_globRecursive -> exitGlobRecursive((CypherParser.GlobRecursiveContext) ctx);
            case CypherParser.RULE_globPart -> exitGlobPart((CypherParser.GlobPartContext) ctx);
            case CypherParser.RULE_stringList -> exitStringList((CypherParser.StringListContext) ctx);
            case CypherParser.RULE_stringLiteral -> exitStringLiteral((CypherParser.StringLiteralContext) ctx);
            case CypherParser.RULE_stringOrParameter -> exitStringOrParameter(
                    (CypherParser.StringOrParameterContext) ctx);
            case CypherParser.RULE_mapOrParameter -> exitMapOrParameter((CypherParser.MapOrParameterContext) ctx);
            case CypherParser.RULE_map -> exitMap((CypherParser.MapContext) ctx);
            case CypherParser.RULE_symbolicNameString -> exitSymbolicNameString(
                    (CypherParser.SymbolicNameStringContext) ctx);
            case CypherParser.RULE_escapedSymbolicNameString -> exitEscapedSymbolicNameString(
                    (CypherParser.EscapedSymbolicNameStringContext) ctx);
            case CypherParser.RULE_unescapedSymbolicNameString -> exitUnescapedSymbolicNameString(
                    (CypherParser.UnescapedSymbolicNameStringContext) ctx);
            case CypherParser.RULE_symbolicLabelNameString -> exitSymbolicLabelNameString(
                    (CypherParser.SymbolicLabelNameStringContext) ctx);
            case CypherParser.RULE_unescapedLabelSymbolicNameString -> exitUnescapedLabelSymbolicNameString(
                    (CypherParser.UnescapedLabelSymbolicNameStringContext) ctx);
            case CypherParser.RULE_endOfFile -> exitEndOfFile((CypherParser.EndOfFileContext) ctx);
            default -> throw new IllegalStateException("Unknown rule index " + ctx.getRuleIndex());
        }
    }
}
