package com.ensoftcorp.open.loop.comprehension.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.utils.LoopPatternConstants;

/**
 * Filters loops headers based on their monotonicity
 * 
 * @author Payas Awadhutkar
 */

public class MonotonicityFilter extends NodeFilter {

	private static final String MONOTONIC = "MONOTONIC";
	private static final String NON_MONOTONIC = "NON_MONOTONIC";

	public MonotonicityFilter() {
		this.addPossibleFlag(MONOTONIC, "Retains monotonic loops.");
		this.addPossibleFlag(NON_MONOTONIC, "Retains non-monotonic loops.");
		this.setMinimumNumberParametersRequired(1);
	}

	@Override
	public String getName() {
		return "Loop Monotonicity";
	}

	@Override
	public String getDescription() {
		return "Filters loops that depend on if they are monotonic or not.";
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { CFGNode.LOOP_HEADER };
	}

	public Q filter(Q input, Map<String, Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filter(input, parameters);

		Q monotonicLoops = input.nodes(LoopPatternConstants.MONOTONIC_LOOP);
		
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		
		if (this.isFlagSet(MONOTONIC, parameters)) {
			result.addAll(monotonicLoops.eval().nodes());
		}
		
		if (this.isFlagSet(NON_MONOTONIC, parameters)) {
			result.addAll(input.difference(monotonicLoops).eval().nodes());
		}
		
		return Common.toQ(result);
	}

}
