import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Kevin on 2015/10/24.
 */
public class BoardUtilTest  {
    private Board boardA;

    @Before
    public void setup() {
        int height = 4;
        int width = 4;
        boardA = new Board(width, height, 2, 0);
    }

    @Test
    public void battlefieldTest() {
        boardA.move(0, 0, 0);
        boardA.move(1, 3, 3);
        boolean[] expectedBattlefield = new boolean[boardA.width*boardA.height];
        int[] battlefieldPoints = {3, 6, 9, 12};
        for(int p : battlefieldPoints) {
            expectedBattlefield[p] = true;
        }
        boolean[] battlefield = BoardUtil.battlefield(boardA);
        assertEquals(expectedBattlefield.length, battlefield.length);
        for(int i = 0; i < battlefield.length; i++) {
            assertEquals(String.format("square %d, expected to be battlefield: %s.", i, expectedBattlefield[i]),expectedBattlefield[i], battlefield[i]);
        }
    }
}
