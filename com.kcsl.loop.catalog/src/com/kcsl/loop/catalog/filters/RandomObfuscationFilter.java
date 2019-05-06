package com.kcsl.loop.catalog.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;

public class RandomObfuscationFilter extends NodeFilter {
	
	private static final String RANDOM = "RANDOM";
	private static final String NON_RANDOM = "NON_RANDOM";
	
	public RandomObfuscationFilter() {
		this.addPossibleFlag(RANDOM, "Retains loops causing random obfuscation.");
		this.addPossibleFlag(NON_RANDOM, "Retains loops that do not cause random obfuscation.");
		this.setMinimumNumberParametersRequired(1);
	}
	
	@Override
	public String getName() {
		return "Random Obfuscation";
	}

	@Override
	public String getDescription() {
		return "Filters artificial loops that obfuscate the code by adding a random delay";
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { XCSG.Loop };
	}
	
	public Q filterInput(Q input, Map<String, Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filterInput(input, parameters);

		Q randomLoops = getRandomLoops(input);
		
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		
		if (this.isFlagSet(RANDOM, parameters)) {
			result.addAll(randomLoops.eval().nodes());
		}
		
		if (this.isFlagSet(NON_RANDOM, parameters)) {
			result.addAll(input.difference(randomLoops).eval().nodes());
		}
		
		return Common.toQ(result);
	}

	public static Q getRandomLoops(Q loopHeaders) {
		Q java_util_Random_random = Common.typeSelect("java.util", "Random").children().selectNode(XCSG.name,"random");
		Q java_util_Math_random = Common.typeSelect("java.lang", "Math").children().selectNode(XCSG.name,"random");
		Q randomMethods = java_util_Random_random.union(java_util_Math_random);
		Q randomReturnValues = Query.resolve(null, randomMethods.children().nodes(XCSG.ReturnValue));
		return getRandomLoops(loopHeaders, randomReturnValues);
	}
	
	private static Q getRandomLoops(Q loopHeaders, Q randomReturnValues) {
		Q bconds = Query.resolve(null, Query.universe().nodes(BoundaryConditions.BOUNDARY_CONDITION).children());
		Q reachingDataFlow = Query.universe().edges(XCSG.DataFlow_Edge).between(randomReturnValues,bconds);
		
		AtlasSet<Node> randomLoops = new AtlasHashSet<Node>();
		for(Node dfCondition: reachingDataFlow.leaves().eval().nodes()) {
			Node cfCondition = dfCondition.in(XCSG.Contains).one().from();
			Object loopHeaderId = null;
			if (cfCondition.taggedWith(XCSG.Loop))
				loopHeaderId = cfCondition.getAttr(CFGNode.LOOP_HEADER_ID);
			else
				loopHeaderId = cfCondition.getAttr(CFGNode.LOOP_MEMBER_ID);
			
			if (loopHeaderId != null) {
				Q loopHeader = Query.universe().selectNode(CFGNode.LOOP_HEADER_ID, loopHeaderId);
				randomLoops.addAll(loopHeader.eval().nodes());
			} else {
				Log.debug("Expected a loopHeaderId for node: " + cfCondition);
			}
		}
		return Common.toQ(randomLoops);
	}

}
