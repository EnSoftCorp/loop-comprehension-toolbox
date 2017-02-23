package com.ensoftcorp.open.loop.comprehension.utils;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;

public class Tags {
	
	public static void setTag(Q q, String tag) {
		// NOTE: [jdm] resolving avoids the possibility of a ConcurrentModificationException (iteration that depend on tags conflicts with modifying tags)
		q = Common.resolve(null, q); 
		
		for (GraphElement node : q.eval().nodes()) {
			node.tag(tag);
		}
		for (GraphElement edge : q.eval().edges()) {
			edge.tag(tag);
		}
	}

}