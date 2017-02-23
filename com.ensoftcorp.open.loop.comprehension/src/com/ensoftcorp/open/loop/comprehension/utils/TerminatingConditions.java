package com.ensoftcorp.open.loop.comprehension.utils;

import static com.ensoftcorp.atlas.core.script.Common.toGraph;
import static com.ensoftcorp.atlas.core.script.Common.toQ;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.StandardQueries;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.pcg.factory.PCGFactory;

public class TerminatingConditions {
	
	public static final String TERMINATING_CONDITION = "TERMINATING_CONDITION";

	public static void tagTerminatingConditions() {
		Q headers = Query.universe().nodesTaggedWithAny(CFGNode.LOOP_HEADER);
		for (GraphElement header : headers.eval().nodes()) {
			Q q = findTerminatingConditionsForLoopHeader(toQ(toGraph(header)));
			Tags.setTag(q, TERMINATING_CONDITION);
		}
	}

	/**
	 * Finds the terminating conditions for a given loopHeader
	 * @param loopHeader
	 * @return
	 */
	public static Q findTerminatingConditionsForLoopHeader(Q loopHeader){
		
		if (loopHeader.eval().nodes().size() > 1){
			throw new IllegalArgumentException("Expected at most one loop header");
		}
		
		Node geLoopHeader = loopHeader.eval().nodes().getFirst();
		
		if (geLoopHeader == null){
			return Query.empty();
		}
		
		Q method = Common.toQ(StandardQueries.getContainingFunction(geLoopHeader));
		Q PCG = PCGFactory.PCGforMethod(method, loopHeader);
		
		Q allConditions = PCG.nodesTaggedWithAll(XCSG.ControlFlowCondition);
		
		Object loopHeaderId = geLoopHeader.getAttr(CFGNode.LOOP_HEADER_ID);
		
		Q terminatingMemberConditions = allConditions.selectNode(CFGNode.LOOP_MEMBER_ID, loopHeaderId);
		Q terminatingLoopHeader = allConditions.intersection(loopHeader);
		
		Q terminatingConditions = terminatingMemberConditions.union(terminatingLoopHeader);
		
		return terminatingConditions;
	}

}
