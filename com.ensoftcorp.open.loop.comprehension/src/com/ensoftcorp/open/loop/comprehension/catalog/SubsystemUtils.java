package com.ensoftcorp.open.loop.comprehension.catalog;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.open.java.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.loop.comprehension.utils.JDKContainer;

/**
 * This is the place to add any subsystem related utilities
 * 
 * @author Payas Awadhutkar
 */
public class SubsystemUtils {
	
	public static Q findSubsetWithJDKTargets(Q statementsWithCallsites) {
		Q jdkMethods = JDKContainer.jdkMethods();
		
		Q jdkCallSites = Common.empty();
		
		for (GraphElement callsite : statementsWithCallsites.eval().nodes()) {
//			Log.info(callsite.toString());
			Q dfCallsites = Common.toQ(callsite).contained().nodesTaggedWithAll(XCSG.CallSite);
			for(GraphElement dfcs : dfCallsites.eval().nodes()) { 
				Q targetMethods = CallSiteAnalysis.getTargetMethods(dfcs);
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
		
		Q callEdges = Common.universe().edgesTaggedWithAny(XCSG.Call);
		
		Q jdkMethods1 = Common.universe().nodesTaggedWithAny(XCSG.Library).selectNode(XCSG.name, "rt.jar").contained().nodesTaggedWithAny(XCSG.Method);
		Q jdkMethods2 = Common.universe().nodesTaggedWithAny(XCSG.Library).selectNode(XCSG.name, "jce.jar").contained().nodesTaggedWithAny(XCSG.Method);
		Q jdkMethods3 = Common.universe().nodesTaggedWithAny(XCSG.Library).selectNode(XCSG.name, "jsse.jar").contained().nodesTaggedWithAny(XCSG.Method);
		Q jdkMethods = Common.resolve(null, jdkMethods1.union(jdkMethods2, jdkMethods3));
		
		Q deepJdkCallSites = Common.empty();
		
		for (GraphElement statement : statementsWithCallsites.eval().nodes()) {
			Q dfCallsites = Common.toQ(statement).contained().nodesTaggedWithAll(XCSG.CallSite);
			for(GraphElement dfcs : dfCallsites.eval().nodes()) { 
				Q targetMethods = CallSite.getTargetMethods(dfcs);
				
				// get the descendants of the target methods on the call graph
				Q deepTargetMethods = callEdges.forward(targetMethods).nodesTaggedWithAny(XCSG.Method);
				
				AtlasSet<GraphElement> nonJDKtargets = deepTargetMethods.difference(jdkMethods).eval().nodes();
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
		
		Q callEdges = Common.universe().edgesTaggedWithAny(XCSG.Call);
		
		Q jdkMethods = JDKContainer.jdkMethods();
		
		Q jdkAPIsCalled = Common.empty();
		
		for (GraphElement statement : statementsWithCallsites.eval().nodes()) {
			Q dfCallsites = Common.toQ(statement).contained().nodesTaggedWithAll(XCSG.CallSite);
			for(GraphElement dfcs : dfCallsites.eval().nodes()) { 
				Q targetMethods = CallSiteAnalysis.getTargetMethods(dfcs);
				
				// get the descendants of the target methods on the call graph
				Q deepTargetMethods = callEdges.forward(targetMethods).nodesTaggedWithAny(XCSG.Method);
				
				jdkAPIsCalled = jdkAPIsCalled.union(deepTargetMethods.intersection(jdkMethods));
			}
		}
		return jdkAPIsCalled;
	}

}
