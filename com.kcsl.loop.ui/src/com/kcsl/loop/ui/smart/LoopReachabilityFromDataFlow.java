package com.kcsl.loop.ui.smart;

import java.awt.Color;

import com.kcsl.loop.catalog.monotonicity.MonotonicityPatternConstants;

import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.kcsl.loop.taint.analysis.LoopReachability;
import com.kcsl.loop.ui.log.Log;

/**
 * Input:
 * One or more 'user input' nodes
 * user input : A data flow node which analyst thinks user can control
 *
 * Output:
 * All the loops which are reachable via taint from the user input
 *
 * Highlights:
 * 
 * 
 * @author payas
 */

public class LoopReachabilityFromDataFlow extends FilteringAtlasSmartViewScript implements AtlasSmartViewScript {
	
	@Override
    protected String[] getSupportedNodeTags() {
            return EVERYTHING;
    }

    @Override
    protected String[] getSupportedEdgeTags() {
            return NOTHING;
    }
	
	@Override
	public String getTitle() {
		return "RULER Reachable Loops (From Input)";
	}
	
	
	@Override
	public StyledResult selectionChanged(IAtlasSelectionEvent event, Q input) {
		if(CommonQueries.isEmpty(input)) {
			Log.info("empty input");
			return null;
		}
		
		Q view = LoopReachability.getLoopReachabilityViaDataFlowView(input, true);
		Q loops = view.nodes(XCSG.Loop);
		Q monotonicLoops = loops.nodes(MonotonicityPatternConstants.MONOTONIC_LOOP);
		Q nonMonotonicLoops = loops.difference(monotonicLoops);
		// Q efEdges = view.edges("EventFlow_Edge");

		Markup m = new Markup();
		m.set(monotonicLoops, MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		m.set(nonMonotonicLoops, MarkupProperty.NODE_BACKGROUND_COLOR, Color.RED);
		// m.setEdge(efEdges, MarkupProperty.EDGE_COLOR, Color.MAGENTA);
		return new StyledResult(view,m);
	}
	

}
