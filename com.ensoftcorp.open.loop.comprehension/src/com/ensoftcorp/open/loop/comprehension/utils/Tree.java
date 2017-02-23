package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.ArrayList;

public class Tree<T> {

	private ForestNode<T> root;

	public Tree(ForestNode<T> root) {
		this.root = root;
	}

	public boolean isEmpty() {
		return (root == null) ? true : false;
	}

	public ForestNode<T> getRoot() {
		return root;
	}

	public void setRoot(ForestNode<T> root) {
		this.root = root;
	}

	public boolean exists(T key) {
		return find(root, key);
	}

	public int getNumberOfForestNodes() {
		return getNumberOfDescendants(root) + 1;
	}

	public int getNumberOfDescendants(ForestNode<T> ForestNode) {
		int n = ForestNode.getChildren().size();
		for (ForestNode<T> child : ForestNode.getChildren())
			n += getNumberOfDescendants(child);

		return n;

	}

	public boolean find(ForestNode<T> ForestNode, T keyForestNode) {
		boolean res = false;
		if (ForestNode.getData().equals(keyForestNode))
			return true;

		else {
			for (ForestNode<T> child : ForestNode.getChildren())
				if (find(child, keyForestNode))
					res = true;
		}

		return res;
	}
	
    public ForestNode<T> findNode(ForestNode<T> ForestNode, T keyForestNode)
    {
    	if(ForestNode == null)
    		return null;
    	if(ForestNode.getData().equals(keyForestNode))
    		return ForestNode;
    	else
    	{
    		ForestNode<T> cForestNode = null;
    		for (ForestNode<T> child : ForestNode.getChildren())
    			if ((cForestNode = findNode(child, keyForestNode)) != null)
    				return cForestNode;
    	}
    	return null;         
    } 

	public ArrayList<ForestNode<T>> getPreOrderTraversal() {
		ArrayList<ForestNode<T>> preOrder = new ArrayList<ForestNode<T>>();
		buildPreOrder(root, preOrder);
		return preOrder;
	}

	public ArrayList<ForestNode<T>> getPostOrderTraversal() {
		ArrayList<ForestNode<T>> postOrder = new ArrayList<ForestNode<T>>();
		buildPostOrder(root, postOrder);
		return postOrder;
	}

	private void buildPreOrder(ForestNode<T> ForestNode, ArrayList<ForestNode<T>> preOrder) {
		preOrder.add(ForestNode);
		for (ForestNode<T> child : ForestNode.getChildren()) {
			buildPreOrder(child, preOrder);
		}
	}

	private void buildPostOrder(ForestNode<T> ForestNode, ArrayList<ForestNode<T>> postOrder) {
		for (ForestNode<T> child : ForestNode.getChildren()) {
			buildPostOrder(child, postOrder);
		}
		postOrder.add(ForestNode);
	}

	public ArrayList<ForestNode<T>> getLongestPathFromRootToAnyLeaf() {
		ArrayList<ForestNode<T>> longestPath = null;
		int max = 0;
		for (ArrayList<ForestNode<T>> path : getPathsFromRootToAnyLeaf()) {
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
	
	public ArrayList<ArrayList<ForestNode<T>>> getPathsFromRootToAnyLeaf() {
		ArrayList<ArrayList<ForestNode<T>>> paths = new ArrayList<ArrayList<ForestNode<T>>>();
		ArrayList<ForestNode<T>> currentPath = new ArrayList<ForestNode<T>>();
		getPath(root, currentPath, paths);

		return paths;
	}

	private void getPath(ForestNode<T> ForestNode, ArrayList<ForestNode<T>> currentPath,
			ArrayList<ArrayList<ForestNode<T>>> paths) {
		if (currentPath == null)
			return;

		currentPath.add(ForestNode);

		if (ForestNode.getChildren().size() == 0) {
			// This is a leaf
			paths.add(clone(currentPath));
		}
		for (ForestNode<T> child : ForestNode.getChildren())
			getPath(child, currentPath, paths);

		int index = currentPath.indexOf(ForestNode);
		for (int i = index; i < currentPath.size(); i++)
			currentPath.remove(index);
	}

	private ArrayList<ForestNode<T>> clone(ArrayList<ForestNode<T>> list) {
		ArrayList<ForestNode<T>> newList = new ArrayList<ForestNode<T>>();
		for (ForestNode<T> ForestNode : list)
			newList.add(new ForestNode<T>(ForestNode));

		return newList;
	}
}