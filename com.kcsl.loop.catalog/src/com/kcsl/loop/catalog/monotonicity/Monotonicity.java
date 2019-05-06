package com.kcsl.loop.catalog.monotonicity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.core.xcsg.XCSG.Java;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.kcsl.loop.catalog.log.Log;
import com.kcsl.loop.taint.analysis.TaintOverlay;
import com.kcsl.loop.util.Explorer;
import com.kcsl.loop.util.IteratorPatternSignatures;
import com.kcsl.loop.util.LoopUtils;
import com.kcsl.loop.util.MethodSignatureMatcher;
import com.kcsl.loop.util.MethodSignatures;

/**
 * @author Payas Awadhutkar
 */

public class Monotonicity {
	
	private static final String NON_LITERAL = "non-literal"; //$NON-NLS-1$
	private static final String LITERAL = "literal"; //$NON-NLS-1$
	
	private static String[] COMPARATOR_TAGS = new String[]{XCSG.EqualTo,XCSG.NotEqualTo,XCSG.LessThan,XCSG.LessThanOrEqualTo,XCSG.GreaterThan,XCSG.GreaterThanOrEqualTo,XCSG.Jimple.Comparison,XCSG.Jimple.ComparisonDefaultGreaterThan,XCSG.Jimple.ComparisonDefaultLessThan};
	private static final List<String> COMPARATOR_TAGS_LIST = Arrays.asList(COMPARATOR_TAGS);;

	public static List<String> getComparatorTags(){
		return COMPARATOR_TAGS_LIST;
	}
	
	public static Q getTaintWithoutCycleEdges(){
		return Query.universe().edges(TaintOverlay.Taint).differenceEdges(Query.universe().edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT));
	}
	
	public static void tagMultiple(AtlasSet<Node> loopHeaders) {
		for(Node loopHeader : loopHeaders) {
			monotonicity(loopHeader);
		}
	}
	
	
	/* Method to add immutability stuff later
	 * public static Q newTaintGraph(Node loopHeader) {
		
		// Includes Immutability Results
		// Assumption : Immutability analysis was run
		
		if(!loopHeader.taggedWith(XCSG.Loop)) {
			throw new RuntimeException("Not a loop header");
		}
		
		//Terminating Conditions
		Q tc = TerminatingConditions.findTerminatingConditionsForLoopHeader(loopHeader);
		
		// all taints without the cycle causing edges
		Q taints = Query.universe().edges(TaintOverlay.Taint).differenceEdges(Common.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT));

		// method containing loop header
		Q method = CommonQueries.getContainingFunctions(loopHeader);

		// method contents
		Q methodContents = CommonQueries.localDeclarations(method);

		// local taints influencing loop termination (taint events)
		Q conditions = tc.children().nodes(XCSG.DataFlowCondition);
		Q loopTerminationTaints = conditions.reverseOn(taints).intersection(methodContents);
				
		// loop termination taints
		loopTerminationTaintGraph = loopTerminationTaints.induce(taints);
		
		
		// callsites in Taint graph
		Q callsites = loopTerminationTaintGraph.nodes(XCSG.CallSite);
		
		// forwarding taint
		Q missing = callsites.forwardOn(Common.edges(XCSG.LocalDataFlow));
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		
		// pulling in missed induction variable assignments
		missing = loopTerminationTaintGraph.nodes(XCSG.IdentityPass).parent().children().nodesTaggedWithAll(XCSG.Assignment);
		
		missing = Common.edges(TaintOverlay.Taint).between(loopTerminationTaintGraph, missing);
		
		// updating Taint graph
		loopTerminationTaintGraph = loopTerminationTaintGraph.union(missing);
		//DisplayUtil.displayGraph(loopTerminationTaintGraph.eval());
		
		return loopTerminationTaintGraph;
						
	}*/
	
	public static void monotonicity(Node loopHeader) {
		if(!loopHeader.taggedWith(XCSG.Loop)) {
			throw new IllegalArgumentException("Not a loop header"); //$NON-NLS-1$
		}
		
		MonotonicityAnalyzer.applyMonotonicityTagsOnControlFlow(loopHeader);
		
		Q taintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
		Q roots = taintGraph.roots();
		Q condition = taintGraph.nodes(XCSG.DataFlowCondition);
		
		// induction variables
		Q inductionVariables = taintGraph.nodes(XCSG.Assignment);
		//DisplayUtil.displayGraph(getTaintGraph(loopHeader).eval());
		
		AtlasSet<Node> inductionNodes = inductionVariables.parent().eval().nodes();
		
		int increments = 0;
		int decrements = 0;
		
		for(Node inductionNode : inductionNodes) {
			if(inductionNode.taggedWith(MonotonicityPatternConstants.INCREMENT_OPERATOR)) {
				increments++;
			}
			if(inductionNode.taggedWith(MonotonicityPatternConstants.DECREMENT_OPERATOR)) {
				decrements++;
			}
		}
		// Log.info("#Increase: " + increments);
		// Log.info("#Decrease: " + decrements);
		if((increments > 0 && decrements == 0) || (decrements > 0 && increments == 0)) {
			loopHeader.tag(MonotonicityPatternConstants.MONOTONIC_LOOP);
		}
		
		if(!loopHeader.taggedWith(MonotonicityPatternConstants.MONOTONIC_LOOP)) {
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
				loopHeader.tag(MonotonicityPatternConstants.MONOTONIC_LOOP);
			}
		}
		return;
	}
	
	private static int checkMonotonicity(Q path) {
		Q inductionVariables = path.nodes(XCSG.Assignment);
		AtlasSet<Node> inductionNodes = inductionVariables.parent().eval().nodes();
		
		int increments = 0;
		int decrements = 0;
		
		for(Node inductionNode : inductionNodes) {
			if(inductionNode.taggedWith(MonotonicityPatternConstants.INCREMENT_OPERATOR)) {
				increments++;
			}
			if(inductionNode.taggedWith(MonotonicityPatternConstants.DECREMENT_OPERATOR)) {
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
	
	public static AtlasSet<Node> getTaintSuccessors(Q dfGraph, Node dfNode) {
		return dfGraph.edges(TaintOverlay.Taint).successors(Common.toQ(dfNode)).eval().nodes();
	}
	
	public static AtlasSet<Node> getTaintPredeccessors(Q dfGraph, Node dfNode) {
		return dfGraph.edges(TaintOverlay.Taint).predecessors(Common.toQ(dfNode)).eval().nodes();
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
	public static String[] bounds(Node loopHeader) throws NullPointerException {
		long depth = LoopUtils.getNestingDepth(loopHeader);
		loopHeader.putAttr(LoopUtils.NESTING_DEPTH, String.valueOf(depth));

		
		if(!loopHeader.taggedWith(XCSG.Loop)) {
			throw new IllegalArgumentException("Not a loop header");
		}

//		Log.info("Start bound analysis for loop header " + loopHeader.getAttr(CFGNode.LOOP_HEADER_ID));
		
		// stores result to return it stats
		String[] bounds = new String[2];
		bounds[0] = bounds[1] = "";
 		// improved tagging
		MonotonicityAnalyzer.applyMonotonicityTagsOnDataFlow(loopHeader);
		
		// check monotonicity
		monotonicity(loopHeader);
		
		if (loopHeader.taggedWith(MonotonicityPatternConstants.MONOTONIC_LOOP)) {
			bounds = monotonicPatterns(loopHeader, bounds);
		}
		else {
			bounds = nonMonotonicPatterns(loopHeader);
		}
		
		if (bounds == null || bounds.length != 2) {
			throw new RuntimeException("Error when computing loop bounds for loop at " + loopHeader.address().toAddressString());
		}
		
		loopHeader.putAttr("LOOP_LOWER_BOUND", bounds[0]);
		loopHeader.putAttr("LOOP_UPPER_BOUND", bounds[1]);

		loopHeader.tag(bounds[0]); // retain this tagging for compatibility with old Loop Catalog driver that an analyst may call
		loopHeader.tag(bounds[1]); // retain this tagging for compatibility with old Loop Catalog driver that an analyst may call
		
		return bounds;
	}
		
	private static String[] monotonicPatterns(Node loopHeader, String[] bounds) { 
		// taint graph
		Q loopTerminationTaintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
		
		if(loopTerminationTaintGraph.edges(TaintOverlay.OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT).eval().edges().size() != 0) {
			// method containing loop header
			Node method = CommonQueries.getContainingFunction(loopHeader);
			
			// method contents
			Q methodContents = CommonQueries.localDeclarations(Common.toQ(method));
			
			// getting rid of cycles if any left (there is a possibility)
			Q leaves = loopTerminationTaintGraph.leaves();
	
			Q taintWithoutCycleEdges = getTaintWithoutCycleEdges();
			loopTerminationTaintGraph = leaves.reverseOn(taintWithoutCycleEdges).intersection(methodContents).induce(taintWithoutCycleEdges);
			//DisplayUtil.displayGraph(taintGraph.eval());
		}
		// taint roots
		Q taintRoots = loopTerminationTaintGraph.roots();
		
		// loop cfg
		Q loopCfg = LoopUtils.getControlFlowMembers(loopHeader);
		AtlasSet<Node> loopCfgNodes = loopCfg.eval().nodes();
		
		Node lowerBound = null, upperBound = null;
		
		for(Node root : taintRoots.eval().nodes()) {
			// root is literal - definitely one of the bounds
			if(root.taggedWith(XCSG.Literal)) {
				AtlasSet<Node> successors = getTaintSuccessors(loopTerminationTaintGraph, root);
				if(successors.size() != 1) {
					throw new RuntimeException("Expected a single taint successor!");
				}
				Node successor = successors.one();
				if(successor.taggedWith(XCSG.Assignment)) {
					
					// TODO: Is the taint successor's parent always a CFG node? ~BH
					if(!loopCfgNodes.contains(Common.toQ(successor).parent().eval().nodes().one())) {
						lowerBound = root;
					}
				}
				if(taggedWithAny(successor, getComparatorTags())) {
					bounds = ioPattern(loopHeader, loopTerminationTaintGraph, taintRoots, root, LITERAL);
				}
				if(Common.toQ(successor).parent().eval().nodes().one().taggedWith(BoundaryConditions.BOUNDARY_CONDITION)) {
					upperBound = root;
				}
			}
			
			// root is array - upper bound has to be array length
			else if(root.taggedWith(XCSG.ArrayInstantiation)) {
				AtlasSet<Node> associatedConditions = Common.toQ(root).forwardOn(getTaintWithoutCycleEdges()).leaves().eval().nodes();
				for(Node condition : associatedConditions) {
					AtlasSet<Node> parents = Common.toQ(condition).parent().eval().nodes();
					if(parents.isEmpty()){
						Log.warning("Condition " + condition.address().toAddressString() + " did not have a parent.");
					}
					for(Node parent : parents){
						if(parent.taggedWith(BoundaryConditions.BOUNDARY_CONDITION)) {
							
							// TODO: I don't understand this line, could there be more than one result?
							upperBound = loopTerminationTaintGraph.between(Common.toQ(root),Common.toQ(condition)).nodes(XCSG.Java.ArrayLengthOperator).eval().nodes().one();
						}
					}
				}
			}
			
			// root is a collection or a string - bound has to be length of collection determined by its iterator
			else if(root.taggedWith(XCSG.Instantiation)) {
				/* we first must get the path between condition and collection instantiation.
				   this will have both variables - collection and iterator*/
				AtlasSet<Node> condition = new AtlasHashSet<Node>(loopTerminationTaintGraph.leaves().nodes(XCSG.DataFlowCondition).eval().nodes());
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
				AtlasSet<Node> callsites = loopTerminationTaintGraph.nodes(XCSG.CallSite).eval().nodes();
				/*
				 * We first detect a has_next kind of API and then match corresponding next API to it
				 */
				Node iteratorApi = checkIteratorPattern(path, callsites);
				if(iteratorApi != null) {
					lowerBound = root;
					upperBound = iteratorApi;
				}
				
			}
			
			// root is a parameter. we need its type to handle it.
			else if(root.taggedWith(XCSG.Parameter)) {
				// actual root which has type
				Q trueRoot = Common.toQ(root).reverseOn(getTaintWithoutCycleEdges()).roots();
				Q arrayRoot = trueRoot.nodes(XCSG.ArrayInstantiation);
				if(!arrayRoot.eval().nodes().isEmpty()) {
					Q associatedCondition = Common.toQ(root).forwardOn(getTaintWithoutCycleEdges()).nodes(XCSG.DataFlowCondition);
					Q arraylen = loopTerminationTaintGraph.between(Common.toQ(root), associatedCondition).nodes(XCSG.Java.ArrayLengthOperator);
					if(arraylen.eval().nodes().size() > 0) {
						lowerBound = root;
						
						// TODO: I don't understand this line, could there be more than one?
						upperBound = arraylen.eval().nodes().one();
					}
				}
				AtlasSet<Node> types = trueRoot.forwardOn(Common.edges(XCSG.TypeOf)).leaves().eval().nodes();
				// path between parameter and condition
				Q condition = loopTerminationTaintGraph.leaves().nodes(XCSG.DataFlowCondition);
				Q path = loopTerminationTaintGraph.between(Common.toQ(root), condition);
				AtlasSet<Node> callsites = loopTerminationTaintGraph.nodes(XCSG.CallSite).eval().nodes();
				for(Node type : types) {
					if(type.taggedWith(XCSG.Classifier)) {
						/* parameter is some class. in order to iterate over it, it must become part of some sort of collection
						 * it means presence of iterators and same logic would follow. only we would have trueRoot as lower bound
						 */
						Node iteratorApi = checkIteratorPattern(path,callsites);
						if(iteratorApi != null) {
							lowerBound = root;
							upperBound = iteratorApi;
						}
					}
				}
				if(lowerBound == null || upperBound == null) {
					bounds = ioPattern(loopHeader, loopTerminationTaintGraph, taintRoots, root, NON_LITERAL);
				}
			}
			
			// root is some callsite. This may or may not lead to a collection.
			else if(root.taggedWith(XCSG.CallSite)) {
				// If there is a collection or array getting formed it will show up in path between condition and the root
				AtlasSet<Node> condition = new AtlasHashSet<Node>(loopTerminationTaintGraph.leaves().nodes(XCSG.DataFlowCondition).eval().nodes());
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
				AtlasSet<Node> callsites = loopTerminationTaintGraph.nodes(XCSG.CallSite).eval().nodes();
				// looking for iterator pattern
				Node iteratorApi = checkIteratorPattern(path,callsites);
				if(iteratorApi != null) {
					lowerBound = root;
					upperBound = iteratorApi;
				}

				// Checking if an array is being formed.
				Q arraylen = path.nodes(XCSG.Java.ArrayLengthOperator);
				if(arraylen.eval().nodes().size() > 0) {
					// convenience. root gets lower bound and arraylen in upperbound
					lowerBound = root;
					
					// TODO: I don't understand this line, could there be more than one?
					upperBound = arraylen.eval().nodes().one();
				}
				
				if(lowerBound == null || upperBound == null) {
					// checking if something is being compared with the callsite
					bounds = ioPattern(loopHeader, loopTerminationTaintGraph, taintRoots, root, NON_LITERAL);
				}
			}
			
			else if(root.taggedWith(XCSG.InstanceVariableAccess)) {
				// root is a field. Trying to determine its type
				Q actualRoots = Common.toQ(root).reverseOn(getTaintWithoutCycleEdges()).roots();
				AtlasSet<Node> condition = new AtlasHashSet<Node>(loopTerminationTaintGraph.nodes(XCSG.DataFlowCondition).eval().nodes());
				Q literalRoots = actualRoots.nodes(XCSG.Literal);
				Q arrayRoots = actualRoots.nodes(XCSG.ArrayInstantiation);
				Q collectionRoots = actualRoots.nodes(XCSG.Instantiation);
				if(!CommonQueries.isEmpty(literalRoots)) {
					// field is a literal
					Q path = loopTerminationTaintGraph.between(Common.toQ(root), Common.toQ(condition));
					Q operators = path.nodes(XCSG.Operator);
					if(operators.eval().nodes().size() > 2) {
						lowerBound = root;
					}
					else if(operators.eval().nodes().size() == 2){
						upperBound = root;
					}
					else {
						bounds = ioPattern(loopHeader, loopTerminationTaintGraph, taintRoots, root, NON_LITERAL);
					}
				}
				if(!CommonQueries.isEmpty(arrayRoots)) {
					// field is an array. let's check for existence of arraylength operator
					Q operator = loopTerminationTaintGraph.between(Common.toQ(root), Common.toQ(condition)).nodes(Java.ArrayLengthOperator);
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
					AtlasSet<Node> callsites = loopTerminationTaintGraph.nodes(XCSG.CallSite).eval().nodes();
					/*
					 * We first detect a has_next kind of API and then match corresponding next API to it
					 */
					Node iteratorApi = checkIteratorPattern(path, callsites);
					if(iteratorApi != null) {
						lowerBound = root;
						upperBound = iteratorApi;
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
							if(loopTerminationTaintGraph.between(Common.toQ(root),Common.toQ(condition)).nodes(XCSG.Java.ArrayLengthOperator).eval().nodes().size() > 0) {
								
								// TODO: I don't understand this line, could there be more than one?
								upperBound = loopTerminationTaintGraph.between(Common.toQ(root), Common.toQ(condition)).nodes(XCSG.Java.ArrayLengthOperator).eval().nodes().one();
							}
						}	
					}
				}
				Q condition = loopTerminationTaintGraph.leaves().nodes(XCSG.DataFlowCondition);
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
				AtlasSet<Node> callsites = loopTerminationTaintGraph.nodes(XCSG.CallSite).eval().nodes();
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
					bounds = ioPattern(loopHeader, loopTerminationTaintGraph, taintRoots, root, NON_LITERAL);
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
			setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_LOCAL_PRIMITIVE);
		}
		else if(lowerBound.taggedWith(XCSG.Literal) && upperBound.taggedWith(XCSG.Java.ArrayLengthOperator)) {
			bounds[0] = lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = upperBound.getAttr(XCSG.name).toString() + " (array length) ";
			setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_LOCAL_ARRAY);
		}
		else if(lowerBound.taggedWith(XCSG.Instantiation) && upperBound.taggedWith(XCSG.CallSite)) {
			AtlasSet<Node> lowerBoundSuccessors = getTaintSuccessors(loopTerminationTaintGraph, lowerBound);
			if(lowerBoundSuccessors.size() != 1) {
				throw new RuntimeException("Expected a single taint successor!");
			}
			Node lowerBoundSuccessor = lowerBoundSuccessors.one();
			lowerBound = lowerBoundSuccessor;
			
			AtlasSet<Node> upperBoundSuccessors = getTaintSuccessors(loopTerminationTaintGraph, upperBound);
			if(upperBoundSuccessors.size() != 1) {
				throw new RuntimeException("Expected a single taint successor!");
			}
			Node upperBoundSuccessor = upperBoundSuccessors.one();
			upperBound = upperBoundSuccessor;
			
			bounds[0] = "collection " + lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = "hasNext API " + upperBound.getAttr(XCSG.name).toString();
			setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_LOCAL_COLLECTION);
		}
		/*if(lowerBound.taggedWith(XCSG.Assignment) && upperBound.taggedWith(XCSG.Instantiation)) {
			bounds[0] = "String " + lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = "and its tokens (length in worst case)";
			tagLoopHeader(loopHeader, "Bounded by " + bounds[0] + " and its " + bounds[1]);
			tagLoopHeader(loopHeader, pattern3_1_1);
		}*/
		else if(lowerBound.taggedWith(XCSG.CallSite) && upperBound.taggedWith(XCSG.CallSite)) {
				AtlasSet<Node> successors = getTaintSuccessors(loopTerminationTaintGraph, upperBound);
				if(successors.size() != 1) {
					throw new RuntimeException("Expected a single taint successor!");
				}
				Node successor = successors.one();
				upperBound = successor;
				bounds[0] = "Iterator pattern via callsite " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = "hasNext API " + upperBound.getAttr(XCSG.name).toString();
				setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_CALLSITE_COLLECTION);
		}
		else if(lowerBound.taggedWith(XCSG.CallSite) && upperBound.taggedWith(XCSG.Java.ArrayLengthOperator)) {
			bounds[0] = "array formed by callsite " + lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = "length";
			setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_CALLSITE_ARRAY);
		}
		else if(lowerBound.taggedWith(XCSG.Parameter)) {
			if(upperBound.taggedWith(XCSG.CallSite)) {
				AtlasSet<Node> successors = getTaintSuccessors(loopTerminationTaintGraph, upperBound);
				if(successors.size() != 1) {
					throw new RuntimeException("Expected a single taint successor!");
				}
				Node successor = successors.one();
				upperBound = successor;
				bounds[0] = "Iterator pattern via parameter" + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = "hasNext API " + upperBound.getAttr(XCSG.name).toString();
				setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_PARAMETER_COLLECTION);
			}
			if(upperBound.taggedWith(XCSG.Java.ArrayLengthOperator)) {
				bounds[0] = "array formed by parameter " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = upperBound.getAttr(XCSG.name).toString() + " (array length)";
				setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_PARAMETER_ARRAY);
			}
		}
		else if(lowerBound.taggedWith(XCSG.InstanceVariableAccess) || upperBound.taggedWith(XCSG.InstanceVariableAccess)) {
			if(lowerBound.taggedWith(XCSG.Literal)) {
				bounds[0] = lowerBound.getAttr(XCSG.name).toString();
				
				// TODO: I don't understand this line, could there be more than one taint root?
				if(Common.toQ(upperBound).reverseOn(Common.edges(TaintOverlay.Taint)).roots().eval().nodes().one().taggedWith(XCSG.ArrayInstantiation)) {
					bounds[1] = "array formed by field " + upperBound.getAttr(XCSG.name).toString();
					setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_FIELD_ARRAY);
				}
				else{
					bounds[1] = "field " + upperBound.getAttr(XCSG.name).toString();
					setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE);
				}
			}
			else if(upperBound.taggedWith(XCSG.Literal)) {
				bounds[0] = "field " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = upperBound.getAttr(XCSG.name).toString();
				setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE);
			}
			else if(upperBound.taggedWith(XCSG.CallSite)) {
				AtlasSet<Node> successors = getTaintSuccessors(loopTerminationTaintGraph, upperBound);
				if(successors.size() != 1) {
					throw new RuntimeException("Expected a single taint successor!");
				}
				Node successor = successors.one();
				upperBound = successor;
				bounds[0] = "field " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = "hasNext API " + upperBound.getAttr(XCSG.name).toString();
				setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_FIELD_COLLECTION);
			}
			else {
				bounds[0] = "field " + lowerBound.getAttr(XCSG.name).toString();
				bounds[1] = "field " + upperBound.getAttr(XCSG.name).toString();
				setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_FIELD_PRIMITIVE);
			}
		}
		else if(upperBound.taggedWith(XCSG.CallSite)){
			AtlasSet<Node> successors = getTaintSuccessors(loopTerminationTaintGraph, upperBound);
			if(successors.size() != 1) {
				throw new RuntimeException("Expected a single taint successor!");
			}
			Node successor = successors.one();
			upperBound = successor;
			bounds[0] = "unknown lowerbound " + lowerBound.getAttr(XCSG.name).toString();
			bounds[1] = "API " + upperBound.getAttr(XCSG.name).toString();
			setLoopPattern(loopHeader, MonotonicityPatternConstants.MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR);
		}
//		Log.info("After Tag loop header bound");
		
		return bounds;
		
		/*// external roots
		Q externalTaintRoots = taintRoots.difference(Query.universe().nodes(XCSG.Literal, XCSG.Instantiation, XCSG.ArrayInstantiation));
		
		// callsite roots
		Q callsiteTaintRoots = externalTaintRoots.nodes(XCSG.CallSite);
								
		// parameter roots
		Q parameterTaintRoots = externalTaintRoots.nodes(XCSG.Parameter);
				
		// global roots
		Q globalVariableTaintRoots = externalTaintRoots.nodes(XCSG.Field, XCSG.Java.ArrayLengthOperator);*/
		
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
	
	private static String[] nonMonotonicPatterns(Node loopHeader) {
		// String io = "IO == ";
		String type;
		String[] bounds = new String[2];
		bounds[0] = bounds[1] = "";
		// String api = "API being compared";
		Q loopTerminationTaintGraph = LoopAbstractions.taintGraphWithoutCycles(loopHeader);
//		Q condition = loopTerminationTaintGraph.leaves().nodes(XCSG.DataFlowCondition);
		Q roots = loopTerminationTaintGraph.roots();
		
		for(Node root : roots.eval().nodes()) {
			
			if(root.taggedWith(XCSG.Literal)) {
				type = LITERAL;
				
			}
			else {
				type = NON_LITERAL;
				
			}
			bounds = ioPattern(loopHeader, loopTerminationTaintGraph, roots, root, type);
			if(bounds[0] != "" && bounds[1] != "") {
				break;
			}
		}
		return bounds;
	}

	/**
	 * 
	 * @param loopHeader
	 * @param loopTerminationTaintGraph
	 * @param roots local roots
	 * @param root local root
	 * @param type
	 * @return
	 */
	private static String[] ioPattern(Node loopHeader, Q loopTerminationTaintGraph, Q roots, Node root, String type) {
		String[] bounds = new String[2];
		bounds[0] = bounds[1] = ""; //$NON-NLS-1$
		String ioBound = "IO API "; //$NON-NLS-1$
		String apiBound = "API "; //$NON-NLS-1$
		String ioTag = MonotonicityPatternConstants.MONOTONICITY_PATTERN_IO_LOCAL;
		String apiTag = MonotonicityPatternConstants.MONOTONICITY_PATTERN_API_LOCAL;
		if(root.taggedWith(XCSG.Parameter)) {
			ioBound = "Parameter using " + ioBound;
			apiBound = "Parameter using " + apiBound;
			ioTag = MonotonicityPatternConstants.MONOTONICITY_PATTERN_IO_PARAMETER;
			apiTag = MonotonicityPatternConstants.MONOTONICITY_PATTERN_API_PARAMETER;
		}
		else if(root.taggedWith(XCSG.InstanceVariableAccess)) {
			ioBound = "Field using " + ioBound;
			apiBound = "Field using " + apiBound;
			ioTag = MonotonicityPatternConstants.MONOTONICITY_PATTERN_IO_FIELD;
			apiTag = MonotonicityPatternConstants.MONOTONICITY_PATTERN_API_FIELD;
		}
		Q conditions = loopTerminationTaintGraph.nodes(XCSG.DataFlowCondition);
		AtlasSet<Node> operators = getTaintSuccessors(loopTerminationTaintGraph, root);
		AtlasSet<Node> comparators = new AtlasHashSet<Node>();
		if(operators.isEmpty()) {
			return bounds;
		} else if(operators.size() != 1) {
			throw new RuntimeException("Expected a single operator taint successor!");
		}
		Node operator = operators.one();
		if(taggedWithAny(operator, getComparatorTags())){
			// something is being compared to null. Could be IO pattern
			// getting callsites that reach this operator and check for IO
			Q callsites = loopTerminationTaintGraph.between(roots.difference(Common.toQ(root)),Common.toQ(operator)).nodes(XCSG.CallSite);
			for(Node callsite : callsites.eval().nodes()) {
				if(MethodSignatureMatcher.isMatchSignatureWithAny(callsite, MethodSignatures.IO_APIs)) {
					bounds[0] = ioBound;
					//bounds[1] = callsite.getAttr(XCSG.name).toString() + " being compared to " + type;
					bounds[1] = callsite.getAttr(XCSG.name).toString() + "being compared to " + type;
					setLoopPattern(loopHeader, ioTag);
//					Log.info("Tag loop header bound");
					return bounds;
				}
			}
			if(callsites.eval().nodes().size() > 0) {
				bounds[0] = apiBound;
				//bounds[1] = targetPackage(callsites) + "being compared to " + type;

				// TODO: Why just grab one callsite here? I'm confused. ~BH
				bounds[1] = callsites.eval().nodes().one().getAttr(XCSG.name).toString() + "being compared to " + type;
				setLoopPattern(loopHeader, apiTag);
//				Log.info("Tag loop header bound");
				return bounds;
			}
		}
		else if(operator.taggedWith(XCSG.Assignment)){
			if(root.taggedWith(XCSG.CallSite)) {
				Q path = loopTerminationTaintGraph.between(Common.toQ(root), conditions);
				
				comparators.addAll(path.nodes(XCSG.Operator).eval().nodes());
				Q lEdges = path.edges(XCSG.leftOperand);
				Q rEdges = path.edges(XCSG.rightOperand);
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
						if(Common.toQ(getTaintSuccessors(loopTerminationTaintGraph,n)).nodes(XCSG.Assignment).eval().nodes().size() != 1) {
							//nonVariables.add(n);
							beingCompared.remove(n);
						}
					}
					//beingCompared = beingCompared.difference(Common.toQ(nonVariables));
					if(Common.toQ(beingCompared).nodes(XCSG.Literal).eval().nodes().size() > 0) {
						type = LITERAL;
					}
				}
				if(rEdges.eval().edges().size() > 0) {
					
					AtlasSet<Node> beingCompared = new AtlasHashSet<Node>(Common.toQ(comparators).reverseOn(Common.edges(XCSG.leftOperand)).reverseOn(getTaintWithoutCycleEdges()).roots().eval().nodes());
					//AtlasSet<Node> nonVariables = Common.empty().eval().nodes();
					for(Node n : new AtlasHashSet<Node>(beingCompared)) {
						if(Common.toQ(getTaintSuccessors(loopTerminationTaintGraph,n)).nodes(XCSG.Assignment).eval().nodes().size() != 1) {
							//nonVariables.add(n);
							beingCompared.remove(n);
						}
					}
					//beingCompared = beingCompared.difference(Common.toQ(nonVariables));
					if(Common.toQ(beingCompared).nodes(XCSG.Literal).eval().nodes().size() > 0) {
						type = LITERAL;
					}
				}
				if(MethodSignatureMatcher.isMatchSignatureWithAny(root, MethodSignatures.IO_APIs)) {
					bounds[0] = ioBound;
					bounds[1] = root.getAttr(XCSG.name).toString() + " being compared to " + type;
					setLoopPattern(loopHeader, ioTag);
//					Log.info("Tag loop header bound");
					return bounds;
				}
				else {
					bounds[0] = apiBound;
					//bounds[1] = targetPackage(Common.toQ(root)) + "being compared to " + type;
					bounds[1] = root.getAttr(XCSG.name).toString() + "being compared to " + type;
					setLoopPattern(loopHeader, apiTag);
//					Log.info("Tag loop header bound");
					return bounds;
				}
			}
			Q qComparators = loopTerminationTaintGraph.between(Common.toQ(root), conditions).nodes(COMPARATOR_TAGS);
			Q callsites = loopTerminationTaintGraph.between(roots.difference(Common.toQ(root)), qComparators.nodes(XCSG.CallSite));
			for(Node callsite : callsites.eval().nodes()) {
				if(MethodSignatureMatcher.isMatchSignatureWithAny(callsite, MethodSignatures.IO_APIs)) {
					bounds[0] = ioBound;
					//bounds[1] = callsite.getAttr(XCSG.name).toString() + " being compared to " + type;
					bounds[1] = callsite.getAttr(XCSG.name).toString() + "being compared to " + type;
					setLoopPattern(loopHeader, ioTag);
//					Log.info("Tag loop header bound");
					return bounds;
				}
			}
			if(callsites.eval().nodes().size() > 0) {
				bounds[0] = apiBound;
				//bounds[1] = targetPackage(callsites) + "being compared to " + type;
				
				// TODO: I don't understand this line, there could be more than one callsite
				bounds[1] = callsites.eval().nodes().one().getAttr(XCSG.name).toString() + "being compared to " + type;
				
				setLoopPattern(loopHeader, apiTag);
//				Log.info("Tag loop header bound");
				return bounds;
			}
		}
		else {
			Q path = loopTerminationTaintGraph.between(Common.toQ(root), conditions);
			comparators = new AtlasHashSet<Node>(path.nodes(XCSG.Operator).eval().nodes());
			Q comparisonEdges = path.edges(XCSG.leftOperand, XCSG.rightOperand);
			//AtlasSet<Node> nonComparators = Common.empty().eval().nodes();
			for(Node comparator : new AtlasHashSet<Node>(comparators)) {
				if(!taggedWithAny(comparator,getComparatorTags())) {
					//nonComparators.add(comparator);
					comparators.remove(comparator);
				}
			}
			//comparators = comparators.difference(Common.toQ(nonComparators));
			Q beingCompared = Common.toQ(comparators).reverseOn(comparisonEdges).reverseOn(getTaintWithoutCycleEdges()).roots();
			if(beingCompared.nodes(XCSG.Literal).eval().nodes().size() > 0) {
				type = LITERAL;
			}
			Q callsites = path.nodes(XCSG.CallSite);
			for(Node callsite : callsites.eval().nodes()) {
				if(MethodSignatureMatcher.isMatchSignatureWithAny(callsite, MethodSignatures.IO_APIs)) {
					bounds[0] = ioBound;
					bounds[1] = callsite.getAttr(XCSG.name).toString() + " being compared to " + type;
					setLoopPattern(loopHeader, ioTag);
//					Log.info("Tag loop header bound");
					return bounds;
				}
			}
			if(callsites.eval().nodes().size() > 0) {
				bounds[0] = apiBound;
				//bounds[1] = targetPackage(callsites) + "being compared to " + type;
				
				// TODO: I don't understand this line, there could be more than one callsite
				bounds[1] = callsites.eval().nodes().one().getAttr(XCSG.name).toString() + "being compared to " + type;
				
				setLoopPattern(loopHeader, apiTag);
//				Log.info("Tag loop header bound");
				return bounds;
			}
			
		}
		return bounds;
	}

	private static void setLoopPattern(Node loopHeader, String pattern) {
		loopHeader.putAttr("LOOP_TERMINATION_PATTERN", pattern);
		loopHeader.tag(pattern); // retain this tagging for compatibility with old Loop Catalog driver that an analyst may call
	}
	
	public static String targetPackage(Q cs) {
		Set<String> targetTypeNames = new HashSet<String>();
		Set<String> targetPackageNames = new HashSet<String>();
		for(Node c : cs.eval().nodes()) {
			Q targetMethods = CallSiteAnalysis.getTargets(Common.toQ(c));
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