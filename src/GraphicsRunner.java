import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GraphicsRunner extends JFrame implements KeyListener, MouseListener, MouseMotionListener {   
	public static final int WIDTH = 1920;
	public static final int HEIGHT = 1080;
	public static final int FRAMES = 60;
	public static Camera cam;
	public static Player player;
	private static Robot robot;
	
	//for mouse movement
	private int centeredX = -1;
    private int centeredY = -1;
    
	public GraphicsRunner() 
	{
		super("Minecraft");
		setSize(WIDTH, HEIGHT);
		setExtendedState(JFrame.MAXIMIZED_BOTH); 
		setUndecorated(true);
		getContentPane().add(new Simulation());
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		//hide cursor
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
		    cursorImg, new Point(0, 0), "blank cursor");
		getContentPane().setCursor(blankCursor);
	}
	
	public static void main(String[] args) throws AWTException
	{
		GraphicsRunner run = new GraphicsRunner();
		robot = new Robot();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//translational movement
		if (e.getKeyCode() == KeyEvent.VK_W)
            player.setMovement(1, 1);
		else if (e.getKeyCode() == KeyEvent.VK_A)
			player.setMovement(0, -1);
		else if (e.getKeyCode() == KeyEvent.VK_S)
			player.setMovement(1, -1);
		else if (e.getKeyCode() == KeyEvent.VK_D)
			player.setMovement(0, 1);
		else if (e.getKeyCode() == KeyEvent.VK_SPACE)
			player.jumping = true;
		
		//crouch
		else if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			player.crouch();
			
		//select block
		else if(e.getKeyCode() >= 48 && e.getKeyCode() <= 57) 
			cam.setCurrentBlock(e.getKeyCode() - 48);

		//quit out
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));	
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		//translational movement
		if (e.getKeyCode() == KeyEvent.VK_W)
			player.setMovement(1, 0);
		else if (e.getKeyCode() == KeyEvent.VK_A)
			player.setMovement(0, 0);
		else if (e.getKeyCode() == KeyEvent.VK_S)
			player.setMovement(1, 0);
		else if (e.getKeyCode() == KeyEvent.VK_D)
			player.setMovement(0, 0);
		else if (e.getKeyCode() == KeyEvent.VK_SPACE)
			player.jumping = false;

		//crouch
		else if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			player.uncrouch();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}

	public void mouseMoved(MouseEvent e) {
        int deltaX = e.getX() - centeredX;
        int deltaY = -(e.getY() - centeredY);
        
        cam.deltaX += deltaX;
        cam.deltaY += deltaY;

        centeredX = WIDTH / 2 + (1920 - WIDTH)/2;
        centeredY = HEIGHT / 2 + (1080 - HEIGHT)/2;
        robot.mouseMove(centeredX, centeredY);
    }
	
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3)
			cam.place();
		if(e.getButton() == MouseEvent.BUTTON1)
			cam.destroying = true;
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			cam.destroying = false;
			cam.destroyed = 0;
		}	
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	
}
