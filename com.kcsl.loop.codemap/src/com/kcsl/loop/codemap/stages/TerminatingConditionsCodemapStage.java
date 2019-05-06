package com.kcsl.loop.codemap.stages;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentificationCodemapStage;
import com.kcsl.loop.preferences.LoopPreferences;

/**
 * Runs the terminating conditions tagging logic
 * 
 * @author Jon Mathews, tagging logic
 * @author Ben Holland, conversion to prioritized codemap
 */
public class TerminatingConditionsCodemapStage extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the codemap stage
	 */
	public static final String IDENTIFIER = "com.kcsl.loop.codemap.terminatingconditions";
	
	@Override
	public String getDisplayName() {
		return "Terminating Conditions";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{ DecompiledLoopIdentificationCodemapStage.IDENTIFIER };
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		boolean runIndexer = LoopPreferences.isIdentifyTerminatingConditionsEnabled();
		// TODO: Discuss with Jon and remove this because Termination condition = Boundary condition is the current understanding (Ganesh, Ben, Payas)
//		if(runIndexer){
//			Log.info("Adding loop terminating conditions tags...");
//			TerminatingConditions.tagTerminatingConditions();
//		}
		return runIndexer;
	}

}
