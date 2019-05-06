package com.kcsl.loop.ui.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
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
import com.kcsl.loop.util.TerminatingConditions;

/**
 * Input:
 * A single Loop Header
 * 
 * Output:
 * The CFG for the enclosing method.
 * 
 * Highlights: 
 * Terminating Conditions for the selected loop header in RED
 * Selected loop header in CYAN 
 * 
 * CFG nodes are darker the deeper they are nested under loop headers. 
 * GRAY  = ControlFlowBackEdge
 * GREEN = true ControlFlow_Edges
 * RED   = false ControlFlow_Edges
 * BLUE  = ExceptionalControlFlow_Edges
 * CYAN  = events (ControlFlow_Node, DataFlow_Node)
 */
public class LoopTerminatingConditions extends AbstractAtlasSmartViewScript implements AtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "RULER Loop Terminating Conditions (LTCs)";
	}

	@Override
	public StyledResult selectionChanged(IAtlasSelectionEvent arg0) {
		Q selected = arg0.getSelection();

		AtlasSet<Node> loopHeaders = selected.nodes(XCSG.Loop).eval().nodes();
		if(loopHeaders.size() != 1){
			// must select a single LoopHeader
			return null;
		}
		Node loopHeader = loopHeaders.one();
		
		Q terminatingConditions = TerminatingConditions.findBoundaryConditionsForLoopHeader(loopHeader);
		
		Q method = Common.toQ(CommonQueries.getContainingFunction(loopHeader));
//		Q cfg = CommonQueries.excfg(method);
		Q cfg = CommonQueries.cfg(method);
		
		Markup m = new Markup();
		CFGHighlighter.applyHighlightsForCFG(m);
		
//		h.highlight(terminatingConditions, Color.RED);
//		h.highlight(loopHeader, Color.CYAN);
		m.setNode(terminatingConditions, MarkupProperty.NODE_BACKGROUND_COLOR, Color.RED);
		m.setNode(Common.toQ(loopHeader), MarkupProperty.NODE_BACKGROUND_COLOR, Color.CYAN);
		return new StyledResult(cfg, m);
	}
}
