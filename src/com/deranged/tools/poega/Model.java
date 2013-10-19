package com.deranged.tools.poega;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class Model {

	private ArrayList<Node> nodes;
	private ArrayList<Group> groups;
	private HashMap<Integer, Integer> hash;
	private HashMap<String, Icon> skilliconshash;  // stores the x,y,w,h positions of the skill icons to cut from spritesheet

	private ArrayList<Build> population;

	private Set<Integer> set; // a set of hashcodes from the builds so that identical builds are analysed over and over
	private Set<Mod> modTotals;

	private int[] size = {1,6,12,12,12};
	private float[] radii = {0, 81.5f, 163, 326, 489};
	private int[] classStartingNodes = {50459,47175,50986,61525,54447,44683};

	private int camX;// = 8333;
	private int camY;// = 5555;

	private int panX=0;
	private int panY=0;

	private int frameWidth=1800;
	private int frameHeight=1000;
	//	private int camX = 0;
	//	private int camY = 0;
	private float scale=0.09f;

	private int hover=0;

	private Image sprites;
	private Image spritesActive;
	private Image ring;
	private Image ringActive;
	private Image notable;
	private Image keystone;
	private Image mastery;
	private Image[] backgroundImages;
	private Image[] classImages;

	private Target target;

	private Build build;
	
	private int startingPopulationSize = 20;
	private int startingKeyNodes = 1;
	

	private Random ran;

	public Model() {
		nodes = new ArrayList<Node>();
		groups = new ArrayList<Group>();
		hash = new HashMap<Integer, Integer>(); // hash for node index to node_id
		population = new ArrayList<Build>();
		set = new HashSet<Integer>();
		modTotals = new HashSet<Mod>();
		skilliconshash = new HashMap<String, Icon>();
		backgroundImages = new Image[4];
		classImages = new Image[6];
		//loadSkillTree(); // now called from within load()
		boolean fromfile = load();
		if (fromfile) {
			calculateDistances();
			save(); 
		}

		camX = (int)(frameWidth/(2*scale));
		camY = (int)(frameHeight/(2*scale));
		sprites=null;
		ring=null;
		try {
			sprites = ImageIO.read(new File("spritesheet.jpg"));
			spritesActive = ImageIO.read(new File("spritesheet_active.jpg"));
			
			mastery = ImageIO.read(new File("spritesheet2.png"));
			ring = ImageIO.read(new File("ring.png"));
			ringActive = ImageIO.read(new File("ring_active.png"));
			notable = ImageIO.read(new File("notable.png"));
			keystone = ImageIO.read(new File("keystone.png"));
			backgroundImages[0] = null;
			backgroundImages[1] = ImageIO.read(new File("PSGroupBackground1.png"));
			backgroundImages[2] = ImageIO.read(new File("PSGroupBackground2.png"));
			backgroundImages[3] = ImageIO.read(new File("PSGroupBackground3.png"));
			classImages[0] = ImageIO.read(new File("centermarauder.png")); // 1
			classImages[1] = ImageIO.read(new File("centerranger.png")); // 2
			classImages[2] = ImageIO.read(new File("centerwitch.png")); // 3
			classImages[3] = ImageIO.read(new File("centerduelist.png")); // 4
			classImages[4] = ImageIO.read(new File("centertemplar.png")); //5
			classImages[5] = ImageIO.read(new File("centershadow.png")); //6

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (nodes.size() > 0) {
			System.out.println("Successfully loaded " + nodes.size()+ " nodes");
		} else {
			System.out.println("<Model> ERROR loading nodes!");
			System.exit(0);
		}

		for (int c=0; c<classStartingNodes.length; c++) {
			nodes.get(hash.get(classStartingNodes[c])).setClassStart(true);
		}
		
		for(int n = 0 ; n < nodes.size(); n++) { // for each node
			for(int m = 0; m < nodes.get(n).getNumberOfMods(); m++) { // for each mod in the node
				
				//System.out.println("adding "+nodes.get(n).getMod(m).getDesc()+" to mod totals");
				boolean found = false;
				Iterator<Mod> iter = modTotals.iterator();
				while (iter.hasNext() && !found) {
					Mod tmp = iter.next();
					if (tmp.getDesc().equals(nodes.get(n).getMod(m).getDesc())) {
						tmp.setValue(tmp.getValue() + nodes.get(n).getMod(m).getValue());
						found=true;
					}
				}
				if (!found) {
					modTotals.add(nodes.get(n).getMod(m).dup());
				}
			}
		}
		
		target = new Target();
		ran = new Random(1);
		
	}

	/*public void init() { // primitive version that loads in 1 build and displays it
		build = new Build(nodes.size(), this, hash);

		// TODO make a Model method called "setClassStartNode" that takes an enum
		//      then it sets the build start node but also disables the other class nodes
		//      so that they can't be used
		build.setStart(nodes.get(hash.get(classStartingNodes[1])));

		build.addKeyNode(nodes.get(hash.get(24383)));
		build.addKeyNode(nodes.get(hash.get(61999))); 
		build.addKeyNode(nodes.get(hash.get(31961))); 
		build.addKeyNode(nodes.get(hash.get(57279))); 
		build.addKeyNode(nodes.get(hash.get(1325))); 
		//build.addKeyNode(nodes.get(hash.get(12809)));
		build.addKeyNode(nodes.get(hash.get(10661)));
		build.addKeyNode(nodes.get(hash.get(54142)));
		build.addKeyNode(nodes.get(hash.get(26557)));
		build.addKeyNode(nodes.get(hash.get(61039)));

//		System.out.println("_Targets_:");
//		for (int t = 0; t < target.getNumberOfMods(); t++) {
//			System.out.println(target.getMod(t));
//		}
		
		build.update();
		build.heuristic(target);
		
		System.out.println("This build's score is "+build.getScore());
	}*/

	public void calculateDistances() {
		System.out.println("Calculating distances from every node to every other node...\n   This might take a while...");
		//		int pathlength = search(1371, 39768).size();
		//		pathlength--;
		//		System.out.println("Path length from 1371 to 39768 is "+pathlength);
		//		nodes.get(hash.get(1371)).setDistance(hash.get(39768), pathlength);
		int pathlength;
		int prevprogress=-1;
		int progress=0;
		for(int i = 0; i < nodes.size(); i++) {
			progress = (int)(100*i / nodes.size());
			if (progress != prevprogress) {
				System.out.println("Progress = "+progress+"%");
				prevprogress = progress;
			}
			if (!nodes.get(i).isMastery()) {
				for(int j = i; j < nodes.size(); j++) {
					//for(int j = i; j < 15; j++) {
					if (!nodes.get(j).isMastery()) {
						if (i==j) {
							nodes.get(i).setDistance(i, 0);
						} else {
							// get distance from i to j
							int id1 = nodes.get(i).getId();
							int id2 = nodes.get(j).getId();
							//System.out.println("Id1:"+id1+" Id2:"+id2);

							ArrayList<Node> path = search(id1, id2, build);
							if (path!=null) {
								pathlength = path.size()-1;
								//System.out.println("Distance from "+nodes.get(i).getId()+" to "+nodes.get(j).getId()+" = "+pathlength);
								// set distance in nodes both ways
								nodes.get(i).setDistance(j, pathlength);
								nodes.get(j).setDistance(i, pathlength);
							} else {
								System.out.println("Path from "+nodes.get(i).getId()+" to "+nodes.get(j).getId()+" was empty :(");
							}
						}
					}
				}
			}
		}
		System.out.println("All distances calculated!");
		save();
	}

	public void init() {
		// create a starting population of builds with random key nodes
		//  and random class
		for(int i = 0 ; i < startingPopulationSize; i++) {
			Build b = new Build(nodes.size(), this, hash);
			b.setStart(nodes.get(hash.get(classStartingNodes[ran.nextInt(6)])));
			for(int k = 0; k < startingKeyNodes; k++) {
				Node node = nodes.get(ran.nextInt(nodes.size()));
				while (node.isMastery()) {
					node = nodes.get(ran.nextInt(nodes.size()));
				}
				b.addKeyNode(node);
			}
			b.update();
			b.heuristic(target);
			set.add(b.hashBits());
			population.add(b);
		}
		
	}
	
	public void newgeneration() {
		// find the highest scoring build
		double bestscore=0;
		for(int i = 1; i < population.size(); i++) {
			double score = population.get(i).getScore();
			System.out.println(population.get(i).getNumberOfKeyNodes()+" "+score);
			if (score>bestscore) {
				bestscore = population.get(i).getScore();
				build = population.get(i);
			}
		}
		
	}
	
	public void generation() {

		// find the highest scoring build
		double bestscore=0;
		System.out.println("Scores:");
		for(int i = 1; i < population.size(); i++) {
			double score = population.get(i).getScore();
			System.out.println(population.get(i).getNumberOfKeyNodes()+" "+score);
			if (score>bestscore) {
				bestscore = population.get(i).getScore();
				build = population.get(i);
			}
		}
		System.out.println("--");

		// choose 2 random builds and compare them.
//		int repeats = (int)Math.floor(population.size());
		int repeats = 20;
		System.out.println("Repeats is set to "+repeats);
		for (int i=0; i < repeats; i++) {
			System.out.println("Add Repeat : "+i);
			Build b1 = population.get(ran.nextInt(population.size()));
			Build b2 = population.get(ran.nextInt(population.size()));
			Build newbuild;
			if (b1.getScore() > b2.getScore()) {
				newbuild = b1.dupe();
			} else {
				newbuild = b2.dupe();
			}
			newbuild.mutate();
			newbuild.update();
			newbuild.heuristic(target);
			population.add(newbuild);
		}

		// remove x build with the lowest score
		double worstscore=1000000;
		int toBeRemoved=-1;
		repeats = population.size() - 20;
		for(int j = 0; j < repeats; j++) {
			toBeRemoved=-1;
			System.out.println("Remove Repeat : "+j);
			for(int i = 0; i < population.size(); i++) {
				if (population.get(i).getScore() < worstscore) {
					worstscore = population.get(i).getScore();
					toBeRemoved = i;
				}
			}
			if (toBeRemoved >= 0) {
				population.remove(toBeRemoved);
				System.out.println("removed "+toBeRemoved+" for being rubbish");
			}
		}
		System.out.println("Population size is now : "+population.size());
	}
	
	/*public void init() {

		int startingPopulationSize = 20;
		int startingKeyNodes = 6;

		Random r = new Random(1);
		for(int i = 0 ; i < startingPopulationSize; i++) {
			Build b = new Build(nodes.size(), this, hash);
			b.setStart(nodes.get(hash.get(classStartingNodes[1])));
			for(int k = 0; k < startingKeyNodes; k++) {
				Node node = nodes.get(r.nextInt(nodes.size()));
				while (node.isMastery()) {
					node = nodes.get(r.nextInt(nodes.size()));
				}
				b.addKeyNode(node);
			}
			b.update();
			b.heuristic(target);
			set.add(b.hashBits());
			population.add(b);
		}

		for (int g  = 0 ; g < 5000; g++) {
			double bestscore=0;
			double[] cum = new double[population.size()];
			cum[0] = population.get(0).getScore();
			for(int i = 1; i < population.size(); i++) {
				//System.out.println(population.get(i).getScore());
				if (population.get(i).getScore()>bestscore) {
					bestscore = population.get(i).getScore();
					build = population.get(i);
				}
				cum[i] = cum[i-1] + population.get(i).getScore();
			}
			System.out.println("g:"+g+" bestscore:"+bestscore);
			//System.out.println("cum:");
			//for(int i = 0; i < cum.length; i++) {
			//	System.out.println(cum[i]);
			//}
			double max = cum[cum.length-1];
			double ran = Math.random() * max;
			//System.out.println("ran = "+ran);
			int index = Arrays.binarySearch(cum, ran);
			index = Math.abs(index) -1 ;
			//System.out.println("index = " + index);
			Build newbuild = population.get(index).dupe();
			newbuild.mutate();
			newbuild.update();
			newbuild.heuristic(target);
			int hash = newbuild.hashBits();
			if (!set.contains(hash)) {
				set.add(newbuild.hashBits());
				if (newbuild.getScore() > bestscore) {
					population.add(newbuild);
				}
			}
		}
		
	}*/
	
	public void addTarget(Mod mod) {
		target.addMod(mod);
		//build.heuristic(target);
		//System.out.println("This build's score is "+build.getScore());
		//System.out.println(target);
	}
	
	public Target getTarget() {
		return target;
	}

	public ArrayList<Node> search(int fromId, int toId, Build build) {
		ArrayList<Node> open = new ArrayList<Node>();
		ArrayList<Node> closed = new ArrayList<Node>();
		ArrayList<Node> neighbours = new ArrayList<Node>();
		ArrayList<Node> path = new ArrayList<Node>();

		Node stop = nodes.get(hash.get(toId));
		Node start = nodes.get(hash.get(fromId));
		if (stop.isMastery() || start.isMastery()) {
			return null;
		} else {
			open.add(start);
			Node current = start;

			boolean found = false;		

			while(!open.isEmpty() && !found) {
				current = open.get(0); // the front of the open list/queue

				if(current.getId()==stop.getId()) {
					found=true;
				}
				open.remove(current);
				closed.add(current);

				neighbours.clear();
				for (int n = 0; n < current.linksCount(); n++) {
					neighbours.add(nodes.get(hash.get(current.getLinks(n))));
				}
				for (int i = 0; i < neighbours.size(); i++) {
					Node neighbour = neighbours.get(i);
					if (!neighbour.isClassStart() || neighbour.getId()==build.getStart().getId()) { // this checks that the node isn't a class start node 
						if(!closed.contains(neighbour)) {
							if (!open.contains(neighbour) ) {
								open.add(neighbour);
								neighbour.setFromIndex(hash.get(current.getId()));
							}
						}
					}
				}
			}

			if(stop!=null && start!=null) {
				current=stop;
				path.add(current);
				while(current!=null && !current.equals(start)) {
					//System.out.println("length "+path.size()+ "  from "+fromId+" to "+toId);
					current = nodes.get(current.getFromIndex());
					path.add(current);
				}
			}
			open.clear();
			closed.clear();

			return path;
		}
	}

	public void printArrayList(ArrayList<Node> list) {
		for(int i = 0; i < list.size(); i++) {
			System.out.print(list.get(i).getId());
			if (i<list.size()-1) System.out.print(", ");
		}
		System.out.println(".");
	}

	public double distance(Node a, Node b) {
		return Math.sqrt(Math.pow(a.getDx()-b.getDx(),2) + Math.pow(a.getDy()-b.getDy(), 2));
	}

	public boolean sanityCheck() {
		// makes sure that all nodes have connections going both ways
		boolean errors=false;
		Node node;
		Node nNode;
		int neighbours=0;
		int id=0;
		boolean found=false;
		for (int n=0; n < nodes.size(); n++) { // for each node
			node = nodes.get(n);
			id = node.getId();                 //   get the id of the node
			if(!node.isMastery()) {            //   that isn't a mastery node (ie has no connections)
				neighbours = node.linksCount();     //     get the number of neighbours
				for (int c=0; c <neighbours; c++) {  // for each neighbouring node
					int nId = node.getLinks(c);      //    get the Id of the neighbour
					int nIndex = hash.get(nId);      //    get the index of the neighbour
					nNode = nodes.get(nIndex);       //    get the neighbouring node
					int nLinks = nNode.linksCount();      //    get the number of neighbours of the neighbouring node
					found=false;
					for (int d=0; d < nLinks; d++) { //    for each of the neighbours of the neighbouring node
						if (nNode.getLinks(d)==id) {
							found=true;
						}
					}
					if (!found) {
						errors = true;
					}
				}
			}
		}
		return errors;
	}

	public void loadSkillTree() {
		System.out.println("Loading skill tree from file...");
		String line="";
		String section="";
		try {
			BufferedReader reader = new BufferedReader(new FileReader("skilltree.txt"));
			line = reader.readLine();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i=0;
		int l=1;
		int index=0;
		int level=0;
		int group=0;
		float x=0f;
		float y=0f;
		int background=0;
		int id=0;
		String c="";
		String icon="";
		//int count=0;
		Pattern pattern1 = Pattern.compile("\"id\":([0-9]+),\"icon\":\"(.+)\",\"ks\":([a-z]+),\"not\":([a-z]+),\"dn\":\"(.+)\",\"m\":([a-z]+),\"spc\":\\[([0-9]*)\\],\"sd\":\\[(.*)\\],.*\"o\":([0-9]+),.*\"oidx\":([0-9]+),.*\"out\":\\[(.*)\\]");
		Pattern xandypatt = Pattern.compile(".x.:(-?[0-9]+\\.*[0-9]*),.y.:(-?[0-9]+\\.*[0-9]*),");
		Pattern grouppatt = Pattern.compile("\"([0-9]+)\"");
		Pattern oopatt = Pattern.compile("(\"[0-9]\":.*)}");
		Pattern npatt = Pattern.compile("\"n\":\\[(.*)\\]");
		while (i+l < line.length()-1) {
			c = line.substring(i, i+l);
			//System.out.println(c+" "+i+" "+l);
			if (level==2 && c.matches(".*groups.*")) {
				//System.out.println("i have found some GROUPS at level "+level);
				section="groups";
			}
			if (level==2 && c.matches(".*main.*")) {
				//System.out.println("i have found some GROUPS at level "+level);
				section="main";
			}
			if (level==3 && c.matches(".*nodes.*")) {
				//System.out.println("i have found some NODES at level "+level);
				section="nodes";
			}
			if(level==3 && section.equals("groups")) {
				Matcher m = grouppatt.matcher(c);
				if (m.matches()) {
					group   = Integer.parseInt(m.group(1));
				}
			}
			if(level==4  && section.equals("groups")) {
				Matcher m = xandypatt.matcher(c);
				if (m.matches()) {
					x = Float.parseFloat(m.group(1));
					y = Float.parseFloat(m.group(2));
				}
			}
			if(level==5 && section.equals("groups")) {
				Matcher m = oopatt.matcher(c);
				if (m.matches()) {
					//System.out.println(m.group(1));
					String[] bgs = m.group(1).split(",");
					background=0;
					for (int oo=0; oo < bgs.length; oo++) {
						String[] t = bgs[oo].split(":");
						t[0] = t[0].replaceAll("\"", "");
						int ti = Integer.parseInt(t[0]);
						//System.out.println(ti);
						if (ti >=1 && ti <= 3) {
							background = ti;
							//System.out.println("background for group "+group+" is "+background);
						}
						// not sure what to do with ti == 0 or ti >= 4 yet... 
					}
				}
			}
			if(level==4  && section.equals("groups")) {
				Matcher m = npatt.matcher(c);
				if (m.matches()) {
					String s = m.group(1);
					String[] sa = s.split(",");
					//groups.add(new Group(x, y, background));
					//System.out.println("creating a group with background = "+background);
					Group g = new Group(x, y, background);
					for (int k = 0; k < sa.length; k++) {
						id = Integer.parseInt(sa[k]);
						//System.out.println("index:"+index);
						//System.out.println("adding node id:"+id);
						nodes.add(new Node(id, x, y, group));
						//System.out.println("adding to hash id:"+id+" index:"+index);
						hash.put(id, index); // hash stores the index for the id of the node
						g.addNodeIds(id);
						index++;
					}
					groups.add(g);
				}
			}

			if(level==4 && section.equals("nodes")) { // 
				//System.out.println(c+" "+level);
				Matcher m = pattern1.matcher(c);
				if (m.matches()) {
					id          = Integer.parseInt(m.group(1));
					icon        = m.group(2);
					boolean ks  = Boolean.parseBoolean(m.group(3));
					boolean not = Boolean.parseBoolean(m.group(4));
					String dn   = m.group(5); // dn
					boolean mas = Boolean.parseBoolean(m.group(6));
					int spc;
					if (m.group(7).equals("")) {
						spc=-1;
					} else {
						spc=Integer.parseInt(m.group(7));
					}
					String sd   = m.group(8); // sd
					int o       = Integer.parseInt(m.group(9)); // o
					int oidx    = Integer.parseInt(m.group(10)); // oidx
					String out  = m.group(11); // out
					//String[] eff = sd.split(",");

					// parse sd. only split at commas that are outside "text"
					ArrayList<String> effAL = new ArrayList<String>();
					//effAL.clear(); //  remove this if it's not necessary
					//StringBuilder sb = new StringBuilder();
					char s;
					int start=0;
					boolean capturing=false;
					for(int a=0;a<sd.length();a++) {
						s = sd.charAt(a);
						//						if (capturing && s == '\"') {
						//							effAL.add(sd.substring(start, a));
						//							capturing=false;
						//						}
						//						if (capturing==false && s == '\"') {
						//							start=a;
						//							capturing=true;
						//						}
						if (s == '\"') {
							if (capturing) {
								effAL.add(sd.substring(start+1, a));
								capturing=false;
							} else {
								start=a;
								capturing=true;
							}
						}

					}
					String[] eff = effAL.toArray(new String[effAL.size()]);

					String[] links = out.split(",");

					Node n = nodes.get(hash.get(id));

					//System.out.println("count "+count+"\tid:"+id);
					//count++;

					n.setSize(o);
					n.setOidx(oidx);
					n.setKeystone(ks);
					n.setNotable(not);
					n.setIcon(icon);
					n.setMastery(mas);
					n.setSpc(spc);
					n.setName(dn);

					double b = 2 * Math.PI * n.getOidx() / size[n.getSize()];
					n.setDx(n.getX()-radii[n.getSize()]*Math.sin(-b));
					n.setDy(n.getY()-radii[n.getSize()]*Math.cos(-b));

					nodes.get(hash.get(id)).setDescription(dn);
					for (int k = 0; k < eff.length; k++) {
						//eff[k] = eff[k].replaceAll("\"", "");
						nodes.get(hash.get(id)).addEffects(eff[k]);
					}
					for (int k = 0; k < links.length; k++) {
						if (!links[k].equals("")) {
							//System.out.println(id +" -> "+Integer.parseInt(links[k]) + " = " + hash.get(id)+" => "+hash.get(Integer.parseInt(links[k])));
							nodes.get(hash.get(id)).addLinks(Integer.parseInt(links[k]));
							nodes.get(hash.get(Integer.parseInt(links[k]))).addLinks(id);
						}
					}
					//System.out.println("Node id:"+id+", dn:"+dn+", sd:"+sd+", o:"+o+", oidx:"+oidx+", out:"+out); 
				}

			}

			if (c.matches(".*\\{$")) {
				//System.out.println("*"+line.substring(i, i+l-1)+" > "+ level); //+" > "+ level
				level+=1;
				i=i+l;
				l=1;
			} else if(c.matches(".*\\}$")){
				//System.out.println(line.substring(i, i+l-1));
				level-=1;
				i=i+l+1;
				l=1;
			} else {
				l+=1;
			}
		}
		System.out.println(nodes.size() + " nodes loaded");
		System.out.println(groups.size() + " groups loaded");
		//		for (int q = 0; q < nodes.size(); q++) {
		//			System.out.println(nodes.get(q));
		//		}
		Pattern skillpatt = Pattern.compile("\"(.+)\":\\{\"x\":([0-9]+),\"y\":([0-9]+),\"w\":([0-9]+),\"h\":([0-9]+)\\}");
		int x1=0;
		int y1=0;
		int w=0;
		int h=0;
		String name="";
		try {
			BufferedReader reader = new BufferedReader(new FileReader("skillicons.txt"));
			while ((line=reader.readLine()) != null) {
				Matcher m = skillpatt.matcher(line);
				if (m.matches()) {
					name = m.group(1);
					x1 = Integer.parseInt(m.group(2));
					y1 = Integer.parseInt(m.group(3));
					w = Integer.parseInt(m.group(4));
					h = Integer.parseInt(m.group(5));
					skilliconshash.put(name, new Icon(x1,y1,w,h));
					//System.out.println("S: "+name +" x:"+x1+" y:"+y1+" w:"+w+" h:"+h);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try{	
			//use buffering
			OutputStream file = new FileOutputStream( "nodes.data" );
			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );
			try{
				output.writeObject(nodes);
			}
			finally{
				output.close();
			}
		}  
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			ex.printStackTrace();
		}
		try{	
			//use buffering
			OutputStream file = new FileOutputStream( "hash.data" );
			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );
			try{
				output.writeObject(hash);
			}
			finally{
				output.close();
			}
		}  
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			ex.printStackTrace();
		}
		try{	
			//use buffering
			OutputStream file = new FileOutputStream( "groups.data" );
			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );
			try{
				output.writeObject(groups);
			}
			finally{
				output.close();
			}
		}  
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			ex.printStackTrace();
		}

		try{	
			//use buffering
			OutputStream file = new FileOutputStream( "shash.data" );
			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );
			try{
				output.writeObject(skilliconshash);
			}
			finally{
				output.close();
			}
		}  
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			ex.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public boolean load() {
		System.out.println("Running load()");
		boolean loading=false;
		try{
			//use buffering
			InputStream file = new FileInputStream( "nodes.data" );
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput input = new ObjectInputStream ( buffer );
			try{
				//deserialize the List
				nodes = (ArrayList<Node>)input.readObject();
			}
			finally{
				input.close();
			}
		}
		catch(FileNotFoundException e) {
			System.out.println("nodes.data not found. Loading skilltree from original data.");
			loadSkillTree();
			return true;
		}
		catch(ClassNotFoundException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
		}
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
		}

		//System.out.println("nodes.data successfully loaded... nodes contains "+nodes.size()+" nodes");
		if(loading==false) {
			try{
				//use buffering
				InputStream file = new FileInputStream( "hash.data" );
				InputStream buffer = new BufferedInputStream( file );
				ObjectInput input = new ObjectInputStream ( buffer );
				try{
					//deserialize the List
					hash = (HashMap<Integer, Integer>)input.readObject();
					//display its data
					//for(String quark: recoveredQuarks){
					//	System.out.println("Recovered Quark: " + quark);
					//}
				}
				finally{
					input.close();
				}
			}
			catch(ClassNotFoundException ex){
				//fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
			}
			catch(IOException ex){
				//fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
			}


			try{
				//use buffering
				InputStream file = new FileInputStream( "groups.data" );
				InputStream buffer = new BufferedInputStream( file );
				ObjectInput input = new ObjectInputStream ( buffer );
				try{
					//deserialize the List
					groups = (ArrayList<Group>)input.readObject();
					//display its data
					//for(String quark: recoveredQuarks){
					//	System.out.println("Recovered Quark: " + quark);
					//}
				}
				finally{
					input.close();
				}
			}
			catch(ClassNotFoundException ex){
				//fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
			}
			catch(IOException ex){
				//fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
			}

			try{
				//use buffering
				InputStream file = new FileInputStream( "shash.data" );
				InputStream buffer = new BufferedInputStream( file );
				ObjectInput input = new ObjectInputStream ( buffer );
				try{
					//deserialize the List
					skilliconshash = (HashMap<String, Icon>)input.readObject();
					//display its data
					//for(String quark: recoveredQuarks){
					//	System.out.println("Recovered Quark: " + quark);
					//}
				}
				finally{
					input.close();
				}
			}
			catch(ClassNotFoundException ex){
				//fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
			}
			catch(IOException ex){
				//fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
			}

		}
		return false;
	}

	public void hover(int x, int y) {
		double d = 0;
		double dist = 1000;
		Node n;
		//Group gr;

		for (int i = 0; i < nodes.size(); i++) {
			n = nodes.get(i);
			d = Math.sqrt(Math.pow(x-n.getDx(), 2) + Math.pow(y-n.getDy(), 2));
			if (d < dist) {
				dist = d;
				hover = i;
			}
		}
		//if(!nodes.get(hover).isMastery()) {
		//build.update(nodes.get(hover).getId());
		//}

		//		for (int i = 0; i < groups.size(); i++) {
		//			gr = groups.get(i);
		//			d = Math.sqrt(Math.pow(x-gr.getX(), 2) + Math.pow(y-gr.getY(), 2));
		//			if (d < dist) {
		//				dist = d;
		//				hover = i;
		//			}
		//		}
		//		System.out.println(groups.get(hover).getX()+" "+groups.get(hover).getY());
	}

	public Node getNode(int i) {
		if (nodes.size()>0) {
			return nodes.get(i);
		} else {
			return null;
		}
	}

	public void setNode(int i, Node node) {
		this.nodes.set(i, node);
	}

	public int getNodesCount() {
		return nodes.size();
	}

	public int getNodeIndexFromId(int id) {
		return hash.get(id);
	}

	public Node getNodeFromId(int id) {
		if (hash.get(id)==null) {
			return null;
		} else {
			return nodes.get(hash.get(id));
		}
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public void setFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}

	public void setFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}

	public int getCamX() {
		return camX;
	}

	public int getCamY() {
		return camY;
	}

	public void setCamX(int camX) {
		this.camX = camX;
	}

	public void setCamY(int camY) {
		this.camY = camY;
	}

	public void addCamX(int dx) {
		this.camX += dx;
	}

	public void addCamY(int dy) {
		this.camY += dy;
	}

	public int getPanX() {
		return panX;
	}

	public int getPanY() {
		return panY;
	}

	public void addPanX(int dx) {
		this.panX += dx;
	}

	public void addPanY(int dy) {
		this.panY += dy;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public void addScale(double d, int x, int y) {
		//float prev = this.scale;
		this.scale += d;
		if (this.scale < 0.05) {
			this.scale = 0.05f;
		}
		if (this.scale > 0.6) {
			this.scale = 0.6f;
		}
		camX = (int)(frameWidth/(2*scale));
		camY = (int)(frameHeight/(2*scale));
		//camX+=panX;
		//camY+=panY;
		// camX and camY are coordinates of the top left of the screen
		// when you zoom you want to move that coordinate


	}

	public int getHover() {
		return hover;
	}

	public void setHover(int hover) {
		this.hover = hover;
	}

	public Image getSprites() {
		return sprites;
	}

	public Image getMastery() {
		return mastery;
	}

	public void setSprites(Image sprites) {
		this.sprites = sprites;
	}

	public Image[] getClassImages() {
		return classImages;
	}

	public Image getSpritesActive() {
		return spritesActive;
	}

	public void setSpritesActive(Image spritesActive) {
		this.spritesActive = spritesActive;
	}

	public void setClassImages(Image[] classImages) {
		this.classImages = classImages;
	}

	public Image getRing() {
		return ring;
	}

	public Image getRingActive() {
		return ringActive;
	}

	public void setRingActive(Image ringActive) {
		this.ringActive = ringActive;
	}

	public Image getNotable() {
		return notable;
	}

	public Image getKeystone() {
		return keystone;
	}

	public void setRing(Image ring) {
		this.ring = ring;
	}

	public int getGroupsSize() {
		return groups.size();
	}

	public Group getGroup(int i) {
		return groups.get(i);
	}

	public Image[] getBackgrounds() {
		return backgroundImages;
	}

	public Icon getSkillIcon(String name) {
		Icon i = skilliconshash.get(name); 
		if (i==null) {
//			System.err.println("ERROR <Model.java> icon for "+name+ " is null");
			return null;
		} else {
			return i;
		}
	}

	public Build getBuild() {
		return build;
	}

	public Set<Mod> getModTotals() {
		return modTotals;
	}

	public void setModTotals(Set<Mod> modTotals) {
		this.modTotals = modTotals;
	}

	public void deleteTarget(int selectedRow) {
		target.deleteMod(selectedRow);		
	}
}

//build.addKeyNode(nodes.get(hash.get(24383)));
//build.addKeyNode(nodes.get(hash.get(61999))); 
//build.addKeyNode(nodes.get(hash.get(31961))); 
//build.addKeyNode(nodes.get(hash.get(57279))); // blood magic
//build.addKeyNode(nodes.get(hash.get(40907))); 
//build.addKeyNode(nodes.get(hash.get(12809))); 
//build.addKeyNode(nodes.get(hash.get(29933))); 
//build.addKeyNode(nodes.get(hash.get(40633))); 
//build.addKeyNode(nodes.get(hash.get(10661)));
//build.addKeyNode(nodes.get(hash.get(56589)));
//build.addKeyNode(nodes.get(hash.get(13714)));
//build.addKeyNode(nodes.get(hash.get(36949)));
//build.addKeyNode(nodes.get(hash.get(30693)));
//build.addKeyNode(nodes.get(hash.get(41472)));
//build.addKeyNode(nodes.get(hash.get(61039)));