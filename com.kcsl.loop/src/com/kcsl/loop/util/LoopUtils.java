package com.kcsl.loop.util;

import java.util.ArrayList;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.subsystems.Subsystems;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGEdge;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.kcsl.loop.log.Log;

public class LoopUtils {

	public static final String NESTING_DEPTH = "NESTING_DEPTH";

	static long edgeCount = 0;
	static long skippedEdgeCount = 0;
	
	public static long getNestingDepth(Node loopHeader) {
		Q loopChildEdges = Query.universe().edges(XCSG.LoopChild).retainEdges();
		Q loopRoots = loopChildEdges.roots();
		Q path = loopChildEdges.between(loopRoots, Common.toQ(loopHeader));
		long depth = path.eval().edges().size();
		return depth;
	}

	public static Q getSubsystemInteractions(Q loopHeader, String... subsystemTags) {
		Q result = Common.empty();
		
		Q targetMethodsFromLoop = getTargetMethodsFromLoop(loopHeader);
		Q subsystemMethods = Subsystems.getSubsystemContents(subsystemTags).nodes(XCSG.Method);
		result = Common.edges(XCSG.Call).between(targetMethodsFromLoop,subsystemMethods);
		Q callsiteContainers = getCallsitesInsideLoop(loopHeader).parent();
		Q summaryCallEdges = Common.edges(Attr.Edge.PER_CONTROL_FLOW).betweenStep(callsiteContainers, result);
		result = result.union(summaryCallEdges);
		return result;
	}

	public static Q getTargetMethodsFromLoop(Q loopHeader) {
		Q cs = getCallsitesInsideLoop(loopHeader);
		Q targetMethodsFromLoop = Common.empty();
		for(Node c : cs.eval().nodes()) {
			targetMethodsFromLoop = targetMethodsFromLoop.union(CallSiteAnalysis.getTargets(Common.toQ(c)));
		}
		return targetMethodsFromLoop;
	}


	public static Q getCallsitesInsideLoop(Q loopHeader) {
		Q lcfg = getControlFlowMembers(loopHeader);
		Q cs = lcfg.contained().nodes(XCSG.CallSite);
		return cs;
	}
	
	public static Q calculateStatementsWithConditionalCallSiteInLoopStats(Q project) {
		Q loopHeaders = project.contained().nodes(XCSG.Loop);
		Q result = Common.empty();
		for(Node loopHeader : loopHeaders.eval().nodes()) {
			Q ccsInLoop = findStatementsWithConditionalCallSitesWithinLoop(loopHeader);
			result = result.union(ccsInLoop);
		}
		return result;
	}
	
	public static Q calculateStatementsWithCallSiteInLoopStats(Q project) {
		Q loopHeaders = project.contained().nodes(XCSG.Loop);
		Q result = Common.empty();
		for(Node loopHeader : loopHeaders.eval().nodes()) {
			Q csInLoop = findStatementsWithCallSitesWithinLoop(loopHeader);
			result = result.union(csInLoop);
		}
		return result;
	}
	
	public static Q findStatementsWithConditionalCallSitesWithinLoop(Node loopHeader) {
		Q cfMembers = getControlFlowMembers(loopHeader);
		Q cfCallsites = findStatementsWithCallSites(cfMembers);
		Q boundaryConditions = cfMembers.nodes("BOUNDARY_CONDIITON");
		Q loopHeaders = cfMembers.nodes(XCSG.Loop);
		Q loopBackEdges = cfMembers.edges(XCSG.ControlFlowBackEdge);
		Q cfConditions = cfMembers.nodes(XCSG.ControlFlowCondition);
		Q internalConditions = cfMembers.intersection(cfConditions.difference(boundaryConditions.union(loopHeaders)));
		
		Q conditionalCallsites = Common.empty();
		for(Node cs : cfCallsites.eval().nodes()) {
			Q reverseControlFlow = cfMembers.differenceEdges(loopBackEdges).reverse(Common.toQ(cs));
			Q internalConditionsGoverningCallsite = reverseControlFlow.intersection(internalConditions);
			if(!CommonQueries.isEmpty(internalConditionsGoverningCallsite)) {
				conditionalCallsites = conditionalCallsites.union(Common.toQ(cs));
			}
		}
		return conditionalCallsites;
	}
	
	public static Q findStatementsWithCallSitesWithinLoop(Node loopHeader) {
		Q cfMembers = getControlFlowMembers(loopHeader);
		Q cfCallsites = findStatementsWithCallSites(cfMembers);
		return cfCallsites;
	}
	
	public static Q findStatementsWithCallSites(Q cfg) {
		
		Q callsites = cfg.contained().nodesTaggedWithAll(XCSG.CallSite);
		AtlasSet<Node> dfCallsites = callsites.eval().nodes();
		if (dfCallsites.isEmpty()) {
			return Common.empty();
		} 

		AtlasSet<Node> cfgCallsites = new AtlasHashSet<Node>();
		
		for (Node dfCallsite : dfCallsites) {

			Q targetMethods = CallSiteAnalysis.getTargets(Common.toQ(dfCallsite));
			
			if (!targetMethods.eval().nodes().isEmpty()) {
				// NOTE: the parent of a CallSite is always a ControlFlow_Node
				Q cfgCallsite = Common.toQ(Common.toGraph(dfCallsite)).parent().nodesTaggedWithAll(XCSG.ControlFlow_Node);
				cfgCallsites.addAll(cfgCallsite.eval().nodes());
			}
		}
		
		return Common.toQ(Common.toGraph(cfgCallsites));
	}
	
	public static Q getControlFlowMembers(Q loopHeaders){
		AtlasSet<Node> loopBodyMembers = new AtlasHashSet<Node>();
		for(Node loopHeader : loopHeaders.eval().nodes()){
			loopBodyMembers.addAll(Common.edges(XCSG.ControlFlow_Edge).between(Common.toQ(loopHeader), Common.toQ(loopHeader)).contained().eval().nodes());
		}
		return Common.toQ(loopBodyMembers);
	}
	
	public static Q getControlFlowGraph(Q loopHeaders) {
		Q cfEdges = Common.edges(XCSG.ControlFlow_Edge);
		return getControlFlowMembers(loopHeaders).nodes(XCSG.ControlFlow_Node).induce(cfEdges);
	}

	
	public static Q getControlFlowMembers(Node loopHeader) {
		return Common.edges(XCSG.ControlFlow_Edge).between(Common.toQ(loopHeader), Common.toQ(loopHeader));
	}
	
	
	public static Q getControlFlowGraph(Node loopHeader) {
		return Common.edges(XCSG.ControlFlow_Edge,XCSG.ExceptionalControlFlow_Edge).between(Common.toQ(loopHeader), Common.toQ(loopHeader));
	}
	
	public static List<String> getLoopsContainingControlFlowNode(Q cfNode) {
		// find all loops containing the callsite (consider loop nesting) 
		Q loopContainers = cfNode.reverseOn(Query.universe().edges(XCSG.LoopChild));
		List<String> headerIds = new ArrayList<String>();
		if(loopContainers != null && !CommonQueries.isEmpty(loopContainers)) {
			for(Node loopContainer: loopContainers.eval().nodes()) {
				String headerId = null;
				if(loopContainer != null) {
					Object h = loopContainer.getAttr(CFGNode.LOOP_HEADER_ID);
					if(h != null) {
						headerId = h.toString();
					}
				}
				if(headerId != null) {
					headerIds.add(headerId);
				}
			}
		}
		return headerIds;
	}

	public static Q analyzeInterproceduralLoopNestingChildren() {
		edgeCount = 0;
		skippedEdgeCount = 0;
		
		AtlasSet<Edge> interproceduralLoopNestingEdges = new AtlasHashSet<Edge>();
		
		Q loopHeaders = Query.universe().nodes(XCSG.Loop);
		AtlasSet<Node> loopHeaderNodes = new AtlasHashSet<Node>(loopHeaders.eval().nodes());
		for(Node loopHeaderNode: loopHeaderNodes) {
			Q loopCallsites = LoopUtils.getControlFlowGraph(loopHeaderNode).contained().nodes(XCSG.CallSite);
			AtlasSet<Node> loopCallsiteNodes = new AtlasHashSet<Node>(loopCallsites.eval().nodes());
			for(Node loopCallsiteNode: loopCallsiteNodes) {
				AtlasSet<Edge> addedEdges = processLoopCallsite(loopHeaderNode, loopCallsiteNode);
				interproceduralLoopNestingEdges.addAll(addedEdges);
			}
		}
		
		Log.info("Added "+edgeCount+" \"" + CFGEdge.INTERPROCEDURAL_LOOP_CHILD + "\" edges to the universe; skipped adding "+skippedEdgeCount+" edges that already exist.");
		return Common.toQ(interproceduralLoopNestingEdges);
	}
	
	private static AtlasSet<Edge> processLoopCallsite(Node loopHeaderNode, Node loopCallsiteNode) {
		Q method = Common.toQ(CommonQueries.getContainingFunction(loopCallsiteNode));
		Q targetMethods = CallSiteAnalysis.getTargets(Common.toQ(loopCallsiteNode));
		Q callGraph = Query.universe().edges(XCSG.Call);
		Q callGraphFromTargets = targetMethods.forwardOn(callGraph);
		Q methodSuccessors = method.successorsOn(Query.universe().edges(XCSG.Call));
		Q edgesToRemove = callGraph.betweenStep(method, methodSuccessors);
		Q callGraphToUse = callGraphFromTargets.differenceEdges(edgesToRemove);
		
		Q methodsContainingLoops = CommonQueries.getContainingFunctions(callGraphToUse.contained().nodes(XCSG.Loop));
		
		//Add edge from a loop A to each loop B such that: 
		//(a) A has a callsite, 
		//(b) B has a loop and 
		//(c) B is the first reachable method in the call graph forward from the targets of the callsite in A
		AtlasSet<Edge> edgesAddedForCallsiteInLoop = new AtlasHashSet<Edge>();
		for(Node m: methodsContainingLoops.eval().nodes()) {
			Q firstReachability = callGraphToUse.between(targetMethods, Common.toQ(m), methodsContainingLoops.difference(Common.toQ(m)));
			if(!CommonQueries.isEmpty(firstReachability)) {
				AtlasSet<Edge> addedEdges = createEdgesFromLoopToRootLoopsInTargetMethod(loopHeaderNode, m);
				edgesAddedForCallsiteInLoop.addAll(addedEdges);
				Log.debug(m.getAttr(XCSG.name)+" is first reachable from "+method.eval().nodes().one().getAttr(XCSG.name)+" in the LCG; added "+addedEdges.size()+" edges");
			}
		}
		
		return edgesAddedForCallsiteInLoop;
	}
	
	public static AtlasSet<Edge> createEdgesFromLoopToRootLoopsInTargetMethod(Node loopHeaderNode, Node targetMethodNode) {
		AtlasSet<Edge> createdEdges = new AtlasHashSet<Edge>();
		Q loopHeadersInTargetMethod = Common.toQ(targetMethodNode).contained().nodes(XCSG.Loop);
		Q loopNestingInTargetMethod = loopHeadersInTargetMethod.induce(Query.universe().edges(XCSG.LoopChild));
		AtlasSet<Node> targetMethodLoopHeaderRootNodes = new AtlasHashSet<Node>(loopNestingInTargetMethod.roots().eval().nodes());
		for(Node targetMethodLoopHeaderRootNode: targetMethodLoopHeaderRootNodes) {
			//Ensure the two loops are in different methods (this can be violated if there is a recursion in the LCG)
			if(CommonQueries.getContainingFunction(loopHeaderNode).equals(CommonQueries.getContainingFunction(targetMethodLoopHeaderRootNode))) {
				continue;
			}
			//Ensure there is no edge already between the two loops
			Q betweenStep = Query.universe().edges(CFGEdge.INTERPROCEDURAL_LOOP_CHILD).betweenStep(Common.toQ(loopHeaderNode), Common.toQ(targetMethodLoopHeaderRootNode));
			if(CommonQueries.isEmpty(betweenStep)) {
				Edge interproceduralLoopNestingEdge = Graph.U.createEdge(loopHeaderNode, targetMethodLoopHeaderRootNode);
				interproceduralLoopNestingEdge.tag(CFGEdge.INTERPROCEDURAL_LOOP_CHILD);
				interproceduralLoopNestingEdge.putAttr(XCSG.name, CFGEdge.INTERPROCEDURAL_LOOP_CHILD);
				createdEdges.add(interproceduralLoopNestingEdge);
				edgeCount++;
			} else {
				skippedEdgeCount++;
			}
		}
		return createdEdges;
	}
	
}
