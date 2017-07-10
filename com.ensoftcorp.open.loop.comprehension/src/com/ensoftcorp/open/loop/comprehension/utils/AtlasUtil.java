package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

import com.ensoftcorp.atlas.core.index.ProjectPropertiesUtil;
import com.ensoftcorp.atlas.core.indexing.IMappingSettings;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.atlas.core.licensing.AtlasLicenseException;
import com.ensoftcorp.atlas.jimple.core.settings.JimpleIndexingSettings;
import com.ensoftcorp.open.loop.comprehension.log.Log;

public class AtlasUtil {

	public static void openShellJob(final ExecutionEvent event, final IProject project) {
		UIJob uiJob = new UIJob("Open RULER Shell") {
			
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
					IViewPart view;
					view = workbenchPage.showView("com.ensoftcorp.atlas.ui.shell.views.AtlasShell", project.getName(),
							IWorkbenchPage.VIEW_VISIBLE);
					workbenchPage.activate(view);
					
				} catch (PartInitException e) {
					Log.error("Error opening RULER Shell Project", e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.schedule();
	}

	public static void mapProject(IProject project) throws AtlasLicenseException {
		// configure project for indexing
		
		// Disable indexing for all projects
		List<IProject> allEnabledProjects = ProjectPropertiesUtil.getAllEnabledProjects();
		ProjectPropertiesUtil.setIndexingEnabledAndDisabled(Collections.<IProject>emptySet(), allEnabledProjects);
		
		// Enable indexing for this project
		List<IProject> ourProjects = Collections.singletonList(project);
		ProjectPropertiesUtil.setIndexingEnabledAndDisabled(ourProjects, Collections.<IProject>emptySet());
	
		//If a filter for jimple files is present then use that
		Set<IMappingSettings> settings;
		IFile jimpleFilterFile = getJimpleFileFilter(project);
		if (jimpleFilterFile.exists()){
			settings = addJimpleIndexingSettings(jimpleFilterFile);
		}else{
			// TODO: set jar indexing mode to: used only (same as default)
			settings = Collections.<IMappingSettings>emptySet();
		}
		IndexingUtil.indexWithSettings(/*saveIndex*/true, /*indexingSettings*/settings, ourProjects.toArray(new IProject[1]));
	}


	private static final String JIMPLE_FILTER = "types-to-map.RULERfilter";
	
	/**
	 */	
	private static IFile getJimpleFileFilter(IProject project) {
		return project.getFile(JIMPLE_FILTER);
	}

	/**
	 * 
	 * @param jimpleFilterFile 
	 * @return
	 */
	private static Set<IMappingSettings> addJimpleIndexingSettings(IFile jimpleFilterFile) {
		JimpleSettingsJob j = new JimpleSettingsJob("Collect jimple files", jimpleFilterFile);

		j.schedule();
		try {
			j.join();
		} catch (InterruptedException e) {
			Log.error("Unable to collect jimple files", e);
		}
		
		return j.set;
	}
		
	 private static class JimpleSettingsJob extends Job{
			Set<IMappingSettings> set = Collections.emptySet();
			private IFile jimpleFilterFile;
	
			public JimpleSettingsJob(String name, IFile jimpleFilterFile) {
				super(name);
				this.jimpleFilterFile=jimpleFilterFile;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				JimpleIndexingSettings settings = new JimpleTypeFilter().getJimpleIndexingSettings(jimpleFilterFile);
				set = Collections.<IMappingSettings>singleton(settings);
				return Status.OK_STATUS;
			}
		}
}
