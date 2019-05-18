---
layout: page
title: Install
permalink: /install/
---

Installing the Loop Comprehension Toolbox Eclipse plugin is easy.  It is recommended to install the plugin from the provided update site, but it is also possible to install from source.

### Installing from Update Site
Follow the steps below to install the Loop Comprehension Toolbox plugin from the Eclipse update site.

1. Start Eclipse, then select `Help` &gt; `Install New Software`.
2. Click `Add`, in the top-right corner.
3. In the `Add Repository` dialog that appears, enter &quot;Atlas Toolboxes&quot; for the `Name` and &quot;[https://ensoftcorp.github.io/toolbox-repository/](https://ensoftcorp.github.io/toolbox-repository/)&quot; for the `Location`.
4. In the `Available Software` dialog, select the checkbox next to "Loop Comprehension Toolbox" and click `Next` followed by `OK`.
5. In the next window, you'll see a list of the tools to be downloaded. Click `Next`.
6. Read and accept the license agreements, then click `Finish`. If you get a security warning saying that the authenticity or validity of the software can't be established, click `OK`.
7. When the installation completes, restart Eclipse.

Note: The toolbox is implemented using [Atlas](http://www.ensoftcorp.com/atlas/). To use it, you'll need Atlas license. Free Atlas license for academic purposes can be obtained [here](http://www.ensoftcorp.com/atlas/academic-license/).

## Installing from Source
If you want to install from source for bleeding edge changes, first grab a copy of the [source](https://github.com/EnSoftCorp/loop-comprehension-toolbox) repository. In the Eclipse workspace, import the Eclipse plugin project located in the source repository.  Right click on the project and select `Export`.  Select `Plug-in Development` &gt; `Deployable plug-ins and fragments`.  Select the `Install into host. Repository:` radio box and click `Finish`.  Press `OK` for the notice about unsigned software.  Once Eclipse restarts the plugin will be installed and it is advisable to close or remove the `com.kcsl.loop.*` projects from the workspace.

Note: If you're installing from source, make sure to either install or grab source for all dependencies - Atlas, Java Commons Toolbox, Jimple Commons Toolbox, PCG Toolbox, JGraphT.

## Changelog

### 3.3.8
- New Loop Catalog View with more Features, Filter View is now obsolete
- Performance Improvements

### 3.1.0
- Initial Release
