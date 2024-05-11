import java.awt.Color;

public class OakLeaves extends Block{
	public OakLeaves() {
		super(new Color[] {
			new Color(9, 18, 11), new Color(48, 102, 61), 
			new Color(39, 92, 52), new Color(30, 74, 40),
			new Color(25, 64, 34)}, 
				new int[][][] {
			{{3, 4, 4, 0, 3, 0, 3, 0},
			{0, 2, 0, 1, 2, 1, 0, 3},
			{4, 0, 4, 0, 2, 0, 2, 4},
			{0, 3, 3, 2, 0, 4, 0, 4},
			{3, 0, 2, 0, 2, 3, 2, 0},
			{4, 1, 0, 2, 0, 2, 0, 4},
			{3, 0, 2, 1, 2, 0, 2, 0},
			{0, 3, 0, 3, 0, 4, 3, 4}}},
				new int[] {0, 0, 0, 0, 0, 0}, 8, null, true, false);
	}
	
	public OakLeaves(boolean invisible)
	{
		super(new Color[] {
				new Color(9, 18, 11), new Color(48, 102, 61), 
				new Color(39, 92, 52), new Color(30, 74, 40),
				new Color(25, 64, 34)}, 
					new int[][][] {
				{{3, 4, 4, 0, 3, 0, 3, 0},
				{0, 2, 0, 1, 2, 1, 0, 3},
				{4, 0, 4, 0, 2, 0, 2, 4},
				{0, 3, 3, 2, 0, 4, 0, 4},
				{3, 0, 2, 0, 2, 3, 2, 0},
				{4, 1, 0, 2, 0, 2, 0, 4},
				{3, 0, 2, 1, 2, 0, 2, 0},
				{0, 3, 0, 3, 0, 4, 3, 4}}},
					new int[] {0, 0, 0, 0, 0, 0}, 8, null, invisible, false);
	}
}