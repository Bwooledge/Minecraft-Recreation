import java.awt.Color;

public class DirtBlock extends Block{
	public DirtBlock() {
		super(new Color[] {
			new Color(140, 187, 93), new Color(103, 164, 64), 
			new Color(88, 144, 52), new Color(161, 112, 77),
			new Color(138, 98, 67), new Color(118, 83, 57),
			new Color(99, 69, 48), new Color(130, 115, 104),
			new Color(95, 156, 58)}, 
				new int[][][] {
			{{5, 3, 4, 3, 3, 4, 3, 6},
			{3, 5, 4, 4, 5, 7, 4, 6},
			{3, 5, 5, 6, 3, 5, 5, 5},
			{4, 7, 3, 5, 4, 6, 5, 3},
			{4, 5, 4, 3, 6, 4, 5, 5},
			{5, 6, 4, 4, 5, 4, 6, 6},
			{3, 5, 5, 5, 7, 5, 5, 3},
			{5, 5, 3, 3, 4, 4, 5, 5}},
			
			{{3, 4, 4, 5, 5, 3, 4, 4},
			{5, 4, 3, 5, 5, 4, 7, 6},
			{3, 5, 5, 6, 3, 5, 5, 5},
			{4, 7, 3, 5, 4, 6, 5, 3},
			{4, 5, 4, 3, 6, 4, 5, 5},
			{5, 6, 4, 4, 5, 4, 6, 6},
			{3, 5, 5, 5, 7, 5, 5, 3},
			{5, 5, 3, 3, 4, 4, 5, 5}}},
				new int[] {0, 0, 0, 0, 1, 1}, 8, null, false, false);
	}
}