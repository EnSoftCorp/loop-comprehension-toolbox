package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ForestNode<T> {
	private T data;
	private List<ForestNode<T>> children;
	private ForestNode<T> parent;

	public ForestNode(T data) {
		this.data = data;
		this.children = new ArrayList<ForestNode<T>>();
	}

	public ForestNode<T> getRoot() {
		ForestNode<T> current = this;
		while(current.getParent() != null) {
			current = current.getParent();
			if(current == null) {
				throw new RuntimeException("Node in the tree can't be null");
			}
		}
		return current;
	}
	

	private static List<ForestNode<? extends Object>> temp = new ArrayList<ForestNode<? extends Object>>();
	private void calculateDescendants() {
		List<ForestNode<T>> desc = getChildren();
		temp.addAll(desc);
		Iterator<ForestNode<T>> iter = desc.iterator();
		while(iter.hasNext()) {
			ForestNode<T> child = iter.next();
			child.calculateDescendants();
		}
	}
 	
	@SuppressWarnings("unchecked")
	public List<ForestNode<T>> getDescendants() {
		temp = new ArrayList<ForestNode<? extends Object>>();
		calculateDescendants();
		List<ForestNode<T>> result = new ArrayList<ForestNode<T>>();
		for(ForestNode<? extends Object> node: temp) {
			result.add((ForestNode<T>)node);
		}
		return result;
	}
	
	public ForestNode(ForestNode<T> node) {
		this.data = (T) node.getData();
		children = new ArrayList<ForestNode<T>>();
	}

	public void addChild(ForestNode<T> child) {
		child.setParent(this);
		children.add(child);
	}

	public void addChildAt(int index, ForestNode<T> child) {
		child.setParent(this);
		this.children.add(index, child);
	}

	public void setChildren(List<ForestNode<T>> children) {
		for (ForestNode<T> child : children)
			child.setParent(this);

		this.children = children;
	}

	public void removeChildren() {
		this.children.clear();
	}

	public ForestNode<T> removeChildAt(int index) {
		return children.remove(index);
	}
	
	public void removeThisIfItsAChild(ForestNode<T> childToBeDeleted)
	{
		List <ForestNode<T>> list = getChildren();
		list.remove(childToBeDeleted);
	}

	public T getData() {
		return this.data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public ForestNode<T> getParent() {
		return this.parent;
	}

	public void setParent(ForestNode<T> parent) {
		this.parent = parent;
	}

	public List<ForestNode<T>> getChildren() {
		return this.children;
	}

	public ForestNode<T> getChildAt(int index) {
		return children.get(index);
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj)
			return false;

		if (obj instanceof ForestNode) {
			if (((ForestNode<?>) obj).getData().equals(this.data))
				return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return this.data.toString();
	}

}