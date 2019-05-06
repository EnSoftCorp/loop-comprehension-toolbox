package com.kcsl.loop.ui.smart;


import java.awt.Color;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.ui.scripts.selections.AbstractAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.highlighter.CFGHighlighter;
import com.kcsl.loop.util.LoopUtils;
import com.kcsl.loop.util.TerminatingConditions;

/**
 * Input:
 * A single Loop Header
 * 
 * Output:
 * The CFG for the loop, including nested loops.
 * 
 * Markup: 
 * Standard CFG +
 * Terminating Conditions for the selected loop header in RED.
 *   Markup is not applied to nested loops.
 * 
 */
public class LoopBodyView extends AbstractAtlasSmartViewScript implements AtlasSmartViewScript{

	@Override
	public String getTitle() {
		return "RULER Loop Body";
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
		
//		Q method = Common.toQ(CommonQueries.getContainingFunction(loopHeader));
//		Q cfg = CommonQueries.excfg(method);
		Q cfg = LoopUtils.getControlFlowGraph(loopHeader);
		
		Markup m = new Markup();
		CFGHighlighter.applyHighlightsForCFG(m);
		
		m.setNode(terminatingConditions, MarkupProperty.NODE_BACKGROUND_COLOR, Color.RED);
		
		return new StyledResult(cfg, m, Common.toQ(loopHeader));
	}
}
