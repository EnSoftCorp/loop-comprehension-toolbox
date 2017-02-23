package com.ensoftcorp.open.loop.comprehension.taint;

import static com.ensoftcorp.atlas.core.query.Query.universe;

import org.eclipse.core.runtime.IProgressMonitor;
import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.index.Index;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.G;
import com.ensoftcorp.open.java.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.loop.comprehension.log.Log;

public class TaintOverlay {

	private static final String INDEX = Index.INDEX_VIEW_TAG;
	
	
	private static final String OVERLAY_OPERATOR = "Overlay.Taint.Operator";

	
	public static final String OVERLAY_TAINT_BYPASSED_INTERPROCEDURAL_DATA_FLOW = "Overlay.Taint.Bypassed_InterproceduralDataFlow";
	
	
	/** From Constructor CallSite to Actual Identity (models Taint from Parameter to Identity) */
	public static final String OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT = "Overlay.Taint.OperatorConstructor.ShortCircuit";

	
	public static final String OVERLAY_OPERATOR_CONSTRUCTOR = "Overlay.Taint.OperatorConstructor";
	
	
	public static final String Taint = "TAINT";
	
	public static void run(IProgressMonitor monitor) {
		try {
			// Apply Taint tag to all DataFlow edges, unless specifically excluded by CallSite conversion
			AtlasSet<Edge> excluded = new AtlasHashSet<Edge>();
			
			// TODO: include other JDK jar files
			
			Q jdkMethods1 = Common.nodes(XCSG.Library).selectNode(XCSG.name, "rt.jar").contained().nodesTaggedWithAny(XCSG.Method);
			Q jdkMethods2 = Common.nodes(XCSG.Library).selectNode(XCSG.name, "jce.jar").contained().nodesTaggedWithAny(XCSG.Method);
			Q jdkMethods3 = Common.nodes(XCSG.Library).selectNode(XCSG.name, "jsse.jar").contained().nodesTaggedWithAny(XCSG.Method);
			Q jdkMethods = Common.resolve(null, jdkMethods1.union(jdkMethods2, jdkMethods3));
			
			
			// For all CallSites to APIs
			//  convert every CallSite that resolves solely to methods in the API
			
			Q callsites = universe().nodesTaggedWithAny(XCSG.CallSite);
			
			monitor.beginTask("Creating taint overlay for selected call sites", (int)callsites.eval().nodes().size());
			
			AtlasSet<Node> ns = callsites.eval().nodes();
			for (Node callsite : ns) {
				Q targetMethods = CallSiteAnalysis.getTargetMethods(callsite);
				AtlasSet<Node> nonJDKtargets = targetMethods.difference(jdkMethods).eval().nodes();
				if (nonJDKtargets.isEmpty()) {
					// all targets are in JDK
					
					try {
						createOperatorOverlayForCallSite(Graph.U, callsite, excluded);
					} catch (Exception e) {
						Log.error("Error on call site: " + callsite.toString(), e);
					}
					
				} else {
					if (nonJDKtargets.size() != targetMethods.eval().nodes().size()) {
						// TODO: some, not all in JDK - consider overlay for all methods in JDK, but retain the connections to application specific methods
						System.out.println("Skipping " + callsite.toString());
					}
				}
				
				monitor.worked(1);
			}
			
			// Apply Taint to all DataFlow not specifically excluded by CallSite->Operator conversion
			Q dfe = Common.resolve(null, universe().edgesTaggedWithAny(XCSG.DataFlow_Edge));
			for (GraphElement df : dfe.eval().edges()) {
				if (excluded.contains(df))
					continue;
				df.tag(Taint);
			}

			// Add edges for array identity of length operator
			Q arrayIdentity = Common.resolve(null, universe().edgesTaggedWithAny(XCSG.Java.ArrayIdentityForLengthOperator));
			for (GraphElement edge : arrayIdentity.eval().edges()) {
				edge.tag(Taint);
			}
			// Add edges for array identity of array access
			arrayIdentity = Common.resolve(null, universe().edgesTaggedWithAny(XCSG.ArrayIdentityFor));
			for (GraphElement edge : arrayIdentity.eval().edges()) {
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
						
			GraphElement targetMethod = G.out(g,callsite,XCSG.InvokedFunction);
			
			if (targetMethod.taggedWith(XCSG.Constructor)) {
				overlayForConstructor(g, callsite, excluded);
				return;
			}
		}
		
		callsite.tag(OVERLAY_OPERATOR);
		
		AtlasSet<Edge> passedToN = G.inEdges(g,callsite,XCSG.PassedTo);
		
		for (GraphElement passedTo : passedToN) {
			Node callInputPass = passedTo.getNode(EdgeDirection.FROM);
			AtlasSet<Edge> paramFlows = G.outEdges(g,callInputPass,XCSG.InterproceduralDataFlow);
			bypassFlows(paramFlows, excluded);
		}
		
		AtlasSet<Edge> returnFlow = G.inEdges(g,callsite,XCSG.InterproceduralDataFlow);
		bypassFlows(returnFlow, excluded);

		
		for (GraphElement passedTo : passedToN) {
			updateTaint(passedTo);
		}
	}

	private static void overlayForConstructor(Graph g, Node callsite, AtlasSet<Edge> excluded) {
		callsite.tag(OVERLAY_OPERATOR_CONSTRUCTOR);

		// short circuit from CallInputs back to Identity predecessor - the Instantiation
						
		AtlasSet<Edge> passedToN = G.inEdges(g,callsite,XCSG.PassedTo); 
		for (GraphElement passedTo : passedToN) {
			Node callInputPass = passedTo.getNode(EdgeDirection.FROM);
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
		for (GraphElement passedTo : passedToN) {
			updateTaint(passedTo);					
		}
		
		return;
	}

	private static void insertTaintFromCallSiteToActualIdentity(GraphElement callsite, GraphElement actualIdentity) {
		// XXX: creates a loop in data, confuses ordering; could introduce an extra node to make order more explicit
		GraphElement taintEdge = insertTaint(callsite, actualIdentity);
		taintEdge.tag(INDEX);
		taintEdge.tag(OVERLAY_OPERATOR_CONSTRUCTOR_SHORT_CIRCUIT);
	}

	/**
	 * New edge required to represent tainting relationship
	 * @param from
	 * @param to
	 */
	private static GraphElement insertTaint(GraphElement from, GraphElement to) {
		GraphElement edge = Graph.U.createEdge(from, to);
		edge.tag(Taint);
		return edge;
	}

	/**
	 * Overload meaning of existing edge {DataFlow_Edge, PassedTo} to include tainting relationship
	 * @param edge
	 */
	private static void updateTaint(GraphElement edge) {
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
