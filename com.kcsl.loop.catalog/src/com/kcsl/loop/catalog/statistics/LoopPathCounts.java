package com.kcsl.loop.catalog.statistics;

public class LoopPathCounts {
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