import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;

public class Artist {
	static ArrayList<Block> drawn_blocks = new ArrayList<Block>();
	
	static Camera cam = Simulation.cam;
	static Player p = Simulation.player;
	
	//for drawing current block UI in bottom left
	static int bsize = 112;
	static int bthick = 6;
	
	public static void drawEverything(Graphics g)
	{
		double[] n = cam.normal;
		
		//background
		g.setColor(new Color(150, 150, 255));
		g.fillRect(0,  0, GraphicsRunner.WIDTH, GraphicsRunner.HEIGHT);
		
		//get new block order depending on player position
		drawn_blocks = orderObjects(drawn_blocks);
		
		//draw each block in order of distance from player
		for(Block b : drawn_blocks)
		{
			double[] adj_pos = new double[] 
					{b.coord[0] * 16 - (p.position[0] + cam.position[0]), 
					b.coord[1] * 16 - (p.position[1] + cam.position[1]), 
					b.coord[2] * 16 - (p.position[2] + cam.position[2])};
			double z = ((adj_pos[0] + b.scale/2) * n[0] + (adj_pos[1] + b.scale/2) * n[1] + (adj_pos[2] + b.scale/2) * n[2]);
			double ray_len = Math.sqrt(Math.pow(adj_pos[0] + b.scale / 2, 2) + Math.pow(adj_pos[1] + b.scale / 2, 2) + Math.pow(adj_pos[2] + b.scale / 2, 2));
			
			//only draw if safely within screen
			if(z > b.scale / 2 && z / ray_len > 0.2)
				b.draw(g, adj_pos);
			
			//render blocks right next to camera
			else if(z > -5 && z < 10 && adj_pos[0] * n[0] + adj_pos[1] * n[1] < 0.2)
				b.draw(g, adj_pos);
		}
		
		//draw crosshair
		g.setColor(new Color(175, 175, 175));
		g.fillRect(GraphicsRunner.WIDTH / 2 - 2, GraphicsRunner.HEIGHT / 2 - 25, 4, 50);
		g.fillRect(GraphicsRunner.WIDTH / 2 - 25, GraphicsRunner.HEIGHT / 2 - 2, 50, 4);
		
		//draw current block UI
		if(cam.cb != null)
		{
			Block b = cam.cb;
			int res = b.resolution;
			
			//draw each pixel
			for(int i = 0; i < b.resolution; i++)
			{
				for(int j = 0; j < b.resolution; j++)
				{
					Color base_col = b.palette[b.pixels[b.patterns[0]][i][j]];
					if(b.invisible && b.pixels[b.patterns[0]][i][j] == 0) continue;
					g.setColor(new Color(base_col.getRed(), base_col.getGreen(), base_col.getBlue(), 225));
					g.fillRect(50 + j * bsize/res, GraphicsRunner.HEIGHT - bsize - 40 + i * bsize/res, bsize/res, bsize/res);
				}
			}
			
			//outline
			((Graphics2D)g).setStroke(new BasicStroke(bthick));
			g.setColor(new Color(175, 175, 175, 225));
			g.drawRect(50 - bthick/2, GraphicsRunner.HEIGHT - bsize - 40 - bthick/2, bsize + bthick, bsize + bthick);
		}	
	}
	
	//orders an arraylist of blocks by distance from player
	public static ArrayList<Block> orderObjects(ArrayList<Block> blocks)
	{
		double[] pp = {p.position[0] + cam.position[0], p.position[1] + cam.position[1], p.position[2] + cam.position[2]};
		for(int i = 1; i < blocks.size(); i++)
		{
			Block b = blocks.get(i);
			double dist = getDist(pp, b.coord);
			
			//sort faces by distance for invisible blocks
			if(b.invisible)
			{
				double[] adj_pp = {pp[0] - b.coord[0] * 16, pp[1] - b.coord[1] * 16, pp[2] - b.coord[2] * 16};
				for(int m = 1; m < b.shown_faces.size(); m++)
				{
					int current_face = b.shown_faces.get(m);
					double dist2 = getDist(adj_pp, new double[]
							{(b.vertices[b.faces[b.shown_faces.get(m)][0]][0] + b.vertices[b.faces[b.shown_faces.get(m)][2]][0])/2,
							(b.vertices[b.faces[b.shown_faces.get(m)][0]][1] + b.vertices[b.faces[b.shown_faces.get(m)][2]][1])/2,
							(b.vertices[b.faces[b.shown_faces.get(m)][0]][2] + b.vertices[b.faces[b.shown_faces.get(m)][2]][2])/2});
					
					int n = m - 1;
					while(n >= 0 && dist2 > getDist(adj_pp, new double[]
							{(b.vertices[b.faces[b.shown_faces.get(n)][0]][0] + b.vertices[b.faces[b.shown_faces.get(n)][2]][0])/2,
							(b.vertices[b.faces[b.shown_faces.get(n)][0]][1] + b.vertices[b.faces[b.shown_faces.get(n)][2]][1])/2,
							(b.vertices[b.faces[b.shown_faces.get(n)][0]][2] + b.vertices[b.faces[b.shown_faces.get(n)][2]][2])/2}))
					{
						b.shown_faces.set(n + 1, b.shown_faces.get(n));
						n--;
					}	
					b.shown_faces.set(n + 1, current_face);
				}
			}
			
			//sort blocks by distance
			int j = i - 1;
			while(j >= 0 && dist > getDist(pp, blocks.get(j).coord))
			{
				blocks.set(j + 1, blocks.get(j));
				j--;
			}
			blocks.set(j + 1, b);
		}	
		return blocks;
	}
	
	//only runs after key events; determines which blocks will be drawn
	public static void updateBlockList()
	{
		drawn_blocks = new ArrayList<Block>();
		Block[][][] b = Simulation.all_blocks;
		for(int k = 0; k < b.length; k++)
			for(int l = 0; l < b[0].length; l++)
				for(int m = 0; m < b[0][0].length; m++)
					if(b[k][l][m] != null && b[k][l][m].rendered == true)
						drawn_blocks.add(b[k][l][m]);
		updateFaces();
	}
	
	//doesn't draw faces of the blocks that are touching other blocks
	public static void updateFaces()
	{
		//NOTE: could optimize by moving through entire array in checkerboard pattern; z+=2, % for staggered starts, and disable two faces at once
		Block[][][] blocks = Simulation.all_blocks;
		double[] pp = {p.position[0] + cam.position[0], p.position[1] + cam.position[1], p.position[2] + cam.position[2]};
		for(int z = 0; z < blocks.length; z++)
			for(int y = 0; y < blocks[0].length; y++)
				for(int x = 0; x < blocks[0][0].length; x++)
				{
					Block b = blocks[z][y][x];
					if(b == null || b.invisible && !b.empty) continue;
					b.shown_faces = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5));
					
					//order: front, right, back, left, top, bottom
					if (z > 0 && blocks[z - 1][y][x] != null && (!blocks[z - 1][y][x].invisible || blocks[z - 1][y][x].empty && blocks[z][y][x].empty)) 
						b.shown_faces.remove(b.shown_faces.indexOf(0));
					if (x < blocks[0][0].length - 1 && blocks[z][y][x + 1] != null && (!blocks[z][y][x + 1].invisible || blocks[z][y][x + 1].empty && blocks[z][y][x].empty)) 
						b.shown_faces.remove(b.shown_faces.indexOf(1));					
					if (z < blocks.length - 1 && blocks[z + 1][y][x] != null && (!blocks[z + 1][y][x].invisible || blocks[z + 1][y][x].empty && blocks[z][y][x].empty)) 
						b.shown_faces.remove(b.shown_faces.indexOf(2));	
					if (x > 0 && blocks[z][y][x - 1] != null && (!blocks[z][y][x - 1].invisible || blocks[z][y][x - 1].empty && blocks[z][y][x].empty)) 
						b.shown_faces.remove(b.shown_faces.indexOf(3));	
					if(y < blocks[0].length - 1 && blocks[z][y + 1][x] != null && (!blocks[z][y + 1][x].invisible || blocks[z][y + 1][x].empty && blocks[z][y][x].empty)) 
						b.shown_faces.remove(b.shown_faces.indexOf(4));
					if(y > 0 && blocks[z][y - 1][x] != null && (!blocks[z][y - 1][x].invisible || blocks[z][y - 1][x].empty && blocks[z][y][x].empty)) 
						b.shown_faces.remove(b.shown_faces.indexOf(5));			
				}
	}
	
	//returns distance from block to player
	public static double getDist(double[] pp, int[] coord)
	{
		return Math.sqrt(Math.pow(coord[0] * 16 - pp[0], 2) + Math.pow(coord[1] * 16 - pp[1], 2) + Math.pow(coord[2] * 16 - pp[2], 2));
	}
	
	//returns distance from face to player
	public static double getDist(double[] pp, double[] coord)
	{
		return Math.sqrt(Math.pow(coord[0] - pp[0], 2) + Math.pow(coord[1] - pp[1], 2) + Math.pow(coord[2] - pp[2], 2));
	}
}
