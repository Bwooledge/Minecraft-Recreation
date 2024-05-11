import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;

public class Camera {
	public double[] position;
	public double[] normal = new double[] {0, 0, 1};
	public double[] right = new double[] {1, 0, 0};
	public double[] up = new double[] {0, 1, 0};
	
	public double FOV = 90 * Math.PI / 180;
	public double fov_slope;
	public int clipping_distance;
	
	public double deltaX = 0;
	public double deltaY = 0;
	public double sensitivity_x = 0.0015; 
	public double sensitivity_y = 0.0025;
	public double roughness = 0.5;
	
	public double place_range = 100;
	public int destroyed = 0;
	public int destroy_speed = 20;
	public int[] current_coords = null;
	public int delay_frames = 0;
	public boolean destroying;
	
	public Player player;
	public Block cb;
	
	public Camera(Player p, double[] position)
	{
		player = p;
		this.position = new double[] {0, p.HEIGHT, 0};
		
		int w = GraphicsRunner.WIDTH;
		clipping_distance = (int)(w * 0.5 / Math.tan(FOV / 2));
		fov_slope = w / clipping_distance;
		
		setCurrentBlock(1);
	}
	
	//handles yaw and pitch with smoothing
	public void rotate()
	{
		rotateYaw(deltaX * roughness * sensitivity_x);
		rotatePitch(deltaY * roughness * sensitivity_y);
		deltaX *= (1 - roughness);
		deltaY *= (1 - roughness);
	}
	
	public void rotateYaw(double deltaX)
	{	
		//adjust angle of normal vector on xz plane
		double theta1 = Math.atan2(normal[2], normal[0]);
		theta1 -= deltaX;
		
		//adjust normal vector
		double c = Math.sqrt(Math.pow(normal[0], 2) + Math.pow(normal[2], 2));
		normal[0] = c * Math.cos(theta1);
		normal[2] = c * Math.sin(theta1);
		
		//adjust up vector
		c = Math.sqrt(Math.pow(up[0], 2) + Math.pow(up[2], 2));
		double theta3 = Math.atan2(up[2], up[0]) - deltaX;
		up[0] = c * Math.cos(theta3);
		up[2] = c * Math.sin(theta3);
				
		//adjust right vector
		c = Math.sqrt(Math.pow(right[0], 2) + Math.pow(right[2], 2));
		right[0] = c * Math.cos(theta1 - Math.PI/2);
		right[2] = c * Math.sin(theta1 - Math.PI/2);
	}

	public void rotatePitch(double deltaY)
	{
		//get angle of normal vector on xz plane and on the plane generated by the normal and up vectors
		double theta1 = Math.atan2(normal[2], normal[0]);
		double c = Math.sqrt(Math.pow(normal[0], 2) + Math.pow(normal[2], 2));
		double theta2 = Math.atan2(normal[1], c);
		
		//change angle
		theta2 += deltaY;
		if(theta2 > Math.PI / 2) theta2 = Math.PI / 2;
		else if(theta2 < -Math.PI / 2) theta2 = -Math.PI / 2;
		
		//adjust normal vector
		normal[1] = Math.sin(theta2);
		c = Math.cos(theta2);
		normal[0] = c * Math.cos(theta1);
		normal[2] = c * Math.sin(theta1);
		
		//adjust up vector
		up[1] = Math.sin(theta2 + Math.PI/2);
		double d = Math.cos(theta2 + Math.PI/2);
		up[0] = d * Math.cos(theta1);
		up[2] = d * Math.sin(theta1);
	}
	
	public void normalizeVectors()
	{
		double normal_len = Math.sqrt(Math.pow(normal[0], 2) + Math.pow(normal[1], 2) + Math.pow(normal[2], 2));
		double right_len = Math.sqrt(Math.pow(right[0], 2) + Math.pow(right[1], 2) + Math.pow(right[2], 2));
		double up_len = Math.sqrt(Math.pow(up[0], 2) + Math.pow(up[1], 2) + Math.pow(up[2], 2));
		for(int i = 0 ; i < 3; i++)
		{
			normal[i] /= normal_len;
			right[i] /= right_len;
			up[i] /= up_len;
		}
	}
	
	//place a block
	public void place()
	{
		int[] coords = rayTrace();
		if(coords == null) return;
		switch(coords[3])
		{
			case 0: coords[2] -= 1; break;
			case 1: coords[0] += 1; break;
			case 2: coords[2] += 1; break;
			case 3: coords[0] -= 1; break;
			case 4: coords[1] += 1; break;
			case 5: coords[1] -= 1; break;
			default: break;
		}
		if(coords[0] > 0 && coords[1] > 0 && coords[2] > 0 
				&& coords[0] < Simulation.all_blocks.length && coords[1] < Simulation.all_blocks[0].length && coords[2] < Simulation.all_blocks[0][0].length
				&& !((int)(player.position[0] / 16.0) == coords[0] && (int)(player.position[1] / 16.0) == coords[1] && (int)(player.position[2] / 16.0) == coords[2]))
		{
			Simulation.newBlock(coords[0], coords[1], coords[2], new Block(cb.palette, cb.pixels, cb.patterns, cb.resolution, cb.edge_color, cb.invisible, cb.empty));
			Artist.updateBlockList();
		}
	}
	
	//loop to destroy blocks
	public void destroy(Graphics g)
	{
		//delay between breaking blocks
		if(delay_frames < 0)
		{
			delay_frames++;
			return;
		}
		
		if(!destroying) return;
		
		//check if block in range
		int[] coords = rayTrace();
		if(coords == null) {
			destroyed = 0;
			current_coords = null;
			return;
		}
		if(current_coords == null || current_coords[0] != coords[0] || current_coords[1] != coords[1] || current_coords[2] != coords[2])
		{
			destroyed = 0;
			current_coords = coords;
		}
		
		//grow arc
		destroyed += destroy_speed;
		
		//draw arc
		g.setColor(Color.WHITE);
		((Graphics2D) g).setStroke(new BasicStroke(6));
		g.drawArc(GraphicsRunner.WIDTH/2 - 50, GraphicsRunner.HEIGHT/2 - 50, 100, 100, 90, -(int)destroyed);
		
		//break block when arc is full
		if(destroyed >= 360)
		{
			destroyed = 0;
			Simulation.removeBlock(coords[0], coords[1], coords[2]);
			Artist.updateBlockList();
			delay_frames = -8;
		}
		
	}
	
	public void setCurrentBlock(int slot)
	{
		switch(slot)
		{
			case 0:
				cb = new Block(new Color[] {new Color(30, 0, 20)}, 
						new int[][][] {{{0}}},
					new int[] {0, 0, 0, 0, 0, 0}, 1, new Color(86, 30, 227), false, false);
				break;
			case 1:
				cb = new DirtBlock();
				break;
			case 2:
				cb = new GrassBlock();
				break;
			case 3:
				cb = new OakLog();
				break;
			case 4:
				cb = new OakLeaves();
				break;
			case 5:
				cb = new OakLeaves(false);
				break;
			case 6:
				cb = new CandyCaneBlock();
				break;
			default:
				cb = new DefaultBlock();
				break;
		}
	}
	
	//find what the camera is looking at
	public int[] rayTrace()
	{		
		//values for parametric equations
		double m1 = 1;
		double m2 = normal[1] / normal[0];
		double m3 = normal[2] / normal[0];
		double b1 = player.position[0] + position[0];
		double b2 = player.position[1] + position[1];
		double b3 = player.position[2] + position[2];
		
		double closest = Double.MAX_VALUE;
		int[] closest_info = null;
		for(int i = 0; i < Artist.drawn_blocks.size(); i++)
		{
			Block b = Artist.drawn_blocks.get(i);
			double[] adj_pos = {b.coord[0] * 16, b.coord[1] * 16, b.coord[2] * 16};
			for(int j = 0; j < b.shown_faces.size(); j++)
			{
				int[] face = b.faces[b.shown_faces.get(j)];
				
				//ignore faces that are the wrong direction
				double[][] verts = new double[4][3];
				for(int k = 0; k < 4; k++) 
					verts[k] = new double[] {b.vertices[face[k]][0] + adj_pos[0], b.vertices[face[k]][1] + adj_pos[1], b.vertices[face[k]][2] + adj_pos[2]};
				double[] l1 = {verts[2][0] - verts[1][0], verts[2][1] - verts[1][1], verts[2][2] - verts[1][2]};
				double[] l2 = {verts[0][0] - verts[1][0], verts[0][1] - verts[1][1], verts[0][2] - verts[1][2]};
				double[] cross = {l1[1] * l2[2] - l1[2] * l2[1], l1[2] * l2[0] - l1[0] * l2[2], l1[0] * l2[1] - l1[1] * l2[0]};
				if(normal[0] * cross[0] + normal[1] * cross[1] + normal[2] * cross[2] >= 0) continue;
								
				//mimics plane equation; this is the d from d = ax + by + cz
				double d = cross[0] * verts[0][0] + cross[1] * verts[0][1] + cross[2] * verts[0][2];
				
				//time of intersection; simplified from: cross[0] * (m1 * t + b1) + cross[1] * (m2 * t + b2) + cross[2] * (m3 * t + b3) = d
				double t = (d - (cross[0] * b1 + cross[1] * b2 + cross[2] * b3)) / (cross[0] * m1 + cross[1] * m2 + cross[2] * m3);
				
				//point of intersection
				double[] point = {m1 * t + b1, m2 * t + b2, m3 * t + b3};	
				
				//check if on face
				if((point[0] >= verts[0][0] && point[0] <= verts[2][0]) || (point[0] <= verts[0][0] && point[0] >= verts[2][0])) {
					if((point[1] >= verts[0][1] && point[1] <= verts[2][1]) || (point[1] <= verts[0][1] && point[1] >= verts[2][1])) {
						if((point[2] >= verts[0][2] && point[2] <= verts[2][2]) || (point[2] <= verts[0][2] && point[2] >= verts[2][2]))
						{
							double[] center = {(verts[0][0] + verts[2][0])/2, (verts[0][1] + verts[2][1])/2, (verts[0][2] + verts[2][2])/2};
							double dist = Math.sqrt(Math.pow(b1 - center[0], 2) + Math.pow(b2 - center[1], 2) + Math.pow(b3 - center[2], 2));
							if(dist < closest)
							{
								closest = dist;
								if (cross[0] > 0) closest_info = new int[]{b.coord[0], b.coord[1], b.coord[2], 1};
								else if (cross[0] < 0) closest_info = new int[]{b.coord[0], b.coord[1], b.coord[2], 3};
								else if (cross[1] > 0) closest_info = new int[]{b.coord[0], b.coord[1], b.coord[2], 4};
								else if (cross[1] < 0) closest_info = new int[]{b.coord[0], b.coord[1], b.coord[2], 5};
								else if (cross[2] > 0) closest_info = new int[]{b.coord[0], b.coord[1], b.coord[2], 2};
								else if (cross[2] < 0) closest_info = new int[]{b.coord[0], b.coord[1], b.coord[2], 0};
							}
						}
					}
				}
			}
		}
				
		if(closest > place_range) return null;
		return closest_info;
	}
}
