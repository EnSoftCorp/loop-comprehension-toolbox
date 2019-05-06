package com.kcsl.loop.catalog.lambda;

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
import com.kcsl.loop.catalog.subsystems.ImplicitLoopSubsystem;
import com.kcsl.loop.catalog.subsystems.IteratorSubsystem;
import com.kcsl.loop.catalog.subsystems.OtherLibrariesSubsystem;

public class LambdaLoopInfo {
	
	String lambdaLoopID;
	String projectString;
	String packageName;
	String typeName;
	String methodName;
	String lambdaLoopStatement;
	String implicitLoopAPI;
	
	int numCfEntryPointsFromWhichLambdaLoopIsReachable;
	int numDfEntryPointsFromWhichLambdaLoopIsReachable;
	List<String> cfEntryPointsFromWhichLambdaLoopIsReachable;
	List<String> dfEntryPointsFromWhichLambdaLoopIsReachable;
	
	int numLoopsContainingLambdaLoop;
	int numBranchesGoverningLambdaLoop;
	List<String> loopsContainingLambdaLoop;
	List<String> branchesGoverningLambdaLoop;
	
	private Map<String, String> fieldMap = new HashMap<String, String>();
	private Map<String, String> fieldSubsystemTagMap = new HashMap<String, String>();
	
	public LambdaLoopInfo() {
		// intentionally left blank
	}
	
	public LambdaLoopInfo (String lambdaLoopID, String projectString, String packageName, String typeName, String methodName, String loopStatement,
			String implicitLoopAPI,
			int numCfEntryPointsFromWhichLambdaLoopIsReachable, List<String> cfEntryPointsFromWhichLambdaLoopIsReachable, 
			int numDfEntryPointsFromWhichLambdaLoopIsReachable, List<String> dfEntryPointsFromWhichLambdaLoopIsReachable,
			int numLoopsContainingLambdaLoop, List<String> loopsContainingLambdaLoop, int numBranchesGoverningLambdaLoop, List<String> branchesGoverningLambdaLoop) {
		this.lambdaLoopID = lambdaLoopID;
		this.projectString = projectString;
		this.packageName = packageName;
		this.typeName = typeName;
		this.methodName = methodName;
		this.lambdaLoopStatement = loopStatement;
		this.implicitLoopAPI = implicitLoopAPI;
		
		this.numCfEntryPointsFromWhichLambdaLoopIsReachable = numCfEntryPointsFromWhichLambdaLoopIsReachable;
		this.cfEntryPointsFromWhichLambdaLoopIsReachable = cfEntryPointsFromWhichLambdaLoopIsReachable;
		this.numDfEntryPointsFromWhichLambdaLoopIsReachable = numDfEntryPointsFromWhichLambdaLoopIsReachable;
		this.dfEntryPointsFromWhichLambdaLoopIsReachable = dfEntryPointsFromWhichLambdaLoopIsReachable;
		
		this.numLoopsContainingLambdaLoop = numLoopsContainingLambdaLoop;
		this.loopsContainingLambdaLoop = loopsContainingLambdaLoop;
		this.numBranchesGoverningLambdaLoop = numBranchesGoverningLambdaLoop;
		this.branchesGoverningLambdaLoop = branchesGoverningLambdaLoop;
		
		updateFieldMaps();
	}
	
	public void updateFieldMaps() {
		updateFieldMaps(lambdaLoopID, projectString, packageName, typeName, methodName, lambdaLoopStatement, implicitLoopAPI,
				numCfEntryPointsFromWhichLambdaLoopIsReachable, cfEntryPointsFromWhichLambdaLoopIsReachable, 
				numDfEntryPointsFromWhichLambdaLoopIsReachable, dfEntryPointsFromWhichLambdaLoopIsReachable,
				numLoopsContainingLambdaLoop, loopsContainingLambdaLoop, numBranchesGoverningLambdaLoop, branchesGoverningLambdaLoop);
	}
	
	private void updateFieldMaps(String lambdaLoopID, String projectString, String packageName, String typeName, String methodName, String loopStatement,
			String implicitLoopAPI,
			int numCfEntryPointsFromWhichLambdaLoopIsReachable, List<String> cfEntryPointsFromWhichLambdaLoopIsReachable,
			int numDfEntryPointsFromWhichLambdaLoopIsReachable, List<String> dfEntryPointsFromWhichLambdaLoopIsReachable,
			int numLoopsContainingLambdaLoop, List<String> loopsContainingLambdaLoop, int numBranchesGoverningLambdaLoop, List<String> branchesGoverningLambdaLoop) {
		fieldMap = new HashMap<String, String>();
		fieldMap.put("lambdaLoopID", lambdaLoopID);
		fieldMap.put("projectString", projectString);
		fieldMap.put("packageName", packageName);
		fieldMap.put("typeName", typeName);
		fieldMap.put("methodName", methodName);
		fieldMap.put("loopStatement", loopStatement);
		fieldMap.put("implicitLoopAPI", implicitLoopAPI);
		fieldMap.put("numCfEntryPointsFromWhichLambdaLoopIsReachable", numCfEntryPointsFromWhichLambdaLoopIsReachable + "");
		fieldMap.put("cfEntryPointsFromWhichLambdaLoopIsReachable", cfEntryPointsFromWhichLambdaLoopIsReachable + "");
		fieldMap.put("numDfEntryPointsFromWhichLambdaLoopIsReachable", numDfEntryPointsFromWhichLambdaLoopIsReachable + "");
		fieldMap.put("dfEntryPointsFromWhichLambdaLoopIsReachable", dfEntryPointsFromWhichLambdaLoopIsReachable + "");
		fieldMap.put("numLoopsContainingLambdaLoop", numLoopsContainingLambdaLoop + "");
		fieldMap.put("loopsContainingLambdaLoop", loopsContainingLambdaLoop + "");
		fieldMap.put("numBranchesGoverningLambdaLoop", numBranchesGoverningLambdaLoop + "");
		fieldMap.put("branchesGoverningLambdaLoop", branchesGoverningLambdaLoop + "");
		
		fieldSubsystemTagMap = new HashMap<String, String>();
		fieldSubsystemTagMap.put("javacoreSS", JavaCoreSubsystem.TAG);
		fieldSubsystemTagMap.put("hardwareSS", HardwareSubsystem.TAG);
		fieldSubsystemTagMap.put("ioSS", IOSubsystem.TAG);
		fieldSubsystemTagMap.put("networkSS", NetworkSubsystem.TAG);
		fieldSubsystemTagMap.put("rmiSS", RMISubsystem.TAG);
		fieldSubsystemTagMap.put("databaseSS", DatabaseSubsystem.TAG);
		fieldSubsystemTagMap.put("logSS", LogSubsystem.TAG);
		fieldSubsystemTagMap.put("serializationSS", SerializationSubsystem.TAG);
		fieldSubsystemTagMap.put("compressionSS", CompressionSubsystem.TAG);
		fieldSubsystemTagMap.put("uiSS", UISubsystem.TAG);
		fieldSubsystemTagMap.put("introspectionSS", IntrospectionSubsystem.TAG);
		fieldSubsystemTagMap.put("testingSS", TestingSubsystem.TAG);
		fieldSubsystemTagMap.put("gcSS", GarbageCollectionSubsystem.TAG);
		fieldSubsystemTagMap.put("securitySS", SecuritySubsystem.TAG);
		fieldSubsystemTagMap.put("cryptoSS", CryptoSubsystem.TAG);
		fieldSubsystemTagMap.put("mathSS", MathSubsystem.TAG);
		fieldSubsystemTagMap.put("randomSS", RandomSubsystem.TAG);
		fieldSubsystemTagMap.put("threadingSS", ThreadingSubsystem.TAG);
		fieldSubsystemTagMap.put("datastructureSS", DataStructureSubsystem.TAG);
		fieldSubsystemTagMap.put("collectionSS", CollectionSubsystem.TAG);
		fieldSubsystemTagMap.put("iteratorSS", IteratorSubsystem.TAG);
		fieldSubsystemTagMap.put("implicitLoopSS", ImplicitLoopSubsystem.TAG);
		fieldSubsystemTagMap.put("otherLibSS", OtherLibrariesSubsystem.TAG);
		fieldSubsystemTagMap.put("appSS", AppSubsystem.TAG);
	}
	
	public String getLambdaLoopID() {
		return lambdaLoopID;
	}
	
	public String getProjectString() {
		return projectString;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getTypeName() {
		return typeName;
	}
	

	public String getMethodName() {
		return methodName;
	}
	
	public String getLoopStatement() {
		return lambdaLoopStatement;
	}
	
	public String getField(String id) {
		return fieldMap.get(id);
	}
	
	public String getSubsystemTagForField(String id) {
		return fieldSubsystemTagMap.get(id);
	}
	
	public String getImplicitLoopAPI() {
		return implicitLoopAPI;
	}
	
	public int getNumCfEntryPointsFromWhichLambdaLoopIsReachable() {
		return numCfEntryPointsFromWhichLambdaLoopIsReachable;
	}

	public List<String> getCfEntryPointsFromWhichLambdaLoopIsReachable() {
		if(cfEntryPointsFromWhichLambdaLoopIsReachable == null) {
			return new ArrayList<String>();
		}
		return cfEntryPointsFromWhichLambdaLoopIsReachable;
	}

	public int getNumDfEntryPointsFromWhichLambdaLoopIsReachable() {
		return numDfEntryPointsFromWhichLambdaLoopIsReachable;
	}

	public List<String> getDfEntryPointsFromWhichLambdaLoopIsReachable() {
		if(dfEntryPointsFromWhichLambdaLoopIsReachable == null) {
			return new ArrayList<String>();
		}
		return dfEntryPointsFromWhichLambdaLoopIsReachable;
	}
	
	public int getNumLoopsContainingLambdaLoop() {
		return numLoopsContainingLambdaLoop;
	}
	
	public List<String> getLoopsContainingLambdaLoop() {
		if(loopsContainingLambdaLoop == null) {
			return new ArrayList<String>();
		}
		return loopsContainingLambdaLoop;
	}
	
	public int getNumBranchesGoverningLambdaLoop() {
		return numBranchesGoverningLambdaLoop;
	}
	
	public List<String> getBranchesGoverningLambdaLoop() {
		if(branchesGoverningLambdaLoop == null) {
			return new ArrayList<String>();
		}
		return branchesGoverningLambdaLoop;
	}

}
