package com.ensoftcorp.open.loop.comprehension.taint;

import org.eclipse.core.runtime.IProgressMonitor;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.loop.comprehension.log.Log;

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
	public static final String IDENTIFIER = "com.ensoftcorp.open.loop.comprehension.taint.callsiteoperatortaint";
	
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
	public void performIndexing(IProgressMonitor monitor) {
		Log.info("Adding CallSite-Operator taint overlay...");
		TaintOverlay.run(monitor);
	}

}
