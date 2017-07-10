package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.ArrayList;

public class Tree<T> {

	private LCNode<T> root;

	public Tree(LCNode<T> root) {
		this.root = root;
	}

	public boolean isEmpty() {
		return (root == null) ? true : false;
	}

	public LCNode<T> getRoot() {
		return root;
	}

	public void setRoot(LCNode<T> root) {
		this.root = root;
	}

	public boolean exists(T key) {
		return find(root, key);
	}

	public int getNumberOfLCNodes() {
		return getNumberOfDescendants(root) + 1;
	}

	public int getNumberOfDescendants(LCNode<T> LCNode) {
		int n = LCNode.getChildren().size();
		for (LCNode<T> child : LCNode.getChildren())
			n += getNumberOfDescendants(child);

		return n;

	}

	public boolean find(LCNode<T> LCNode, T keyLCNode) {
		boolean res = false;
		if (LCNode.getData().equals(keyLCNode))
			return true;

		else {
			for (LCNode<T> child : LCNode.getChildren())
				if (find(child, keyLCNode))
					res = true;
		}

		return res;
	}
	
    public LCNode<T> findNode(LCNode<T> LCNode, T keyLCNode)
    {
    	if(LCNode == null)
    		return null;
    	if(LCNode.getData().equals(keyLCNode))
    		return LCNode;
    	else
    	{
    		LCNode<T> cLCNode = null;
    		for (LCNode<T> child : LCNode.getChildren())
    			if ((cLCNode = findNode(child, keyLCNode)) != null)
    				return cLCNode;
    	}
    	return null;         
    } 

	public ArrayList<LCNode<T>> getPreOrderTraversal() {
		ArrayList<LCNode<T>> preOrder = new ArrayList<LCNode<T>>();
		buildPreOrder(root, preOrder);
		return preOrder;
	}

	public ArrayList<LCNode<T>> getPostOrderTraversal() {
		ArrayList<LCNode<T>> postOrder = new ArrayList<LCNode<T>>();
		buildPostOrder(root, postOrder);
		return postOrder;
	}

	private void buildPreOrder(LCNode<T> LCNode, ArrayList<LCNode<T>> preOrder) {
		preOrder.add(LCNode);
		for (LCNode<T> child : LCNode.getChildren()) {
			buildPreOrder(child, preOrder);
		}
	}

	private void buildPostOrder(LCNode<T> LCNode, ArrayList<LCNode<T>> postOrder) {
		for (LCNode<T> child : LCNode.getChildren()) {
			buildPostOrder(child, postOrder);
		}
		postOrder.add(LCNode);
	}

	public ArrayList<LCNode<T>> getLongestPathFromRootToAnyLeaf() {
		ArrayList<LCNode<T>> longestPath = null;
		int max = 0;
		for (ArrayList<LCNode<T>> path : getPathsFromRootToAnyLeaf()) {
			if (path.size() > max) {
				max = path.size();
				longestPath = path;
			}
		}
		return longestPath;
	}

	public int getMaxDepth()
	{
		return getLongestPathFromRootToAnyLeaf().size();
	}
	
	public ArrayList<ArrayList<LCNode<T>>> getPathsFromRootToAnyLeaf() {
		ArrayList<ArrayList<LCNode<T>>> paths = new ArrayList<ArrayList<LCNode<T>>>();
		ArrayList<LCNode<T>> currentPath = new ArrayList<LCNode<T>>();
		getPath(root, currentPath, paths);

		return paths;
	}

	private void getPath(LCNode<T> LCNode, ArrayList<LCNode<T>> currentPath,
			ArrayList<ArrayList<LCNode<T>>> paths) {
		if (currentPath == null)
			return;

		currentPath.add(LCNode);

		if (LCNode.getChildren().size() == 0) {
			// This is a leaf
			paths.add(clone(currentPath));
		}
		for (LCNode<T> child : LCNode.getChildren())
			getPath(child, currentPath, paths);

		int index = currentPath.indexOf(LCNode);
		for (int i = index; i < currentPath.size(); i++)
			currentPath.remove(index);
	}

	private ArrayList<LCNode<T>> clone(ArrayList<LCNode<T>> list) {
		ArrayList<LCNode<T>> newList = new ArrayList<LCNode<T>>();
		for (LCNode<T> LCNode : list)
			newList.add(new LCNode<T>(LCNode));

		return newList;
	}
}