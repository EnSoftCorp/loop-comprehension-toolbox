package com.kcsl.loop.catalog.subsystems;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.subsystems.Subsystem;
import com.ensoftcorp.open.java.commons.subsystems.JavaSubsystem;
import com.kcsl.loop.util.SetDefinitions;

public class OtherLibrariesSubsystem extends JavaSubsystem {

	public static final String TAG = "OTHER_LIBRARIES_SUBSYSTEM";

	@Override
	public String getName() {
		return "Other libraries";
	}

	@Override
	public String getDescription() {
		return "Other Libraries (non-JDK)";
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
		Q otherLibraries = SetDefinitions.libraries();//.difference(SetDefinitions.JDKLibraries().contained());
		Q packagesInOtherLibraries = otherLibraries.contained().nodes(XCSG.Package);
		AtlasSet<Node> packagesToTag = new AtlasHashSet<Node>(packagesInOtherLibraries.eval().nodes());
		for(Node p: packagesToTag) {
			p.tag(getTag());
		}
	}
}
