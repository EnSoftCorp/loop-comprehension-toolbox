package com.kcsl.loop.codemap.stages;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentificationCodemapStage;
import com.kcsl.loop.codemap.log.Log;
import com.kcsl.loop.lcg.EnhancedLoopCallGraph;
import com.kcsl.loop.preferences.LoopPreferences;

/**
 * Runs the Enhanced Loop Call Graph overlay
 * 
 * @author Jon Mathews
 */
public class EnhancedLoopCallGraphOverlayCodemapStage extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the codemap stage
	 */
	public static final String IDENTIFIER = "com.kcsl.loop.codemap.enhancedloopcallgraph";
	
	@Override
	public String getDisplayName() {
		return "Enhanced Loop Call Graph Overlay";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{DecompiledLoopIdentificationCodemapStage.IDENTIFIER};
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		boolean runIndexer = LoopPreferences.isEnhancedLoopCallGraphOverlayEnabled();
		if(runIndexer){
			Log.info("Adding Enhanced Loop Call Graph Overlay...");
			new EnhancedLoopCallGraph().run(monitor);
		}
		return runIndexer;
	}

}
