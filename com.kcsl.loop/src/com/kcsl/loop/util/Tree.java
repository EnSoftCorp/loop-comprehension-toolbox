package com.kcsl.loop.util;

import java.util.ArrayList;

public class Tree<T> {

	private RULERNode<T> root;

	public Tree(RULERNode<T> root) {
		this.root = root;
	}

	public boolean isEmpty() {
		return (root == null) ? true : false;
	}

	public RULERNode<T> getRoot() {
		return root;
	}

	public void setRoot(RULERNode<T> root) {
		this.root = root;
	}

	public boolean exists(T key) {
		return find(root, key);
	}

	public int getNumberOfRULERNodes() {
		return getNumberOfDescendants(root) + 1;
	}

	public int getNumberOfDescendants(RULERNode<T> RULERNode) {
		int n = RULERNode.getChildren().size();
		for (RULERNode<T> child : RULERNode.getChildren())
			n += getNumberOfDescendants(child);

		return n;

	}

	public boolean find(RULERNode<T> RULERNode, T keyRULERNode) {
		boolean res = false;
		if (RULERNode.getData().equals(keyRULERNode))
			return true;

		else {
			for (RULERNode<T> child : RULERNode.getChildren())
				if (find(child, keyRULERNode))
					res = true;
		}

		return res;
	}
	
    public RULERNode<T> findNode(RULERNode<T> RULERNode, T keyRULERNode)
    {
    	if(RULERNode == null)
    		return null;
    	if(RULERNode.getData().equals(keyRULERNode))
    		return RULERNode;
    	else
    	{
    		RULERNode<T> cRULERNode = null;
    		for (RULERNode<T> child : RULERNode.getChildren())
    			if ((cRULERNode = findNode(child, keyRULERNode)) != null)
    				return cRULERNode;
    	}
    	return null;         
    } 

	public ArrayList<RULERNode<T>> getPreOrderTraversal() {
		ArrayList<RULERNode<T>> preOrder = new ArrayList<RULERNode<T>>();
		buildPreOrder(root, preOrder);
		return preOrder;
	}

	public ArrayList<RULERNode<T>> getPostOrderTraversal() {
		ArrayList<RULERNode<T>> postOrder = new ArrayList<RULERNode<T>>();
		buildPostOrder(root, postOrder);
		return postOrder;
	}

	private void buildPreOrder(RULERNode<T> RULERNode, ArrayList<RULERNode<T>> preOrder) {
		preOrder.add(RULERNode);
		for (RULERNode<T> child : RULERNode.getChildren()) {
			buildPreOrder(child, preOrder);
		}
	}

	private void buildPostOrder(RULERNode<T> RULERNode, ArrayList<RULERNode<T>> postOrder) {
		for (RULERNode<T> child : RULERNode.getChildren()) {
			buildPostOrder(child, postOrder);
		}
		postOrder.add(RULERNode);
	}

	public ArrayList<RULERNode<T>> getLongestPathFromRootToAnyLeaf() {
		ArrayList<RULERNode<T>> longestPath = null;
		int max = 0;
		for (ArrayList<RULERNode<T>> path : getPathsFromRootToAnyLeaf()) {
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
	
	public ArrayList<ArrayList<RULERNode<T>>> getPathsFromRootToAnyLeaf() {
		ArrayList<ArrayList<RULERNode<T>>> paths = new ArrayList<ArrayList<RULERNode<T>>>();
		ArrayList<RULERNode<T>> currentPath = new ArrayList<RULERNode<T>>();
		getPath(root, currentPath, paths);

		return paths;
	}

	private void getPath(RULERNode<T> RULERNode, ArrayList<RULERNode<T>> currentPath,
			ArrayList<ArrayList<RULERNode<T>>> paths) {
		if (currentPath == null)
			return;

		currentPath.add(RULERNode);

		if (RULERNode.getChildren().size() == 0) {
			// This is a leaf
			paths.add(clone(currentPath));
		}
		for (RULERNode<T> child : RULERNode.getChildren())
			getPath(child, currentPath, paths);

		int index = currentPath.indexOf(RULERNode);
		for (int i = index; i < currentPath.size(); i++)
			currentPath.remove(index);
	}

	private ArrayList<RULERNode<T>> clone(ArrayList<RULERNode<T>> list) {
		ArrayList<RULERNode<T>> newList = new ArrayList<RULERNode<T>>();
		for (RULERNode<T> RULERNode : list)
			newList.add(new RULERNode<T>(RULERNode));

		return newList;
	}
}