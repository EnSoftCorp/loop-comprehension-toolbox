package com.kcsl.loop.taint.analysis;

import static com.ensoftcorp.atlas.core.script.Common.toGraph;
import static com.ensoftcorp.atlas.core.script.Common.toQ;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.db.set.SingletonAtlasSet;
import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.highlight.Highlighter.ConflictStrategy;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.loop.util.Lookup;
import com.kcsl.loop.util.Traversal;

import net.ontopia.utils.CompactHashMap;

public class ApplicationSpecificTaintConfiguration {
	
	public static final String SECRET = "SECRET";
	public static final String INPUT = "INPUT";
	
	public static class Tainting {
		
		@SuppressWarnings("unused")
		private MainArgs mainArgs;

		public Tainting(MainArgs mainArgs) {
			this.mainArgs = mainArgs;
		}

		public void apply() {
//			initExample1(mainArgs);
//			initExample2(mainArgs);
//			initExample3(mainArgs);
		}
		
		@SuppressWarnings("unused")
		private void initExample1(MainArgs mainArg) {
			// ex1
			taintMainArrayRead(mainArg, SECRET, 1);
			taintMainArrayRead(mainArg, INPUT, 2);
		}
		
		@SuppressWarnings("unused")
		private void initExample2(MainArgs mainArg) {
 			// ex2
			taintStdin(INPUT);
			taintMainArrayRead(mainArg, "INPUT.BUCKETS", 0);
		}
		
		@SuppressWarnings("unused")
		private void initExample3(MainArgs mainArg) {
			// ex3
			taintMainArrayRead(mainArg, SECRET, 0);
			taintMainArrayRead(mainArg, INPUT, 1);
		}
		
		private Q dataflow(Q origin) {
			Q flow = origin.forwardOn(qContext().edges(TaintOverlay.Taint));
			return flow;
		}

		private void taintMainArrayRead(MainArgs mainArg, String tag, int i) {
			Q arrayRead = mainArg.getArg(i);
			Q flow = dataflow(arrayRead);
			Util.setTag(flow, tag);
		}
		
		private void taintStdin(String tag) {
			Q in0 = Lookup.findField("java.lang.System", "in");
			Q flow = dataflow(in0);
			Util.setTag(flow, tag);
		}
		

		public static Highlighter h() {
			Highlighter h = new Highlighter(ConflictStrategy.LAST_MATCH);
			
			h.highlightNodes(qContext().nodes(INPUT), java.awt.Color.BLUE);
			h.highlightEdges(qContext().edges(INPUT), java.awt.Color.BLUE);
			h.highlightNodes(qContext().nodes(SECRET), java.awt.Color.RED);
			h.highlightEdges(qContext().edges(SECRET), java.awt.Color.RED);
			h.highlightNodes(qContext().nodesTaggedWithAll(SECRET, INPUT), java.awt.Color.YELLOW);
			h.highlightEdges(qContext().edgesTaggedWithAll(SECRET, INPUT), java.awt.Color.YELLOW);
			
			return h;
		}
		
		public static Q allTaints() {
			return qContext().nodes(SECRET, INPUT).induce(qContext().edges(SECRET, INPUT));
		}
	}
	
	public static class MainArgs {
		/* map from index value to ArrayAccess reached */
		private CompactHashMap<Integer, AtlasSet<Node>> arrayDereferences;
		
		/**
		 * 
		 * @param given i, find args[x] where x==i or x==?
		 * 
		 * @return possible ArrayRead
		 * 
		 */
		public Q getArg(Integer i) {
			AtlasSet<Node> accessAny = arrayDereferences.get(null);
			AtlasSet<Node> accessGiven = arrayDereferences.get(i);
			AtlasSet<Node> access = new AtlasHashSet<Node>();
			if (accessAny != null)
				access.addAll(accessAny);
			if (accessGiven != null)
				access.addAll(accessGiven);
			
			return toQ(toGraph(access));
		}

		/**
		 * 
		 * @param gContext
		 * @param mainMethod
		 * @param argIndex
		 * 
		 * 
		 */
		public static MainArgs mainArg(Graph gContext, Node mainMethod) {
			Q u = Query.toQ(gContext);
			
			CompactHashMap<Integer, AtlasSet<Node>> arrayDereferences = new CompactHashMap<>();		
			
			Edge hasParameter = gContext.edges(mainMethod, NodeDirection.OUT).taggedWithAll(XCSG.HasParameter).one();
			Node parameter = hasParameter.to();
			
			// flow forward until ArrayRead
			
			SingletonAtlasSet<Node> origin = new SingletonAtlasSet<Node>(parameter);
			AtlasSet<Node> stop = gContext.nodes().taggedWithAll(XCSG.ArrayRead);
			
			Graph context = u.edges(XCSG.DataFlow_Edge, XCSG.ArrayIdentityFor).eval();
			
			Graph argFlow = Traversal.traverse(context, TraversalDirection.FORWARD, origin, stop);
			
			AtlasSet<Node> accesses = argFlow.nodes().taggedWithAny(XCSG.ArrayRead);
			
			for (Node access : accesses) {
				// for each root, classify as Literal x or maybe (-1)
				// FIXME: should stop at intermediate operations {!Assignment,!Variable}
				
				Edge arrayIndexFor = gContext.edges(access, NodeDirection.IN).taggedWithAll(XCSG.ArrayIndexFor).one();
				Node index = arrayIndexFor.from();
				Graph reverseDF = Traversal.reverseDF(new SingletonAtlasSet<Node>(index));
				
				AtlasSet<Node> roots = Common.toQ(reverseDF.roots()).eval().nodes();
				for (Node root : roots) {
					
					Integer literalValue = Util.getLiteralValue(root);
		
					AtlasSet<Node> s = arrayDereferences.get(literalValue);
					if (s == null) {
						s = new AtlasHashSet<Node>();
						arrayDereferences.put(literalValue, s);
					}
					
					s.add(access);
				}
			}
			
			MainArgs mainArgs = new MainArgs();
			mainArgs.arrayDereferences = arrayDereferences;
			return mainArgs;
		}

	}
	
	public static class Util {

		/**
		 * 
		 * @param ge
		 * @return the Integer value, or null if not applicable, e.g. ge is not a Literal
		 */
		public static Integer getLiteralValue(Node node) {
			if (node.taggedWith(XCSG.Literal)) {
				return Integer.decode((String)node.getAttr(XCSG.name));
			}
			
			return null;
		}

		public static void setTag(Q q, String tag) {
			q = Common.resolve(null, q);
			for (Node node : q.eval().nodes()) {
				node.tag(tag);
			}
			for (Edge edge : q.eval().edges()) {
				edge.tag(tag);
			}
		}
		
	}
	
	public static Graph gContext() {
		Graph gContext = Graph.U;
		return gContext;
	}
	
	public static Q qContext() {
		Q u = Query.toQ(gContext());
		return u;
	}

	public static MainArgs init() {
		Q u = qContext();
		
		// TODO: select a specific main method
		Node main = u.methods("main").eval().nodes().one(); 
		if (main != null) {
			MainArgs mainArg = MainArgs.mainArg(gContext(), main);
			return mainArg;
		}
		return null;
	}
	
	public static void run(IProgressMonitor monitor) {
		MainArgs mainArgs = ApplicationSpecificTaintConfiguration.init();
		if (mainArgs != null) {
			Tainting tainting = new Tainting(mainArgs);			
			tainting.apply();
		}
	}
	
}
