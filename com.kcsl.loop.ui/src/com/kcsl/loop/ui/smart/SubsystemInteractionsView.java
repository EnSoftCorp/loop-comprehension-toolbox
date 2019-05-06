package com.kcsl.loop.ui.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasNodeHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.highlighter.CFGHighlighter;
import com.ensoftcorp.open.commons.subsystems.Subsystems;
import com.kcsl.loop.codemap.stages.BranchCatalogCodemapStage;
import com.kcsl.loop.ui.SubsystemComponents;
import com.kcsl.loop.ui.SubsystemsBuilderView;
import com.kcsl.loop.ui.log.Log;
import com.kcsl.loop.util.LoopUtils;
import com.kcsl.loop.util.SetDefinitions;

/**
 * Input: One or more Methods
 *
 *
 * Output: For each input Method, the Loop Call Graph. If a Method has a Loop or
 * can reach a Method which has a loop, it will be in the call graph. Otherwise
 * the result is the empty graph.
 *
 *
 * Highlights: * Methods containing loops == bright BLUE * Call edges which may
 * have been from a CallSite within a loop == ORANGE
 *
 */
public class SubsystemInteractionsView extends FilteringAtlasSmartViewScript implements AtlasSmartViewScript {

	public static final String ID = "org.rulersoftware.ui.SubsystemInteractionsView";

	
	public static String[] subsystemTags = new String[] { "IO_SUBSYSTEM" };
	
	public static String subsystemBuilderName = "";

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { XCSG.Loop, XCSG.Method, XCSG.CallSite, XCSG.ControlFlow_Node };
	}

	@Override
	protected String[] getSupportedEdgeTags() {
		return NOTHING;
	}

	@Override
	public String getTitle() {
		return "RULER Subsystem Interaction View";
	}

	@Override
	protected StyledResult selectionChanged(IAtlasSelectionEvent event, Q filteredSelection) {

		if (CommonQueries.isEmpty(filteredSelection))
			return null;

		Q interactions = getInteractions(filteredSelection);
		Q selection = filteredSelection.nodes(XCSG.Loop,XCSG.Function,XCSG.ControlFlow_Node,XCSG.CallSite);
		Q callSiteEvents = getSubsystemInteractionEvents(selection.eval().nodes().one(),subsystemTags);
		
		Markup m = new Markup();
		CFGHighlighter.applyHighlightsForCFG(m);

		// call sites to subsystem
		m.setNode(callSiteEvents, MarkupProperty.NODE_BACKGROUND_COLOR, Color.GREEN.brighter());

		// magenta interaction with methods
		m.setNode(SetDefinitions.JDKLibraries().contained().nodes(XCSG.Method), MarkupProperty.NODE_BACKGROUND_COLOR, Color.magenta.brighter());
		m.setNode(interactions.nodes(XCSG.Method).difference(SetDefinitions.JDKLibraries().contained().nodes(XCSG.Method)), MarkupProperty.NODE_BORDER_COLOR, Color.MAGENTA.brighter());
		m.setNode(interactions.nodes(XCSG.Method).difference(SetDefinitions.JDKLibraries().contained().nodes(XCSG.Method)), MarkupProperty.NODE_BORDER_WEIGHT, new Integer(2));
		
		// magenta interaction with methods
		m.setEdge(interactions.edges(Attr.Edge.PER_CONTROL_FLOW, XCSG.Call), MarkupProperty.EDGE_STYLE, MarkupProperty.LineStyle.DASHED_DOTTED);
		m.setEdge(interactions.edges(Attr.Edge.PER_CONTROL_FLOW, XCSG.Call), MarkupProperty.EDGE_WEIGHT, new Integer(2));
		m.setEdge(interactions.edges(Attr.Edge.PER_CONTROL_FLOW, XCSG.Call), MarkupProperty.EDGE_COLOR, Color.MAGENTA.brighter());
		
		return new StyledResult(interactions, m, interactions.nodes(XCSG.Loop));

	}

	public static Q getInteractions(Q filteredSelection) {
		Q interactions = Common.empty();

		Q context = filteredSelection.nodes(XCSG.Loop, XCSG.Method, XCSG.ControlFlow_Node, XCSG.CallSite);
		for (Node node : context.eval().nodes()) {
			if(subsystemBuilderName != null && subsystemBuilderName.length()>0) {
				Log.info("Retrieving set of functions from SubsystemBuilderView "+subsystemBuilderName);
				SubsystemComponents subsystemComponents = SubsystemsBuilderView.getSubsystemComponents(subsystemBuilderName);
				if(subsystemComponents != null) {
					Q methods = Common.toQ(subsystemComponents.getFunctionsSelectedForInteraction());
					Log.info("# Functions retrieved from " + subsystemBuilderName + ": "+methods.eval().nodes().size());
					if(!CommonQueries.isEmpty(methods)) {
						interactions = getSubsystemInteractionGraph(node, methods);
						Log.info("Interaction size with functions in " + subsystemBuilderName + ": ("+interactions.eval().nodes().size()+","+interactions.eval().edges().size()+")");
					}
				}
			} else {
				interactions = getSubsystemInteractionGraph(node, subsystemTags);
			}
			Q cfg = Common.empty();
			if (node.taggedWith(XCSG.Method)) {
				cfg = CommonQueries.cfg(Common.toQ(node));
			} else if (node.taggedWith(XCSG.Loop)) {
				cfg = LoopUtils.getControlFlowGraph(node);
			} else if (node.taggedWith(XCSG.ControlFlowCondition)) {
				Q method = Common.toQ(CommonQueries.getContainingFunction(node));
				cfg = CommonQueries.cfg(method);
			}
			interactions = interactions.union(cfg);
		}

		return interactions;
	}

	public static Q getSubsystemInteractionGraph(Node context, String... subsystemTags) {
		Q result = Common.empty();

		Q targetMethods = getTargetMethodsForContainedCallsites(context);
		Q subsystemMethods = Subsystems.getSubsystemContents(subsystemTags).nodes(XCSG.Method);
		result = Common.edges(XCSG.Call).between(targetMethods, subsystemMethods);

		Q callsiteContainers = getCallsiteContainers(context);
		Q summaryCallEdges = Common.edges(Attr.Edge.PER_CONTROL_FLOW).betweenStep(callsiteContainers, result);
		result = result.union(summaryCallEdges);
		return result;
	}
	
	public static Q getSubsystemInteractionGraph(Node context, Q methodSet) {
		Q result = Common.empty();

		Q targetMethods = getTargetMethodsForContainedCallsites(context);
		result = Common.edges(XCSG.Call).between(targetMethods, methodSet);

		Q callsiteContainers = getCallsiteContainers(context);
		Q summaryCallEdges = Common.edges(Attr.Edge.PER_CONTROL_FLOW).betweenStep(callsiteContainers, result);
		result = result.union(summaryCallEdges);
		return result;
	}

	public static Q getSubsystemInteractionEvents(Node context, String... subsystemTags) {
		Q result = Common.empty();

		Q targetMethods = getTargetMethodsForContainedCallsites(context);
		Q subsystemMethods = Subsystems.getSubsystemContents(subsystemTags).nodes(XCSG.Method);
		result = Common.edges(XCSG.Call).between(targetMethods, subsystemMethods);

		Q callsiteContainers = getCallsiteContainers(context);
		Q summaryCallEdges = Common.edges(Attr.Edge.PER_CONTROL_FLOW).betweenStep(callsiteContainers, result);
		return summaryCallEdges.roots().nodes(XCSG.ControlFlow_Node);
	}

	private static Q getCallsiteContainers(Node context) {
		Q callsites = getCallsites(context);

		if (CommonQueries.isEmpty(callsites)) {
			return Common.empty();
		}

		return callsites.parent();
	}

	private static Q getCallsites(Node context) {
		Q callsites = Query.empty();

		if (context.taggedWith(XCSG.Loop)) {
			callsites = callsites.union(LoopUtils.getCallsitesInsideLoop(Common.toQ(context)));
		} else if (context.taggedWith(XCSG.ControlFlowCondition)) {
//			Q dominanceFrontierEdges = Query.universe().edges(DominanceAnalysis.DOMINANCE_FRONTIER_EDGE);
//			Q branchGovernanceContext = dominanceFrontierEdges.forward(Common.toQ(context));
//			callsites = callsites.union(branchGovernanceContext.contained().nodes(XCSG.CallSite));
			Q method = Common.toQ(CommonQueries.getContainingFunction(context));
//			Q methodCfg = method.contained().nodes(XCSG.ControlFlow_Node).induce(Query.universe().edges(XCSG.ControlFlow_Edge));
			Q excfgWithoutBackEdges = CommonQueries.excfg(CommonQueries.getContainingFunction(context)).differenceEdges(Query.universe().edges(XCSG.ControlFlowBackEdge));
			AtlasSet<Node> frontier = new AtlasNodeHashSet();
			for(Node cfNode: method.contained().nodes(XCSG.ControlFlow_Node).eval().nodes()) {
				if((BranchCatalogCodemapStage.branchGovernsEvent(Common.toQ(context), Common.toQ(cfNode), excfgWithoutBackEdges))) {
					frontier.add(cfNode);
				}
			}
			callsites = callsites.union(Common.toQ(frontier).contained().nodes(XCSG.CallSite));
		} else if (context.taggedWith(XCSG.Method) || context.taggedWith(XCSG.ControlFlow_Node)) {
			callsites = callsites.union(Common.toQ(context).contained().nodes(XCSG.CallSite));
		} else if (context.taggedWith(XCSG.CallSite)) {
			callsites = callsites.union(Common.toQ(context).nodes(XCSG.CallSite));
		}

		return callsites;
	}

	public static Q getTargetMethodsForContainedCallsites(Node context) {
		Q callsites = getCallsites(context);
		Q targetMethods = Common.empty();
		for (Node c : callsites.eval().nodes()) {
			targetMethods = targetMethods.union(CallSiteAnalysis.getTargets(Common.toQ(c)));
		}
		return targetMethods;
	}

}
