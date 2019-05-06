package com.kcsl.loop.taint.analysis;

import com.kcsl.loop.lcg.LoopCallGraph;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasNodeHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.kcsl.loop.taint.log.Log;

public class LoopReachability {
	
	public static Q getTaints() {
		return Query.universe().edges(TaintOverlay.Taint,"INFERRED_DATA_FLOW",XCSG.DataFlow_Edge,XCSG.PassedTo,XCSG.ArrayIdentityFor);
	}
	
	public static Q getLoopChildEdges() {
		return Query.universe().edges(XCSG.LoopChild);
	}

	public static Q getLoopReachabilityViaDataFlowView(Q input, boolean forward) {
		if(CommonQueries.isEmpty(input)) {
			Log.info("Empty input");
			return Common.empty();
		}

//		Log.info("Input: "+input.eval().nodes().one().toString());
		Q wholeTaintGraph = Common.empty();
		if(forward) {
			Q t = getTaints();
//			long tSize = t.eval().nodes().size();
			wholeTaintGraph = input.forwardOn(t);			
		} else {
			wholeTaintGraph = input.reverseOn(getTaints());
		}
//		DisplayUtils.show(wholeTaintGraph, "Whole taint");
//		Log.info("Size of taint graph: "+wholeTaintGraph.eval().nodes().size());
		Q qReachableLoopHeaders = wholeTaintGraph.containers().nodes(XCSG.Loop);
//		Log.info("Size of loop headers reached: "+reachableLoopHeaders.eval().nodes().size());
		Q reachableControlFlow = wholeTaintGraph.containers().nodes(XCSG.ControlFlow_Node);
		
		AtlasSet<Node> reachableLoopHeaders = new AtlasNodeHashSet(qReachableLoopHeaders.eval().nodes());
		for(Node m: reachableControlFlow.eval().nodes()) {
			if(m.hasAttr(CFGNode.LOOP_MEMBER_ID)) {
				Q containingLoopHeader = Query.universe().selectNode(CFGNode.LOOP_HEADER_ID, m.getAttr(CFGNode.LOOP_MEMBER_ID));
				reachableLoopHeaders.addAll(containingLoopHeader.eval().nodes());
			} else {
//				Log.info("Control flow containing relevant data flow outside of loop: "+m.toString());
			}
		}
		qReachableLoopHeaders = Common.toQ(reachableLoopHeaders);
//		Log.info("Size of loops reached after processing reachable control flow: "+reachableLoopHeaders.eval().nodes().size());
		return qReachableLoopHeaders.induce(getLoopChildEdges());
	}
	
	public static Q getBranchReachabilityViaDataFlowView(Q input, boolean forward) {
		if(CommonQueries.isEmpty(input)) {
			Log.info("Empty input");
			return Common.empty();
		}

		Q wholeTaintGraph = Common.empty();
		if(forward) {
			Q t = getTaints();
			wholeTaintGraph = input.forwardOn(t);			
		} else {
			wholeTaintGraph = input.reverseOn(getTaints());
		}
		Q reachableBranches = wholeTaintGraph.containers().nodesTaggedWithAll(XCSG.ControlFlow_Node,XCSG.ControlFlowCondition);
		
		return reachableBranches;
	}
	
	public static Q getHiddenLoopReachabilityViaDataFlowView(Q input, boolean forward) {
		if(CommonQueries.isEmpty(input))
		{
			Log.info("Empty input");
			return Common.empty();
		}
		
		Q wholeTaintGraph = Common.empty();
		if(forward) {
			wholeTaintGraph = input.forwardOn(getTaints());
		} else {
			wholeTaintGraph = input.reverseOn(getTaints());
		}
		Q reachableHiddenLoops = wholeTaintGraph.containers().nodesTaggedWithAll(XCSG.ControlFlow_Node,"HIDDEN_LOOP");
		
		return reachableHiddenLoops;
		
	}
	
	public static Q getLoopReachabilityViaDataFlowView(Q input, Q loopHeader) {
		if(CommonQueries.isEmpty(input)) {
			Log.info("Empty input");
			return Common.empty();
		}

//		Log.info("Input: "+input.eval().nodes().one().toString());
//		Q wholeTaintGraph = input.forwardOn(getTaints()).union(input.reverseOn(getTaints()));
//		Log.info("Size of taint graph: "+wholeTaintGraph.eval().nodes().size());
		Q loopControlFlow = Common.edges(XCSG.ControlFlow_Edge).between(loopHeader, loopHeader);
		Q inputToLoop = getTaints().between(input,loopControlFlow.contained()).retainEdges();
		Q loopToInput = getTaints().between(loopControlFlow.contained(),input).retainEdges();
		return inputToLoop.union(loopToInput);
	}
	
	public static Q getLoopReachabilityViaDataFlowRetainLoopView(Q input, Q loopHeader) {
		if(CommonQueries.isEmpty(input)) {
			Log.info("Empty input");
			return Common.empty();
		}

//		Log.info("Input: "+input.eval().nodes().one().toString());
		Q loopControlFlow = Common.edges(XCSG.ControlFlow_Edge).between(loopHeader, loopHeader);
		return loopControlFlow.union(LoopReachability.getTaints().between(input,loopControlFlow.contained()).retainEdges().intersection(loopControlFlow.contained()));
	}
	
	/**
	 * 
	 * @param entryPoints Methods
	 * @return
	 */
	public static Q getLoopReachabilityViaControlFlowView(Q entryPoints) {
		if(CommonQueries.isEmpty(entryPoints)) {
			Log.info("Empty input");
			return Common.empty();
		}
		
		LoopCallGraph lcg = new LoopCallGraph();
		Q lcgQ = lcg.lcgWithPredecessorCallGraph();
		lcgQ = Common.resolve(null, lcgQ);
		
		lcgQ = lcgQ.forward(entryPoints);
		return lcgQ;
	}
}
