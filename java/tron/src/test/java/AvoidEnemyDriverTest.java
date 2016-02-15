import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Kevin on 11/02/2016.
 */
public class AvoidEnemyDriverTest {

    @Test
    public void testAoid() {
        String boardStr =
                "X  x  3h x  x  2h\n" +
                "x  x  x  x  x  x\n" +
                "x  x  x  x  x  x\n" +
                "x  x  0h x  x  x\n" +
                "x  x  x  x  x  x\n" +
                "x  x  x  x  x  x\n" +
                "x  1h x  x  x  x\n";
        Board b = Board.fromString(boardStr);
        AvoidEnemyDriver d = new AvoidEnemyDriver();
        Direction move = d.move(b);
        b.move(b.US, b.tileFrom(b.ourTile(), move));
        assertEquals(Direction.RIGHT, move);
        move = d.move(b);
        b.move(b.US, b.tileFrom(b.ourTile(), move));
        assertEquals(Direction.RIGHT, move);
        move = d.move(b);
        assertEquals(Direction.DOWN, move);
    }
}
