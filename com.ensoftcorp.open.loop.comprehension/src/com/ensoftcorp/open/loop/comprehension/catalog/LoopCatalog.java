package com.ensoftcorp.open.loop.comprehension.catalog;

import static com.ensoftcorp.atlas.core.script.Common.toGraph;
import static com.ensoftcorp.atlas.core.script.Common.toQ;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.highlight.Highlighter.ConflictStrategy;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;
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
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.analysis.LoopAbstractions;
import com.ensoftcorp.open.loop.comprehension.analysis.Monotonicity;
import com.ensoftcorp.open.loop.comprehension.log.Log;
import com.ensoftcorp.open.loop.comprehension.utils.LoopPatternConstants;
import com.ensoftcorp.open.loop.comprehension.utils.LoopUtils;
import com.ensoftcorp.open.loop.comprehension.utils.PathProperty;
import com.ensoftcorp.open.loop.comprehension.utils.TerminatingConditions;

/**
 * Generates Loop Catalog
 * 
 * @author Payas Awadhutkar
 */

public class LoopCatalog {
	
	public static void run(File outputFile) throws IOException, InterruptedException {
		run(outputFile, 4);
	}
	
	public static void run(File outputFile, File directory1, File directory2) throws IOException, InterruptedException {
		run(outputFile, directory1, directory2, 4);
	}
	
	public static void run(File outputFile, File directory1, File directory2, int flag) throws IOException, InterruptedException {
		FileWriter output = new FileWriter(outputFile);
		directory1.mkdirs();
		directory2.mkdirs();
	/*	// flag = 1 only monotonicity
		// flag = 2 monotonicity + bounds
		if(flag == 1) {
			String Headings = "LOOP_HEADER_ID,Project,Type,Method,Header Statement,Container,No. of Terminating Conditions,Monotonic,Pattern,Taint Graph Nodes,Taint Graphs Edges,PCG Nodes,PCG Edges";
			output.write(Headings);
			output.write("\n");
			output.flush();
			monotonicityStats(output,directory1,directory2);
		}
		else if(flag == 2) {
			String Headings = "LOOP_HEADER_ID,Project,Type,Method,Header Statement,Container,No. of Terminating Conditions,Monotonic,Pattern,Lower Bound,Upper Bound,Taint Graph Nodes,Taint Graphs Edges,PCG Nodes,PCG Edges";
			output.write(Headings);
			output.write("\n");
			output.flush();
			boundStats(output,directory1,directory2);
		}
		else if(flag == 3) {
			String subsystem = "CompressionSubsystem,CryptoSubSystem,DatabaseSubsystem,DataStructureSubsystem,GarbageCollectionSubsystem,HardwareSubsystem,IntrospectionSubsystem,IOSubsystem,JavaCoreSubsystem,LogSubsystem,MathSubsystem,NetworkSubsystem,RandomSubsystem,RMISubsystem,SecuritySubsystem,SerializationSubsystem,TestingSubsystem,ThreadingSubsystem,UISubsystem,NumSubsystems";
			String Headings = "LOOP_HEADER_ID,Project,Type,Method,Header Statement,Container,No. of Terminating Conditions,Monotonic,Pattern,Lower Bound,Upper Bound,Data Flow Nodes,Data Flow Edges,Taint Graph Nodes,Taint Graphs Edges,Control Flow Nodes,Control Flow Edges,PCG Nodes,PCG Edges,";
			output.write(Headings);
			output.write(subsystem);
			output.write("\n");
			output.flush();
			statsWithSubsystems(output, directory1, directory2, false);
		}
		else if(flag == 4) {*/
			String headings = "LOOP_HEADER_ID,Project,Type,Method,Header Statement,Container,No. of Terminating Conditions,Monotonic,Pattern,Lower Bound,Upper Bound,Data Flow Nodes,Data Flow Edges,Taint Graph Nodes,Taint Graphs Edges,Control Flow Nodes,Control Flow Edges,PCG Nodes,PCG Edges,";
			String nestingDepth = "NestingDepth,";
			String subsystem = "CompressionSubsystem,CryptoSubSystem,DatabaseSubsystem,DataStructureSubsystem,GarbageCollectionSubsystem,HardwareSubsystem,IntrospectionSubsystem,IOSubsystem,JavaCoreSubsystem,LogSubsystem,MathSubsystem,NetworkSubsystem,RandomSubsystem,RMISubsystem,SecuritySubsystem,SerializationSubsystem,TestingSubsystem,ThreadingSubsystem,UISubsystem,NumSubsystems";
			String callsiteProperties = "callSites,callSitesJDK,conditionalCallSites,conditionalCallSitesJDK,numPaths";
			String pathProperties = PathProperty.ContainsCallSites.toString() + "," + 
					PathProperty.ContainsJDKCallSites.toString() + "," +
					PathProperty.ContainsJDKCallSitesOnly.toString()  + "," +
					PathProperty.ContainsNoCallSites.toString()  + "," +
					PathProperty.ContainsNoJDKCallSites.toString() + "," +
					PathProperty.ContainsNonJDKCallSites.toString() + ",";
			output.write(headings);
			output.write(nestingDepth);
			output.write(subsystem);
			output.write(","+callsiteProperties);
			output.write(","+pathProperties);
			output.write("\n");
			output.flush();
			statsWithSubsystems(output, directory1, directory2, true);
		/*}
		else {
			Log.info("Invalid Flag");
		}*/
		output.close();
		Log.info("Finished analysis");
	}
	
	public static void run(File outputFile, int flag) throws IOException, InterruptedException {
		FileWriter output = new FileWriter(outputFile);
		
		// flag = 1 only monotonicity
		// flag = 2 monotonicity + bounds
		
		/*if(flag == 1) {
			String headings = "LOOP_HEADER_ID,Project,Type,Method,Header Statement,Container,No. of Terminating Conditions,Monotonic,Pattern,Taint Graph Nodes,Taint Graphs Edges,PCG Nodes,PCG Edges";
			output.write(headings);
			output.write("\n");
			output.flush();
			monotonicityStats(output);
		}
		else if(flag == 2) {
			String headings = "LOOP_HEADER_ID,Project,Type,Method,Header Statement,Container,No. of Terminating Conditions,Monotonic,Pattern,Lower Bound, Upper Bound,Taint Graph Nodes,Taint Graphs Edges,PCG Nodes,PCG Edges";
			output.write(headings);
			output.write("\n");
			output.flush();
			boundStats(output);
		}
		else if(flag == 3) {
			String headings = "LOOP_HEADER_ID,Project,Type,Method,Header Statement,Container,No. of Terminating Conditions,Monotonic,Pattern,Lower Bound,Upper Bound,Data Flow Nodes,Data Flow Edges,Taint Graph Nodes,Taint Graphs Edges,Control Flow Nodes,Control Flow Edges,PCG Nodes,PCG Edges,";
			String subsystem = "CompressionSubsystem,CryptoSubSystem,DatabaseSubsystem,DataStructureSubsystem,GarbageCollectionSubsystem,HardwareSubsystem,IntrospectionSubsystem,IOSubsystem,JavaCoreSubsystem,LogSubsystem,MathSubsystem,NetworkSubsystem,RandomSubsystem,RMISubsystem,SecuritySubsystem,SerializationSubsystem,TestingSubsystem,ThreadingSubsystem,UISubsystem,NumSubsystems";
			output.write(headings);
			output.write(subsystem);
			output.write("\n");
			output.flush();
			statsWithSubsystems(output, false);
		}
		else if(flag == 4) {*/
			String headings = "LOOP_HEADER_ID,Project,Type,Method,Header Statement,Container,No. of Terminating Conditions,Monotonic,Pattern,Lower Bound,Upper Bound,Data Flow Nodes,Data Flow Edges,Taint Graph Nodes,Taint Graphs Edges,Control Flow Nodes,Control Flow Edges,PCG Nodes,PCG Edges,";
			String nestingDepth = "NestingDepth,";
			String subsystem = "CompressionSubsystem,CryptoSubSystem,DatabaseSubsystem,DataStructureSubsystem,GarbageCollectionSubsystem,HardwareSubsystem,IntrospectionSubsystem,IOSubsystem,JavaCoreSubsystem,LogSubsystem,MathSubsystem,NetworkSubsystem,RandomSubsystem,RMISubsystem,SecuritySubsystem,SerializationSubsystem,TestingSubsystem,ThreadingSubsystem,UISubsystem,NumSubsystems";
			String callsiteProperties = "callSites,callSitesJDK,conditionalCallSites,conditionalCallSitesJDK,numPaths";
			String pathProperties = PathProperty.ContainsCallSites.toString() + "," + 
					PathProperty.ContainsJDKCallSites.toString() + "," +
					PathProperty.ContainsJDKCallSitesOnly.toString()  + "," +
					PathProperty.ContainsNoCallSites.toString()  + "," +
					PathProperty.ContainsNoJDKCallSites.toString() + "," +
					PathProperty.ContainsNonJDKCallSites.toString() + ",";
			output.write(headings);
			output.write(nestingDepth);
			output.write(subsystem);
			output.write(","+callsiteProperties);
			output.write(","+pathProperties);
			output.write("\n");
			output.flush();
			statsWithSubsystems(output, true);
		/*}
		else {
			Log.info("Invalid Flag");
		}*/
		output.close();
		Log.info("Finished analysis");
	
	}

	/*private static void boundStats(FileWriter output) throws IOException, InterruptedException {
		boundStats(output,null,null);
		
	}

	private static void monotonicityStats(FileWriter output) throws IOException, InterruptedException {
		monotonicityStats(output,null,null);
		
	}*/
	
	private static void statsWithSubsystems(FileWriter output, boolean includeCallsiteAndPathStats) throws IOException, InterruptedException {
		statsWithSubsystems(output,null,null,includeCallsiteAndPathStats);
	}

	/*public static void monotonicityStats(FileWriter output, File directory1, File directory2) throws IOException, InterruptedException {
		
		Q loops = Common.universe().nodesTaggedWithAny(CFGNode.LOOP_HEADER);
		
		for(Node loop : loops.eval().nodes() ) {	
			Monotonicity.monotonicity(Common.toQ(loop));	
		}
		
		for(Node loop : loops.eval().nodes()) {
			writeStats(loop, output);
			Q conditions = TerminatingConditions.findTerminatingConditionsForLoopHeader(Common.toQ(loop));
			output.write(conditions.eval().nodes().size() + ",");
			if(loop.taggedWith(LoopPatternConstants.MONOTONIC_LOOP)) {
				output.write("Yes" + ",");
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_PRIMITIVE + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_PRIMITIVE + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_PRIMITIVE + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_ARRAY + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_ARRAY + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_ARRAY + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_ARRAY + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_COLLECTION + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_COLLECTION + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_COLLECTION + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_COLLECTION + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR + ",");
				}
			}
			else {
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD + ",");
				}
				else {
					output.write("No" + ",,");
				}
			}
			Q lcefg = MonotonicityViews.createView(Common.toQ(loop));
			Q tg = TaintGraph.taintGraphWithoutCycles(Common.toQ(loop));
			long lcefgNodes = lcefg.eval().nodes().size();
			long lcefgEdges = lcefg.eval().edges().size();
			long tgNodes = tg.eval().nodes().size();
			long tgEdges = tg.eval().edges().size();
			output.write(tgNodes + "," + tgEdges + "," + lcefgNodes + "," + lcefgEdges + "\n");
			output.flush();
			if(directory1 != null && directory2 != null) {
				saveGraphs(loop,lcefg,tg,conditions,directory1,directory2);
			}
		}
	}
	
	private static void boundStats(FileWriter output, File directory1, File directory2) throws IOException, InterruptedException {
		String[] bounds = new String[2];
		Q loops = Common.universe().nodesTaggedWithAll(CFGNode.LOOP_HEADER);
		for(Node loop : loops.eval().nodes()) {
			writeStats(loop, output);
			bounds = Monotonicity.bounds(Common.toQ(loop));
			Q conditions = TerminatingConditions.findTerminatingConditionsForLoopHeader(Common.toQ(loop));
			output.write(conditions.eval().nodes().size() + ",");
			if(conditions.eval().nodes().size() == 0) {
				output.write("No,0-TC,,,");
			}
			if(loop.taggedWith(LoopPatternConstants.MONOTONIC_LOOP)) {
				output.write("Yes" + ",");
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_PRIMITIVE + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_PRIMITIVE + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_PRIMITIVE + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_ARRAY + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_ARRAY + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_ARRAY + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_ARRAY + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_COLLECTION + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_COLLECTION + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_COLLECTION + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_COLLECTION + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD + ",");
				}
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR + ",");
				}
				
				output.write(bounds[0] + "," + bounds[1] + ",");
			}
			else {
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD)) {
					output.write("No,"+ LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD + ",");
				}
				else {
					output.write("No" + ",,");
				}
				output.write(bounds[0] + "," + bounds[1] + ",");
			}
			Q lcefg = MonotonicityViews.createView(Common.toQ(loop));
			Q tg = TaintGraph.taintGraphWithoutCycles(Common.toQ(loop));
			long lcefgNodes = lcefg.eval().nodes().size();
			long lcefgEdges = lcefg.eval().edges().size();
			long tgNodes = tg.eval().nodes().size();
			long tgEdges = tg.eval().edges().size();
			output.write(tgNodes + "," + tgEdges + "," + lcefgNodes + "," + lcefgEdges + "\n");
			output.flush();
			if(directory1 != null && directory2 != null) {
				saveGraphs(loop,lcefg,tg,conditions,directory1,directory2);
			}
		}
		
	}
	*/
	private static void statsWithSubsystems(FileWriter output, File directory1, File directory2, boolean includeCallsiteAndPathStats) throws IOException, InterruptedException {
		// writes stats about subsystems
		String[] bounds = new String[2];
		int interactions;
		Q loops = Common.universe().nodesTaggedWithAll(CFGNode.LOOP_HEADER);
		long[] stat = new long[31];
		// total loops
		stat[0] = loops.eval().nodes().size();
		for(Node loop : loops.eval().nodes()) {
			Q conditions = TerminatingConditions.findTerminatingConditionsForLoopHeader(Common.toQ(loop));
			if(conditions.eval().nodes().size() == 0) {
				continue;
			}
			writeLoopLocationStats(loop, output);
			bounds = Monotonicity.bounds(Common.toQ(loop));
			
			output.write(conditions.eval().nodes().size() + ",");
			if(loop.taggedWith(LoopPatternConstants.MONOTONIC_LOOP)) {
				// monotonic loop
				stat[1] = stat[1] + 1;
				output.write("Yes" + ",");
				if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_PRIMITIVE + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_PRIMITIVE + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_PRIMITIVE + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_ARRAY + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_ARRAY + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_ARRAY + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_ARRAY)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_ARRAY + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_COLLECTION + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_COLLECTION + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_COLLECTION + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_COLLECTION)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_COLLECTION + ",");
				}
				else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR)) {
					output.write(LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR + ",");
				}
				else {
					if(ioTagCheck(output, loop)) {
						stat[2] = stat[2] + 1;
					}
					else {
						output.write(",");
					}	
				}
				output.write(bounds[0] + "," + bounds[1] + ",");
			}
			else {
				output.write("No" + ",");
				/*if(conditions.eval().nodes().size() == 0) {
					stat[2] = stat[2] + 1;
					output.write("0-TC,,,");
				}*/
				if(ioTagCheck(output, loop)) {
					stat[2] = stat[2] + 1;
					output.write(bounds[0] + "," + bounds[1] + ",");
				}
				else {
					output.write("," + bounds[0] + "," + bounds[1] + ",");
				}
			}
			Q lcefg = LoopAbstractions.getTerminationPCG(Common.toQ(loop));
			Q tg = LoopAbstractions.taintGraphWithoutCycles(Common.toQ(loop));
			//Q method = CommonQueries.getContainingFunctions(Common.toQ(loop));
			//Q df = method.forwardOn(Common.edges(XCSG.Contains)).induce(Common.edges(XCSG.LocalDataFlow));
			Q lcfg = lcefg.difference(Common.universe().nodesTaggedWithAny("EventFlow_Master_Entry","EventFlow_Master_Exit"));
			lcfg = Common.universe().edgesTaggedWithAny(XCSG.ControlFlow_Edge).between(Common.toQ(loop), lcfg.nodesTaggedWithAny(BoundaryConditions.BOUNDARY_CONDITION));
			Q df = lcfg.contained().induce(Common.edges(XCSG.LocalDataFlow));
			long dfNodes = df.eval().nodes().size();
			stat[22] = stat[22] + dfNodes;
			long dfEdges = df.eval().edges().size();
			stat[23] = stat[23] + dfEdges;
			long tgNodes = tg.eval().nodes().size();
			stat[24] = stat[24] + tgNodes;
			long tgEdges = tg.eval().edges().size();
			stat[25] = stat[25] + tgEdges;
			long cfNodes = lcfg.eval().nodes().size();
			stat[26] = stat[26] + cfNodes;
			long cfEdges = lcfg.eval().edges().size();
			stat[27] = stat[27] + cfEdges;
			long lcefgNodes = lcefg.eval().nodes().size();
			stat[28] = stat[28] + lcefgNodes;
			long lcefgEdges = lcefg.eval().edges().size();
			stat[29] = stat[29] + lcefgEdges;
			output.write(dfNodes + "," + dfEdges + "," + tgNodes + "," + tgEdges + "," + cfNodes + "," + cfEdges + "," + lcefgNodes + "," + lcefgEdges + ",");
			long depth = LoopUtils.getNestingDepth(loop);
			output.write(depth+",");
			loop.putAttr(LoopUtils.NESTING_DEPTH, depth);
			interactions = writeSubSystemStats(Common.toQ(loop), output, stat);
			stat[30] = stat[30] + interactions;
			output.write(interactions + ",");
			if(includeCallsiteAndPathStats) {
				LoopCallSiteStats.writeCallsitePathStatsForLoop(loop, output);
			}
			output.write("\n");
			output.flush();
			if(directory1 != null && directory2 != null) {
				saveGraphs(loop,lcefg,tg,conditions,directory1,directory2);
			}
		}
		double totalLoops = stat[0];
		output.write(",,,,,,,");
		output.write(stat[1]*100.0/totalLoops + "," + stat[2]*100.0/totalLoops + ",,,");
		output.write(stat[22]/totalLoops + "," + stat[23]/totalLoops + "," + stat[24]/totalLoops + "," + stat[25]/totalLoops + ",");
		output.write(stat[26]/totalLoops + "," + stat[27]/totalLoops + "," + stat[28]/totalLoops + "," + stat[29]/totalLoops + ",");
		for(int i = 3; i < 22; i++) {
			output.write(stat[i]*100.0/totalLoops + ",");
		}
		output.write(stat[30]/totalLoops + ",");
		output.write("\n");
		output.flush();
		
	}
	
	private static boolean ioTagCheck(FileWriter output, Node loop) throws IOException {
		if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL)) {
			output.write(LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL + ",");
			return true;
		}
		else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER)) {
			output.write(LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER + ",");
			return true;
		}
		else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD)) {
			output.write(LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD + ",");
			return true;
		}
		else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL)) {
			output.write(LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL + ",");
			return true;
		}
		else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER)) {
			output.write(LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER + ",");
			return true;
		}
		else if(loop.taggedWith(LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD)) {
			output.write(LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD + ",");
			return true;
		}
		/*if(loop.taggedWith("IO == ")) {
			output.write("IO pattern,");
			return true;
		}
		else if(loop.taggedWith("API being compared")) {
			output.write("API pattern,");
			return true;
		}*/
		else {
			return false;
		}
	}

	private static int writeSubSystemStats(Q loopHeader, FileWriter output, long[] stat) throws IOException {
		Q value;
		int count = 0;
		// CompressionSubsystem
		value = LoopUtils.getSubsystemInteractions(loopHeader, CompressionSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[3] = stat[3] + 1;
			count = count + 1;
		}
		// Crypto Subsystem
		value = LoopUtils.getSubsystemInteractions(loopHeader, CryptoSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[4] = stat[4] + 1;
			count = count + 1;
		}
		// Database Subsystem
		value = LoopUtils.getSubsystemInteractions(loopHeader, DatabaseSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[5] = stat[5] + 1;
			count = count + 1;
		}
		// Data structure Subsystem
		value = LoopUtils.getSubsystemInteractions(loopHeader, DataStructureSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[6] = stat[6] + 1;
			count = count + 1;
		}
		// Garbage Collection
		value = LoopUtils.getSubsystemInteractions(loopHeader, GarbageCollectionSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[7] = stat[7] + 1;
			count = count + 1;
		}
		// Hardware
		value = LoopUtils.getSubsystemInteractions(loopHeader, HardwareSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[8] = stat[8] + 1;
			count = count + 1;
		}
		// Introspection
		value = LoopUtils.getSubsystemInteractions(loopHeader, IntrospectionSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[9] = stat[9] + 1;
			count = count + 1;
		}
		// IO
		value = LoopUtils.getSubsystemInteractions(loopHeader, IOSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[10] = stat[10] + 1;
			count = count + 1;
		}
		// Java Core
		value = LoopUtils.getSubsystemInteractions(loopHeader, JavaCoreSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[11] = stat[11] + 1;
			count = count + 1;
		}
		// Log
		value = LoopUtils.getSubsystemInteractions(loopHeader, LogSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[12] = stat[12] + 1;
			count = count + 1;
		}
		// Math
		value = LoopUtils.getSubsystemInteractions(loopHeader, MathSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[13] = stat[13] + 1;
			count = count + 1;
		}
		// Network
		value = LoopUtils.getSubsystemInteractions(loopHeader, NetworkSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[14] = stat[14] + 1;
			count = count + 1;
		}
		// Random
		value = LoopUtils.getSubsystemInteractions(loopHeader, RandomSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[15] = stat[15] + 1;
			count = count + 1;
		}
		// RMI
		value = LoopUtils.getSubsystemInteractions(loopHeader, RMISubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[16] = stat[16] + 1;
			count = count + 1;
		}
		// Security
		value = LoopUtils.getSubsystemInteractions(loopHeader, SecuritySubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[17] = stat[17] + 1;
			count = count + 1;
		}
		// Serialization
		value = LoopUtils.getSubsystemInteractions(loopHeader, SerializationSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[18] = stat[18] + 1;
			count = count + 1;
		}
		// Testing
		value = LoopUtils.getSubsystemInteractions(loopHeader, TestingSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[19] = stat[19] + 1;
			count = count + 1;
		}
		// Threading
		value = LoopUtils.getSubsystemInteractions(loopHeader, ThreadingSubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[20] = stat[20] + 1;
			count = count + 1;
		}
		// UI
		value = LoopUtils.getSubsystemInteractions(loopHeader, UISubsystem.TAG);
		if(subystemStats(output, value)) {
			stat[21] = stat[21] + 1;
			count = count + 1;
		}
		return count;
	}

	public static boolean subystemStats(FileWriter output, Q value) throws IOException {
		if(value.eval().nodes().isEmpty()) {
			output.write("No" + ",");
			return false;
		}
		else {
			output.write("Yes" + ",");
			return true;
		}
	}

	private static void saveGraphs(Node loop, Q lcefg, Q tg, Q conditions, File directory1, File directory2) throws InterruptedException {
		Highlighter h = highlighterForLoopDepth(lcefg);
		h.highlightNodes(Common.toQ(loop), Color.YELLOW);
		h.highlightNodes(conditions, Color.RED);
		String typeName = CommonQueries.getContainingFunctions(Common.toQ(loop)).reverseStepOn(Common.universe().edgesTaggedWithAny(XCSG.Contains),1).roots().eval().nodes().getFirst().getAttr(XCSG.name).toString();
		String methodName = CommonQueries.getContainingFunction(loop).getAttr(XCSG.name).toString();
		String loopStatement = loop.getAttr(XCSG.name).toString().substring(0, loop.getAttr(XCSG.name).toString().indexOf(":"));
		String filename = typeName + "-" + methodName + "-" + loopStatement;
		//change directory path before running
		DisplayUtils.save(tg, h, false, "tg-"+filename, directory1);
		DisplayUtils.save(lcefg, h, false, "lcefg-"+filename, directory2);
	}
	
	public static void writeLoopLocationStats(Node loopHeader, FileWriter output) throws IOException {
		output.write(loopHeader.getAttr(CFGNode.LOOP_HEADER_ID).toString() + ",");
		LoopCallSiteStats.writeLoopLocation(loopHeader, output);
	}
	
	public static Highlighter highlighterForLoopDepth(Q cfg) {
		Highlighter h = new Highlighter(ConflictStrategy.LAST_MATCH);

		Q loopHeadersQ = cfg.nodesTaggedWithAll(CFGNode.LOOP_HEADER);
		AtlasSet<Node> loopHeaders = loopHeadersQ.eval().nodes();
		
		Map<GraphElement, Color> colorMap = new HashMap<>();
		for (GraphElement loopHeader : loopHeaders) {
			Color color = highlighterForLoopDepth(colorMap, loopHeader);
			h.highlight(toQ(toGraph(loopHeader)), color);
		}
		
		// set color of loop members (other than loop headers) to same color as header 
		Q loopFragments = cfg.selectNode(CFGNode.LOOP_MEMBER_ID).difference(loopHeadersQ);
		for (GraphElement member : loopFragments.eval().nodes()) {
			Object id = member.getAttr(CFGNode.LOOP_MEMBER_ID);
			GraphElement loopHeader = Query.universe().selectNode(CFGNode.LOOP_HEADER_ID, id).eval().nodes().getFirst();

			h.highlight(Common.toQ(Common.toGraph(member)), colorMap.get(loopHeader));
		}
		return h;
	}
	
	private static Color highlighterForLoopDepth(Map<GraphElement, Color> colorMap, GraphElement loopHeader) {
		final Color cfgNodeFillColor = new Color(51, 175, 243);
		Color color = colorMap.get(loopHeader);
		if (color == null) {
			Object idObj = loopHeader.getAttr(CFGNode.LOOP_MEMBER_ID);
			if (idObj == null) {
				// loop is not nested
				color = cfgNodeFillColor;
			} else {
				GraphElement parentLoopHeader = Query.universe().selectNode(CFGNode.LOOP_HEADER_ID, idObj).eval().nodes().getFirst();
				color = highlighterForLoopDepth(colorMap, parentLoopHeader);
				color = color.darker();
			}
			colorMap.put(loopHeader, color);
		}
		return color;
	}
	

}
