package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.Collection;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.jimple.commons.analysis.SetDefinitions;

/**
 * Matches signature strings with the ##signature tag on data flow callsite node
 * Evaluates targets of the callsite and finds targets in JDK and those outside of JDK
 * @author Payas Awadhutkar, Ganesh Ram Santhanam
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
		Node target = CallSiteAnalysis.getTargets(callsite).one(); // TODO: there could be more than one!
		if(target == null || target.getAttr("##signature") == null) {
			if(target != null) {
				
				// this is really noisy, my logs are screaming at me...also why are we only using one of the getTargetMethods? ~BH
				
//				Log.info("null " + target.address().toAddressString());
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
	public static Q getTargetsWithinJDK(Node callsite) {

		if(!callsite.taggedWith(XCSG.CallSite)) {
			throw new RuntimeException("Expected a callsite as argument");
		}
		
		return Common.toQ(CallSiteAnalysis.getTargets(callsite)).intersection(SetDefinitions.JDKLibraries().nodes(XCSG.Method));
	}
	
	/**
	 * Returns true, if subset of targets for the callsite that are in the JDK is not empty
	 * 
	 * @param callsite data flow node 
	 * @return
	 */
	public static boolean hasTargetsWithinJDK(Node callsite) {

		return !CommonQueries.isEmpty(getTargetsWithinJDK(callsite));
	}
	
	/**
	 * Subset of targets for the callsite that are outside the JDK
	 * 
	 * @param callsite data flow node 
	 * @return
	 */
	public static Q getTargetsOutsideJDK(Node callsite) {

		if(!callsite.taggedWith(XCSG.CallSite)) {
			throw new RuntimeException("Expected a callsite as argument");
		}
		
		return Common.toQ(CallSiteAnalysis.getTargets(callsite)).difference(SetDefinitions.JDKLibraries().nodes(XCSG.Method));
	}
	
	/**
	 * Returns true, if subset of targets for the callsite that are outside the JDK is not empty
	 * 
	 * @param callsite data flow node 
	 * @return
	 */

	public static boolean hasTargetsOutsideJDK(Node callsite) {

		return !CommonQueries.isEmpty(getTargetsOutsideJDK(callsite));
	}
}
