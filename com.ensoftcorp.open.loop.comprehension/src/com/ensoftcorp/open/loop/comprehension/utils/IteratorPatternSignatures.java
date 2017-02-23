package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IteratorPatternSignatures {
		
	public static final Map<String, List<String>> iteratorAPIs;
	
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
		
		iteratorAPIs = new HashMap<String, List<String>>();
		iteratorAPIs.put(MethodSignatures.SIGNATURE_HAS_NEXT_TOKEN, new ArrayList<String>(Arrays.asList(new String[]{MethodSignatures.SIGNATURE_STRING_NEXT_TOKEN})));
		iteratorAPIs.put(MethodSignatures.SIGNATURE_ITERATOR_HAS_NEXT, new ArrayList<String>(Arrays.asList(new String[]{MethodSignatures.SIGNATURE_ITERATOR_NEXT})));
		iteratorAPIs.put(MethodSignatures.SIGNATURE_ENUMERATION_HASMORELEMENTS, new ArrayList<String>(Arrays.asList(new String[]{MethodSignatures.SIGNATURE_ENUMERATION_NEXTELEMENT})));
	}
	
}
