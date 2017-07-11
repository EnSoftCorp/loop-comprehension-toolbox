package com.ensoftcorp.open.loop.comprehension.utils;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/*
 * Utilities related to differential branches
 * 
 * @author Ganesh Ram Santhanam
 */

public class Observables {
	
	public static Q getNTBranchesGovernedByCollectionSize() {
		Q ntBranches = Common.nodes(XCSG.DataFlowCondition).difference(Common.nodes("BOUNDARY_CONDITION").contained());
		AtlasSet<Node> res = new AtlasHashSet<Node>();
		for(Node branch: ntBranches.eval().nodes()) {
			Q cs = Common.toQ(branch).reverseOn(Common.edges(XCSG.DataFlow_Edge)).nodes(XCSG.CallSite);
			if(cs == null || CommonQueries.isEmpty(cs)){
				continue;
			}
			Q methodTargets = getMethodTargetsForCallsites(cs);
			Q m = methodTargets.intersection(getCollectionSizeMethod());
			if(!CommonQueries.isEmpty(m)) {
				res.add(branch);
			}	
		}
		return Common.toQ(res);
	}
	
	public static Q getMethodTargetsForCallsites(Q cs) {
		Q methodTargets = Common.empty();
		for(Node callsite: cs.eval().nodes()) {
			methodTargets = methodTargets.union(CallSiteAnalysis.getTargets(Common.toQ(callsite)));
		}
		return methodTargets;
	}
	
	public static Q getCollectionSizeMethod() {
		Q csMethod = Common.types("Collection").contained().methods("size").nodes(XCSG.Method);
		Q result = csMethod.reverseOn(Common.universe().edges(XCSG.Overrides)).nodes(XCSG.Method);
		return result;
	}
}