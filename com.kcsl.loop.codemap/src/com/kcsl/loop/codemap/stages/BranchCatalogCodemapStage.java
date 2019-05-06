package com.kcsl.loop.codemap.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasNodeHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.subsystems.Subsystem;
import com.ensoftcorp.open.commons.subsystems.Subsystems;
import com.ensoftcorp.open.java.commons.analysis.CommonQueries;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.kcsl.loop.codemap.log.Log;
import com.kcsl.loop.preferences.LoopPreferences;
import com.kcsl.loop.util.Attributes;
import com.kcsl.loop.util.Tags;

/**
 * Builds the PDGs for each function
 * 
 * @author Ben Holland
 */
public class BranchCatalogCodemapStage extends PrioritizedCodemapStage {

	public static final String BRANCH_GOVERNS_EVENT = "BRANCH_GOVERNS_EVENT";

	public static final String CALLSITE_ID = "CALLSITE_ID";

	public static final String BRANCH_ID = "BRANCH_ID";

	/**
	 * The unique identifier for the Branch catalog codemap stage
	 */
	public static final String IDENTIFIER = "com.kcsl.loop.codemap.branchcatalog";
	
	private int branchIDCount = 0;
	private int eventIDCount = 0;
	
	@Override
	public String getDisplayName() {
		return "Branch Catalog";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{LoopCatalogCodemapStage.IDENTIFIER}; 
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		boolean runIndexer = LoopPreferences.isCatalogBranchesEnabled();
		if(runIndexer){
			Log.info("Cataloging branches...");
			map = new HashMap<Subsystem,Q>();
			assignBranchIDs(Query.universe().nodes(XCSG.ControlFlowCondition));
			Log.debug("Assigned branch IDs");
			computeSubsystemCallsiteDataFlowNodes();
			assignSubsystemCallsiteIDs(CALLSITE_ID);
			Log.debug("Assigned callsite IDs");
			analyzeBranchesAndEvents();
			Log.debug("Analyzed branches governing events");
		}
		return runIndexer;
	}
	
	public void assignBranchIDs(Q branches) {
		for(Node branch: branches.eval().nodes()) {
			branch.putAttr(BRANCH_ID,""+(branchIDCount));
			branchIDCount++;
		}
	}
	
	public Map<Subsystem,Q> computeSubsystemCallsiteDataFlowNodes() {
//		long t = System.currentTimeMillis();
//		long tTag = 0L;
		if(map.size() <= 0) {
			map = new HashMap<Subsystem,Q>();
			for(Subsystem subsystem: Subsystems.getRegisteredSubsystems()) {
				Q ssCallsiteDataFlowNodes = CallSiteAnalysis.getCallSites(Subsystems.getSubsystemContents(subsystem));
				Log.debug("# Subsystem callsites for "+subsystem.getTag()+": "+(CommonQueries.isEmpty(ssCallsiteDataFlowNodes)?""+0:ssCallsiteDataFlowNodes.eval().nodes().size()));
				map.put(subsystem, ssCallsiteDataFlowNodes);
//				long tTemp = System.currentTimeMillis();
				Tags.setTag(ssCallsiteDataFlowNodes, subsystem.getTag()+"_CALLSITE");
//				tTag += System.currentTimeMillis() - tTemp;
			}
		}
		Log.debug("Total # Subsystem callsites computed: " + map.values().size());
		return map;
	}
	
	public static Map<Subsystem,Q> map = new HashMap<Subsystem,Q>();
	
	public static Map<Subsystem, Q> getMap() {
		return map;
	}

	public void assignSubsystemCallsiteIDs(String eventIdAttributeName) {
		for(Q ssCallsites: map.values()) {
			// ensure this callsite's parent is a control flow node
			assert (ssCallsites.parent()!=null && ssCallsites.parent().eval().nodes().one().taggedWith(XCSG.ControlFlow_Node));
			assignEventIDs(ssCallsites.parent().eval().nodes(),eventIdAttributeName);	
		}
	}
	
	public void assignEventIDs(AtlasSet<Node> events, String eventIdAttributeName) {
		for(Node event: events) {
			event.putAttr(eventIdAttributeName,""+(eventIDCount));
			eventIDCount++;
		}
	}
	

	public static void analyzeBranchesAndEvents() {
//		if(!branchesAnalyzed) {
			analyzeBranchesGoverningEvents(Query.universe().nodes(XCSG.Loop), "LoopsGoverned", "GoverningBranches", CFGNode.LOOP_HEADER_ID);
			for(Entry<Subsystem,Q> subsystemCallsites: BranchCatalogCodemapStage.map.entrySet()) {
				Q events = subsystemCallsites.getValue().parent();
				Log.debug("# events for "+subsystemCallsites.getKey().getTag()+": "+events.eval().nodes().size());
				analyzeBranchesGoverningEvents(events, subsystemCallsites.getKey().getTag()+"_CallsitesGoverned", "GoverningBranches", "CALLSITE_ID");
			}
//			branchesAnalyzed = true;
			Log.debug("Completed analyzing branches and events");
//		} else {
//			Log.debug("Already analyzed branches and events");
//		}
	}
	
	public static void analyzeBranchesGoverningEvents(Q events, String eventsGovernedByBranchAttributeName, String branchesGoverningEventsAttributeName, String eventIDAttributeName) {
		Log.debug("Analyzing branches governing "+ events.eval().nodes().size() + " events with attribute " + eventIDAttributeName); //$NON-NLS-1$ //$NON-NLS-2$
		
		if(events == null || CommonQueries.isEmpty(events)) {
			return;
		}
		int count = 0;
		int countEdges = 0;
		AtlasSet<Node> eventSet = new AtlasHashSet<Node>(events.eval().nodes());
		Q backEdges = Query.universe().edges(XCSG.ControlFlowBackEdge);
		for(Node event: eventSet) {
			assert event.hasAttr(eventIDAttributeName) && event.getAttr(eventIDAttributeName)!=null;
			Q excfgWithoutBackEdges = Query.resolve(null, CommonQueries.excfg(CommonQueries.getContainingMethod(event)).differenceEdges(backEdges));
			
			if(!event.hasAttr(branchesGoverningEventsAttributeName)) {
				event.putAttr(branchesGoverningEventsAttributeName,Attributes.ListAttr.toString(new ArrayList<String>()));
			}
			
			AtlasSet<Node> branchesGoverningEvent = new AtlasNodeHashSet();
			
			Q _branchesInCFG = excfgWithoutBackEdges.nodes(XCSG.ControlFlowCondition).difference(excfgWithoutBackEdges.nodes(XCSG.Loop));
			AtlasSet<Node> branchesInCFG = new AtlasHashSet<Node>(_branchesInCFG.eval().nodes());
			for(Node branch: branchesInCFG) {
				assert branch.hasAttr(BRANCH_ID) && branch.getAttr(BRANCH_ID)!=null;
//				boolean dom = branchGovernsEvent(Common.toQ(branch), Common.toQ(event), excfgWithoutBackEdges);
				
			
				boolean governance = branchGovernsEvent(Common.toQ(branch), Common.toQ(event), excfgWithoutBackEdges);
//				Q governanceGraph =  excfgWithoutBackEdges.between(Common.toQ(branch), exits, Common.toQ(event));
//						//branchGovernsEvent(Common.toQ(branch), Common.toQ(event), excfgWithoutBackEdges);
//				boolean governance = !CommonQueries.isEmpty(governanceGraph);
				if(governance) {
					count++;
					
					assert branch.taggedWith(XCSG.ControlFlowCondition);
					assert event.taggedWith(XCSG.Loop) || (Common.toQ(event).contained().nodes(XCSG.CallSite).eval().nodes().size()>0);
					
					branchesGoverningEvent.add(branch);
					Q branchesGoverningEventEdges = Query.universe().edges(BRANCH_GOVERNS_EVENT);
					Edge branchGoverningEventEdge = branchesGoverningEventEdges.betweenStep(Common.toQ(branch), Common.toQ(event)).eval().edges().one();
					if(branchGoverningEventEdge == null){
						branchGoverningEventEdge = Graph.U.createEdge(branch, event);
						branchGoverningEventEdge.tag(BRANCH_GOVERNS_EVENT);
						branchGoverningEventEdge.putAttr(XCSG.name, BRANCH_GOVERNS_EVENT);
						countEdges++;
					}
					
					if(!branch.hasAttr(eventsGovernedByBranchAttributeName)) {
						branch.putAttr(eventsGovernedByBranchAttributeName,Attributes.ListAttr.toString(new ArrayList<String>()));
					}
					
					if(event.getAttr(eventIDAttributeName) == null) {
						Log.warning("No "+eventIDAttributeName +" for "+event+"; expect null pointers ahead");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					}
					String eventID = event.getAttr(eventIDAttributeName).toString();
					List<String> eventsGovernedList = Attributes.ListAttr.toListOfStrings((String)branch.getAttr(eventsGovernedByBranchAttributeName));
					if(!eventsGovernedList.contains(eventID)) {
						eventsGovernedList.add(eventID);
					}
					branch.putAttr(eventsGovernedByBranchAttributeName,Attributes.ListAttr.toString(eventsGovernedList));
					
					List<String> governingBranchesList = Attributes.ListAttr.toListOfStrings((String)event.getAttr(branchesGoverningEventsAttributeName));
					String branchID = branch.getAttr(BRANCH_ID).toString();
					if(!governingBranchesList.contains(branchID)) {
						governingBranchesList.add(branchID);
					}
					event.putAttr(branchesGoverningEventsAttributeName,Attributes.ListAttr.toString(governingBranchesList));
				}
			}
		}
		
		Log.debug(count + " (branch,event) pairs were detected; " + countEdges + " edges were added"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Computes whether a branch governs an event, i.e. the branch dominates the event and
	 * there exists a path which does not include the event.
	 * <p>
	 * Alternatively, computes whether a branch would be retained as relevant in a PCG
	 * of the given event.
	 * 
	 * TODO: [jdm] this has some scaling issues in practice; revisit callers and do it in bulk
	 * 
	 * @param branch
	 * @param event
	 * @param localCFGwithoutBackEdges CFG without back edges
	 * @return
	 */
	public static boolean branchGovernsEvent(Q branch, Q event, Q localCFGwithoutBackEdges) {
		// assert context.edges(XCSG.ControlFlowBackEdge).eval().edges().size() == 0

		Q entry = localCFGwithoutBackEdges.roots();
		
		// dominance in the sense that all paths to event must pass through the branch
		boolean dominates = CommonQueries.isEmpty(localCFGwithoutBackEdges.between(entry, event, branch));
		if (!dominates)
			return false;
		
		Q exits = localCFGwithoutBackEdges.leaves();
		if(CommonQueries.isEmpty(exits)) {
			Log.warning("Unable to find exits for CFG when computing governance of event by a branch"); //$NON-NLS-1$
		}
		
		// is there a path out of branch which does not include the event and which reaches the exit?
		boolean optional = !CommonQueries.isEmpty(localCFGwithoutBackEdges.between(branch, exits, event));
		
		boolean governance = dominates && optional;
		return governance;
	}
}