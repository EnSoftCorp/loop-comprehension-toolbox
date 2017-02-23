package com.ensoftcorp.open.loop.comprehension.utils;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Helper to quickly find artifacts in the JDK 
 * @author gsanthan
 *
 */
public class JDKContainer {
	
	/**
	 * Returns the methods in the JDK 
	 * 
	 * @return
	 */
	public static Q jdkMethods() {
		Q jdkMethods1 = Common.universe().nodesTaggedWithAny(XCSG.Library).selectNode(XCSG.name, "rt.jar").contained().nodesTaggedWithAny(XCSG.Method);
		Q jdkMethods2 = Common.universe().nodesTaggedWithAny(XCSG.Library).selectNode(XCSG.name, "jce.jar").contained().nodesTaggedWithAny(XCSG.Method);
		Q jdkMethods3 = Common.universe().nodesTaggedWithAny(XCSG.Library).selectNode(XCSG.name, "jsse.jar").contained().nodesTaggedWithAny(XCSG.Method);
		Q jdkMethods = Common.resolve(null, jdkMethods1.union(jdkMethods2, jdkMethods3));
		return jdkMethods;
	}
}
