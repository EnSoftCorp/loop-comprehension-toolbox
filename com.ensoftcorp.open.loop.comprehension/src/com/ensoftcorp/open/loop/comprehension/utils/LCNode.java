package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LCNode<T> {
	private T data;
	private List<LCNode<T>> children;
	private LCNode<T> parent;

	public LCNode(T data) {
		this.data = data;
		this.children = new ArrayList<LCNode<T>>();
	}

	public LCNode<T> getRoot() {
		LCNode<T> current = this;
		while(current.getParent() != null) {
			current = current.getParent();
			if(current == null) {
				throw new RuntimeException("Node in the tree can't be null");
			}
		}
		return current;
	}
	

	private static List<LCNode<? extends Object>> temp = new ArrayList<LCNode<? extends Object>>();
	private void calculateDescendants() {
		List<LCNode<T>> desc = getChildren();
		temp.addAll(desc);
		Iterator<LCNode<T>> iter = desc.iterator();
		while(iter.hasNext()) {
			LCNode<T> child = iter.next();
			child.calculateDescendants();
		}
	}
 	
	@SuppressWarnings("unchecked")
	public List<LCNode<T>> getDescendants() {
		temp = new ArrayList<LCNode<? extends Object>>();
		calculateDescendants();
		List<LCNode<T>> result = new ArrayList<LCNode<T>>();
		for(LCNode<? extends Object> node: temp) {
			result.add((LCNode<T>)node);
		}
		return result;
	}
	
	public LCNode(LCNode<T> node) {
		this.data = (T) node.getData();
		children = new ArrayList<LCNode<T>>();
	}

	public void addChild(LCNode<T> child) {
		child.setParent(this);
		children.add(child);
	}

	public void addChildAt(int index, LCNode<T> child) {
		child.setParent(this);
		this.children.add(index, child);
	}

	public void setChildren(List<LCNode<T>> children) {
		for (LCNode<T> child : children)
			child.setParent(this);

		this.children = children;
	}

	public void removeChildren() {
		this.children.clear();
	}

	public LCNode<T> removeChildAt(int index) {
		return children.remove(index);
	}
	
	public void removeThisIfItsAChild(LCNode<T> childToBeDeleted)
	{
		List <LCNode<T>> list = getChildren();
		list.remove(childToBeDeleted);
	}

	public T getData() {
		return this.data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public LCNode<T> getParent() {
		return this.parent;
	}

	public void setParent(LCNode<T> parent) {
		this.parent = parent;
	}

	public List<LCNode<T>> getChildren() {
		return this.children;
	}

	public LCNode<T> getChildAt(int index) {
		return children.get(index);
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj)
			return false;

		if (obj instanceof LCNode) {
			if (((LCNode<?>) obj).getData().equals(this.data))
				return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return this.data.toString();
	}

}