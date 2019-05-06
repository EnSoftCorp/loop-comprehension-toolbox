package com.kcsl.loop.util;

import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.java.core.index.summary.SummaryGraph.SummaryNode;

public class Lookup {

	private static Q u() {
		Q u = Query.universe();
		return u;
	}
	
	public static Q findType(String binaryName) {
		Q system = u().selectNode(Attr.Node.BINARY_NAME, binaryName);
		return system;
	}
	
	public static Q findMethod(String binaryName, String methodName) {
		Q t = findType(binaryName);
		Q methods = t.children().methods(methodName);
		return methods;
	}

	public static Q findMethodSignature(String binaryName, String methodSignature) {
		Q t = findType(binaryName);
		Q methods = t.children().selectNode(SummaryNode.METHOD_KEY);
		return methods;
	}

	public static Q findField(String binaryName, String fieldName) {
		Q t = findType(binaryName);
		Q fields = t.children().fields(fieldName);
		return fields;
	}


}
