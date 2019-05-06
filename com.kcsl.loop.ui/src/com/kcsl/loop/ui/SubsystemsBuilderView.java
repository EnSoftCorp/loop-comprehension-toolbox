package com.kcsl.loop.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.IAtlasSelectionListener;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;
import com.kcsl.loop.ui.log.Log;

public class SubsystemsBuilderView extends ViewPart {

	private static final int FONT_SIZE = 11;
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.kcsl.loop.ui.subsystemsBuilderView";
	
	// the current Atlas selection
	private AtlasSet<Node> selection =  new AtlasHashSet<Node>();

	private static Map<String,SubsystemComponents> subsystems = new HashMap<String,SubsystemComponents>();
	
	public static SubsystemComponents getSubsystemComponents(String builderName) {
		return subsystems.get(builderName);
	}
	
	private static boolean initialized = false;
	private static int subsystemsCounter = 1;
	
	/**
	 * The constructor.
	 */
	public SubsystemsBuilderView() {
		// intentionally left blank
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		final CTabFolder subsystemsFolder = new CTabFolder(parent, SWT.CLOSE);
		subsystemsFolder.setBorderVisible(true);
		subsystemsFolder.setSimple(false); // adds the Eclise style "swoosh"
		subsystemsFolder.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		// add a prompt to ask if we should really close the builder tab
		subsystemsFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(),
						SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("Close Subsystems builder instance?");
				messageBox.setText("Closing Tab");
				int response = messageBox.open();
				if (response == SWT.YES) {
					String tabName = subsystemsFolder.getSelection().getText();
					subsystems.remove(tabName);
				} else {
					event.doit = false;
				}
			}
		});
		
		subsystemsFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		// create a new SubsystemComponent if this is the first launch
		if(!initialized){
			int SUBSYSTEMS_NUMBER = (subsystemsCounter++);
			String SUBSYSTEMS_NAME = "SUBSYSTEM" + SUBSYSTEMS_NUMBER;
			SubsystemComponents subsystem = new SubsystemComponents(SUBSYSTEMS_NAME);
			subsystems.put(SUBSYSTEMS_NAME, subsystem);
			addSubsystems(subsystemsFolder, subsystem);
			initialized = true;
		} else {
			// otherwise load what is already in memory
			ArrayList<SubsystemComponents> sortedSubsystems = new ArrayList<SubsystemComponents>(subsystems.values());
			Collections.sort(sortedSubsystems); // sorted by creation time
			for(SubsystemComponents subsystem : sortedSubsystems){
				addSubsystems(subsystemsFolder, subsystem);
			}
		}
		
		// add an add SubsystemComponents tab button to the action bar
		final Action addSubsystemAction = new Action() {
			public void run() {
				int SUBSYSTEM_NUMBER = (subsystemsCounter++);
				String SUBSYSTEM_NAME = "SUBSYSTEM" + SUBSYSTEM_NUMBER;
				SubsystemComponents subsystem = new SubsystemComponents(SUBSYSTEM_NAME);
				subsystems.put(SUBSYSTEM_NAME, subsystem);
				addSubsystems(subsystemsFolder, subsystem);
			}
		};
		addSubsystemAction.setText("New SUBSYSTEM");
		//TODO: implement
//		addPCGAction.setToolTipText("Creates another PCG builder tab");
//		addPCGAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEW_CONFIG));
//		addPCGAction.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEW_CONFIG));
//		addPCGAction.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEW_CONFIG));
//		getViewSite().getActionBars().getToolBarManager().add(addPCGAction);
		
		addSubsystemAction.setToolTipText("Creates another Subsystem interaction builder tab");
		ImageDescriptor newConfigurationIcon = ImageDescriptor.createFromImage(ResourceManager.getPluginImage("org.rulersoftware.ui", "icons/new_configuration_button.png"));
		addSubsystemAction.setImageDescriptor(newConfigurationIcon);
		addSubsystemAction.setDisabledImageDescriptor(newConfigurationIcon);
		addSubsystemAction.setHoverImageDescriptor(newConfigurationIcon);
		getViewSite().getActionBars().getToolBarManager().add(addSubsystemAction);
		
		// setup the Atlas selection event listener
		IAtlasSelectionListener selectionListener = new IAtlasSelectionListener(){
			@Override
			public void selectionChanged(IAtlasSelectionEvent atlasSelection) {
				try {
					selection = atlasSelection.getSelection().eval().nodes();
				} catch (Exception e){
					selection = new AtlasHashSet<Node>();
				}
			}				
		};
		
		// add the selection listener
		SelectionUtil.addSelectionListener(selectionListener);
	}
	
	private void addSubsystems(final CTabFolder subsystemFolder, final SubsystemComponents subsystem) {
		final CTabItem subsystemTab = new CTabItem(subsystemFolder, SWT.NONE);
		subsystemTab.setText(subsystem.getName());
		
		Composite subsystemComposite = new Composite(subsystemFolder, SWT.NONE);
		subsystemTab.setControl(subsystemComposite);
		subsystemComposite.setLayout(new GridLayout(1, false));
		
		Composite subsystemControlPanelComposite = new Composite(subsystemComposite, SWT.NONE);
		subsystemControlPanelComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		subsystemControlPanelComposite.setLayout(new GridLayout(3, false));
		
		Label subsystemNameLabel = new Label(subsystemControlPanelComposite, SWT.NONE);
		subsystemNameLabel.setSize(66, 14);
		subsystemNameLabel.setText("Subsystem Interactions Label: ");
		subsystemNameLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		final Text subsystemLabelText = new Text(subsystemControlPanelComposite, SWT.BORDER);
		subsystemLabelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		subsystemLabelText.setSize(473, 19);
		subsystemLabelText.setText(subsystem.getName());
		
		subsystemLabelText.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent event) {
				if(event.detail == SWT.TRAVERSE_RETURN){
					String newName = subsystemLabelText.getText();
					subsystemTab.setText(newName);
					subsystem.setName(newName);
				}
			}
		});
		
		Button showButton = new Button(subsystemControlPanelComposite, SWT.NONE);
		showButton.setText("Show Loops Interacting with Subsystem");
		showButton.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		Composite subsystemBuilderComposite = new Composite(subsystemComposite, SWT.NONE);
		subsystemBuilderComposite.setLayout(new GridLayout(1, false));
		subsystemBuilderComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		SashForm groupSashForm = new SashForm(subsystemBuilderComposite, SWT.NONE);
		groupSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		groupSashForm.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		Group loopsGroup = new Group(groupSashForm, SWT.NONE);
		loopsGroup.setText("Loops");
		loopsGroup.setLayout(new GridLayout(1, false));
		loopsGroup.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		Composite addLoopHeaderElementComposite = new Composite(loopsGroup, SWT.NONE);
		addLoopHeaderElementComposite.setLayout(new GridLayout(2, false));
		addLoopHeaderElementComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label addLoopHeaderElementLabel = new Label(addLoopHeaderElementComposite, SWT.NONE);
		addLoopHeaderElementLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		addLoopHeaderElementLabel.setText("Add Selected");
		addLoopHeaderElementLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		Label addLoopHeaderElementButton = new Label(addLoopHeaderElementComposite, SWT.NONE);
		addLoopHeaderElementButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		addLoopHeaderElementButton.setImage(ResourceManager.getPluginImage("org.rulersoftware.ui", "icons/add_button.png"));
		
		Composite clearLoopHeaderElementComposite = new Composite(loopsGroup, SWT.NONE);
		clearLoopHeaderElementComposite.setLayout(new GridLayout(2, false));
		clearLoopHeaderElementComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label clearLoopHeaderElementLabel = new Label(clearLoopHeaderElementComposite, SWT.NONE);
		clearLoopHeaderElementLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		clearLoopHeaderElementLabel.setText("Clear All");
		clearLoopHeaderElementLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		Label clearLoopHeaderElementButton = new Label(clearLoopHeaderElementComposite, SWT.NONE);
		clearLoopHeaderElementButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		clearLoopHeaderElementButton.setImage(ResourceManager.getPluginImage("org.rulersoftware.ui", "icons/delete_button.png"));
		clearLoopHeaderElementButton.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		final ScrolledComposite loopHeadersScrolledComposite = new ScrolledComposite(loopsGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		loopHeadersScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		loopHeadersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		loopHeadersScrolledComposite.setExpandHorizontal(true);
		loopHeadersScrolledComposite.setExpandVertical(true);
//		sashForm.setWeights(new int[] {1, 1});
//		groupSashForm.setWeights(new int[] {250, 441});
		
		final Group functionsGroup = new Group(groupSashForm, SWT.NONE);
		functionsGroup.setText("Functions");
		functionsGroup.setLayout(new GridLayout(1, false));
		functionsGroup.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		Composite addFunctionComposite = new Composite(functionsGroup, SWT.NONE);
		addFunctionComposite.setLayout(new GridLayout(2, false));
		addFunctionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label addFunctionLabel = new Label(addFunctionComposite, SWT.NONE);
		addFunctionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		addFunctionLabel.setBounds(0, 0, 59, 14);
		addFunctionLabel.setText("Add Selected");
		addFunctionLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		final Label addFunctionButton = new Label(addFunctionComposite, SWT.NONE);
		addFunctionButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		addFunctionButton.setSize(20, 20);
		addFunctionButton.setImage(ResourceManager.getPluginImage("org.rulersoftware.ui", "icons/add_button.png"));
		
		Composite clearFunctionComposite = new Composite(functionsGroup, SWT.NONE);
		clearFunctionComposite.setLayout(new GridLayout(2, false));
		clearFunctionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label clearFunctionLabel = new Label(clearFunctionComposite, SWT.NONE);
		clearFunctionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		clearFunctionLabel.setBounds(0, 0, 59, 14);
		clearFunctionLabel.setText("Clear All");
		clearFunctionLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		
		final Label clearFunctionButton = new Label(clearFunctionComposite, SWT.NONE);
		clearFunctionButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		clearFunctionButton.setSize(20, 20);
		clearFunctionButton.setImage(ResourceManager.getPluginImage("org.rulersoftware.ui", "icons/delete_button.png"));
		
		final ScrolledComposite functionsScrolledComposite = new ScrolledComposite(functionsGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		functionsScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		functionsScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		functionsScrolledComposite.setExpandHorizontal(true);
		functionsScrolledComposite.setExpandVertical(true);
		
		addFunctionButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				if(selection.isEmpty()){
					DisplayUtils.showError("Nothing is selected.");
				} else {
					AtlasSet<Node> functions = getFilteredSelections(XCSG.Function);
					
					if(functions.isEmpty()){
						DisplayUtils.showError("Selections must be functions.");
					} else {
						if(subsystem.addFunctionsSelectedForInteraction(functions)){
							refreshSubsystemFunctions(functionsGroup, functionsScrolledComposite, loopHeadersScrolledComposite, subsystem);
						}
					}
				}
			}
		});
		
		clearFunctionButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				subsystem.getFunctionsSelectedForInteraction().clear();
			}
		});
		
		addLoopHeaderElementButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if(selection.isEmpty()){
					DisplayUtils.showError("Nothing is selected.");
				} else {
//					AtlasSet<Node> controlFlowNodes = getFilteredSelections(XCSG.ControlFlow_Node);
					AtlasSet<Node> loopHeaderNodes = getFilteredSelections(XCSG.Loop);
					
					// expand search to control flow nodes that correspond to this node
					if(loopHeaderNodes.isEmpty()){
						loopHeaderNodes = Common.toQ(selection).containers().nodes(XCSG.ControlFlow_Node).eval().nodes();
					}
					
					if(loopHeaderNodes.isEmpty()){
						DisplayUtils.showError("Selections must correspond to loop headers.");
					} else {
						if(subsystem.addLoopHeadersSelected(loopHeaderNodes)){
							refreshLoopHeaders(loopHeadersScrolledComposite,subsystem);
						}
						/*AtlasSet<Node> containingFunctions = new AtlasHashSet<Node>();
						for(Node controlFlowNode : loopHeaderNodes){
							containingFunctions.add(CommonQueries.getContainingFunction(controlFlowNode));
						}
						if(subsystem.addFunctionsSelectedForInteraction(containingFunctions)){
							refreshSubsystemFunctions(functionsGroup, functionsScrolledComposite, loopHeadersScrolledComposite, subsystem);
						}*/
					}
				}
			}
		});
		
		clearLoopHeaderElementButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				subsystem.getLoopHeadersSelected().clear();
			}
		});
		
		showButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean noLoopHeaders = subsystem.getLoopHeadersSelected().isEmpty();
				Log.info("Loop headers selected: "+subsystem.getLoopHeadersSelected().size());
//				boolean noDifferentiatingCallsiteEvents = subsystem.getDifferentiatingCallsitesSetA().isEmpty() && subsystem.getDifferentiatingCallsitesSetB().isEmpty();
//				if(noControlFlowEvents && noDifferentiatingCallsiteEvents){
				if(noLoopHeaders){
					DisplayUtils.showError("No loop headers defined.");
				} else if(subsystem.getFunctionsSelectedForInteraction().isEmpty()){
					DisplayUtils.showError("No functions defined for interaction.");
				} else {
					Log.info("Loop headers selected: "+subsystem.getLoopHeadersSelected().size());
					Q functions = Common.toQ(subsystem.getFunctionsSelectedForInteraction());
//					Q methods1 = Common.toQ(subsystem.getDifferentiatingCallsitesSetA());
//					Q methods2 = Common.toQ(subsystem.getDifferentiatingCallsitesSetB());
					Q loopHeaders = Common.toQ(subsystem.getLoopHeadersSelected());
//					Q pcgResult = IPCGFactory.getIPCGFromInteractions(functions, methods1, methods2, events);
//					DisplayUtils.show(pcgResult, subsystem.getName());
					Q interactingLoops = getInteractingLoops(loopHeaders, functions);
					Log.info("Loop headers filtered: "+interactingLoops.eval().nodes().size());
					Log.info("SubsystemComponent name: "+subsystem.getName());
					DisplayUtils.show(interactingLoops, subsystem.getName());
				}
			}
		});
		
		// set the tab selection to this newly created tab
		subsystemFolder.setSelection(subsystemFolder.getItemCount()-1);
	}
	
	public static Q getInteractingLoops(Q loopHeaders, Q functions) {
		Q interactingLoops = Common.empty();
		for(Node loopHeader: loopHeaders.eval().nodes()) {
			Q interactions = getLoopInteractions(functions, loopHeader);
			if(!CommonQueries.isEmpty(interactions)) {
				interactingLoops = interactingLoops.union(Common.toQ(loopHeader));
			}
		}
		return interactingLoops;
	}

	public static Q getLoopInteractions(Q functions, Node loopHeader) {
//		Q loopCFG = LoopUtils.getControlFlowGraph(loopHeader);
		Q loopCFG = Common.edges(XCSG.ControlFlow_Edge).between(Common.toQ(loopHeader), Common.toQ(loopHeader));	
		Log.info("Loop "+loopHeader.getAttr(XCSG.name).toString());
		Log.info("CFG size: "+loopCFG.eval().nodes().size());
		Q loopCFGForward = loopCFG.forwardOn(Common.edges(Attr.Edge.PER_CONTROL_FLOW));
		Q reverseOnInteractingFunctions = functions.reverseOn(Common.edges(Attr.Edge.PER_CONTROL_FLOW));
		Q interactions = loopCFGForward.intersection(reverseOnInteractingFunctions);
		Log.info("Interactions size: "+interactions.eval().nodes().size());
		return interactions;
	}
	
	/**
	 * Given a callsite this method returns the invoked method signature
	 * @param callsite
	 * @return
	 */
	public static Node getInvokedMethodSignature(Node callsite) {
		// XCSG.InvokedSignature connects a dynamic dispatch to its signature method
		// XCSG.InvokedFunction connects a static dispatch to it actual target method
		Q invokedEdges = Query.universe().edges(XCSG.InvokedSignature, XCSG.InvokedFunction);
		Node method = invokedEdges.successors(Common.toQ(callsite)).eval().nodes().one();
		return method;
	}
	
	private void refreshSubsystemFunctions(final Group entryFunctionsGroup, final ScrolledComposite entryFunctionsScrolledComposite, final ScrolledComposite controlFlowEventsScrolledComposite, final SubsystemComponents subsystem) {
		Composite entryFunctionsScrolledCompositeContent = new Composite(entryFunctionsScrolledComposite, SWT.NONE);
		
		long numNodes = subsystem.getFunctionsSelectedForInteraction().size();
//		Q callEdges = Query.universe().edges(XCSG.Call);
//		long numEdges = Common.toQ(pcg.getCallGraphFunctions()).induce(callEdges).eval().edges().size();
		entryFunctionsGroup.setText("Functions (" 
				+ numNodes + " function" + (numNodes > 1 ? "s" : "") 
				/*+ ", " + numEdges + " induced edge" + (numEdges > 1 ? "s" : "")*/
				+ ")");
		
		for(final Node function : subsystem.getFunctionsSelectedForInteraction()){
			entryFunctionsScrolledCompositeContent.setLayout(new GridLayout(1, false));
			
			Label entryFunctionsSeperatorLabel = new Label(entryFunctionsScrolledCompositeContent, SWT.SEPARATOR | SWT.HORIZONTAL);
			entryFunctionsSeperatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			entryFunctionsSeperatorLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
			
			Composite entryFunctionsEntryComposite = new Composite(entryFunctionsScrolledCompositeContent, SWT.NONE);
			entryFunctionsEntryComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
			entryFunctionsEntryComposite.setLayout(new GridLayout(2, false));
			
			final Label deleteButton = new Label(entryFunctionsEntryComposite, SWT.NONE);
			deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
			deleteButton.setImage(ResourceManager.getPluginImage("org.rulersoftware.ui", "icons/delete_button.png"));

			Label functionLabel = new Label(entryFunctionsEntryComposite, SWT.NONE);
			functionLabel.setToolTipText(function.toString());
			functionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			functionLabel.setBounds(0, 0, 59, 14);
			functionLabel.setText(CommonQueries.getQualifiedFunctionName(function));
			functionLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
			
			deleteButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					AtlasSet<Node> controlFlowNodesToRemove = new AtlasHashSet<Node>();
					for(Node controlFlowNode : subsystem.getLoopHeadersSelected()){
						Node containingFunction = CommonQueries.getContainingFunction(controlFlowNode);
						if(function.equals(containingFunction)){
							controlFlowNodesToRemove.add(controlFlowNode);
						}
					}
					
					if(!controlFlowNodesToRemove.isEmpty()){
						/*MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(),
								SWT.ICON_QUESTION | SWT.YES | SWT.NO);
						messageBox.setMessage("Removing this function from the call graph context would remove " 
								+ controlFlowNodesToRemove.size() + " control flow events. Would you like to proceed?");
						messageBox.setText("Removing Control Flow Events");
						int response = messageBox.open();
						if (response == SWT.YES) {
							subsystem.removeFunctionsSelectedForInteraction(function);
							refreshSubsystemFunctions(entryFunctionsGroup, entryFunctionsScrolledComposite, controlFlowEventsScrolledComposite, subsystem);
							for(Node controlFlowEventToRemove : controlFlowNodesToRemove){
								subsystem.removeLoopHeadersSelected(controlFlowEventToRemove);
							}
							refreshLoopHeaders(controlFlowEventsScrolledComposite, subsystem);
						}*/
					} else {
						subsystem.removeFunctionsSelectedForInteraction(function);
						refreshSubsystemFunctions(entryFunctionsGroup, entryFunctionsScrolledComposite, controlFlowEventsScrolledComposite, subsystem);
					}
				}
			});
		}
		entryFunctionsScrolledComposite.setContent(entryFunctionsScrolledCompositeContent);
		entryFunctionsScrolledComposite.setMinSize(entryFunctionsScrolledCompositeContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void refreshLoopHeaders(final ScrolledComposite controlFlowEventsScrolledComposite, final SubsystemComponents subsystem) {
		Composite controlFlowEventsScrolledCompositeContent = new Composite(controlFlowEventsScrolledComposite, SWT.NONE);
		for(final Node event : subsystem.getLoopHeadersSelected()){
			controlFlowEventsScrolledCompositeContent.setLayout(new GridLayout(1, false));
			
			Label controlFlowEventsSeperatorLabel = new Label(controlFlowEventsScrolledCompositeContent, SWT.SEPARATOR | SWT.HORIZONTAL);
			controlFlowEventsSeperatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Composite controlFlowEventsEntryComposite = new Composite(controlFlowEventsScrolledCompositeContent, SWT.NONE);
			controlFlowEventsEntryComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
			controlFlowEventsEntryComposite.setLayout(new GridLayout(2, false));
			
			final Label deleteButton = new Label(controlFlowEventsEntryComposite, SWT.NONE);
			deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
			deleteButton.setImage(ResourceManager.getPluginImage("org.rulersoftware.ui", "icons/delete_button.png"));

			Label eventLabel = new Label(controlFlowEventsEntryComposite, SWT.NONE);
			eventLabel.setToolTipText(event.toString());
			eventLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			eventLabel.setBounds(0, 0, 59, 14);
			
			Node function = CommonQueries.getContainingFunction(event);
			eventLabel.setText(event.getAttr(XCSG.name).toString() 
					+ " (" + CommonQueries.getQualifiedFunctionName(function) + ")");
			eventLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
			
			deleteButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					subsystem.removeLoopHeadersSelected(event);
					refreshLoopHeaders(controlFlowEventsScrolledComposite, subsystem);
				}
			});
		}
		controlFlowEventsScrolledComposite.setContent(controlFlowEventsScrolledCompositeContent);
		controlFlowEventsScrolledComposite.setMinSize(controlFlowEventsScrolledCompositeContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private AtlasSet<Node> getFilteredSelections(String... tags){
		AtlasSet<Node> currentSelection = new AtlasHashSet<Node>(selection);
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		for(Node node : currentSelection){
			for(String tag : tags){
				if(node.taggedWith(tag)){
					result.add(node);
					break;
				}
			}
		}
		return result;
	}

	@Override
	public void setFocus() {
		// intentionally left blank
	}
}