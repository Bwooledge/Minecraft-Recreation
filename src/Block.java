import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Block {
	public int scale = Simulation.BLOCK_SIZE;
	public int[] coord;
	public double[][] vertices;
	public int[][] faces;
	public boolean rendered = true;
	public ArrayList<Integer> shown_faces = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5));
	
	//triangle-related rendering (unused in current code)
	boolean triangular = false;
	int[][] triangles;
	
	//texture-related rendering
	boolean texture = false;
	boolean edges = false;
	boolean invisible = false;
	boolean empty = false;
	Color[] palette;
	Color edge_color;
	int[] patterns;
	int[][][] pixels;
	int resolution;
	
	//constructors
	public Block()
	{
		this.vertices = new double[][]
		{
			{0, 0, 0},
			{0, scale, 0},
			{scale, scale, 0},
			{scale, 0, 0},
			{scale, 0, scale},
			{scale, scale, scale},
			{0, scale, scale},
			{0, 0, scale}
		};
		this.faces = new int[][] 
		{
			{0, 1, 2, 3},
			{3, 2, 5, 4},
			{4, 5, 6, 7},
			{7, 6, 1, 0},
			{1, 6, 5, 2},
			{7, 0, 3, 4}
		};
	}
	public Block(Color[] palette, int[][][] pixels, int[] patterns, int resolution, Color edge_color, boolean invisible, boolean empty)
	{
		this.palette = palette;
		this.pixels = pixels;
		this.patterns = patterns;
		this.resolution = resolution;
		this.edge_color = edge_color;
		this.invisible = invisible;
		this.empty = empty;
		texture = true;
		edges = true;
		if(edge_color == null) edges = false;
		
		//location of each vertice with front bottom left being (0, 0, 0)
		this.vertices = new double[][]
		{
			{0, 0, 0},
			{0, scale, 0},
			{scale, scale, 0},
			{scale, 0, 0},
			{scale, 0, scale},
			{scale, scale, scale},
			{0, scale, scale},
			{0, 0, scale}
		};
		
		//the indices of the vertices used in each face; order: front, right, back, left, top, bottom
		this.faces = new int[][] 
		{
			{0, 1, 2, 3},
			{3, 2, 5, 4},
			{4, 5, 6, 7},
			{7, 6, 1, 0},
			{1, 6, 5, 2},
			{7, 0, 3, 4}
		};
	}

	public void draw(Graphics g, double[] adj_pos)
	{
		//alternative drawing styles
		if(triangular) triangularDraw(g, adj_pos);
		else if(!texture) outlineDraw(g, adj_pos);
		if(triangular || !texture) return;
		
		//variable intialization
		double[] n = Simulation.cam.normal;
		double[] r = Simulation.cam.right;
		double[] u = Simulation.cam.up;
		double clipping_dist = Simulation.cam.clipping_distance;
		int w = GraphicsRunner.WIDTH;
		int h = GraphicsRunner.HEIGHT;
			
		//draw each visible face
		for(int a = 0; a < shown_faces.size(); a++)
		{
			int[] f = faces[shown_faces.get(a)];
			double[][] verts = new double[][]{vertices[f[0]], vertices[f[1]], vertices[f[2]], vertices[f[3]]};
			double[] l1 = {verts[2][0] - verts[1][0], verts[2][1] - verts[1][1], verts[2][2] - verts[1][2]};
			double[] l2 = {verts[0][0] - verts[1][0], verts[0][1] - verts[1][1], verts[0][2] - verts[1][2]};
			double[] cross = {l1[1] * l2[2] - l1[2] * l2[1], l1[2] * l2[0] - l1[0] * l2[2], l1[0] * l2[1] - l1[1] * l2[0]};

			//don't draw if not facing camera; dots normal of triangle with vector to center of triangle
			if(!invisible && cross[0] * ((verts[0][0] + verts[1][0] + verts[2][0]) / 3 + adj_pos[0]) 
				+ cross[1] * ((verts[0][1] + verts[1][1] + verts[2][1]) / 3 + adj_pos[1]) 
				+ cross[2] * ((verts[0][2] + verts[1][2] + verts[2][2]) / 3 + adj_pos[2]) > 0) continue;
			
			//get spatial delta of "horizontal" and "vertical" edges
			double[] delta_h = {verts[2][0] - verts[1][0], verts[2][1] - verts[1][1], verts[2][2] - verts[1][2]};
			double[] delta_v = {verts[3][0] - verts[2][0], verts[3][1] - verts[2][1], verts[3][2] - verts[2][2]};
			
			//draw each pixel
			for(int x = 0; x < resolution; x++)
			{
				for(int y = 0; y < resolution; y++)
				{
					//don't waste time on invisible pixels
					if(invisible && pixels[patterns[shown_faces.get(a)]][y][x] == 0)
						continue;
					
					//get screen coordinates for each corner
					int[][] screen_pts = new int[2][4];
					for(int i = 0; i < 2; i++)
					{
						for(int j = 0; j < 2; j++)
						{
							double[] v = {verts[1][0] + adj_pos[0] + (x + i)*1.0/resolution * delta_h[0] + (y + j)*1.0/resolution * delta_v[0],
									verts[1][1] + adj_pos[1] + (x + i)*1.0/resolution * delta_h[1] + (y + j)*1.0/resolution * delta_v[1],
									verts[1][2] + adj_pos[2] + (x + i)*1.0/resolution * delta_h[2] + (y + j)*1.0/resolution * delta_v[2]};
							double ndot = v[0] * n[0] + v[1] * n[1] + v[2] * n[2];				
							double c = clipping_dist / Math.abs(ndot);
							
							//essentially moving the point backward along a line toward the camera
							v[0] *= c;
							v[1] *= c;
							v[2] *= c;
							
							//transform to 2D based on the camera vectors
							double x_coord = w/2 + (r[0] * v[0] + r[1] * v[1] + r[2] * v[2]);
							double y_coord = h/2 - (u[0] * v[0] + u[1] * v[1] + u[2] * v[2]);
							
							//order the points in the array properly
							screen_pts[0][i*2+(i==0?j:1-j)] = (int)(x_coord + 0.5);
							screen_pts[1][i*2+(i==0?j:1-j)] = (int)(y_coord + 0.5);
						}
					}
					//draw the pixel
					g.setColor(palette[pixels[patterns[shown_faces.get(a)]][y][x]]);
					g.fillPolygon(screen_pts[0], screen_pts[1], 4);
				}
			}
		}
		
		//outlines of blocks
		if(edges)
			outlineDraw(g, adj_pos);
	}
	
	public void outlineDraw(Graphics g, double[] adj_pos)
	{
		//this is essentially the same code as draw() but only for the outlines
		double[] n = Simulation.cam.normal;
		double[] r = Simulation.cam.right;
		double[] u = Simulation.cam.up;
		double clipping_dist = Simulation.cam.clipping_distance;
		int w = GraphicsRunner.WIDTH;
		int h = GraphicsRunner.HEIGHT;
				
		for(int a = 0; a < shown_faces.size(); a++)
		{
			int[] f = faces[shown_faces.get(a)];
			double[][] verts = new double[][]{vertices[f[0]], vertices[f[1]], vertices[f[2]], vertices[f[3]]};
			double[] l1 = {verts[2][0] - verts[1][0], verts[2][1] - verts[1][1], verts[2][2] - verts[1][2]};
			double[] l2 = {verts[0][0] - verts[1][0], verts[0][1] - verts[1][1], verts[0][2] - verts[1][2]};
			double[] cross = {l1[1] * l2[2] - l1[2] * l2[1], l1[2] * l2[0] - l1[0] * l2[2], l1[0] * l2[1] - l1[1] * l2[0]};

			if(cross[0] * ((verts[0][0] + verts[1][0] + verts[2][0]) / 3 + adj_pos[0]) 
					+ cross[1] * ((verts[0][1] + verts[1][1] + verts[2][1]) / 3 + adj_pos[1]) 
					+ cross[2] * ((verts[0][2] + verts[1][2] + verts[2][2]) / 3 + adj_pos[2]) > 0) continue;
			
			//instead of getting the corners of the pixels, just gets the corners of the face
			int[][] screen_points = new int[2][4];
			for(int i = 0; i < 4; i++)
			{
				double[] v = {verts[i][0] + adj_pos[0], verts[i][1] + adj_pos[1], verts[i][2] + adj_pos[2]};

				double ndot = v[0] * n[0] + v[1] * n[1] + v[2] * n[2];				
				double c = clipping_dist / Math.abs(ndot);
				
				v[0] *= c;
				v[1] *= c;
				v[2] *= c;
								
				double x = w/2 + (r[0] * v[0] + r[1] * v[1] + r[2] * v[2]);
				double y = h/2 - (u[0] * v[0] + u[1] * v[1] + u[2] * v[2]);
				
				screen_points[0][i] = (int)(x + 0.5);
				screen_points[1][i] = (int)(y + 0.5);
			}	
			
			//check that at least 1 point is within the screen
			int count_out = 0;
			for(int i = 0; i < screen_points[0].length; i++)
				if(screen_points[0][i] < 0 || screen_points[0][i] > GraphicsRunner.WIDTH || 
						screen_points[1][i] < 0 || screen_points[1][i] > GraphicsRunner.HEIGHT) count_out++;
			
			//draw the outline
			if(count_out != 4)
			{
				Color mainColor = palette[pixels[patterns[0]][0][0]];
				if(edge_color == null) g.setColor(new Color(255 - mainColor.getRed(), 255 - mainColor.getGreen(), 255 - mainColor.getBlue()));
				else g.setColor(edge_color);
				((Graphics2D)g).setStroke(new BasicStroke(1));
				g.drawPolygon(screen_points[0], screen_points[1], 4);
			}
		}
	}
	
	public void triangularDraw(Graphics g, double[] adj_pos)
	{
		//similar to outline draw, but it uses triangles instead of squares (more common in 3D rendering)
		double[] n = Simulation.cam.normal;
		double[] r = Simulation.cam.right;
		double[] u = Simulation.cam.up;
		double clipping_dist = Simulation.cam.clipping_distance;
		int w = GraphicsRunner.WIDTH;
		int h = GraphicsRunner.HEIGHT;
				
		for(int[] tri : triangles)
		{
			double[][] verts = new double[][]{vertices[tri[0]], vertices[tri[1]], vertices[tri[2]]};
			double[] l1 = {verts[2][0] - verts[1][0], verts[2][1] - verts[1][1], verts[2][2] - verts[1][2]};
			double[] l2 = {verts[0][0] - verts[1][0], verts[0][1] - verts[1][1], verts[0][2] - verts[1][2]};
			double[] cross = {l1[1] * l2[2] - l1[2] * l2[1], l1[2] * l2[0] - l1[0] * l2[2], l1[0] * l2[1] - l1[1] * l2[0]};

			if(cross[0] * ((verts[0][0] + verts[1][0] + verts[2][0]) / 3 + adj_pos[0]) 
					+ cross[1] * ((verts[0][1] + verts[1][1] + verts[2][1]) / 3 + adj_pos[1]) 
					+ cross[2] * ((verts[0][2] + verts[1][2] + verts[2][2]) / 3 + adj_pos[2]) >= 0) continue;
			
			int[][] screen_points = new int[2][3];
			for(int i = 0; i < 3; i++)
			{
				double[] v = {verts[i][0] + adj_pos[0], verts[i][1] + adj_pos[1], verts[i][2] + adj_pos[2]};

				double ndot = v[0] * n[0] + v[1] * n[1] + v[2] * n[2];				
				double c = clipping_dist / ndot;
				
				v[0] *= c;
				v[1] *= c;
				v[2] *= c;
				
				double x = w/2 + (r[0] * v[0] + r[1] * v[1] + r[2] * v[2]);
				double y = h/2 - (u[0] * v[0] + u[1] * v[1] + u[2] * v[2]);
				
				screen_points[0][i] = (int)(x + 0.5);
				screen_points[1][i] = (int)(y + 0.5);
			}
			g.setColor(Color.BLACK);
			g.drawLine(screen_points[0][0], screen_points[1][0], screen_points[0][1], screen_points[1][1]);
			g.drawLine(screen_points[0][1], screen_points[1][1], screen_points[0][2], screen_points[1][2]);
			
			//draw full triangle
			//g.drawPolygon(screen_points[0], screen_points[1], 3);
		}
	}
}
