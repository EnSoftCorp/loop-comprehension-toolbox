package com.kcsl.loop.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.ensoftcorp.abp.common.soot.ConfigManager;
import com.ensoftcorp.atlas.jimple.core.settings.JimpleIndexingSettings;
import com.ensoftcorp.atlas.jimple.core.settings.PathInclusionSetFilter;
import com.kcsl.loop.log.Log;

public class JimpleTypeFilter {
	
	public JimpleIndexingSettings getJimpleIndexingSettings(IFile file){
		File f = file.getRawLocation().toFile();
		IProject iProject = file.getProject();
		Collection<IPath> types = getTypes(f, iProject);
		JimpleIndexingSettings settings = new JimpleIndexingSettings();
		if (types != null){
			settings.setResourceProxyFilter(new PathInclusionSetFilter(types));
		}
		return settings;
	}
	
	
	
	private Collection<IPath> getTypes(File file, IProject project) {
		List<IPath> list = new LinkedList<IPath>();
		try {
			Scanner scanner = new Scanner(file);
			
			JimpleScanner jimpleScanner = walkJimple(project);
			HashMap<String, IPath> map = jimpleScanner.getMap();
			
			while(scanner.hasNextLine()){
				String type = scanner.nextLine().trim();
				IPath iPath = map.get(type);
				if (iPath != null){
					list.add(iPath);
				}else{
					Log.warning("Could not find type: " + type);
				}
			}
			scanner.close();
		} catch (FileNotFoundException | CoreException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
		return list;
	}
	
	private JimpleScanner walkJimple(IProject project) throws CoreException{
		try{
			ConfigManager.getInstance().startTempConfig();
			
			JimpleScanner jimpleScanner = new JimpleScanner();
			jimpleScanner.walk(project);
			return jimpleScanner;
		}finally{
			ConfigManager.getInstance().endTempConfig();
		}
	}
	
	
	public class JimpleScanner {

		private HashMap<String, IPath> map = new HashMap<String, IPath>();
		public JimpleScanner() {
		}

		public HashMap<String, IPath> getMap() {
			return map;
		}

		public void walk(IProject project) throws CoreException{
			project.accept(new IResourceProxyVisitor() {
				
				@Override
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if( proxy.getType() == IResource.FILE && proxy.getName().endsWith(".jimple")) {//$NON-NLS-1$
						IFile jimpleFile = (IFile) proxy.requestResource();
						try {
							IPath jimplePath = jimpleFile.getFullPath();
							String name = jimplePath.removeFirstSegments(jimplePath.segmentCount()-1).removeFileExtension().toString();	
							IPath path = jimpleFile.getFullPath();
							
							map.put(name, path);
						//} catch (ParserException | LexerException | IOException e) {
						}finally{
						}
					}
					return true;
				}

			},0);
		}
	}
}
