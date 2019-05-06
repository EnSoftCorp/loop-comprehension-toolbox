package com.kcsl.loop.util;

import java.util.Iterator;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.db.set.EmptyAtlasSet;
import com.ensoftcorp.atlas.core.db.set.IntersectionSet;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

public class Traversal {
	
	public static Graph reverseDF(AtlasSet<Node> origin) {
		AtlasSet<Node> stop = EmptyAtlasSet.<Node>instance(Graph.U);
		return reverseDF(origin, stop);
	}
	
	public static Graph reverseDF(AtlasSet<Node> origin, AtlasSet<Node> stop) {
		Graph context = Query.universe().edges(XCSG.DataFlow_Edge).eval();
		return traverse(context, TraversalDirection.REVERSE, origin, stop);
	}
	
	public static Graph forwardDF(AtlasSet<Node> origin) {
		AtlasSet<Node> stop = EmptyAtlasSet.<Node>instance(Graph.U);
		return forwardDF(origin, stop);
	}
	
	public static Graph forwardDF(AtlasSet<Node> origin, AtlasSet<Node> stop) {
		Graph context = Query.universe().edges(XCSG.DataFlow_Edge).eval();
		return traverse(context, TraversalDirection.FORWARD, origin, stop);
	}

	/**
	 * Selects a subgraph of the given graph, by traversal in a given direction
	 * from the origin nodes.
	 * 
	 * @param graph
	 * @param direction FORWARD or REVERSE
	 * @param origin possible starting nodes
	 * @param stop nodes at which to stop traversal if encountered; such nodes are included in the result
	 * @return
	 */
	public static Graph traverse(Graph graph, TraversalDirection direction, AtlasSet<Node> origin, AtlasSet<Node> stop) {
		
		final NodeDirection nodeDirection = (direction == TraversalDirection.REVERSE) ? NodeDirection.IN : NodeDirection.OUT;
		final EdgeDirection edgeDirection = (direction == TraversalDirection.REVERSE) ? EdgeDirection.FROM : EdgeDirection.TO;
		
		AtlasHashSet<Node> nodesInGraph = new AtlasHashSet<Node>();
		AtlasHashSet<Edge> edgesInGraph = new AtlasHashSet<Edge>();

		AtlasHashSet<Node> frontier = new AtlasHashSet<Node>();
		frontier.addAll(new IntersectionSet<Node>(graph.nodes(), origin));
		
		while (!frontier.isEmpty()) {
			Iterator<Node> itr = frontier.iterator();
			Node currentNode = itr.next();
			itr.remove();

			nodesInGraph.add(currentNode);
			
			if (stop.contains(currentNode)) {
				continue;
			}
				
			AtlasSet<Edge> edges = graph.edges(currentNode, nodeDirection);

			for (Edge edge : edges) {
				edgesInGraph.add(edge);
				Node nextNode = edge.getNode(edgeDirection);
				if (!nodesInGraph.contains(nextNode)) {
					frontier.add(nextNode);
				}
			}
			
		}
		
		return new UncheckedGraph(nodesInGraph, edgesInGraph);
	}
}
