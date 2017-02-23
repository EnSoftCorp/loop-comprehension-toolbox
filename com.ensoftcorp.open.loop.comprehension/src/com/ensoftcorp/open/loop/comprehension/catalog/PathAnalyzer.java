package com.ensoftcorp.open.loop.comprehension.catalog;

import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.open.loop.comprehension.utils.PathPropertyMap;

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
