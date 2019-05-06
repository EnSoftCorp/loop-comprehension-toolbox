package com.kcsl.loop.taint.analysis;

import static com.ensoftcorp.atlas.core.query.Query.universe;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.index.Index;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.G;
import com.kcsl.loop.taint.log.Log;
import com.kcsl.loop.util.SetDefinitions;
import com.kcsl.loop.xcsg.LoopXCSG;

public class TaintOverlay {

	private static final String INDEX = Index.INDEX_VIEW_TAG;
	
	@LoopXCSG
	public static final String OVERLAY_OPERATOR = "Overlay.Taint.Operator";

	@LoopXCSG
	public static final String OVERLAY_TAINT_BYPASSED_INTERPROCEDURAL_DATA_FLOW = "Overlay.Taint.Bypassed_InterproceduralDataFlow";
	
	@LoopXCSG
	/** From Constructor CallSite to Actual Identity (models Taint from Parameter to Identity) */
	public static final String OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT = "Overlay.Taint.OperatorConstructor.ShortCircuit";

	@LoopXCSG
	public static final String OVERLAY_OPERATOR_CONSTRUCTOR = "Overlay.Taint.OperatorConstructor";
	
	@LoopXCSG
	public static final String Taint = "TAINT";
	
	public static void run(IProgressMonitor monitor) {
		try {
			// Apply Taint tag to all DataFlow edges, unless specifically excluded by CallSite conversion
			AtlasSet<Edge> excluded = new AtlasHashSet<Edge>();
			
			Q jdkMethods = Query.resolve(null, SetDefinitions.JDKLibraries().contained().nodes(XCSG.Method));
			Q otherLibraryMethods = Query.resolve(null, SetDefinitions.libraries().contained().nodes(XCSG.Method));
			Q nonAppMethods = Query.resolve(null, jdkMethods.union(otherLibraryMethods));
			
			// For all CallSites to APIs
			//  convert every CallSite that resolves solely to methods in the API
			
			Q callsites = universe().nodes(XCSG.CallSite);
			
			monitor.beginTask("Creating taint overlay for selected call sites", (int)callsites.eval().nodes().size());
			
			AtlasSet<Node> ns = callsites.eval().nodes();
			for (Node callsite : ns) {
				Q targetMethods = Common.toQ(CallSiteAnalysis.getTargets(callsite));
				AtlasSet<Node> apptargets = targetMethods.difference(nonAppMethods).eval().nodes();
				if (apptargets.isEmpty()) {
					// all targets are in libraries outside of app
					try {
						createOperatorOverlayForCallSite(Graph.U, callsite, excluded);
					} catch (Exception e) {
						Log.error("Error on call site: " + callsite.toString(), e);
					}
				} else {
					if (apptargets.size() != targetMethods.eval().nodes().size()) {
						// TODO: some in app, some in libraries. Consider overlay for all methods in libraries, 
						// but retain the connections to application specific methods
//						Log.info("Skipping " + callsite.toString());
					}
				}
				
				monitor.worked(1);
			}
			
			// Apply Taint to all DataFlow not specifically excluded by CallSite->Operator conversion
			Q dfe = Common.resolve(null, universe().edges(XCSG.DataFlow_Edge));
			for (Edge df : dfe.eval().edges()) {
				if (excluded.contains(df)) {
					continue;
				}
				df.tag(Taint);
			}

			// Add edges for array identity of length operator
			Q arrayIdentity = Common.resolve(null, universe().edges(XCSG.Java.ArrayIdentityForLengthOperator));
			for (Edge edge : arrayIdentity.eval().edges()) {
				edge.tag(Taint);
			}
			// Add edges for array identity of array access
			arrayIdentity = Common.resolve(null, universe().edges(XCSG.ArrayIdentityFor));
			for (Edge edge : arrayIdentity.eval().edges()) {
				edge.tag(Taint);
			}
			
		} catch (Exception e) {
			Log.error("CallSite conversion failed",e);
		} finally {
			monitor.done();
		}
		
	}

	private static void createOperatorOverlayForCallSite(Graph g, Node callsite, AtlasSet<Edge> excluded) {
		if (callsite.taggedWith(XCSG.ObjectOrientedStaticCallSite)) {
			Node targetMethod = G.out(g,callsite,XCSG.InvokedFunction);
			if (targetMethod.taggedWith(XCSG.Constructor)) {
				overlayForConstructor(g, callsite, excluded);
				return;
			}
		}
		
		callsite.tag(OVERLAY_OPERATOR);
		AtlasSet<Edge> passedToN = G.inEdges(g,callsite,XCSG.PassedTo);
		
		for (Edge passedTo : passedToN) {
			Node callInputPass = passedTo.from();
			AtlasSet<Edge> paramFlows = G.outEdges(g,callInputPass,XCSG.InterproceduralDataFlow);
			bypassFlows(paramFlows, excluded);
		}
		
		AtlasSet<Edge> returnFlow = G.inEdges(g,callsite,XCSG.InterproceduralDataFlow);
		bypassFlows(returnFlow, excluded);

		for (Edge passedTo : passedToN) {
			updateTaint(passedTo);
		}
	}

	private static void overlayForConstructor(Graph g, Node callsite, AtlasSet<Edge> excluded) {
		callsite.tag(OVERLAY_OPERATOR_CONSTRUCTOR);

		// short circuit from CallInputs back to Identity predecessor - the Instantiation
						
		AtlasSet<Edge> passedToN = G.inEdges(g,callsite,XCSG.PassedTo); 
		for (Edge passedTo : passedToN) {
			Node callInputPass = passedTo.from();
			AtlasSet<Edge> paramFlows = G.outEdges(g,callInputPass,XCSG.InterproceduralDataFlow);
			bypassFlows(paramFlows, excluded);
			
		}
		
		// There is no return flow from the constructor; make one from the Constructor to the Instantiation
		
		Node identity = G.in(g,callsite,XCSG.IdentityPassedTo);
		
		while (!identity.taggedWith(XCSG.Instantiation) && !identity.taggedWith(XCSG.Identity)) {
			identity = G.in(g,identity,XCSG.LocalDataFlow);
		}
		
		insertTaintFromCallSiteToActualIdentity(callsite, identity);

		// overload PassedTo edges to model Taint from actual parameters (ParameterPass) to actual return (CallSite)
		for (Edge passedTo : passedToN) {
			updateTaint(passedTo);					
		}
		
		return;
	}

	private static void insertTaintFromCallSiteToActualIdentity(Node callsite, Node actualIdentity) {
		// XXX: creates a loop in data, confuses ordering; could introduce an extra node to make order more explicit
		Edge taintEdge = insertTaint(callsite, actualIdentity);
		taintEdge.tag(INDEX);
		taintEdge.tag(OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT);
	}

	/**
	 * New edge required to represent tainting relationship
	 * @param from
	 * @param to
	 */
	private static Edge insertTaint(Node from, Node to) {
		Edge edge = Graph.U.createEdge(from, to);
		edge.tag(Taint);
		return edge;
	}

	/**
	 * Overload meaning of existing edge {DataFlow_Edge, PassedTo} to include tainting relationship
	 * @param edge
	 */
	private static void updateTaint(Edge edge) {
		edge.tag(Taint);
	}
	
	private static void bypassFlows(AtlasSet<Edge> paramFlows, AtlasSet<Edge> excluded) {
		for (Edge paramFlow : paramFlows) {
			bypassFlow(paramFlow, excluded);
		}
	}

	private static void bypassFlow(Edge paramFlow, AtlasSet<Edge> excluded) {
		excluded.add(paramFlow);
		paramFlow.tag(OVERLAY_TAINT_BYPASSED_INTERPROCEDURAL_DATA_FLOW);
	}

}
