package com.kcsl.loop.taint.highlighter;

import java.awt.Color;

import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;

public class TaintHighlighter {
	
	/** 
	 * The default color for taint
	 * A medium green
	 */
	public static final Color taintColor = new java.awt.Color(60,140,70);
	
	/**
	 * GRAY  = Unconditional ControlFlow Edge
	 * WHITE = Conditional True ControlFlow Edge
	 * BLACK = Conditional False ControlFlow Edge
	 * BLUE  = Exceptional ControlFlow Edge
	 * @param m
	 */
	public static void applyHighlightsForTaintEdges(Markup m) {
		Q taintEdges = Query.universe().edges(com.kcsl.loop.taint.analysis.TaintOverlay.Taint);
		m.setEdge(taintEdges, MarkupProperty.EDGE_COLOR, taintColor);
	}

}
