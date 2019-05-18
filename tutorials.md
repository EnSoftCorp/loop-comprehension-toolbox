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

## Loop Catalog and Loop Filtering Framework

The loop comprehension toolbox provides a framework to enable the selection of loops likely to cause ACVs. This framework is built into Loop Catalog View as of version 3.3.8 (Filter View introduced in 3.1.0 is now obsolete). The filters correspond to Loop Patterns of the form of triple (Monotonicity, Loop Control Variable Type, Loop Control Variable Dependency), Reachable Loops (from app main methods or web handlers) and API usage in the loop body. Ensure that you've indexed the application using Atlas before you use this view.

First, navigate to `Window` &gt; `Show View` &gt; `Other...` &gt; `Loop Catalog View`. This will open an empty spreadsheet-like window with controls at the bottom. The spreadsheet corresponds to the Loop Catalog, a catalog of the loops in the indexed app and their characterizations. These characterizations form the filter criteria to filter loops. Click `All` to load information about all the loops in the given app in the Loop Catalog View. The empty spreadsheet is now filled with information. Each row in the spreadsheet corresponds to a loop and each column in the spreadsheet corresponds to a characterization of the loop. Clicking on the column header sorts the catalog according to the column values. For example, clicking on Monotonicity column will sort loops according to their Monotonicity (true or false). Clicking it again reverses the sorting order. Clicking on a cell enables selection of the entire row, which corresponds to a loop. If you wish to discard certain loops from the analysis, select the loops to be discarded and click `Delete`. This will delete the information about these loops from the loop catalog view. This information is still in the database and can be loaded back into the view. Conversely, if you wish to retain certain loops, select those loops and click `Retain`.

A detailed walkthrough of how to detect an ACV using Loop Catalog View is demonstrated in the following video

<iframe width="560" height="315" src="https://www.youtube.com/embed/LKk6TR_u8ys" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
