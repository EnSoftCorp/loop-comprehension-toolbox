package com.ensoftcorp.open.loop.comprehension.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.scripts.util.SimpleScriptUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.analysis.LoopAbstractions;
import com.ensoftcorp.open.loop.comprehension.taint.TaintOverlay;
import com.ensoftcorp.open.loop.comprehension.utils.TerminatingConditions;

public class TDGSmartView extends FilteringAtlasSmartViewScript implements IResizableScript {

	private static java.awt.Color mediumGreen = new java.awt.Color(60,140,70);
	
	@Override
	protected String[] getSupportedNodeTags() {
		return new String[]{XCSG.Loop, CFGNode.LOOP_HEADER};
	}

	@Override
	protected String[] getSupportedEdgeTags() {
		return NOTHING;
	}
	
	@Override
	public String getTitle() {
		return "Loop Termination Dependence Graph (TDG)";
	}

	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		// get a selected loop header
		Q loopHeader = filter(event.getSelection());

		// nothing to compute...
		if (CommonQueries.isEmpty(loopHeader)){
			return null;
		}
		
		// EFG only applies to one loop header at a time
		if(loopHeader.eval().nodes().size() > 1){
			return null;
		}

		// termination conditions for loop header
		Q tc = TerminatingConditions.findTerminatingConditionsForLoopHeader(loopHeader);

		// all taints
		Q taints = Common.edges(TaintOverlay.Taint).differenceEdges(Common.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT));
		
//		Q conditions = tc.children().nodesTaggedWithAny(XCSG.DataFlowCondition);
		/*// method containing loop header
		Q method = CommonQueries.getContainingFunctions(loopHeader);

		// method contents
		Q methodContents = CommonQueries.localDeclarations(method);

		// local taints influencing loop termination (taint events)
		Q loopTerminationTaints = conditions.reverseOn(taints).intersection(methodContents);

		// EFG of loop terminating conditions and statements influence terminating conditions (loop termination slice)
		Q events = loopTerminationTaints.parent().nodesTaggedWithAny(XCSG.ControlFlow_Node);
		Q efg = EFGFactory.EFG(CommonQueries.cfg(method), events);
		
		// loop termination taints
		Q loopTerminationTaintGraph = loopTerminationTaints.induce(taints);*/

		Q loopTerminationTaintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
		// loop termination taint roots
		Q taintRoots = loopTerminationTaintGraph.roots();

		// loop termination taint roots minus any known values instantiated inside the method
		Q unknownTaintRoots = taintRoots.difference(Common.universe().nodesTaggedWithAny(XCSG.Literal, XCSG.Instantiation, XCSG.ArrayInstantiation, XCSG.Parameter, XCSG.DynamicDispatchCallSite, XCSG.InstanceVariableAccess));
		
		// color the loop termination conditions red
		Markup m = new Markup();
		m.setNode(tc, MarkupProperty.NODE_BACKGROUND_COLOR, Color.RED);
		m.setEdge(taints, MarkupProperty.EDGE_COLOR, mediumGreen);
	
		m.setNode(taintRoots, MarkupProperty.NODE_BACKGROUND_COLOR, Color.GRAY);
		m.setNode(unknownTaintRoots, MarkupProperty.NODE_BACKGROUND_COLOR, Color.BLACK);

		return SimpleScriptUtil.evaluate(taintRoots, reverse, forward, loopTerminationTaintGraph, m);
	}

	@Override
	public int getDefaultStepBottom() {
		return 0;
	}

	@Override
	public int getDefaultStepTop() {
		return 0;
	}

	@Override
	protected StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
		return null;
	}

}