package com.kcsl.loop.catalog.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;
import com.kcsl.loop.catalog.monotonicity.MonotonicityPatternConstants;

public class UnknownCallsiteFilter extends NodeFilter {

	private static final String EXCLUDE_MATCHES = "EXCLUDE_MATCHES";

	public UnknownCallsiteFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only loops that do not match this pattern.");
	}

	@Override
	public String getName() {
		return "Unknown Callsite";
	}

	@Override
	public String getDescription() {
		return "Filters loops that depend on an unknown callsite.";
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { XCSG.Loop };
	}

	public Q filterInput(Q input, Map<String, Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filterInput(input, parameters);

		Q matches = input.nodes(MonotonicityPatternConstants.MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR);
		if (this.isFlagSet(EXCLUDE_MATCHES, parameters)) {
			return input.difference(matches);
		} else {
			return input.intersection(matches);
		}
	}

}
