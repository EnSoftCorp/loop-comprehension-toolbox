package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.ArrayList;
import java.util.List;

public class Forest<T> {

	private List<Tree<T>> trees;

	public Forest() {
		this.trees = new ArrayList<Tree<T>>();
	}
	
	public Forest(List<Tree<T>> trees) {
		this.trees = trees;
	}

	public boolean isEmpty() {
		return (trees == null || trees.isEmpty()) ? true : false;
	}

	public List<Tree<T>> getTrees() {
		return trees;
	}

	public void setTrees(List<Tree<T>> trees) {
		this.trees = trees;
	}
	
	public void addTree(Tree<T> tree) {
		if(trees == null) {
			trees = new ArrayList<Tree<T>>();
		}
		trees.add(tree);
	}
	
	public Tree<T> findTree(String id) {
		for(Tree<T> tree : trees) {
			@SuppressWarnings("unchecked")
			LCNode<T> node = tree.findNode(tree.getRoot(), (T)id);
			if(node != null) {
				return tree;
			}
		}
		return null;
	}
}