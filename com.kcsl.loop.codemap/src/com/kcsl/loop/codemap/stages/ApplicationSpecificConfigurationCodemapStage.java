package com.kcsl.loop.codemap.stages;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.kcsl.loop.codemap.log.Log;
import com.kcsl.loop.preferences.LoopPreferences;
import com.kcsl.loop.taint.analysis.ApplicationSpecificTaintConfiguration;

/**
 * Runs the callsite operator taint overlay
 * 
 * @author Jon Mathews, refinement logic
 * @author Ben Holland, conversion to prioritized codemap
 */
public class ApplicationSpecificConfigurationCodemapStage extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the codemap stage
	 */
	public static final String IDENTIFIER = "com.kcsl.loop.codemap.appspecific";
	
	@Override
	public String getDisplayName() {
		return "Application specific configurations";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{ CallSiteOperatorTaintOverlayCodemapStage.IDENTIFIER };
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		boolean runIndexer = LoopPreferences.isApplicationSpecificConfigurationEnabled();
		if(runIndexer){
			Log.info("Apply Input and Secret labels to taint overlay...");
			ApplicationSpecificTaintConfiguration.run(monitor);
		}
		return runIndexer;
	}

}
