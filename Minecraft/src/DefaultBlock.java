import java.awt.Color;

public class DefaultBlock extends Block {
	public DefaultBlock() {
		super(new Color[] {Color.MAGENTA, Color.BLACK}, 
				new int[][][] {
				{{0, 1}, 
				{1, 0}}},
				new int[] {0, 0, 0, 0, 0, 0}, 2, null, false, false);
	}
}
