package com.kcsl.loop.catalog.subsystems;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.subsystems.Subsystem;
import com.ensoftcorp.open.java.commons.subsystems.JavaSubsystem;
import com.kcsl.loop.util.SetDefinitions;

public class AppSubsystem extends JavaSubsystem {

	public static final String TAG = "APP_SUBSYSTEM";

	@Override
	public String getName() {
		return "App";
	}

	@Override
	public String getDescription() {
		return "App";
	}

	@Override
	public String getTag() {
		return TAG;
	}

	@Override
	public String[] getParentTags() {
		return new String[] { Subsystem.ROOT_SUBSYSTEM_TAG };
	}
	
	public void tagSubsystem() {
		Q app = SetDefinitions.app();
		Q packagesInApp = app.contained().nodes(XCSG.Package);
		AtlasSet<Node> packagesToTag = new AtlasHashSet<Node>(packagesInApp.eval().nodes());
		for(Node p: packagesToTag) {
			p.tag(getTag());
		}
	}
}
