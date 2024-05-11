import java.util.ArrayList;

public class Player {
	public double[] position;
	public Camera cam;
	public boolean grounded = false;
	public boolean jumping = false;
	public boolean crouched = false;
	public boolean crouch_trapped = false;
	public final double WIDTH;
	public double HEIGHT;
	
	private double[] movement = new double[2];
	private double leniency = 10;
	private double speed_multiplier = 1;
	private double max_speed = 1.75;
	private double smoothing = 0.2;
	private long last_forward = System.nanoTime() - 1000000000;
	private double[] vel = {0, 0, 0};
	
	public Player(double[] position, double width, double height)
	{
		this.position = position;
		this.WIDTH = width;
		this.HEIGHT = height;
		
		this.cam = new Camera(this, new double[]{position[0], position[1] + height, position[2]});
		Simulation.cam = cam;
		GraphicsRunner.cam = cam;
		GraphicsRunner.player = this;
		cam.player = this;
	}
	
	public void move()
	{
		//get xz plane angles of normal and right vector 
		//NOTE: this function keeps up and down always along the y axis, but xz is based on camera direction
		double theta = Math.atan2(cam.normal[2], cam.normal[0]);
		double theta2 = theta - Math.PI/2;
						
		//move player according to input
		if(movement[0] != 0) vel[0] += smoothing * movement[0] * speed_multiplier;
		else vel[0] = Math.abs(vel[0]) < smoothing ? 0 : vel[0] - smoothing * vel[0] / Math.abs(vel[0]);
		if(movement[1] != 0) vel[2] += smoothing * movement[1] * speed_multiplier;
		else vel[2] = Math.abs(vel[2]) < smoothing ? 0 : vel[2] - smoothing * vel[2] / Math.abs(vel[2]);
		if(Math.abs(vel[0]) > max_speed * speed_multiplier) vel[0] = max_speed * speed_multiplier * vel[0] / Math.abs(vel[0]);
		if(Math.abs(vel[2]) > max_speed * speed_multiplier) vel[2] = max_speed * speed_multiplier * vel[2] / Math.abs(vel[2]);
		double c = Math.sqrt(vel[0] * vel[0] + vel[2] * vel[2]);
		if(c > max_speed * speed_multiplier)
		{
			vel[0] *= max_speed * speed_multiplier / c;
			vel[2] *= max_speed * speed_multiplier / c;
		}
		position[0] += Math.cos(theta2) * vel[0] + Math.cos(theta) * vel[2];
		position[2] += Math.sin(theta2) * vel[0] + Math.sin(theta) * vel[2];
		
		//gravity
		vel[1] -= 0.125;
		
		//jumping
		if(jumping && grounded) vel[1] = 2.25;
		
		//vertical motion
		position[1] += vel[1];
		
		//collisions
		checkCollisions();
		
		//camera bobbing
		if(movement[0] != 0 || movement[1] != 0)
			cam.position[1] = HEIGHT + 0.3 * speed_multiplier * Math.sin((System.nanoTime()/100000000.0) * 1.25 * speed_multiplier);
		else
			cam.position[1] = HEIGHT;
		
		//fix crouch trap (crouched under a block)
		if(crouch_trapped) uncrouch();
		
		//smooth FOV shifts
		if(speed_multiplier == 1 && cam.FOV != 90 * Math.PI / 180)
		{
			cam.FOV -= cam.FOV - 90 * Math.PI / 180 < smoothing * Math.PI / 18 ? (cam.FOV - 90 * Math.PI / 180) : smoothing * Math.PI / 18;
			cam.clipping_distance = (int)(GraphicsRunner.WIDTH * 0.5 / Math.tan(cam.FOV / 2));
			cam.fov_slope = GraphicsRunner.WIDTH / cam.clipping_distance;
		}
		else if(speed_multiplier > 1 && cam.FOV != 100 * Math.PI / 180)
		{
			cam.FOV += 100 * Math.PI / 180 - cam.FOV < smoothing * Math.PI / 18 ? (100 * Math.PI / 180 - cam.FOV) : smoothing * Math.PI / 18;
			cam.clipping_distance = (int)(GraphicsRunner.WIDTH * 0.5 / Math.tan(cam.FOV / 2));
			cam.fov_slope = GraphicsRunner.WIDTH / cam.clipping_distance;
		}
	}
	
	public void checkCollisions()
	{
		grounded = false;
		ArrayList<Block> blocks = Artist.drawn_blocks;
		for(Block b: blocks)
		{
			//ignore if not close enough
			if(Math.sqrt(Math.pow((b.coord[0]*16+b.scale/2) - position[0], 2) + Math.pow((b.coord[2]*16+b.scale/2) - position[2], 2)) > WIDTH + b.scale/Math.sqrt(2)) continue;

			//horizontal collisions
			if(position[1] < b.coord[1] * 16 + b.scale - leniency && position[1] + HEIGHT > b.coord[1] * 16)
			{
				if(Math.abs(position[0] - (b.coord[0] * 16 + b.scale/2)) > Math.abs(position[2] - (b.coord[2] * 16 + b.scale/2))
						&& position[2] + WIDTH/2 > b.coord[2] * 16 && position[2] - WIDTH/2 < b.coord[2] * 16 + b.scale)
				{
					if(position[0] < b.coord[0] * 16 + b.scale/2 && position[0] + WIDTH/2 + leniency / 5 > b.coord[0] * 16) 
					{
						position[0] = b.coord[0] * 16 - WIDTH/2 - leniency / 5;
						speed_multiplier = 1;
					}	
					else if(position[0] > b.coord[0] * 16 + b.scale/2 && position[0] - WIDTH/2 - leniency / 5 < b.coord[0] * 16 + b.scale) 
					{
						position[0] = b.coord[0] * 16 + b.scale + WIDTH/2 + leniency / 5;
						speed_multiplier = 1;
					}
				}
				else if (position[0] + WIDTH/2 > b.coord[0] * 16 && position[0] - WIDTH/2 < b.coord[0] * 16 + b.scale)
				{
					if(position[2] < b.coord[2] * 16 + b.scale/2 && position[2] + WIDTH/2 + leniency / 5 > b.coord[2] * 16) 
					{
						position[2] = b.coord[2] * 16 - WIDTH/2 - leniency / 5;
						speed_multiplier = 1;
					}
					else if(position[2] + leniency / 5 > b.coord[2] * 16 + b.scale/2 && position[2] - WIDTH/2 - leniency / 5 < b.coord[2] * 16 + b.scale) 
					{
						position[2] = b.coord[2] * 16 + b.scale + WIDTH/2 + leniency / 5;
						speed_multiplier = 1;
					}
				}
			}		
			
			//vertical collisions
			if(position[0] + WIDTH/2 > b.coord[0] * 16 && position[0] - WIDTH/2 < b.coord[0] * 16 + b.scale
					&& position[2] + WIDTH/2 > b.coord[2] * 16 && position[2] - WIDTH/2 < b.coord[2] * 16 + b.scale)
			{
				if(position[1] < b.coord[1] * 16 + b.scale && position[1] > b.coord[1] * 16 + b.scale - leniency && vel[1] < 0)
				{
					position[1] = b.coord[1] * 16 + b.scale;
					vel[1] = 0;
					grounded = true;
				}
				else if(position[1] + HEIGHT > b.coord[1] * 16 && position[1] + HEIGHT < b.coord[1] * 16 + leniency && vel[1] > 0)
				{
					position[1] = b.coord[1] * 16 - HEIGHT;
					vel[1] = 0;
				}
			}
		}
		
		if(!grounded)
		{
			for(Block b: Artist.drawn_blocks)
			{
				//ignore if not close enough
				if(Math.sqrt(Math.pow((b.coord[0]*16+b.scale/2) - position[0], 2) + Math.pow((b.coord[2]*16+b.scale/2) - position[2], 2)) > WIDTH + b.scale/Math.sqrt(2)) continue;
				
				//crouched collisions
				if(crouched && position[1] - 1 < b.coord[1] * 16 + b.scale && position[1] > b.coord[1] * 16 + b.scale - leniency)
				{
					if(position[0] + WIDTH/2 + leniency/5 > b.coord[0] * 16 && position[0] - WIDTH/2 - leniency/5 < b.coord[0] * 16 + b.scale
							&& position[2] + WIDTH/2 + leniency/5 > b.coord[2] * 16 && position[2] - WIDTH/2 - leniency/5 < b.coord[2] * 16 + b.scale)
					{
						if(position[0] - WIDTH/2 > b.coord[0] * 16 + b.scale && Simulation.all_blocks[b.coord[2]][b.coord[1]][b.coord[0] + 1] == null)
							position[0] = b.coord[0] * 16 + b.scale + WIDTH/2;
						else if(position[0] + WIDTH/2 < b.coord[0] * 16 && Simulation.all_blocks[b.coord[2]][b.coord[1]][b.coord[0] - 1] == null)
							position[0] = b.coord[0] * 16 - WIDTH/2;
						if(position[2] - WIDTH/2 > b.coord[2] * 16 + b.scale && Simulation.all_blocks[b.coord[2] + 1][b.coord[1]][b.coord[0]] == null)
							position[2] = b.coord[2] * 16 + b.scale + WIDTH/2;
						else if(position[2] + WIDTH/2 < b.coord[2] * 16 && Simulation.all_blocks[b.coord[2] - 1][b.coord[1]][b.coord[0]] == null)
							position[2] = b.coord[2] * 16 - WIDTH/2;
						position[1] = b.coord[1] * 16 + b.scale;
						vel[1] = 0;
						grounded = true;
						break;
					}
				}
			}
		}
	}
	
	public void crouch()
	{
		if(crouched) return;
		crouched = true;
		HEIGHT *= 0.5;
		speed_multiplier = 0.4;
	}
	
	public void uncrouch()
	{
		if(!crouched) return;
		
		for(Block b: Artist.drawn_blocks)
		{
			//ignore if not close enough
			if(Math.sqrt(Math.pow((b.coord[0]*16+b.scale/2) - position[0], 2) + Math.pow((b.coord[2]*16+b.scale/2) - position[2], 2)) > WIDTH + b.scale/Math.sqrt(2)) continue;
			
			//crouched collisions
			if(crouched && position[1] + cam.position[1] + HEIGHT * 0.5 > b.coord[1] * 16 && position[1] + cam.position[1] + HEIGHT * 0.5 < b.coord[1] * 16 + b.scale)
			{
				if(position[0] + WIDTH/2 + leniency/5 > b.coord[0] * 16 && position[0] - WIDTH/2 - leniency/5 < b.coord[0] * 16 + b.scale
						&& position[2] + WIDTH/2 + leniency/5 > b.coord[2] * 16 && position[2] - WIDTH/2 - leniency/5 < b.coord[2] * 16 + b.scale)
				{
					crouch_trapped = true;
					return;
				}
			}
		}
		crouched = false;
		crouch_trapped = false;
		HEIGHT *= 2;
		speed_multiplier = 1;
	}

	public void setMovement(int i, int v)
	{
		movement[i] = v;
		
		//weakened diagonal
		if(movement[0] != 0 && movement[1] != 0)
		{
			movement[0] = Math.sqrt(0.5) * Math.abs(movement[0])/movement[0];
			movement[1] = Math.sqrt(0.5) * Math.abs(movement[1])/movement[1];
		}
		else
		{
			movement[0] = movement[0] == 0 ? 0 : Math.abs(movement[0])/movement[0];
			movement[1] = movement[1] == 0 ? 0 : Math.abs(movement[1])/movement[1];
		}
		
		//sprint
		if(i == 1 && !crouched)
		{
			if(v == 0)
			{
				if(speed_multiplier > 1) speed_multiplier = 1;
				else last_forward = System.nanoTime();
			}
			else if(System.nanoTime() - last_forward < 100000000) speed_multiplier = 1.5;
		}
			
	}

}
