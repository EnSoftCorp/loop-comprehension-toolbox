package com.kcsl.loop.catalog.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ensoftcorp.open.java.commons.subsystems.CompressionSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.CryptoSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.DataStructureSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.DatabaseSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.GarbageCollectionSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.HardwareSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.IOSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.IntrospectionSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.JavaCoreSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.LogSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.MathSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.NetworkSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.RMISubsystem;
import com.ensoftcorp.open.java.commons.subsystems.RandomSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.SecuritySubsystem;
import com.ensoftcorp.open.java.commons.subsystems.SerializationSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.TestingSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.ThreadingSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.UISubsystem;
import com.kcsl.loop.catalog.subsystems.AppSubsystem;
import com.kcsl.loop.catalog.subsystems.CollectionSubsystem;
import com.kcsl.loop.catalog.subsystems.FunctionalSubsystem;
import com.kcsl.loop.catalog.subsystems.ImplicitLoopSubsystem;
import com.kcsl.loop.catalog.subsystems.IteratorSubsystem;
import com.kcsl.loop.catalog.subsystems.OtherLibrariesSubsystem;
import com.kcsl.loop.catalog.subsystems.SpliteratorSubsystem;
import com.kcsl.loop.catalog.subsystems.StreamSubsystem;

public class LoopInfo {


	int headerID;
	String projectString;
	String packageName;
	String typeName;
	String methodName;
	String loopStatement;
	String container;
	
	int numTerminatingConditions;
	int nestingDepth;
	boolean monotonicity;
	String pattern;
	String lowerBound;
	String upperBound;
	
	long dfNodes;
	long dfEdges;
	long tgNodes;
	long tgEdges;
	long cfNodes;
	long cfEdges;
	long lcefgNodes;
	long lcefgEdges;
	
	int numCfEntryPointsFromWhichLoopIsReachable;
	int numDfEntryPointsFromWhichLoopIsReachable;
	int numBranchesContainedInLoop;
	int numBranchesGoverningLoop;
	int numLambdaLoopsContainedInLoop;
	List<String> cfEntryPointsFromWhichLoopIsReachable;
	List<String> dfEntryPointsFromWhichLoopIsReachable;
	List<String> branchesContainedInLoop;
	List<String> branchesGoverningLoop;
	List<String> lambdaLoopsContainedInLoop;
	
	int countSS;
	
	boolean javacoreSS;
	boolean hardwareSS;
	boolean ioSS;
	boolean networkSS;
	boolean rmiSS;
	boolean databaseSS;
	boolean logSS;
	boolean serializationSS;
	boolean compressionSS;
	boolean uiSS;
	boolean introspectionSS;
	boolean testingSS;
	boolean gcSS;
	boolean securitySS;
	boolean cryptoSS;
	boolean mathSS;
	boolean randomSS;
	boolean threadingSS;
	boolean datastructureSS;
	boolean collectionSS;
	boolean iteratorSS;
	boolean implicitLoopSS;
	boolean functionalSS;
	boolean spliteratorSS;
	boolean streamSS;
	boolean appSS;
	boolean otherLibSS;
	
	// callsite stats
	long callsitesInTheLoop;
	long callsitesInTheLoopJDK;
	long conditionalCallsitesInTheLoop;
	long conditionalCallsitesInTheLoopJDK;
	
	// path statistics
	long pathCount;
	long pathsWithCallSites;
	long pathsWithJDKCallSites;
	long pathsWithJDKCallSitesOnly;
	long pathsWithoutCallSites;
	long pathsWithoutJDKCallSites;
	long pathsWithNonJDKCallSites;
	
	private Map<String, String> fieldMap = new HashMap<String, String>();
	private Map<String, String> fieldSubsystemTagMap = new HashMap<String, String>();

	/*
	 * Dynamic analysis related info about loops
	 */
	LoopRuntimeInfo loopRuntimeInfo = new LoopRuntimeInfo();

	public LoopInfo() {
		
	}
	
	
	public LoopInfo(int headerID, String projectString, String packageName, String typeName, String methodName,
			String loopStatement, String container, int numTerminatingConditions, int nestingDepth,
			boolean monotonicity, String pattern, String lowerBound, String upperBound, long dfNodes, long dfEdges,
			long tgNodes, long tgEdges, long cfNodes, long cfEdges, long lcefgNodes, long lcefgEdges,  
			int numCfEntryPointsFromWhichLoopIsReachable, List<String> cfEntryPointsFromWhichLoopIsReachable, 
			int numDfEntryPointsFromWhichLoopIsReachable, List<String> dfEntryPointsFromWhichLoopIsReachable, 
			int numBranchesContainedInLoop, List<String> branchesContainedInLoop, int numBranchesGoverningLoop, List<String> branchesGoverningLoop,
			int numLambdaLoopsContainedInLoop, List<String> lambdaLoopsContainedInLoop,
			int countSS, boolean javacoreSS, boolean networkSS, boolean ioSS, boolean logSS, boolean mathSS, boolean randomSS, boolean threadingSS,
			boolean collectionSS, boolean iteratorSS, boolean implicitLoopSS, boolean functionalSS, boolean spliteratorSS, boolean streamSS,
			boolean compressionSS, boolean hardwareSS, boolean rmiSS, boolean databaseSS, boolean serializationSS, boolean uiSS, boolean introspectionSS,
			boolean testingSS, boolean gcSS, boolean securitySS, boolean cryptoSS,  boolean datastructureSS,  boolean otherLibSS, boolean appSS, 
			long callsitesInTheLoop, long callsitesInTheLoopJDK, long conditionalCallsitesInTheLoop, long conditionalCallsitesInTheLoopJDK,
			long pathCount, long pathsWithCallSites, long pathsWithJDKCallSites, long pathsWithJDKCallSitesOnly,
			long pathsWithoutCallSites, long pathsWithoutJDKCallSites, long pathsWithNonJDKCallSites, 
			LoopRuntimeInfo loopRuntimeInfo) {
		super();
		this.headerID = headerID;
		this.projectString = projectString;
		this.packageName = packageName;
		this.typeName = typeName;
		this.methodName = methodName;
		this.loopStatement = loopStatement;
		this.container = container;
		this.numTerminatingConditions = numTerminatingConditions;
		this.nestingDepth = nestingDepth;
		this.monotonicity = monotonicity;
		this.pattern = pattern;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.numCfEntryPointsFromWhichLoopIsReachable = numCfEntryPointsFromWhichLoopIsReachable;
		this.cfEntryPointsFromWhichLoopIsReachable = cfEntryPointsFromWhichLoopIsReachable;
		this.numDfEntryPointsFromWhichLoopIsReachable = numDfEntryPointsFromWhichLoopIsReachable;
		this.dfEntryPointsFromWhichLoopIsReachable = dfEntryPointsFromWhichLoopIsReachable;
		this.numBranchesContainedInLoop = numBranchesContainedInLoop;
		this.branchesContainedInLoop = branchesContainedInLoop;
		this.numBranchesGoverningLoop = numBranchesGoverningLoop;
		this.branchesGoverningLoop = branchesGoverningLoop;
		this.numLambdaLoopsContainedInLoop = numLambdaLoopsContainedInLoop;
		this.lambdaLoopsContainedInLoop = lambdaLoopsContainedInLoop;
		this.dfNodes = dfNodes;
		this.dfEdges = dfEdges;
		this.tgNodes = tgNodes;
		this.tgEdges = tgEdges;
		this.cfNodes = cfNodes;
		this.cfEdges = cfEdges;
		this.lcefgNodes = lcefgNodes;
		this.lcefgEdges = lcefgEdges;
		this.countSS = countSS;
		this.javacoreSS = javacoreSS;
		this.networkSS = networkSS;
		this.ioSS = ioSS;
		this.logSS = logSS;
		this.mathSS = mathSS;
		this.randomSS = randomSS;
		this.threadingSS = threadingSS;
		this.collectionSS = collectionSS;
		this.iteratorSS = iteratorSS;
		this.implicitLoopSS = implicitLoopSS;
		this.functionalSS = functionalSS;
		this.spliteratorSS = spliteratorSS;
		this.streamSS = streamSS;
		this.hardwareSS = hardwareSS;
		this.rmiSS = rmiSS;
		this.databaseSS = databaseSS;
		this.serializationSS = serializationSS;
		this.compressionSS = compressionSS;
		this.uiSS = uiSS;
		this.introspectionSS = introspectionSS;
		this.testingSS = testingSS;
		this.gcSS = gcSS;
		this.securitySS = securitySS;
		this.cryptoSS = cryptoSS;
		this.datastructureSS = datastructureSS;
		this.otherLibSS = otherLibSS;
		this.appSS = appSS;
		
		this.callsitesInTheLoop = callsitesInTheLoop;
		this.callsitesInTheLoopJDK = callsitesInTheLoopJDK;
		this.conditionalCallsitesInTheLoop = conditionalCallsitesInTheLoop;
		this.conditionalCallsitesInTheLoopJDK = conditionalCallsitesInTheLoopJDK;
		this.pathCount = pathCount;
		this.pathsWithCallSites = pathsWithCallSites;
		this.pathsWithJDKCallSites = pathsWithJDKCallSites;
		this.pathsWithJDKCallSitesOnly = pathsWithJDKCallSitesOnly;
		this.pathsWithoutCallSites = pathsWithoutCallSites;
		this.pathsWithoutJDKCallSites = pathsWithoutJDKCallSites;
		this.pathsWithNonJDKCallSites = pathsWithNonJDKCallSites;
		
		this.loopRuntimeInfo = loopRuntimeInfo;
		
		updateFieldMaps();
	}

	public void updateFieldMaps() {
		updateFieldMaps(headerID, projectString, packageName, typeName, methodName, loopStatement, container,
				numTerminatingConditions, nestingDepth, monotonicity, pattern, lowerBound, upperBound, dfNodes, dfEdges,
				tgNodes, tgEdges, cfNodes, cfEdges, lcefgNodes, lcefgEdges, 
				numCfEntryPointsFromWhichLoopIsReachable, cfEntryPointsFromWhichLoopIsReachable, 
				numDfEntryPointsFromWhichLoopIsReachable, dfEntryPointsFromWhichLoopIsReachable, 
				numBranchesContainedInLoop, branchesContainedInLoop,
				numBranchesGoverningLoop, branchesGoverningLoop,
				numLambdaLoopsContainedInLoop, lambdaLoopsContainedInLoop,
				countSS, javacoreSS, networkSS, ioSS, logSS, mathSS, randomSS, threadingSS,
				collectionSS, iteratorSS, implicitLoopSS, functionalSS, spliteratorSS, streamSS, 
				compressionSS, hardwareSS, rmiSS, databaseSS, serializationSS, uiSS, introspectionSS, 
				testingSS, gcSS, securitySS, cryptoSS, datastructureSS, otherLibSS, appSS, 
				callsitesInTheLoop, callsitesInTheLoopJDK, conditionalCallsitesInTheLoop,
				conditionalCallsitesInTheLoopJDK, pathCount, pathsWithCallSites, pathsWithJDKCallSites,
				pathsWithJDKCallSitesOnly, pathsWithoutCallSites, pathsWithoutJDKCallSites, pathsWithNonJDKCallSites,
				loopRuntimeInfo);
	}
	
	private void updateFieldMaps(int headerID, String projectString, String packageName, String typeName,
			String methodName, String loopStatement, String container, int numTerminatingConditions, int nestingDepth,
			boolean monotonicity, String pattern, String lowerBound, String upperBound, long dfNodes, long dfEdges,
			long tgNodes, long tgEdges, long cfNodes, long cfEdges, long lcefgNodes, long lcefgEdges,
			int numCfEntryPointsFromWhichLoopIsReachable, List<String> cfEntryPointsFromWhichLoopIsReachable,
			int numDfEntryPointsFromWhichLoopIsReachable, List<String> dfEntryPointsFromWhichLoopIsReachable,
			int numBranchesContainedInLoop, List<String> branchesContainedInLoop, int numBranchesGoverningLoop,
			List<String> branchesGoverningLoop, int numLambdaLoopsContainedInLoop, List<String> lambdaLoopsContainedInLoop, 
			int countSS, boolean javacoreSS, boolean networkSS, boolean ioSS, boolean logSS, boolean mathSS, boolean randomSS, boolean threadingSS,
			boolean collectionSS, boolean iteratorSS, boolean implicitLoopSS, boolean functionalSS, boolean spliteratorSS, boolean streamSS,
			boolean compressionSS, boolean hardwareSS, boolean rmiSS, boolean databaseSS, boolean serializationSS, boolean uiSS, boolean introspectionSS,
			boolean testingSS, boolean gcSS, boolean securitySS, boolean cryptoSS, boolean datastructureSS,  boolean otherLibSS, boolean appSS,
			long callsitesInTheLoop, long callsitesInTheLoopJDK, long conditionalCallsitesInTheLoop,
			long conditionalCallsitesInTheLoopJDK, long pathCount, long pathsWithCallSites, long pathsWithJDKCallSites,
			long pathsWithJDKCallSitesOnly, long pathsWithoutCallSites, long pathsWithoutJDKCallSites,
			long pathsWithNonJDKCallSites, LoopRuntimeInfo runtimeInfo) {
		fieldMap = new HashMap<String, String>();
		fieldMap.put("headerID", headerID+"");
		fieldMap.put("projectString", projectString+"");
		fieldMap.put("packageName", packageName+"");
		fieldMap.put("typeName", typeName+"");
		fieldMap.put("methodName", methodName+"");
		fieldMap.put("loopStatement", loopStatement+"");
		fieldMap.put("container", container+"");
		fieldMap.put("numTerminatingConditions", numTerminatingConditions+"");
		fieldMap.put("nestingDepth", nestingDepth+"");
		fieldMap.put("monotonicity", monotonicity+"");
		fieldMap.put("pattern", pattern+"");
		fieldMap.put("lowerBound", lowerBound+"");
		fieldMap.put("upperBound", upperBound+"");
		fieldMap.put("numCfEntryPointsFromWhichLoopIsReachable", numCfEntryPointsFromWhichLoopIsReachable+"");
		fieldMap.put("cfEntryPointsFromWhichLoopIsReachable", cfEntryPointsFromWhichLoopIsReachable+"");
		fieldMap.put("numDfEntryPointsFromWhichLoopIsReachable", numDfEntryPointsFromWhichLoopIsReachable+"");
		fieldMap.put("dfEntryPointsFromWhichLoopIsReachable", dfEntryPointsFromWhichLoopIsReachable+"");
		fieldMap.put("numBranchesContainedInLoop", numBranchesContainedInLoop+"");
		fieldMap.put("branchesContainedInLoop", branchesContainedInLoop+"");
		fieldMap.put("numBranchesGoverningLoop", numBranchesGoverningLoop+"");
		fieldMap.put("branchesGoverningLoop", branchesGoverningLoop+"");
		fieldMap.put("numLambdaLoopsContainedInLoop", numLambdaLoopsContainedInLoop+"");
		fieldMap.put("lambdaLoopsContainedInLoop", lambdaLoopsContainedInLoop+"");
		fieldMap.put("dfNodes", dfNodes+"");
		fieldMap.put("dfEdges", dfEdges+"");
		fieldMap.put("tgNodes", tgNodes+"");
		fieldMap.put("tgEdges", tgEdges+"");
		fieldMap.put("cfNodes", cfNodes+"");
		fieldMap.put("cfEdges", cfEdges+"");
		fieldMap.put("lcefgNodes", lcefgNodes+"");
		fieldMap.put("lcefgEdges", lcefgEdges+"");
		fieldMap.put("countSS", countSS+"");
		fieldMap.put("javacoreSS", javacoreSS+"");
		fieldMap.put("networkSS", networkSS+"");
		fieldMap.put("ioSS", ioSS+"");
		fieldMap.put("logSS", logSS+"");
		fieldMap.put("mathSS", mathSS+"");
		fieldMap.put("randomSS", randomSS+"");
		fieldMap.put("threadingSS", threadingSS+"");
		fieldMap.put("collectionSS", collectionSS+"");
		fieldMap.put("iteratorSS", iteratorSS+"");
		fieldMap.put("implicitLoopSS", implicitLoopSS+"");
		fieldMap.put("functionalSS", functionalSS+"");
		fieldMap.put("spliteratorSS", spliteratorSS+"");
		fieldMap.put("streamSS", streamSS+"");
		fieldMap.put("compressionSS", compressionSS+"");
		fieldMap.put("hardwareSS", hardwareSS+"");
		fieldMap.put("rmiSS", rmiSS+"");
		fieldMap.put("databaseSS", databaseSS+"");
		fieldMap.put("serializationSS", serializationSS+"");
		fieldMap.put("uiSS", uiSS+"");
		fieldMap.put("introspectionSS", introspectionSS+"");
		fieldMap.put("testingSS", testingSS+"");
		fieldMap.put("gcSS", gcSS+"");
		fieldMap.put("securitySS", securitySS+"");
		fieldMap.put("cryptoSS", cryptoSS+"");
		fieldMap.put("datastructureSS", datastructureSS+"");
		fieldMap.put("otherLibSS", otherLibSS+"");
		fieldMap.put("appSS", appSS+"");
		
		fieldMap.put("callsitesInTheLoop", callsitesInTheLoop+"");
		fieldMap.put("callsitesInTheLoopJDK", callsitesInTheLoopJDK+"");
		fieldMap.put("conditionalCallsitesInTheLoop", conditionalCallsitesInTheLoop+"");
		fieldMap.put("conditionalCallsitesInTheLoopJDK", conditionalCallsitesInTheLoopJDK+"");
		fieldMap.put("pathCount", pathCount+"");
		fieldMap.put("pathsWithCallSites", pathsWithCallSites+"");
		fieldMap.put("pathsWithJDKCallSites", pathsWithJDKCallSites+"");
		fieldMap.put("pathsWithJDKCallSitesOnly", pathsWithJDKCallSitesOnly+"");
		fieldMap.put("pathsWithoutCallSites", pathsWithoutCallSites+"");
		fieldMap.put("pathsWithoutJDKCallSites", pathsWithoutJDKCallSites+"");
		fieldMap.put("pathsWithNonJDKCallSites", pathsWithNonJDKCallSites+"");
		fieldMap.put("runtimeIterationCount", runtimeInfo.getIterationCount()+"");
		fieldMap.put("avgTimeForLoopExecution", runtimeInfo.getAvgTimeForLoopExecution()+"");
		fieldMap.put("avgTimePerIteration", runtimeInfo.getAvgTimePerIteration()+"");
		
		fieldSubsystemTagMap = new HashMap<String, String>();
		fieldSubsystemTagMap.put("javacoreSS", JavaCoreSubsystem.TAG);
		fieldSubsystemTagMap.put("networkSS", NetworkSubsystem.TAG);
		fieldSubsystemTagMap.put("ioSS", IOSubsystem.TAG);
		fieldSubsystemTagMap.put("logSS", LogSubsystem.TAG);
		fieldSubsystemTagMap.put("mathSS", MathSubsystem.TAG);
		fieldSubsystemTagMap.put("randomSS", RandomSubsystem.TAG);
		fieldSubsystemTagMap.put("threadingSS", ThreadingSubsystem.TAG);
		fieldSubsystemTagMap.put("collectionSS", CollectionSubsystem.TAG);
		fieldSubsystemTagMap.put("iteratorSS", IteratorSubsystem.TAG);
		fieldSubsystemTagMap.put("implicitLoopSS", ImplicitLoopSubsystem.TAG);
		fieldSubsystemTagMap.put("functionalSS", FunctionalSubsystem.TAG);
		fieldSubsystemTagMap.put("spliteratorSS", SpliteratorSubsystem.TAG);
		fieldSubsystemTagMap.put("streamSS", StreamSubsystem.TAG);
		fieldSubsystemTagMap.put("compressionSS", CompressionSubsystem.TAG);
		fieldSubsystemTagMap.put("hardwareSS", HardwareSubsystem.TAG);		
		fieldSubsystemTagMap.put("rmiSS", RMISubsystem.TAG);
		fieldSubsystemTagMap.put("databaseSS", DatabaseSubsystem.TAG);
		fieldSubsystemTagMap.put("serializationSS", SerializationSubsystem.TAG);
		fieldSubsystemTagMap.put("uiSS", UISubsystem.TAG);
		fieldSubsystemTagMap.put("introspectionSS", IntrospectionSubsystem.TAG);
		fieldSubsystemTagMap.put("testingSS", TestingSubsystem.TAG);
		fieldSubsystemTagMap.put("gcSS", GarbageCollectionSubsystem.TAG);
		fieldSubsystemTagMap.put("securitySS", SecuritySubsystem.TAG);
		fieldSubsystemTagMap.put("cryptoSS", CryptoSubsystem.TAG);
		fieldSubsystemTagMap.put("datastructureSS", DataStructureSubsystem.TAG);
		fieldSubsystemTagMap.put("otherLibSS", OtherLibrariesSubsystem.TAG);
		fieldSubsystemTagMap.put("appSS", AppSubsystem.TAG);
	}
	
	public long getHeaderID() {
		return headerID;
	}

	public void setHeaderID(int headerID) {
		this.headerID = headerID;
	}

	public String getProjectString() {
		return projectString;
	}

	public void setProjectString(String projectString) {
		this.projectString = projectString;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getLoopStatement() {
		return loopStatement;
	}

	public void setLoopStatement(String loopStatement) {
		this.loopStatement = loopStatement;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public int getNumTerminatingConditions() {
		return numTerminatingConditions;
	}

	public void setNumTerminatingConditions(int numTerminatingConditions) {
		this.numTerminatingConditions = numTerminatingConditions;
	}

	public int getNestingDepth() {
		return nestingDepth;
	}

	public void setNestingDepth(int nestingDepth) {
		this.nestingDepth = nestingDepth;
	}

	public boolean isMonotonicity() {
		return monotonicity;
	}

	public void setMonotonicity(boolean monotonicity) {
		this.monotonicity = monotonicity;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(String lowerBound) {
		this.lowerBound = lowerBound;
	}

	public String getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(String upperBound) {
		this.upperBound = upperBound;
	}

	public long getDfNodes() {
		return dfNodes;
	}

	public void setDfNodes(long dfNodes) {
		this.dfNodes = dfNodes;
	}

	public long getDfEdges() {
		return dfEdges;
	}

	public void setDfEdges(long dfEdges) {
		this.dfEdges = dfEdges;
	}

	public long getTgNodes() {
		return tgNodes;
	}

	public void setTgNodes(long tgNodes) {
		this.tgNodes = tgNodes;
	}

	public long getTgEdges() {
		return tgEdges;
	}

	public void setTgEdges(long tgEdges) {
		this.tgEdges = tgEdges;
	}

	public long getCfNodes() {
		return cfNodes;
	}

	public void setCfNodes(long cfNodes) {
		this.cfNodes = cfNodes;
	}

	public long getCfEdges() {
		return cfEdges;
	}

	public void setCfEdges(long cfEdges) {
		this.cfEdges = cfEdges;
	}

	public long getLcefgNodes() {
		return lcefgNodes;
	}

	public void setLcefgNodes(long lcefgNodes) {
		this.lcefgNodes = lcefgNodes;
	}

	public long getLcefgEdges() {
		return lcefgEdges;
	}

	public void setLcefgEdges(long lcefgEdges) {
		this.lcefgEdges = lcefgEdges;
	}

	public int getCountSS() {
		return countSS;
	}

	public void setCountSS(int countSS) {
		this.countSS = countSS;
	}

	public boolean isJavacoreSS() {
		return javacoreSS;
	}

	public void setJavacoreSS(boolean javacoreSS) {
		this.javacoreSS = javacoreSS;
	}

	public boolean isHardwareSS() {
		return hardwareSS;
	}

	public void setHardwareSS(boolean hardwareSS) {
		this.hardwareSS = hardwareSS;
	}

	public boolean isIoSS() {
		return ioSS;
	}

	public void setIoSS(boolean ioSS) {
		this.ioSS = ioSS;
	}

	public boolean isNetworkSS() {
		return networkSS;
	}

	public void setNetworkSS(boolean networkSS) {
		this.networkSS = networkSS;
	}

	public boolean isRmiSS() {
		return rmiSS;
	}

	public void setRmiSS(boolean rmiSS) {
		this.rmiSS = rmiSS;
	}

	public boolean isDatabaseSS() {
		return databaseSS;
	}

	public void setDatabaseSS(boolean databaseSS) {
		this.databaseSS = databaseSS;
	}

	public boolean isLogSS() {
		return logSS;
	}

	public void setLogSS(boolean logSS) {
		this.logSS = logSS;
	}

	public boolean isSerializationSS() {
		return serializationSS;
	}

	public void setSerializationSS(boolean serializationSS) {
		this.serializationSS = serializationSS;
	}

	public boolean isCompressionSS() {
		return compressionSS;
	}

	public void setCompressionSS(boolean compressionSS) {
		this.compressionSS = compressionSS;
	}

	public boolean isUiSS() {
		return uiSS;
	}

	public void setUiSS(boolean uiSS) {
		this.uiSS = uiSS;
	}

	public boolean isIntrospectionSS() {
		return introspectionSS;
	}

	public void setIntrospectionSS(boolean introspectionSS) {
		this.introspectionSS = introspectionSS;
	}

	public boolean isTestingSS() {
		return testingSS;
	}

	public void setTestingSS(boolean testingSS) {
		this.testingSS = testingSS;
	}

	public boolean isGcSS() {
		return gcSS;
	}

	public void setGcSS(boolean gcSS) {
		this.gcSS = gcSS;
	}

	public boolean isSecuritySS() {
		return securitySS;
	}

	public void setSecuritySS(boolean securitySS) {
		this.securitySS = securitySS;
	}

	public boolean isCryptoSS() {
		return cryptoSS;
	}

	public void setCryptoSS(boolean cryptoSS) {
		this.cryptoSS = cryptoSS;
	}

	public boolean isMathSS() {
		return mathSS;
	}

	public void setMathSS(boolean mathSS) {
		this.mathSS = mathSS;
	}

	public boolean isRandomSS() {
		return randomSS;
	}

	public void setRandomSS(boolean randomSS) {
		this.randomSS = randomSS;
	}

	public boolean isThreadingSS() {
		return threadingSS;
	}

	public void setThreadingSS(boolean threadingSS) {
		this.threadingSS = threadingSS;
	}

	public boolean isDatastructureSS() {
		return datastructureSS;
	}

	public void setDatastructureSS(boolean datastructureSS) {
		this.datastructureSS = datastructureSS;
	}

	public boolean isCollectionSS() {
		return collectionSS;
	}

	public void setCollectionSS(boolean collectionSS) {
		this.collectionSS = collectionSS;
	}

	public long getCallsitesInTheLoop() {
		return callsitesInTheLoop;
	}

	public void setCallsitesInTheLoop(long callsitesInTheLoop) {
		this.callsitesInTheLoop = callsitesInTheLoop;
	}

	public long getCallsitesInTheLoopJDK() {
		return callsitesInTheLoopJDK;
	}

	public void setCallsitesInTheLoopJDK(long callsitesInTheLoopJDK) {
		this.callsitesInTheLoopJDK = callsitesInTheLoopJDK;
	}

	public long getConditionalCallsitesInTheLoop() {
		return conditionalCallsitesInTheLoop;
	}

	public void setConditionalCallsitesInTheLoop(long conditionalCallsitesInTheLoop) {
		this.conditionalCallsitesInTheLoop = conditionalCallsitesInTheLoop;
	}

	public long getConditionalCallsitesInTheLoopJDK() {
		return conditionalCallsitesInTheLoopJDK;
	}

	public void setConditionalCallsitesInTheLoopJDK(long conditionalCallsitesInTheLoopJDK) {
		this.conditionalCallsitesInTheLoopJDK = conditionalCallsitesInTheLoopJDK;
	}

	public long getPathCount() {
		return pathCount;
	}

	public void setPathCount(long pathCount) {
		this.pathCount = pathCount;
	}

	public long getPathsWithCallSites() {
		return pathsWithCallSites;
	}

	public void setPathsWithCallSites(long pathsWithCallSites) {
		this.pathsWithCallSites = pathsWithCallSites;
	}

	public long getPathsWithJDKCallSites() {
		return pathsWithJDKCallSites;
	}

	public void setPathsWithJDKCallSites(long pathsWithJDKCallSites) {
		this.pathsWithJDKCallSites = pathsWithJDKCallSites;
	}

	public long getPathsWithJDKCallSitesOnly() {
		return pathsWithJDKCallSitesOnly;
	}

	public void setPathsWithJDKCallSitesOnly(long pathsWithJDKCallSitesOnly) {
		this.pathsWithJDKCallSitesOnly = pathsWithJDKCallSitesOnly;
	}

	public long getPathsWithoutCallSites() {
		return pathsWithoutCallSites;
	}

	public void setPathsWithoutCallSites(long pathsWithoutCallSites) {
		this.pathsWithoutCallSites = pathsWithoutCallSites;
	}

	public long getPathsWithoutJDKCallSites() {
		return pathsWithoutJDKCallSites;
	}

	public void setPathsWithoutJDKCallSites(long pathsWithoutJDKCallSites) {
		this.pathsWithoutJDKCallSites = pathsWithoutJDKCallSites;
	}

	public long getPathsWithNonJDKCallSites() {
		return pathsWithNonJDKCallSites;
	}

	public void setPathsWithNonJDKCallSites(long pathsWithNonJDKCallSites) {
		this.pathsWithNonJDKCallSites = pathsWithNonJDKCallSites;
	}
	
	public String getField(String id) {
		return fieldMap.get(id);
	}
	
	public String getSubsystemTagForField(String id) {
		return fieldSubsystemTagMap.get(id);
	}
	
	public int getNumBranchesContainedInLoop() {
		return numBranchesContainedInLoop;
	}

	public void setNumBranchesContainedInLoop(int numBranchesContainedInLoop) {
		this.numBranchesContainedInLoop = numBranchesContainedInLoop;
	}

	public int getNumBranchesGoverningLoop() {
		return numBranchesGoverningLoop;
	}

	public void setNumBranchesGoverningLoop(int numBranchesGoverningLoop) {
		this.numBranchesGoverningLoop = numBranchesGoverningLoop;
	}

	public List<String> getBranchesContainedInLoop() {
		if(branchesContainedInLoop == null) {
			return new ArrayList<String>();
		}
		return branchesContainedInLoop;
	}

	public void setBranchesContainedInLoop(List<String> branchesContainedInLoop) {
		this.branchesContainedInLoop = branchesContainedInLoop;
	}

	public List<String> getBranchesGoverningLoop() {
		if(branchesGoverningLoop == null) {
			return new ArrayList<String>();
		}
		return branchesGoverningLoop;
	}

	public void setBranchesGoverningLoop(List<String> branchesGoverningLoop) {
		this.branchesGoverningLoop = branchesGoverningLoop;
	}
	
	public int getNumLambdaLoopsContainedInLoop() {
		return numLambdaLoopsContainedInLoop;
	}

	public void setNumLambdaLoopsContainedInLoop(int numLambdaLoopsContainedInLoop) {
		this.numLambdaLoopsContainedInLoop = numLambdaLoopsContainedInLoop;
	}
	
	public List<String> getLambdaLoopsContainedInLoop() {
		if(lambdaLoopsContainedInLoop == null) {
			return new ArrayList<String>();
		}
		return lambdaLoopsContainedInLoop;
	}
	
	public void setLambdaLoopsContainedInLoop(List<String> lambdaLoopsContainedInLoop) {
		this.lambdaLoopsContainedInLoop = lambdaLoopsContainedInLoop;
	}
	
	public int getNumCfEntryPointsFromWhichLoopIsReachable() {
		return numCfEntryPointsFromWhichLoopIsReachable;
	}

	public void setNumCfEntryPointsFromWhichLoopIsReachable(int numCfEntryPointsFromWhichLoopIsReachable) {
		this.numCfEntryPointsFromWhichLoopIsReachable = numCfEntryPointsFromWhichLoopIsReachable;
	}

	public List<String> getCfEntryPointsFromWhichLoopIsReachable() {
		if(cfEntryPointsFromWhichLoopIsReachable == null) {
			return new ArrayList<String>();
		}
		return cfEntryPointsFromWhichLoopIsReachable;
	}

	public void setCfEntryPointsFromWhichLoopIsReachable(List<String> cfEntryPointsFromWhichLoopIsReachable) {
		this.cfEntryPointsFromWhichLoopIsReachable = cfEntryPointsFromWhichLoopIsReachable;
	}

	public int getNumDfEntryPointsFromWhichLoopIsReachable() {
		return numDfEntryPointsFromWhichLoopIsReachable;
	}

	public void setNumDfEntryPointsFromWhichLoopIsReachable(int numDfEntryPointsFromWhichLoopIsReachable) {
		this.numDfEntryPointsFromWhichLoopIsReachable = numDfEntryPointsFromWhichLoopIsReachable;
	}

	public List<String> getDfEntryPointsFromWhichLoopIsReachable() {
		return dfEntryPointsFromWhichLoopIsReachable;
	}

	public void setDfEntryPointsFromWhichLoopIsReachable(List<String> dfEntryPointsFromWhichLoopIsReachable) {
		this.dfEntryPointsFromWhichLoopIsReachable = dfEntryPointsFromWhichLoopIsReachable;
	}

	public boolean isIteratorSS() {
		return iteratorSS;
	}

	public void setIteratorSS(boolean iteratorSS) {
		this.iteratorSS = iteratorSS;
	}
	
	public boolean isImplicitLoopSS() {
		return implicitLoopSS;
	}
	
	public void setImplicitLoopSS(boolean implicitLoopSS) {
		this.implicitLoopSS = implicitLoopSS;
	}
	
	public boolean isFunctionalSS() {
		return functionalSS;
	}
	
	public void setFunctionalSS(boolean functionalSS) {
		this.functionalSS = functionalSS;
	}
	
	public boolean isSpliteratorSS() {
		return spliteratorSS;
	}
	
	public void setSpliteratorSS(boolean spliteratorSS) {
		this.spliteratorSS = spliteratorSS;
	}
	
	public boolean isStreamSS() {
		return streamSS;
	}
	
	public void setStreamSS(boolean streamSS) {
		this.streamSS = streamSS;
	}

	public boolean isAppSS() {
		return appSS;
	}

	public void setAppSS(boolean appSS) {
		this.appSS = appSS;
	}

	public boolean isOtherLibSS() {
		return otherLibSS;
	}

	public void setOtherLibSS(boolean otherLibSS) {
		this.otherLibSS = otherLibSS;
	}
	
	public LoopRuntimeInfo getLoopRuntimeInfo() {
		return loopRuntimeInfo;
	}

	public void setLoopRuntimeInfo(LoopRuntimeInfo loopRuntimeInfo) {
		this.loopRuntimeInfo = loopRuntimeInfo;
	}
	
}
