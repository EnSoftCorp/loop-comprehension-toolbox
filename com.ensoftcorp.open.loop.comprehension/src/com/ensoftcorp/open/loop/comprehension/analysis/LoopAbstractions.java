package com.ensoftcorp.open.loop.comprehension.analysis;

import java.awt.Color;

import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CFG;
import com.ensoftcorp.open.commons.analysis.StandardQueries;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.taint.TaintOverlay;
import com.ensoftcorp.open.loop.comprehension.utils.TerminatingConditions;
// import org.rulersoftware.taint.TaintOverlay;
import com.ensoftcorp.open.pcg.factory.PCGFactory;

public class LoopAbstractions {

	public static Q getFullTerminationDependenceGraph(Q loopHeader) {
		// this will get you the entire reverse taint from termination conditions of given loop header.
		if(!loopHeader.eval().nodes().one().taggedWith(CFGNode.LOOP_HEADER)) {
			throw new RuntimeException("Not a loop header");
		}
		
		// terminating Conditions
		Q tc = TerminatingConditions.findTerminatingConditionsForLoopHeader(loopHeader);
		
		// all taints
		Q taints = Common.universe().edgesTaggedWithAny(TaintOverlay.Taint);

		// local taints influencing loop termination (taint events)
		Q conditions = tc.children().nodesTaggedWithAny(XCSG.DataFlowCondition);
		Q loopTerminationTaints = conditions.reverseOn(taints);
				
		// loop termination taints
		Q loopTerminationTaintGraph = loopTerminationTaints.induce(taints);
		
		// callsites in Taint graph
		Q callsites = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.DynamicDispatchCallSite);
		
		// forwarding taint
		Q missing = callsites.forwardOn(Common.edges(XCSG.LocalDataFlow));
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		// pulling in missed induction variable assignments
		missing = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.IdentityPass).parent().children().nodesTaggedWithAll(XCSG.Assignment);
		missing = Common.edges(TaintOverlay.Taint).between(loopTerminationTaintGraph, missing);
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		//DisplayUtil.displayGraph(loopTerminationTaintGraph.eval());
		
		return loopTerminationTaintGraph;
						
	}
	
	public static Q getLocalTerminationDependenceGraph(Q loopHeader) {
		// this gives you a taint graph restricted at intra-procedural level
		// but short circuit edges corresponding to constructors are retained
		if(!loopHeader.eval().nodes().one().taggedWith(CFGNode.LOOP_HEADER)) {
			throw new RuntimeException("Not a loop header");
		}
		
		// terminating Conditions
		Q tc = TerminatingConditions.findTerminatingConditionsForLoopHeader(loopHeader);
		
		// all taints
		Q taints = Common.universe().edgesTaggedWithAny(TaintOverlay.Taint);

		// method containing loop header
		Q method = StandardQueries.getContainingFunctions(loopHeader);

		// method contents
		Q methodContents = StandardQueries.localDeclarations(method);

		// local taints influencing loop termination (taint events)
		Q conditions = tc.children().nodesTaggedWithAny(XCSG.DataFlowCondition);
		Q loopTerminationTaints = conditions.reverseOn(taints).intersection(methodContents);
				
		// loop termination taints
		Q loopTerminationTaintGraph = loopTerminationTaints.induce(taints);
		
		// callsites in Taint graph
		Q callsites = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.DynamicDispatchCallSite);
		
		// forwarding taint
		Q missing = callsites.forwardOn(Common.edges(XCSG.LocalDataFlow));
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		// pulling in missed induction variable assignments
		missing = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.IdentityPass).parent().children().nodesTaggedWithAll(XCSG.Assignment);
		missing = Common.edges(TaintOverlay.Taint).between(loopTerminationTaintGraph, missing);
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		//DisplayUtil.displayGraph(loopTerminationTaintGraph.eval());
		
		return loopTerminationTaintGraph;
						
	}
	
	public static Q taintGraphWithoutCycles(Q loopHeader) {
		if(!loopHeader.eval().nodes().one().taggedWith(CFGNode.LOOP_HEADER)) {
			throw new RuntimeException("Not a loop header");
		}
		
		// terminating Conditions
		Q tc = TerminatingConditions.findTerminatingConditionsForLoopHeader(loopHeader);
		
		// all taints without the cycle causing edges
		Q taints = Common.universe().edgesTaggedWithAny(TaintOverlay.Taint).differenceEdges(Common.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT));;

		// method containing loop header
		Q method = StandardQueries.getContainingFunctions(loopHeader);

		// method contents
		Q methodContents = StandardQueries.localDeclarations(method);

		// local taints influencing loop termination (taint events)
		Q conditions = tc.children().nodesTaggedWithAny(XCSG.DataFlowCondition);
		Q loopTerminationTaints = conditions.reverseOn(taints).intersection(methodContents);
				
		// loop termination taints
		Q loopTerminationTaintGraph = loopTerminationTaints.induce(taints);
		
//		// callsites in Taint graph
//		Q callsites = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.DynamicDispatchCallSite);
//		
//		// forwarding taint
//		Q missing = callsites.forwardOn(Common.edges(XCSG.LocalDataFlow));
		
		Q assignments = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.Assignment);
		
		Q missing = assignments.forwardOn(taints.differenceEdges(Common.edges(XCSG.InterproceduralDataFlow)));
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
//		// pulling in missed induction variable assignments
//		missing = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.IdentityPass).parent().children().nodesTaggedWithAll(XCSG.Assignment);
//		
//		missing = taints.between(loopTerminationTaintGraph, missing);
//		
//		// updating Taint graph
//		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);

		//DisplayUtil.displayGraph(loopTerminationTaintGraph.eval());
		
		return loopTerminationTaintGraph;
	}

	public static Q getTerminationPCG(Q loopHeader) {
		// taint graph
		Q taintGraph = taintGraphWithoutCycles(loopHeader);
		// getting rid of cycles
		Q leaves = taintGraph.leaves();
		// conditions
		Q conditions = leaves.nodesTaggedWithAny(XCSG.DataFlowCondition).parent();
		Q cycleEdges = Common.edges(TaintOverlay.Taint).differenceEdges(Common.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT));
		taintGraph = leaves.reverseOn(cycleEdges);
		// taint roots
		Q taintRoots = taintGraph.roots().parent();
		// operators
		Q operators = taintGraph.nodesTaggedWithAny("Overlay.Taint.Operator").parent();
		// method containing loop header
		Q method = StandardQueries.getContainingFunctions(loopHeader);
		// method Cfg
		Q methodCfg = CFG.cfg(method);
		// loop Cfg
		Q loopCfg = LoopAbstractions.loopControlFlowGraph(loopHeader);
		Q callsites = loopCfg.children().nodesTaggedWithAny(XCSG.CallSite).parent();
		//AtlasSet<GraphElement> loopCfgNodes = loopCfg.eval().nodes();
		// events for Efg
		Q events = taintRoots.union(loopHeader).union(conditions).union(operators).union(callsites);
		Highlighter h = new Highlighter(Highlighter.ConflictStrategy.LAST_MATCH);
		h.highlightNodes(callsites, Color.GREEN);
		h.highlightNodes(conditions, Color.RED);
		h.highlightNodes(loopHeader, Color.YELLOW);
		// Efg
		Q taintEfg = PCGFactory.PCG(methodCfg, events);
		return taintEfg;
	}

	public static Q loopControlFlowGraph(Q loopHeader) {
		return Common.edges(XCSG.ControlFlow_Edge).between(loopHeader, loopHeader);		
	}

}
