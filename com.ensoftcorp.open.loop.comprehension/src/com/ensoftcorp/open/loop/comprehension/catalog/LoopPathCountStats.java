package com.ensoftcorp.open.loop.comprehension.catalog;

import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.log.Log;
import com.ensoftcorp.open.loop.comprehension.utils.PathProperty;
import com.ensoftcorp.open.loop.comprehension.utils.PathPropertyMap;

public class LoopPathCountStats {

	public static LoopPathCounts computePathCountStats(Node loopHeader) {

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

class LoopPathCounts {
	int numPaths = 0;
	int numPathsWithCallSites = 0;
	int numPathsWithoutCallSites = 0;
	int numPathsWithJDKCallSites = 0;
	int numPathsWithJDKCallSitesOnly = 0;
	int numPathsWithoutJDKCallSites = 0;
	int numPathsWithNonJDKCallSites = 0;
	
	public int getNumPaths() {
		return numPaths;
	}
	public void setNumPaths(int numPaths) {
		this.numPaths = numPaths;
	}
	public int getNumPathsWithCallSites() {
		return numPathsWithCallSites;
	}
	public void setNumPathsWithCallSites(int numPathsWithCallSites) {
		this.numPathsWithCallSites = numPathsWithCallSites;
	}
	public int getNumPathsWithoutCallSites() {
		return numPathsWithoutCallSites;
	}
	public void setNumPathsWithoutCallSites(int numPathsWithoutCallSites) {
		this.numPathsWithoutCallSites = numPathsWithoutCallSites;
	}
	public int getNumPathsWithJDKCallSites() {
		return numPathsWithJDKCallSites;
	}
	public void setNumPathsWithJDKCallSites(int numPathsWithJDKCallSites) {
		this.numPathsWithJDKCallSites = numPathsWithJDKCallSites;
	}
	public int getNumPathsWithJDKCallSitesOnly() {
		return numPathsWithJDKCallSitesOnly;
	}
	public void setNumPathsWithJDKCallSitesOnly(int numPathsWithJDKCallSitesOnly) {
		this.numPathsWithJDKCallSitesOnly = numPathsWithJDKCallSitesOnly;
	}
	public int getNumPathsWithoutJDKCallSites() {
		return numPathsWithoutJDKCallSites;
	}
	public void setNumPathsWithoutJDKCallSites(int numPathsWithoutJDKCallSites) {
		this.numPathsWithoutJDKCallSites = numPathsWithoutJDKCallSites;
	}
	public int getNumPathsWithNonJDKCallSites() {
		return numPathsWithNonJDKCallSites;
	}
	public void setNumPathsWithNonJDKCallSites(int numPathsWithNonJDKCallSites) {
		this.numPathsWithNonJDKCallSites = numPathsWithNonJDKCallSites;
	}
	
	
}