package com.kcsl.loop.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.kcsl.loop.log.Log;

public class Attributes {
	
	public static Q getElementsWithAttribute(Q elements, String attribute) {
		Q result = Common.empty();
		for(Node n: elements.eval().nodes()) {
			if(n.hasAttr(attribute)) {
				result = result.union(Common.toQ(n));
			}
		}
		for(Edge e: elements.eval().edges()) {
			if(e.hasAttr(attribute)) {
				result = result.union(Common.toQ(e));
			}
		}
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	public static Q getElementsWithAttributeValue(Q elements, String attribute, Object value) {
		Q result = Common.empty();
		Object elementValue;
		
		for(Node n: elements.eval().nodes()) {
			elementValue = n.getAttr(attribute);
			if(elementValue != null) {
				Log.info("Value of attribute "+attribute+" is of type "+elementValue.getClass()+" and has content "+elementValue);
				if(elementValue instanceof String) {
					if(elementValue.equals(value)) {
						result = result.union(Common.toQ(n));
					}
				} else if(elementValue instanceof Collection) {
					if(((Collection) elementValue).contains(value)) {
						result = result.union(Common.toQ(n));
					}
				}
				else {
					// TODO: Handle Arrays
				}
			}
		}
		for(Edge e: elements.eval().edges()) {
			elementValue = e.getAttr(attribute);
			if(elementValue != null) {
				if(elementValue instanceof String) {
					if(elementValue.equals(value)) {
						result = result.union(Common.toQ(e));
					}
				}
				else if(elementValue instanceof Collection) {
					if(((Collection) elementValue).contains(value)) {
						result = result.union(Common.toQ(e));
					}
				}
				else {
					// TODO: Handle Arrays
				}
			}
		}
		return result;
	}
	
	public static Long getLongAttributeValue(Node node,String attributeName){
		Object attributeValue = node.getAttr(attributeName);
		if(attributeValue == null){
			return 0L;
		}
		return Long.parseLong(attributeValue.toString());
	}
	
	public static String getAttributeValueAsString(Node node,String attributeName, String defaultValueIfNull){
		Object attributeValue = node.getAttr(attributeName);
		if(attributeValue == null){
			return defaultValueIfNull;
		}
		return attributeValue.toString();
	}
	
	public static class ListAttr{

		public static String toString(List<String> value){
			if (value == null){
				return null;
			}
			if (value.isEmpty()){
				return "";
			}
			
			String s = value.get(0) != null ? value.get(0) : "";
			StringBuilder sb = new StringBuilder(s);
			for (int i=1;i<value.size();i++){
				sb.append(",");
				sb.append(value.get(i) != null ? value.get(i) : "");
			}
			return sb.toString();
		}

		public static ArrayList<String> toListOfStrings(String s){
			if (s==null){
				return null;
			} else if (s.isEmpty()){
				return new ArrayList<String>();
			}
			return new ArrayList<String>(Arrays.asList(s.split(",")));
		}
	}
}