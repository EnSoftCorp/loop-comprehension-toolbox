package com.kcsl.loop.catalog.subsystems;

import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.open.java.commons.subsystems.JavaCoreSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.JavaSubsystem;
import com.ensoftcorp.open.java.commons.wishful.JavaStopGap;
import com.kcsl.loop.util.MethodSignatures;

public class CollectionSubsystem extends JavaSubsystem {

	public static final String TAG = "COLLECTION_SUBSYSTEM";

	@Override
	public String getName() {
		return "Collection";
	}

	@Override
	public String getDescription() {
		return "Collections in JDK";
	}

	@Override
	public String getTag() {
		return TAG;
	}

	@Override
	public String[] getParentTags() {
		return new String[] { JavaCoreSubsystem.TAG };
	}
	
	public void tagSubsystem() {
		Set<String> collectionAPIs = MethodSignatures.COLLECTION_APIs;
		if (collectionAPIs != null) {
			for (String api : collectionAPIs) {
				Q methodNodes = Query.universe().selectNode(JavaStopGap.SIGNATURE,api);
				for(Node mNode : methodNodes.eval().nodes()) {
					mNode.tag(getTag());
				}
			}
		}
	}
}
