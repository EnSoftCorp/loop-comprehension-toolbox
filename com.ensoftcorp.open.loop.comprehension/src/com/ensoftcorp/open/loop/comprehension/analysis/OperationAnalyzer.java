package com.ensoftcorp.open.loop.comprehension.analysis;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.loop.comprehension.taint.TaintOverlay;
import com.ensoftcorp.open.loop.comprehension.utils.LoopPatternConstants;
import com.ensoftcorp.open.loop.comprehension.utils.LoopUtils;
import com.ensoftcorp.open.loop.comprehension.utils.MethodSignatureMatcher;
import com.ensoftcorp.open.loop.comprehension.utils.MethodSignatures;
// import org.rulersoftware.taint.TaintOverlay;
import com.ensoftcorp.open.loop.comprehension.utils.Tags;

public class OperationAnalyzer {
	
	public static void applyMonotonicityTagsOnControlFlow(Q loopHeader) {
		//loop cfg
		Q loopCfg = LoopUtils.getControlFlowMembers(loopHeader.eval().nodes().getFirst());
		AtlasSet<Node> children;
		String literal = "";
		for(GraphElement cfgnode : loopCfg.eval().nodes()) {
			children = Common.toQ(cfgnode).children().eval().nodes();
			for(Node child : children) {
				if(child.taggedWith(XCSG.CallSite)) {
					// Log.info("Name of callsite: " + child.toString());
					if(MethodSignatureMatcher.isMatchSignatureWithAny(child, MethodSignatures.INCREMENT_APIs)) {
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.INCREMENT_OPERATOR);
					}
					if(MethodSignatureMatcher.isMatchSignatureWithAny(child, MethodSignatures.DECREMENT_APIs)) {
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.DECREMENT_OPERATOR);
					}
					/*if(child.getAttr(XCSG.name).toString().contains("replace")) {
						Tags.setTag(Common.toQ(cfgnode), UPDATE);
					}*/
				}
				else {
					if(child.taggedWith(XCSG.Addition) || child.taggedWith(XCSG.Multiplication) || child.taggedWith(XCSG.Increment)) {
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.INCREMENT_OPERATOR);
					}
					if(child.taggedWith(XCSG.Subtraction) || child.taggedWith(XCSG.Division) || child.taggedWith(XCSG.Decrement)) {
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.DECREMENT_OPERATOR);
					}
					if(child.taggedWith(XCSG.Literal)) {
						literal = child.getAttr(XCSG.name).toString();
					}
				}
			}
			if(isInteger(literal)) {
				if(Integer.parseInt(literal) < 0) {
					if(cfgnode.taggedWith(LoopPatternConstants.INCREMENT_OPERATOR)) {
						cfgnode.untag(LoopPatternConstants.INCREMENT_OPERATOR);
						Tags.setTag(Common.toQ(cfgnode),LoopPatternConstants.DECREMENT_OPERATOR);
					}
					else if(cfgnode.taggedWith(LoopPatternConstants.DECREMENT_OPERATOR)) {
						cfgnode.untag(LoopPatternConstants.DECREMENT_OPERATOR);
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.INCREMENT_OPERATOR);
					}
					else{
						
					}
				}
			}
		}
		return;
	}
	
	public static void applyMonotonicityTagsOnDataFlow(Q loopHeader) {
		
		Q loopCfg = LoopUtils.getControlFlowMembers(loopHeader.eval().nodes().getFirst());
		Graph dataFlow;
		AtlasSet<Node> children;
		String literal = "";
		for(GraphElement cfgnode : loopCfg.eval().nodes()) {
			dataFlow = Common.toQ(cfgnode).children().induce(Common.edges(TaintOverlay.Taint)).eval();
			children = dataFlow.nodes();
			for(GraphElement child : children) {
				if(child.taggedWith(XCSG.CallSite)) {
					if(child.getAttr(XCSG.name).toString().contains("concat") || child.getAttr(XCSG.name).toString().contains("next") || child.getAttr(XCSG.name).toString().contains("push")) {
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.INCREMENT_OPERATOR);
						Tags.setTag(Common.toQ(dataFlow).edgesTaggedWithAny(TaintOverlay.Taint).successors(Common.toQ(child)), LoopPatternConstants.INCREMENT_OPERATOR);
					}
					if(child.getAttr(XCSG.name).toString().contains("remove") || child.getAttr(XCSG.name).toString().contains("pop")) {
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.DECREMENT_OPERATOR);
						Tags.setTag(Common.toQ(dataFlow).edgesTaggedWithAny(TaintOverlay.Taint).successors(Common.toQ(child)), LoopPatternConstants.DECREMENT_OPERATOR);
					}
					/*if(child.getAttr(XCSG.name).toString().contains("replace")) {
						Tags.setTag(Common.toQ(cfgnode), UPDATE);
					}*/
					
				}
				else {
					if(child.taggedWith(XCSG.Addition) || child.taggedWith(XCSG.Multiplication) || child.taggedWith(XCSG.Increment)) {
						// Log.info("Tagging ++");
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.INCREMENT_OPERATOR);
						Tags.setTag(Common.toQ(dataFlow).edgesTaggedWithAny(TaintOverlay.Taint).successors(Common.toQ(child)), LoopPatternConstants.INCREMENT_OPERATOR);
					}
					if(child.taggedWith(XCSG.Subtraction) || child.taggedWith(XCSG.Division) || child.taggedWith(XCSG.Decrement)) {
						// Log.info("Tagging --");
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.DECREMENT_OPERATOR);
						Tags.setTag(Common.toQ(dataFlow).edgesTaggedWithAny(TaintOverlay.Taint).successors(Common.toQ(child)), LoopPatternConstants.DECREMENT_OPERATOR);
					}
					if(child.taggedWith(XCSG.Literal)) {
						literal = child.getAttr(XCSG.name).toString();
					}
				}
			}
			if(isInteger(literal)) {
				if(Integer.parseInt(literal) < 0) {
					if(cfgnode.taggedWith(LoopPatternConstants.INCREMENT_OPERATOR)) {
						cfgnode.untag(LoopPatternConstants.INCREMENT_OPERATOR);
						Tags.setTag(Common.toQ(cfgnode),LoopPatternConstants.DECREMENT_OPERATOR);
					}
					else if(cfgnode.taggedWith(LoopPatternConstants.DECREMENT_OPERATOR)) {
						cfgnode.untag(LoopPatternConstants.DECREMENT_OPERATOR);
						Tags.setTag(Common.toQ(cfgnode), LoopPatternConstants.INCREMENT_OPERATOR);
					}
					else{
						
					}
				}
			}
		}
		return;
	}
	
	private static boolean isInteger( String input ) {
	    try {
	        Integer.parseInt( input );
	        return true;
	    }
	    catch(NumberFormatException e ) {
	        return false;
	    }
	}

}