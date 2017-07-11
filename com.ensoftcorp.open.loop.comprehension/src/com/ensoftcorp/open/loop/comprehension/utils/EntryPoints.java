package com.ensoftcorp.open.loop.comprehension.utils;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.subsystems.Subsystems;
import com.ensoftcorp.open.java.commons.analysis.CommonQueries;
import com.ensoftcorp.open.java.commons.analyzers.JavaProgramEntryPoints;

public class EntryPoints {
	
	/*
	 * Control Flow entry points
	 */
	
	public static Q getAllCFEntryPoints() { 
		return getMainMethods().union(getHTTPRequestHandlers(), getMethodsAcceptingSocketConnections());
	}
	
	public static Q getMainMethods() {
		return JavaProgramEntryPoints.findMainMethods();
	}
	
	public static Q getHTTPRequestHandlers() {
		Q result = getAllOveriddenMethodsinClass("com.sun.net.httpserver.HttpHandler");
		result = result.union(getAllOveriddenMethodsinClass("javax.servlet.http.HttpServlet"));
		result = result.union(getAllOveriddenMethodsinClass("fi.iki.elonen.NanoHTTPD"));
//		Log.info("Found "+result.eval().nodes().size()+" HTTP request handler entry points");
		return result;
	}
	
	public static Q getMethodsAcceptingSocketConnections() {
		Q methods = CommonQueries.nodesContaining(Subsystems.getSubsystemContents("IO_SUBSYSTEM","NETWORK_SUBSYSTEM").contained().nodes(XCSG.Method),"ccept");
		Q callsites = CallSiteAnalysis.getCallSites(methods);
		Q methodsContainingCallsites = CommonQueries.getContainingMethods(callsites);
//		Log.info("Found "+methodsContainingCallsites.eval().nodes().size()+" methods accepting socket connections");
		return methodsContainingCallsites;
	}
	
	private static Q getAllOveriddenMethodsinClass(String binaryClassName){
		Q type = Common.universe().selectNode("binaryName", binaryClassName);
		Q methodsinType = type.children().nodes(XCSG.Method);
		return Common.universe().edges(XCSG.Overrides).predecessors(methodsinType);
	}
	
	/*
	 * Data Flow entry points
	 */
	
	public static Q getAllDFEntryPoints() { 
		return getMainMethodArgs().union(getHTTPRequestParams(),getSocketDataEntryPoints());
	}
	
	public static Q getMainMethodArgs() {
		Q mainMethods = JavaProgramEntryPoints.findMainMethods();
		AtlasSet<Node> args = new AtlasHashSet<Node>();
		
		for(Node main: mainMethods.eval().nodes()) {
			Q oneDimStringArray = Common.edges(XCSG.ArrayElementType).predecessors(Common.typeSelect("java.lang", "String")).selectNode(XCSG.Java.arrayTypeDimension, new java.lang.Integer(1));
			Q mainMethodFirstParam = CommonQueries.methodParameter(Common.toQ(main), 0).intersection(Common.edges(XCSG.TypeOf).predecessors(oneDimStringArray));
			args.add(mainMethodFirstParam.eval().nodes().getFirst());
		}
//		Log.info("Found "+args.size()+" arguments to main methods");
		return Common.toQ(args);
	}
	
	public static Q getHTTPRequestParams() {
		Q httpRequestHandlers = getHTTPRequestHandlers();
		Q httpServletRequestObjects = Common.universe().selectNode(XCSG.name,"javax.servlet.http.HttpServletRequest").predecessors(Common.edges(XCSG.TypeOf)).intersection(httpRequestHandlers.contained());
//		Log.info("Found "+httpServletRequestObjects.eval().nodes().size()+" HTTP servlet request objects");
		return httpServletRequestObjects;
	}
	
	public static Q getSocketDataEntryPoints() {
		Q methodsAcceptingSocketConnections = getMethodsAcceptingSocketConnections();
		Q socketType = Common.typeSelect("java.net", "Socket");
		Q socketGetInputStreamCallsites = CommonQueries.nodesStartingWith(socketType.contained().nodes(XCSG.Method),"getInputStream").predecessorsOn(Common.edges(XCSG.InvokedSignature)).nodes(XCSG.CallSite);
		Q socketDataEntryPoints = socketGetInputStreamCallsites.intersection(methodsAcceptingSocketConnections.contained());
//		Log.info("Found "+socketDataEntryPoints.eval().nodes().size()+" HTTP request parameter objects");
		return socketDataEntryPoints;
	}
}
