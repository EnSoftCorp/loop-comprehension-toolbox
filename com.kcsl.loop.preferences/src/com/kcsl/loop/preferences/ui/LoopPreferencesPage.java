package com.kcsl.loop.preferences.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.kcsl.loop.preferences.Activator;
import com.kcsl.loop.preferences.LoopPreferences;

/**
 * UI RULER analysis preferences
 * 
 * @author Ben Holland
 */
public class LoopPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private static final String DEBUG_LOGGING_DESCRIPTION = "Debug logging";
	
	private static final String ANALYZE_JIMPLE_DESCRIPTION = "Analyze Jimple";
	
	private static final String APPLICATION_SPECIFIC_CONFIGURATION_DESCRIPTION = "Apply application-specific configurations";
	private static final String CALLSITE_TAINT_OVERLAY_DESCRIPTION = "Apply callsite-taint overlay";
	private static final String ENHANCED_LOOP_CALL_GRAPH_DESCRIPTION = "Apply Enhanced Loop Call Graph overlay";
	private static final String IDENTIFY_TERMINATING_CONDITIONS_DESCRIPTION = "Identify terminating conditions";
	private static final String CATALOG_BRANCHES_DESCRIPTION = "Catalog branches";
	private static final String CATALOG_LOOPS_DESCRIPTION = "Catalog loops";
	private static final String CATALOG_LOOPS_PATH_STATISTICS_DESCRIPTION = "Catalog loops: path statistics";
	
	private static final String MAIN_METHOD_ENTRY_POINT_DESCRIPTION = "Include main method(s) as entry point(s)";
	private static final String HTTP_HANDLER_ENTRY_POINT_DESCRIPTION = "Include HTTP handler(s) as entry point(s)";
	private static final String SPRING_FRAMEWORK_ENTRY_POINT_DESCRIPTION = "Include spring framework read(s) as entry point(s)";
	private static final String SPARK_ENTRY_POINT_DESCRIPTION = "Include spark route(s) as entry point(s)";
	
	private static final String CONTROL_FLOW_REACHABILITY_DESCRIPTION = "Tag loops reachable via control flow from entry points";
	private static final String DATA_FLOW_REACHABILITY_DESCRIPTION = "Tag loops reachable via data flow from entry points";
	
	private static boolean changeListenerAdded = false;

	public LoopPreferencesPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(preferences);
		setDescription("Configure RULER preferences.");
		// use to update cached values if user edits a preference
		if(!changeListenerAdded){
			getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
					// reload the preference variable cache
					LoopPreferences.loadPreferences();
				}
			});
			changeListenerAdded = true;
		}
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(LoopPreferences.DEBUG_LOGGING, "&" + DEBUG_LOGGING_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.ANALYZE_JIMPLE, "&" + ANALYZE_JIMPLE_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.APPLICATION_SPECIFIC_CONFIGURATION, "&" + APPLICATION_SPECIFIC_CONFIGURATION_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.CALLSITE_TAINT_OVERLAY, "&" + CALLSITE_TAINT_OVERLAY_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.ENHANCED_LOOP_CALL_GRAPH_OVERLAY, "&" + ENHANCED_LOOP_CALL_GRAPH_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.IDENTIFY_TERMINATING_CONDITIONS, "&" + IDENTIFY_TERMINATING_CONDITIONS_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.CATALOG_BRANCHES, "&" + CATALOG_BRANCHES_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.CATALOG_LOOPS, "&" + CATALOG_LOOPS_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.CATALOG_LOOPS_PATH_STATISTICS, "&" + CATALOG_LOOPS_PATH_STATISTICS_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.MAIN_METHOD_ENTRY_POINT, "&" + MAIN_METHOD_ENTRY_POINT_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.HTTP_HANDLER_ENTRY_POINT, "&" + HTTP_HANDLER_ENTRY_POINT_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.SPRING_FRAMEWORK_ENTRY_POINT, "&" + SPRING_FRAMEWORK_ENTRY_POINT_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.SPARK_ENTRY_POINT, "&" + SPARK_ENTRY_POINT_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.CONTROL_FLOW_REACHABILITY, "&" + CONTROL_FLOW_REACHABILITY_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(LoopPreferences.DATA_FLOW_REACHABILITY, "&" + DATA_FLOW_REACHABILITY_DESCRIPTION, getFieldEditorParent()));
	}

}

