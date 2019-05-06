package com.kcsl.loop.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.DominanceAnalysis;
import com.ensoftcorp.open.commons.algorithms.UniqueEntryExitControlFlowGraph;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;

public class DifferentialBranchPoints {

	/**
	 * Tag applied to differential branch point nodes
	 */
	public static final String DIFFERENTIAL_BRANCH_POINT = "DIFFERENTIAL_BRANCH_POINT";

	public static void findAllDifferentialBranchPoints() throws InterruptedException {
//		IndexingPhases.confirmLoopAnalyzer();
		
		Job j = new Job("Find All Differential Branch Points") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				findAllDifferentialBranchPoints(monitor);
				return Status.OK_STATUS;
			}
		};
		
		j.schedule();
		j.join();
	}

	private static void findAllDifferentialBranchPoints(IProgressMonitor monitor){
		AtlasSet<Node> methods = Query.universe().nodesTaggedWithAll(XCSG.Method).eval().nodes();
		try {
			monitor.beginTask("Finding Differential Branch Points", (int)methods.size());
			for(Node method : methods){
				findDifferentialBranchPoints(SubMonitor.convert(monitor, 1), Common.toQ(method));
				monitor.worked(1);
				if (monitor.isCanceled()){
					return;
				}
			}
		} finally {
			monitor.done();
		}
	}
	
	private static void findDifferentialBranchPoints(IProgressMonitor monitor, Q method){
		Q cfg = CommonQueries.excfg(method);
		Node cfgRoot = cfg.nodes(XCSG.controlFlowRoot).eval().nodes().one();
		if(cfgRoot == null){
			return;
		}
		
		Q postDominanceFrontier;
		if(CommonsPreferences.isComputeControlFlowGraphDominanceEnabled() || CommonsPreferences.isComputeExceptionalControlFlowGraphDominanceEnabled()){
			// use the pre-compute relationships if they are available
			postDominanceFrontier = DominanceAnalysis.getPostDominanceFrontierEdges();
		} else {
			postDominanceFrontier = Common.toQ(DominanceAnalysis.computePostDominanceTree(new UniqueEntryExitControlFlowGraph(cfg.eval())));
		}

		Q events = cfg.nodes(XCSG.Loop);
		for(Node loopHeader : events.eval().nodes()){
			for(Node node : postDominanceFrontier.successors(Common.toQ(loopHeader)).eval().nodes()){
				if(node.taggedWith(XCSG.ControlFlowCondition)){
					node.tag(DifferentialBranchPoints.DIFFERENTIAL_BRANCH_POINT);
				}
			}
		}
	}

}
