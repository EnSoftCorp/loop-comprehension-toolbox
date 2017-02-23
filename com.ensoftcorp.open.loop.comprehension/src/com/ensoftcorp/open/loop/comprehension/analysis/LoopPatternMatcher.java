package com.ensoftcorp.open.loop.comprehension.analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// import org.rulersoftware.taint.TaintOverlay;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.core.xcsg.XCSG.Java;
import com.ensoftcorp.open.commons.analysis.StandardQueries;
import com.ensoftcorp.open.java.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.log.Log;
import com.ensoftcorp.open.loop.comprehension.taint.TaintOverlay;
import com.ensoftcorp.open.loop.comprehension.utils.Explorer;
import com.ensoftcorp.open.loop.comprehension.utils.IteratorPatternSignatures;
import com.ensoftcorp.open.loop.comprehension.utils.LoopPatternConstants;
import com.ensoftcorp.open.loop.comprehension.utils.LoopUtils;
import com.ensoftcorp.open.loop.comprehension.utils.MethodSignatureMatcher;
import com.ensoftcorp.open.loop.comprehension.utils.MethodSignatures;
import com.ensoftcorp.open.loop.comprehension.utils.Tags;

public class LoopPatternMatcher {
	

	public static List<String> getComparatorTags(){
		return  Arrays.asList(new String[]{XCSG.EqualTo,XCSG.NotEqualTo,XCSG.LessThan,XCSG.LessThanOrEqualTo,XCSG.GreaterThan,XCSG.GreaterThanOrEqualTo,XCSG.Jimple.Comparison,XCSG.Jimple.ComparisonDefaultGreaterThan,XCSG.Jimple.ComparisonDefaultLessThan});
	}
	
	public static Q getTaintWithoutCycleEdges(){
		return Common.edges(TaintOverlay.Taint).differenceEdges(Common.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT));
	}
	
	public static void tagMultiple(Q loopHeaders) {
		for(Node loopHeader : loopHeaders.eval().nodes()) {
			monotonicity(Common.toQ(loopHeader));
		}
	}
	
	
	/* Method to add immutability stuff later
	 * public static Q newTaintGraph(Q loopHeader) {
		
		// Includes Immutability Results
		// Assumption : Immutability analysis was run
		
		if(!loopHeader.eval().nodes().one().taggedWith(CFGNode.LOOP_HEADER)) {
			throw new RuntimeException("Not a loop header");
		}
		
		//Terminating Conditions
		Q tc = TerminatingConditions.findTerminatingConditionsForLoopHeader(loopHeader);
		
		// all taints without the cycle causing edges
		Q taints = Common.universe().edgesTaggedWithAny(TaintOverlay.Taint).differenceEdges(Common.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT));

		// method containing loop header
		Q method = StandardQueries.getContainingFunctions(loopHeader);

		// method contents
		Q methodContents = StandardQueries.localDeclarations(method);

		// local taints influencing loop termination (taint events)
		Q conditions = tc.children().nodesTaggedWithAny(XCSG.DataFlowCondition);
		Q loopTerminationTaints = conditions.reverseOn(taints).intersection(methodContents);
				
		// loop termination taints
		loopTerminationTaintGraph = loopTerminationTaints.induce(taints);
		
		
		// callsites in Taint graph
		Q callsites = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.CallSite);
		
		// forwarding taint
		Q missing = callsites.forwardOn(Common.edges(XCSG.LocalDataFlow));
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		// pulling in missed induction variable assignments
		missing = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.IdentityPass).parent().children().nodesTaggedWithAll(XCSG.Assignment);
		
		missing = Common.edges(TaintOverlay.Taint).between(loopTerminationTaintGraph, missing);
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		//DisplayUtil.displayGraph(loopTerminationTaintGraph.eval());
		
		return loopTerminationTaintGraph;
						
	}*/
	
	public static void monotonicity(Q loopHeader) {
		
		if(!loopHeader.eval().nodes().one().taggedWith(CFGNode.LOOP_HEADER)) {
			throw new RuntimeException("Not a loop header");
		}
		
		OperationAnalyzer.applyMonotonicityTagsOnControlFlow(loopHeader);
		
		Q taintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
		Q roots = taintGraph.roots();
		Q condition = taintGraph.nodesTaggedWithAny(XCSG.DataFlowCondition);
		
		// induction variables
		Q inductionVariables = taintGraph.nodesTaggedWithAny(XCSG.Assignment);
		//DisplayUtil.displayGraph(getTaintGraph(loopHeader).eval());
		
		AtlasSet<Node> inductionNodes = inductionVariables.parent().eval().nodes();
		
		int increments = 0;
		int decrements = 0;
		
		for(Node inductionNode : inductionNodes) {
			if(inductionNode.taggedWith(LoopPatternConstants.INCREMENT_OPERATOR)) {
				increments++;
			}
			if(inductionNode.taggedWith(LoopPatternConstants.DECREMENT_OPERATOR)) {
				decrements++;
			}
		}
		// Log.info("#Increase: " + increments);
		// Log.info("#Decrease: " + decrements);
		if((increments > 0 && decrements == 0) || (decrements > 0 && increments == 0)) {
			Tags.setTag(loopHeader, LoopPatternConstants.MONOTONIC_LOOP);
		}
		
		if(!loopHeader.eval().nodes().one().taggedWith(LoopPatternConstants.MONOTONIC_LOOP)) {
			int flag = 0,result;
			for(Node root : roots.eval().nodes()){
				Q path = taintGraph.between(Common.toQ(root), condition);
				result = checkMonotonicity(path);
				if(result == -1) {
					return;
				}
				else {
					flag = flag + result;
				}
			}
			if(flag > 0) {
				Tags.setTag(loopHeader, LoopPatternConstants.MONOTONIC_LOOP);
			}
		}
		return;
	}
	
	private static int checkMonotonicity(Q path) {
		Q inductionVariables = path.nodesTaggedWithAny(XCSG.Assignment);
		AtlasSet<Node> inductionNodes = inductionVariables.parent().eval().nodes();
		
		int increments = 0;
		int decrements = 0;
		
		for(Node inductionNode : inductionNodes) {
			if(inductionNode.taggedWith(LoopPatternConstants.INCREMENT_OPERATOR)) {
				increments++;
			}
			if(inductionNode.taggedWith(LoopPatternConstants.DECREMENT_OPERATOR)) {
				decrements++;
			}
		}
		// Log.info("#Increase: " + increments);
		// Log.info("#Decrease: " + decrements);
		if((increments > 0 && decrements > 0)) {
			return -1;
		}
		else if(increments == 0 && decrements == 0) {
			return 0;
		}
		else {
			return 1;
		}
	}
	
	public static Q getSuccessor(Q dfGraph, Q dfNode) {
		return dfGraph.edgesTaggedWithAny(TaintOverlay.Taint).successors(dfNode);
	}
	
	public static Q getPredeccessor(Q dfGraph, Q dfNode) {
		return dfGraph.edgesTaggedWithAny(TaintOverlay.Taint).predecessors(dfNode);
	}
	
	public static boolean taggedWithAny(Node n, List<String> tags) {
		for(String tag : tags) {
			if(n.taggedWith(tag)) {
				return true;
			}
		}
		
		return false;
	}
	
	// Calculates bounds on a loop. Assumption : There is only one boundary condition. For multiple boundary conditions we need convergence analysis
	public static String[] bounds(Q loopHeader) throws NullPointerException {
		
		if(!loopHeader.eval().nodes().one().taggedWith(CFGNode.LOOP_HEADER)) {
			throw new RuntimeException("Not a loop header");
		}

		Log.info("Start bound analysis for loop header " + loopHeader.eval().nodes().one().getAttr(CFGNode.LOOP_HEADER_ID));
		
		// stores result to return it stats
		String[] bounds = new String[2];
		bounds[0] = bounds[1] = "";
 		// improved tagging
		OperationAnalyzer.applyMonotonicityTagsOnDataFlow(loopHeader);
		
		// check monotonicity
		monotonicity(loopHeader);
		
		if(loopHeader.eval().nodes().one().taggedWith(LoopPatternConstants.MONOTONIC_LOOP)) {
			return monotonicPatterns(loopHeader,bounds);
		}
		else{
			return nonMonotonicPatterns(loopHeader);
		}
	}
		
	private static String[] monotonicPatterns(Q loopHeader, String[] bounds) { 
		// Pattern tags list
/*		String pattern1_1 = "Pattern 1.1";
		String pattern2_1 = "Pattern 2.1";
		String pattern3_1 = "Pattern 3.1";
//		String pattern3_1_1 = "Pattern 3.1.1";
//		String pattern1_2 = "Pattern 1.2";
		String pattern2_2 = "Pattern 2.2";
		String pattern3_2 = "Pattern 3.2";
//		String pattern1_3 = "Pattern 1.3";
		String pattern2_3 = "Pattern 2.3";
		String pattern3_3 = "Pattern 3.3";
		String pattern1_4 = "Pattern 1.4";
		String pattern2_4 = "Pattern 2.4";
		String pattern3_4 = "Pattern 3.4";
		String io = "IO == ";
		String api = "API being compared";
		String patternx = "Unknown and callsite";*/
		
		// taint graph
		Q loopTerminationTaintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
		
		if(loopTerminationTaintGraph.edgesTaggedWithAny(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT).eval().edges().size() != 0) {
			// method containing loop header
			Q method = StandardQueries.getContainingFunctions(loopHeader);
			
			// method contents
			Q methodContents = StandardQueries.localDeclarations(method);
			
			// getting rid of cycles if any left (there is a possibility)
			Q leaves = loopTerminationTaintGraph.leaves();
	
			Q taintWithoutCycleEdges = getTaintWithoutCycleEdges();
			loopTerminationTaintGraph = leaves.reverseOn(taintWithoutCycleEdges).intersection(methodContents).induce(taintWithoutCycleEdges);
			//DisplayUtil.displayGraph(taintGraph.eval());
		}
		// taint roots
		Q taintRoots = loopTerminationTaintGraph.roots();
		
		// loop cfg
		Q loopCfg = LoopUtils.getControlFlowMembers(loopHeader.eval().nodes().one());
		AtlasSet<Node> loopCfgNodes = loopCfg.eval().nodes();
		
		GraphElement lowerBound = null, upperBound = null;
		
		for(Node root : taintRoots.eval().nodes()) {
			
			// root is literal - definitely one of the bounds
			if(root.taggedWith(XCSG.Literal)) {
				Node successor = getSuccessor(loopTerminationTaintGraph, Common.toQ(root)).eval().nodes().one();
				if(successor == null) {
					continue;
				}
				if(successor.taggedWith(XCSG.Assignment)) {
					if(!loopCfgNodes.contains(Common.toQ(successor).parent().eval().nodes().one())) {
						lowerBound = root;
					}
				}
				if(taggedWithAny(successor, getComparatorTags())) {
					// Log.info("found comparator");
					bounds = ioPattern(loopHeader, taintRoots, root, "literal");
				}
				if(Common.toQ(successor).parent().eval().nodes().one().taggedWith(BoundaryConditions.BOUNDARY_CONDITION)) {
					upperBound = root;
				}
			}
			
			// root is array - upper bound has to be arraylength
			else if(root.taggedWith(XCSG.ArrayInstantiation)) {
				AtlasSet<Node> associatedConditions = Common.toQ(root).forwardOn(getTaintWithoutCycleEdges()).leaves().eval().nodes();
				for(Node condition : associatedConditions) {
					AtlasSet<Node> parents = Common.toQ(condition).parent().eval().nodes();
					if(parents.isEmpty()){
						Log.warning("Condition " + condition.address().toAddressString() + " did not have a parent.");
					}
					for(Node parent : parents){
						if(parent.taggedWith(BoundaryConditions.BOUNDARY_CONDITION)) {
							upperBound = loopTerminationTaintGraph.between(Common.toQ(root),Common.toQ(condition)).nodesTaggedWithAny(XCSG.Java.ArrayLengthOperator).eval().nodes().one();
						}
					}
				}
				
			}
			
			// root is a collection or a string - bound has to be length of collection determined by its iterator
			else if(root.taggedWith(XCSG.Instantiation)) {
				/* we first must get the path between condition and collection instantiation.
				   this will have both variables - collection and iterator*/
				AtlasSet<Node> condition = new AtlasHashSet<Node>(loopTerminationTaintGraph.leaves().nodesTaggedWithAny(XCSG.DataFlowCondition).eval().nodes());
				if(condition.size() != 1) {
					//getting rid of non-boundary conditions
					AtlasSet<Node> conditions = new AtlasHashSet<Node>(condition);
					for(Node c : conditions) {
						if(!Common.toQ(c).parent().eval().nodes().one().taggedWith(BoundaryConditions.BOUNDARY_CONDITION)) {
							condition.remove(c);
						}
					}
				}
				Q path = loopTerminationTaintGraph.between(Common.toQ(root), Common.toQ(condition));
				AtlasSet<Node> callsites = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.CallSite).eval().nodes();
				/*
				 * We first detect a has_next kind of API and then match corresponding next API to it
				 */
				Node iteratorApi = checkIteratorPattern(path, callsites);
				if(iteratorApi != null) {
					lowerBound = root;
					upperBound = iteratorApi;
					// Log.info("iterator");
				}
				
			}
			
			// root is a parameter. we need its type to handle it.
			else if(root.taggedWith(XCSG.Parameter)) {
				// actual root which has type
				Q trueRoot = Common.toQ(root).reverseOn(getTaintWithoutCycleEdges()).roots();
				Q arrayRoot = trueRoot.nodesTaggedWithAny(XCSG.ArrayInstantiation);
				if(!arrayRoot.eval().nodes().isEmpty()) {
					Q associatedCondition = Common.toQ(root).forwardOn(getTaintWithoutCycleEdges()).nodesTaggedWithAny(XCSG.DataFlowCondition);
					Q arraylen = loopTerminationTaintGraph.between(Common.toQ(root), associatedCondition).nodesTaggedWithAny(XCSG.Java.ArrayLengthOperator);
					if(arraylen.eval().nodes().size() > 0) {
						lowerBound = root;
						upperBound = arraylen.eval().nodes().one();
					}
				}
				AtlasSet<Node> types = trueRoot.forwardOn(Common.edges(XCSG.TypeOf)).leaves().eval().nodes();
				// path between parameter and condition
				Q condition = loopTerminationTaintGraph.leaves().nodesTaggedWithAny(XCSG.DataFlowCondition);
				Q path = loopTerminationTaintGraph.between(Common.toQ(root), condition);
				AtlasSet<Node> callsites = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.CallSite).eval().nodes();
				for(Node type : types) {
					if(type.taggedWith(XCSG.Classifier)) {
						/* parameter is some class. in order to iterate over it, it must become part of some sort of collection
						 * it means presence of iterators and same logic would follow. only we would have trueRoot as lower bound
						 */
						Node iteratorApi = checkIteratorPattern(path,callsites);
						if(iteratorApi != null) {
							lowerBound = root;
							upperBound = iteratorApi;
							// Log.info("iterator");
						}
					}
				}
				if(lowerBound == null || upperBound == null) {
					bounds = ioPattern(loopHeader, taintRoots, root, "non-literal");
				}
			}
			
			// root is some callsite. This may or may not lead to a collection.
			else if(root.taggedWith(XCSG.CallSite)) {
				// If there is a collection or array getting formed it will show up in path between condition and the root
				AtlasSet<Node> condition = new AtlasHashSet<Node>(loopTerminationTaintGraph.leaves().nodesTaggedWithAny(XCSG.DataFlowCondition).eval().nodes());
				if(condition.size() != 1) {
					//getting rid of non-boundary conditions
					AtlasSet<Node> conditions = new AtlasHashSet<Node>(condition);
					for(Node c : conditions) {
						if(!Common.toQ(c).parent().eval().nodes().one().taggedWith(BoundaryConditions.BOUNDARY_CONDITION)) {
							condition.remove(c);
						}
					}
				}
				Q path = loopTerminationTaintGraph.between(Common.toQ(root), Common.toQ(condition));
				AtlasSet<Node> callsites = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.CallSite).eval().nodes();
				// looking for iterator pattern
				Node iteratorApi = checkIteratorPattern(path,callsites);
				if(iteratorApi != null) {
					lowerBound = root;
					upperBound = iteratorApi;
					// Log.info("iterator");
				}

				// Checking if an array is being formed.
				Q arraylen = path.nodesTaggedWithAny(XCSG.Java.ArrayLengthOperator);
				if(arraylen.eval().nodes().size() > 0) {
					// gotcha!!
					// convenience. root gets lower bound and arraylen in upperbound
					lowerBound = root;
					upperBound = arraylen.eval().nodes().one();
				}
				
				if(lowerBound == null || upperBound == null) {
					// checking if something is being compared with the callsite
					bounds = ioPattern(loopHeader, taintRoots, root, "non-literal");
				}
			}
			
			else if(root.taggedWith(XCSG.InstanceVariableAccess)) {
				// root is a field. Trying to determine its type
				Q actualRoots = Common.toQ(root).reverseOn(getTaintWithoutCycleEdges()).roots();
				AtlasSet<Node> condition = new AtlasHashSet<Node>(loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.DataFlowCondition).eval().nodes());
				Q literalRoots = actualRoots.nodesTaggedWithAny(XCSG.Literal);
				Q arrayRoots = actualRoots.nodesTaggedWithAny(XCSG.ArrayInstantiation);
				Q collectionRoots = actualRoots.nodesTaggedWithAny(XCSG.Instantiation);
				if(!CommonQueries.isEmpty(literalRoots)) {
					// field is a literal
					Q path = loopTerminationTaintGraph.between(Common.toQ(root), Common.toQ(condition));
					Q operators = path.nodesTaggedWithAny(XCSG.Operator);
					if(operators.eval().nodes().size() > 2) {
						lowerBound = root;
					}
					else if(operators.eval().nodes().size() == 2){
						upperBound = root;
					}
					else {
						bounds = ioPattern(loopHeader, taintRoots, root, "non-literal");
					}
				}
				if(!CommonQueries.isEmpty(arrayRoots)) {
					// field is an array. let's check for existence of arraylength operator
					Q operator = loopTerminationTaintGraph.between(Common.toQ(root), Common.toQ(condition)).nodesTaggedWithAny(Java.ArrayLengthOperator);
					if(!CommonQueries.isEmpty(operator)) {
						upperBound = root;
					}
				}
				if(!CommonQueries.isEmpty(collectionRoots)) {
					if(condition.size() != 1) {
						//getting rid of non-boundary conditions
						AtlasSet<Node> conditions = new AtlasHashSet<Node>(condition);
						for(Node c : new AtlasHashSet<Node>(conditions)) {
							if(!Common.toQ(c).parent().eval().nodes().one().taggedWith(BoundaryConditions.BOUNDARY_CONDITION)) {
								condition.remove(c);
							}
						}
					}
					Q path = loopTerminationTaintGraph.between(Common.toQ(root), Common.toQ(condition));
					AtlasSet<Node> callsites = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.CallSite).eval().nodes();
					/*
					 * We first detect a has_next kind of API and then match corresponding next API to it
					 */
					Node iteratorApi = checkIteratorPattern(path, callsites);
					if(iteratorApi != null) {
						lowerBound = root;
						upperBound = iteratorApi;
						// Log.info("iterator");
					}
				}
			}
			else {
				AtlasSet<Node> associatedConditions = Common.toQ(root).forwardOn(getTaintWithoutCycleEdges()).leaves().eval().nodes();
				for(Node condition : associatedConditions) {
					AtlasSet<Node> parents = Common.toQ(condition).parent().eval().nodes();
					if(parents.isEmpty()){
						Log.warning("Condition " + condition.address().toAddressString() + " did not have a parent.");
					}
					for(Node parent : parents){
						if(parent.taggedWith(BoundaryConditions.BOUNDARY_CONDITION)) {
							if(loopTerminationTaintGraph.between(Common.toQ(root),Common.toQ(condition)).nodesTaggedWithAny(XCSG.Java.ArrayLengthOperator).eval().nodes().size() > 0) {
								upperBound = loopTerminationTaintGraph.between(Common.toQ(root), Common.toQ(condition)).nodesTaggedWithAny(XCSG.Java.ArrayLengthOperator).eval().nodes().one();
							}
						}	
					}
				}
				Q condition = loopTerminationTaintGraph.leaves().nodesTaggedWithAny(XCSG.DataFlowCondition);
				if(condition.eval().nodes().size() != 1) {
					//getting rid of non-boundary conditions
					AtlasSet<Node> conditions = condition.eval().nodes();
					for(Node c : conditions) {
						if(!Common.toQ(c).parent().eval().nodes().one().taggedWith(BoundaryConditions.BOUNDARY_CONDITION)) {
							condition = condition.difference(Common.toQ(c));
						}
					}
				}
				Q path = loopTerminationTaintGraph.between(Common.toQ(root), condition);
				AtlasSet<Node> callsites = loopTerminationTaintGraph.nodesTaggedWithAny(XCSG.CallSite).eval().nodes();
				/*
				 * We first detect a has_next kind of API and then match corresponding next API to it
				 */
				Node iteratorApi = checkIteratorPattern(path, callsites);
				if(iteratorApi != null) {
					lowerBound = root;
					upperBound = iteratorApi;
					// Log.info("iterator");
				}
				else {
					bounds = ioPattern(loopHeader, taintRoots, root, "non-literal");
				}
				
			}
			if(lowerBound != null && upperBound != null) {
				// we have found our bounds. no point in continuing
				break;
			}
			
		}
		
		
		if(lowerBound == null || upperBound == null) {
			return bounds;
		}
		
		if(lowerBound.taggedWith(XCSG.Literal) && upperBound.taggedWith(XCSG.Literal)) {
			bounds[0] = lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = upperBound.getAttr(XCSG.name).toString();
			//Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and " + bounds[1]);
			Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_PRIMITIVE);
			Log.info("Tag loop header bound");
		}
		else if(lowerBound.taggedWith(XCSG.Literal) && upperBound.taggedWith(XCSG.Java.ArrayLengthOperator)) {
			bounds[0] = lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = upperBound.getAttr(XCSG.name).toString() + " (array length) ";
			//Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and " + bounds[1]);
			Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_ARRAY);
			Log.info("Tag loop header bound");
		}
		else if(lowerBound.taggedWith(XCSG.Instantiation) && upperBound.taggedWith(XCSG.CallSite)) {
			lowerBound = getSuccessor(loopTerminationTaintGraph,Common.toQ(lowerBound)).eval().nodes().one();
			upperBound = getSuccessor(loopTerminationTaintGraph,Common.toQ(upperBound)).eval().nodes().one();
			bounds[0] = "collection " + lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = "hasNext API " + upperBound.getAttr(XCSG.name).toString();
			//Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and " + bounds[1]);
			Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_COLLECTION);
			Log.info("Tag loop header bound");
		}
		/*if(lowerBound.taggedWith(XCSG.Assignment) && upperBound.taggedWith(XCSG.Instantiation)) {
			bounds[0] = "String " + lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = "and its tokens (length in worst case)";
			Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and its " + bounds[1]);
			Tags.setTag(loopHeader, pattern3_1_1);
			Log.info("Tag loop header bound");
		}*/
		else if(lowerBound.taggedWith(XCSG.CallSite) && upperBound.taggedWith(XCSG.CallSite)) {
				upperBound = getSuccessor(loopTerminationTaintGraph,Common.toQ(upperBound)).eval().nodes().one();
				bounds[0] = "Iterator pattern via callsite " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = "hasNext API " + upperBound.getAttr(XCSG.name).toString();
				//Tags.setTag(loopHeader, bounds[0] + " and " + bounds[1]);
				Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_COLLECTION);
				Log.info("Tag loop header bound");
		}
		else if(lowerBound.taggedWith(XCSG.CallSite) && upperBound.taggedWith(XCSG.Java.ArrayLengthOperator)) {
			bounds[0] = "array formed by callsite " + lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = "length";
			//Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and its " + bounds[1]);
			Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_ARRAY);
			Log.info("Tag loop header bound");
		}
		else if(lowerBound.taggedWith(XCSG.Parameter)) {
			if(upperBound.taggedWith(XCSG.CallSite)) {
				upperBound = getSuccessor(loopTerminationTaintGraph,Common.toQ(upperBound)).eval().nodes().one();
				bounds[0] = "Iterator pattern via parameter" + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = "hasNext API " + upperBound.getAttr(XCSG.name).toString();
				//Tags.setTag(loopHeader, bounds[0] + " and " + bounds[1]);
				Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_COLLECTION);
				Log.info("Tag loop header bound");
			}
			if(upperBound.taggedWith(XCSG.Java.ArrayLengthOperator)) {
				bounds[0] = "array formed by parameter " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = upperBound.getAttr(XCSG.name).toString() + " (array length)";
				//Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and " + bounds[1]);
				Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_PARAMETER_ARRAY);
			}
		}
		else if(lowerBound.taggedWith(XCSG.InstanceVariableAccess) || upperBound.taggedWith(XCSG.InstanceVariableAccess)) {
			if(lowerBound.taggedWith(XCSG.Literal)) {
				bounds[0] = lowerBound.getAttr(XCSG.name).toString();
				if(Common.toQ(upperBound).reverseOn(Common.edges(TaintOverlay.Taint)).roots().eval().nodes().one().taggedWith(XCSG.ArrayInstantiation)) {
					bounds[1] = "array formed by field " + upperBound.getAttr(XCSG.name).toString();
					//Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and length of" + bounds[1]);
					Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_ARRAY);
				}
				else{
					bounds[1] = "field " + upperBound.getAttr(XCSG.name).toString();
					//Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and " + bounds[1]);
					Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE);
				}
				Log.info("Tag loop header bound");
			}
			else if(upperBound.taggedWith(XCSG.Literal)) {
				bounds[0] = "field " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = upperBound.getAttr(XCSG.name).toString();
				//Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and " + bounds[1]);
				Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE);
				Log.info("Tag loop header bound");
			}
			else if(upperBound.taggedWith(XCSG.CallSite)) {
				upperBound = getSuccessor(loopTerminationTaintGraph,Common.toQ(upperBound)).eval().nodes().one();
				bounds[0] = "field " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = "hasNext API " + upperBound.getAttr(XCSG.name).toString();
				Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and " + bounds[1]);
				Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_COLLECTION);
				Log.info("Tag loop header bound");
			}
			else {
				bounds[0] = "field " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = "field " + upperBound.getAttr(XCSG.name).toString();
				//Tags.setTag(loopHeader, "Bounded by " + bounds[0] + " and " + bounds[1]);
				Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE);
				Log.info("Tag loop header bound");
			}
		}
		else if(upperBound.taggedWith(XCSG.CallSite)){
			upperBound = getSuccessor(loopTerminationTaintGraph,Common.toQ(upperBound)).eval().nodes().one();
			bounds[0] = "unknown lowerbound " + lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = "API " + upperBound.getAttr(XCSG.name).toString();
			Tags.setTag(loopHeader, LoopPatternConstants.MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR);
			Log.info("Tag loop header bound");
			
		}
		Log.info("After Tag loop header bound");
		return bounds;
		
		/*// external roots
		Q externalTaintRoots = taintRoots.difference(Common.universe().nodesTaggedWithAny(XCSG.Literal, XCSG.Instantiation, XCSG.ArrayInstantiation));
		
		// callsite roots
		Q callsiteTaintRoots = externalTaintRoots.nodesTaggedWithAny(XCSG.CallSite);
								
		// parameter roots
		Q parameterTaintRoots = externalTaintRoots.nodesTaggedWithAny(XCSG.Parameter);
				
		// global roots
		Q globalVariableTaintRoots = externalTaintRoots.nodesTaggedWithAny(XCSG.Field, XCSG.Java.ArrayLengthOperator);*/
		
	}
	
	private static Node checkIteratorPattern(Q path, AtlasSet<Node> callsites) {
		String key = null;
		Node hasNext = null;
		for(Node callsite : callsites) {
			key = MethodSignatureMatcher.matchSignatureWithAny(callsite, IteratorPatternSignatures.iteratorAPIs.keySet());
			if(key != null) {
				hasNext = callsite;
				break;
			}
		}
		if(key == null) {
			return hasNext;
		}
		for(Node callsite : callsites) {
			if(MethodSignatureMatcher.isMatchSignatureWithAny(callsite, IteratorPatternSignatures.iteratorAPIs.get(key))) {
				return hasNext;
			}
		}
		return null;
	}
	
	private static String[] nonMonotonicPatterns(Q loopHeader) {
		// String io = "IO == ";
		String type;
		String[] bounds = new String[2];
		bounds[0] = bounds[1] = "";
		// String api = "API being compared";
		Q loopTerminationTaintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
//		Q condition = loopTerminationTaintGraph.leaves().nodesTaggedWithAny(XCSG.DataFlowCondition);
		Q roots = loopTerminationTaintGraph.roots();
		for(Node root : roots.eval().nodes()) {
			
			if(root.taggedWith(XCSG.Literal)) {
				type = "literal";
				
			}
			else {
				type = "non-literal";
				
			}
			bounds = ioPattern(loopHeader, roots, root, type);
			if(bounds[0] != "" && bounds[1] != "") {
				break;
			}
		}
		return bounds;
	}

	private static String[] ioPattern(Q loopHeader, Q roots, Node root, String type) {
		Q loopTerminationTaintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
		// check with Nikhil later what is unmodifiable set
		// throws illegal operation exception
		String[] bounds = new String[2];
		bounds[0] = bounds[1] = "";
		String ioBound = "IO API ";
		String apiBound = "API ";
		String ioTag = LoopPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL;
		String apiTag = LoopPatternConstants.MONOTONICITY_PATTERN_API_LOCAL;
		if(root.taggedWith(XCSG.Parameter)) {
			ioBound = "Parameter using " + ioBound;
			apiBound = "Parameter using " + apiBound;
			ioTag = LoopPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER;
			apiTag = LoopPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER;
		}
		else if(root.taggedWith(XCSG.InstanceVariableAccess)) {
			ioBound = "Field using " + ioBound;
			apiBound = "Field using " + apiBound;
			ioTag = LoopPatternConstants.MONOTONICITY_PATTERN_IO_FIELD;
			apiTag = LoopPatternConstants.MONOTONICITY_PATTERN_API_FIELD;
		}
		Q conditions = loopTerminationTaintGraph.leaves().nodesTaggedWithAny(XCSG.DataFlowCondition);
		Q operator = getSuccessor(loopTerminationTaintGraph,Common.toQ(root));
		AtlasSet<Node> comparators = new AtlasHashSet<Node>();
		if(operator.eval().nodes().isEmpty()) {
			return bounds;
		}
		if(taggedWithAny(operator.eval().nodes().one(),getComparatorTags())){
			// something is being compared to null. Could be IO pattern
			// getting callsites that reach this operator and check for IO
			Q callsites = loopTerminationTaintGraph.between(roots.difference(Common.toQ(root)),operator).nodesTaggedWithAny(XCSG.CallSite);
			for(Node callsite : callsites.eval().nodes()) {
				if(MethodSignatureMatcher.isMatchSignatureWithAny(callsite, MethodSignatures.IO_APIs)) {
					bounds[0] = ioBound;
					//bounds[1] = callsite.getAttr(XCSG.name).toString() + " being compared to " + type;
					bounds[1] = callsites.eval().nodes().one().getAttr(XCSG.name).toString() + "being compared to " + type;
					Tags.setTag(loopHeader, ioTag);
					Log.info("Tag loop header bound");
					return bounds;
				}
			}
			if(callsites.eval().nodes().size() > 0) {
				bounds[0] = apiBound;
				//bounds[1] = targetPackage(callsites) + "being compared to " + type;
				bounds[1] = callsites.eval().nodes().one().getAttr(XCSG.name).toString() + "being compared to " + type;
				Tags.setTag(loopHeader, apiTag);
				Log.info("Tag loop header bound");
				return bounds;
			}
		}
		else if(operator.eval().nodes().one().taggedWith(XCSG.Assignment)){
			if(root.taggedWith(XCSG.CallSite)) {
				Q path = loopTerminationTaintGraph.between(Common.toQ(root), conditions);
				
				comparators.addAll(path.nodesTaggedWithAny(XCSG.Operator).eval().nodes());
				Q lEdges = path.edgesTaggedWithAny(XCSG.leftOperand);
				Q rEdges = path.edgesTaggedWithAny(XCSG.rightOperand);
				//AtlasSet<Node> nonComparators = Common.empty().eval().nodes();
				for(Node comparator : new AtlasHashSet<Node>(comparators)) {
					if(!taggedWithAny(comparator,getComparatorTags())) {
						//nonComparators.add(comparator);
						comparators.remove(comparator);
					}
				}
				//comparators = comparators.difference(Common.toQ(nonComparators));
				if(lEdges.eval().edges().size() > 0) {
					AtlasSet<Node> beingCompared = new AtlasHashSet<Node>(Common.toQ(comparators).reverseOn(Common.edges(XCSG.rightOperand)).reverseOn(getTaintWithoutCycleEdges()).roots().eval().nodes());
					//AtlasSet<Node> nonVariables = Common.empty().eval().nodes();
					
					for(Node n : new AtlasHashSet<Node>(beingCompared)) {
						if(getSuccessor(loopTerminationTaintGraph,Common.toQ(n)).nodesTaggedWithAny(XCSG.Assignment).eval().nodes().size() != 1) {
							//nonVariables.add(n);
							beingCompared.remove(n);
						}
					}
					//beingCompared = beingCompared.difference(Common.toQ(nonVariables));
					if(Common.toQ(beingCompared).nodesTaggedWithAny(XCSG.Literal).eval().nodes().size() > 0) {
						type = "literal";
					}
				}
				if(rEdges.eval().edges().size() > 0) {
					
					AtlasSet<Node> beingCompared = new AtlasHashSet<Node>(Common.toQ(comparators).reverseOn(Common.edges(XCSG.leftOperand)).reverseOn(getTaintWithoutCycleEdges()).roots().eval().nodes());
					//AtlasSet<Node> nonVariables = Common.empty().eval().nodes();
					for(Node n : new AtlasHashSet<Node>(beingCompared)) {
						if(getSuccessor(loopTerminationTaintGraph,Common.toQ(n)).nodesTaggedWithAny(XCSG.Assignment).eval().nodes().size() != 1) {
							//nonVariables.add(n);
							beingCompared.remove(n);
						}
					}
					//beingCompared = beingCompared.difference(Common.toQ(nonVariables));
					if(Common.toQ(beingCompared).nodesTaggedWithAny(XCSG.Literal).eval().nodes().size() > 0) {
						type = "literal";
					}
				}
				if(MethodSignatureMatcher.isMatchSignatureWithAny(root, MethodSignatures.IO_APIs)) {
					bounds[0] = ioBound;
					bounds[1] = root.getAttr(XCSG.name).toString() + " being compared to " + type;
					Tags.setTag(loopHeader, ioTag);
					Log.info("Tag loop header bound");
					return bounds;
				}
				else {
					bounds[0] = apiBound;
					//bounds[1] = targetPackage(Common.toQ(root)) + "being compared to " + type;
					bounds[1] = root.getAttr(XCSG.name).toString() + "being compared to " + type;
					Tags.setTag(loopHeader, apiTag);
					Log.info("Tag loop header bound");
					return bounds;
				}
			}
			comparators = new AtlasHashSet<Node>(loopTerminationTaintGraph.between(Common.toQ(root), conditions).nodesTaggedWithAny(XCSG.Operator).eval().nodes());
			//AtlasSet<Node> nonComparators = Common.empty().eval().nodes();
			for(Node comparator : new AtlasHashSet<Node>(comparators)) {
				if(!taggedWithAny(comparator,getComparatorTags())) {
					//nonComparators.add(comparator);
					comparators.remove(comparator);
				}
			}
			//comparators = comparators.difference(Common.toQ(nonComparators));
			Q callsites = loopTerminationTaintGraph.between(roots.difference(Common.toQ(root)),Common.toQ(comparators)).nodesTaggedWithAny(XCSG.CallSite);
			for(Node callsite : callsites.eval().nodes()) {
				if(MethodSignatureMatcher.isMatchSignatureWithAny(callsite, MethodSignatures.IO_APIs)) {
					bounds[0] = ioBound;
					//bounds[1] = callsite.getAttr(XCSG.name).toString() + " being compared to " + type;
					bounds[1] = callsites.eval().nodes().one().getAttr(XCSG.name).toString() + "being compared to " + type;
					Tags.setTag(loopHeader, ioTag);
					Log.info("Tag loop header bound");
					return bounds;
				}
			}
			if(callsites.eval().nodes().size() > 0) {
				bounds[0] = apiBound;
				//bounds[1] = targetPackage(callsites) + "being compared to " + type;
				bounds[1] = callsites.eval().nodes().one().getAttr(XCSG.name).toString() + "being compared to " + type;
				Tags.setTag(loopHeader, apiTag);
				Log.info("Tag loop header bound");
				return bounds;
			}
		}
		else {
			Q path = loopTerminationTaintGraph.between(Common.toQ(root), conditions);
			comparators = new AtlasHashSet<Node>(path.nodesTaggedWithAny(XCSG.Operator).eval().nodes());
			Q comparisonEdges = path.edgesTaggedWithAny(XCSG.leftOperand, XCSG.rightOperand);
			//AtlasSet<Node> nonComparators = Common.empty().eval().nodes();
			for(Node comparator : new AtlasHashSet<Node>(comparators)) {
				if(!taggedWithAny(comparator,getComparatorTags())) {
					//nonComparators.add(comparator);
					comparators.remove(comparator);
				}
			}
			//comparators = comparators.difference(Common.toQ(nonComparators));
			Q beingCompared = Common.toQ(comparators).reverseOn(comparisonEdges).reverseOn(getTaintWithoutCycleEdges()).roots();
			if(beingCompared.nodesTaggedWithAny(XCSG.Literal).eval().nodes().size() > 0) {
				type = "literal";
			}
			Q callsites = path.nodesTaggedWithAny(XCSG.CallSite);
			for(Node callsite : callsites.eval().nodes()) {
				if(MethodSignatureMatcher.isMatchSignatureWithAny(callsite, MethodSignatures.IO_APIs)) {
					bounds[0] = ioBound;
					bounds[1] = callsite.getAttr(XCSG.name).toString() + " being compared to " + type;
					Tags.setTag(loopHeader, ioTag);
					Log.info("Tag loop header bound");
					return bounds;
				}
			}
			if(callsites.eval().nodes().size() > 0) {
				bounds[0] = apiBound;
				//bounds[1] = targetPackage(callsites) + "being compared to " + type;
				bounds[1] = callsites.eval().nodes().one().getAttr(XCSG.name).toString() + "being compared to " + type;
				Tags.setTag(loopHeader, apiTag);
				Log.info("Tag loop header bound");
				return bounds;
			}
			
		}
		return bounds;
	}
	
	public static String targetPackage(Q cs) {
		Set<String> targetTypeNames = new HashSet<String>();
		Set<String> targetPackageNames = new HashSet<String>();
		for(GraphElement c : cs.eval().nodes()) {
			Q targetMethods = CallSiteAnalysis.getTargetMethods(c);
			for(Node t : targetMethods.eval().nodes()) {
				Node containingNode = Explorer.getContainingNode(t,XCSG.Package);
				if(containingNode != null) {
					targetPackageNames.add(containingNode.getAttr(XCSG.name).toString());
				}
				targetTypeNames.add(((containingNode!=null)?containingNode.getAttr(XCSG.name):"no-package") + "." + Explorer.getContainingNode(t,XCSG.Type).getAttr(XCSG.name)+"."+t.getAttr(XCSG.name).toString());
			}
		}
		return targetPackageNames.toString().replaceAll(",", ";") + "," + targetTypeNames.toString().replaceAll(",", ";");
	}

}
