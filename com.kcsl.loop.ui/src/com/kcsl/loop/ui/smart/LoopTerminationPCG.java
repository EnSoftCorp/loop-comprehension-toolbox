package com.kcsl.loop.ui.smart;

import java.awt.Color;

import com.kcsl.loop.ui.smart.ControlFlowSelection;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.ui.scripts.selections.AbstractAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.ensoftcorp.open.pcg.common.highlighter.PCGHighlighter;
import com.kcsl.loop.catalog.monotonicity.LoopAbstractions;
import com.kcsl.loop.util.TerminatingConditions;

/**
 * Input:
 * One ControlFlow_Node corresponding to loop header
 * 
 * Output:
 * The PCG where events are:
 * 1) Roots of the loop termination taint graph of the loop
 * 2) Conditions in the taint graph
 * 3) Relevant callsites (e.g. iterator)
 * 
 * In the RULER Manual, the output is described as:
 * Projected Control Graph with respect to events automatically determined from loop header. 
 * The events determined consist of - the loop header itself, boundary conditions, instantiation 
 * of variables governing the boundary conditions and all callsites which either 
 * 1) dominate at least one boundary condition on paths originating from the loop header, or 
 * 2) modify at least one of the loop control variables of the selected loop header. 
 * The nested loops and events inside them are included.
 * 
 * 
 * Markup:
 * Standard PCG + 
 * 
 * RED    = Boundary Condition
 * CYAN   = Non-boundary condition
 * GREEN  = CallSite
 */
public class LoopTerminationPCG extends AbstractAtlasSmartViewScript implements AtlasSmartViewScript {

		@Override
		public String getTitle() {
			return "RULER Loop Termination PCG";
		}

		@Override
		public StyledResult selectionChanged(IAtlasSelectionEvent arg0) {
			ControlFlowSelection cfSelection = ControlFlowSelection.processSelection(arg0);

//			Q events = cfSelection.getImpliedControlFlow();
//			
//			LCG_PCG lcg_PCG = LCGUtil.LCG_PCG(methods, events, true, true);
//			Q pcg = lcg_PCG.pcg;
//			
			Q loopHeaders = cfSelection.getSelectedControlFlow().nodes(XCSG.Loop);
			AtlasSet<Node> loopHeadersSet = loopHeaders.eval().nodes();
			if(loopHeadersSet.size() != 1){
				// must select a single LoopHeader
				return null;
			}
			Node loopHeader = loopHeadersSet.one();
			
			if(CommonQueries.isEmpty(loopHeaders)) {
				// script only responds to loop headers
				return null;
			}
			
			Q taintPCG = LoopAbstractions.getTerminationPCG(loopHeader);
			Q loopCfg = LoopAbstractions.loopControlFlowGraph(loopHeader);
			Q tcs = TerminatingConditions.findBoundaryConditionsForLoopHeader(loopHeader);
			Q nonTcs = taintPCG.nodes(XCSG.ControlFlowCondition).difference(tcs);
			Q callsites = taintPCG.children().nodes(XCSG.CallSite);
			
			Markup m = new Markup(PCGHighlighter.getPCGMarkup(Query.empty()));

			m.setNode(nonTcs, MarkupProperty.NODE_BACKGROUND_COLOR, Color.CYAN);
			m.setNode(loopCfg.nodes(BoundaryConditions.BOUNDARY_CONDITION), MarkupProperty.NODE_BACKGROUND_COLOR, Color.RED);
			m.setNode(callsites.parent(), MarkupProperty.NODE_BACKGROUND_COLOR, Color.GREEN);

			return new StyledResult(taintPCG, m, Common.toQ(loopHeader));
		}
}
