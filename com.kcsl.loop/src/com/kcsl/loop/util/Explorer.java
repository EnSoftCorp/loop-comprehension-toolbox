package com.kcsl.loop.util;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;

public class Explorer {

	public static Q step(Q origin) {
		return Query.universe().forwardStep(origin).union(Query.universe().reverseStep(origin));
	}
	
	public static Q stepLinked(Q origin) {
		Q result = Query.universe().forwardStep(origin).union(Query.universe().reverseStep(origin));
		return result.induce(Common.toQ(Query.universe().eval().edges()));
	}
	
	public static Node getContainingNode(Node node, String containingTag) {
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
}
