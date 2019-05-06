package com.kcsl.loop.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IteratorPatternSignatures {
		
	public static final Map<String, Set<String>> iteratorAPIs;
	
	public enum IteratorPatternTypes {
	    Iterator (MethodSignatures.SIGNATURE_ITERATOR_HAS_NEXT),
	    StringTokenizer (MethodSignatures.SIGNATURE_HAS_NEXT_TOKEN),
	    Enumeration (MethodSignatures.SIGNATURE_ENUMERATION_HASMORELEMENTS);

	    private final String api;       

	    private IteratorPatternTypes(String s) {
	        api = s;
	    }

	    public boolean equalsName(String otherName) {
	        return (otherName == null) ? false : api.equals(otherName);
	    }

	    public String toString() {
	       return this.api;
	    }
	}
	
	static {
		
		iteratorAPIs = new HashMap<>();
		iteratorAPIs.put(MethodSignatures.SIGNATURE_HAS_NEXT_TOKEN, new HashSet<>(Arrays.<String>asList(MethodSignatures.SIGNATURE_STRING_NEXT_TOKEN)));
		iteratorAPIs.put(MethodSignatures.SIGNATURE_ITERATOR_HAS_NEXT, new HashSet<>(Arrays.<String>asList(MethodSignatures.SIGNATURE_ITERATOR_NEXT)));
		iteratorAPIs.put(MethodSignatures.SIGNATURE_ENUMERATION_HASMORELEMENTS, new HashSet<>(Arrays.<String>asList(MethodSignatures.SIGNATURE_ENUMERATION_NEXTELEMENT)));
	}
	
}
