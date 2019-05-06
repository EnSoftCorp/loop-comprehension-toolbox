package com.kcsl.loop.catalog.monotonicity;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.pcg.common.PCGFactory;
import com.kcsl.loop.taint.analysis.TaintOverlay;
import com.kcsl.loop.util.TerminatingConditions;

/**
 * @author Payas Awadhutkar
 */

public class LoopAbstractions {

	public static Q getFullTerminationDependenceGraph(Node loopHeader) {
		// this will get you the entire reverse taint from termination conditions of given loop header.
		if(!loopHeader.taggedWith(XCSG.Loop)) {
			throw new RuntimeException("Not a loop header");
		}
		
		// terminating Conditions
		Q tc = TerminatingConditions.findBoundaryConditionsForLoopHeader(loopHeader);
		
		// all taints
		Q taints = Query.universe().edges(TaintOverlay.Taint);

		// local taints influencing loop termination (taint events)
		Q conditions = tc.children().nodes(XCSG.DataFlowCondition);
		Q loopTerminationTaints = conditions.reverseOn(taints);
				
		// loop termination taints
		Q loopTerminationTaintGraph = loopTerminationTaints.induce(taints);
		
		// callsites in Taint graph
		Q callsites = loopTerminationTaintGraph.nodes(XCSG.DynamicDispatchCallSite);
		
		// forwarding taint
		Q missing = callsites.forwardOn(Common.edges(XCSG.LocalDataFlow));
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		// pulling in missed induction variable assignments
		missing = loopTerminationTaintGraph.nodes(XCSG.IdentityPass).parent().children().nodesTaggedWithAll(XCSG.Assignment);
		missing = Common.edges(TaintOverlay.Taint).between(loopTerminationTaintGraph, missing);
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		//DisplayUtil.displayGraph(loopTerminationTaintGraph.eval());
		
		return loopTerminationTaintGraph;
						
	}
	
	public static Q getLocalTerminationDependenceGraph(Node loopHeader) {
		// this gives you a taint graph restricted at intra-procedural level
		// but short circuit edges corresponding to constructors are retained
		if(!loopHeader.taggedWith(XCSG.Loop)) {
			throw new RuntimeException("Not a loop header");
		}
		
		// terminating Conditions
		Q tc = TerminatingConditions.findBoundaryConditionsForLoopHeader(loopHeader);
		
		// all taints
		Q taints = Query.universe().edges(TaintOverlay.Taint);

		// method containing loop header
		Node method = CommonQueries.getContainingFunction(loopHeader);

		// method contents
		Q methodContents = CommonQueries.localDeclarations(Common.toQ(method));

		// local taints influencing loop termination (taint events)
		Q conditions = tc.children().nodes(XCSG.DataFlowCondition);
		Q loopTerminationTaints = conditions.reverseOn(taints).intersection(methodContents);
				
		// loop termination taints
		Q loopTerminationTaintGraph = loopTerminationTaints.induce(taints);
		
		// callsites in Taint graph
		Q callsites = loopTerminationTaintGraph.nodes(XCSG.DynamicDispatchCallSite);
		
		// forwarding taint
		Q missing = callsites.forwardOn(Common.edges(XCSG.LocalDataFlow));
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		// pulling in missed induction variable assignments
		missing = loopTerminationTaintGraph.nodes(XCSG.IdentityPass).parent().children().nodesTaggedWithAll(XCSG.Assignment);
		missing = Common.edges(TaintOverlay.Taint).between(loopTerminationTaintGraph, missing);
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		//DisplayUtil.displayGraph(loopTerminationTaintGraph.eval());
		
		return loopTerminationTaintGraph;
						
	}
	
	public static Q taintGraphWithoutCycles(Node loopHeader) {
		Q u = Query.universe();

		if(!loopHeader.taggedWith(XCSG.Loop)) {
			throw new RuntimeException("Not a loop header");
		}
		
		// terminating Conditions
		Q tc = TerminatingConditions.findBoundaryConditionsForLoopHeader(loopHeader);
		
		// all taints without the cycle causing edges, restricted to local taint (enclosing method body)
		Q taints = u.edges(TaintOverlay.Taint).differenceEdges(u.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT, XCSG.InterproceduralDataFlow));;

		// local taints influencing loop termination (taint events)
		Q conditions = tc.children().nodes(XCSG.DataFlowCondition);
		// loop termination taints
		Q loopTerminationTaints = conditions.reverseOn(taints);
		
		// TODO: [jdm] comment needed
		Q loopTerminationTaintGraph = loopTerminationTaints.nodes(XCSG.Assignment).forwardOn(taints).union(loopTerminationTaints);
		
		loopTerminationTaintGraph = Query.resolve(null, loopTerminationTaintGraph);
		return loopTerminationTaintGraph;
	}

	public static Q getTerminationPCG(Node loopHeader) {

		// taint graph
		Q taintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
		// getting rid of cycles
		Q leaves = taintGraph.leaves();
		// conditions
		Q conditions = leaves.nodes(XCSG.DataFlowCondition);
		Q cycleEdges = Common.edges(TaintOverlay.Taint).differenceEdges(Common.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT));
		taintGraph = leaves.reverseOn(cycleEdges);
		// taint roots
		Q taintRoots = taintGraph.roots();
		// operators
		Q operators = taintGraph.nodes(TaintOverlay.OVERLAY_OPERATOR);
		// loop Cfg
		Q loopCfg = loopControlFlowGraph(loopHeader);
		Q callsites = loopCfg.contained().nodes(XCSG.CallSite);
//		Q nonTcs = loopCfg.nodes(XCSG.ControlFlowCondition);
		//AtlasSet<Node> loopCfgNodes = loopCfg.eval().nodes();	
		// events for PCG
		Q dfEvents = taintRoots;
		dfEvents = dfEvents.union(conditions);
		dfEvents = dfEvents.union(operators);
		dfEvents = dfEvents.union(callsites);
		Q method = Common.toQ(loopHeader).containers().nodes(XCSG.Method);
		Q methodsCFG = CommonQueries.cfg(method);
		Q events = dfEvents.parent();
		events = events.union(Common.toQ(loopHeader)).intersection(methodsCFG);
		
		Q taintPCG = Common.empty();
		// pcg
		if(!CommonQueries.isEmpty(events.nodes(XCSG.ControlFlow_Node))) {
			taintPCG = PCGFactory.create(events).getPCG();
		}
		return taintPCG;
	}


	public static Q loopControlFlowGraph(Node loopHeader) {
		return Common.edges(XCSG.ControlFlow_Edge).between(Common.toQ(loopHeader), Common.toQ(loopHeader));		
	}

}
