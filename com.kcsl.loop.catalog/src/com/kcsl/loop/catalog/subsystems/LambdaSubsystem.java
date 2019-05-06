package com.kcsl.loop.catalog.subsystems;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.java.commons.subsystems.JavaSubsystem;

public class LambdaSubsystem extends JavaSubsystem {
	
	public static final String TAG = "LAMBDA_SUBSYSTEM";

	@Override
	public String getName() {
		return "Lambda";
	}

	@Override
	public String getDescription() {
		return "Callsites created to represent lambda expressions";
	}

	@Override
	public String getTag() {
		return TAG;
	}
	
	@Override
	public String[] getParentTags() {
		return new String[] {AppSubsystem.TAG};
	}
	
	// A dirty hack
	// Ideally overriding getMethods is the way. But it needs a class name
	// Class name is not fixed for these. Hence, the hack
	public void tagSubsystem() {
		Q methodNodes = Common.methods("bootstrap$");
		for(Node mNode : methodNodes.eval().nodes()) {
			mNode.tag(getTag());
		}
	}
	
}
