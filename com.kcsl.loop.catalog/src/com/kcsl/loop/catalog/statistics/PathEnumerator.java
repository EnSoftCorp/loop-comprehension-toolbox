package com.kcsl.loop.catalog.statistics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DirectedPseudograph;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.kcsl.loop.util.LimitedDirectedPaths;

public class PathEnumerator {

//	private static List<List<Node>> paths = new ArrayList<List<Node>>();
//	private static AtlasSet<Node> tempReentryNodes = new AtlasHashSet<Node>();

/*	public static void main(String[] args) {
		Q m = Common.methods("sendBody");
		Log.info(m.eval().nodes().one().getAttr(XCSG.name).toString());
		pathsInMethod(m);
	}*/

/*	public static void undoChanges() {
		for (Node g : tempReentryNodes) {
			Graph.U.nodes().remove(g);
		}
		tempReentryNodes = new AtlasHashSet<Node>();
	}

	public static List<List<Node>> getPaths() {
		return paths;
	}

	public static void setPaths(List<List<Node>> paths) {
		PathEnumerator.paths = paths;
	}*/

/*	//TODO
	public static void pathsInEFG(Q efg) {
		Graph efgGraph = efg.eval();
		Node entryNode = Common.toQ(efgGraph).nodes(EventFlowGraph.EFGNode.EventFlow_Master_Entry).eval().nodes().one();
		traverse(efgGraph, entryNode, new ArrayList<Node>());
	}
*/
/*	*//**
	 * Computes the set of all paths in the cfg of the method, starting from the entry point of the method
	 * 
	 * @param aSelectedMethod
	 * @return
	 *//*
	public static List<List<Node>> pathsInMethod(Q aSelectedMethod) {
		System.out.println(aSelectedMethod.eval().nodes().one().getAttr(XCSG.name));
		paths = new ArrayList<List<Node>>();
		Q methodCFG = CommonQueries.cfg(aSelectedMethod);
		
		// walk all paths in the method from its control flow entry point  
		return pathsInCFG(methodCFG);
	}

	*//**
	 * Computes the set of all paths in the cfg starting from the designated control flow root, or otherwise, one of the roots of the cfg
	 * 
	 * @param cfg
	 * @return
	 *//*
	public static List<List<Node>> pathsInCFG(Q cfg) {
		paths = new ArrayList<List<Node>>();
		if(CommonQueries.isEmpty(cfg)) {
			return paths;
		}
		// assume a unique entry node for a method's CFG 
		Node entryNode = cfg.nodes(XCSG.controlFlowRoot).eval().nodes().one();

		if(entryNode == null) {
			if(CommonQueries.isEmpty(cfg.roots())) {
				throw new RuntimeException("No control flow root available");
			} else {
				entryNode = cfg.roots().eval().nodes().one();
				if(cfg.roots().eval().nodes().size() > 1) {
					Log.warning("Walking paths from first control flow root among multiple");
				}
			}
		}
		// insert new intermediate loop re-entry nodes on the loop back edges
		Q updatedCFG = processLoopBackEdgesForCFG(cfg);
		traverse(updatedCFG.eval(), entryNode, new ArrayList<Node>());
		// clean up -- remove nodes and edges added to enumerate paths
		Log.info("Enumerated " + paths.size() + " paths");
		undoChanges();
		return paths;
	}
	
	*//**
	 * Computes the set of all paths in the cfg starting from entry
	 * @param cfg
	 * @param entry
	 * @return
	 *//*
	private static Q currentLoopHeader = null;
	public static List<List<Node>> pathsInCFG(Q cfg, Q entry) {
		currentLoopHeader = entry;
		paths = new ArrayList<List<Node>>();
		if(CommonQueries.isEmpty(cfg) || CommonQueries.isEmpty(entry) ) {
			return paths;
		}
		// assume a unique entry node for a method's CFG 
		Node entryNode = entry.eval().nodes().one();

		// insert new intermediate loop re-entry nodes on the loop back edges
		Q updatedCFG = processLoopBackEdgesForCFG(cfg);
		
		try {
			traverse(updatedCFG.eval(), entryNode, new ArrayList<Node>());	
		} catch (RuntimeException e) {
			undoChanges();
			Log.warning("Too many paths");
			return paths;
		}
		
		
		// clean up -- remove nodes and edges added to enumerate paths
		Log.info("Enumerated " + paths.size() + " paths");
		undoChanges();
		return paths;
	}

	*//**
	 * For each loop header in the cfg, adds a loop "re-entry" node 
	 * -- see method addReentryNodesForLoop
	 *  
	 * @param cfg
	 * @return
	 *//*
	public static Q processLoopBackEdgesForCFG(Q cfg) {
		
		tempReentryNodes = new AtlasHashSet<Node>();

		AtlasSet<Node> newNodes = new AtlasHashSet<Node>();
		AtlasSet<Edge> newEdges = new AtlasHashSet<Edge>();
		AtlasSet<Node> loopHeaders = cfg.eval().nodes().taggedWithAll(XCSG.Loop);

		for (Node loopHeader : loopHeaders) {

			addReentryNodesForLoop(loopHeader, newNodes, newEdges);
			
			// save added nodes globally, so they can be removed later
			tempReentryNodes.addAll(newNodes);
		}
		return cfg.union(Common.toQ(newNodes)).union(Common.toQ(newEdges));
	}

	*//**
	 * Inserts a new "re-entry" node on all loop back edges, 
	 * so that when the loop back edges are removed for path enumeration (to break cycles),
	 * re-entry nodes will help account for cyclic paths as well
	 *  
	 * @param loopHeader
	 * @param newNodes
	 * @param newEdges
	 *//*
	public static void addReentryNodesForLoop(Node loopHeader,
			AtlasSet<Node> newNodes, AtlasSet<Edge> newEdges) {
		// a loop may have multiple loop back edges 
		// we call the source of each loop back edge a "re-entry node"

		// get the unique header id
		int id = (int) loopHeader.getAttr(CFGNode.LOOP_HEADER_ID);
		Q existingReentryNode = Query.universe()
				.nodes("LOOP_REENTRY_NODE")
				.selectNode(XCSG.name, "LOOP_REENTRY_NODE for " + id);

		if (!CommonQueries.isEmpty(existingReentryNode)) {
			return; // already processing has been done
		}

		// get all back edges for this loop
		AtlasSet<Edge> backEdges = Common.toQ(loopHeader).reverseStepOn(Query.universe().edges(XCSG.ControlFlowBackEdge)).eval().edges();

		// add new re-entry node, which will act as the single hub connecting
		// all current re-entry nodes to this loop header
		Node newReentryNode = Graph.U.createNode();
		newReentryNode.attr().put(XCSG.name, "LOOP_REENTRY_NODE for " + id);
		newReentryNode.attr().put(CFGNode.LOOP_MEMBER_ID, id);
		newReentryNode.tags().add("LOOP_REENTRY_NODE");
		newReentryNode.tags().add(XCSG.ControlFlow_Node);
		newNodes.add(newReentryNode);

		// add the new re-entry node to the method via XCSG.Contains, 
		// so it is in the method scope
		Edge newContainsEdge = Graph.U.createEdge(CommonQueries.getContainingFunction(loopHeader), newReentryNode);
		newContainsEdge.tag(XCSG.Contains);
		newEdges.add(newContainsEdge);

		for (Edge backEdge : backEdges) {
			// locate the current re-entry node (source of the current loop back edge)
			Node reentryNode = backEdge.from();

			// add a new loop back edge from the new re-entry node to the loop header
			Edge newBackEdge = Graph.U.createEdge(newReentryNode, loopHeader);
			newBackEdge.putAllAttr(backEdge.attr());
			newBackEdge.attr().put(XCSG.name, "LOOP_BACK_EDGE for " + id);
			newBackEdge.tags().add(XCSG.ControlFlow_Edge);
			newBackEdge.tags().add(XCSG.ControlFlowBackEdge);
			newBackEdge.tags().add("LOOP_BACK_EDGE");
			newEdges.add(newBackEdge);

			// add a new control flow edge from the original re-entry node to the new re-entry node
			Edge newIntermediateEdge = Graph.U.createEdge(reentryNode, newReentryNode);
			newIntermediateEdge.attr().put(XCSG.name, "TEMP_EDGE" + id);
			newIntermediateEdge.tags().add(XCSG.ControlFlow_Edge);
			newEdges.add(newIntermediateEdge);
		}

	}

	*//**
	 * Recursive enumeration of paths starting from the current node in the given graph
	 * Thanks to Ahmed for sharing the code for depth first traversal 
	 * 
	 * @param graph
	 * @param currentNode
	 * @param path
	 *//*
	private static boolean traverse(Graph graph, Node currentNode,
			ArrayList<Node> path) {

		path.add(currentNode);

		AtlasSet<Node> children = getChildNodes(graph, currentNode);
		if (children.size() > 1) {
			for (Node child : children) {
				ArrayList<Node> newPath = new ArrayList<Node>(
						path);
				try {
					boolean rec = traverse(graph, child, newPath);
					if(!rec) {
						return false;
					}
				} catch (java.lang.StackOverflowError e) {
				    Log.warning(currentLoopHeader.eval().nodes().one().getAttr(CFGNode.LOOP_HEADER_ID).toString()+
				    		currentLoopHeader.eval().nodes().one().getAttr(XCSG.name).toString()+
				    		currentLoopHeader.parent().eval().nodes().one().getAttr(XCSG.name)+
				    		" Recursion depth for path enumeration is exceeded");
				    return false;
				}
			}
		} else if (children.size() == 1) {
			try {
				boolean rec = traverse(graph, children.one(), path);
				if(!rec) {
					return false;
				}
			} catch (java.lang.StackOverflowError e) {
				Log.warning(currentLoopHeader.eval().nodes().one().getAttr(CFGNode.LOOP_HEADER_ID).toString()+
			    		currentLoopHeader.eval().nodes().one().getAttr(XCSG.name).toString()+
			    		currentLoopHeader.parent().eval().nodes().one().getAttr(XCSG.name)+
			    		" Recursion depth for path enumeration is exceeded");
			    return false;
			}
		} else if (children.isEmpty()) {
			paths.add(path);
//			printPath(path);
		}
		return true;
	}
	
		public static AtlasSet<Node> getBoundaryConditions(Node loopHeader) {
		return Query.universe().selectNode(CFGNode.LOOP_MEMBER_ID,loopHeader.getAttr(CFGNode.LOOP_HEADER_ID)).nodes(BoundaryConditions.BOUNDARY_CONDITION).eval().nodes();
	}
*/
	/**
	 * Finds all control flow children except back edges  
	 * Thanks Ahmed
	 * 
	 * @param graph
	 * @param node
	 * @return
	 *//*
	private static AtlasSet<Node> getChildNodes(Graph graph,
			Node node) {
		AtlasSet<Edge> edges = graph.edges(node, NodeDirection.OUT);
		AtlasSet<Edge> backEdges = edges
				.taggedWithAny("LOOP_BACK_EDGE",XCSG.ControlFlowBackEdge);
		AtlasSet<Node> childNodes = new AtlasHashSet<Node>();

		for (Edge edge : edges) {
			if (backEdges.contains(edge))
				continue;
			Node child = edge.to();
			childNodes.add(child);
		}
		return childNodes.taggedWithAll(XCSG.ControlFlow_Node);
	}*/
	
	
	
	
	
	
	
	
	
	/**
	 * Return nodes from which a loop back edge goes into the loop header
	 * 
	 * @param loopHeader
	 * @return
	 */
	public static Q getLoopReentryNodes(Node loopHeader) {
		Q reentryNodes = Common.empty();
		AtlasSet<Edge> backEdges = Common.toQ(loopHeader).reverseStepOn(Query.universe().edges(XCSG.ControlFlowBackEdge)).eval().edges();
		
		for (Edge backEdge : backEdges) {
			// locate the current re-entry node (source of the current loop back edge)
			Node reentryNode = backEdge.from();
			reentryNodes = reentryNodes.union(Common.toQ(reentryNode));
		}		
		
		return reentryNodes;
	}
	
	/**
	 * Pretty printer for a path in the graph
	 * @param path
	 */
	private static void printPath(List<Node> path) {
		@SuppressWarnings("unused")
		String s = "";
		for (Node g : path) {
			s += g.getAttr(XCSG.name);
			s += " -> ";
		}
//		Log.info("Path " + s);
	}
	

	/**
	 * Returns the set of paths from the loop header to the exit points of the loop, i.e., 
	 * control flow nodes from which a loop back edge goes back into the loop header
	 * 
	 * @param loopHeader
	 * @return
	 */
	public static List<List<Node>> getLoopPaths(Node loopHeader) {
		Q method = Common.toQ(CommonQueries.getContainingFunction(loopHeader));
		
		AtlasSet<Node> loopReentryNodes = getLoopReentryNodes(loopHeader).eval().nodes();
		
		Set<Node> sources = new HashSet<Node>();
		sources.add(loopHeader);
		
		Set<Node> targets = new HashSet<Node>();
		for(Node b : loopReentryNodes) {
			targets.add(b);
		}
		targets.add(loopHeader);
		
		Q cfgMethod = CommonQueries.cfg(method);
		Q pathsForBC = cfgMethod.between(Common.toQ(loopHeader), Common.toQ(loopReentryNodes));
		
		DirectedGraph<Node, Edge> jg = getJgraphTDirectedGraph(pathsForBC);
		int maxPaths = 500; // limit to 500 paths
		LimitedDirectedPaths<Node, Edge> adp = new LimitedDirectedPaths<Node, Edge>(jg, maxPaths);
		List<GraphPath<Node,Edge>> pathsInLoop = adp.getAllPaths(sources, targets, true, null);
	
		List<List<Node>> paths = new ArrayList<List<Node>>();
		for(GraphPath<Node, Edge> gp : pathsInLoop) {
			List<Node> p = gp.getVertexList();
			paths.add(p);
			printPath(p);
		}
		
		return paths;
	}
	
	/**
	 * Convenience method to make a JGraphT DirectedGraph from a query 
	 * 
	 * @param g
	 * @return
	 */
	private static DirectedGraph<Node, Edge> getJgraphTDirectedGraph(Q g) {
		DirectedPseudograph<Node, Edge> jGraph = new DirectedPseudograph<Node, Edge>(Edge.class);
		
		for(Node node : g.eval().nodes()) 
			jGraph.addVertex(node);
		
		for(Edge edge : g.eval().edges()) 
			jGraph.addEdge(edge.from(), edge.to(), edge);
		
		return jGraph;
	}
}