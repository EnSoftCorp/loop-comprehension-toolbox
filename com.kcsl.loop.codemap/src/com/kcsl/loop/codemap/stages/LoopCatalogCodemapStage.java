package com.kcsl.loop.codemap.stages;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditionsCodemapStage;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentificationCodemapStage;
import com.kcsl.loop.catalog.monotonicity.Monotonicity;
import com.kcsl.loop.codemap.log.Log;
import com.kcsl.loop.preferences.LoopPreferences;

/**
 * Runs the control flow graph refinement step as a codemap stage
 * 
 * @author Ben Holland
 */
public class LoopCatalogCodemapStage extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the codemap stage
	 */
	public static final String IDENTIFIER = "com.kcsl.loop.codemap.loopcatalog";
	
	@Override
	public String getDisplayName() {
		return "Cataloging Loops";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{ 
					CallSiteOperatorTaintOverlayCodemapStage.IDENTIFIER, 
					DecompiledLoopIdentificationCodemapStage.IDENTIFIER,
					BoundaryConditionsCodemapStage.IDENTIFIER
				};
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		boolean runIndexer = LoopPreferences.isCatalogLoopsEnabled();
		if(runIndexer){
			Log.info("Cataloging loops..."); //$NON-NLS-1$
			Q loopHeaders = Query.universe().nodes(XCSG.Loop);
			long size = loopHeaders.eval().nodes().size();
			long processed = 0;
			Log.debug("Number of loops: " + size); //$NON-NLS-1$
			for(Node loopHeader : new AtlasHashSet<Node>(loopHeaders.eval().nodes())){
				Monotonicity.bounds(loopHeader);
				processed++;
				if (processed%10==0) {
					Log.debug("Loops processed: " + processed + "/" + size);  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}
		return runIndexer;
	}

}
