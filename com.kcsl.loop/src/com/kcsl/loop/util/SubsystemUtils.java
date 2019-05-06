package com.kcsl.loop.util;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;

/**
 * This is the place to add any subsystem related utilities
 * 
 * @author Payas Awadhutkar
 */
public class SubsystemUtils {
	
	/**
	 * 
	 * @param statementsWithCallsites
	 * @return
	 */
	public static Q findSubsetWithJDKTargets(Q statementsWithCallsites) {
		// FIXME: [jdm] this should be cached for performance reasons
		Q jdkMethods = SetDefinitions.JDKLibraries().contained().nodes(XCSG.Method);
		
		Q jdkCallSites = Common.empty();
		
		for (Node callsite : statementsWithCallsites.eval().nodes()) {
//			Log.info(callsite.toString());
			Q dfCallsites = Common.toQ(callsite).contained().nodesTaggedWithAll(XCSG.CallSite);
			for(Node dfcs : dfCallsites.eval().nodes()) { 
				Q targetMethods = CallSiteAnalysis.getTargets(Common.toQ(dfcs));
//				Log.info(targetMethods.eval().nodes().size()+"");
				AtlasSet<com.ensoftcorp.atlas.core.db.graph.Node> nonJDKtargets = targetMethods.difference(jdkMethods).eval().nodes();
//				Log.info(nonJDKtargets.size()+"");
				if (nonJDKtargets.isEmpty()) {
					// all targets (including those possibly due to dynamic dispatch) are in the JDK
					jdkCallSites = jdkCallSites.union(Common.toQ(callsite));
				}
			}
		}
		return jdkCallSites;
	}
	
/*	public static Q findSubsetWithDeepJDKTargets(Q statementsWithCallsites) {
		
		Q callEdges = Query.universe().edges(XCSG.Call);
		
		Q jdkMethods1 = Query.universe().nodes(XCSG.Library).selectNode(XCSG.name, "rt.jar").contained().nodes(XCSG.Method);
		Q jdkMethods2 = Query.universe().nodes(XCSG.Library).selectNode(XCSG.name, "jce.jar").contained().nodes(XCSG.Method);
		Q jdkMethods3 = Query.universe().nodes(XCSG.Library).selectNode(XCSG.name, "jsse.jar").contained().nodes(XCSG.Method);
		Q jdkMethods = Common.resolve(null, jdkMethods1.union(jdkMethods2, jdkMethods3));
		
		Q deepJdkCallSites = Common.empty();
		
		for (Node statement : statementsWithCallsites.eval().nodes()) {
			Q dfCallsites = Common.toQ(statement).contained().nodesTaggedWithAll(XCSG.CallSite);
			for(Node dfcs : dfCallsites.eval().nodes()) { 
				Q targetMethods = CallSite.getTargetMethods(dfcs);
				
				// get the descendants of the target methods on the call graph
				Q deepTargetMethods = callEdges.forward(targetMethods).nodes(XCSG.Method);
				
				AtlasSet<Node> nonJDKtargets = deepTargetMethods.difference(jdkMethods).eval().nodes();
				if (nonJDKtargets.isEmpty()) {
					// all deep targets are in JDK
					deepJdkCallSites = deepJdkCallSites.union(Common.toQ(statement));
				}
			}
		}
		return deepJdkCallSites;
	}
	*/
	
	public static Q findJDKAPIsEventuallyCalled(Q statementsWithCallsites) {
		
		Q callEdges = Query.universe().edges(XCSG.Call);
		
		Q jdkMethods = SetDefinitions.JDKLibraries().contained().nodes(XCSG.Method);
		
		Q jdkAPIsCalled = Common.empty();
		
		for (Node statement : statementsWithCallsites.eval().nodes()) {
			Q dfCallsites = Common.toQ(statement).contained().nodesTaggedWithAll(XCSG.CallSite);
			for(Node dfcs : dfCallsites.eval().nodes()) { 
				Q targetMethods = CallSiteAnalysis.getTargets(Common.toQ(dfcs));
				
				// get the descendants of the target methods on the call graph
				Q deepTargetMethods = callEdges.forward(targetMethods).nodes(XCSG.Method);
				
				jdkAPIsCalled = jdkAPIsCalled.union(deepTargetMethods.intersection(jdkMethods));
			}
		}
		return jdkAPIsCalled;
	}

}
