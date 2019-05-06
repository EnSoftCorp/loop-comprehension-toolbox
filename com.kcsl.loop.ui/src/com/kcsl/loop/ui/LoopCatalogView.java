package com.kcsl.loop.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.editors.EditorUtils;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.subsystems.Subsystems;
import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;
import com.ensoftcorp.open.commons.utilities.selection.GraphSelectionProviderView;
import com.ensoftcorp.open.java.commons.subsystems.CompressionSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.CryptoSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.DataStructureSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.DatabaseSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.GarbageCollectionSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.HardwareSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.IOSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.IntrospectionSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.JavaCoreSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.LogSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.MathSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.NetworkSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.RMISubsystem;
import com.ensoftcorp.open.java.commons.subsystems.RandomSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.SecuritySubsystem;
import com.ensoftcorp.open.java.commons.subsystems.SerializationSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.TestingSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.ThreadingSubsystem;
import com.ensoftcorp.open.java.commons.subsystems.UISubsystem;
import com.ensoftcorp.open.jimple.commons.loops.DecompiledLoopIdentification.CFGNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcsl.loop.catalog.lambda.LambdaIdentifier;
import com.kcsl.loop.catalog.monotonicity.MonotonicityPatternConstants;
import com.kcsl.loop.catalog.statistics.LoopCallSiteStats;
import com.kcsl.loop.catalog.statistics.LoopCallsiteCounts;
import com.kcsl.loop.catalog.statistics.LoopInfo;
import com.kcsl.loop.catalog.statistics.LoopPathCountStats;
import com.kcsl.loop.catalog.statistics.LoopPathCounts;
import com.kcsl.loop.catalog.statistics.LoopRuntimeInfo;
import com.kcsl.loop.catalog.subsystems.AppSubsystem;
import com.kcsl.loop.catalog.subsystems.CollectionSubsystem;
import com.kcsl.loop.catalog.subsystems.FunctionalSubsystem;
import com.kcsl.loop.catalog.subsystems.ImplicitLoopSubsystem;
import com.kcsl.loop.catalog.subsystems.IteratorSubsystem;
import com.kcsl.loop.catalog.subsystems.OtherLibrariesSubsystem;
import com.kcsl.loop.catalog.subsystems.SpliteratorSubsystem;
import com.kcsl.loop.catalog.subsystems.StreamSubsystem;
import com.kcsl.loop.codemap.stages.BranchCatalogCodemapStage;
import com.kcsl.loop.codemap.stages.ReachabilityAnalysisCodemapStage;
import com.kcsl.loop.codemap.usertags.ArtifactTagKindForLoops;
import com.kcsl.loop.codemap.usertags.UserTagKind;
import com.kcsl.loop.lcg.EnhancedLoopCallGraph;
import com.kcsl.loop.ui.log.Log;
import com.kcsl.loop.ui.smart.SubsystemInteractionsView;
import com.kcsl.loop.util.Attributes;
import com.kcsl.loop.util.LoopUtils;
import com.kcsl.loop.util.Tags;
import com.kcsl.loop.util.TerminatingConditions;
import com.kcsl.sidis.dis.Import;

public class LoopCatalogView extends GraphSelectionProviderView {

	private static final String LOOP_SUMMARY = "LOOP_SUMMARY";
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.kcsl.loop.ui.loopCatalogView";
	
	/**
	 * The constructor.
	 */
	public LoopCatalogView() {
		// intentionally left blank
	}
	
	private Table tableOfLoops;
	private Map<String, LoopInfo> loopInfoModel = new HashMap<String, LoopInfo>();
	private List<LoopInfo> loopInfoView = new ArrayList<LoopInfo>();
	Label lblCatalog;
	Label lblShowing;
	Combo comboArtifactKindToBeTagged;
	private Label lblSelected;

	@Override
	public void createPartControl(Composite parent) {
		
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 0, 0));

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		
		Composite compositeForTable = new Composite(composite, SWT.NONE);
		compositeForTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeForTable.setLayout(new GridLayout(1, false));
		
		tableOfLoops = new Table(compositeForTable, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableOfLoops.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableOfLoops.setHeaderVisible(true);
		tableOfLoops.setLinesVisible(true);
		
		TableColumn tblclmnLoopID = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnLoopID.setToolTipText("Unique ID assigned to every loop during codemap");
		tblclmnLoopID.setWidth(100);
		tblclmnLoopID.setText("Loop ID");
		tblclmnLoopID.setData("loopInfoField", "headerID");
		
		TableColumn tblclmnProject = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnProject.setToolTipText("Project in which the loop is located");
		tblclmnProject.setWidth(100);
		tblclmnProject.setText("Project");
		tblclmnProject.setData("loopInfoField","projectString");
		
		TableColumn tblclmnPackage = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnPackage.setToolTipText("Package in which the loop is located");
		tblclmnPackage.setWidth(100);
		tblclmnPackage.setText("Package");
		tblclmnPackage.setData("loopInfoField","packageName");
		
		TableColumn tblclmnType = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnType.setToolTipText("Type in which the loop is located");
		tblclmnType.setWidth(100);
		tblclmnType.setText("Type");
		tblclmnType.setData("loopInfoField","typeName");
		
		TableColumn tblclmnMethod = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnMethod.setToolTipText("Method in which the loop is located");
		tblclmnMethod.setWidth(100);
		tblclmnMethod.setText("Method");
		tblclmnMethod.setData("loopInfoField","methodName");
		
		TableColumn tblclmnContainer = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnContainer.setToolTipText("Immediate control flow container (parent) in which the loop is located");
		tblclmnContainer.setWidth(100);
		tblclmnContainer.setText("Container");
		tblclmnContainer.setData("loopInfoField","container");
		
		TableColumn tblclmnLoopStatement = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnLoopStatement.setToolTipText("Loop statement");
		tblclmnLoopStatement.setWidth(100);
		tblclmnLoopStatement.setText("Loop Statement");
		tblclmnLoopStatement.setData("loopInfoField","loopStatement");

		TableColumn tblclmnTerminatingconditions = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnTerminatingconditions.setToolTipText("Number of terminating (boundary) conditions in the loop");
		tblclmnTerminatingconditions.setWidth(100);
		tblclmnTerminatingconditions.setText("TerminatingConditions");
		tblclmnTerminatingconditions.setData("loopInfoField","numTerminatingConditions");

		TableColumn tblclmnNestingDepth = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnNestingDepth.setToolTipText("Depth to which this loop is nested");
		tblclmnNestingDepth.setWidth(100);
		tblclmnNestingDepth.setText("Nesting Depth");
		tblclmnNestingDepth.setData("loopInfoField","nestingDepth");

		TableColumn tblclmnMonotonic = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnMonotonic.setToolTipText("Whether the loop is monotonic or not");
		tblclmnMonotonic.setWidth(100);
		tblclmnMonotonic.setText("Monotonic");
		tblclmnMonotonic.setData("loopInfoField","monotonicity");
		
		TableColumn tblclmnRuntimeiterationcount = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnRuntimeiterationcount.setToolTipText("Number of iterations of the loop in a certain run (dynamic analysis info)");
		tblclmnRuntimeiterationcount.setWidth(100);
		tblclmnRuntimeiterationcount.setText("RuntimeIterationCount");
		tblclmnRuntimeiterationcount.setData("loopInfoField","runtimeIterationCount");
		
		TableColumn tblclmnAvgExecTime = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnAvgExecTime.setToolTipText("Average execution time from loop start to termination during a certain run (dynamic analysis info)");
		tblclmnAvgExecTime.setWidth(100);
		tblclmnAvgExecTime.setText("Avg Execution Time");
		tblclmnAvgExecTime.setData("loopInfoField","avgTimeForLoopExecution");
		
		TableColumn tblclmnAvgIterationTime = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnAvgIterationTime.setToolTipText("Average execution time per iteration during a certain run (dynamic analysis info)");
		tblclmnAvgIterationTime.setWidth(100);
		tblclmnAvgIterationTime.setText("Avg Iteration Time");
		tblclmnAvgIterationTime.setData("loopInfoField","avgTimePerIteration");

		TableColumn tblclmnPattern = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnPattern.setToolTipText("Loop termination pattern (not always found)");
		tblclmnPattern.setWidth(100);
		tblclmnPattern.setText("Pattern");
		tblclmnPattern.setData("loopInfoField","pattern");
		
		TableColumn tblclmnLowerbound = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnLowerbound.setToolTipText("Lower bound of the loop (there may be multiple, in which case one is chosen non-deterministically)");
		tblclmnLowerbound.setWidth(100);
		tblclmnLowerbound.setText("LowerBound");
		tblclmnLowerbound.setData("loopInfoField","lowerBound");
		
		TableColumn tblclmnUpperbound = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnUpperbound.setToolTipText("Upper bound of the loop (there may be multiple, in which case one is chosen non-deterministically)");
		tblclmnUpperbound.setWidth(100);
		tblclmnUpperbound.setText("UpperBound");
		tblclmnUpperbound.setData("loopInfoField","upperBound");
		
		TableColumn tblclmnNumCfEntryPoints = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnNumCfEntryPoints.setToolTipText("Number of control flow entry points from which the loop is reachable");
		tblclmnNumCfEntryPoints.setWidth(100);
		tblclmnNumCfEntryPoints.setText("#CfEntryPointsLoopIsReachableFrom");
		tblclmnNumCfEntryPoints.setData("loopInfoField","numCfEntryPointsFromWhichLoopIsReachable");

		TableColumn tblclmnCfEntrypointsloopisreachablefrom = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnCfEntrypointsloopisreachablefrom.setToolTipText("List of control flow entry points from which the loop is reachable");
		tblclmnCfEntrypointsloopisreachablefrom.setWidth(100);
		tblclmnCfEntrypointsloopisreachablefrom.setText("CfEntryPointsLoopIsReachableFrom");
		tblclmnCfEntrypointsloopisreachablefrom.setData("loopInfoField","cfEntryPointsFromWhichLoopIsReachable");
		
		TableColumn tblclmndfentrypointreachablefrom = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmndfentrypointreachablefrom.setToolTipText("Number of data flow entry points from which the loop is reachable");
		tblclmndfentrypointreachablefrom.setWidth(100);
		tblclmndfentrypointreachablefrom.setText("#DfEntryPointReachableFrom");
		tblclmndfentrypointreachablefrom.setData("loopInfoField","numDfEntryPointsFromWhichLoopIsReachable");

		TableColumn tblclmnDfentrypointreachablefrom = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnDfentrypointreachablefrom.setToolTipText("List of data flow entry points from which the loop is reachable");
		tblclmnDfentrypointreachablefrom.setWidth(100);
		tblclmnDfentrypointreachablefrom.setText("DfEntryPointReachableFrom");
		tblclmnDfentrypointreachablefrom.setData("loopInfoField","dfEntryPointsFromWhichLoopIsReachable");

		TableColumn tblclmnNumBranchescontained = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnNumBranchescontained.setToolTipText("Number of branches contained in the loop");
		tblclmnNumBranchescontained.setWidth(100);
		tblclmnNumBranchescontained.setText("#BranchesContained");
		tblclmnNumBranchescontained.setData("loopInfoField","numBranchesContainedInLoop");

		TableColumn tblclmnBranchescontainedinloop = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnBranchescontainedinloop.setToolTipText("List of branches contained in the loop");
		tblclmnBranchescontainedinloop.setWidth(100);
		tblclmnBranchescontainedinloop.setText("BranchesContainedInLoop");
		tblclmnBranchescontainedinloop.setData("loopInfoField","branchesContainedInLoop");
		
		TableColumn tblclmnNumBranchesgoverning = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnNumBranchesgoverning.setToolTipText("Number of branches governing the loop");
		tblclmnNumBranchesgoverning.setWidth(100);
		tblclmnNumBranchesgoverning.setText("#BranchesGoverning");
		tblclmnNumBranchesgoverning.setData("loopInfoField","numBranchesGoverningLoop");
		
		TableColumn tblclmnBranchesgoverningloop = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnBranchesgoverningloop.setToolTipText("List of branches governing the loop");
		tblclmnBranchesgoverningloop.setWidth(100);
		tblclmnBranchesgoverningloop.setText("BranchesGoverningLoop");
		tblclmnBranchesgoverningloop.setData("loopInfoField","branchesGoverningLoop");
		
		TableColumn tblclmnNumLambdaLoopscontainedinloop = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnNumLambdaLoopscontainedinloop.setToolTipText("Number of lambda loops contained in the loop");
		tblclmnNumLambdaLoopscontainedinloop.setWidth(100);
		tblclmnNumLambdaLoopscontainedinloop.setText("#LambdaLoopsContained");
		tblclmnNumLambdaLoopscontainedinloop.setData("loopInfoField","numLambdaLoopsContainedInLoop");
		
		TableColumn tblclmnLambdaLoopscontainedinloop = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnLambdaLoopscontainedinloop.setToolTipText("List of lambda loops contained in the loop");
		tblclmnLambdaLoopscontainedinloop.setWidth(100);
		tblclmnLambdaLoopscontainedinloop.setText("LambdaLoopsContainedInLoop");
		tblclmnLambdaLoopscontainedinloop.setData("loopInfoField","lambdaLoopsContainedInLoop");

		TableColumn tblclmnCountSubsystems = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnCountSubsystems.setToolTipText("Number of subsystems with which the loop interacts");
		tblclmnCountSubsystems.setWidth(100);
		tblclmnCountSubsystems.setText("Count Subsystems");
		tblclmnCountSubsystems.setData("loopInfoField","countSS");
		
		TableColumn tblclmnJavacoreSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnJavacoreSS.setToolTipText("Callsites in the loop with targets in the Javacore subsystem");
		tblclmnJavacoreSS.setWidth(100);
		tblclmnJavacoreSS.setText("Javacore SS");
		tblclmnJavacoreSS.setData("loopInfoField","javacoreSS");
		
		TableColumn tblclmnNetworkSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnNetworkSS.setToolTipText("Callsites in the loop with targets in the Network subsystem");
		tblclmnNetworkSS.setWidth(100);
		tblclmnNetworkSS.setText("Network SS");
		tblclmnNetworkSS.setData("loopInfoField","networkSS");
		
		TableColumn tblclmnIOSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnIOSS.setToolTipText("Callsites in the loop with targets in the IO subsystem");
		tblclmnIOSS.setWidth(100);
		tblclmnIOSS.setText("IO SS");
		tblclmnIOSS.setData("loopInfoField","ioSS");
		
		TableColumn tblclmnLogSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnLogSS.setToolTipText("Callsites in the loop with targets in the Log subsystem");
		tblclmnLogSS.setWidth(100);
		tblclmnLogSS.setText("Log SS");
		tblclmnLogSS.setData("loopInfoField","logSS");
		
		TableColumn tblclmnMathSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnMathSS.setToolTipText("Callsites in the loop with targets in the Math subsystem");
		tblclmnMathSS.setWidth(100);
		tblclmnMathSS.setText("Math SS");
		tblclmnMathSS.setData("loopInfoField","mathSS");
		
		TableColumn tblclmnRandomSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnRandomSS.setToolTipText("Callsites in the loop with targets in the Random subsystem");
		tblclmnRandomSS.setWidth(100);
		tblclmnRandomSS.setText("Random SS");
		tblclmnRandomSS.setData("loopInfoField","randomSS");
		
		TableColumn tblclmnThreadingSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnThreadingSS.setToolTipText("Callsites in the loop with targets in the Threading subsystem");
		tblclmnThreadingSS.setWidth(100);
		tblclmnThreadingSS.setText("Threading SS");
		tblclmnThreadingSS.setData("loopInfoField","threadingSS");
		
		TableColumn tblclmnCollectionSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnCollectionSS.setToolTipText("Callsites in the loop with targets in the Collection subsystem");
		tblclmnCollectionSS.setWidth(100);
		tblclmnCollectionSS.setText("Collection SS");
		tblclmnCollectionSS.setData("loopInfoField","collectionSS");
		
		TableColumn tblclmnIteratorSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnIteratorSS.setToolTipText("Callsites in the loop with targets in the Iterator subsystem");
		tblclmnIteratorSS.setWidth(100);
		tblclmnIteratorSS.setText("IteratorSS");
		tblclmnIteratorSS.setData("loopInfoField","iteratorSS");
		
		TableColumn tblclmnImplicitLoopss = new TableColumn(tableOfLoops, SWT.None);
		tblclmnImplicitLoopss.setToolTipText("Callsites in the loop with targets in the Implicit Loop subsystem");
		tblclmnImplicitLoopss.setWidth(100);
		tblclmnImplicitLoopss.setText("ImplicitLoopSS");
		tblclmnImplicitLoopss.setData("loopInfoField","implicitLoopSS");
		
		TableColumn tblclmnFunctionalSS = new TableColumn(tableOfLoops, SWT.None);
		tblclmnFunctionalSS.setToolTipText("Callsites in the loop with targets in the Functional subsystem");
		tblclmnFunctionalSS.setWidth(100);
		tblclmnFunctionalSS.setText("FunctionalSS");
		tblclmnFunctionalSS.setData("loopInfoField","FunctionalSS");
		
		TableColumn tblclmnSpliteratorSS = new TableColumn(tableOfLoops, SWT.None);
		tblclmnSpliteratorSS.setToolTipText("Callsites in the loop with targets in the Spliterator subsystem");
		tblclmnSpliteratorSS.setWidth(100);
		tblclmnSpliteratorSS.setText("SpliteratorSS");
		tblclmnSpliteratorSS.setData("loopInfoField","SpliteratorSS");
		
		TableColumn tblclmnStreamSS = new TableColumn(tableOfLoops, SWT.None);
		tblclmnStreamSS.setToolTipText("Callsites in the loop with targets in the Stream subsystem");
		tblclmnStreamSS.setWidth(100);
		tblclmnStreamSS.setText("StreamSS");
		tblclmnStreamSS.setData("loopInfoField","StreamSS");
		
		TableColumn tblclmnCompressionSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnCompressionSS.setToolTipText("Callsites in the loop with targets in the Compression subsystem");
		tblclmnCompressionSS.setWidth(100);
		tblclmnCompressionSS.setText("Compression SS");
		tblclmnCompressionSS.setData("loopInfoField","compressionSS");
		
		TableColumn tblclmnHardwareSs = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnHardwareSs.setToolTipText("Callsites in the loop with targets in the Hardware subsystem");
		tblclmnHardwareSs.setWidth(100);
		tblclmnHardwareSs.setText("Hardware SS");
		tblclmnHardwareSs.setData("loopInfoField","hardwareSS");
		
		TableColumn tblclmnRmiSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnRmiSS.setToolTipText("Callsites in the loop with targets in the RMI subsystem");
		tblclmnRmiSS.setWidth(100);
		tblclmnRmiSS.setText("RMI SS");
		tblclmnRmiSS.setData("loopInfoField","rmiSS");
		
		TableColumn tblclmnDatabaseSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnDatabaseSS.setToolTipText("Callsites in the loop with targets in the Database subsystem");
		tblclmnDatabaseSS.setWidth(100);
		tblclmnDatabaseSS.setText("Database SS");
		tblclmnDatabaseSS.setData("loopInfoField","databaseSS");

		TableColumn tblclmnSerializationSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnSerializationSS.setToolTipText("Callsites in the loop with targets in the Serialization subsystem");
		tblclmnSerializationSS.setWidth(100);
		tblclmnSerializationSS.setText("Serialization SS");
		tblclmnSerializationSS.setData("loopInfoField","serializationSS");
		
		TableColumn tblclmnUiSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnUiSS.setToolTipText("Callsites in the loop with targets in the UI subsystem");
		tblclmnUiSS.setWidth(100);
		tblclmnUiSS.setText("UI SS");
		tblclmnUiSS.setData("loopInfoField","uiSS");
		
		TableColumn tblclmnIntrospectionSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnIntrospectionSS.setToolTipText("Callsites in the loop with targets in the Introspection subsystem");
		tblclmnIntrospectionSS.setWidth(100);
		tblclmnIntrospectionSS.setText("Introspection SS");
		tblclmnIntrospectionSS.setData("loopInfoField","introspectionSS");
		
		TableColumn tblclmnTestingSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnTestingSS.setToolTipText("Callsites in the loop with targets in the Testing subsystem");
		tblclmnTestingSS.setWidth(100);
		tblclmnTestingSS.setText("Testing SS");
		tblclmnTestingSS.setData("loopInfoField","testingSS");
		
		TableColumn tblclmnGCSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnGCSS.setToolTipText("Callsites in the loop with targets in the Garbage Collection subsystem");
		tblclmnGCSS.setWidth(100);
		tblclmnGCSS.setText("GarbageCollection SS");
		tblclmnGCSS.setData("loopInfoField","gcSS");
		
		TableColumn tblclmnSecuritySS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnSecuritySS.setToolTipText("Callsites in the loop with targets in the Security subsystem");
		tblclmnSecuritySS.setWidth(100);
		tblclmnSecuritySS.setText("Security SS");
		tblclmnSecuritySS.setData("loopInfoField","securitySS");
		
		TableColumn tblclmnCryptoSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnCryptoSS.setToolTipText("Callsites in the loop with targets in the Crypto subsystem");
		tblclmnCryptoSS.setWidth(100);
		tblclmnCryptoSS.setText("Crypto SS");
		tblclmnCryptoSS.setData("loopInfoField","cryptoSS");
		
		TableColumn tblclmnDatastructureSS = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnDatastructureSS.setToolTipText("Callsites in the loop with targets in the Datastructure subsystem");
		tblclmnDatastructureSS.setWidth(100);
		tblclmnDatastructureSS.setText("DataStructure SS");
		tblclmnDatastructureSS.setData("loopInfoField","datastructureSS");
		
		TableColumn tblclmnOtherlibss = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnOtherlibss.setToolTipText("Callsites in the loop with targets in the Other libraries subsystem");
		tblclmnOtherlibss.setWidth(100);
		tblclmnOtherlibss.setText("OtherLibSS");
		tblclmnOtherlibss.setData("loopInfoField","otherLibSS");

		TableColumn tblclmnAppss = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnAppss.setToolTipText("Callsites in the loop with targets in the App subsystem");
		tblclmnAppss.setWidth(100);
		tblclmnAppss.setText("AppSS");
		tblclmnAppss.setData("loopInfoField","appSS");
		
		TableColumn tblclmnCallsites = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnCallsites.setToolTipText("Number of callsites in the loop");
		tblclmnCallsites.setWidth(100);
		tblclmnCallsites.setText("Callsites");
		tblclmnCallsites.setData("loopInfoField","callsitesInTheLoop");
		
		TableColumn tblclmnJdkcallsites = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnJdkcallsites.setToolTipText("Number of JDK callsites in the loop");
		tblclmnJdkcallsites.setWidth(100);
		tblclmnJdkcallsites.setText("JDKCallsites");
		tblclmnJdkcallsites.setData("loopInfoField","callsitesInTheLoopJDK");
		
		TableColumn tblclmnConditionalcallsites = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnConditionalcallsites.setToolTipText("Number of callsites in the loop that are governed by a condition");
		tblclmnConditionalcallsites.setWidth(100);
		tblclmnConditionalcallsites.setText("ConditionalCallsites");
		tblclmnConditionalcallsites.setData("loopInfoField","conditionalCallsitesInTheLoop");
		
		TableColumn tblclmnConditionaljdkcallsites = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnConditionaljdkcallsites.setToolTipText("Number of JDK callsites in the loop that are governed by a condition");
		tblclmnConditionaljdkcallsites.setWidth(100);
		tblclmnConditionaljdkcallsites.setText("ConditionalJDKCallsites");
		tblclmnConditionaljdkcallsites.setData("loopInfoField","conditionalCallsitesInTheLoopJDK");
		
		TableColumn tblclmnPathCount = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnPathCount.setToolTipText("Number of paths in the loop");
		tblclmnPathCount.setWidth(100);
		tblclmnPathCount.setText("Path Count");
		tblclmnPathCount.setData("loopInfoField","pathCount");
		
		TableColumn tblclmnPathswithcallsites = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnPathswithcallsites.setToolTipText("Number of paths in the loop having callsites");
		tblclmnPathswithcallsites.setWidth(100);
		tblclmnPathswithcallsites.setText("PathsWithCallsites");
		tblclmnPathswithcallsites.setData("loopInfoField","pathsWithCallSites");
		
		TableColumn tblclmnPathswithJDKcallsites = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnPathswithJDKcallsites.setToolTipText("Number of paths in the loop having JDK callsites");
		tblclmnPathswithJDKcallsites.setWidth(100);
		tblclmnPathswithJDKcallsites.setText("PathsWithJDKCallsites");
		tblclmnPathswithJDKcallsites.setData("loopInfoField","pathsWithJDKCallSites");
		
		TableColumn tblclmnPathswithoutjdkcallsites = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnPathswithoutjdkcallsites.setToolTipText("Number of paths in the loop without JDK callsites");
		tblclmnPathswithoutjdkcallsites.setWidth(100);
		tblclmnPathswithoutjdkcallsites.setText("PathsWithoutJDKCallsites");
		tblclmnPathswithoutjdkcallsites.setData("loopInfoField","pathsWithoutJDKCallSites");
		
		TableColumn tblclmnPathswithjdkcallsitesonly = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnPathswithjdkcallsitesonly.setToolTipText("Number of paths in the loop having only JDK callsites");
		tblclmnPathswithjdkcallsitesonly.setWidth(100);
		tblclmnPathswithjdkcallsitesonly.setText("PathsWithJDKCallsitesOnly");
		tblclmnPathswithjdkcallsitesonly.setData("loopInfoField","pathsWithJDKCallSitesOnly");
		
		TableColumn tblclmnPathswithnonjdkcallsites = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnPathswithnonjdkcallsites.setToolTipText("Number of paths in the loop having non-JDK callsites");
		tblclmnPathswithnonjdkcallsites.setWidth(100);
		tblclmnPathswithnonjdkcallsites.setText("PathsWithNonJDKCallsites");
		tblclmnPathswithnonjdkcallsites.setData("loopInfoField","pathsWithNonJDKCallSites");
		
		TableColumn tblclmnPathswithoutcallsites = new TableColumn(tableOfLoops, SWT.NONE);
		tblclmnPathswithoutcallsites.setToolTipText("Number of paths in the loop without callsites");
		tblclmnPathswithoutcallsites.setWidth(100);
		tblclmnPathswithoutcallsites.setText("PathsWithoutCallsites");
		tblclmnPathswithoutcallsites.setData("loopInfoField","pathsWithoutCallSites");
		
		Composite compositeForBottom = new Composite(composite, SWT.NONE);
		GridLayout gl_compositeForBottom = new GridLayout(3, false);
		gl_compositeForBottom.verticalSpacing = 0;
		compositeForBottom.setLayout(gl_compositeForBottom);
		compositeForBottom.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
		Group grpLoadLoops = new Group(compositeForBottom, SWT.NONE);
		grpLoadLoops.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.BOLD));
		grpLoadLoops.setText("Load loops");
		grpLoadLoops.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		GridLayout gl_grpLoadLoops = new GridLayout(1, false);
		gl_grpLoadLoops.marginWidth = 0;
		gl_grpLoadLoops.horizontalSpacing = 1;
		gl_grpLoadLoops.verticalSpacing = 0;
		gl_grpLoadLoops.marginHeight = 0;
		grpLoadLoops.setLayout(gl_grpLoadLoops);
		
		Composite compositeLoadAll = new Composite(grpLoadLoops, SWT.NONE);
		compositeLoadAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_compositeLoadAll = new GridLayout(1, false);
		gl_compositeLoadAll.verticalSpacing = 0;
		gl_compositeLoadAll.marginHeight = 0;
		compositeLoadAll.setLayout(gl_compositeLoadAll);
		
		Button btnReloadAllLoops = new Button(compositeLoadAll, SWT.NONE);
		btnReloadAllLoops.setToolTipText("Load all loops in the app");
		btnReloadAllLoops.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnReloadAllLoops.setEnabled(false);
				final Thread thread = new Thread() {
					@Override
					public void run() {
						computeLoopInfoForLoops(Query.universe().nodes(XCSG.Loop));
						updateSelectionStatus();
					}
				};
				thread.start();
				btnReloadAllLoops.setEnabled(true);
			}
		});
		btnReloadAllLoops.setText("All");
		
		Composite compositeLoadTags = new Composite(grpLoadLoops, SWT.NONE);
		compositeLoadTags.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_compositeLoadTags = new GridLayout(3, false);
		gl_compositeLoadTags.verticalSpacing = 0;
		gl_compositeLoadTags.marginHeight = 0;
		compositeLoadTags.setLayout(gl_compositeLoadTags);
		
		Button btnLoadLoopsTagged = new Button(compositeLoadTags, SWT.NONE);
		btnLoadLoopsTagged.setToolTipText("Load loops tagged with the selected tag name");
		btnLoadLoopsTagged.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String tagText = comboLoadLoopsTagged.getText();
				btnLoadLoopsTagged.setEnabled(false);
//				parent.getShell().getDisplay().syncExec(new Runnable() {
				final Thread thread = new Thread() {
					@Override
					public void run() {
						
						String tag = (tagText != null)?tagText.trim():"";
						if(tag.length() > 0) {
							Q loopsToLoad = Query.universe().nodesTaggedWithAll(XCSG.Loop,tag);
							if(CommonQueries.isEmpty(loopsToLoad)) {
								DisplayUtils.showMessage("No loops were found with tag \""+tag+"\"");
							} else {
								computeLoopInfoForLoops(loopsToLoad);
							}
						} else {
							DisplayUtils.showMessage("No loops were found with tag \"\"");
						}
						updateSelectionStatus();
					}
				};
				thread.start();
				btnLoadLoopsTagged.setEnabled(true);
				comboLoadLoopsTagged.setText("");
				comboLoadLoopsTagged.redraw();
			}
		});
		btnLoadLoopsTagged.setText("Tagged");
		
		comboLoadLoopsTagged = new Combo(compositeLoadTags, SWT.READ_ONLY);
		GridData gd_comboLoadLoopsTagged = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboLoadLoopsTagged.widthHint = 175;
		comboLoadLoopsTagged.setLayoutData(gd_comboLoadLoopsTagged);
		comboLoadLoopsTagged.setToolTipText("List of tags prefixed with \"LOOP-\"");
		//		comboLoadLoopsTagged.setItems(new String[]{""});
		comboLoadLoopsTagged.setItems(Tags.getTagsWithPrefix(UserTagKind.LOOP.getTagPrefix()).toArray(new String[0]));
		
		Button btnRefreshTagsButton = new Button(compositeLoadTags, SWT.NONE);
		btnRefreshTagsButton.setToolTipText("Refresh the list of tags with prefix \"LOOP-\"");
		btnRefreshTagsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comboLoadLoopsTagged.setItems(Tags.getTagsWithPrefix(UserTagKind.LOOP.getTagPrefix()).toArray(new String[0]));
			}
		});
		btnRefreshTagsButton.setText("Refresh");
		
		Group grpProcessSelectedLoops = new Group(compositeForBottom, SWT.NONE);
		grpProcessSelectedLoops.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.BOLD));
		grpProcessSelectedLoops.setText("Process selected loops");
		GridLayout gl_grpProcessSelectedLoops = new GridLayout(1, false);
		gl_grpProcessSelectedLoops.horizontalSpacing = 1;
		gl_grpProcessSelectedLoops.verticalSpacing = 0;
		gl_grpProcessSelectedLoops.marginWidth = 0;
		gl_grpProcessSelectedLoops.marginHeight = 0;
		grpProcessSelectedLoops.setLayout(gl_grpProcessSelectedLoops);
		grpProcessSelectedLoops.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Composite compositeCreateTags = new Composite(grpProcessSelectedLoops, SWT.NONE);
		compositeCreateTags.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_compositeCreateTags = new GridLayout(3, false);
		gl_compositeCreateTags.verticalSpacing = 0;
		gl_compositeCreateTags.marginHeight = 0;
		compositeCreateTags.setLayout(gl_compositeCreateTags);
		
		Button btnTagSelected = new Button(compositeCreateTags, SWT.NONE);
		btnTagSelected.setToolTipText("Tag the selected artifacts (or those related to the selected) as per the selection with the specified tag name");
		btnTagSelected.setText("Tag");
		btnTagSelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String artifactKindToBeTaaged = comboArtifactKindToBeTagged.getText();
				String tagName = textTagToBeApplied.getText().trim();

//				btnTagSelected.setEnabled(false);
				parent.getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						if(tagName.length() == 0) {
							DisplayUtils.showMessage("Empty tag name specified.");
							Log.warning("Empty tag name specified.");
							return;
						}
						
						TableItem[] selection = tableOfLoops.getSelection();
						
						Boolean proceed = false;
						String prefixedTagName = "";
						Q artifactsToTag = Common.empty();
							switch(artifactKindToBeTaaged) {
								case ArtifactTagKindForLoops.SELECTED_LOOPS:
									prefixedTagName = UserTagKind.LOOP.getTagPrefix() + tagName;
									proceed = checkExistingArtifactsWithTag(prefixedTagName);
									if(proceed) {
										for(TableItem item : selection) {
											Node loop = (Node)item.getData("loop");
											artifactsToTag = artifactsToTag.union(Common.toQ(loop));
										}
									} else {
										Log.warning("User canceled tagging loops");
									}
									break;
								case ArtifactTagKindForLoops.BRANCH_CONTAINED:
									prefixedTagName = UserTagKind.BRANCH.getTagPrefix() + tagName;
									proceed = checkExistingArtifactsWithTag(prefixedTagName);
									if(proceed) {
										for(TableItem item : selection) {
											Node loop = (Node)item.getData("loop");
											LoopInfo loopInfo = loopInfoModel.get((String)loop.getAttr(CFGNode.LOOP_HEADER_ID));
											if(loopInfo.getBranchesContainedInLoop() != null && !loopInfo.getBranchesContainedInLoop().isEmpty()) {
												Q branchesContained = Query.universe().selectNode(BranchCatalogCodemapStage.BRANCH_ID, loopInfo.getBranchesContainedInLoop().toArray());
												artifactsToTag = artifactsToTag.union(branchesContained);
											}
										}
									} else {
										Log.warning("User canceled tagging branches contained in loops");
									}
									break;
								case ArtifactTagKindForLoops.BRANCH_GOVERNING:
									prefixedTagName = UserTagKind.BRANCH.getTagPrefix() + tagName;
									proceed = checkExistingArtifactsWithTag(prefixedTagName);
									if(proceed) {
										for(TableItem item : selection) {
											Node loop = (Node)item.getData("loop");
											LoopInfo loopInfo = loopInfoModel.get((String)loop.getAttr(CFGNode.LOOP_HEADER_ID));
											if(loopInfo.getBranchesGoverningLoop() != null && !loopInfo.getBranchesGoverningLoop().isEmpty()) {
												Q branchesGoverning = Query.universe().selectNode(BranchCatalogCodemapStage.BRANCH_ID, loopInfo.getBranchesGoverningLoop().toArray());
												artifactsToTag = artifactsToTag.union(branchesGoverning);
											}
										}
									} else {
										Log.warning("User canceled tagging branches governing loops");
									}
									break;
								case ArtifactTagKindForLoops.METHOD_CONTAINING:
									prefixedTagName = UserTagKind.METHOD.getTagPrefix() + tagName;
									proceed = checkExistingArtifactsWithTag(prefixedTagName);
									if(proceed) {
										for(TableItem item : selection) {
											Node loop = (Node)item.getData("loop");
											Q methodContaining = Common.toQ(loop).containers().nodes(XCSG.Method);
											artifactsToTag = artifactsToTag.union(methodContaining);
										}
									} else {
										Log.warning("User canceled tagging methods containing loops");
									}
									break;
								case ArtifactTagKindForLoops.TYPE_CONTAINING:
									prefixedTagName = UserTagKind.TYPE.getTagPrefix() + tagName;
									proceed = checkExistingArtifactsWithTag(prefixedTagName);
									if(proceed) {
										for(TableItem item : selection) {
											Node loop = (Node)item.getData("loop");
											Q typeContaining = Common.toQ(loop).containers().nodes(XCSG.Type);
											artifactsToTag = artifactsToTag.union(typeContaining);
										}
									} else {
										Log.warning("User canceled tagging types containing loops");
									}
									break;
								case ArtifactTagKindForLoops.CALLSITE_CONTAINED:
									prefixedTagName = UserTagKind.CALLSITE.getTagPrefix() + tagName;
									proceed = checkExistingArtifactsWithTag(prefixedTagName);
									if(proceed) {
										for(TableItem item : selection) {
											Node loop = (Node)item.getData("loop");
											Q callsitesContained = LoopUtils.getCallsitesInsideLoop(Common.toQ(loop)).parent();
											artifactsToTag = artifactsToTag.union(callsitesContained);
										}
									} else {
										Log.warning("User canceled tagging branches contained in loops");
									}
									break;
								case ArtifactTagKindForLoops.LAMBDA_LOOP_CONTAINED:
									prefixedTagName = UserTagKind.LAMBDA_LOOP.getTagPrefix() + tagName;
									proceed = checkExistingArtifactsWithTag(prefixedTagName);
									if(proceed) {
										for(TableItem item : selection) {
											Node loop = (Node)item.getData("loop");
											LoopInfo loopInfo = loopInfoModel.get((String)loop.getAttr(CFGNode.LOOP_HEADER_ID));
											if(loopInfo.getLambdaLoopsContainedInLoop() != null && !loopInfo.getLambdaLoopsContainedInLoop().isEmpty()) {
												Q lambdaLoopsContained = Query.universe().selectNode(LambdaIdentifier.LAMBDA_LOOP_ID, loopInfo.getLambdaLoopsContainedInLoop().toArray());
												artifactsToTag = artifactsToTag.union(lambdaLoopsContained);
											}
										}
									} else {
										Log.warning("User canceled tagging lambda loops contained in loops");
									}
									break;
								default:
									Log.info("Tagging loops by default");
									prefixedTagName = UserTagKind.LOOP.getTagPrefix() + tagName;
									proceed = checkExistingArtifactsWithTag(prefixedTagName);
									if(proceed) {
										for(TableItem item : selection) {
											Node loop = (Node)item.getData("loop");
											artifactsToTag = artifactsToTag.union(Common.toQ(loop));
										}
									} else {
										Log.warning("User canceled tagging loops");
									}
									break;
							}
						Tags.setTagWithConfirmation(artifactsToTag, prefixedTagName);
						textTagToBeApplied.setText("");
						updateSelectionStatus();
					}
				});
//				btnTagSelected.setEnabled(true);
			}
		});
		String[] comboItems = new String[] {
					ArtifactTagKindForLoops.SELECTED_LOOPS,
					ArtifactTagKindForLoops.METHOD_CONTAINING,
					ArtifactTagKindForLoops.TYPE_CONTAINING,
					ArtifactTagKindForLoops.BRANCH_GOVERNING,
					ArtifactTagKindForLoops.BRANCH_CONTAINED,
					ArtifactTagKindForLoops.CALLSITE_CONTAINED,
					ArtifactTagKindForLoops.LAMBDA_LOOP_CONTAINED
				};
		
		comboArtifactKindToBeTagged = new Combo(compositeCreateTags, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboArtifactKindToBeTagged.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		comboArtifactKindToBeTagged.setToolTipText("Type of artifact to be tagged");
		comboArtifactKindToBeTagged.setItems(comboItems);
		comboArtifactKindToBeTagged.select(0);
		
		textTagToBeApplied = new Text(compositeCreateTags, SWT.BORDER);
		textTagToBeApplied.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textTagToBeApplied.setToolTipText("Tag name to be applied");

		Composite compositeLoopActions = new Composite(grpProcessSelectedLoops, SWT.NONE);
		compositeLoopActions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_compositeLoopActions = new GridLayout(4, false);
		gl_compositeLoopActions.marginHeight = 0;
		gl_compositeLoopActions.verticalSpacing = 0;
		compositeLoopActions.setLayout(gl_compositeLoopActions);
		
		Button btnShowSelectedLoops = new Button(compositeLoopActions, SWT.NONE);
		btnShowSelectedLoops.setToolTipText("Show the selected loop headers as graph elements in a graph editor");
		btnShowSelectedLoops.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnShowSelectedLoops.setEnabled(false);
				parent.getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						TableItem[] selection = tableOfLoops.getSelection();
						AtlasSet<Node> selectedLoops = new AtlasHashSet<Node>();
						for(TableItem item : selection) {
							Node loop = (Node)item.getData("loop");
							selectedLoops.add(loop);
						}
						DisplayUtils.show(Common.toQ(selectedLoops), true, "Loops Selected");
						updateSelectionStatus();
					}
				});
				btnShowSelectedLoops.setEnabled(true);
			}
		});
		btnShowSelectedLoops.setText("Show");
		
		Button btnRetainSelected = new Button(compositeLoopActions, SWT.NONE);
		btnRetainSelected.setToolTipText("Retain the selected loops in the table");
		btnRetainSelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnRetainSelected.setEnabled(false);
				parent.getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						TableItem[] selection = tableOfLoops.getSelection();
						if(selection.length <= 0) {
							DisplayUtils.showMessage("No loops were selected.");
						} else {
							Q selectedLoops = getDataAsQForSelectedTableItems(selection, "loop");
							computeLoopInfoForLoops(selectedLoops);
							updateSelectionStatus();
						}
					}
				});
				btnRetainSelected.setEnabled(true);
			}
		});
		btnRetainSelected.setText("Retain");
		
		Button btnInvert = new Button(compositeLoopActions, SWT.NONE);
		btnInvert.setToolTipText("Invert the selection in the table");
		btnInvert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				parent.getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						TableItem[] allItems = tableOfLoops.getItems();
						TableItem[] selection = tableOfLoops.getSelection();
						List<TableItem> selectionSet = Arrays.asList(selection);
						Set<TableItem> result = new HashSet<TableItem>(); 
						for(TableItem item: allItems) {
							if(!selectionSet.contains(item)) {
								result.add(item);
							}
						}
						tableOfLoops.setSelection(result.toArray(new TableItem[0]));
						updateSelectionStatus();
					}
				});
			}

			
		});
		btnInvert.setText("Invert");
		
		Button btnDelete = new Button(compositeLoopActions, SWT.NONE);
		btnDelete.setToolTipText("Delete selected loops from the table");
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				parent.getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						TableItem[] selection = tableOfLoops.getSelection();
						if(selection.length <= 0) {
							DisplayUtils.showMessage("No items were selected.");
						} else {
							Q selected = getDataAsQForSelectedTableItems(selection, "loop");
							Q all = getDataAsQForSelectedTableItems(tableOfLoops.getItems(), "loop");
							computeLoopInfoForLoops(all.difference(selected));
						}
						updateSelectionStatus();
					}
				});
			}
		});
		btnDelete.setText("Delete");
		
		Group grpStatus = new Group(compositeForBottom, SWT.NONE);
		grpStatus.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.BOLD));
		grpStatus.setText("Status");
		grpStatus.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		GridLayout gl_grpStatus = new GridLayout(1, false);
		gl_grpStatus.marginHeight = 0;
		grpStatus.setLayout(gl_grpStatus);
		
		lblCatalog = new Label(grpStatus, SWT.NONE);
		lblCatalog.setToolTipText("Number of loops in the catalog");
		GridData gd_lblCatalog = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_lblCatalog.widthHint = 99;
		lblCatalog.setLayoutData(gd_lblCatalog);
		lblCatalog.setText("Catalog: ");
		
		lblShowing = new Label(grpStatus, SWT.NONE);
		lblShowing.setToolTipText("Number of loops shown in the table");
		GridData gd_lblShowing = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_lblShowing.widthHint = 99;
		lblShowing.setLayoutData(gd_lblShowing);
		lblShowing.setText("Showing: ");
		
		lblSelected = new Label(grpStatus, SWT.NONE);
		lblSelected.setToolTipText("Number of loops selected in the table");
		GridData gd_lblSelected = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_lblSelected.widthHint = 99;
		lblSelected.setLayoutData(gd_lblSelected);
		lblSelected.setText("Selected:");
		
		
		Listener sortListener = new Listener() {
			
            public void handleEvent(Event e) {
                TableColumn sortColumn = tableOfLoops.getSortColumn();
                TableColumn selectedColumn = (TableColumn) e.widget;
                String loopInfoField = selectedColumn.getData("loopInfoField").toString();

                int dir = tableOfLoops.getSortDirection();
                if (sortColumn == selectedColumn) {
                    dir = (dir == SWT.UP) ? SWT.DOWN : SWT.UP;
                } else {
                	tableOfLoops.setSortColumn(selectedColumn);
                    dir = SWT.UP;
                }
                tableOfLoops.setSortDirection(dir);
                loopInfoView.sort((l1, l2) -> {
					String f1 = l1.getField(loopInfoField);
					String f2 = l2.getField(loopInfoField);
					if(StringUtils.isNumeric(f1) && StringUtils.isNumeric(f2)) {
						return NumberUtils.createDouble(f1).compareTo(NumberUtils.createDouble(f2));
					} else {
						return f1.compareTo(f2);
					}
				});
                if(dir == SWT.DOWN) {
                	Collections.reverse(loopInfoView);
                }
                
                updateLoopsTable();
            }
        };
        
        tableOfLoops.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
//            	Log.info("plain listener");
              TableItem[] selection = tableOfLoops.getSelection();
              if(selection == null || selection.length == 0) {
            	  Log.warning("Received a selection event on tableOfLoops, but no selection item to process.");
            	  return;
              }
              AtlasSet<Node> selectedLoops = new AtlasHashSet<Node>(); 
              for(TableItem item: selection) {
	              Node loop = (Node)item.getData("loop");
	              selectedLoops.add(loop);
              }
              updateSelectionStatus();
              setSelection(Common.toQ(selectedLoops));
            }
        });
        
        // double click shows loop header in text editor
		tableOfLoops.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				try {
					TableItem [] selection = tableOfLoops.getSelection();
					Node loop = (Node) selection[0].getData("loop");
					EditorUtils.getInstance().openFileinEditor((SourceCorrespondence) loop.getAttr(XCSG.sourceCorrespondence));
				} catch (Exception e1){
					Log.debug("Exception while trying to show loop header in text editor", e1);
					return;
				}
				updateSelectionStatus();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
			
		});
        
        for(TableColumn col: tableOfLoops.getColumns()) {
        	col.addListener(SWT.Selection, sortListener);
//        	col.pack();
        }
        
        final TableCursor cursor = new TableCursor(tableOfLoops, SWT.NONE);
		cursor.addSelectionListener(new SelectionAdapter() {
			// when the TableEditor is over a cell, select the corresponding row in 
			// the table
			public void widgetSelected(SelectionEvent e) {
				TableItem row = cursor.getRow();
				int columnIndex = cursor.getColumn();
				if(row.getData("loop") != null) {
					Node loop = (Node)row.getData("loop");
					TableColumn loopColumn = tableOfLoops.getColumn(columnIndex);
					String loopInfoField = (String)loopColumn.getData("loopInfoField");
					LoopInfo loopInfo = loopInfoModel.get((String)loop.getAttr(CFGNode.LOOP_HEADER_ID));
					String subsystemTag = loopInfo.getSubsystemTagForField(loopInfoField);
					if(subsystemTag != null) {
						SubsystemInteractionsView.subsystemTags = new String[]{subsystemTag};
					}
				}
				
			}
		});
		
		registerGraphHandlers();
	}
	
	public void updateSelectionStatus() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				lblSelected.setText("Selected: "+tableOfLoops.getSelection().length);
		      	lblSelected.redraw();
			}
			
		});
      	
	}
	
	@Override
	public void setFocus() {
		// intentionally left blank
	}

    private Combo comboLoadLoopsTagged;
    private Text textTagToBeApplied;
    
	private void updateLoopsTable() {
		tableOfLoops.removeAll();
		tableOfLoops.clearAll();
		
		for(LoopInfo loopInfo : loopInfoView) {
			long hid = loopInfo.getHeaderID();
			String id = String.valueOf(hid);
			Q lhQ = Query.universe().selectNode(CFGNode.LOOP_HEADER_ID, id);
			Node loopHeader = lhQ.eval().nodes().one();
			TableItem item = new TableItem(tableOfLoops, SWT.NONE);
			item.setData("loop", loopHeader);
		    item.setText(new String[] { 
		    		loopInfo.getHeaderID()+"",
		    		loopInfo.getProjectString(), 
		    		loopInfo.getPackageName(), 
		    		loopInfo.getTypeName(), 
		    		loopInfo.getMethodName(),
		    		loopInfo.getContainer(), 
		    		loopInfo.getLoopStatement(), 
		    		loopInfo.getNumTerminatingConditions()+"",
		    		loopInfo.getNestingDepth()+"", 
		    		loopInfo.isMonotonicity()+"", 
		    		loopInfo.getLoopRuntimeInfo().getIterationCount()+"",
		    		loopInfo.getLoopRuntimeInfo().getAvgTimeForLoopExecution()+"",
		    		loopInfo.getLoopRuntimeInfo().getAvgTimePerIteration()+"",
		    		loopInfo.getPattern(),
		    		loopInfo.getLowerBound(),
		    		loopInfo.getUpperBound(),
		    		loopInfo.getNumCfEntryPointsFromWhichLoopIsReachable()+"",
		    		Attributes.ListAttr.toString(loopInfo.getCfEntryPointsFromWhichLoopIsReachable()),
		    		loopInfo.getNumDfEntryPointsFromWhichLoopIsReachable()+"",
		    		Attributes.ListAttr.toString(loopInfo.getDfEntryPointsFromWhichLoopIsReachable()),
		    		loopInfo.getNumBranchesContainedInLoop()+"",
		    		Attributes.ListAttr.toString(loopInfo.getBranchesContainedInLoop()),
		    		loopInfo.getNumBranchesGoverningLoop()+"",
		    		Attributes.ListAttr.toString(loopInfo.getBranchesGoverningLoop()),
		    		loopInfo.getNumLambdaLoopsContainedInLoop()+"",
		    		Attributes.ListAttr.toString(loopInfo.getLambdaLoopsContainedInLoop()),
		    		loopInfo.getCountSS()+"",
		    		loopInfo.isJavacoreSS()+"",
		    		loopInfo.isNetworkSS()+"",
		    		loopInfo.isIoSS()+"",
		    		loopInfo.isLogSS()+"",
		    		loopInfo.isMathSS()+"",
		    		loopInfo.isRandomSS()+"",
		    		loopInfo.isThreadingSS()+"",
		    		loopInfo.isCollectionSS()+"",
		    		loopInfo.isIteratorSS()+"",
		    		loopInfo.isImplicitLoopSS()+"",
		    		loopInfo.isFunctionalSS()+"",
		    		loopInfo.isSpliteratorSS()+"",
		    		loopInfo.isStreamSS()+"",
		    		loopInfo.isCompressionSS()+"",
		    		loopInfo.isHardwareSS()+"",
		    		loopInfo.isRmiSS()+"",
		    		loopInfo.isDatabaseSS()+"",
		    		loopInfo.isSerializationSS()+"",
		    		loopInfo.isUiSS()+"",
		    		loopInfo.isIntrospectionSS()+"",
		    		loopInfo.isTestingSS()+"",
		    		loopInfo.isGcSS()+"",
		    		loopInfo.isSecuritySS()+"",
		    		loopInfo.isCryptoSS()+"",
		    		loopInfo.isDatastructureSS()+"",
		    		loopInfo.isOtherLibSS()+"",
		    		loopInfo.isAppSS()+"",
		    		loopInfo.getCallsitesInTheLoop()+"",
		    		loopInfo.getCallsitesInTheLoopJDK()+"",
		    		loopInfo.getConditionalCallsitesInTheLoop()+"",
		    		loopInfo.getConditionalCallsitesInTheLoopJDK()+"",
		    		loopInfo.getPathCount() + "", 
		    		loopInfo.getPathsWithCallSites()+"",
		    		loopInfo.getPathsWithJDKCallSites()+"",
		    		loopInfo.getPathsWithJDKCallSitesOnly()+"",
		    		loopInfo.getPathsWithNonJDKCallSites()+"",
		    		loopInfo.getPathsWithoutCallSites()+"",
		    		loopInfo.getPathsWithoutJDKCallSites()+"",
		    		});
		}
//		for(TableColumn col : tableOfLoops.getColumns()) {
//			col.pack();
//		}
		updateSelectionStatus();
	}
	
	public void computeLoopInfoForLoops(Q loopHeadersParam) {
				
		compileLoopCatalog();	
		
		for(Node loopHeader: Query.universe().nodes(XCSG.Loop).eval().nodes()) {
			LoopInfo loopInfo = loopInfoModel.get(loopHeader.getAttr(CFGNode.LOOP_HEADER_ID));
			LoopRuntimeInfo lri = getLoopRuntimeInfo(loopHeader);
			loopInfo.setLoopRuntimeInfo(lri);
		}
		
		//Populate loopInfoView with the subset of loops from loopInfoModel corresponding to the passed parameter
		loopInfoView = new ArrayList<LoopInfo>();
		int count = 0;
		for(Node loopToView: loopHeadersParam.eval().nodes()) {
			LoopInfo loopInfo = loopInfoModel.get(loopToView.getAttr(CFGNode.LOOP_HEADER_ID));
			loopInfoView.add(loopInfo);
			count++;
			final int loopCount = count;
			Display.getDefault().syncExec(new Runnable(){
	            public void run() {
	            	lblShowing.setText("Showing: "+loopCount);
                	lblShowing.redraw();
	            }
	        });
		}
			
		//Update the view 
		Display.getDefault().syncExec(new Runnable(){
            public void run() {
        		updateLoopsTable();
            }
        });
	}

	public void compileLoopCatalog() {
		// Gather loop catalog information (first time the view is opened)
		LoopCallSiteStats lcsStats = new LoopCallSiteStats();
		int numLoops = (int)Query.universe().nodes(XCSG.Loop).eval().nodes().size();
		Log.info("Gathering loop catalog information for " + numLoops + " loops ... " );

		int count=0;
		int succeeded = 0;
		int failed = 0;
		double progressPercent = 10;
		
		Map<String, Q> subsystemMethods = getSubsystemMethods();
		loopInfoModel = new HashMap<String, LoopInfo>();
		AtlasHashSet<Node> loopHeaders = new AtlasHashSet<Node>(Query.universe().nodes(XCSG.Loop).eval().nodes());
		int inMemory = 0;
		int recompute = 0;
		int updateCodemap = 0;
		for(Node loopHeader : loopHeaders) {
			final int countLoops = ++count;
			Object loopHeaderID = loopHeader.getAttr(CFGNode.LOOP_HEADER_ID);
			if(loopHeaderID!=null && StringUtils.isNumeric(loopHeaderID.toString())) {

				LoopInfo loopInfo = null;
				if(loopHeader.hasAttr(LOOP_SUMMARY) && loopHeader.getAttr(LOOP_SUMMARY)!=null) {
					String jsonString = Attributes.getAttributeValueAsString(loopHeader, LOOP_SUMMARY, "");
					try {
						ObjectMapper jsonMapper = new ObjectMapper();
						loopInfo = jsonMapper.readValue(jsonString, LoopInfo.class);
						loopInfo.setLoopRuntimeInfo(getLoopRuntimeInfo(loopHeader));
						loopInfo.updateFieldMaps();
						inMemory++;
					} catch (IOException e) {
						Log.error("Unable to deserialize loop info from LOOP_SUMMARY attribute in the codemap for loop "+loopHeaderID, e);
					}
				} 
				
				if(loopInfo == null) {
					loopInfo = getLoopInfo(loopHeader, subsystemMethods, lcsStats);
					recompute++;
				}
				
				try {
					ObjectMapper jsonMapper = new ObjectMapper();
					String jsonString = jsonMapper.writeValueAsString(loopInfo);
					loopHeader.putAttr(LOOP_SUMMARY, jsonString);	
					updateCodemap++;
				} catch (JsonProcessingException e) {
					Log.error("Unable to serialize loop info as json string attribute in the codemap for loop "+loopHeaderID, e);
				}
				
				loopInfoModel.put(loopHeaderID.toString(), loopInfo);
				succeeded++;
			} else {
				Log.error("Loop with header ID "+loopHeaderID+" not found", new IllegalArgumentException("Error finding loop with ID "+loopHeaderID));
				failed++;
			}
			if(((count*100) / numLoops) > progressPercent || (count==numLoops-1)) {
				Log.info(((int)((count*100) / numLoops)) + "% complete; "+count+" done; "+succeeded+" succeeded; "+failed+" failed; "+(numLoops-count)+" remaining");
				progressPercent += 10;
			}
			Display.getDefault().syncExec(new Runnable(){
                public void run() {
                	lblCatalog.setText("Catalog: "+(countLoops));
                	lblCatalog.redraw();
                }
            });
		}
		Log.info("Completed gathering loop catalog information for " + numLoops + " loops");
		Log.info("Loop catalog load summary -- inMemory: "+inMemory+"; recompute: "+recompute+"; updateCodemap: "+updateCodemap);
	}

	/**
	 * Precompute the methods and loop headers which reach subsystem methods,
	 * for efficient interaction calculations
	 * @return
	 */
	private Map<String, Q> getSubsystemMethods() {
		Map<String,Q> subsystemMethods = new HashMap<>();
		
		String tags [] = new String [] {JavaCoreSubsystem.TAG, NetworkSubsystem.TAG, IOSubsystem.TAG, LogSubsystem.TAG,
				MathSubsystem.TAG, RandomSubsystem.TAG, ThreadingSubsystem.TAG, CollectionSubsystem.TAG, IteratorSubsystem.TAG,
				ImplicitLoopSubsystem.TAG, FunctionalSubsystem.TAG, SpliteratorSubsystem.TAG, StreamSubsystem.TAG, CompressionSubsystem.TAG,
				HardwareSubsystem.TAG, RMISubsystem.TAG, DatabaseSubsystem.TAG, SerializationSubsystem.TAG, UISubsystem.TAG,
				IntrospectionSubsystem.TAG, TestingSubsystem.TAG, GarbageCollectionSubsystem.TAG, SecuritySubsystem.TAG, CryptoSubsystem.TAG,  
				DataStructureSubsystem.TAG, OtherLibrariesSubsystem.TAG, AppSubsystem.TAG};

		for (String tag : tags) {
			Q methods = Query.resolve(null, Subsystems.getSubsystemContents(tag).nodes(XCSG.Method));
			Q reachesMethods = Query.resolve(null, Query.universe().edges(EnhancedLoopCallGraph.Edge).reverse(methods));
			subsystemMethods.put(tag, reachesMethods);
		}
		return subsystemMethods;
	}

	public LoopInfo getLoopInfo(Node loopHeader, Map<String, Q> subsystemMethods, LoopCallSiteStats lcsStats) {
		Q loopHeaderQ = Common.toQ(loopHeader);

		//LOOP_HEADER_ID	Project	Type	Method	Header Statement	Container	No. of Terminating Conditions	Monotonic	Pattern	Lower Bound	Upper Bound	Data Flow Nodes	Data Flow Edges	Taint Graph Nodes	Taint Graphs Edges	Control Flow Nodes	Control Flow Edges	PCG Nodes	PCG Edges	NestingDepth	CompressionSubsystem	CryptoSubSystem	DatabaseSubsystem	DataStructureSubsystem	GarbageCollectionSubsystem	HardwareSubsystem	IntrospectionSubsystem	IOSubsystem	JavaCoreSubsystem	LogSubsystem	MathSubsystem	NetworkSubsystem	RandomSubsystem	RMISubsystem	SecuritySubsystem	SerializationSubsystem	TestingSubsystem	ThreadingSubsystem	UISubsystem	NumSubsystems	callSites	callSitesJDK	conditionalCallSites	conditionalCallSitesJDK	numPaths	ContainsCallSites	ContainsJDKCallSites	ContainsJDKCallSitesOnly	ContainsNoCallSites	ContainsNoJDKCallSites	ContainsNonJDKCallSites
		Integer headerID = Integer.parseInt(loopHeader.getAttr(CFGNode.LOOP_HEADER_ID).toString());
		String projectString = LoopCallSiteStats.getProjectContainingLoop(loopHeader);
		String packageName = loopHeaderQ.containers().nodes(XCSG.Package).eval().nodes().one().getAttr(XCSG.name).toString();//CommonQueries.getContainingFunctions(Common.toQ(loopHeader)).reverseStepOn(Query.universe().edges(XCSG.Contains),2).roots().eval().nodes().one().getAttr(XCSG.name).toString();
		String typeName = loopHeaderQ.containers().nodes(XCSG.Type).eval().nodes().one().getAttr(XCSG.name).toString();//CommonQueries.getContainingFunctions(Common.toQ(loopHeader)).reverseStepOn(Query.universe().edges(XCSG.Contains),1).roots().eval().nodes().one().getAttr(XCSG.name).toString();
		String methodName = loopHeaderQ.containers().nodes(XCSG.Method).eval().nodes().one().getAttr(XCSG.name).toString();//CommonQueries.getContainingFunction(loopHeader).getAttr(XCSG.name).toString();
		String loopStatement = LoopCallSiteStats.getLoopStatement(loopHeader);
		String container = Common.toQ(loopHeader).parent().eval().nodes().one().getAttr(XCSG.name).toString();
		Integer nestingDepth = (loopHeader.getAttr(LoopUtils.NESTING_DEPTH)!=null)?Integer.parseInt(loopHeader.getAttr(LoopUtils.NESTING_DEPTH).toString()):0;
		boolean monotonicity = (loopHeader.taggedWith(MonotonicityPatternConstants.MONOTONIC_LOOP)?true:false);
		
		Q tcs = TerminatingConditions.findBoundaryConditionsForLoopHeader(loopHeader);
				//CommonQueries.getContainingFunctions(loopHeaderQ).contained().nodes(TerminatingConditions.TERMINATING_CONDITION);
		
		Integer numTerminatingConditions = 0;
		if(!CommonQueries.isEmpty(tcs)) {
			numTerminatingConditions = (int) tcs.eval().nodes().size();
		}

		String loopTerminationPattern = "";
		if(loopHeader.getAttr("LOOP_TERMINATION_PATTERN") != null) {
			loopTerminationPattern = loopHeader.getAttr("LOOP_TERMINATION_PATTERN").toString();
		}
		
		String lowerBound = "";
		if(loopHeader.getAttr("LOOP_LOWER_BOUND") != null) {
			lowerBound = loopHeader.getAttr("LOOP_LOWER_BOUND").toString();
		}

		String upperBound = "";
		if(loopHeader.getAttr("LOOP_UPPER_BOUND") != null) {
			upperBound = loopHeader.getAttr("LOOP_UPPER_BOUND").toString();
		}
		
		Integer countSS = 0;
		
		boolean javacoreSS = hasSubsystemInteractions(loopHeader, subsystemMethods, JavaCoreSubsystem.TAG);
		boolean networkSS = hasSubsystemInteractions(loopHeader, subsystemMethods, NetworkSubsystem.TAG);
		boolean ioSS = hasSubsystemInteractions(loopHeader, subsystemMethods, IOSubsystem.TAG);
		boolean logSS = hasSubsystemInteractions(loopHeader, subsystemMethods, LogSubsystem.TAG);
		boolean mathSS = hasSubsystemInteractions(loopHeader, subsystemMethods, MathSubsystem.TAG);
		boolean randomSS = hasSubsystemInteractions(loopHeader, subsystemMethods, RandomSubsystem.TAG);
		boolean threadingSS = hasSubsystemInteractions(loopHeader, subsystemMethods, ThreadingSubsystem.TAG);
		boolean collectionSS = hasSubsystemInteractions(loopHeader, subsystemMethods, CollectionSubsystem.TAG);
		boolean iteratorSS = hasSubsystemInteractions(loopHeader, subsystemMethods, IteratorSubsystem.TAG);
		boolean implicitLoopSS = hasSubsystemInteractions(loopHeader, subsystemMethods, ImplicitLoopSubsystem.TAG);
		boolean functionalSS = hasSubsystemInteractions(loopHeader, subsystemMethods, FunctionalSubsystem.TAG);
		boolean spliteratorSS = hasSubsystemInteractions(loopHeader, subsystemMethods, SpliteratorSubsystem.TAG);
		boolean streamSS = hasSubsystemInteractions(loopHeader, subsystemMethods, StreamSubsystem.TAG);
		boolean compressionSS = hasSubsystemInteractions(loopHeader, subsystemMethods, CompressionSubsystem.TAG);
		boolean hardwareSS = hasSubsystemInteractions(loopHeader, subsystemMethods, HardwareSubsystem.TAG);		
		boolean rmiSS = hasSubsystemInteractions(loopHeader, subsystemMethods, RMISubsystem.TAG);
		boolean databaseSS = hasSubsystemInteractions(loopHeader, subsystemMethods, DatabaseSubsystem.TAG);
		boolean serializationSS = hasSubsystemInteractions(loopHeader, subsystemMethods, SerializationSubsystem.TAG);
		boolean uiSS = hasSubsystemInteractions(loopHeader, subsystemMethods, UISubsystem.TAG);
		boolean introspectionSS = hasSubsystemInteractions(loopHeader, subsystemMethods, IntrospectionSubsystem.TAG);
		boolean testingSS = hasSubsystemInteractions(loopHeader, subsystemMethods, TestingSubsystem.TAG);
		boolean gcSS = hasSubsystemInteractions(loopHeader, subsystemMethods, GarbageCollectionSubsystem.TAG);
		boolean securitySS = hasSubsystemInteractions(loopHeader, subsystemMethods, SecuritySubsystem.TAG);
		boolean cryptoSS = hasSubsystemInteractions(loopHeader, subsystemMethods, CryptoSubsystem.TAG);
		boolean datastructureSS = hasSubsystemInteractions(loopHeader, subsystemMethods, DataStructureSubsystem.TAG);
		boolean otherLibSS = hasSubsystemInteractions(loopHeader, subsystemMethods, OtherLibrariesSubsystem.TAG);
		boolean appSS = hasSubsystemInteractions(loopHeader, subsystemMethods, AppSubsystem.TAG);

		countSS += javacoreSS?1:0;
		countSS += hardwareSS?1:0;
		countSS += ioSS?1:0;
		countSS += networkSS?1:0;
		countSS += rmiSS?1:0;
		countSS += databaseSS?1:0;
		countSS += logSS?1:0;
		countSS += serializationSS?1:0;
		countSS += compressionSS?1:0;
		countSS += uiSS?1:0;
		countSS += introspectionSS?1:0;
		countSS += testingSS?1:0;
		countSS += gcSS?1:0;
		countSS += securitySS?1:0;
		countSS += cryptoSS?1:0;
		countSS += mathSS?1:0;
		countSS += randomSS?1:0;
		countSS += threadingSS?1:0;
		countSS += datastructureSS?1:0;
		countSS += collectionSS?1:0;
		countSS += iteratorSS?1:0;
		countSS += implicitLoopSS?1:0;
		countSS += functionalSS?1:0;
		countSS += spliteratorSS?1:0;
		countSS += streamSS?1:0;
		countSS += otherLibSS?1:0;
		countSS += appSS?1:0;
		
//		Q lcefg = LoopAbstractions.getTerminationPCG(loopHeaderQ);
//		Q tg = LoopAbstractions.taintGraphWithoutCycles(loopHeaderQ);
//		Q lcfg = lcefg.difference(Query.universe().nodes("EventFlow_Master_Entry","EventFlow_Master_Exit"));
//		lcfg = Query.universe().edges(XCSG.ControlFlow_Edge).between(loopHeaderQ, lcfg.nodes(BoundaryConditions.BOUNDARY_CONDITION));
//		Q df = lcfg.contained().induce(Common.edges(XCSG.LocalDataFlow));
		Long dfNodes = 0L;//df.eval().nodes().size();
		Long dfEdges = 0L;//df.eval().edges().size();
		Long tgNodes = 0L;//tg.eval().nodes().size();
		Long tgEdges = 0L;//tg.eval().edges().size();
		Long cfNodes = 0L;//lcfg.eval().nodes().size();
		Long cfEdges = 0L;//lcfg.eval().edges().size();
		Long lcefgNodes = 0L;//lcefg.eval().nodes().size();
		Long lcefgEdges = 0L;//lcefg.eval().edges().size();

		// callsite stats
		LoopCallsiteCounts callsiteCounts = lcsStats.computeCallsiteStats(loopHeader);
		Long callsitesInTheLoop = callsiteCounts.getCallsitesInLoop();
		Long callsitesInTheLoopJDK = callsiteCounts.getCallsitesInLoopJDK();
		Long conditionalCallsitesInTheLoop = callsiteCounts.getConditionalCallsitesInLoop();
		Long conditionalCallsitesInTheLoopJDK = callsiteCounts.getConditionalCallsitesInLoopJDK();
		
		// path statistics
		LoopPathCounts loopPathCounts = LoopPathCountStats.computePathCountStats(loopHeader); 
		long pathCount = loopPathCounts.getNumPaths();
		long pathsWithCallSites = loopPathCounts.getNumPathsWithCallSites();
		long pathsWithJDKCallSites = loopPathCounts.getNumPathsWithJDKCallSites();
		long pathsWithJDKCallSitesOnly = loopPathCounts.getNumPathsWithJDKCallSitesOnly();
		long pathsWithoutCallSites = loopPathCounts.getNumPathsWithoutCallSites();
		long pathsWithoutJDKCallSites = loopPathCounts.getNumPathsWithoutJDKCallSites();
		long pathsWithNonJDKCallSites = loopPathCounts.getNumPathsWithNonJDKCallSites();
		
		List<String> cfEntryPointsFromWhichLoopIsReachable = Attributes.ListAttr.toListOfStrings((String) loopHeader.getAttr(ReachabilityAnalysisCodemapStage.CONTROL_FLOW_REACHABLE_FROM));
		int numCfEntryPointsFromWhichLoopIsReachable = cfEntryPointsFromWhichLoopIsReachable!=null?cfEntryPointsFromWhichLoopIsReachable.size():0;
		List<String> dfEntryPointsFromWhichLoopIsReachable = Attributes.ListAttr.toListOfStrings((String) loopHeader.getAttr(ReachabilityAnalysisCodemapStage.DATA_FLOW_REACHABLE_FROM));
		int numDfEntryPointsFromWhichLoopIsReachable = dfEntryPointsFromWhichLoopIsReachable!=null?dfEntryPointsFromWhichLoopIsReachable.size():0;
		Q branchesContainedInLoopQ = LoopUtils.getControlFlowGraph(loopHeader).nodes(XCSG.ControlFlowCondition);
		List<String> listOfBranchesContainedInLoop = new ArrayList<String>();
		for(Node b: branchesContainedInLoopQ.eval().nodes()) {
			listOfBranchesContainedInLoop.add((String)b.getAttr(BranchCatalogCodemapStage.BRANCH_ID));
		}
		int numBranchesContainedInLoop = listOfBranchesContainedInLoop.size();
		Object governingBranches = loopHeader.getAttr("GoverningBranches");
		List<String> branchesGoverningLoop = Attributes.ListAttr.toListOfStrings((String)(governingBranches!=null?governingBranches.toString():""));;
		int numBranchesGoverningLoop = branchesGoverningLoop.size();
		
		Q lambdaLoopsContainedInLoopQ = LoopUtils.getControlFlowGraph(loopHeader).nodes(LambdaIdentifier.LAMBDA_LOOP);
		List<String> listOfLambdaLoopsContainedInLoop = new ArrayList<String>();
		for(Node ll : lambdaLoopsContainedInLoopQ.eval().nodes()) {
			listOfLambdaLoopsContainedInLoop.add((String)ll.getAttr(LambdaIdentifier.LAMBDA_LOOP_ID));
		}
		int numLambdaLoopsContainedInLoop = listOfLambdaLoopsContainedInLoop.size();
		
		LoopRuntimeInfo runtimeInfo = getLoopRuntimeInfo(loopHeader);

		LoopInfo loopInfo = new LoopInfo(headerID, projectString, packageName, typeName, methodName, loopStatement, container, 
			numTerminatingConditions, nestingDepth, monotonicity, loopTerminationPattern, lowerBound, upperBound, 
			dfNodes, dfEdges, tgNodes, tgEdges, cfNodes, cfEdges, lcefgNodes, lcefgEdges, 
			numCfEntryPointsFromWhichLoopIsReachable, cfEntryPointsFromWhichLoopIsReachable, 
			numDfEntryPointsFromWhichLoopIsReachable, dfEntryPointsFromWhichLoopIsReachable,
			numBranchesContainedInLoop, listOfBranchesContainedInLoop, numBranchesGoverningLoop, 
			branchesGoverningLoop, numLambdaLoopsContainedInLoop, listOfLambdaLoopsContainedInLoop,
			countSS, javacoreSS, networkSS, ioSS, logSS, mathSS, randomSS, threadingSS, collectionSS, iteratorSS, implicitLoopSS, functionalSS, spliteratorSS, streamSS,
			hardwareSS, rmiSS, databaseSS, serializationSS, compressionSS, uiSS, introspectionSS, testingSS, gcSS, securitySS, cryptoSS, datastructureSS,  otherLibSS, appSS,  
			callsitesInTheLoop, callsitesInTheLoopJDK, conditionalCallsitesInTheLoop, conditionalCallsitesInTheLoopJDK, 
			pathCount, pathsWithCallSites, pathsWithJDKCallSites, pathsWithJDKCallSitesOnly, pathsWithoutCallSites, pathsWithoutJDKCallSites, pathsWithNonJDKCallSites,
			runtimeInfo);
		
		return loopInfo;
	}
	
	private boolean hasSubsystemInteractions(Node loopHeader, Map<String, Q> subsystemMethods, String tag) {
		Q cachedMethods = subsystemMethods.get(tag);
		if (cachedMethods==null) {
			Log.error("No methods cached for tag: " + tag, null);
			return false;
		}
		return cachedMethods.eval().nodes().contains(loopHeader);
	}

	private LoopRuntimeInfo getLoopRuntimeInfo(Node loopHeader) {
		Long iterationCount = Attributes.getLongAttributeValue(loopHeader,Import.STATEMENT_EXECUTION_COUNT_ATTRIBUTE_NAME);
		Long avgExecutionTime = Attributes.getLongAttributeValue(loopHeader,"AVG_LOOP_EXECUTION_TIME_ATTRIBUTE");
		Long avgIterationTime = Attributes.getLongAttributeValue(loopHeader,"AVG_LOOP_ITERATION_TIME_ATTRIBUTE");
		LoopRuntimeInfo lri = new LoopRuntimeInfo(iterationCount, avgIterationTime, avgExecutionTime);
		return lri;
	}

	public Q getDataAsQForSelectedTableItems(TableItem[] selection, String string) {
		AtlasSet<Node> selectedLoops = new AtlasHashSet<Node>();
		for(TableItem item : selection) {
			Node loop = (Node)item.getData(string);
			selectedLoops.add(loop);
		}
		return Common.toQ(selectedLoops);
	}
	
	public Boolean checkExistingArtifactsWithTag(String prefixedTagName) {
		Q existingArtifactsWithTagName = Query.universe().nodes(prefixedTagName).union(Query.universe().edges(prefixedTagName).retainEdges());
		if(!CommonQueries.isEmpty(existingArtifactsWithTagName)) {
			return promptUserToTagArtifactsWithExistingTagName(prefixedTagName);
		}
		return true;
	}
	
	public Boolean promptUserToTagArtifactsWithExistingTagName(String prefixedTagName) {
		Log.info("Artifacts with user defined tag name already exist.");
		Boolean choice = DisplayUtils.promptBoolean("Artifact tag exists","Artifacts with tag name "+prefixedTagName+" already exist. Do you want to continue with this tag name?");
		return choice;
	}

	@Override
	public void selectionChanged(Graph selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void indexBecameUnaccessible() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void indexBecameAccessible() {
		// TODO Auto-generated method stub
		
	}
}