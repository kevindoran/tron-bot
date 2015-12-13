import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Kevin on 2015/10/24.
 */
public class BoardUtilTest {
    private Board boardA;

    @Before
    public void setup() {
        int height = 4;
        int width = 4;
        boardA = new Board(width, height, 2, 0);
    }

    @Test
    public void testBattlefield() {
        boardA.move(0, 0, 0);
        boardA.move(1, 3, 3);
        boolean[] expectedBattlefield = new boolean[boardA.width * boardA.height];
        int[] battlefieldPoints = {3, 6, 9, 12};
        for (int p : battlefieldPoints) {
            expectedBattlefield[p] = true;
        }
        boolean[] battlefield = BoardUtil.battlefield(boardA);
        assertEquals(expectedBattlefield.length, battlefield.length);
        for (int i = 0; i < battlefield.length; i++) {
            assertEquals(String.format("square %d, expected to be battlefield: %s.", i, expectedBattlefield[i]), expectedBattlefield[i], battlefield[i]);
        }
    }

    @Test
    public void testConnectedComponents() {
        boardA.move(0, 0, 3);
        boardA.move(1, 3, 0);
        boolean[] outOfBounds = new boolean[]{true, true, false, false,
                false, false, true, false,
                false, true, false, false,
                false, false, false, false};
        BoardUtil.ConnectedComponents cc = new BoardUtil.ConnectedComponents(boardA, boardA.ourTile(), outOfBounds);
        assertEquals(2, cc.getComponentCount());
        int largerCCCount = 5;
        int smallerCCCount = 3;
        int smallerComponentID = cc.getConnectedComponents()[4];
        int largerComponentID = cc.getConnectedComponents()[7];
        assertEquals(smallerCCCount, cc.getMaxMoves(smallerComponentID));
        assertEquals(largerCCCount, cc.getMaxMoves(largerComponentID));
        assertEquals(largerCCCount, cc.getMaxMoves());

        // Try 2
        Board boardB = new Board(4, 4, 2, 0);
        boardB.move(0, 0, 3);
        boardB.move(0, 0, 2);
        boardB.move(0, 1, 2);
        boardB.move(1, 3, 0);
        boardB.move(1, 3, 1);
        boardB.move(1, 3, 2);
        boardB.move(1, 2, 2);
        outOfBounds = BoardUtil.battlefield(boardB);
        cc = new BoardUtil.ConnectedComponents(boardB, boardB.ourTile(), outOfBounds);
        largerCCCount = 4;
        smallerCCCount = 1;
        smallerComponentID = cc.getConnectedComponents()[13];
        largerComponentID = cc.getConnectedComponents()[0];
        assertEquals(smallerCCCount, cc.getMaxMoves(smallerComponentID));
        assertEquals(largerCCCount, cc.getMaxMoves(largerComponentID));
        assertEquals(largerCCCount, cc.getMaxMoves());
    }
}
