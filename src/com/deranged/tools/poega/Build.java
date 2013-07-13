package com.deranged.tools.poega;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Build {

	private Node start;
	private ArrayList<Node> keyNodes; // not the same as keystones
	private int[] activatedNodes;

	private Model model;
	private HashMap<Integer, Integer> hash;

	private HashMap<String, Mod> summ;
	
	private int life;
	private int gearlife = 357;
	private int maxpoints = 66;
	
	private int level;
	
	private int points; // how many nodes have been activated
	
	private double score;

	public Build(int size, Model model, HashMap<Integer, Integer> hash) {
		keyNodes = new ArrayList<Node>(); // this is the genotype
		this.model = model;
		activatedNodes = new int[size];   // this is the phenotype sort of
		this.hash = hash;
		for (int i = 0; i < size; i++) {
			activatedNodes[i]=0;
		}
		summ = new HashMap<String, Mod>();
		//summ.put("#tomaximumLife", new Mod("lifepc", 0));
		//summ.put("#toStrength", new Mod("str", 0));
		//summ.put("#increasedmaximumLife", new Mod("pluslife", 0));
		level=63;
		points=0;
	}
	
	public void clear() {
		for (int i = 0; i < activatedNodes.length; i++) {
			activatedNodes[i]=0;
		}
		keyNodes.clear();
	}

	public void update() {
		// for each keynode
		ArrayList<Node> path=null;
		int shortestPathLength=1000;
		ArrayList<Node> shortestPath=null;

		// set the build to have all nodes deactivated
		for (int i = 0; i < activatedNodes.length; i++) {
			activatedNodes[i]=0;	
		}
		// activate the class node
		activatedNodes[hash.get(this.start.getId()).intValue()]=1;
		
		//int bestNode=0;
		// for each key node find the shortest path from it to another already activated node
		for (int i = 0; i < keyNodes.size(); i++) {
			shortestPathLength=1000;
			for (int a = 0; a < activatedNodes.length; a++) {
				if(activatedNodes[a]==1) {
					//TODO rewrite the use of search. make it use precalculated distances
					path = model.search(keyNodes.get(i).getId(), model.getNode(a).getId(), this); 
					if (path!=null && path.size() < shortestPathLength) {
						shortestPath = path;
						shortestPathLength = path.size();
						//bestNode = model.getNode(a).getId();
					}
				}
			}
			// then activate all the nodes along the best path
			if (shortestPath!=null) {
				//System.out.println("Adding a path from "+keyNodes.get(i).getId()+" to "+bestNode+" with a path of length "+shortestPathLength);
				for (int p = 0; p < shortestPath.size(); p++) {
					activatedNodes[hash.get(shortestPath.get(p).getId())]=1;
				}
			}
		}
		summary();
		//int baseLife = (int)Math.round(gearlife + 50 + (6*level) + summ.get("#tomaximumLife").getValue() + 0.5*summ.get("#toStrength").getValue());
		//System.out.println("baselife   " + baseLife);
		//life = (int)Math.round((gearlife + 50 + (6 * level) + summ.get("#tomaximumLife").getValue() + 0.5*summ.get("#toStrength").getValue()) * (1+(summ.get("#increasedmaximumLife").getValue()/100)));
		//double multiplier =  (1+(summ.get("#increasedmaximumLife").getValue()/100));
		//System.out.println("multiplier " + multiplier);
		//life = (int)Math.round(baseLife * multiplier);
		//if (summ.containsKey("MaximumLifebecomes1,ImmunetoChaosDamage")) {
			//life=1;
		//}
		// add things like eldritch battery here
		
		//System.out.println("Total life = "+ life);
	}
	
	public void heuristic(Target target) { // TODO possibly change it so that it is distance from nearest build keynode
		// TODO add a scoring system that punishes having too many points. target should contain a number points to spend
		//      punish builds for going over that
		//System.out.println("heuristic:");
		Mod targetmod;
		String key;
		score=0.05; // base score so that roulette wheel selection is more random
		for(int i = 0 ; i < target.getNumberOfMods(); i++) {        // for each mod in the target 
			                                                        //compare to what's in the summary and see how well we did
			targetmod = target.getMod(i);
			
			if (targetmod.getValue()==0) { // unique keystone
				// check if the keystone is already in the summ
				if (summ.containsKey(targetmod.getDesc())) {
					score += 2.0;
				} else {				// if not then
					// find the distance between the target keystone and the nearest node in the build
					int nodeIndex = -1;
					if (targetmod.getKeystoneId() > 0) {
						nodeIndex = model.getNodeIndexFromId(targetmod.getKeystoneId());
					} else {
						// find the node that matches the description in the target mod
						for(int n = 0; n < model.getNodesCount(); n++) {
							Node node = model.getNode(n);
							for(int m = 0; m < node.getNumberOfMods(); m++) {
								if (node.getMod(m).getDesc().equals(targetmod.getDesc())) {
									nodeIndex = n;
								}
							}
						}
					}

					float bestdistance=2000.0f;
					if(nodeIndex>=0) {
//						for (int a=0; a < activatedNodes.length; a++) {
//							if (activatedNodes[a]==1) { // TODO instead of activated nodes use build keynodes ?
//								if (model.getNode(nodeIndex).getDistance(a) < bestdistance) {
//									bestdistance = model.getNode(nodeIndex).getDistance(a);
//								}
//							}
						//						}
						for (int a=0; a < keyNodes.size(); a++) {
							if (model.getNode(nodeIndex).getDistance(hash.get(keyNodes.get(a).getId())) < bestdistance) {
								bestdistance = model.getNode(nodeIndex).getDistance(a);
							}
						}
					}
					//System.out.println("Closest node is "+bestdistance+" away from "+targetmod.getDesc());

					//score += ((28 - bestdistance) / 28) * target.getWeight(i);
					score += ((28 - bestdistance) / 28);
				}
			} else { // normal node with values
				// check if the key is in the summ
				// score += summ.get(key).getValue() / targetmod.getValue();
				if (summ.containsKey(targetmod.getDesc())) {
					//System.out.println("Adding score of "+(summ.get(targetmod.getDesc()).getValue() +"/"+ targetmod.getValue())+ " for "+targetmod.getDesc());
					score += summ.get(targetmod.getDesc()).getValue() / targetmod.getValue() * target.getWeight(i);
				}
				// TODO add some sort of distance measure for the rarer non-keystone mods like shock chance
			}
			

//			Iterator<String> iter = summ.keySet().iterator();
//			while(iter.hasNext()) { // iterate over mods in build
//				
//				key = iter.next();
//				//System.out.println(key);
//				String a = summ.get(key).getDesc();
//				String b = targetmod.getDesc();
//				if (a.equals(b)) {
//					System.out.print("target met:" + a +" = "+b);
//					System.out.println(summ.get(key).getValue() +" "+ targetmod.getValue());
//					score += summ.get(key).getValue() / targetmod.getValue();
//				}
//			}
		}
		
		if(points > maxpoints) {
			double p = points + 0.0;
			score *= ( 1 - Math.pow(((p-maxpoints)/1284), 0.2) );
			//System.out.println("Multiplying points by " + ( 1 - Math.pow(((p-maxpoints)/1284), 0.2) ) + " for too many points " + points);
		}
//		if(points < maxpoints) {
//			score *= ( 1 - Math.pow(((maxpoints-points)/1284), 0.5) );
//		}
	}

	// 
	public void summary() {
		summ.clear();
		Node node;
		String key="";
		points=-1;
		// for each activated node in the build
		for (int i = 0; i < activatedNodes.length; i++) {
			if (activatedNodes[i]==1) { 
				points++;                                     // count up the number of skill points used
				node = model.getNode(i);
				for(int m = 0 ; m < node.getNumberOfMods(); m++) {
					Mod mod = node.getMod(m).dup();
					//System.out.println(mod.toString());
					String desc = mod.getDesc();
					if (summ.containsKey(desc)) {
						Mod tmp = summ.get(desc).dup();
						summ.get(desc).setValue(tmp.getValue() + mod.getValue());
					} else {
						summ.put(desc, mod);
					}
				}
			}
		}
		/*System.out.println("\n_Summary_\nThis build uses "+points+" points");
		Iterator<String> iter = summ.keySet().iterator();
		while(iter.hasNext()) {
			key = iter.next();
			//System.out.println(key);
			//System.out.println(summ.get(key).toStringWithoutHash());
			//System.out.println(summ.get(key).printElements());
			System.out.println(summ.get(key));
		}*/
	}
	
	// HASH FUNCTION
	
	public int hashBits() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < activatedNodes.length; i++) {
			//sb.append(activatedNodes[i]);
			if (activatedNodes[i]==0) {
				sb.append("0");
			} else {
				sb.append("1");
			}
		}
		return sb.toString().hashCode();
	}
	
	// MUTATION METHODS
	// these methods should only be run on new Builds and not ones that have been scored already
	
	public void mutate() { // TODO add a moveMutate that moves a keynode to a random location
		double r = Math.random();
		if(r < 0.2) {
			neighbourMutate();
		} else if(r < 0.4) {
			addMutate();
		} else if(r < 0.6) {
			deleteMutate();
		} else if(r < 0.8) {
			swapMutate();
		} else {
			shuffleMutate();
		}
	}
	
	public void neighbourMutate() { // move a node to one of its neighbours
		Random ran = new Random();
		//System.out.println("<Build> keynodes.size() = " + keyNodes.size());
		int r = ran.nextInt(keyNodes.size());
		Node node = keyNodes.get(r);
		int neighbour = node.getLinks(ran.nextInt(node.links()));
		node = model.getNodeFromId(neighbour);
		keyNodes.set(r, node);
	}
	
	public void addMutate() { // add a random node (that isn't already there) to the list in a random place
		Random ran = new Random();
		int r = ran.nextInt(model.getNodesCount());
		Node node = model.getNode(r);
		//System.out.println("<Build> keynodes.size() = " + keyNodes.size());
		int p = ran.nextInt(keyNodes.size());
		keyNodes.add(p, node);
	}
	
	public void deleteMutate() {// delete a random node from the list
		if (keyNodes.size()>=2) {
			Random ran = new Random();
			keyNodes.remove(ran.nextInt(keyNodes.size()));
		} else {
			addMutate();
		}
	}
	
	public void swapMutate() { // swap the positions of two nodes in the list
		Random ran = new Random();
		int r1 = ran.nextInt(keyNodes.size());
		int r2=r1;
		while (r2==r1) {
			r2 = ran.nextInt(keyNodes.size());
		}
		//System.out.println("swap nodes at positions "+r1+" & "+r2);
		Node tmp = keyNodes.get(r1);
		keyNodes.set(r1, keyNodes.get(r2));
		keyNodes.set(r2, tmp);
	}
	
	public void shuffleMutate() { // shuffle the order of all the nodes in the list
		Collections.shuffle(keyNodes);
	}

	public Node getKeyNode(int i) {
		return keyNodes.get(i);
	}

	public int getNumberOfKeyNodes() {
		return keyNodes.size();
	}

	public void addKeyNode(Node node) {
		keyNodes.add(node);
	}

	public void insertKeyNode(Node node, int i) {
		keyNodes.add(i, node);
	}

	public void setStart(Node node) {
		this.start = node;
		activatedNodes[hash.get(this.start.getId()).intValue()]=1;
	}

	public Node getStart() {
		return this.start;
	}

	public void activateNode(int i) {
		activatedNodes[i]=1;
	}

	public void deactivateNode(int i) {
		activatedNodes[i]=0;
	}

	public boolean isNodeActivated(Node n) {
		if (activatedNodes[hash.get(n.getId()).intValue()]==1) {
			return true;
		} else {
			return false;
		}
	}

	public int getLife() {
		return life;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	public Build dupe() {
		Build b = new Build(activatedNodes.length, model, hash);
		for(int i = 0; i < keyNodes.size(); i++) {
			b.addKeyNode(this.keyNodes.get(i));
		}
		b.setStart(start);
		return b;
	}

}
