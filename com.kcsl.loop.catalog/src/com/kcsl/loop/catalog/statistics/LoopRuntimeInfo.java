package com.kcsl.loop.catalog.statistics;

public class LoopRuntimeInfo {

	private long iterationCount;
	private long avgTimePerIteration;
	private long avgTimeForLoopExecution;
	
	public LoopRuntimeInfo() {
		
	}
	
	public LoopRuntimeInfo(long iterationCount, long avgTimePerIteration, long avgTimeForLoopExecution) {
		super();
		this.iterationCount = iterationCount;
		this.avgTimePerIteration = avgTimePerIteration;
		this.avgTimeForLoopExecution = avgTimeForLoopExecution;
	}
	
	public long getIterationCount() {
		return iterationCount;
	}
	public void setIterationCount(long iterationCount) {
		this.iterationCount = iterationCount;
	}
	public long getAvgTimePerIteration() {
		return avgTimePerIteration;
	}
	public void setAvgTimePerIteration(long avgTimePerIteration) {
		this.avgTimePerIteration = avgTimePerIteration;
	}
	public long getAvgTimeForLoopExecution() {
		return avgTimeForLoopExecution;
	}
	public void setAvgTimeForLoopExecution(long avgTimeForLoopExecution) {
		this.avgTimeForLoopExecution = avgTimeForLoopExecution;
	}
}
