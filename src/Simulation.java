import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;
import java.io.File;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;

public class Simulation extends JPanel implements Runnable {	
	public static Graphics g;
	public static Camera cam;
	public static final int FRAMES = 60;
	public static final int BLOCK_SIZE = 16;
	
	public static Player player = new Player(new double[] {300, 64, 300}, 5, 24);
	public static Block[][][] all_blocks = new Block[64][16][64];
	
	public static double[] light_dir = {0.7, -1, 0.5};
	public static double light_intensity = 0.3;
	
	//music player
	public static MediaPlayer mediaPlayer;
	
	public Simulation()
	{
		setVisible(true);
		
		//plane of blank blocks
		for(int i = 16; i < 48; i++)
			for(int j = 3; j < 4; j++)
				for(int k = 16; k < 48; k++)
					newBlock(i, j, k, new Block(new Color[] {new Color(30, 0, 20)}, 
							new int[][][] {{{0}}},
						new int[] {0, 0, 0, 0, 0, 0}, 1, new Color(86, 30, 227), false, false));
		
		//grass platform with a tree on top
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++)
				newBlock(21 + i, 4, 21 + j, new GrassBlock());
		for(int i = 0; i < 4; i++)
			newBlock(23, 5 + i, 23, new OakLog());
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++)
				for(int k = 0; k < 2; k++)
					if(!(i == 2 && j == 2 && k == 0))
						newBlock(21 + i, 8 + k, 21 + j, new OakLeaves(false));
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				newBlock(22 + i, 10, 22 + j, new OakLeaves(false));
		newBlock(23, 11, 23, new OakLeaves(false));
		newBlock(22, 11, 23, new OakLeaves(false));
		newBlock(24, 11, 23, new OakLeaves(false));
		newBlock(23, 11, 22, new OakLeaves(false));
		newBlock(23, 11, 24, new OakLeaves(false));
		
		//initialize lists of what will be drawn
		Artist.updateBlockList();
		Artist.updateFaces();

		//normalize light_dir
		double ld_len = Math.sqrt(Math.pow(light_dir[0], 2) + Math.pow(light_dir[1], 2) + Math.pow(light_dir[2], 2));
		for(int i = 0; i < 3; i++)
			light_dir[i] /= ld_len;
		
		//music
		final JFXPanel fxPanel = new JFXPanel();
		String randomSong = "" + (int)(Math.random() * 9);
		Media hit = new Media(new File(("music/minecraft" + randomSong + ".mp3")).toURI().toString());
		mediaPlayer = new MediaPlayer(hit);
		mediaPlayer.play();
		
		new Thread(this).start();
	}
	
	@Override
	public void paint(Graphics g)
	{
		((Graphics2D) g).setStroke(new BasicStroke(2));
		moveEverything();
		Artist.drawEverything(g);
		cam.destroy(g);
	}
		
	public static void moveEverything()
	{
		player.move();
		cam.rotate();
	}
	
	public static void newBlock(int x, int y, int z, Block b)
	{
		all_blocks[z][y][x] = b;
		b.coord = new int[]{x, y, z};
	}

	@Override
	public void run() {
		repaint();
		try
		{
			//loop that maintains a constant FPS
		   	long executionStamp = System.nanoTime(); 
		   	while(true)
		   	{
		   		if (System.nanoTime() - executionStamp > 1000000000 / FRAMES) 
		   		{
		   			repaint();
		   			executionStamp = System.nanoTime();
		   		}
		   	} 		
		} catch(Exception e){}
	}
}
