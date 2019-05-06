package com.kcsl.loop.codemap.stages;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.java.commons.analysis.CommonQueries;
import com.kcsl.loop.catalog.lambda.LambdaIdentifier;
import com.kcsl.loop.preferences.LoopPreferences;
import com.kcsl.loop.taint.analysis.LoopReachability;
import com.kcsl.loop.util.Attributes;
import com.kcsl.loop.util.EntryPoints;
import com.kcsl.loop.util.Tags;

/**
 * Runs the control flow graph refinement step as a codemap stage
 * 
 * @author Ben Holland
 */
public class ReachabilityAnalysisCodemapStage extends PrioritizedCodemapStage {
	public static final String CONTROL_FLOW_REACHABLE = "CONTROL_FLOW_REACHABLE";
	public static final String DATA_FLOW_REACHABLE = "DATA_FLOW_REACHABLE";
	public static final String CONTROL_FLOW_REACHABLE_FROM = "CONTROL_FLOW_REACHABLE_FROM";
	public static final String DATA_FLOW_REACHABLE_FROM = "DATA_FLOW_REACHABLE_FROM";
	/**
	 * The unique identifier for the codemap stage
	 */
	public static final String IDENTIFIER = "com.kcsl.loop.codemap.reachability";
	
	@Override
	public String getDisplayName() {
		return "Tagging reachable loops";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{ 
					LoopCatalogCodemapStage.IDENTIFIER,
					LambdaCodemapStage.IDENTIFIER
				};
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		boolean runIndexer = false;
		Q entryPoints = Common.empty();
		Q inputs = Common.empty();
		if(LoopPreferences.isMainMethodEntryPointEnabled()) {
			runIndexer = true;
			entryPoints = entryPoints.union(EntryPoints.mainEP.getControlFlowEP());
			inputs = inputs.union(EntryPoints.mainEP.getDataFlowEP());
		}
		if(LoopPreferences.isHTTPHandlerEntryPointEnabled()) {
			runIndexer = true;
			entryPoints = entryPoints.union(EntryPoints.sunHttpRequestEP.getControlFlowEP());
			inputs = inputs.union(EntryPoints.sunHttpRequestEP.getDataFlowEP());
		}
		if(LoopPreferences.isSpringFrameworkEntryPointEnabled()) {
			runIndexer = true;
			// TODO: Handle Spring framework entry points
		}
		if(LoopPreferences.isSparkEntryPointEnabled()) {
			runIndexer = true;
			entryPoints = entryPoints.union(EntryPoints.sparkEP.getControlFlowEP());
			inputs = inputs.union(EntryPoints.sparkEP.getDataFlowEP());
		}
		
		AtlasSet<Node> entryPointMethods = entryPoints.eval().nodes();
		int value = 1;
		for(Node m: entryPointMethods) {
			m.putAttr(EntryPoints.ENTRY_POINT_ID, "E" + value);
			value = value + 1;
		}
		if(LoopPreferences.isControlFlowReachabilityEnabled() && !CommonQueries.isEmpty(entryPoints)) {
			runIndexer = true;
			//Tag loops reachable
			for(Node m: entryPointMethods) {
				String entryPointID = (String) m.getAttr(EntryPoints.ENTRY_POINT_ID);
				Q lcg = LoopReachability.getLoopReachabilityViaControlFlowView(Common.toQ(m));
				AtlasSet<Node> reachableLoops = lcg.contained().nodes(XCSG.Loop).eval().nodes();
				for(Node loop: reachableLoops) {
					Tags.setTag(Common.toQ(loop), CONTROL_FLOW_REACHABLE);
					if(!loop.hasAttr(CONTROL_FLOW_REACHABLE_FROM)) {
						loop.putAttr(CONTROL_FLOW_REACHABLE_FROM, Attributes.ListAttr.toString(new ArrayList<String>()));
					}
					ArrayList<String> entryPointList = Attributes.ListAttr.toListOfStrings((String) loop.getAttr(CONTROL_FLOW_REACHABLE_FROM));
					if(!entryPointList.contains(entryPointID)) {
						entryPointList.add(entryPointID);
					}
					loop.putAttr(CONTROL_FLOW_REACHABLE_FROM,Attributes.ListAttr.toString(entryPointList));
				}
			}
			//Tag branches reachable
			for(Node m: entryPointMethods) {
				String entryPointID = (String) m.getAttr(EntryPoints.ENTRY_POINT_ID);
				Q callGraphFromEntryPoints = Common.toQ(m).forwardOn(Query.universe().edges(XCSG.Call));
				Q branchesReachableFromEntryPoints = callGraphFromEntryPoints.nodes(XCSG.Method).contained().nodes(XCSG.ControlFlowCondition);
				AtlasSet<Node> reachableBranches = branchesReachableFromEntryPoints.eval().nodes();
				for(Node branch: reachableBranches) {
					Tags.setTag(Common.toQ(branch), CONTROL_FLOW_REACHABLE);
					if(!branch.hasAttr(CONTROL_FLOW_REACHABLE_FROM)) {
						branch.putAttr(CONTROL_FLOW_REACHABLE_FROM, Attributes.ListAttr.toString(new ArrayList<String>()));
					}
					ArrayList<String> entryPointList = Attributes.ListAttr.toListOfStrings((String) branch.getAttr(CONTROL_FLOW_REACHABLE_FROM));
					if(!entryPointList.contains(entryPointID)) {
						entryPointList.add(entryPointID);
					}
					branch.putAttr(CONTROL_FLOW_REACHABLE_FROM,Attributes.ListAttr.toString(entryPointList));
				}
			}
			
			//Tag hidden loops reachable
			for(Node m: entryPointMethods) {
				String entryPointID = (String) m.getAttr(EntryPoints.ENTRY_POINT_ID);
				Q callGraphFromEntryPoints = Common.toQ(m).forwardOn(Query.universe().edges(XCSG.Call));
				Q lambdaLoopsReachableFromEntryPoints = callGraphFromEntryPoints.nodes(XCSG.Method).contained().nodes(LambdaIdentifier.LAMBDA_LOOP);
				AtlasSet<Node> reachableHiddenLoops = lambdaLoopsReachableFromEntryPoints.eval().nodes();
				for(Node hiddenLoop: reachableHiddenLoops) {
					Tags.setTag(Common.toQ(hiddenLoop), CONTROL_FLOW_REACHABLE);
					if(!hiddenLoop.hasAttr(CONTROL_FLOW_REACHABLE_FROM)) {
						hiddenLoop.putAttr(CONTROL_FLOW_REACHABLE_FROM, Attributes.ListAttr.toString(new ArrayList<String>()));
					}
					ArrayList<String> entryPointList = Attributes.ListAttr.toListOfStrings((String) hiddenLoop.getAttr(CONTROL_FLOW_REACHABLE_FROM));
					if(!entryPointList.contains(entryPointID)) {
						entryPointList.add(entryPointID);
					}
					hiddenLoop.putAttr(CONTROL_FLOW_REACHABLE_FROM,Attributes.ListAttr.toString(entryPointList));
				}
			}
		}
		if(LoopPreferences.isDataFlowReachabilityEnabled() && !CommonQueries.isEmpty(inputs)) {
			runIndexer = true;
			//Tag reachable loops
			for(Node i: inputs.eval().nodes()) {
				// corresponding entry point method
				Node e = Common.toQ(i).containers().nodes(XCSG.Method).eval().nodes().one();
				String entryPointID = (String) e.getAttr(EntryPoints.ENTRY_POINT_ID);
				Q dfReachability = LoopReachability.getLoopReachabilityViaDataFlowView(Common.toQ(i), true).union(LoopReachability.getLoopReachabilityViaDataFlowView(Common.toQ(i), false));
				AtlasSet<Node> reachableLoops = dfReachability.nodes(XCSG.Loop).eval().nodes();
				for(Node loop: reachableLoops) {
					Tags.setTag(Common.toQ(loop), DATA_FLOW_REACHABLE);
					if(!loop.hasAttr(DATA_FLOW_REACHABLE_FROM)) {
						loop.putAttr(DATA_FLOW_REACHABLE_FROM, Attributes.ListAttr.toString(new ArrayList<String>()));
					}
					ArrayList<String> entryPointList = Attributes.ListAttr.toListOfStrings((String) loop.getAttr(DATA_FLOW_REACHABLE_FROM));
					if(!entryPointList.contains(entryPointID)) {
						entryPointList.add(entryPointID);
					}
					loop.putAttr(DATA_FLOW_REACHABLE_FROM,Attributes.ListAttr.toString(entryPointList));
				}
			}
			//Tag reachable branches
			for(Node i: inputs.eval().nodes()) {
				// corresponding entry point method
				Node e = Common.toQ(i).containers().nodes(XCSG.Method).eval().nodes().one();
				String entryPointID = (String) e.getAttr(EntryPoints.ENTRY_POINT_ID);
				Q dfReachability = LoopReachability.getBranchReachabilityViaDataFlowView(Common.toQ(i), true).union(LoopReachability.getBranchReachabilityViaDataFlowView(Common.toQ(i), false));
				AtlasSet<Node> reachableBranches = dfReachability.nodes(XCSG.ControlFlowCondition).eval().nodes();
				for(Node branch: reachableBranches) {
					Tags.setTag(Common.toQ(branch), DATA_FLOW_REACHABLE);
					if(!branch.hasAttr(DATA_FLOW_REACHABLE_FROM)) {
						branch.putAttr(DATA_FLOW_REACHABLE_FROM, Attributes.ListAttr.toString(new ArrayList<String>()));
					}
					ArrayList<String> entryPointList = Attributes.ListAttr.toListOfStrings((String) branch.getAttr(DATA_FLOW_REACHABLE_FROM));
					if(!entryPointList.contains(entryPointID)) {
						entryPointList.add(entryPointID);
					}
					branch.putAttr(DATA_FLOW_REACHABLE_FROM,Attributes.ListAttr.toString(entryPointList));
				}
			}
			
			//Tag reachable hidden loops
			for(Node i: inputs.eval().nodes()) {
				// corresponding entry point method
				Node e = Common.toQ(i).containers().nodes(XCSG.Method).eval().nodes().one();
				String entryPointID = (String) e.getAttr(EntryPoints.ENTRY_POINT_ID);
				Q dfReachability = LoopReachability.getHiddenLoopReachabilityViaDataFlowView(Common.toQ(i), true).union(LoopReachability.getHiddenLoopReachabilityViaDataFlowView(Common.toQ(i), false));
				AtlasSet<Node> reachableHiddenLoops = dfReachability.nodes(LambdaIdentifier.LAMBDA_LOOP).eval().nodes();
				for(Node hiddenLoop: reachableHiddenLoops) {
					Tags.setTag(Common.toQ(hiddenLoop), DATA_FLOW_REACHABLE);
					if(!hiddenLoop.hasAttr(DATA_FLOW_REACHABLE_FROM)) {
						hiddenLoop.putAttr(DATA_FLOW_REACHABLE_FROM, Attributes.ListAttr.toString(new ArrayList<String>()));
					}
					ArrayList<String> entryPointList = Attributes.ListAttr.toListOfStrings((String) hiddenLoop.getAttr(DATA_FLOW_REACHABLE_FROM));
					if(!entryPointList.contains(entryPointID)) {
						entryPointList.add(entryPointID);
					}
					hiddenLoop.putAttr(DATA_FLOW_REACHABLE_FROM,Attributes.ListAttr.toString(entryPointList));
				}
			}
		}
		return runIndexer;
	}
}