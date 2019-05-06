package com.kcsl.loop.lcg;

import static com.ensoftcorp.atlas.core.script.Common.toGraph;
import static com.ensoftcorp.atlas.core.script.Common.toQ;
import static com.ensoftcorp.atlas.core.script.Common.universe;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.map.AtlasGraphKeyHashMap;
import com.ensoftcorp.atlas.core.db.set.AtlasNodeHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.db.set.EmptyAtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification;
import com.kcsl.loop.xcsg.LoopXCSG;

import net.ontopia.utils.CompactHashMap;

/**
 * 
 * @author jmathews
 * 
 * Creates a Graph similar to a LoopCallGraph, but at finer granularity (CallSites)
 * and with explicit loop headers.
 * 
 * Graph includes Methods, CallSites, Loops.
 * 
 * Creates Enhanced Loop Call Graph edges {@link #Edge} 
 * 1) from CallSites to possible targets
 * 2) From Methods to Loop and CallSites in the body.
 *    Edges represent nesting levels.  For example, a CallSite in a Loop
 *    will be reachable along a path: Method -> Loop -> CallSite.
 *
 */
public class EnhancedLoopCallGraph {
	
	private static class LoopContextGraph {
		
		private AtlasGraphKeyHashMap<Node, AtlasSet<Node>> lcgSuccessors = new AtlasGraphKeyHashMap<>();
		private AtlasGraphKeyHashMap<Node, AtlasSet<Node>> lcgPredecessors = new AtlasGraphKeyHashMap<>();
		
		public Set<Node> nodes() {
			return Collections.unmodifiableSet(lcgSuccessors.keySet());
		}
		
		public void addEdge(Node from, Node to) {
			if (from == null)
				throw new NullPointerException("from cannot be null");
			if (to == null)
				throw new NullPointerException("to cannot be null");
			
			AtlasSet<Node> successors = lcgSuccessors.get(from);
			if (successors == null) {
				successors = new AtlasNodeHashSet();
				lcgSuccessors.put(from, successors);
			}
			successors.add(to);
			
			AtlasSet<Node> predecessors = lcgPredecessors.get(to);
			if (predecessors == null) {
				predecessors = new AtlasNodeHashSet();
				lcgPredecessors.put(to, predecessors);
			}
			predecessors.add(from);
		}
		
		public AtlasSet<Node> successors(Node e) {
			AtlasSet<Node> successors = lcgSuccessors.get(e);
			if (successors == null) {
				return EmptyAtlasSet.<Node>instance(e.universe());
			}
			return successors;
		}
		
		@SuppressWarnings("unused")
		public AtlasSet<Node> predecessors(Node e) {
			AtlasSet<Node> predecessors = lcgPredecessors.get(e);
			if (predecessors == null) {
				return EmptyAtlasSet.<Node>instance(e.universe());
			}
			return predecessors;
		}
	}
	
	private static AtlasSet<Node> methodBody(Node method) {
		return universe().edges(XCSG.Contains).forward(toQ(toGraph(method))).eval().nodes();
	}
	
	private Node getContainingMethod(Node ge) {
		// NOTE: the enclosing method may be two steps or more above
		
		Node parent = parentCache.get(ge);
		if (parent != null)
			return parent;
		
		String enclosingTag = XCSG.Method;
		parent = getContainingNode(ge, enclosingTag);
		parentCache.put(ge, parent);
		return parent;
	}

	private static Node getContainingControlFlow(Node ge) {
		// NOTE: the enclosing method may be two steps or more above
		String enclosingTag = XCSG.ControlFlow_Node;
		return getContainingNode(ge, enclosingTag);
	}
	
	/**
	 * Find the next immediate containing node with the given tag.
	 * 
	 * @param node 
	 * @param containingTag
	 * @return the next immediate containing node, or null if none exists; never returns the given node
	 */
	private static Node getContainingNode(Node node,
			String containingTag) {
		if (node == null)
			return null;
		
		while (true) {
			Edge containsEdge = Graph.U.edges(node, NodeDirection.IN).taggedWithAll(XCSG.Contains).one();
			if (containsEdge == null)
				return null;
			
			Node parent = containsEdge.from();
			
			if (parent.taggedWith(containingTag))
				return parent;
			
			node = parent;
		}

	}
	
	private Q getTargetMethods(IProgressMonitor monitor, Graph dataFlowGraph, Node callsite) {
		if (callsite.taggedWith(XCSG.StaticDispatchCallSite)) {
			return toQ(dataFlowGraph).edges(XCSG.InvokedFunction)
				.successors(toQ(toGraph(callsite)));
			
		} else if (callsite.taggedWith(XCSG.DynamicDispatchCallSite)) {
			
			AtlasSet<Node> targetMethods = new AtlasNodeHashSet();
			AtlasSet<Node> targetIdentities = getIdentity(dataFlowGraph, callsite).eval().nodes();
			for (Node targetIdentity : targetIdentities) {
				Node targetMethod = getContainingMethod(targetIdentity);
				if (targetMethod != null)
					targetMethods.add(targetMethod);
				else
					Log.warning("LXG: Cannot find containing Method for Identity: " + targetIdentity);
				
				if (monitor.isCanceled())
					throw new OperationCanceledException();
			}
			return toQ(toGraph(targetMethods));
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Given a call site, return formal identity arguments for possible target methods.  
	 **/
	private static Q getIdentity(Graph dataFlowGraph, Node callsite) {
		return getIdentity(toQ(dataFlowGraph), toQ(callsite));
	}
	
	private static Q getIdentity(Q dataFlowGraph, Q callsites) { 
		Q actualArgument = dataFlowGraph.edges(XCSG.IdentityPassedTo).predecessors(callsites);
		return dataFlowGraph.edges(XCSG.InterproceduralDataFlow)
			.successors(actualArgument); 
	}
	


	/** Call graph, all nodes are XCSG.Method and all edges are XCSG.Call */
	private Graph callGraph = null;
	
	/** superset of Data flow graph */
	private Graph dataFlowGraph = null;

	private LoopContextGraph lcg;
	
	/** node -> parent Method */
	private CompactHashMap<Node, Node> parentCache;

	@LoopXCSG
	/**
	 * Enhanced Loop Call Graph edge.
	 * 1) from CallSites to possible targets
	 * 2) From Methods to Loop and CallSites in the body.
	 *    Edges represent nesting levels.  For example, a CallSite in a Loop
	 *    will be reachable along a path: Method -> Loop -> CallSite.
	 */
	public static final String Edge = "EnhancedLoopCallGraph_Edge";
	
	public static void run() {
		Graph callGraph = Graph.U;
		Graph dataFlowGraph = Graph.U;
		run(callGraph, dataFlowGraph);
	}
	
	public static void run(final Graph callGraph, final Graph dataFlowGraph) {
		Job j = new Job("Enhanced Loop Call Graph") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				EnhancedLoopCallGraph lcg = new EnhancedLoopCallGraph();
				lcg.run(monitor, callGraph, dataFlowGraph);
				return Status.OK_STATUS;
			}	
		};
		j.schedule();
		try {
			j.join();
		} catch (InterruptedException e) {
			OperationCanceledException e2 = new OperationCanceledException();
			throw e2;
		}
	}
	
	public void run(IProgressMonitor monitor) {
		Graph callGraph = Graph.U;
		Graph dataFlowGraph = Graph.U;
		run(monitor, callGraph, dataFlowGraph);
	}
	
	private void run(IProgressMonitor monitor, Graph callGraph, Graph dataFlowGraph) {
		
		// enforce: callGraph : nodes <- Method, edges <- Call
		Q qCallGraph = toQ(callGraph);
		qCallGraph = qCallGraph.nodesTaggedWithAll(XCSG.Method).induce(qCallGraph.edges(XCSG.Call));
		this.callGraph = Common.resolve(monitor, qCallGraph).eval();
		
		// Note: nodes currently need not be bounded (i.e. any ModelElement is acceptable)
		// enforce: dataFlowGraph : nodes <- ModelElement | DataFlow_Node | Variable , edges <- DataFlow_Edge | InvokedSignature | InvokedFunction | IdentityPassedTo
		this.dataFlowGraph = Common.resolve(monitor, 
				toQ(dataFlowGraph).edges(XCSG.DataFlow_Edge, XCSG.InvokedSignature, XCSG.InvokedFunction, XCSG.IdentityPassedTo)).eval();
		
		createLoopCallGraph(monitor);
	}

	private void createLoopCallGraph(IProgressMonitor monitor) {
		
		lcg = new LoopContextGraph();
		
		parentCache = new CompactHashMap<>();
		
		// FIXME: the XCSG.Call edges are currently ignored, in favor of data flow;
		// unclear how to bound the input to specific methods and call
		// relations.
		// Related to performance issues: need to know the upper bound of methods at the
		// outset to facilitate tracing backwards along the invocations ( back
		// from Method on InvokedFunction, or from Identity ).
		// If Call edges are used as an upper bound, they would have to limit
		// that traversal.
		// Is data flow meant to be a bound as well?  If so, works out naturally by omitting invokedFunction
		// and DataFlow_Edge to Identity.
		AtlasSet<Node> methods = callGraph.nodes().taggedWithAny(XCSG.Method);
		monitor.beginTask("Processing methods", (int)methods.size());
		
		try {
		
			for (Node method : methods) {
				AtlasSet<Node> methodBody = methodBody(method);
				processMethod(monitor, method, methodBody);
				
				monitor.worked(1);
				if (monitor.isCanceled())
					throw new OperationCanceledException();
			}
			
			apply();
		
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Translates the internal version of the LCG to the Atlas Graph
	 */
	private void apply() {
		Set<Node> keySet = lcg.nodes();
		for (Node from : keySet) {
			for (Node successor : lcg.successors(from)) {
				Edge edge = Graph.U.createEdge(from, successor);
				edge.tag(EnhancedLoopCallGraph.Edge);
			}
		}
	}

	/**
	 * post: LOOP_HEADER has attr LoopCallGraph.NESTING_DEPTH = depth
	 * 
	 * @param method
	 * @param methodBody 
	 */
	private void processMethod(IProgressMonitor monitor, Node method, AtlasSet<Node> methodBody) {
		
		AtlasSet<Node> visited = new AtlasNodeHashSet();
		
		for (Node member : methodBody) {
			_processMethodBody(monitor, method, member, visited);
			
			if (monitor.isCanceled())
				throw new OperationCanceledException();
		}
	}
	
	private void _processMethodBody(IProgressMonitor monitor, Node method, Node member,
			AtlasSet<Node> visited) {

		if (visited.contains(member)) {
			return;
		}
		visited.add(member);
		
		Object mid;
		
		if (member.taggedWith(XCSG.CallSite)) {
			// CallSite -> [target methods]
			
			AtlasSet<Node> targetMethods = getTargetMethods(monitor, dataFlowGraph, member).eval().nodes(); 
			for (Node targetMethod : targetMethods) {
				lcg.addEdge(member, targetMethod);
			}
			
			// CallSite is DataFlow_Node, loop member id is on the containing control flow node
			Node containingControlFlow = getContainingControlFlow(member);
			mid = containingControlFlow.getAttr(DecompiledLoopIdentification.CFGNode.LOOP_MEMBER_ID);
			
		} else if (member.taggedWith(XCSG.Loop)) {

			mid = member.getAttr(DecompiledLoopIdentification.CFGNode.LOOP_MEMBER_ID);
			
		} else {
			return;
		}
		
		// assert: member is CallSite or LoopHeader
		
		Node parent = null;
		
		if (mid == null) {
		 	// top-level member
			
			// Method -> member
			parent = method;
			
		} else {
			// is a member, have to recurse up to containing loop to get depth
			
			AtlasSet<Node> loopHeaders = universe().selectNode(DecompiledLoopIdentification.CFGNode.LOOP_HEADER_ID, mid).eval().nodes();
			
			// sanity check
			if (loopHeaders.size() != 1) {
				Log.error("Loop header missing, id: " + mid, null);
				return;
			}
			
			// LoopHeader -> member
			parent = loopHeaders.one();
		}
		
		lcg.addEdge(parent, member);

	}

	
}
