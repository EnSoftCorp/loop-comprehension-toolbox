package com.ensoftcorp.open.loop.comprehension.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.taint.LoopReachability;
import com.ensoftcorp.open.loop.comprehension.utils.EntryPoints;

public class ReachabilityFilter extends NodeFilter{

	public static final String MAIN_METHODS = "Main Methods";
	public static final String HTTP_HANDLER_METHODS = "HTTP Handler Methods";
	
	public ReachabilityFilter() {
		this.addPossibleFlag(MAIN_METHODS, "Returns loops reachable only from main methods");
		this.addPossibleFlag(HTTP_HANDLER_METHODS, "Returns loops reachable only from HTTP Handlers");
	}
	@Override
	public String getName() {
		return "Reachable Loops";
	}

	@Override
	public String getDescription() {
		return "Returns reachable loops from specified app entry points.";
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { CFGNode.LOOP_HEADER };
	}

	public Q filter(Q input, Map<String, Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filter(input, parameters);
		

		Q entryPoints = Common.empty();
		if (this.isFlagSet(HTTP_HANDLER_METHODS, parameters)) {
			entryPoints = entryPoints.union(EntryPoints.getHTTPRequestHandlers());
		}
		if (this.isFlagSet(MAIN_METHODS, parameters)) {
			entryPoints = entryPoints.union(EntryPoints.getMainMethods());
		}
		

		Q userInputs = entryPoints.forwardStepOn(Common.edges(XCSG.Contains)).nodes(XCSG.Parameter);
		
		Q reachableLoops = LoopReachability.getLoopReachabilityViaDataFlowView(userInputs,true);

		return super.filter(reachableLoops, parameters);
	}
}
