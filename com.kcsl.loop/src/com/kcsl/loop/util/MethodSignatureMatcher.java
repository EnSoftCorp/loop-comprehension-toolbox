package com.kcsl.loop.util;

import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.xcsg.Undocumented;

/**
 * Matches signature strings with the Undocumented.SIGNATURE tag on data flow callsite node

 * @author gsanthan
 * @author jmathews
 */
public class MethodSignatureMatcher {
	
	/**
	 * Matches one of the signature strings with the Undocumented.SIGNATURE tag on data flow callsite node
	 * 
	 * @param callsite data flow node whose call target is to be be matched against signatures
	 * @param signatures Set<String> set of signatures to be matched
	 * @return the signature if the call target signature matches, without checking the invoked type
	 */
	public static String matchSignatureWithAny(Node callsite, Set<String> signatures) {
		
		if(!callsite.taggedWith(XCSG.CallSite)) {
			throw new IllegalArgumentException("Expected a callsite as argument"); //$NON-NLS-1$
		}
		AtlasSet<Edge> out = callsite.out(XCSG.InvokedFunction);
		if (out == null) {
			throw new IllegalStateException();
		}
		
		Edge invokedFunction = out.one();
		if (invokedFunction == null) {
			// incomplete code (phantom reference)
			return null;
		}
		
		Node targetFunction = invokedFunction.to();
		String targetSignature = (String) targetFunction.getAttr(Undocumented.SIGNATURE);
		if (signatures.contains(targetSignature))
			return targetSignature;
		
		// callsite matches none of the signatures passed
		return null;
	}
	
	/**
	 * Matches one of the signature strings with the Undocumented.SIGNATURE tag on data flow callsite node
	 * 
	 * @param callsite data flow node whose call target is to be be matched against signatures
	 * @param signatures Set<String> set of signatures to be matched
	 * @return true if the signature if the call target signature matches, without checking the invoked type
	 */
	public static boolean isMatchSignatureWithAny(Node callsite, Set<String> signatures) {
		return matchSignatureWithAny(callsite, signatures) != null;
	}
}
