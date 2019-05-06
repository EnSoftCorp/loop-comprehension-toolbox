package com.kcsl.loop.codemap.stages;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.kcsl.loop.codemap.log.Log;
import com.kcsl.loop.preferences.LoopPreferences;
import com.kcsl.loop.taint.analysis.TaintOverlay;

/**
 * Runs the callsite operator taint overlay
 * 
 * @author Jon Mathews, refinement logic
 * @author Ben Holland, conversion to prioritized codemap
 */
public class CallSiteOperatorTaintOverlayCodemapStage extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the codemap stage
	 */
	public static final String IDENTIFIER = "com.kcsl.loop.codemap.callsiteoperatortaint";
	
	@Override
	public String getDisplayName() {
		return "CallSite-Operator Taint Overlay";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{}; // no dependencies
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		boolean runIndexer = LoopPreferences.isCallsiteTaintOverlayEnabled();
		if(runIndexer){
			Log.info("Adding CallSite-Operator taint overlay...");
			TaintOverlay.run(monitor);
		}
		return runIndexer;
	}

}
