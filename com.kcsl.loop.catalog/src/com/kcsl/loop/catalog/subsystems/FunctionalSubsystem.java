package com.kcsl.loop.catalog.subsystems;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.subsystems.JavaCoreSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.JavaSubsystem;

public class FunctionalSubsystem extends JavaSubsystem {

	public static final String TAG = "FUNCTIONAL_SUBSYSTEM";
	
	@Override
	public String getName() {
		return "Functional";
	}

	@Override
	public String getDescription() {
		return "APIs related to functional programming (added in Java 8)";
	}

	@Override
	public String getTag() {
		return TAG;
	}
	
	@Override
	public String[] getParentTags() {
		return new String[] { JavaCoreSubsystem.TAG };
	}

	@Override
	public String[] getNamespaces() {
		return new String[] {
			"java.util.function"	
		};
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
