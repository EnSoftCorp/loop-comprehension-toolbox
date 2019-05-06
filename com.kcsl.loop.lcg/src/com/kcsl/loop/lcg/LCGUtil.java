package com.kcsl.loop.lcg;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.pcg.common.PCGFactory;

public class LCGUtil {
	
	public static class LCG_PCG {
		public Q pcg;
		public Q loopHeaders;
		public Q callSites;
	}

	/**
	 * Create the event flow graph for the given method where the events are:
	 * 0- the given events
	 * 1- the loop headers in the given method;
	 * 2- the call-sites for the methods that will eventually call methods containing loop headers
	 * 
	 * @param lcg if null, one will be constructed with defaults
	 * @param method
	 * @param events
	 * @param includeLoopHeaders 
	 * @param includeCallSites True if you need to consider the call-sites as events if they eventually call methods containing loop headers.
	 * @return the event flow graph 
	 */
	public static LCG_PCG LCG_PCG(LoopCallGraph lcg, Q method, Q events, boolean includeLoopHeaders, boolean includeCallSites){
		LCG_PCG lcgpcg = new LCG_PCG();
		
		Q cfg = CommonQueries.cfg(method);
		
		if(includeLoopHeaders) {
			Q loopHeaders = events.union(cfg.nodes(XCSG.Loop));
			lcgpcg.loopHeaders = loopHeaders;
			events = events.union(loopHeaders);
		}
		
		if(includeCallSites){
			if (lcg == null){
				lcg = new LoopCallGraph();
			}
			Q callsites = findCallsiteEvents(lcg, method);
			lcgpcg.callSites = callsites;
			events = events.union(callsites);
		}

		lcgpcg.pcg = PCGFactory.create(cfg, events, false).getPCG();
		
		return lcgpcg;
	}
	
	/**
	 * Create the event flow graph for the given method where the events are:
	 * 0- the given events
	 * 1- the loop headers in the given method;
	 * 2- the call-sites for the methods that will eventually call methods containing loop headers
	 * 
	 * @return the event flow graph 
	 */
	public static LCG_PCG LCG_PCG(Q method, Q events, boolean includeLoopHeaders, boolean includeCallSites){
		return LCG_PCG(null, method, events, includeLoopHeaders, includeCallSites);
	}

	/**
	 * Given the LCG and a method of interest, find ControlFlow_Nodes containing callsites within the method which
	 * may resolve to other methods in the LCG
	 * 
	 * @param lcg
	 * @param method
	 * @return
	 */
	private static Q findCallsiteEvents(LoopCallGraph lcg, Q method) {
		
		Q callsites = method.contained().nodesTaggedWithAll(XCSG.CallSite);
		AtlasSet<Node> dfCallsites = callsites.eval().nodes();
		if (dfCallsites.isEmpty()) {
			return Common.empty();
		} 

		Q lcgQ = lcg.lcgWithPredecessorCallGraph();
		
		AtlasSet<Node> cfgCallsites = new AtlasHashSet<Node>();
		
		// if a CallSite could be resolved to a method in the LCG, include it
		
		for (Node dfCallsite : dfCallsites) {
			Q targetMethodsInLCG = CallSiteAnalysis.getTargets(Common.toQ(dfCallsite)).intersection(lcgQ);
			if (!targetMethodsInLCG.eval().nodes().isEmpty()) {
				// NOTE: the parent of a CallSite is always a ControlFlow_Node
				Q cfgCallsite = Common.toQ(Common.toGraph(dfCallsite)).parent().nodesTaggedWithAll(XCSG.ControlFlow_Node);
				cfgCallsites.addAll(cfgCallsite.eval().nodes());
			}
		}
		
		return Common.toQ(Common.toGraph(cfgCallsites));
	}

}
