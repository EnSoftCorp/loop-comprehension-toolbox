package com.kcsl.loop.lcg;

import static com.ensoftcorp.atlas.core.script.Common.resolve;
import static com.ensoftcorp.atlas.core.script.Common.toQ;
import static com.ensoftcorp.atlas.core.script.Common.universe;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;
import com.ensoftcorp.open.commons.xcsg.Toolbox;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.kcsl.loop.lcg.log.Log;

public class LoopCallGraph {

	/**
	 * An integer attribute added to a CFG Node to indicate the intra-procedural loop nesting depth
	 */
	public final static String NESTING_DEPTH = "NESTING_DEPTH";

	public final static String LCG_DEPTH = "LCG_DEPTH";
	
	public final static String INTERPROCEDURAL_DEPTH = "INTERPROCEDURAL_DEPTH";

	/**
	 * A call graph, consisting of XCSG.Method nodes and XCSG.Call edges
	 */
	private Q callContext;

	/**
	 * The call graph from call sites to methods
	 */
	private Q callsitesCallEdges() { return universe().edges(XCSG.InvokedFunction, XCSG.InvokedSignature); };

	public LoopCallGraph() {
		this.callContext = resolve(null, universe().edges(XCSG.Call));
	}

	/**
	 * @param callContext A call graph, consisting of XCSG.Method nodes and XCSG.Call edges
	 */
	public LoopCallGraph(Q callContext) {
		this.callContext = resolve(null, callContext);
	}

	/**
	 * Constructs the loop call graph (lcg) of a given system, An lcg is a subset of the system's call graph
	 * where the roots are the root methods containing loops and the leaves are the leaf methods containing loops 
	 * @see #lcgWithPredecessorCallGraph()
	 * @return lcg
	 */
	public Q lcg(){
		Q loopingMethods = getMethodsContainingLoops();
		Q callEdges = getCallGraph();
		Q callGraph = callEdges.between(loopingMethods, loopingMethods);
		return callGraph;
	}

	/**
	 * Constructs the Loop Call Graph (LCG) along with the call graph which reaches it.
	 * If predecessor graph is needed, this method is cheaper than modifying the result of {@link #lcg()}.
	 * @see #lcg()
	 * 
	 * @return
	 */
	public Q lcgWithPredecessorCallGraph() {
		Q loopingMethods = getMethodsContainingLoops();
		Q callEdges = getCallGraph();
		Q lcgQ = callEdges.reverse(loopingMethods);
		return lcgQ;
	}

	/**
	 * Returns a Highlighter coloring the methods containing loops and edges corresponding to call from within loops
	 * 
	 * Methods == bright BLUE
	 * Call Edges which may have been from within a loop == ORANGE
	 * 
	 * @return
	 */
	public IMarkup colorMethodsCalledFromWithinLoops(){
		AtlasSet<Edge> callEdgesFromWithinLoops = new AtlasHashSet<Edge>();
		// Get all the call sites
		Q allCallsites = universe().nodesTaggedWithAll(XCSG.CallSite);

		// Retrieve the CFG nodes that are containing the call sites and are part of a loop
		Q loopingCFGNodes = getContainingLoopingCFGNodes(allCallsites);

		//Iterate through the looping CFG nodes and compute the call graph for the methods called from within loops
		for(Node cfgNode : loopingCFGNodes.eval().nodes()){
			// Get the methods containing the current cfgNode
			Q caller = CommonQueries.getContainingFunctions(toQ(cfgNode));

			// Get the call site contained within the cfgNode
			AtlasSet<Node> loopingCallsites = toQ(cfgNode).children().nodesTaggedWithAll(XCSG.CallSite).eval().nodes();
			if (loopingCallsites.size() != 1){
				Log.warning("Internal error, expected exactly one callsite");
			}
			Node loopingCallsite = loopingCallsites.one();

			// Get the possible target methods from this call site
			Q targetMethods = CallSiteAnalysis.getTargets(Common.toQ(loopingCallsite));

			// The resulting call graph should be between the called and the invoked methods
			Q subResult = getCallGraph().betweenStep(caller, targetMethods);

			callEdgesFromWithinLoops.addAll(subResult.eval().edges());
		}

		//Highlighter h = new Highlighter(ConflictStrategy.LAST_MATCH);
		Markup m = new Markup();
		// Highlight methods containing loops with BLUE
		Q methodsContainingLoops = getMethodsContainingLoops();
		//h.highlight(methodsContainingLoops, Color.BLUE.brighter().brighter());
		m.setNode(methodsContainingLoops, MarkupProperty.NODE_BACKGROUND_COLOR, Color.BLUE.brighter().brighter());

		// Retrieve the call edges from the resultant graph and color them ORANGE
		//h.highlightEdges(toQ(callEdgesFromWithinLoops), Color.ORANGE);
		m.setEdge(toQ(callEdgesFromWithinLoops), MarkupProperty.EDGE_COLOR, Color.ORANGE);
		return m;
	}

	/**
	 * Calculate the depth of intra- and inter-procedural loop nesting 
	 */
	public void calculateLoopNestingHeight(){
		// Before start traversing the call graph, tag the call sites called from within loop with its intra-procedural loop nesting depth
		Q loopingCFGNodes = TagCallSitesIntraprocedualNestingLoopingDepth();
		Q results = Common.empty();

		///Iterate through the looping CFG nodes and compute the call graph for the methods called from within loops 
		for(Node cfgNode : loopingCFGNodes.eval().nodes()){
			// Get the methods containing the current cfgNode
			Q caller = CommonQueries.getContainingFunctions(toQ(cfgNode));

			// Get the call sites containing within the cfgNode
			Q loopingCallsites = toQ(cfgNode).children().nodesTaggedWithAll(XCSG.CallSite);

			// Get the invoked methods from these call sites
			Q invokedMethods = callsitesCallEdges().successors(loopingCallsites).nodesTaggedWithAll(XCSG.Method);

			// The resulting call graph should be between the called and the invoked methods
			Q subResult = getCallGraph().betweenStep(caller, invokedMethods);
			//results = results.union((caller.union(invokedMethods)).induce(getCallGraph()));
			results = results.union(subResult);
		}

		// Retrieve the call edges from the resultant graph and color them with RED
		AtlasSet<Edge> callEdgesFromWithinLoops = results.eval().edges();

		Q lcg = lcg();
		AtlasSet<Node> methodsContainingLoops = getMethodsContainingLoops().eval().nodes();

		// Iterate through the methods containing loops and compute its loop nesting depth
		for(Node method : methodsContainingLoops){
			// The LCG for the current method
			Q graph = lcg.reverse(toQ(method));
			AtlasSet<Node> roots = graph.roots().eval().nodes();
			// DisplayUtil.displayGraph(graph);

			int depth = 0;
			for(Node root: roots) {
				// Recursively traverse the LCG for the method and compute its loop nesting depth
				int temp = traverse(graph.eval(), root, 0, new AtlasHashSet<Node>(), callEdgesFromWithinLoops);
				if(depth < temp) {
					depth = temp;
				}
			}
			method.putAttr(LCG_DEPTH,Integer.toString(depth));
			Q loopsWithinMethod = Common.toQ(method).contained().nodes(XCSG.Loop);
			for(Node loopWithinMethod: loopsWithinMethod.eval().nodes()) {
				int intraDepth = Integer.parseInt(loopWithinMethod.getAttr(Toolbox.loopDepth).toString());
				int d = intraDepth + depth - 1;
				loopWithinMethod.putAttr(INTERPROCEDURAL_DEPTH, Integer.toString(d));
			}
		}

		/*AtlasSet<Node> loopHeaders = Query.universe().nodes(XCSG.Loop).eval().nodes();
		for(Node loopHeader: loopHeaders) {
			Q loopHeaderQ = Common.toQ(loopHeader);
			Node loopMethod = CommonQueries.getContainingFunctions(loopHeaderQ).eval().nodes().one();
			// Graph graph = lcg.reverse(Common.toQ(loopMethod)).eval();
			int intraDepth = Integer.parseInt(loopHeader.getAttr(NESTING_DEPTH).toString());
			int methodDepth = Integer.parseInt(loopMethod.getAttr(LCG_DEPTH).toString());
			int d = intraDepth + methodDepth;
			loopHeader.putAttr(NESTING_DEPTH, Integer.toString(d));
		}*/


	}

	/**
	 * Performs DFS traversal on the given call graph from the current node to calculate the depth of loop nesting
	 * @param graph: The graph to traverse
	 * @param node: The current node traversed
	 * @param depth: The current depth
	 * @param visited: The set of node visited along the traversal
	 * @param callEdgesFromWithinLoops: The edges corresponding to call from within a loop
	 * @return the current depth at the node visited
	 */
	private int traverse(Graph graph, Node node, int depth, AtlasSet<Node> visited, AtlasSet<Edge> callEdgesFromWithinLoops){
		// If the node has been visited before, return the current depth
		if(visited.contains(node)){
			return depth;
		}
		visited.add(node);
		AtlasSet<Edge> outEdges = graph.edges(node, NodeDirection.OUT);
		ArrayList<Integer> depths = new ArrayList<Integer>();
		// Recursively iterate through the children of the current node
		for(Edge edge : outEdges){
			Node child = edge.to();
			int depth_new = depth;

			// If the child is being called from within a loop then add the nesting depth to the current depth
			if(callEdgesFromWithinLoops.contains(edge)){
				// Get the call sites for the child in the caller (node)
				Q callsites = getCallSitesForMethodInCaller(toQ(child), toQ(node));

				// Retrieves the set of CFG nodes containing the call sites and are part of loop
				Q loopingCFGNodes = getContainingCFGNodes(callsites).selectNode(NESTING_DEPTH);
				ArrayList<Integer> iDepths = new ArrayList<Integer>();
				if(loopingCFGNodes.eval().nodes().isEmpty()){
					DisplayUtils.show(edge, "no");
				}

				// Iterate through the looping CFG nodes and compute the maximum nesting depth
				for(Node cfgCallsite : loopingCFGNodes.eval().nodes()){
					int intraprocedural_depth = Integer.parseInt((String)cfgCallsite.getAttr(NESTING_DEPTH));
					iDepths.add(intraprocedural_depth);
				}
				depth_new += Collections.max(iDepths);
				depth_new++; // From inter-procedural nesting
			}
			int d = traverse(graph, child, depth_new, new AtlasHashSet<Node>(visited), callEdgesFromWithinLoops);
			depths.add(d);
		}
		// Calculate the maximum depth and return it
		if(!depths.isEmpty()){
			depth = Collections.max(depths);
		}
		return depth;
	}

	/**
	 * Returns the set of call sites for the callee in the caller
	 * @param callee
	 * @param caller
	 * @return
	 */
	private Q getCallSitesForMethodInCaller(Q callee, Q caller){
		Q callsites = getCallSitesForMethods(callee);
		Q containedCallsites = caller.contained().nodesTaggedWithAll(XCSG.CallSite).intersection(callsites);
		return containedCallsites;
	}

	/**
	 * Returns the set of call sites for the passed method(s)
	 * @param methods
	 * @return
	 */
	private Q getCallSitesForMethods(Q methods){
		Q callsites = callsitesCallEdges().predecessors(methods).nodesTaggedWithAll(XCSG.CallSite);
		return callsites;
	}

	/**
	 * Returns the set of CFG nodes tagged with LOOP_HEADER that are containing within the passed method
	 * @param method
	 * @return
	 */
	private Q getLoopHeadersForMethod(Q method){
		Q loopHeaders = method.contained().nodesTaggedWithAll(XCSG.ControlFlow_Node, XCSG.Loop);
		return loopHeaders;
	}

	/**
	 * Returns the set of control flow nodes that are part of a loop and containing the passed nodes
	 * @param nodes
	 * @return
	 */
	public Q getContainingLoopingCFGNodes(Q nodes){
		Q containingCFGNodes = getContainingCFGNodes(nodes);
		Q loopingCFGNodes = containingCFGNodes.selectNode(CFGNode.LOOP_MEMBER_ID);
		loopingCFGNodes = loopingCFGNodes.union(containingCFGNodes.nodesTaggedWithAll(XCSG.Loop));
		return loopingCFGNodes;
	}

	/**
	 * Returns the set of control flow nodes that are containing the passed nodes
	 * @param callsites
	 * @return
	 */
	private Q getContainingCFGNodes(Q callsites) {
		Q CFGNodes = callsites.parent().nodesTaggedWithAll(XCSG.ControlFlow_Node);
		return CFGNodes;
	}

	/**
	 * Adds the attribute NESTING_DEPTH to all call sites that are part of a loop
	 * @returns the set of CFG node containing the calls from within loops
	 */
	private Q TagCallSitesIntraprocedualNestingLoopingDepth(){
		// Get all call sites
		Q allCallsites = universe().nodesTaggedWithAll(XCSG.CallSite);

		// Get only the containing CFG nodes that are part of a loop 
		Q loopingCFGNodes = getContainingLoopingCFGNodes(allCallsites);
		AtlasSet<Node> nodes = loopingCFGNodes.eval().nodes();

		// Iterate through the looping CFG nodes and add the NESTING_DEPTH attribute
		for(Node node : nodes){
			Q loopHeaders = getLoopHeadersForMethod(CommonQueries.getContainingFunctions(toQ(node)));
			int depth = calculateIntraproceduralLoopNestingDepth(node, 0, loopHeaders);
			node.putAttr(NESTING_DEPTH, Integer.toString(depth));
		}
		return loopingCFGNodes;
	}

	/**
	 * Returns the set of loop header for the loops that are nested within other loops
	 * @return the set of nested loop headers
	 */
	public Q getNestedLoopHeaders(){
		Q loopHeaders = universe().nodesTaggedWithAll(XCSG.Loop);
		Q nestedLoopHeaders = Common.empty();
		for(Node header : loopHeaders.eval().nodes()){
			Q method = CommonQueries.getContainingFunctions(toQ(header));
			Q headersInMethods = getLoopHeadersForMethod(method);
			int depth = calculateIntraproceduralLoopNestingDepth(header, 1, headersInMethods);
			if(depth > 0){
				// A (depth > 0) means that the loop is nested
				nestedLoopHeaders = nestedLoopHeaders.union(toQ(header));
			}
		}
		return nestedLoopHeaders;
	}

	/**
	 * For a given CFG Node, recursively computes the number of loops this node is nested under
	 * @param ge the CFG node within a loop
	 * @param depth the current depth
	 * @param loopHeaders the set of loop headers in the containing method
	 * @return the nesting depth
	 */
	public int calculateIntraproceduralLoopNestingDepth(Node cfgNode, int depth, Q loopHeaders){
		if(cfgNode.hasAttr(CFGNode.LOOP_MEMBER_ID)){
			Object loopHeaderId = cfgNode.getAttr(CFGNode.LOOP_MEMBER_ID);
			Node loopHeader = loopHeaders.selectNode(CFGNode.LOOP_HEADER_ID, loopHeaderId).eval().nodes().one();
			depth = cfgNode.taggedWith(XCSG.Loop) ? ++depth : depth;
			return calculateIntraproceduralLoopNestingDepth(loopHeader, depth, loopHeaders);
		}
		return depth;
	}

	/**
	 * Retrieves the XCSG.Call graph
	 * @return the call graph
	 */
	public Q getCallGraph(){
		return callContext;
	}

	/**
	 * Returns the set of methods that are containing loops that depend on the DLI algorithm
	 * @return The set of methods containing loops
	 */
	public static Q getMethodsContainingLoops(){
		//		IndexingPhases.confirmLoopAnalyzer();
		Q u = universe();
		Q headers = u.nodes(XCSG.Loop);
		Q loopingMethods = CommonQueries.getContainingFunctions(headers);
		return loopingMethods;
	}

	public int callsitesinlcg(){
		Q lcg = lcg();
		Q methods = lcg.nodesTaggedWithAll(XCSG.Method);
		int count = 0;
		for(Node method : methods.eval().nodes()){
			AtlasSet<Node> callsites = toQ(method).contained().nodesTaggedWithAll(XCSG.CallSite).eval().nodes();
			for(Node callsite : callsites){
				Q invokedMethod = callsitesCallEdges().successors(toQ(callsite));
				if(!methods.intersection(invokedMethod).eval().nodes().isEmpty()){
					count++;
				}
			}
		}
		return count;
	}

	public void check(){
		Q lcg = lcg();
		AtlasSet<Edge> edges = lcg.edgesTaggedWithAll(XCSG.Call).eval().edges();
		for(Edge edge : edges){
			Node from = edge.from();
			Node to = edge.to();
			Q callsites = getCallSitesForMethodInCaller(toQ(to), toQ(from));
			if(callsites.eval().nodes().isEmpty()){
				DisplayUtils.show(from, "1");
				DisplayUtils.show(to, "2");
			}
		}
	}

	public Q getCallEdgesFromWithinLoops(){

		Q results = Common.empty();
		// Get all the call sites
		Q allCallsites = universe().nodesTaggedWithAll(XCSG.CallSite);

		// Retrieve the CFG nodes that are containing the call sites and are part of a loop
		Q loopingCFGNodes = getContainingLoopingCFGNodes(allCallsites);

		//Iterate through the looping CFG nodes and compute the call graph for the methods called from within loops
		for(Node cfgNode : loopingCFGNodes.eval().nodes()){
			// Get the methods containing the current cfgNode
			Q caller = CommonQueries.getContainingFunctions(toQ(cfgNode));

			// Get the call sites containing within the cfgNode
			Q loopingCallsites = toQ(cfgNode).children().nodesTaggedWithAll(XCSG.CallSite);

			// Get the invoked methods from these call sites
			Q invokedMethods = callsitesCallEdges().successors(loopingCallsites).nodesTaggedWithAll(XCSG.Method);

			// The resulting call graph should be between the called and the invoked methods
			Q subResult = getCallGraph().betweenStep(caller, invokedMethods);

			results = results.union(subResult);
		}
		// Retrieve the call edges from the resultant graph and color them with RED
		return results.edgesTaggedWithAll(XCSG.Call);
	}
}
