package com.kcsl.loop.ui.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.subsystems.Subsystems;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditions;
import com.ensoftcorp.open.pcg.common.PCGFactory;
import com.ensoftcorp.open.pcg.common.highlighter.PCGHighlighter;
import com.kcsl.loop.util.LoopUtils;
import com.kcsl.loop.util.SetDefinitions;

/**
 * Input:
 * One or more functions

 * Output:
 * For each input Function, the Loop Call Graph.
 * If a Function has a Loop or can reach a Function which has a loop,
 * it will be in the call graph.  Otherwise the result is the empty graph.
 *
 *
 * Highlights:
 * Functions containing loops == bright BLUE
 * Call edges which may have been from a CallSite within a loop == ORANGE
 *
 */
public class SubsystemInteractionsPCGView extends FilteringAtlasSmartViewScript implements AtlasSmartViewScript {

        @Override
        protected String[] getSupportedNodeTags() {
                return new String[]{XCSG.Loop,XCSG.Function,XCSG.CallSite,XCSG.ControlFlow_Node};
        }

        @Override
        protected String[] getSupportedEdgeTags() {
                return NOTHING;
        }

        @Override
        public String getTitle() {
                return "RULER Subsystem Interaction PCG View";
        }


        @Override
        protected StyledResult selectionChanged(IAtlasSelectionEvent event, Q filteredSelection) {

            if (CommonQueries.isEmpty(filteredSelection)){
                    return null;
            }

            Q interactions = getInteractions(filteredSelection);
            Q selection = filteredSelection.nodes(XCSG.Loop,XCSG.Function,XCSG.ControlFlow_Node,XCSG.CallSite);
            Q events = getSubsystemInteractionEvents(selection.eval().nodes().one(),SubsystemInteractionsView.subsystemTags);
    		
            
            Markup m = new Markup(PCGHighlighter.getPCGMarkup(events));
            
            // call sites to subsystem
            m.setNode(events, MarkupProperty.NODE_BACKGROUND_COLOR, Color.GREEN.brighter());
            
            // magenta methods in subsystem
            m.setNode(SetDefinitions.JDKLibraries().contained().nodes(XCSG.Method), MarkupProperty.NODE_BACKGROUND_COLOR, Color.magenta.brighter());
            m.setNode(interactions.nodes(XCSG.Function).difference(SetDefinitions.JDKLibraries().contained().nodes(XCSG.Method)), MarkupProperty.NODE_BORDER_COLOR, Color.MAGENTA.brighter());
            m.setNode(interactions.nodes(XCSG.Function).difference(SetDefinitions.JDKLibraries().contained().nodes(XCSG.Method)), MarkupProperty.NODE_BORDER_WEIGHT, new Integer(2));
            
            // dashed magenta call edge to interaction
            m.setEdge(interactions.edges(Attr.Edge.PER_CONTROL_FLOW,XCSG.Call), MarkupProperty.EDGE_STYLE, MarkupProperty.LineStyle.DASHED_DOTTED);
            m.setEdge(interactions.edges(Attr.Edge.PER_CONTROL_FLOW,XCSG.Call), MarkupProperty.EDGE_WEIGHT, new Integer(2));
            m.setEdge(interactions.edges(Attr.Edge.PER_CONTROL_FLOW,XCSG.Call), MarkupProperty.EDGE_COLOR, Color.MAGENTA.brighter());
            
            return new StyledResult(interactions, m, interactions.nodes(XCSG.Loop));

        }

		public static Q getInteractions(Q filteredSelection) {
			Q interactions = Common.empty();
			
			Q context = filteredSelection.nodes(XCSG.Loop,XCSG.Function,XCSG.ControlFlow_Node,XCSG.CallSite);
			for(Node node: context.eval().nodes()) {
				Q events = getSubsystemInteractionEvents(node, SubsystemInteractionsView.subsystemTags);
				Q cfg = Common.empty();
				Q pcg = Common.empty();
		        
				interactions = getSubsystemInteractionGraph(node, SubsystemInteractionsView.subsystemTags);
		        
		        if(node.taggedWith(XCSG.Function)) {
		        		pcg = PCGFactory.create(events).getPCG();
		        } else if(node.taggedWith(XCSG.Loop)) {
		        		cfg = LoopUtils.getControlFlowGraph(node);
		        		pcg = PCGFactory.create(cfg, Common.toQ(node), cfg.nodes(BoundaryConditions.BOUNDARY_CONDITION), events.union(Common.toQ(node))).getPCG();
		        } else if (node.taggedWith(XCSG.ControlFlowCondition)) {
					pcg = PCGFactory.create(events).getPCG();
				}
		        
		        interactions = interactions.union(pcg);
			}
			
			return interactions;
		}

        public static Q getSubsystemInteractionGraph(Node context, String... subsystemTags) {
                Q result = Common.empty();

                Q targetFunctions = getTargetFunctionsForContainedCallsites(context);
                Q subsystemFunctions = Subsystems.getSubsystemContents(subsystemTags).nodes(XCSG.Function);
                result = Common.edges(XCSG.Call).between(targetFunctions,subsystemFunctions);

                Q callsiteContainers = getCallsiteContainers(context);
                Q summaryCallEdges = Common.edges(Attr.Edge.PER_CONTROL_FLOW).betweenStep(callsiteContainers, result);
                result = result.union(summaryCallEdges);
                return result;
        }
        
        public static Q getSubsystemInteractionEvents(Node context, String... subsystemTags) {
            Q result = Common.empty();

            Q targetFunctions = getTargetFunctionsForContainedCallsites(context);
            Q subsystemFunctions = Subsystems.getSubsystemContents(subsystemTags).nodes(XCSG.Function);
            result = Common.edges(XCSG.Call).between(targetFunctions,subsystemFunctions);

            Q callsiteContainers = getCallsiteContainers(context);
            Q summaryCallEdges = Common.edges(Attr.Edge.PER_CONTROL_FLOW).betweenStep(callsiteContainers, result);
            return summaryCallEdges.roots().nodes(XCSG.ControlFlow_Node);
        }

		private static Q getCallsiteContainers(Node context) {
			Q callsites = getCallsites(context);
			
			if(CommonQueries.isEmpty(callsites)) {
				return Common.empty();
			} 
			
			return callsites.parent();
		}
		
		private static Q getCallsites(Node context) {
			Q callsites = Common.empty();
			
			if(context.taggedWith(XCSG.Loop)) {
				callsites = callsites.union(LoopUtils.getCallsitesInsideLoop(Common.toQ(context)));
			} else if (context.taggedWith(XCSG.Function) || context.taggedWith(XCSG.ControlFlow_Node)) {
				callsites = callsites.union(Common.toQ(context).contained().nodes(XCSG.CallSite));
			} else if (context.taggedWith(XCSG.CallSite)) {
				callsites = callsites.union(Common.toQ(context).nodes(XCSG.CallSite));
			} 
			
			return callsites;
		}

        public static Q getTargetFunctionsForContainedCallsites(Node context) {
                Q callsites = getCallsites(context);
                Q targetFunctions = Common.empty();
                for(Node c : callsites.eval().nodes()) {
                	targetFunctions = targetFunctions.union(CallSiteAnalysis.getTargets(Common.toQ(c)));
                }
                return targetFunctions;
        }

}
