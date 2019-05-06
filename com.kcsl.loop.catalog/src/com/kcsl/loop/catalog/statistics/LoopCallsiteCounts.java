package com.kcsl.loop.catalog.statistics;

public class LoopCallsiteCounts {
	
	long callsitesInLoop;
	long callsitesInLoopJDK;
	long conditionalCallsitesInLoop;
	long conditionalCallsitesInLoopJDK;
	
	public long getCallsitesInLoop() {
		return callsitesInLoop;
	}
	public void setCallsitesInLoop(long callsitesInLoop) {
		this.callsitesInLoop = callsitesInLoop;
	}
	public long getCallsitesInLoopJDK() {
		return callsitesInLoopJDK;
	}
	public void setCallsitesInLoopJDK(long callsitesInLoopJDK) {
		this.callsitesInLoopJDK = callsitesInLoopJDK;
	}
	public long getConditionalCallsitesInLoop() {
		return conditionalCallsitesInLoop;
	}
	public void setConditionalCallsitesInLoop(long conditionalCallsitesInLoop) {
		this.conditionalCallsitesInLoop = conditionalCallsitesInLoop;
	}
	public long getConditionalCallsitesInLoopJDK() {
		return conditionalCallsitesInLoopJDK;
	}
	public void setConditionalCallsitesInLoopJDK(long conditionalCallsitesInLoopJDK) {
		this.conditionalCallsitesInLoopJDK = conditionalCallsitesInLoopJDK;
	}
}