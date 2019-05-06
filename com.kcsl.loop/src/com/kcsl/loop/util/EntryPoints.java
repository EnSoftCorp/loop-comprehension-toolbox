package com.kcsl.loop.util;

import java.util.ArrayList;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.subsystems.Subsystems;
import com.ensoftcorp.open.java.commons.analysis.CommonQueries;
import com.ensoftcorp.open.java.commons.analyzers.JavaProgramEntryPoints;
import com.kcsl.loop.xcsg.LoopXCSG;

public class EntryPoints {
	
	/*
	 * Entry Point ID.
	 * Used in the reachability codemap stage
	 */
	@LoopXCSG // populated by Atlas
	private static final String XCSG_BINARY_NAME = "binaryName";
	@LoopXCSG // attribute key; value = "E" + number
	public static final String ENTRY_POINT_ID = "ENTRY_POINT_ID";
	
	static List<EP> entryPoints = new ArrayList<EP>();
	public static EP mainEP = new MainEP();
	public static EP sunHttpRequestEP= new SunHttpRequestEP();
	public static EP httpServletEP = new HttpServletEP();
	public static EP nanoHttpDEP = new NanoHttpDEP();
	public static EP springFrameworkEP= new SpringFrameworkEP();
	public static EP sparkEP = new SparkEP();
	public static EP socketEP = new SocketEP();
	public static EP nettyEP = new NettyEP();
	
	static {
		entryPoints.add(mainEP);
		entryPoints.add(sunHttpRequestEP);
		entryPoints.add(httpServletEP);
		entryPoints.add(nanoHttpDEP);
		entryPoints.add(springFrameworkEP);
		entryPoints.add(sparkEP);
		entryPoints.add(socketEP);
		entryPoints.add(nettyEP);
	}
	
	/*
	 * Control Flow entry points
	 */
	
	public static Q getAllCFEntryPoints() { 
		Q res = Common.empty();
		for (EP ep: entryPoints) {
			res = res.union(ep.getControlFlowEP());
		}
		return res;
	}
	
	/*
	 * Data Flow entry points
	 */
	
	public static Q getAllDFEntryPoints() { 
		Q res = Common.empty();
		for (EP ep: entryPoints) {
			res = res.union(ep.getDataFlowEP());
		}
		return res;	
	}
	
	public static String[] getAllDesc() { 
		String [] res = new String[entryPoints.size()];
		for (int i=0;i<entryPoints.size();i++) {
			res[i] = entryPoints.get(i).getDesc();
		}
		return res;	
	}
	
	
	private static Q getAllOveriddenMethodsinClass(String binaryClassName){
		Q type = Query.universe().selectNode(XCSG_BINARY_NAME, binaryClassName);
		Q methodsinType = type.children().nodes(XCSG.Method);
		return Query.universe().edges(XCSG.Overrides).predecessors(methodsinType);
	}
	
	public static interface EP{
		public Q getControlFlowEP();
		public Q getDataFlowEP();
		public String getDesc();
	}
	
	public static class MainEP implements EP{

		@Override
		public Q getControlFlowEP() {
			return JavaProgramEntryPoints.findMainMethods();
		}

		@Override
		public Q getDataFlowEP() {
			Q mainMethods = JavaProgramEntryPoints.findMainMethods();
			AtlasSet<Node> args = new AtlasHashSet<Node>();
			
			for(Node main: mainMethods.eval().nodes()) {
				Q oneDimStringArray = Common.edges(XCSG.ArrayElementType).predecessors(Common.typeSelect("java.lang", "String")).selectNode(XCSG.Java.arrayTypeDimension, new java.lang.Integer(1));
				Q mainMethodFirstParam = CommonQueries.methodParameter(Common.toQ(main), 0).intersection(Common.edges(XCSG.TypeOf).predecessors(oneDimStringArray));
				args.add(mainMethodFirstParam.eval().nodes().one());
			}
//			Log.info("Found "+args.size()+" arguments to main methods");
			return Common.toQ(args);
		}

		@Override
		public String getDesc() {
			return "java Main Method.";
		}
	}
	
	static class SunHttpRequestEP implements EP{
		@Override
		public Q getControlFlowEP() {
			return getAllOveriddenMethodsinClass("com.sun.net.httpserver.HttpHandler");		}

		@Override
		public Q getDataFlowEP() {
			Q httpRequestHandlers = getControlFlowEP();
			Q httpServletRequestObjects = Query.universe().selectNode(XCSG_BINARY_NAME,"javax.servlet.http.HttpServletRequest").predecessorsOn(Common.edges(XCSG.TypeOf)).intersection(httpRequestHandlers.contained());
//			Log.info("Found "+httpServletRequestObjects.eval().nodes().size()+" HTTP servlet request objects");
			return httpServletRequestObjects;
		}

		@Override
		public String getDesc() {
			return "com.sun.net.httpserver.HttpHandler request handlers.";
		}
	}
	

	static class HttpServletEP implements EP{
		@Override
		public Q getControlFlowEP() {
			return getAllOveriddenMethodsinClass("javax.servlet.http.HttpServlet");
		}
		@Override
		public Q getDataFlowEP() {
			// FIXME 
			return Common.empty();
		}
		@Override
		public String getDesc() {
			return "javax.servlet.http.HttpServlet request handlers.";
		}
	}
	
	static class NanoHttpDEP implements EP{
		@Override
		public Q getControlFlowEP() {
			return getAllOveriddenMethodsinClass("fi.iki.elonen.NanoHTTPD");
		}
		@Override
		public Q getDataFlowEP() {
			// FIXME 
			return Common.empty();
		}
		@Override
		public String getDesc() {
			return "fi.iki.elonen.NanoHTTPD request handlers.";
		}
	}
	
	
	static class NettyEP implements EP{
		@Override
		public Q getControlFlowEP() {
			return Query.universe().edges(XCSG.Overrides).predecessors(CommonQueries.nodesContaining(Subsystems.getSubsystemContents("IO_SUBSYSTEM","NETWORK_SUBSYSTEM").contained().nodes(XCSG.Method),"channelRead0"));
		}
		@Override
		public Q getDataFlowEP() {
			// FIXME 
			return Common.empty();
		}
		@Override
		public String getDesc() {
			return "Netty ChannelRead0 method.";
		}
	}
	
	static class SpringFrameworkEP implements EP{
		@Override
		public Q getControlFlowEP() {
			return getEntryPointMethodsForSpringFramework();
		}
		@Override
		public Q getDataFlowEP() {
			// FIXME 
			return Common.empty();
		}
		
		/**
		 * The methods are annotated with RequestMapping Annotation. Return all the methods that are tagged with this annotation
		 * FIXME: NAR: We are not checking if the RequestMapping is part of say example "org.springframework.web.bind.annotation" class
		 * at this point. So we might get false positives, but we don't want to miss these if the package structure changes.
		 * @return
		 */
		private static Q getEntryPointMethodsForSpringFramework(){
			return Query.universe().nodes(XCSG.Java.Annotation).selectNode(XCSG.name, "RequestMapping").
					reverseStepOn(Query.universe().edges(XCSG.Java.AnnotatedWith)).nodes(XCSG.Method);
		}
		@Override
		public String getDesc() {
			return "Spring framework entrypoints - methods annotated with RequestMapping Annotation.";
		}
		
	}
	
	static class SparkEP implements EP{
		@Override
		public Q getControlFlowEP() {
			return getAllOveriddenMethodsinClass("spark.Route");
		}
		@Override
		public Q getDataFlowEP() {
			// FIXME 
			return Common.empty();
		}
		@Override
		public String getDesc() {
			return "spark.Spark request handlers.";
		}
	}
	
	static class SocketEP implements EP{
		@Override
		public Q getControlFlowEP() {
			return getMethodsAcceptingSocketConnections();
		}
		@Override
		public Q getDataFlowEP() {
			return getSocketDataEntryPoints();
		}
		
		
		public static Q getMethodsAcceptingSocketConnections() {
			Q methods = CommonQueries.nodesContaining(Subsystems.getSubsystemContents("IO_SUBSYSTEM","NETWORK_SUBSYSTEM").contained().nodes(XCSG.Method),"ccept");
			Q callsites = CallSiteAnalysis.getCallSites(methods);
			Q methodsContainingCallsites = CommonQueries.getContainingMethods(callsites);
//			Log.info("Found "+methodsContainingCallsites.eval().nodes().size()+" methods accepting socket connections");
			return methodsContainingCallsites;
		}
	

			
		
		public static Q getSocketDataEntryPoints() {
			Q methodsAcceptingSocketConnections = getMethodsAcceptingSocketConnections();
			Q socketType = Common.typeSelect("java.net", "Socket");
			Q socketGetInputStreamCallsites = CommonQueries.nodesStartingWith(socketType.contained().nodes(XCSG.Method),"getInputStream").predecessorsOn(Common.edges(XCSG.InvokedSignature)).nodes(XCSG.CallSite);
			Q socketDataEntryPoints = socketGetInputStreamCallsites.intersection(methodsAcceptingSocketConnections.contained());
//			Log.info("Found "+socketDataEntryPoints.eval().nodes().size()+" HTTP request parameter objects");
			return socketDataEntryPoints;
		}
		@Override
		public String getDesc() {
			return "java.net.Socket entry points";
		}
	}
	
	
}
