package com.deranged.tools.poega;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

public class ViewPanel extends JPanel {

	private Model model;

	//$number = Array[1,6,12,12,12]
	//$radii = Array[0,81.5,163,326,489]
	private int[] size = {1,6,12,12,12};
	private float[] radii = {0, 81.5f, 163, 326, 489};

	public ViewPanel(Model model) {
		super();
		this.model = model;
	}

	public void paint(Graphics g) {
		g.setColor(new Color(27,21,17));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		float scale = model.getScale();
		Node n;
		Group gr;
		Build build=model.getBuild();
		Image sprites = model.getSprites();
		Image spritesActive = model.getSpritesActive();
		if (spritesActive == null) {
			System.err.println("ERROR - spritesActive not loaded correctly");
			System.exit(0);
		}
		Image mastery = model.getMastery();
		Image ring = model.getRing();
		Image ringActive = model.getRingActive();
		Image notable = model.getNotable();
		Image keystone = model.getKeystone();
		Image[] backgrounds = model.getBackgrounds();
		Image[] classes = model.getClassImages();
		int sx=81;
		int sy=0;
		int w=27;
		int h=27;
		int dx=0;
		int dy=0;
		double ringsize=40*2.1;
		double ringactivesize=57*2.1;
		double notablesize=58*2.2;
		double keystonesize=85*2.2;
		double orbsize=27*2.1;

		int panX = model.getPanX();
		int panY = model.getPanY();
		int camX = model.getCamX();
		int camY = model.getCamY();
		// GROUPS // // // // // // // // // // // // // // // // // // // // // // // // // // //
		for(int i = 0; i < model.getGroupsSize(); i++) {
			gr = model.getGroup(i);
			int b = gr.getBackground();
			//System.out.println("background = "+b);
			if (b==1) {
				double bg1width=138*2.3;
				int x = (int)((gr.getX()+camX)*scale)+panX;
				int y = (int)((gr.getY()+camY)*scale)+panY;
				if (scale > 0.06) {
					dx = (int)(x-bg1width*0.5*scale);
					dy = (int)(y-bg1width*0.5*scale);
					g.drawImage(backgrounds[b], dx, dy, (int)(bg1width*scale), (int)(bg1width*scale), null);
				}
			}
			if (b==2) {
				double bg1width=179*2.6;
				int x = (int)((gr.getX()+camX)*scale)+panX;
				int y = (int)((gr.getY()+camY)*scale)+panY;
				if (scale > 0.06) {
					dx = (int)(x-bg1width*0.5*scale);
					dy = (int)(y-bg1width*0.5*scale);
					g.drawImage(backgrounds[b], dx, dy, (int)(bg1width*scale), (int)(bg1width*scale), null);
				}
			}
			if (b==3) {
				double bg1width=310*2.6;
				int x = (int)((gr.getX()+camX)*scale)+panX;
				int y = (int)((gr.getY()+camY)*scale)+panY;
				if (scale > 0.06) {
					dx = (int)(x-bg1width*0.5*scale);
					dy = (int)(y-bg1width*0.5*scale);
					g.drawImage(backgrounds[b], dx, dy, (int)(bg1width*scale), (int)(bg1width*scale), null);
				}
			}
		}
		// LINKS // // // // // // // // // // // // // // // // // // // // // // // // // // //
		for(int i = 0; i < model.getNodesCount(); i++) {
			n = model.getNode(i);
			int x1 = (int)((n.getDx()+camX)*scale)+panX;
			int y1 = (int)((n.getDy()+camY)*scale)+panY;
			int xc = (int)((n.getX()+camX)*scale)+panX;
			int yc = (int)((n.getY()+camY)*scale)+panY;
			Node m;
			for (int j = 0; j < n.links(); j++) {
				m=model.getNodeFromId(n.getLinks(j));
				if (n.getId() > m.getId()) {
					int x2 = (int)((m.getDx()+camX)*scale)+panX;
					int y2 = (int)((m.getDy()+camY)*scale)+panY;

					if (build!=null && build.isNodeActivated(n) && build.isNodeActivated(m)) {
						g.setColor(new Color(255, 255, 40)); // yellow
					} else {
						//g.setColor(new Color(200, 200, 150));
						g.setColor(new Color(190, 190, 140));
					}
					Graphics2D g2 = (Graphics2D)g;
					g2.setStroke(new BasicStroke(20*scale));
					if ( n.getGroup()==m.getGroup() && n.getSize()==m.getSize()) { //n.getId()==4397 && m.getId()==33783 && 
						double r = Math.sqrt(Math.pow(xc-x1, 2) + Math.pow(yc-y1, 2));
						double width = 2*r;
						double x = xc - r;
						double y = yc - r;
						double startAngle = (180/Math.PI)*Math.atan2(yc-y1, x1-xc);
						double endAngle = (180/Math.PI)*Math.atan2(yc-y2, x2-xc);
						double arcAngle = endAngle-startAngle;
						if (arcAngle > 180) {
							arcAngle-=360;
						}
						if (arcAngle <= -180) {
							arcAngle+=360;
						}
						g.drawArc((int)x, (int)y, (int)width, (int)width, (int)startAngle, (int)arcAngle);
						//System.out.println(n.getId()+"->"+m.getId()+"\t n= "+x1+","+y1+" m= "+x2+","+y2+"\tc= "+xc+","+yc+"\tstart="+(int)startAngle+"\tend="+(int)endAngle);
					} else {
						g.drawLine(x1, y1, x2, y2);
					}
				}
				//System.out.println(x+" "+y+" "+x2+" "+y2);
			} // endfor
		}
		//System.out.println("___");
		// NODES // // // // // // // // // // // // // // // // // // // // // // // // // // //
		for(int i = 0; i < model.getNodesCount(); i++) { //
			n = model.getNode(i);

			int x = (int)((n.getDx()+model.getCamX())*scale)+panX;
			int y = (int)((n.getDy()+model.getCamY())*scale)+panY;


			// draw node ids next to nodes
			//if(!n.isMastery()) {
			//g.setColor(Color.white);
			//g.drawString(""+n.getId(), x+10, y);
			//}

			//Icon icon = new Icon(81,0,27,27);
			//System.out.println(n.getIcon());

			//System.out.println(n.getIcon());
			Icon icon = model.getSkillIcon(n.getIcon());
			if (icon !=null) {
				if (n.getSpc()>0) {
					int spc = n.getSpc();
					w = 241*2;
					h = 222*2;
					dx = (int)(x-w*0.5*scale);
					dy = (int)(y-h*0.5*scale);
					g.drawImage(classes[spc-1], dx, dy, (int)(w*scale), (int)(h*scale),null);
				} else {
					if (scale > 0.06) {
						if (sprites!=null) {
							orbsize = icon.getW()*2.0;
							sx=icon.getX();
							sy=icon.getY();
							w=icon.getW();
							h=icon.getH();
							dx = (int)(x-orbsize*0.5*scale);
							dy = (int)(y-orbsize*0.5*scale);
							if (n.isMastery()) {
								g.drawImage(mastery, dx, dy, (int)(dx+orbsize*scale), (int)(dy+orbsize*scale), sx, sy, sx+w, sy+h, null);
							} else {

								if (build!=null&&build.isNodeActivated(n)) {
									g.drawImage(spritesActive, dx, dy, (int)(dx+orbsize*scale), (int)(dy+orbsize*scale), sx, sy, sx+w, sy+h, null);
								} else {
									g.drawImage(sprites, dx, dy, (int)(dx+orbsize*scale), (int)(dy+orbsize*scale), sx, sy, sx+w, sy+h, null);
								}
							}
						}
						if(n.isNotable()) {
							if (notable != null) {
								dx = (int)(x-notablesize*0.5*scale);
								dy = (int)(y-notablesize*0.5*scale);
								g.drawImage(notable, dx, dy, (int)(notablesize*scale), (int)(notablesize*scale), null);
							}
						} else if(n.isKeystone()) {
							if (keystone != null) {
								dx = (int)(x-keystonesize*0.5*scale);
								dy = (int)(y-keystonesize*0.5*scale);
								g.drawImage(keystone, dx, dy, (int)(keystonesize*scale), (int)(keystonesize*scale), null);
							}
						} else {
							if (!n.isMastery()) {
								if (ring != null) {
									if (build!=null && build.isNodeActivated(n)) {
										dx = (int)(x-ringactivesize*0.5*scale);
										dy = (int)(y-ringactivesize*0.5*scale);
										g.drawImage(ringActive, dx, dy, (int)(ringactivesize*scale), (int)(ringactivesize*scale), null);
									} else {
										dx = (int)(x-ringsize*0.5*scale);
										dy = (int)(y-ringsize*0.5*scale);
										g.drawImage(ring, dx, dy, (int)(ringsize*scale), (int)(ringsize*scale), null);
									}
								}
							}
						}

					}
				}

			}
		}
		
		int hover = model.getHover();
		n = model.getNode(hover);
		//System.out.println(n.getDescription());
		if (n!=null) {
			g.setColor(new Color(50,50,50));
			g.fillRect(0, 0, 500, n.getNumberOfMods()*14+16);
			String desc = n.getDescription();
			g.setColor(Color.white);
			g.drawString(desc+"    "+n.getId()+"   "+n.getGroup(), 10, 12);
			for(int i = 0; i < n.getNumberOfMods(); i++) {
				g.drawString(n.getMod(i).toString(), 10, 12+(14*(i+1)));
			}
			//g.drawString(n.getIcon(), 10, 12+(14*(n.getEffectsSize()+1)));
		}


	}
}
