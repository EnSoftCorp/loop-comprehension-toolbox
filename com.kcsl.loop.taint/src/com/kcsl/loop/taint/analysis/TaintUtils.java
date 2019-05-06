package com.kcsl.loop.taint.analysis;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;

public class TaintUtils {

	/**
	 * Returns a taint slice of the events passed to a method's parameters
	 * @param methods
	 * @return
	 */
	public static Q getParameterUsageEvents(Q methods){
		Q callsiteParametersPassed = CallSiteAnalysis.getCallSites(methods).parent().children().nodes(XCSG.ParameterPass);
		Q taint = Common.edges(TaintOverlay.Taint).differenceEdges(Common.edges(XCSG.InterproceduralDataFlow)); // local taint
	    Q updates = taint.reverse(callsiteParametersPassed);
	    Q events = updates.parent();
	    return events;
	}
	
}
