package com.kcsl.loop.ui;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;

public class SubsystemComponents implements Comparable<SubsystemComponents> {
	private String name;
	private long createdAt;
	private AtlasSet<Node> functionsSelectedForInteraction;
//	private AtlasSet<Node> differentiatingCallsitesSetA;
//	private AtlasSet<Node> differentiatingCallsitesSetB;
	private AtlasSet<Node> loopHeadersSelected;

	public SubsystemComponents(String name) {
		this.name = name;
		this.createdAt = System.currentTimeMillis();
		this.functionsSelectedForInteraction = new AtlasHashSet<Node>();
//		this.differentiatingCallsitesSetA = new AtlasHashSet<Node>();
//		this.differentiatingCallsitesSetB = new AtlasHashSet<Node>();
		this.loopHeadersSelected = new AtlasHashSet<Node>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public AtlasSet<Node> getFunctionsSelectedForInteraction() {
		return functionsSelectedForInteraction;
	}
	
	public void setFunctionsSelectedForInteraction(AtlasSet<Node> param) {
		this.functionsSelectedForInteraction = param;
	}

	public boolean addFunctionsSelectedForInteraction(AtlasSet<Node> param){
		return this.functionsSelectedForInteraction.addAll(param);
	}
	
	public boolean removeFunctionsSelectedForInteraction(Node param){
		return this.functionsSelectedForInteraction.remove(param);
	}
/*
	public AtlasSet<Node> getDifferentiatingCallsitesSetA() {
		return differentiatingCallsitesSetA;
	}

	public void setDifferentiatingCallsitesSetA(AtlasSet<Node> differentiatingCallsitesSetA) {
		this.differentiatingCallsitesSetA = differentiatingCallsitesSetA;
	}
	
	public boolean addDifferentiatingCallsitesSetA(AtlasSet<Node> differentiatingCallsitesSetA) {
		return this.differentiatingCallsitesSetA.addAll(differentiatingCallsitesSetA);
	}
	
	public boolean removeDifferentiatingCallsiteSetA(Node differentiatingCallsiteSetA) {
		return this.differentiatingCallsitesSetA.remove(differentiatingCallsiteSetA);
	}

	public AtlasSet<Node> getDifferentiatingCallsitesSetB() {
		return differentiatingCallsitesSetB;
	}

	public void setDifferentiatingCallsitesSetB(AtlasSet<Node> differentiatingCallsitesSetB) {
		this.differentiatingCallsitesSetB = differentiatingCallsitesSetB;
	}
	
	public boolean addDifferentiatingCallsitesSetB(AtlasSet<Node> differentiatingCallsitesSetB) {
		return this.differentiatingCallsitesSetB.addAll(differentiatingCallsitesSetB);
	}
	
	public boolean removeDifferentiatingCallsiteSetB(Node differentiatingCallsiteSetB) {
		return this.differentiatingCallsitesSetB.remove(differentiatingCallsiteSetB);
	}
*/
	public AtlasSet<Node> getLoopHeadersSelected() {
		return loopHeadersSelected;
	}

	public void setLoopHeadersSelected(AtlasSet<Node> param) {
		this.loopHeadersSelected = param;
	}
	
	public boolean addLoopHeadersSelected(AtlasSet<Node> param) {
		return this.loopHeadersSelected.addAll(param);
	}
	
	public boolean removeLoopHeadersSelected(Node param) {
		return this.loopHeadersSelected.remove(param);
	}

	@Override
	public int compareTo(SubsystemComponents other) {
		return Long.compare(this.createdAt, other.createdAt);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubsystemComponents other = (SubsystemComponents) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}