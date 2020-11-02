package com.kcsl.loop.ui.smart;

import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.highlighter.CFGHighlighter;

/**
 * Input: One or more Methods or Loop Headers
 *
 *
 * Output: For each input method, all the loop headers contained with induced
 * Loop Child edges are shown. For each input loop header, all the loop headers 
 * connected to it by Loop Child edges (in either direction) are shown.
 *
 * Highlights: * Selected Loop/Method = YELLOW, Loop Headers = BLUE
 * the deeper the loop header, the darker the shade of BLUE used.
 *
 */
public class LoopNestingView extends FilteringAtlasSmartViewScript implements AtlasSmartViewScript {

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { XCSG.Loop, XCSG.Method };
	}

	@Override
	protected String[] getSupportedEdgeTags() {
		return NOTHING;
	}

	@Override
	public String getTitle() {
		return "RULER Loop Nesting View";
	}

	@Override
	protected StyledResult selectionChanged(IAtlasSelectionEvent event, Q filteredSelection) {
		if (CommonQueries.isEmpty(filteredSelection)) {
			return null;
		}

		Q nestingGraph = getLoopNestingGraph(filteredSelection);

		Markup m = new Markup();
		CFGHighlighter.applyHighlightsForCFG(m);

//		m.setNode(filteredSelection, MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW.brighter().brighter());
		return new StyledResult(nestingGraph, m);
	}

	private Q getLoopNestingGraph(Q loopHeaderOrMethod) {
		Q nestingGraph = Common.empty();

		if (loopHeaderOrMethod.nodes(XCSG.Method).eval().nodes().size() > 0) {
			nestingGraph = nestingGraph.union(loopHeaderOrMethod.contained()
					.nodes(XCSG.Loop));
		} else if (loopHeaderOrMethod.nodes(XCSG.Loop).eval().nodes().size() > 0) {
			nestingGraph = nestingGraph.union(loopHeaderOrMethod.nodes(XCSG.Loop)
					       .reverseOn(Query.universe().edges(XCSG.LoopChild)).nodes(XCSG.Loop));
			nestingGraph = nestingGraph.union(loopHeaderOrMethod.nodes(XCSG.Loop)
				       .forwardOn(Query.universe().edges(XCSG.LoopChild)).nodes(XCSG.Loop));
		}
		return nestingGraph.induce(Query.universe().edges(XCSG.LoopChild));
	}
}
