package com.kcsl.loop.catalog.subsystems;

import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.open.java.commons.subsystems.JavaCoreSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.JavaSubsystem;
import com.ensoftcorp.open.java.commons.wishful.JavaStopGap;
import com.kcsl.loop.util.ImplicitLoopAPISignatures;

public class ImplicitLoopSubsystem extends JavaSubsystem {

	public static final String TAG = "IMPLICIT_LOOP_SUBSYSTEM";
	
	@Override
	public String getName() {
		return "ImplicitLoop";
	}

	@Override
	public String getDescription() {
		return "APIs forming implicit loops";
	}

	@Override
	public String getTag() {
		return TAG;
	}
	
	@Override
	public String[] getParentTags() {
		return new String[] {JavaCoreSubsystem.TAG};
	}
	
	public void tagSubsystem() {
		List<String> implicitLoopAPIs = ImplicitLoopAPISignatures.IMPLICIT_LOOP_APIS;
		if (implicitLoopAPIs != null) {
			for (String api : implicitLoopAPIs) {
				Q methodNodes = Query.universe().selectNode(JavaStopGap.SIGNATURE,api);
				for(Node mNode : methodNodes.eval().nodes()) {
					mNode.tag(getTag());
				}
			}
		}
	}
}
