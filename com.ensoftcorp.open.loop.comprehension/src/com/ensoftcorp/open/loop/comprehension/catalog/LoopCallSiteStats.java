package com.ensoftcorp.open.loop.comprehension.catalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.licensing.AtlasLicenseException;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.log.Log;
import com.ensoftcorp.open.loop.comprehension.taint.TaintOverlay;
import com.ensoftcorp.open.loop.comprehension.utils.AtlasUtil;
import com.ensoftcorp.open.loop.comprehension.utils.Explorer;
import com.ensoftcorp.open.loop.comprehension.utils.LoopUtils;
import com.ensoftcorp.open.loop.comprehension.utils.PathProperty;
import com.ensoftcorp.open.loop.comprehension.utils.TerminatingConditions;
import com.ensoftcorp.open.pcg.common.PCGFactory;

public class LoopCallSiteStats {
	 
	public static void runOnProjects(final String commaSeparatedProjectNames, final String outputPath) throws AtlasLicenseException, IOException, InterruptedException {
		
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

		Job j1 = new Job("Loop stats") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				for(String projectName: commaSeparatedProjectNames.split(",")) {
					final IProject iProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
					SubMonitor sm = SubMonitor.convert(monitor, 42161);
					try {
						String m = "Indexing "+projectName+" with Atlas";
						sm.setTaskName(m);
						Log.info(m);
						AtlasUtil.mapProject(iProject);
						sm.worked(30);
						
						m = "Running loop stats for "+projectName;
						sm.setTaskName(m);
						Log.info(m);
						LoopCallSiteStats.run(new File(outputPath+"loopstats-"+projectName+"-"+sdf.format(new Date())+".csv"));
						sm.worked(10);
					} catch (AtlasLicenseException | IOException e) {
						Log.warning("Cannot run Atlas or IO Exception", e);
					}
				}
				return Status.OK_STATUS;
			}
		};
		j1.schedule();
		j1.join();
		
		Log.info("Completed loop stats");
	}

	
	public static void run(File outputFile) throws IOException {
		FileWriter output = new FileWriter(outputFile);
		
		String headings = "LOOP_HEADER_ID,Project,Type,Method,Header Statement,Container,#Terminating Conditions,#Boundary Conditions,Method CFG Nodes,Method CFG Edges,Loop CFG Nodes,Loop CFG Edges,Slice Nodes,Slice Edges,externalTaintRoots,callsiteTaintRoots,parameterTaintRoots,globalVariableTaintRoots,external sanity check passed,localTaintRoots,literalRoots,instantiationRoots,local sanity check passed,#invokedTypesOfCallsiteTaintRoots,invokedTypesOfCallsiteTaintRoots,callsiteContainingPackages,callsiteQualifiedNames,#typesOfInstantiationRoots,typesOfInstantiationRoots,callSites,callSitesJDK,conditionalCallSites,conditionalCallSitesJDK,numPaths";

		String pathPropertyHeadings = PathProperty.ContainsCallSites.toString() + "," + 
										PathProperty.ContainsJDKCallSites.toString() + "," +
										PathProperty.ContainsJDKCallSitesOnly.toString()  + "," +
										PathProperty.ContainsNoCallSites.toString()  + "," +
										PathProperty.ContainsNoJDKCallSites.toString() + "," +
										PathProperty.ContainsNonJDKCallSites.toString() + ",";
		headings = headings + "," + pathPropertyHeadings + "\n";
		output.write(headings);
		
		Log.info("Starting loop stats");
		Q loopHeaders = Common.universe().nodesTaggedWithAny(XCSG.Loop, CFGNode.LOOP_HEADER);
		for(com.ensoftcorp.atlas.core.db.graph.Node loopHeader : loopHeaders.eval().nodes()){
			computeLoopStatistics(loopHeader, output);
			output.flush();
		}
		Log.info("Completed loop stats");
		output.close();
	}
	
	/*public static void saveDOTGraph(File outputFile, Graph<String, DefaultEdge> graph) throws IOException {
		VertexNameProvider<String> provider = new StringNameProvider<String>();
		DOTExporter<String, DefaultEdge> exporter = new DOTExporter<String, DefaultEdge>(
				provider, null, null);
		exporter.export(new FileWriter(outputFile), graph);
	}*/
	
	private static void computeLoopStatistics(com.ensoftcorp.atlas.core.db.graph.Node loopHeader, FileWriter output) throws IOException {

		output.write(loopHeader.getAttr(CFGNode.LOOP_HEADER_ID).toString() + ",");

		writeLoopLocation(loopHeader, output);
		
		// termination conditions for loop header
		Q tc = TerminatingConditions.findTerminatingConditionsForLoopHeader(Common.toQ(loopHeader));
		output.write(CommonQueries.nodeSize(tc) + ",");
		
		// boundary conditions for loop header
		Q bc = Common.universe().selectNode(CFGNode.LOOP_MEMBER_ID,loopHeader.getAttr(CFGNode.LOOP_HEADER_ID)).nodesTaggedWithAny(BoundaryConditions.BOUNDARY_CONDITION);
		output.write(CommonQueries.nodeSize(bc) + ",");
		
		// all taints
		Q taints = Common.universe().edgesTaggedWithAny(TaintOverlay.Taint);

		// method containing loop header
		Q method = CommonQueries.getContainingFunctions(Common.toQ(loopHeader));

		// method contents
		Q methodLocalDeclarations = CommonQueries.localDeclarations(method);

		// local taints influencing loop termination (taint events)
		Q conditions = tc.children().nodesTaggedWithAny(XCSG.DataFlowCondition);
		Q loopTerminationTaints = conditions.reverseOn(taints).intersection(methodLocalDeclarations);

		// control flow graph of method
		Q methodCFG = CommonQueries.cfg(method);
		output.write(CommonQueries.nodeSize(methodCFG) + "," + CommonQueries.edgeSize(methodCFG) + ",");
		
		// control flow graph of loop body (includes nested loops' control flow as well)
		Q cfMembers = LoopUtils.getControlFlowMembers(loopHeader);
		output.write(CommonQueries.nodeSize(cfMembers) + "," + CommonQueries.edgeSize(cfMembers) + ",");
		
		// PCG of loop terminating conditions and statements influence terminating conditions (loop termination slice)
		Q events = loopTerminationTaints.parent().nodesTaggedWithAny(XCSG.ControlFlow_Node);
		
		Q pcg = PCGFactory.create(events).getPCG();
		output.write(CommonQueries.nodeSize(pcg) + "," + CommonQueries.edgeSize(pcg) + ",");
		
		// loop termination taints
		Q loopTerminationTaintGraph = loopTerminationTaints.induce(taints);

		// loop termination taint roots
		Q taintRoots = loopTerminationTaintGraph.roots();

		// external roots
		
		Q externalTaintRoots = taintRoots.difference(Common.universe().nodesTaggedWithAny(XCSG.Literal, XCSG.Instantiation, XCSG.ArrayInstantiation));
		output.write(CommonQueries.nodeSize(externalTaintRoots) + ",");
		
		Q callsiteTaintRoots = externalTaintRoots.nodesTaggedWithAny(XCSG.CallSite);
		output.write(CommonQueries.nodeSize(callsiteTaintRoots) + ",");
		
		Q parameterTaintRoots = externalTaintRoots.nodesTaggedWithAny(XCSG.Parameter);
		output.write(CommonQueries.nodeSize(parameterTaintRoots) + ",");
		
		Q globalVariableTaintRoots = externalTaintRoots.nodesTaggedWithAny(XCSG.Field, XCSG.Java.ArrayLengthOperator);
		output.write(CommonQueries.nodeSize(globalVariableTaintRoots) + ",");
		
		// sanity check
		output.write((CommonQueries.nodeSize(externalTaintRoots)==CommonQueries.nodeSize(externalTaintRoots.nodesTaggedWithAny(XCSG.CallSite, XCSG.Parameter, XCSG.Field))) + ",");
		
		// local roots
		
		Q localTaintRoots = taintRoots.difference(externalTaintRoots);
		output.write(CommonQueries.nodeSize(localTaintRoots) + ",");
		
		Q literalRoots = taintRoots.nodesTaggedWithAny(XCSG.Literal);
		output.write(CommonQueries.nodeSize(literalRoots) + ",");
		
		Q instantiationRoots = taintRoots.nodesTaggedWithAny(XCSG.Instantiation, XCSG.ArrayInstantiation);
		output.write(CommonQueries.nodeSize(instantiationRoots) + ",");
		
		// sanity check
		output.write((CommonQueries.nodeSize(localTaintRoots)==CommonQueries.nodeSize(localTaintRoots.nodesTaggedWithAny(XCSG.Literal, XCSG.Instantiation, XCSG.ArrayInstantiation))) + ",");
		
		// invoked types for call sites in the slice
				Q typesOfCallsiteTaintRoots = callsiteTaintRoots.forwardOn(Common.edges(XCSG.InvokedType)).leaves();
				output.write(CommonQueries.nodeSize(typesOfCallsiteTaintRoots) + ",");
				String invokedTypeNames = "";
				for(Node t : typesOfCallsiteTaintRoots.eval().nodes()) {
					invokedTypeNames += t.getAttr(XCSG.name);
					invokedTypeNames += "; ";
				}
				output.write(invokedTypeNames + ",");

		// invoked types for call sites in the loop CFG
				Q cs = cfMembers.contained().nodesTaggedWithAny(XCSG.CallSite);
				Set<String> targetTypeNames = new HashSet<String>();
				Set<String> targetPackageNames = new HashSet<String>();
				for(Node c : cs.eval().nodes()) {
					Q targetMethods = CallSiteAnalysis.getTargets(Common.toQ(c));
					for(Node t : targetMethods.eval().nodes()) {
						Node containingNode = Explorer.getContainingNode(t,XCSG.Package);
						if(containingNode != null) {
							targetPackageNames.add(containingNode.getAttr(XCSG.name).toString());
						}
						Node containingNode2 = Explorer.getContainingNode(t,XCSG.Type);
						targetTypeNames.add(((containingNode!=null)?containingNode.getAttr(XCSG.name):"no-package") + "." + (containingNode2!=null?containingNode2.getAttr(XCSG.name):"?")+"."+(t!=null?t.getAttr(XCSG.name):"?"));
					}
				}
				output.write(targetPackageNames.toString().replaceAll(",", ";") + ",");
				output.write(targetTypeNames.toString().replaceAll(",", ";") + ",");	
				
				// types of instantiation roots
				Q dfTerminatingConditions = tc.contained().nodesTaggedWithAll(XCSG.DataFlowCondition);
				Q tcTaintRoots = Common.edges(TaintOverlay.Taint).reverse(dfTerminatingConditions).differenceEdges(Common.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT)).roots().nodesTaggedWithAny(XCSG.Literal, XCSG.Instantiation, XCSG.ArrayInstantiation, XCSG.Initialization);
				Q typesOfTaintRoots = tcTaintRoots.forwardOn(Common.edges(XCSG.TypeOf)).leaves();
				output.write(CommonQueries.nodeSize(typesOfTaintRoots) + ",");
				String typeNames = "";
				for(Node t : typesOfTaintRoots.eval().nodes()) {
					 typeNames += t.getAttr(XCSG.name);
					 typeNames += "; ";
				}
				output.write(typeNames + ",");
				
		writeCallsitePathStatsForLoop(loopHeader, output);
		
		
		output.write("\n");
		output.flush();
	}


	public static void writeCallsitePathStatsForLoop(com.ensoftcorp.atlas.core.db.graph.Node loopHeader, FileWriter output)
			throws IOException {
		// callsite stats
		LoopCallsiteCounts callsiteCounts = computeCallsiteStats(loopHeader);
		
		output.write(callsiteCounts.getCallsitesInLoop() + ",");
		output.write(callsiteCounts.getCallsitesInLoopJDK() + ",");
		output.write(callsiteCounts.getConditionalCallsitesInLoop() + ",");
		output.write(callsiteCounts.getConditionalCallsitesInLoopJDK() + ",");
		
		
		// path statistics
		LoopPathCounts pathCounts = LoopPathCountStats.computePathCountStats(loopHeader); 
		
		output.write(pathCounts.getNumPaths() + ",");
		output.write(pathCounts.getNumPathsWithCallSites() + ",");
		output.write(pathCounts.getNumPathsWithJDKCallSites() + ",");
		output.write(pathCounts.getNumPathsWithJDKCallSitesOnly() + ",");
		output.write(pathCounts.getNumPathsWithoutCallSites() + ",");
		output.write(pathCounts.getNumPathsWithoutJDKCallSites() + ",");
		output.write(pathCounts.getNumPathsWithNonJDKCallSites() + ",");
	}


	public static LoopCallsiteCounts computeCallsiteStats(com.ensoftcorp.atlas.core.db.graph.Node loopHeader) {
		// callsite stats
		Q csInLoop = LoopUtils.findStatementsWithCallSitesWithinLoop(loopHeader);
		Q csInLoopJDK = SubsystemUtils.findSubsetWithJDKTargets(csInLoop);
		Q ccsInLoop = LoopUtils.findStatementsWithConditionalCallSitesWithinLoop(loopHeader);
		Q ccsInLoopJDK = SubsystemUtils.findSubsetWithJDKTargets(ccsInLoop);
		
		long callsitesInLoop = CommonQueries.nodeSize(csInLoop);
		long callsitesInLoopJDK = CommonQueries.nodeSize(csInLoopJDK);
		long conditionalCallsitesInLoop = CommonQueries.nodeSize(ccsInLoop);
		long conditionalCallsitesInLoopJDK = CommonQueries.nodeSize(ccsInLoopJDK);
		
		LoopCallsiteCounts counts = new LoopCallsiteCounts();
		counts.setCallsitesInLoop(callsitesInLoop);
		counts.setCallsitesInLoopJDK(callsitesInLoopJDK);
		counts.setConditionalCallsitesInLoop(conditionalCallsitesInLoop);
		counts.setConditionalCallsitesInLoopJDK(conditionalCallsitesInLoopJDK);
		
		return counts;
	}

	public static void writeLoopLocation(com.ensoftcorp.atlas.core.db.graph.Node loopHeader, FileWriter output) throws IOException {
		Q project = Common.toQ(loopHeader).containers().intersection(Common.universe().nodesTaggedWithAny(XCSG.Project));
		if(!CommonQueries.isEmpty(project)) {
			output.write(project.eval().nodes().getFirst().getAttr(XCSG.name) + ",");
		} else {
			project = Common.toQ(loopHeader).reverseOn(Common.universe().edgesTaggedWithAny(XCSG.Contains)).roots();
			if(!CommonQueries.isEmpty(project)) {
				output.write(project.eval().nodes().getFirst().getAttr(XCSG.name) + ",");
			} else {
				output.write("?"+",");
			}
		}
		
		String typeName = CommonQueries.getContainingFunctions(Common.toQ(loopHeader)).reverseStepOn(Common.universe().edgesTaggedWithAny(XCSG.Contains),1).roots().eval().nodes().getFirst().getAttr(XCSG.name).toString();
		output.write(typeName + ",");
		
		String methodName = CommonQueries.getContainingFunction(loopHeader).getAttr(XCSG.name).toString();
		output.write(methodName + ",");
		
		String loopStatement = "";
		try {
			int i = loopHeader.getAttr(XCSG.name).toString().indexOf(":");
			if(i<0) {
				loopStatement = loopHeader.getAttr(XCSG.name).toString();
			} else {
				loopStatement = loopHeader.getAttr(XCSG.name).toString().substring(0, i);
			}
		} catch(Throwable t) {
			//TODO call to substring throws String index out of bounds	
		}
		output.write(loopStatement + ",");
		
		String containerString = Common.toQ(loopHeader).parent().eval().nodes().getFirst().getAttr(XCSG.name).toString();
		output.write(containerString + ",");		
	}
}

class LoopCallsiteCounts {
	
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