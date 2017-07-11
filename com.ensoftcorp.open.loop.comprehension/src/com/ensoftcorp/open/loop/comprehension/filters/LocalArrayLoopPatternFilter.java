package com.ensoftcorp.open.loop.comprehension.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.utils.LoopPatternConstants;

/**
 * Filters loops headers that match with Local Array Pattern
 * 
 * @author Payas Awadhutkar
 */

public class LocalArrayLoopPatternFilter extends NodeFilter {

	private static final String EXCLUDE_MATCHES = "EXCLUDE_MATCHES";

	public LocalArrayLoopPatternFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only loops that do not match this pattern.");
	}

	@Override
	public String getName() {
		return "Local Array";
	}

	@Override
	public String getDescription() {
		return "Filters loops by boundary conditions that depend on local arrays.";
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { CFGNode.LOOP_HEADER };
	}

	public Q filter(Q input, Map<String, Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filter(input, parameters);

		Q matches = input.nodes(LoopPatternConstants.MONOTONICITY_PATTERN_LOCAL_ARRAY);
		if (this.isFlagSet(EXCLUDE_MATCHES, parameters)) {
			return input.difference(matches);
		} else {
			return input.intersection(matches);
		}
	}

}
