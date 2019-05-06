package com.kcsl.loop.catalog.subsystems;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.subsystems.JavaSubsystem;

public class SpliteratorSubsystem extends JavaSubsystem {

	public static final String TAG = "SPLITERATOR_SUBSYSTEM";
	
	@Override
	public String getName() {
		return "Spliterator";
	}

	@Override
	public String getDescription() {
		return "Spliterators in JDK";
	}

	@Override
	public String getTag() {
		return TAG;
	}
	
	@Override
	public String[] getParentTags() {
		return new String[] { IteratorSubsystem.TAG };
	}
	
	@Override
	public String[] getTypes() {
		return new String[] {
			"java.util.Spliterator", "java.util.Spliterators",
			"java.util.Spliterator.AbstractDoubleSpliterator",
			"java.util.Spliterator.AbstractIntSpliterator",
			"java.util.Spliterator.AbstractLongSpliterator",
			"java.util.Spliterator.AbstractSpliterator"
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
