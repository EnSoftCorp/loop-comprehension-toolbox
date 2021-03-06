package com.kcsl.loop.catalog.statistics;

import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.kcsl.loop.catalog.log.Log;
import com.kcsl.loop.preferences.LoopPreferences;
import com.kcsl.loop.util.PathProperty;
import com.kcsl.loop.util.PathPropertyMap;

public class LoopPathCountStats {

	public static LoopPathCounts computePathCountStats(Node loopHeader) {

		if (!LoopPreferences.isCatalogLoopsPathStatisticsEnabled()) {
			LoopPathCounts counts = new LoopPathCounts();
			counts.setNumPaths(-1);
			counts.setNumPathsWithCallSites(-1);
			counts.setNumPathsWithJDKCallSites(-1);
			counts.setNumPathsWithJDKCallSitesOnly(-1);
			counts.setNumPathsWithNonJDKCallSites(-1);
			counts.setNumPathsWithoutCallSites(-1);
			counts.setNumPathsWithoutJDKCallSites(-1);
			
			return counts;
		}
		
		int numPaths = 0;
		int numPathsWithCallSites = 0;
		int numPathsWithoutCallSites = 0;
		int numPathsWithJDKCallSites = 0;
		int numPathsWithJDKCallSitesOnly = 0;
		int numPathsWithoutJDKCallSites = 0;
		int numPathsWithNonJDKCallSites = 0;
		
		try {
			List<List<com.ensoftcorp.atlas.core.db.graph.Node>> paths = PathEnumerator.getLoopPaths(loopHeader);

			// total path stats
			numPaths = paths.size();

			// path stats by bucket
			for(List<com.ensoftcorp.atlas.core.db.graph.Node> path: paths) {
				PathPropertyMap properties = PathAnalyzer.analyze(path);
				if((boolean)properties.getProperty(PathProperty.ContainsCallSites)) {
					numPathsWithCallSites++;
				} 
				if((boolean)properties.getProperty(PathProperty.ContainsJDKCallSites)) {
					numPathsWithJDKCallSites++;
				}
				if((boolean)properties.getProperty(PathProperty.ContainsJDKCallSitesOnly)) {
					numPathsWithJDKCallSitesOnly++;
				}
				if((boolean)properties.getProperty(PathProperty.ContainsNoCallSites)) {
					numPathsWithoutCallSites++;
				}
				if((boolean)properties.getProperty(PathProperty.ContainsNoJDKCallSites)) {
					numPathsWithoutJDKCallSites++;
				}
				if((boolean)properties.getProperty(PathProperty.ContainsNonJDKCallSites)) {
					numPathsWithNonJDKCallSites++;
				}
			}
		} catch(Throwable t) {
			Log.warning("Error enumerating paths of loop "+loopHeader.getAttr(CFGNode.LOOP_HEADER_ID).toString());
		}
		
		LoopPathCounts counts = new LoopPathCounts();
		counts.setNumPaths(numPaths);
		counts.setNumPathsWithCallSites(numPathsWithCallSites);
		counts.setNumPathsWithJDKCallSites(numPathsWithJDKCallSites);
		counts.setNumPathsWithJDKCallSitesOnly(numPathsWithJDKCallSitesOnly);
		counts.setNumPathsWithNonJDKCallSites(numPathsWithNonJDKCallSites);
		counts.setNumPathsWithoutCallSites(numPathsWithoutCallSites);
		counts.setNumPathsWithoutJDKCallSites(numPathsWithoutJDKCallSites);
		
		return counts;
	}
	
}

