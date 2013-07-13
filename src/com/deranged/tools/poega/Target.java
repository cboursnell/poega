package com.deranged.tools.poega;

import java.util.ArrayList;
import java.util.Iterator;

public class Target {

	private ArrayList<Mod> targetMods;
	private ArrayList<Float> weights;
	
	public Target() {
		targetMods = new ArrayList<Mod>();
		weights = new ArrayList<Float>();
	}
	
	public void addMod(Mod mod) {
		targetMods.add(mod);
		weights.add(1.0f);
	}
	
	public Mod getMod(int i) {
		return targetMods.get(i);
	}
	
	public int getNumberOfMods() {
		return targetMods.size();
	}
	
	public Iterator<Mod> getIterator() {
		return targetMods.iterator();
	}
	
	public float getWeight(int i) {
		return weights.get(i);
	}
	
	public void setWeight(int i, float w) {
		weights.set(i, w);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int count = targetMods.size();
		for(int i = 0; i < count; i++) {
			sb.append(targetMods.get(i));
			sb.append("\t");
			sb.append(weights.get(i));
			sb.append("\n");
		}
		return sb.toString();
	}

	public void deleteMod(int selectedRow) {
		targetMods.remove(selectedRow);
		weights.remove(selectedRow);
	}

	public void setWeight(int row, Object valueAt) {
		weights.set(row, Float.parseFloat((String)valueAt));
	}
}
