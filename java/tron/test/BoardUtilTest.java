import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

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
    /**
     *   x  x  0  0
     *   x  x  0  0h
     *   x  x  0  x
     *   1h x  0  0
     */
    public void isAloneInComponentTest() {
        boardA.move(0, 3, 3);
        assertTrue(BoardUtil.isAloneInComponent(boardA));
        boardA.move(1, 0, 3);
        boardA.move(0, 2, 3);
        boardA.move(0, 2, 2);
        boardA.move(0, 2, 1);
        boardA.move(0, 2, 0);
        assertFalse(BoardUtil.isAloneInComponent(boardA));
        boardA.move(0, 3, 0);
        boardA.move(0, 3, 1);
        assertTrue(BoardUtil.isAloneInComponent(boardA));
    }

    @Test
    public void isAloneInComponentTest2() {
        Board b = Board.fromString(
                "x  x  x  x  x\n" +
                "0h x  x  x  x\n" +
                "0  0  0  0  0\n" +
                "x  x  x  x  2h\n" +
                "1h x  x  x  2\n");
        assertTrue(BoardUtil.isAloneInComponent(b));
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
            assertEquals(String.format("square %d, expected to be battlefield: %s.", i, expectedBattlefield[i]),
                    expectedBattlefield[i], battlefield[i]);
        }
    }

    @Test
    public void testCutVirtices() {
        String boardStr =
                "2  x  x  x  2  2  2\n" +
                "2  2  2  2  2  x  2h\n" +
                "x  c  c  c  c  c  x\n" +
                "c  x  1  1  x  x  x\n" +
                "c  1  1  1  x  x  x\n" +
                "c  1  x  1  1  c  0h\n" +
                "x  1h x  x  1  x  0\n";
        Board b = Board.fromString(boardStr);
        BoardUtil.CutVertices cv = new BoardUtil.CutVertices(b);
        boolean[] cutVirtices = cv.getCutVirtices();
        Integer[] cutVerticesExpected = new Integer[] {15, 16, 17, 18, 19, 21, 28, 35, 40, 41};
        Set<Integer> expected  = new HashSet<Integer>(Arrays.asList(cutVerticesExpected));
        for(int i = 0; i < cutVirtices.length; i++) {
            assertEquals("Failed on index " + i, expected.contains(i), cutVirtices[i]);
        }
    }

    @Test
    public void testBorderingPlayerCount() {
        String boardStr =
                "2  x  x  x  2  2  2\n" +
                "2  2  2  2  2  x  2h\n" +
                "x  c  c  c  c  c  x\n" +
                "c  x  1  1  x  x  x\n" +
                "c  1  1  1  x  x  x\n" +
                "c  1  x  1  1  c  0h\n" +
                "x  1h x  x  1  x  0\n";
        Board b = Board.fromString(boardStr);
        BoardUtil.BoardZones bz = new BoardUtil.BoardZones(b, b.US);
        assertEquals(1, bz.borderingPlayerCount(0));
        assertEquals(1, bz.borderingPlayerCount(1));
        assertEquals(2, bz.borderingPlayerCount(2));
    }


    @Test
    public void testPlayerEdgeCount() {
        String boardStr =
                "2  x  x  x  2  2  2\n" +
                "2  2  2  2  2  c  2h\n" +
                "b  b  c  c  c  c  c\n" +
                "b  b  1  1  a  a  a\n" +
                "b  1  1  1  a  a  a\n" +
                "b  1  b  1  1  a  0h\n" +
                "b  1h b  b  1  a  0\n";
        Board b = Board.fromString(boardStr);
        BoardUtil.BoardZones bz = new BoardUtil.BoardZones(b, b.US);
        // Currently, it is half counting the actual battlefield. Not sure yet if this is + / -.
        assertEquals(24, bz.getEdgeCount(0));
        assertEquals(17, bz.getEdgeCount(2));
        // Not quite sure why this is one off yet.
        assertEquals(21, bz.getEdgeCount(1));
    }

//    @Test
//    public void testSameComponentCount() {
//        String boardStr =
//                "0h 0  1  x  x\n" +
//                "x  x  1  x  x\n" +
//                "x  x  1  x  x\n" +
//                "x  x  1h x  x\n" +
//                "x  x  x  2  2h\n";
//        Board b = Board.fromString(boardStr);
//        BoardUtil.BoardZones bz = new BoardUtil.BoardZones(b, b.US);
//        assertTrue(bz.isInSameComponent(0, 1));
//        assertTrue(bz.isInSameComponent(1, 2));
//        assertFalse(bz.isInSameComponent(0, 2));
//    }

    @Test
    public void testSmartAvaiableSpace() {
        String boardStr =
                "b  w  b  w  b  w  b\n" +
                "w  b  1  1  1  b  w\n" +
                "b  w  x  x  1  w  b\n" +
                "w  b  2  x  1  0h w\n" +
                "b  w  2  x  1h 0  x\n" +
                "w  b  2  2  2h 0  x";
        Board b = Board.fromString(boardStr);
        BoardUtil.AvailableSpace as = new BoardUtil.AvailableSpace(b, b.US, b.US);
        assertEquals(11, as.getMaxMoves());
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
