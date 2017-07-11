package com.ensoftcorp.open.loop.comprehension.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.loop.comprehension.utils.Observables;

public class DifferentialBranchFilter extends NodeFilter {

	@Override
	public String getName() {
		return "Differential Branch";
	}

	@Override
	public String getDescription() {
		return "Filters loops based on whether they contain a differential branch";
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { CFGNode.LOOP_HEADER };
	}

	public Q filter(Q input, Map<String, Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filter(input, parameters);

		Q branchNodes = Observables.getNTBranchesGovernedByCollectionSize().parent();
		Q loopCfgs = loopControlFlowGraph(input);
		Q matches = loopCfgs.reverse(branchNodes).nodesTaggedWithAny(CFGNode.LOOP_HEADER);
		return super.filter(matches, parameters);
	}
	
	public static Q loopControlFlowGraph(Q loopHeader) {
		return Common.edges(XCSG.ControlFlow_Edge).between(loopHeader, loopHeader);		
	}
}
