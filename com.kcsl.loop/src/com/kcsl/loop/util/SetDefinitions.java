package com.kcsl.loop.util;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.kcsl.loop.preferences.LoopPreferences;

public class SetDefinitions {

	// hide constructor
	private SetDefinitions() {}

	/**
	 * A collection of specifically just the JDK libraries
	 * @return
	 */
	public static Q JDKLibraries(){
		if(LoopPreferences.isAnalyzeJimpleEnabled()){
			return com.ensoftcorp.open.jimple.commons.analysis.SetDefinitions.JDKLibraries(); 
		} else {
			return com.ensoftcorp.open.java.commons.analysis.SetDefinitions.JDKLibraries(); 
		}
	}
	
	/**
	 * Types which represent arrays of other types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q arrayTypes() {
		if(LoopPreferences.isAnalyzeJimpleEnabled()){
			return com.ensoftcorp.open.jimple.commons.analysis.SetDefinitions.arrayTypes(); 
		} else {
			return com.ensoftcorp.open.java.commons.analysis.SetDefinitions.arrayTypes(); 
		}
	}

	/**
	 * Types which represent language primitive types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q primitiveTypes() {
		if(LoopPreferences.isAnalyzeJimpleEnabled()){
			return com.ensoftcorp.open.jimple.commons.analysis.SetDefinitions.primitiveTypes(); 
		} else {
			return com.ensoftcorp.open.java.commons.analysis.SetDefinitions.primitiveTypes(); 
		}
	}

	/**
	 * Everything declared under any of the known API projects, if they are in
	 * the index.
	 */
	public static Q libraries() {
		if(LoopPreferences.isAnalyzeJimpleEnabled()){
			return com.ensoftcorp.open.jimple.commons.analysis.SetDefinitions.libraries(); 
		} else {
			return com.ensoftcorp.open.java.commons.analysis.SetDefinitions.libraries(); 
		}
	}
	
	/**
	 * Everything in the universe which is part of the app (not part of the
	 * libraries, or any "floating" nodes).
	 * 
	 * For Jimple everything can appear under a library, so we define the application with respect to the JDK libraries
	 */
	public static Q app() {
		Q app;
		if(LoopPreferences.isAnalyzeJimpleEnabled()){
			app = com.ensoftcorp.open.jimple.commons.analysis.SetDefinitions.app(); 
		} else {
			app = com.ensoftcorp.open.java.commons.analysis.SetDefinitions.app(); 
		}
		Q appPackages = app.nodes(XCSG.Package);
		
		Q e5Package1 = CommonQueries.nodesStartingWith(appPackages, "com.ainfosec");
		Q e5Package2 = CommonQueries.nodesStartingWith(appPackages, "com.bbn");
		Q e5Package3 = CommonQueries.nodesStartingWith(appPackages, "com.stac");
		Q e5Package4 = CommonQueries.nodesStartingWith(appPackages, "com.cyberpointllc");
		Q e5Package5 = CommonQueries.nodesStartingWith(appPackages, "soot.dummy"); // include lamda code
		appPackages = e5Package1.union(e5Package2, e5Package3, e5Package4, e5Package5);
		return Common.toQ(appPackages.containers().union(appPackages.contained()).intersection(app).eval().nodes());
	}
	
}
