package com.kcsl.loop.catalog.statistics;

import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.kcsl.loop.util.PathPropertyMap;

public abstract class PathAnalyzer {

	@SuppressWarnings("rawtypes")
	private static Class[] concretePathAnalyzers = new Class[]{CallSitesInPathAnalyzer.class};   
	
	@SuppressWarnings({ "rawtypes" })
	public final static PathPropertyMap analyze(List<Node> path) throws InstantiationException, IllegalAccessException {
		PathPropertyMap map = new PathPropertyMap();
		
		for(Class analyzer: concretePathAnalyzers) {
			PathAnalyzer concreteImplementation = ((PathAnalyzer)analyzer.newInstance());
			map.append(concreteImplementation.analyzeProperty(path));
		}
		
		return map;
	}
	
	public abstract PathPropertyMap analyzeProperty(List<Node> path);
}
