package com.kcsl.loop.catalog.statistics;

import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.kcsl.loop.util.LoopUtils;
import com.kcsl.loop.util.PathProperty;
import com.kcsl.loop.util.PathPropertyMap;
import com.kcsl.loop.util.SubsystemUtils;

public class CallSitesInPathAnalyzer extends PathAnalyzer {

	@Override
	public PathPropertyMap analyzeProperty(List<Node> path) {
		
		
		AtlasSet<Node> nodeSet = new AtlasHashSet<Node>();
		for (Node n: path){
			nodeSet.add(n);
		}
		
		Q statementsWithCallSites = LoopUtils.findStatementsWithCallSites(Common.toQ(nodeSet));
		Q statementsWithJDKCallSites = SubsystemUtils.findSubsetWithJDKTargets(statementsWithCallSites);
		
		long numCallSites = statementsWithCallSites.eval().nodes().size();
		long numJDKCallSites = statementsWithJDKCallSites.eval().nodes().size();
		long numNodes = nodeSet.size();
		
		PathPropertyMap map = new PathPropertyMap();
		
		map.putProperty(PathProperty.StatementsWithCallSites, numCallSites);
		map.putProperty(PathProperty.StatementsWithJDKCallSites, numJDKCallSites);
		map.putProperty(PathProperty.StatementsWithoutCallSites, numNodes - numCallSites);
		
		map.putProperty(PathProperty.ContainsCallSites, numCallSites > 0);
		map.putProperty(PathProperty.ContainsJDKCallSites, numJDKCallSites > 0);
		map.putProperty(PathProperty.ContainsJDKCallSitesOnly, (numCallSites > 0 && numCallSites == numJDKCallSites));
		map.putProperty(PathProperty.ContainsNoJDKCallSites, (numJDKCallSites == 0));
		map.putProperty(PathProperty.ContainsNoCallSites, (numCallSites == 0));
		map.putProperty(PathProperty.ContainsNonJDKCallSites, (numCallSites > numJDKCallSites));

		return map;
	}
}