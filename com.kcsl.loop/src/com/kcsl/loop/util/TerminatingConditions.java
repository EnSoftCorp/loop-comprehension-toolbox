package com.kcsl.loop.util;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;

public class TerminatingConditions {
	
	/**
	 * 
	 * @param loopHeader
	 * @return Nodes (XCSG.ControlFlow_Node) which are boundary conditions for the given loop header 
	 */
	public static Q findBoundaryConditionsForLoopHeader(Node loopHeader){
		if (loopHeader == null){
			return Query.empty();
		}
		
		if (!loopHeader.taggedWith(XCSG.Loop))
			throw new IllegalArgumentException("Expected a loop header");
		if (!loopHeader.hasAttr(CFGNode.LOOP_HEADER_ID))
			throw new IllegalArgumentException("Expected a loop header id");
		
		Object loopHeaderId = loopHeader.getAttr(CFGNode.LOOP_HEADER_ID);

		if (loopHeaderId == null)
			throw new IllegalArgumentException("Expected a non-null loop header");

		Q allLoopMembers = Query.universe().selectNode(CFGNode.LOOP_MEMBER_ID, loopHeaderId);
		
		Q boundaryMemberConditions = allLoopMembers.nodes(BoundaryConditions.BOUNDARY_CONDITION); 
		Q boundaryLoopHeader = Common.toQ(loopHeader).nodes(BoundaryConditions.BOUNDARY_CONDITION);
		
		Q boundaryConditions = boundaryMemberConditions.union(boundaryLoopHeader);
		
		boundaryConditions = Query.resolve(null, boundaryConditions);
		
		return boundaryConditions;
	}

}
