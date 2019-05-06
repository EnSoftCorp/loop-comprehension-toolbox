package com.kcsl.loop.ui.smart;

import com.ensoftcorp.atlas.core.db.graph.Node;
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
			nestingGraph = loopHeaderOrMethod.contained()
					.nodes(XCSG.Loop)
					.induce(Query.universe().edges(XCSG.LoopChild));
		} else if (loopHeaderOrMethod.nodes(XCSG.Loop).eval().nodes().size() > 0) {
			Node loopHeader = loopHeaderOrMethod.nodes(XCSG.Loop).eval().nodes().one();
			nestingGraph = Common.toQ(CommonQueries.getContainingFunction(loopHeader)).contained()
					.nodes(XCSG.Loop)
					.induce(Query.universe().edges(XCSG.LoopChild));
		}
		return nestingGraph;
	}
}
