---
layout: page
title: Tutorials
permalink: /tutorials/
---

If you haven't already, [install](/loop-comprehension-toolbox/install) the Loop Comprehension Toolbox plugin into Eclipse.

The loop comprehension toolbox provides interactive tools to reason about loops. It supports analysis of Java source code and Java Byte Code (Jimple). It is implemented as a eclipse plugin using Atlas, a graphical program analysis platform. Atlas facilitates creation of graphical abstractions to analyze programs and thus before you could use the toolbox, you will need to 'index' your application into Atlas. The steps to do that are documented [here](http://ensoftatlas.com/wiki/Indexing_Workspace#Step_1.29_Create.2FImport_Projects_to_Index).

## Smart View Interactions

The loop comprehension toolbox provides two Atlas Smart Views to reason about loops, their termination and operations in loops' body. These Smart Views implement following abstractions
1) Termination Dependence Graph (TDG) - A data flow abstraction with respect to statements related to termination of a loop.
2) Loop Projected Control Flow Graph (LPCG) - A control flow abstraction with respect to inter-procedural interactions of a loop in addition to terminating conditions of the loop.

First, navigate to `Atlas` &gt; `Open Atlas Smart View`.  In the Smart View selection window select either the `Termination Dependence Graph (TDG)` or `Loop Projected Control Flow Graph (LPCG)`. In the source editor, select a program statement containing a loop header and the Smart View will automatically update with the resulting TDG or LPCG for the loop.

## Loop Catalog Generation

First, navigate to `Atlas` &gt; `Open Atlas Shell`. In Atlas Shell, run the following command (replace Output File Path with path to the file in which you want to save loop catalog. The catalog will be saved as a .csv file.)

LoopCatalog.run(new File(<Output File Path>))

## Loop Filtering Framework

The loop comprehension toolbox provides a framework to enable the selection of loops likely to cause ACVs. We provide 26 filters corresponding to Loop Patterns of the form of triple (Monotonicity, Loop Control Variable Type, Loop Control Variable Dependency), Reachable Loops (from app main methods or web handlers) and Differential Branch.

First, navigate to `Window` &gt; `Show View` &gt; `Other...` &gt; `Filter View`. Filter view is divided into two panes, 1) displays results of filtering, 2) displays various filters applicable. You may see additional filters related to generic analysis which come from other toolboxes for program analysis.

In the right hand pane, navigate to `Options` &gt; `Load Default Rootsets`. This will load the following program artifacts as two different sets - 1) The entire `Universe` which represents every program artifact in your app. 2) All loop headers in your app.
Click on the rootset you want to filter and on the right pane you will list of applicable filters. Select the filter you want to apply and it may display some options based on what kind of filter it is. E.g. If it is a pattern filter, it will show `Exclude Matches` as it is a boolean property and selecting this option will give you all loops that don't match with the pattern. 
