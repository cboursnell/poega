package com.deranged.tools.poega;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable{

	private float x;
	private float y;
	
	private int background; // either 1, 2 or 3
	private ArrayList<Integer> nodeIds;
	
	public Group(float x, float y, int background) {
		this.x = x;
		this.y = y;
		this.background = background;
		nodeIds = new ArrayList<Integer>();
	}
	
	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public int getBackground() {
		return background;
	}

	public void setBackground(int i) {
		background = i;
	}
	
	public void addNodeIds(int node) {
		nodeIds.add(node);
	}
}
