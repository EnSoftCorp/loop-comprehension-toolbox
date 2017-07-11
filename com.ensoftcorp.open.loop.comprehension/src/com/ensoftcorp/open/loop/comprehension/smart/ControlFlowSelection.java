package com.ensoftcorp.open.loop.comprehension.smart;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

public class ControlFlowSelection {
	
	public Q getSelectedMethods() {
		return selectedMethods;
	}

	public Q getSelectedDataFlow() {
		return selectedDataFlow;
	}

	public Q getSelectedControlFlow() {
		return selectedControlFlow;
	}

	/**
	 * Directly selected control flow nodes and the 
	 * control flow nodes which enclose any selected
	 * data flow nodes.
	 * 
	 * @return
	 */
	public Q getImpliedControlFlow() {
		return impliedControlFlow;
	}

	/**
	 * Directly selected methods and the 
	 * methods which enclose any selected
	 * data or control flow nodes.
	 * 
	 * @return
	 */
	public Q getImpliedMethods() {
		return impliedMethods;
	}

	private Q selectedMethods;
	private Q selectedDataFlow;
	private Q selectedControlFlow;
	private Q impliedControlFlow;
	private Q impliedMethods;

	private ControlFlowSelection(Q selectedMethods, Q selectedDataFlow,
			Q selectedControlFlow, Q enclosingControlFlow,
			Q enclosingMethods) {
				this.selectedMethods = selectedMethods;
				this.selectedDataFlow = selectedDataFlow;
				this.selectedControlFlow = selectedControlFlow;
				this.impliedControlFlow = enclosingControlFlow;
				this.impliedMethods = enclosingMethods;
	}

	public static ControlFlowSelection processSelection(IAtlasSelectionEvent atlasSelection) {
		Q selected = atlasSelection.getSelection();

		Q selectedMethods = selected.nodesTaggedWithAny(XCSG.Method);
		Q selectedDataFlow = selected.nodesTaggedWithAny(XCSG.DataFlow_Node);
		Q selectedControlFlow = selected.nodesTaggedWithAny(XCSG.ControlFlow_Node);		
		
		Q impliedControlFlow = selectedControlFlow.union(selectedDataFlow.parent());
		
		Q impliedMethods = selectedMethods.union(CommonQueries.getContainingFunctions(selectedControlFlow.union(selectedDataFlow)));
		
		return new ControlFlowSelection(selectedMethods, selectedDataFlow, selectedControlFlow, impliedControlFlow, impliedMethods);
	}
}
