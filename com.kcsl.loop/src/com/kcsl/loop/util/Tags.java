package com.kcsl.loop.util;

import java.util.HashSet;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.Reflection;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;

public class Tags {
	
	public static void setTagWithConfirmation(Q q, String tag) {
		if(q != null) {
			if(promptUserToTagArtifacts(tag, q.eval().nodes().size()+q.eval().edges().size())) {
				setTag(q, tag);
			}
		}
	}
	
	public static void setTag(Q q, String tag) {
		// NOTE: [jdm] resolving avoids the possibility of a ConcurrentModificationException (iteration that depend on tags conflicts with modifying tags)
		q = Common.resolve(null, q); 
		
		for (Node node : q.eval().nodes()) {
			node.tag(tag);
		}
		for (Edge edge : q.eval().edges()) {
			edge.tag(tag);
		}
		
//		Log.debug("Tagged " + (q.eval().nodes().size()+q.eval().edges().size()) + " artifacts with \""+tag+"\"");
	}

	public static Boolean promptUserToTagArtifacts(String tag, long count) {
		Boolean choice = DisplayUtils.promptBoolean("Confirm tag instruction","This operation will tag "+count+" artifacts with \""+tag+"\". Do you want to proceed?");
		return choice;
	}
	
	public static Set<String> getTagsWithPrefix(String prefix) {
		Set<String> tagNames = new HashSet<String>();
		for(String nodeTag: Reflection.nodeTags(Graph.U)) {
			if(nodeTag.startsWith(prefix)) {
				tagNames.add(nodeTag);
			}
		}
		return tagNames;
	}
}
