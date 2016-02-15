import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Kevin on 2015/12/19.
 */
public class BoardTest {
    Board boardA;

    @Before
    public void setup() {
        boardA = Board.fromString(
            "1  x  x  x  x\n" +
            "1  x  x  x  x\n" +
            "1  x  x  x  x\n" +
            "1  x  0  0  0\n" +
            "1  1h 0h 0  0");
    }

    @Test
    public void testMove() {
        Board b = new Board(4, 4, 2, 0);
        assertEquals(Board.NOT_ON_BOARD, b.playerTile(0));
        b.move(0, 0, 0);
        assertEquals(0, b.playerTile(0));
        assertEquals(0, b.getTileValue(0));
    }

    @Test
    public void testUnmove() {
        Board b = new Board(4, 4, 2, 0);
        b.move(0, 0, 0);
        b.move(0, 1, 0);
        assertEquals(1, b.playerTile(0));
        b.undoMove();
        assertEquals(Board.EMPTY, b.getTileValue(1));
        assertEquals(0, b.playerTile(0));
        b.undoMove();
        assertEquals(Board.NOT_ON_BOARD, b.playerTile(0));
    }

    @Test
    public void testMoveUnmove() {
        boardA.move(1, 1, 3);
        assertEquals(2, boardA.getAliveCount());
        boardA.move(1, 2, 3);
        assertEquals(1, boardA.getAliveCount());
        assertEquals(18, BoardUtil.availableSpaces(boardA, boardA.playerTile(0)));
//        assertEquals(0, BoardUtil.availableSpaces(b, 1));
        boardA.undoMove();
        assertEquals(2, boardA.getAliveCount());
        assertEquals(16, boardA.playerTile(1));
        assertEquals(0, BoardUtil.availableSpaces(boardA, boardA.playerTile(0)));
        assertEquals(12, BoardUtil.availableSpaces(boardA, boardA.playerTile(1)));
    }
}
