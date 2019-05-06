package com.kcsl.loop.catalog.lambda;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.analysis.CallSiteAnalysis;
import com.kcsl.loop.catalog.subsystems.AppSubsystem;
import com.kcsl.loop.catalog.subsystems.ImplicitLoopSubsystem;
import com.kcsl.loop.taint.analysis.TaintOverlay;
import com.kcsl.loop.util.LoopUtils;
import com.kcsl.loop.util.Tags;

/*
 * This class identifies lambda expressions
 * 
 * @author Payas Awadhutkar
 */

public class LambdaIdentifier {

	// Lambda Expression Tag
	public static final String LAMBDA_EXPRESSION = "LAMBDA_EXPRESSION";
	
	// Lambda Expression ID
	public static final String LAMBDA_EXPRESSION_ID = "LAMBDA_EXPRESSION_ID";
	
	// Hidden Loop Tag
	public static final String LAMBDA_LOOP = "LAMBDA_LOOP";
	
	// Hidden Loop ID
	public static final String LAMBDA_LOOP_ID = "LAMBDA_LOOP_ID";
	
	// Lambda Expression Member ID
	public static final String LAMBDA_EXPRESSION_MEMBER_ID = "LAMBDA_EXPRESSION_MEMBER_ID";
	
	/** Control Flow edges without backedges **/
	private Q controlFlowWithoutBackEdges = Query.universe().edges(XCSG.ControlFlow_Edge).differenceEdges(Query.universe().edges(XCSG.ControlFlowBackEdge));
	
	public LambdaIdentifier() {
	}
	
	public void processLambdaExpressions() {
		Q lambdaExpressions = getLambdaExpressions();
		
		// Tag Lambda Expression Callsites
		Tags.setTag(lambdaExpressions, LAMBDA_EXPRESSION);
	}
	
	public void processLambdaLoops() {
		Q lambdaExpressions = Query.universe().nodes(LAMBDA_EXPRESSION);
		
		int LE_ID = 1;
		int LL_ID = 1;
		
		for(Node lambdaExpression : lambdaExpressions.eval().nodes()) {
			lambdaExpression.putAttr(LAMBDA_EXPRESSION_ID, Integer.toString(LE_ID));
			Q lambdaLoops = identifyLambdaLoops(Common.toQ(lambdaExpression));
			// lambdaLoop not empty
			if(!CommonQueries.isEmpty(lambdaLoops)) {
				for(Node lambdaLoop: lambdaLoops.eval().nodes()) {
					Tags.setTag(Common.toQ(lambdaLoop), LAMBDA_LOOP);
					lambdaLoop.putAttr(LAMBDA_LOOP_ID, Integer.toString(LL_ID));
					lambdaLoop.putAttr(LAMBDA_EXPRESSION_MEMBER_ID, Integer.toString(LE_ID));
					LL_ID++;
				}
			}
			LE_ID++;
		}
		
	}

	// Method to get all lambda expressions
	public Q getLambdaExpressions() {
		Q lambdaMethods = Query.universe().nodes("LAMBDA_SUBSYSTEM");
		Q lambdaCallSites = lambdaMethods.predecessorsOn(Common.edges(XCSG.InvokedFunction));
		Q lambdaExpressions = lambdaCallSites.parent();
		return lambdaExpressions;
	}
	
	// Method to identify lambda loops
	public Q identifyLambdaLoops(Q lambdaExpression) {
		
		Q lambdaLoops = Common.empty();
		
		Q lambdaObject = lambdaExpression.children().nodes(XCSG.Assignment);
		
		Q currentNodeQ = lambdaExpression.successorsOn(controlFlowWithoutBackEdges);
		
		while(!CommonQueries.isEmpty(currentNodeQ)) {
			for(Node currentNode : currentNodeQ.eval().nodes()) {
				if(!currentNode.taggedWith(LAMBDA_EXPRESSION) || !currentNode.taggedWith(XCSG.controlFlowExitPoint)) {
					Q callSitesInTheCurrentNode = currentNodeQ.children().nodes(XCSG.CallSite);
					if(!CommonQueries.isEmpty(callSitesInTheCurrentNode)) {
						if(checkCallSitesForLambdaLoops(callSitesInTheCurrentNode,lambdaObject)) {
							lambdaLoops = lambdaLoops.union(currentNodeQ);
						}
					}
				}
			}
			currentNodeQ = currentNodeQ.successorsOn(controlFlowWithoutBackEdges);
		}
		
		return lambdaLoops;
	}
	
	public boolean checkCallSitesForLambdaLoops(Q callSites, Q lambdaObject) {
		
		for(Node callSite: callSites.eval().nodes()) {
			if(isImplicitLoop(callSite,lambdaObject)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isImplicitLoop(Node callSite, Q lambdaObject) {
		boolean flag = false;
		AtlasSet<Node> targetMethods = CallSiteAnalysis.getTargetMethods(callSite);
		for(Node targetMethod : targetMethods) {
			if(targetMethod.taggedWith(ImplicitLoopSubsystem.TAG)) {
				flag = true;
			}
			else if(targetMethod.taggedWith(AppSubsystem.TAG)) {
				Q targetMethodQ = Common.toQ(targetMethod);
				Q loopsContained = targetMethodQ.contained().nodes(XCSG.Loop);
				if(!CommonQueries.isEmpty(loopsContained)) {
					Q loopMembers = LoopUtils.getControlFlowGraph(loopsContained).children();
					Q lambdaTaint = Query.universe().edges(TaintOverlay.Taint).forward(lambdaObject);
					if(!CommonQueries.isEmpty(lambdaTaint.intersection(loopMembers))) {
						flag = true;
					}
				}
			}
		}
		return flag;
	}
	
}
