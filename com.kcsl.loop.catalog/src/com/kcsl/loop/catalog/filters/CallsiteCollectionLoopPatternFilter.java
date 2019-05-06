package com.kcsl.loop.catalog.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;
import com.kcsl.loop.catalog.monotonicity.MonotonicityPatternConstants;

public class CallsiteCollectionLoopPatternFilter extends NodeFilter {

	private static final String EXCLUDE_MATCHES = "EXCLUDE_MATCHES";

	public CallsiteCollectionLoopPatternFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only loops that do not match this pattern.");
	}

	@Override
	public String getName() {
		return "Callsite Collection";
	}

	@Override
	public String getDescription() {
		return "Filters loops by boundary conditions that depend on callsite collections.";
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { XCSG.Loop };
	}

	public Q filterInput(Q input, Map<String, Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filterInput(input, parameters);

		Q matches = input.nodes(MonotonicityPatternConstants.MONOTONICITY_PATTERN_CALLSITE_COLLECTION);
		if (this.isFlagSet(EXCLUDE_MATCHES, parameters)) {
			return input.difference(matches);
		} else {
			return input.intersection(matches);
		}
	}

}
