package com.deranged.tools.poega;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Node implements Serializable {

	private int id;
	private int group;
	private String name; // like Vaal Pact, Chaos Inoculation etc

	private float x;
	private float y; // centre of the node group

	private double dx;
	private double dy; // actual position of node

	private int size;
	private int oidx;
	private int spc;

	private int fromIndex;

	private String icon;

	private boolean keystone;
	private boolean notable;
	private boolean mastery;
	private boolean classStart; // false for all except the 6 starting locations

	private String description;
	//private ArrayList<String> effects;
	private ArrayList<Mod> mods;      // parsed string
	private ArrayList<Integer> links; // list of the id of neighbouring nodes

	private ArrayList<Integer> distances; // a list of the distances to other nodes in the tree.

	//TODO add storage of location of graphics

	public Node(int id, float x, float y, int group) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.group = group;
		this.size=0;
		this.oidx=0;
		//effects = new ArrayList<String>();
		mods = new ArrayList<Mod>();
		links = new ArrayList<Integer>();
		distances = new ArrayList<Integer>();
		this.description="";
		this.keystone = false;
		this.notable = false;
	}

	// Pattern grouppatt = Pattern.compile("\"([0-9]+)\"");
	// Matcher m = grouppatt.matcher(effect);
	//	if (m.matches()) {
	//		group   = Integer.parseInt(m.group(1));
	//	}
	public void addEffects(String effect) {
		//this.effects.add(effect);

		Pattern plus = Pattern.compile("\\+([\\.0-9]+)(.*)");         // +10 to Intelligence
		Pattern percent = Pattern.compile("([\\.0-9]+)%(.*)");      // 10% increased joojoo
		Pattern pluspercent = Pattern.compile("\\+([\\.0-9]+)%(.*)");  // +10% resistance to slime

		float value=0;
		String prefix = "";
		String postfix = "";
		String text = "";
		boolean matched=false;
		// +X%
		Matcher m = pluspercent.matcher(effect);
		if (m.matches()) {
			value   = Float.parseFloat(m.group(1));
			text = m.group(2);
			//System.out.println("type 1 - plus-percent");
			prefix = "+";
			postfix = "%";
			matched=true;
		}
		// X%
		if (!matched) {
			m = percent.matcher(effect);
			if (m.matches()) {
				value   = Float.parseFloat(m.group(1));
				text = m.group(2);
				//System.out.println("type 2 - percent");
				postfix = "%";
				matched=true;
			}
		}
		// +X

		if (!matched) {
			m = plus.matcher(effect);
			if (m.matches()) {
				value   = Float.parseFloat(m.group(1));
				text = m.group(2);
				//System.out.println("type 3 - plus");
				prefix = "+";
			}
		}
		if (value==0) {
			text=effect;
		}
		//System.out.println(prefix+value+postfix+" "+text+" == "+effect);
		Mod mod = new Mod(text, name, value, prefix, postfix);
		if (keystone) {
			mod.setKeystoneId(id);
		}
		mods.add(mod);
		
	}
	
	public Mod getMod(int i) {
		if (i >= mods.size()) {
			System.err.println("<Node> accessing out of range mod. size:"+mods.size()+" index:"+i);
			return null;
		} else {
			return mods.get(i);
		}
	}
	
	public int getNumberOfMods() {
		return mods.size();
	}

	public void setDistance(int index, int distance) {
		while (index > distances.size()-1) {
			distances.add(-1);
		}
		distances.set(index, distance);
	}

	public int getDistance(int i) { // how far is it from this node to node i
		if (i > distances.size()-1) {
			return -1;
		} else {
			return distances.get(i);
		}
	}

	public String toString() {
		String s = x + ", " + y + ", " + id + ", " + group;
		return s;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public double getDx() {
		return dx;
	}

	public double getDy() {
		return dy;
	}

	public void setDx(double dx) {
		this.dx = dx;
	}

	public void setDy(double dy) {
		this.dy = dy;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getSize() {
		return size;
	}

	public int getOidx() {
		return oidx;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setOidx(int oidx) {
		this.oidx = oidx;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSpc() {
		return spc;
	}

	public int getGroup() {
		return group;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public String getDescription() {
		return description;
	}

	/*public String getEffects(int i) {
		return effects.get(i);
	}

	public int getEffectsSize() {
		return effects.size();
	}*/

	public void setDescription(String description) {
		this.description = description;
	}


	public int getLinks(int i) {
		return links.get(i);
	}

	public void addLinks(int link) {
		this.links.add(link);
	}

	public boolean isMastery() {
		return mastery;
	}

	public boolean isKeystone() {
		return keystone;
	}

	public boolean isNotable() {
		return notable;
	}

	public boolean isClassStart() {
		return classStart;
	}

	public void setClassStart(boolean classStart) {
		this.classStart = classStart;
	}

	public void setKeystone(boolean keystone) {
		this.keystone = keystone;
	}

	public void setNotable(boolean notable) {
		this.notable = notable;
	}

	public int links() {
		return links.size();
	}

	public void setMastery(boolean mastery) {
		this.mastery = mastery;
	}

	public void setSpc(int spc) {
		this.spc = spc;		
	}

	public int getFromIndex() {
		return fromIndex;
	}

	public void setFromIndex(int fromIndex) {
		this.fromIndex = fromIndex;
	}	

}
