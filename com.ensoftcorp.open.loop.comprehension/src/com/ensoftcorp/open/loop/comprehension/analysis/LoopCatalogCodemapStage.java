package com.ensoftcorp.open.loop.comprehension.analysis;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.jimple.commons.loops.BoundaryConditionsCodemapStage;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentificationCodemapStage;
import com.ensoftcorp.open.loop.comprehension.log.Log;
import com.ensoftcorp.open.loop.comprehension.taint.CallSiteOperatorTaintOverlayCodemapStage;

/**
 * Runs the loop catalog step as a codemap stage
 * 
 * @author Ben Holland
 */
public class LoopCatalogCodemapStage extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.loop.comprehension.analysis.loopcatalog";
	
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
	public void performIndexing(IProgressMonitor monitor) {
		Log.info("Cataloging loops...");
		for(Node loopHeader : Common.universe().nodesTaggedWithAny(CFGNode.LOOP_HEADER).eval().nodes()){
			Monotonicity.bounds(Common.toQ(loopHeader));
		}
	}

}
