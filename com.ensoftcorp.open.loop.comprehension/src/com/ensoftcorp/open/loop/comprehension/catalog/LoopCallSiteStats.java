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

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.licensing.AtlasLicenseException;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CFG;
import com.ensoftcorp.open.commons.analysis.StandardQueries;
import com.ensoftcorp.open.java.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.log.Log;
import com.ensoftcorp.open.loop.comprehension.utils.Explorer;
import com.ensoftcorp.open.loop.comprehension.utils.LoopUtils;
import com.ensoftcorp.open.loop.comprehension.utils.PathProperty;
import com.ensoftcorp.open.loop.comprehension.utils.TerminatingConditions;
import com.ensoftcorp.open.pcg.factory.PCGFactory;

public class LoopCallSiteStats {

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
		
		String typeName = StandardQueries.getContainingFunctions(Common.toQ(loopHeader)).reverseStepOn(Common.universe().edgesTaggedWithAny(XCSG.Contains),1).roots().eval().nodes().getFirst().getAttr(XCSG.name).toString();
		output.write(typeName + ",");
		
		String methodName = StandardQueries.getContainingFunction(loopHeader).getAttr(XCSG.name).toString();
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