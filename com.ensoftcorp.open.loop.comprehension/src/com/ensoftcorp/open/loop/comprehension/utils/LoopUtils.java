package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.StandardQueries;
import com.ensoftcorp.open.commons.subsystems.Subsystems;
import com.ensoftcorp.open.java.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;

public class LoopUtils {

	public static final String NESTING_DEPTH = "NESTING_DEPTH";
	
	public static long getNestingDepth(Node loopHeader) {
		Q loopChildEdges = Common.universe().edgesTaggedWithAny(XCSG.LoopChild).retainEdges();
		Q loopRoots = loopChildEdges.roots();
		Q path = loopChildEdges.between(loopRoots, Common.toQ(loopHeader));
		long depth = path.eval().edges().size();
		return depth;
	}

	public static Q getSubsystemInteractions(Q loopHeader, String... subsystemTags) {
		Q result = Common.empty();
		
		Q targetMethodsFromLoop = getTargetMethodsFromLoop(loopHeader);
		Q subsystemMethods = Subsystems.getSubsystemContents(subsystemTags).nodesTaggedWithAny(XCSG.Method);
		result = Common.edges(XCSG.Call).between(targetMethodsFromLoop,subsystemMethods);
		Q callsiteContainers = getCallsitesInsideLoop(loopHeader).parent();
		Q summaryCallEdges = Common.edges("summary.call").betweenStep(callsiteContainers, result);
		result = result.union(summaryCallEdges);
		return result;
	}

	public static Q getMethodSetInteractions(Q loopHeader, Q methodSet) {
		Q result = Common.empty();
		
		Q targetMethodsFromLoop = getTargetMethodsFromLoop(loopHeader);
		Q subsystemMethods = methodSet.nodesTaggedWithAny(XCSG.Method);
		result = Common.edges(XCSG.Call).between(targetMethodsFromLoop,subsystemMethods);
		Q callsiteContainers = getCallsitesInsideLoop(loopHeader).parent();
		Q summaryCallEdges = Common.edges("summary.call").betweenStep(callsiteContainers, result);
		result = result.union(summaryCallEdges);
		result = result.union(getControlFlowMembers(loopHeader).nodesTaggedWithAny(XCSG.ControlFlow_Node)).induce(Common.edges(XCSG.ControlFlow_Edge));
		
//		show(LoopCallSiteStats.getMethodSetInteractions(l,stackMethods).union(LoopCallSiteStats.getControlFlowMembers(l).nodesTaggedWithAny(XCSG.ControlFlow_Node)).induce(edges(XCSG.ControlFlow_Edge)))

		return result;
	}

	public static Q getTargetMethodsFromLoop(Q loopHeader) {
		Q cs = getCallsitesInsideLoop(loopHeader);
		Q targetMethodsFromLoop = Common.empty();
		for(GraphElement c : cs.eval().nodes()) {
			targetMethodsFromLoop = targetMethodsFromLoop.union(CallSiteAnalysis.getTargetMethods(c));
		}
		return targetMethodsFromLoop;
	}


	public static Q getCallsitesInsideLoop(Q loopHeader) {
		Q lcfg = getControlFlowMembers(loopHeader);
		Q cs = lcfg.contained().nodesTaggedWithAny(XCSG.CallSite);
		return cs;
	}
	
	public static Q calculateStatementsWithConditionalCallSiteInLoopStats(Q project) {
		Q loopHeaders = project.contained().nodesTaggedWithAny(CFGNode.LOOP_HEADER);
		Q result = Common.empty();
		for(com.ensoftcorp.atlas.core.db.graph.Node loopHeader : loopHeaders.eval().nodes()) {
			Q ccsInLoop = findStatementsWithConditionalCallSitesWithinLoop(loopHeader);
			result = result.union(ccsInLoop);
		}
		return result;
	}
	
	public static Q calculateStatementsWithCallSiteInLoopStats(Q project) {
		Q loopHeaders = project.contained().nodesTaggedWithAny(CFGNode.LOOP_HEADER);
		Q result = Common.empty();
		for(com.ensoftcorp.atlas.core.db.graph.Node loopHeader : loopHeaders.eval().nodes()) {
			Q csInLoop = findStatementsWithCallSitesWithinLoop(loopHeader);
			result = result.union(csInLoop);
		}
		return result;
	}
	
	public static Q findStatementsWithConditionalCallSitesWithinLoop(com.ensoftcorp.atlas.core.db.graph.Node loopHeader) {
		Q cfMembers = getControlFlowMembers(loopHeader);
		Q cfCallsites = findStatementsWithCallSites(cfMembers);
		Q boundaryConditions = cfMembers.nodesTaggedWithAny("BOUNDARY_CONDIITON");
		Q loopHeaders = cfMembers.nodesTaggedWithAny(CFGNode.LOOP_HEADER);
		Q loopBackEdges = cfMembers.edgesTaggedWithAny("LOOP_BACK_EDGE",XCSG.ControlFlowBackEdge);
		Q cfConditions = cfMembers.nodesTaggedWithAny(XCSG.ControlFlowCondition);
		Q internalConditions = cfMembers.intersection(cfConditions.difference(boundaryConditions.union(loopHeaders)));
		
		Q conditionalCallsites = Common.empty();
		for(GraphElement cs : cfCallsites.eval().nodes()) {
			Q reverseControlFlow = cfMembers.differenceEdges(loopBackEdges).reverse(Common.toQ(cs));
			Q internalConditionsGoverningCallsite = reverseControlFlow.intersection(internalConditions);
			if(!CommonQueries.isEmpty(internalConditionsGoverningCallsite)) {
				conditionalCallsites = conditionalCallsites.union(Common.toQ(cs));
			}
		}
		return conditionalCallsites;
	}
	
	public static Q findStatementsWithCallSitesWithinLoop(com.ensoftcorp.atlas.core.db.graph.Node loopHeader) {
		Q cfMembers = getControlFlowMembers(loopHeader);
		Q cfCallsites = findStatementsWithCallSites(cfMembers);
		return cfCallsites;
	}
	
	public static Q findStatementsWithCallSites(Q cfg) {
		
		Q callsites = cfg.contained().nodesTaggedWithAll(XCSG.CallSite);
		AtlasSet<com.ensoftcorp.atlas.core.db.graph.Node> dfCallsites = callsites.eval().nodes();
		if (dfCallsites.isEmpty()) {
			return Common.empty();
		} 

		AtlasSet<GraphElement> cfgCallsites = new AtlasHashSet<GraphElement>();
		
		for (GraphElement dfCallsite : dfCallsites) {

			Q targetMethods = CallSiteAnalysis.getTargetMethods(dfCallsite);
			
			if (!targetMethods.eval().nodes().isEmpty()) {
				// NOTE: the parent of a CallSite is always a ControlFlow_Node
				Q cfgCallsite = Common.toQ(Common.toGraph(dfCallsite)).parent().nodesTaggedWithAll(XCSG.ControlFlow_Node);
				cfgCallsites.addAll(cfgCallsite.eval().nodes());
			}
		}
		
		return Common.toQ(Common.toGraph(cfgCallsites));
	}
	
	public static Q getControlFlowMembers(Q loopHeaders){
		AtlasSet<com.ensoftcorp.atlas.core.db.graph.Node> loopBodyMembers = new AtlasHashSet<com.ensoftcorp.atlas.core.db.graph.Node>();
		for(com.ensoftcorp.atlas.core.db.graph.Node loopHeader : loopHeaders.eval().nodes()){
			loopBodyMembers.addAll(getControlFlowMembers(loopHeader).contained().eval().nodes());
		}
		return Common.toQ(loopBodyMembers);
	}
	
	public static Q getControlFlowGraph(Q loopHeaders){
		Q cfEdges = Common.edges(XCSG.ControlFlow_Edge);
		return getControlFlowMembers(loopHeaders).nodesTaggedWithAny(XCSG.ControlFlow_Node).induce(cfEdges);
	}
	
	public static Q getControlFlowMembers(com.ensoftcorp.atlas.core.db.graph.Node loopHeader) {
		Q result = Common.empty();
		Q u = Common.universe();
		Q cfNodes = u.nodesTaggedWithAny(XCSG.ControlFlow_Node);
		Q cfEdges = u.edgesTaggedWithAny(XCSG.ControlFlow_Edge);
		
		String id = loopHeader.getAttr(CFGNode.LOOP_HEADER_ID).toString();
		List<String> descendants = getLoopsNestedUnder(loopHeader);
		descendants.add(id);
		for (String d : descendants) {
//			Log.info("Loop "+id+" -> "+d);
			Q descendantLoopHeader = cfNodes.selectNode(CFGNode.LOOP_HEADER_ID, new Integer(d));
			Q descendantLoopMembers = cfNodes.selectNode(CFGNode.LOOP_MEMBER_ID, new Integer(d));
			result = result.union(descendantLoopHeader).union(descendantLoopMembers);
		}
		
		return result.induce(cfEdges);
	}
	
	public static Q getControlFlowGraph(com.ensoftcorp.atlas.core.db.graph.Node loopHeader) {
		Q cfMembers = getControlFlowMembers(loopHeader);
		Q cfEdges = Common.edges(XCSG.ControlFlow_Edge);
		return cfMembers.nodesTaggedWithAny(XCSG.ControlFlow_Node).induce(cfEdges);
	}
	
	public static List<String> getLoopsNestedUnder(com.ensoftcorp.atlas.core.db.graph.Node loopHeader) {
		List<String> descendants = new ArrayList<String>();
		String headerId = loopHeader.getAttr(CFGNode.LOOP_HEADER_ID).toString();
		Forest<String> forest = buildLoopHierarchy(StandardQueries.getContainingFunction(loopHeader));
		Tree<String> treeNode = forest.findTree(headerId);
		if(treeNode == null || treeNode.isEmpty()) {
			throw new RuntimeException("Loop " + headerId + " not in the hierarchy");
		}
		ForestNode<String> rootNode = treeNode.getRoot();
		ForestNode<String> loopNode = treeNode.findNode(rootNode, headerId);
		if(loopNode == null) {
			throw new RuntimeException("Loop " + headerId + " not in the tree rooted at "+rootNode.getData());
		}
		List<ForestNode<String>> desc = loopNode.getDescendants();
		for(ForestNode<String> node : desc) {
			descendants.add(node.getData());
		}
		return descendants;
	}

	public static Forest<String> buildLoopHierarchy(GraphElement method) {
		Q cfNodes = Common.toQ(method).contained().nodesTaggedWithAny(XCSG.ControlFlow_Node);
		Map<String, ForestNode<String>> loopMap = new HashMap<String, ForestNode<String>>();
		Forest<String> forest = new Forest<String>(); 
		
		// create JGraphT graph as well
		SimpleDirectedGraph<String, DefaultEdge> jg = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

		// create one node in the loop hierarchy tree for each loop header 
		// build a map from header id to nodes in the loop hierarchy tree 
		Q loopHeaders = cfNodes.nodesTaggedWithAny(CFGNode.LOOP_HEADER);
		for(GraphElement l : loopHeaders.eval().nodes()) {
			String headerId = l.getAttr(CFGNode.LOOP_HEADER_ID).toString();
			ForestNode<String> loop = new ForestNode<String>(headerId);
			loopMap.put(headerId, loop);
			
			// add jgrapht node
			jg.addVertex(headerId);
		}
		
		
		// infer the parent-child relation between loops and build the loop hierarchy tree
		for(GraphElement l : loopHeaders.eval().nodes()) {
			String headerId = l.getAttr(CFGNode.LOOP_HEADER_ID).toString();
			
			// look up the node created for this loop 
			ForestNode<String> loop = loopMap.get(headerId);
			
			if(l.hasAttr(CFGNode.LOOP_MEMBER_ID)) {
				// if loop is a member of another loop, enforce the parent-child relationship
				String memberOf = l.getAttr(CFGNode.LOOP_MEMBER_ID).toString();
				if(!loopMap.containsKey(memberOf)) {
					throw new RuntimeException("Loop hierarchy is broken - a child has no parent");
				}
				
				// look up node for the loop the current loop header is a member of
				ForestNode<String> parent = loopMap.get(memberOf);
				
				parent.addChild(loop);
				
				// add jgrapht edge
				jg.addEdge(headerId, memberOf);
			} else {
				// if loop is not member of any other loop, this is a top level (non-nested) loop
				Tree<String> tree = new Tree<String>(loop);
				forest.addTree(tree);
			}
		}
		
		/* Commenting this for now.. no need to save DOT files, eventually this info will be used for a smart view.
		// save the loop hierarchy as a graph to a DOT file for quick visualization using xdot or graphviz
		String fileName = "/home/gsanthan/Desktop/STAC/loopstats-all/"+Common.toQ(method).parent().eval().nodes().getFirst().getAttr(XCSG.name)+"-"+method.getAttr(XCSG.name)+"-loops.dot";
		try {
			saveDOTGraph(new File(fileName),jg);
		} catch (IOException ie) {
			Log.error("Exception when saving loop hierarchy for "+fileName, ie);
		}*/
		return forest;
	}

}
