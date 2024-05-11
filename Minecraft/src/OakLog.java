import java.awt.Color;

public class OakLog extends Block{
	public OakLog() {
		super(new Color[] {
			new Color(105, 81, 50), new Color(134, 103, 60), 
			new Color(85, 66, 40), new Color(68, 55, 36),
			new Color(50, 39, 25), new Color(152, 120, 69), 
			new Color(146, 115, 67), new Color(161, 130, 76)}, 
				new int[][][] {
			{{0, 3, 0, 2, 0, 4, 1, 2},
			{0, 4, 2, 2, 0, 4, 1, 2},
			{1, 4, 0, 2, 0, 3, 0, 2},
			{1, 3, 0, 2, 0, 4, 0, 2},
			{1, 0, 3, 0, 0, 2, 3, 0},
			{2, 0, 3, 0, 1, 2, 4, 0},
			{2, 0, 4, 0, 1, 2, 3, 1},
			{0, 3, 4, 0, 1, 2, 3, 1}},
			
			{{3, 3, 2, 0, 2, 3, 2, 0},
			{2, 6, 7, 7, 5, 5, 6, 2},
			{0, 7, 7, 7, 7, 7, 7, 2},
			{2, 7, 6, 5, 5, 7, 7, 3},
			{3, 5, 6, 5, 7, 7, 7, 3},
			{3, 7, 7, 6, 7, 5, 5, 2},
			{2, 7, 7, 7, 7, 7, 6, 2},
			{0, 2, 2, 3, 3, 2, 0, 3}}},
				new int[] {0, 0, 0, 0, 1, 1}, 8, null, false, false);
	}
}