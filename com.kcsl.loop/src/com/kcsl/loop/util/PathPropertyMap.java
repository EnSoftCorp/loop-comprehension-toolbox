package com.kcsl.loop.util;

import java.util.HashMap;
import java.util.Map;

public class PathPropertyMap {
	private Map<PathProperty, Object> propertyMap;
	
	public PathPropertyMap() {
		 propertyMap = new HashMap<PathProperty, Object>();	
	}
	
	public PathPropertyMap(PathPropertyMap copy) {
		 propertyMap = new HashMap<PathProperty, Object>(copy.getPropertyMap());	
	}
	
	public Map<PathProperty, Object> getPropertyMap() {
		return this.propertyMap;
	}
	
	public void setPropertyMap(Map<PathProperty, Object> map) {
		this.propertyMap = map;
	}
	
	public Object getProperty(PathProperty property) {
		return this.propertyMap.get(property);
	}
	
	public void putProperty(PathProperty property, Object value) {
		this.propertyMap.put(property, value);
	}
	
	public void append(Map<PathProperty, Object> map) {
		this.propertyMap.putAll(map);
	}
	
	public void append(PathPropertyMap propertyMap) {
		this.propertyMap.putAll(propertyMap.getPropertyMap());
	}
}
