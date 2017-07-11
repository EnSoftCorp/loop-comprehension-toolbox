package com.ensoftcorp.open.loop.comprehension.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.AbstractAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.highlighter.CFGHighlighter;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.jimple.commons.loops.LoopHighlighter;
import com.ensoftcorp.open.loop.comprehension.analysis.LoopAbstractions;
import com.ensoftcorp.open.loop.comprehension.taint.TaintOverlay;
import com.ensoftcorp.open.pcg.common.PCGFactory;

/**
 * Input:
 * One ControlFlow_Node corresponding to loop header
 * 
 * Output:
 * The Event Flow Graph where events are,
 * 1) Roots of the loop termination taint graph of the loop
 * 2) Conditions in the taint graph
 * 3) Relevant callsites (e.g. iterator)
 * 
 * Highlights: 
 * CFG nodes are darker the deeper they are nested under loop headers. 
 * GRAY  = ControlFlowBackEdge
 * WHITE = true ControlFlow_Edges
 * BLACK = false ControlFlow_Edges
 * BLUE  = Loop Header
 * CYAN  = events (ControlFlow_Node, DataFlow_Node)
 */
public class LPCGSmartView extends AbstractAtlasSmartViewScript implements AtlasSmartViewScript{

		@Override
		public String getTitle() {
			return "Loop Projected Control Flow Graph (LPCG)";
		}

		@Override
		public StyledResult selectionChanged(IAtlasSelectionEvent arg0) {
			ControlFlowSelection cfSelection = ControlFlowSelection.processSelection(arg0);

			Q methods = cfSelection.getImpliedMethods();
			
			if (methods.eval().nodes().isEmpty()){
				return null;
			}
			
//			Q events = cfSelection.getImpliedControlFlow();
//			
//			LCG_PCG lcg_PCG = LCGUtil.LCG_PCG(methods, events, true, true);
//			Q pcg = lcg_PCG.pcg;
//			
			Q loopHeader = cfSelection.getSelectedControlFlow().nodesTaggedWithAny(CFGNode.LOOP_HEADER);

			// taint graph
			Q taintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
			// getting rid of cycles
			Q leaves = taintGraph.leaves();
			// conditions
			Q conditions = leaves.nodesTaggedWithAny(XCSG.DataFlowCondition);
			Q cycleEdges = Common.edges(TaintOverlay.Taint).differenceEdges(Common.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT));
			taintGraph = leaves.reverseOn(cycleEdges);
			// taint roots
			Q taintRoots = taintGraph.roots();
			// operators
			Q operators = taintGraph.nodesTaggedWithAny(TaintOverlay.OVERLAY_OPERATOR);
			// loop Cfg
			Q loopCfg = loopCfg(loopHeader);
			Q callsites = loopCfg.contained().nodesTaggedWithAny(XCSG.CallSite);
			Q nonTcs = loopCfg.nodesTaggedWithAny(XCSG.ControlFlowCondition);
			//AtlasSet<GraphElement> loopCfgNodes = loopCfg.eval().nodes();
			// events for Efg
			Q events = taintRoots;
			events = events.union(conditions);
			events = events.union(operators);
			events = events.union(callsites);
			
			Q methodsCFG = CommonQueries.cfg(methods);
			events = events.union(events.parent()).intersection(methodsCFG);
			
			// pcg
			Q taintPCG = PCGFactory.create(events.nodes(XCSG.ControlFlow_Node)).getPCG();

			Markup m = new Markup();
			LoopHighlighter.applyHighlightsForLoopDepth(m);
			CFGHighlighter.applyHighlightsForCFEdges(m);

//			h.highlightNodes(loopHeader, Color.BLUE);
//			h.highlightNodes(events, Color.CYAN);
			m.setNode(nonTcs, MarkupProperty.NODE_BACKGROUND_COLOR, Color.CYAN);
			m.setNode(loopCfg.nodesTaggedWithAny(BoundaryConditions.BOUNDARY_CONDITION), MarkupProperty.NODE_BACKGROUND_COLOR, Color.RED);
			m.setNode(callsites.parent(), MarkupProperty.NODE_BACKGROUND_COLOR, Color.GREEN);
			m.setNode(loopHeader, MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);

			return new StyledResult(taintPCG, m);
		}
		
		public static Q loopCfg(Q loopHeader) {
			return Common.edges(XCSG.ControlFlow_Edge).between(loopHeader, loopHeader);		
		}

}
