package com.kcsl.loop.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RULERNode<T> {
	private T data;
	private List<RULERNode<T>> children;
	private RULERNode<T> parent;

	public RULERNode(T data) {
		this.data = data;
		this.children = new ArrayList<RULERNode<T>>();
	}

	public RULERNode<T> getRoot() {
		RULERNode<T> current = this;
		while(current.getParent() != null) {
			current = current.getParent();
			if(current == null) {
				throw new RuntimeException("Node in the tree can't be null");
			}
		}
		return current;
	}
	

	private static List<RULERNode<? extends Object>> temp = new ArrayList<RULERNode<? extends Object>>();
	private void calculateDescendants() {
		List<RULERNode<T>> desc = getChildren();
		temp.addAll(desc);
		Iterator<RULERNode<T>> iter = desc.iterator();
		while(iter.hasNext()) {
			RULERNode<T> child = iter.next();
			child.calculateDescendants();
		}
	}
 	
	@SuppressWarnings("unchecked")
	public List<RULERNode<T>> getDescendants() {
		temp = new ArrayList<RULERNode<? extends Object>>();
		calculateDescendants();
		List<RULERNode<T>> result = new ArrayList<RULERNode<T>>();
		for(RULERNode<? extends Object> node: temp) {
			result.add((RULERNode<T>)node);
		}
		return result;
	}
	
	public RULERNode(RULERNode<T> node) {
		this.data = (T) node.getData();
		children = new ArrayList<RULERNode<T>>();
	}

	public void addChild(RULERNode<T> child) {
		child.setParent(this);
		children.add(child);
	}

	public void addChildAt(int index, RULERNode<T> child) {
		child.setParent(this);
		this.children.add(index, child);
	}

	public void setChildren(List<RULERNode<T>> children) {
		for (RULERNode<T> child : children)
			child.setParent(this);

		this.children = children;
	}

	public void removeChildren() {
		this.children.clear();
	}

	public RULERNode<T> removeChildAt(int index) {
		return children.remove(index);
	}
	
	public void removeThisIfItsAChild(RULERNode<T> childToBeDeleted)
	{
		List <RULERNode<T>> list = getChildren();
		list.remove(childToBeDeleted);
	}

	public T getData() {
		return this.data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public RULERNode<T> getParent() {
		return this.parent;
	}

	public void setParent(RULERNode<T> parent) {
		this.parent = parent;
	}

	public List<RULERNode<T>> getChildren() {
		return this.children;
	}

	public RULERNode<T> getChildAt(int index) {
		return children.get(index);
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj)
			return false;

		if (obj instanceof RULERNode) {
			if (((RULERNode<?>) obj).getData().equals(this.data))
				return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return this.data.toString();
	}

}