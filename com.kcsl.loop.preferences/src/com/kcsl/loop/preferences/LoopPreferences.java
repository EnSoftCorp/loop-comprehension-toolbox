package com.kcsl.loop.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import com.kcsl.loop.preferences.log.Log;

public class LoopPreferences extends AbstractPreferenceInitializer {

	private static boolean initialized = false;
	
	/**
	 * Enable/disable debug logging
	 */
	public static final String DEBUG_LOGGING = "DEBUG_LOGGING";
	public static final Boolean DEBUG_LOGGING_DEFAULT = false;
	private static boolean debugLoggingValue = DEBUG_LOGGING_DEFAULT;
	
	/**
	 * Configures debug logging
	 */
	public static void enableDebugLogging(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(DEBUG_LOGGING, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if loop cataloging is enabled
	 * @return
	 */
	public static boolean isDebugLoggingEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return debugLoggingValue;
	}
	
	/**
	 * Enable/disable analysis of jimple
	 */
	public static final String ANALYZE_JIMPLE = "ANALYZE_JIMPLE";
	public static final Boolean ANALYZE_JIMPLE_DEFAULT = true;
	private static boolean analyzeJimpleValue = ANALYZE_JIMPLE_DEFAULT;
	
	/**
	 * Configures debug logging
	 */
	public static void enableAnalyzeJimple(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(ANALYZE_JIMPLE, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if jimple analysis is enabled
	 * @return
	 */
	public static boolean isAnalyzeJimpleEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return analyzeJimpleValue;
	}
	
	/**
	 * Enable/disable branch cataloging
	 */
	public static final String CATALOG_BRANCHES = "CATALOG_BRANCHES";
	public static final Boolean CATALOG_BRANCHES_DEFAULT = true;
	private static boolean catalogBranchesValue = CATALOG_BRANCHES_DEFAULT;
	
	/**
	 * Configures branch cataloging
	 */
	public static void enableCatalogBranches(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(CATALOG_BRANCHES, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if branch cataloging is enabled
	 * @return
	 */
	public static boolean isCatalogBranchesEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return catalogBranchesValue;
	}
	
	/**
	 * Enable/disable loop cataloging
	 */
	public static final String CATALOG_LOOPS = "CATALOG_LOOPS";
	public static final Boolean CATALOG_LOOPS_DEFAULT = true;
	private static boolean catalogLoopsValue = CATALOG_LOOPS_DEFAULT;
	
	/**
	 * Configures loop cataloging
	 */
	public static void enableCatalogLoops(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(CATALOG_LOOPS, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if loop cataloging is enabled
	 * @return
	 */
	public static boolean isCatalogLoopsEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return catalogLoopsValue;
	}
	
	/**
	 * Enable/disable loop cataloging - path statistics
	 */
	public static final String CATALOG_LOOPS_PATH_STATISTICS = "CATALOG_LOOPS_PATH_STATISTICS";
	public static final Boolean CATALOG_LOOPS_PATH_STATISTICS_DEFAULT = false;
	private static boolean catalogLoopsPathStatisticsValue = CATALOG_LOOPS_PATH_STATISTICS_DEFAULT;
	
	/**
	 * Configures loop cataloging - path statistics
	 */
	public static void enableCatalogLoopsPathStatistics(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(CATALOG_LOOPS_PATH_STATISTICS, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if loop cataloging is enabled
	 * @return
	 */
	public static boolean isCatalogLoopsPathStatisticsEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return catalogLoopsPathStatisticsValue;
	}
	
	/**
	 * Enable/disable terminating condition analysis
	 */
	public static final String IDENTIFY_TERMINATING_CONDITIONS = "IDENTIFY_TERMINATING_CONDITIONS";
	public static final Boolean IDENTIFY_TERMINATING_CONDITIONS_DEFAULT = false;
	private static boolean identifyTerminatingConditionsValue = IDENTIFY_TERMINATING_CONDITIONS_DEFAULT;
	
	/**
	 * Configures whether or not to run terminating condition analysis
	 */
	public static void enableIdentifyTerminatingConditions(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(IDENTIFY_TERMINATING_CONDITIONS, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if terminating condition analysis is enabled
	 * @return
	 */
	public static boolean isIdentifyTerminatingConditionsEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return identifyTerminatingConditionsValue;
	}
	
	/**
	 * Enable/disable callsite-taint overlay
	 */
	public static final String CALLSITE_TAINT_OVERLAY = "CALLSITE_TAINT_OVERLAY";
	public static final Boolean CALLSITE_TAINT_OVERLAY_DEFAULT = true;
	private static boolean callsiteTaintOverlayValue = CALLSITE_TAINT_OVERLAY_DEFAULT;
	
	/**
	 * Configures whether or not to run callsite-taint overlay
	 */
	public static void enableCallsiteTaintOverlay(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(CALLSITE_TAINT_OVERLAY, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if callsite-taint overlay is enabled
	 * @return
	 */
	public static boolean isCallsiteTaintOverlayEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return callsiteTaintOverlayValue;
	}
	
	/**
	 * Enable/disable application-specific configuration
	 */
	public static final String APPLICATION_SPECIFIC_CONFIGURATION = "APPLICATION_SPECIFIC_CONFIGURATION";
	public static final Boolean APPLICATION_SPECIFIC_CONFIGURATION_DEFAULT = false;
	private static boolean applicationSpecificConfiguration = APPLICATION_SPECIFIC_CONFIGURATION_DEFAULT;
	
	/**
	 * Configures whether or not to run application-specific configuration
	 */
	public static void enableApplicationSpecificConfiguration(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(APPLICATION_SPECIFIC_CONFIGURATION, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if application-specific configuration is enabled
	 * @return
	 */
	public static boolean isApplicationSpecificConfigurationEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return applicationSpecificConfiguration;
	}
	
//	public static final String REACHABILITY_ANALYSIS_MODE = "REACHABILITY_ANALYSIS_MODE";
//	public static final String ENTRY_POINTS = "ENTRY_POINTS";
	
	/*
	 * Enable/Disable Reachability from main methods
	 */
	public static final String MAIN_METHOD_ENTRY_POINT = "MAIN_METHOD_ENTRY_POINT";
	public static final Boolean MAIN_METHOD_ENTRY_POINT_DEFAULT = true;
	private static boolean mainMethodEntryPoint = MAIN_METHOD_ENTRY_POINT_DEFAULT;

	/*
	 * Enable/Disable Reachability from http handlers
	 */
	public static final String HTTP_HANDLER_ENTRY_POINT = "HTTP_HANDLER_ENTRY_POINT";
	public static final Boolean HTTP_HANDLER_ENTRY_POINT_DEFAULT = true;
	private static boolean httpHandlerEntryPoint = HTTP_HANDLER_ENTRY_POINT_DEFAULT;
	
	/*
	 * Enable/Disable Reachability from spring framework entry points
	 */
	public static final String SPRING_FRAMEWORK_ENTRY_POINT = "SPRING_FRAMEWORK_ENTRY_POINT";
	public static final boolean SPRING_FRAMEWORK_ENTRY_POINT_DEFAULT = true;
	private static boolean springFrameworkEntryPoint = SPRING_FRAMEWORK_ENTRY_POINT_DEFAULT;
	
	/*
	 * Enable/Disable Reachability from spark entry points
	 */
	public static final String SPARK_ENTRY_POINT = "SPARK_ENTRY_POINT";
	public static final boolean SPARK_ENTRY_POINT_DEFAULT = true;
	private static boolean sparkEntryPoint = SPARK_ENTRY_POINT_DEFAULT;
	
//	public static final String REACHABILITY_TYPE = "REACHABILITY_TYPE";

	/*
	 * Enable/Disable Reachability from main methods
	 */
	public static final String CONTROL_FLOW_REACHABILITY = "CONTROL_FLOW_REACHABILITY";
	public static final Boolean CONTROL_FLOW_REACHABILITY_DEFAULT = true;
	private static boolean controlFlowReachability = CONTROL_FLOW_REACHABILITY_DEFAULT;

	/*
	 * Enable/Disable Reachability from main methods
	 */
	public static final String DATA_FLOW_REACHABILITY = "DATA_FLOW_REACHABILITY";
	public static final Boolean DATA_FLOW_REACHABILITY_DEFAULT = true;
	private static boolean dataFlowReachability = DATA_FLOW_REACHABILITY_DEFAULT;
	
	/**
	 * Enable/disable callsite-taint overlay
	 */
	public static final String ENHANCED_LOOP_CALL_GRAPH_OVERLAY = "ENHANCED_LOOP_CALL_GRAPH_OVERLAY";
	public static final Boolean ENHANCED_LOOP_CALL_GRAPH_OVERLAY_DEFAULT = true;
	private static boolean enhancedLoopCallGraphOverlayValue = ENHANCED_LOOP_CALL_GRAPH_OVERLAY_DEFAULT;
	
	/**
	 * Configures whether or not to run Enhanced Loop Call Graph Overlay 
	 */
	public static void enableEnhancedLoopCallGraphOverlay(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(ENHANCED_LOOP_CALL_GRAPH_OVERLAY, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if Enhanced Loop Call Graph Overlay is enabled
	 * @return
	 */
	public static boolean isEnhancedLoopCallGraphOverlayEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return enhancedLoopCallGraphOverlayValue;
	}
	
	public void enableMainMethodEntryPoint(boolean enabled) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(MAIN_METHOD_ENTRY_POINT, enabled);
		loadPreferences();
	}
	
	public static boolean isMainMethodEntryPointEnabled() {
		if(!initialized) {
			loadPreferences();
		}
		return mainMethodEntryPoint;
	}
	
	public void enableHTTPHandlerEntryPoint(boolean enabled) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(HTTP_HANDLER_ENTRY_POINT, enabled);
		loadPreferences();
	}
	
	public static boolean isHTTPHandlerEntryPointEnabled() {
		if(!initialized) {
			loadPreferences();
		}
		return httpHandlerEntryPoint;
	}
	
	public void enableSpringFrameworkEntryPoint(boolean enabled) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(SPRING_FRAMEWORK_ENTRY_POINT, enabled);
		loadPreferences();
	}
	
	public static boolean isSpringFrameworkEntryPointEnabled() {
		if(!initialized) {
			loadPreferences();
		}
		return springFrameworkEntryPoint;
	}
	
	public void enableSparkEntryPoint(boolean enabled) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(SPARK_ENTRY_POINT, enabled);
		loadPreferences();
	}
	
	public static boolean isSparkEntryPointEnabled() {
		if(!initialized) {
			loadPreferences();
		}
		return sparkEntryPoint;
	}
	
	public void enableControlFlowReachability(boolean enabled) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(CONTROL_FLOW_REACHABILITY, enabled);
		loadPreferences();
	}
	
	public static boolean isControlFlowReachabilityEnabled() {
		if(!initialized) {
			loadPreferences();
		}
		return controlFlowReachability;
	}
	
	public void enableDataFlowReachability(boolean enabled) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(MAIN_METHOD_ENTRY_POINT, enabled);
		loadPreferences();
	}
	
	public static boolean isDataFlowReachabilityEnabled() {
		if(!initialized) {
			loadPreferences();
		}
		return dataFlowReachability;
	}
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setDefault(DEBUG_LOGGING, DEBUG_LOGGING_DEFAULT);
		preferences.setDefault(ANALYZE_JIMPLE, ANALYZE_JIMPLE_DEFAULT);
		preferences.setDefault(CATALOG_BRANCHES, CATALOG_BRANCHES_DEFAULT);
		preferences.setDefault(CATALOG_LOOPS, CATALOG_LOOPS_DEFAULT);
		preferences.setDefault(CATALOG_LOOPS_PATH_STATISTICS, CATALOG_LOOPS_PATH_STATISTICS_DEFAULT);
		preferences.setDefault(IDENTIFY_TERMINATING_CONDITIONS, IDENTIFY_TERMINATING_CONDITIONS_DEFAULT);
		preferences.setDefault(CALLSITE_TAINT_OVERLAY, CALLSITE_TAINT_OVERLAY_DEFAULT);
		preferences.setDefault(ENHANCED_LOOP_CALL_GRAPH_OVERLAY, ENHANCED_LOOP_CALL_GRAPH_OVERLAY_DEFAULT);
		preferences.setDefault(APPLICATION_SPECIFIC_CONFIGURATION, APPLICATION_SPECIFIC_CONFIGURATION_DEFAULT);
		preferences.setDefault(MAIN_METHOD_ENTRY_POINT, MAIN_METHOD_ENTRY_POINT_DEFAULT);
		preferences.setDefault(HTTP_HANDLER_ENTRY_POINT, HTTP_HANDLER_ENTRY_POINT_DEFAULT);
		preferences.setDefault(SPRING_FRAMEWORK_ENTRY_POINT, SPRING_FRAMEWORK_ENTRY_POINT_DEFAULT);
		preferences.setDefault(SPARK_ENTRY_POINT, SPARK_ENTRY_POINT);
		preferences.setDefault(CONTROL_FLOW_REACHABILITY, CONTROL_FLOW_REACHABILITY_DEFAULT);
		preferences.setDefault(DATA_FLOW_REACHABILITY, DATA_FLOW_REACHABILITY_DEFAULT);
	}

	/**
	 * Restores the default preferences
	 */
	public static void restoreDefaults() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(DEBUG_LOGGING, DEBUG_LOGGING_DEFAULT);
		preferences.setValue(ANALYZE_JIMPLE, ANALYZE_JIMPLE_DEFAULT);
		preferences.setValue(CATALOG_BRANCHES, CATALOG_BRANCHES_DEFAULT);
		preferences.setValue(CATALOG_LOOPS, CATALOG_LOOPS_DEFAULT);
		preferences.setValue(CATALOG_LOOPS_PATH_STATISTICS, CATALOG_LOOPS_PATH_STATISTICS_DEFAULT);
		preferences.setValue(IDENTIFY_TERMINATING_CONDITIONS, IDENTIFY_TERMINATING_CONDITIONS_DEFAULT);
		preferences.setValue(CALLSITE_TAINT_OVERLAY, CALLSITE_TAINT_OVERLAY_DEFAULT);
		preferences.setValue(ENHANCED_LOOP_CALL_GRAPH_OVERLAY, ENHANCED_LOOP_CALL_GRAPH_OVERLAY_DEFAULT);
		preferences.setValue(APPLICATION_SPECIFIC_CONFIGURATION, APPLICATION_SPECIFIC_CONFIGURATION_DEFAULT);
		preferences.setValue(MAIN_METHOD_ENTRY_POINT, MAIN_METHOD_ENTRY_POINT_DEFAULT);
		preferences.setValue(HTTP_HANDLER_ENTRY_POINT, HTTP_HANDLER_ENTRY_POINT_DEFAULT);
		preferences.setValue(SPRING_FRAMEWORK_ENTRY_POINT, SPRING_FRAMEWORK_ENTRY_POINT_DEFAULT);
		preferences.setValue(SPARK_ENTRY_POINT, SPARK_ENTRY_POINT_DEFAULT);
		preferences.setValue(CONTROL_FLOW_REACHABILITY, CONTROL_FLOW_REACHABILITY_DEFAULT);
		preferences.setValue(DATA_FLOW_REACHABILITY, DATA_FLOW_REACHABILITY_DEFAULT);
		loadPreferences();
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
			debugLoggingValue = preferences.getBoolean(DEBUG_LOGGING);
			analyzeJimpleValue = preferences.getBoolean(ANALYZE_JIMPLE);
			catalogBranchesValue = preferences.getBoolean(CATALOG_BRANCHES);
			catalogLoopsValue = preferences.getBoolean(CATALOG_LOOPS);
			catalogLoopsPathStatisticsValue = preferences.getBoolean(CATALOG_LOOPS_PATH_STATISTICS);
			identifyTerminatingConditionsValue = preferences.getBoolean(IDENTIFY_TERMINATING_CONDITIONS);
			callsiteTaintOverlayValue = preferences.getBoolean(CALLSITE_TAINT_OVERLAY);
			enhancedLoopCallGraphOverlayValue = preferences.getBoolean(ENHANCED_LOOP_CALL_GRAPH_OVERLAY);
			applicationSpecificConfiguration = preferences.getBoolean(APPLICATION_SPECIFIC_CONFIGURATION);
			mainMethodEntryPoint = preferences.getBoolean(MAIN_METHOD_ENTRY_POINT);
			httpHandlerEntryPoint = preferences.getBoolean(HTTP_HANDLER_ENTRY_POINT);
			springFrameworkEntryPoint = preferences.getBoolean(SPRING_FRAMEWORK_ENTRY_POINT);
			sparkEntryPoint = preferences.getBoolean(SPARK_ENTRY_POINT);
			controlFlowReachability = preferences.getBoolean(CONTROL_FLOW_REACHABILITY);
			dataFlowReachability = preferences.getBoolean(DATA_FLOW_REACHABILITY);
		} catch (Exception e){
			Log.warning("Error accessing RULER preferences, using defaults...", e);
		}
		initialized = true;
	}

}
