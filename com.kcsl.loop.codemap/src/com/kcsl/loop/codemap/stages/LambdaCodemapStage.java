package com.kcsl.loop.codemap.stages;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.kcsl.loop.catalog.lambda.LambdaIdentifier;
import com.kcsl.loop.codemap.log.Log;

public class LambdaCodemapStage extends PrioritizedCodemapStage {

	public static final String IDENTIFIER = "com.kcsl.loop.codemap.lambda";
	
	@Override
	public String getDisplayName() {
		return "Lambda Expressions";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[] {
			LoopCatalogCodemapStage.IDENTIFIER
		};
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		LambdaIdentifier li = new LambdaIdentifier();
		Log.info("Tagging Lambda Expressions");
		li.processLambdaExpressions();
		Log.info("Tagging Lambda Loops");
		li.processLambdaLoops();
		return true;
	}
	

}
