package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.Collection;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.loop.comprehension.log.Log;

/**
 * Matches signature strings with the ##signature tag on data flow callsite node
 * Evaluates targets of the callsite and finds targets in JDK and those outside of JDK
 * @author gsanthan
 *
 */
public class MethodSignatureMatcher {

	/**
	 * Matches signature strings with the ##signature tag on data flow callsite node
	 * 
	 * @param callsite data flow node whose signature is matched
	 * @param signature String signature to be matched
	 * @return 
	 */
	public static boolean matchSignature(Node callsite, String signature) {
		
		if(!callsite.taggedWith(XCSG.CallSite)) {
			throw new RuntimeException("Expected a callsite as argument");
		}
		// ge matches the signature
//		return CallSite.getTargetMethods(callsite).eval().nodes().one().getAttr("##signature").equals(signature);
		Node target = CallSiteAnalysis.getTargetMethods(callsite).eval().nodes().one();
		if(target == null || target.getAttr("##signature") == null) {
			if(target != null) {
				Log.info("null " + target.address().toAddressString());
			}
			return false;
		}
		return	target.getAttr("##signature").equals(signature);
	}
	
	/**
	 * Matches one of the signature strings with the ##signature tag on data flow callsite node
	 * 
	 * @param callsite data flow node whose signature is matched
	 * @param signature String signature to be matched
	 * @return
	 */
	public static String matchSignatureWithAny(Node callsite, Collection<String> signatures) {
		
		if(!callsite.taggedWith(XCSG.CallSite)) {
			throw new RuntimeException("Expected a callsite as argument");
		}
		for(String signature: signatures) {
			// if even one signatures matches, return true
			if(matchSignature(callsite, signature)) {
				return signature;
			}
		}
		
		// ge matches none of the signatures passed
		return null;
	}
	
	/**
	 * Matches one of the signature strings with the ##signature tag on data flow callsite node
	 * 
	 * @param callsite data flow node whose signature is matched
	 * @param signature String signature to be matched
	 * @return
	 */
	public static boolean isMatchSignatureWithAny(Node callsite, Collection<String> signatures) {
		
		String result = matchSignatureWithAny(callsite, signatures);
		if(result != null && result.trim().length() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Subset of targets for the callsite that are in the JDK
	 * 
	 * @param callsite data flow node 
	 * @return
	 */
	public static Q getTargetsWithinJDK(GraphElement callsite) {

		if(!callsite.taggedWith(XCSG.CallSite)) {
			throw new RuntimeException("Expected a callsite as argument");
		}
		
		return CallSiteAnalysis.getTargetMethods(callsite).intersection(JDKContainer.jdkMethods());
	}
	
	/**
	 * Returns true, if subset of targets for the callsite that are in the JDK is not empty
	 * 
	 * @param callsite data flow node 
	 * @return
	 */
	public static boolean hasTargetsWithinJDK(GraphElement callsite) {

		return !CommonQueries.isEmpty(getTargetsWithinJDK(callsite));
	}
	
	/**
	 * Subset of targets for the callsite that are outside the JDK
	 * 
	 * @param callsite data flow node 
	 * @return
	 */
	public static Q getTargetsOutsideJDK(GraphElement callsite) {

		if(!callsite.taggedWith(XCSG.CallSite)) {
			throw new RuntimeException("Expected a callsite as argument");
		}
		
		return CallSiteAnalysis.getTargetMethods(callsite).difference(JDKContainer.jdkMethods());
	}
	
	/**
	 * Returns true, if subset of targets for the callsite that are outside the JDK is not empty
	 * 
	 * @param callsite data flow node 
	 * @return
	 */

	public static boolean hasTargetsOutsideJDK(GraphElement callsite) {

		return !CommonQueries.isEmpty(getTargetsOutsideJDK(callsite));
	}
}