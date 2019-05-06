package com.kcsl.loop.catalog.subsystems;

import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.open.java.commons.subsystems.JavaCoreSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.JavaSubsystem;
import com.ensoftcorp.open.java.commons.wishful.JavaStopGap;
import com.kcsl.loop.util.MethodSignatures;

public class IteratorSubsystem extends JavaSubsystem {

	public static final String TAG = "ITERATOR_SUBSYSTEM";

	@Override
	public String getName() {
		return "Iterator";
	}

	@Override
	public String getDescription() {
		return "Iterators in JDK";
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
		Set<String> iteratorAPIs = MethodSignatures.ITERATOR_APIs;
		if (iteratorAPIs != null) {
			for (String api : iteratorAPIs) {
				Q methodNodes = Query.universe().selectNode(JavaStopGap.SIGNATURE,api);
				for(Node mNode : methodNodes.eval().nodes()) {
					mNode.tag(getTag());
				}
			}
		}
	}
/*
	@Override
	public String[] getNamespaces() {
		return new String[] { "java.applet", "java.awt", "java.awt.color", "java.awt.datatransfer", "java.awt.dnd",
				"java.awt.event", "java.awt.font", "java.awt.geom", "java.awt.im", "java.awt.im.spi", "java.awt.image",
				"java.awt.image.renderable", "java.awt.print", "javax.accessibility", "javax.swing",
				"javax.swing.border", "javax.swing.colorchooser", "javax.swing.event", "javax.swing.filechooser",
				"javax.swing.plaf", "javax.swing.plaf.basic", "javax.swing.plaf.metal", "javax.swing.plaf.multi",
				"javax.swing.plaf.nimbus", "javax.swing.plaf.synth", "javax.swing.table", "javax.swing.text",
				"javax.swing.text.html", "javax.swing.text.html.parser", "javax.swing.text.rtf", "javax.swing.tree",
				"javax.swing.undo" };
	}
*/
}
