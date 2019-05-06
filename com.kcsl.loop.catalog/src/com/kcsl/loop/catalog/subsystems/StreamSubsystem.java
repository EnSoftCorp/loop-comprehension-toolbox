package com.kcsl.loop.catalog.subsystems;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.subsystems.JavaSubsystem;

public class StreamSubsystem extends JavaSubsystem {

	public static final String TAG = "STREAM_SUBSYSTEM";

	@Override
	public String getName() {
		return "Stream";
	}

	@Override
	public String getDescription() {
		return "Stream APIs in JDK";
	}

	@Override
	public String getTag() {
		return TAG;
	}

	@Override
	public String[] getParentTags() {
		return new String[] { CollectionSubsystem.TAG };
	}

	@Override
	public String[] getNamespaces() {
		return new String[] { "java.util.stream" };
	}

	@Override
	public void tagSubsystem() {

		super.tagSubsystem();

		Q methodNodes = Query.universe().nodesTaggedWithAll(getTag()).contained().nodes(XCSG.Method);
		Q overridenMethodNodes = Query.universe().edges(XCSG.Overrides).reverse(methodNodes).retainNodes();
		for (Node mNode : overridenMethodNodes.eval().nodes()) {
			mNode.tag(getTag());
		}
	}

}
