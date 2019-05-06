package com.kcsl.loop.catalog.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;
import com.ensoftcorp.open.commons.subsystems.Subsystems;

/**
 * Filters loops headers that depend on how whether or not the loop bodies interact
 * with the specified subsystems
 * 
 * @author Ben Holland
 */
public class LoopSubsystemInteractionFilter extends NodeFilter {

	private static final String SUBSYSTEM_TAGS = "SUBSYSTEM_TAGS";

	public LoopSubsystemInteractionFilter() {
		this.addPossibleParameter(SUBSYSTEM_TAGS, String.class, true, "A comma separated list of subsystem tags");
		this.setMinimumNumberParametersRequired(1);
	}

	@Override
	public String getName() {
		return "Loop Body Subsystem Interactions";
	}

	@Override
	public String getDescription() {
		return "Filters loops headers that depend on how whether or not the loop bodies interact with the specified subsystems.";
	}

	@Override
	public Q filterInput(Q input, Map<String, Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filterInput(input, parameters);

		String[] subsystems = ((String) getParameterValue(SUBSYSTEM_TAGS, parameters)).replaceAll("\\s", "").split(",");

		AtlasSet<Node> result = new AtlasHashSet<Node>();
		for (String subsystem : subsystems) {
			// TODO: not sure exactly why this approach didn't work yet...~BH
//			// control flow and data flow children of loop body
//			Q loopBodies = LoopUtils.getControlFlowMembers(input).contained();
//			Q interactions = Subsystems.getSubsystemInteractions(loopBodies, subsystem, interactionEdges);
//			result.addAll(interactions.containers().intersection(input).eval().nodes());
			
			Q interactions = getInteractingLoops(input, Subsystems.getSubsystemContents(subsystem).nodes(XCSG.Function)).intersection(input);
			result.addAll(interactions.eval().nodes());
		}

		return Common.toQ(result);
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { XCSG.Loop };
	}
	
	public static Q getInteractingLoops(Q loopHeaders, Q functions) {
		Q interactingLoops = Common.empty();
		for(Node loopHeader: loopHeaders.eval().nodes()) {
			Q interactions = getLoopInteractions(functions, loopHeader);
			if(!CommonQueries.isEmpty(interactions)) {
				interactingLoops = interactingLoops.union(Common.toQ(loopHeader));
			}
		}
		return interactingLoops;
	}

	public static Q getLoopInteractions(Q functions, Node loopHeader) {
		Q loopCFG = Common.edges(XCSG.ControlFlow_Edge).between(Common.toQ(loopHeader), Common.toQ(loopHeader));	
		Q loopCFGForward = loopCFG.forwardOn(Common.edges(Attr.Edge.PER_CONTROL_FLOW));
		Q reverseOnInteractingFunctions = functions.reverseOn(Common.edges(Attr.Edge.PER_CONTROL_FLOW));
		Q interactions = loopCFGForward.intersection(reverseOnInteractingFunctions);
		return interactions;
	}

}