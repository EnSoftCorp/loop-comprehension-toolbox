package com.kcsl.loop.catalog.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;
import com.kcsl.loop.catalog.monotonicity.MonotonicityPatternConstants;

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
		return new String[] { XCSG.Loop };
	}

	public Q filterInput(Q input, Map<String, Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filterInput(input, parameters);

		Q monotonicLoops = input.nodes(MonotonicityPatternConstants.MONOTONIC_LOOP);
		
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
