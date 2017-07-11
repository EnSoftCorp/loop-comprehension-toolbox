package com.ensoftcorp.open.loop.comprehension.taint;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.log.Log;

public class LoopReachability {
	
	public static Q getTaints() {
		return Common.universe().edgesTaggedWithAny(TaintOverlay.Taint,"INFERRED_DATA_FLOW",XCSG.DataFlow_Edge,XCSG.PassedTo,XCSG.ArrayIdentityFor);
	}
	
	public static Q getLoopChildEdges() {
		return Common.universe().edgesTaggedWithAny(XCSG.LoopChild);
	}

	public static Q getLoopReachabilityViaDataFlowView(Q input, boolean forward) {
		if(CommonQueries.isEmpty(input)) {
			Log.info("Empty input");
			return Common.empty();
		}

		Log.info("Input: "+input.eval().nodes().one().toString());
		Q wholeTaintGraph = Common.empty();
		if(forward) {
			wholeTaintGraph = input.forwardOn(getTaints());			
		} else {
			wholeTaintGraph = input.reverseOn(getTaints());
		}
		
		Log.info("Size of taint graph: "+wholeTaintGraph.eval().nodes().size());
		Q reachableLoopHeaders = wholeTaintGraph.containers().nodesTaggedWithAll(XCSG.ControlFlow_Node,CFGNode.LOOP_HEADER);
		Log.info("Size of loop headers reached: "+reachableLoopHeaders.eval().nodes().size());
		Q reachableControlFlow = wholeTaintGraph.containers().nodesTaggedWithAll(XCSG.ControlFlow_Node);
		for(Node m: reachableControlFlow.eval().nodes()) {
			if(m.hasAttr("LOOP_MEMBER_ID")) {
				Log.info("Control flow containing relevant data flow inside of loop: "+m.toString());
				Log.info("Loop with control flow containing relevant data flow: "+m.getAttr("LOOP_MEMBER_ID"));
				Q containingLoopHeader = Common.universe().selectNode("LOOP_HEADER_ID", m.getAttr("LOOP_MEMBER_ID"));
				reachableLoopHeaders = reachableLoopHeaders.union(containingLoopHeader);
			} else {
				Log.info("Control flow containing relevant data flow outside of loop: "+m.toString());
			}
		}
		Log.info("Size of loops reached: "+reachableLoopHeaders.eval().nodes().size());
		return reachableLoopHeaders.induce(getLoopChildEdges());
	}
	
	public static Q getLoopReachabilityViaDataFlowView(Q input, Q loopHeader) {
		if(CommonQueries.isEmpty(input)) {
			Log.info("Empty input");
			return Common.empty();
		}

		Log.info("Input: "+input.eval().nodes().one().toString());
		Q wholeTaintGraph = input.forwardOn(getTaints()).union(input.reverseOn(getTaints()));
		Log.info("Size of taint graph: "+wholeTaintGraph.eval().nodes().size());
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

		Log.info("Input: "+input.eval().nodes().one().toString());
		Q loopControlFlow = Common.edges(XCSG.ControlFlow_Edge).between(loopHeader, loopHeader);
		return loopControlFlow.union(LoopReachability.getTaints().between(input,loopControlFlow.contained()).retainEdges().intersection(loopControlFlow.contained()));
	}
}
