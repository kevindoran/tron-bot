import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Kevin on 2015/12/25.
 */
public class BruteForceEndGameTest {

    @Test
    public void testMove() {
        Board b = Board.fromString(
                "0h x  x  x  x\n" +
                "x  x  x  x  x\n" +
                "x  x  x  x  x\n" +
                "x  x  x  x  x\n" +
                "x  x  x  x  x\n");
        BruteForceEndGame bfDriver = new BruteForceEndGame();
        int count = 0;
        while(b.isAlive(b.US)) {
            Direction move = bfDriver.move(b);
            b.move(b.US, b.tileFrom(b.ourTile(), move));
            count++;
        }
        assertEquals(25, count);
    }

    @Test
    public void moveCornerTest() {
        Board b = Board.fromString(
                "0  0  x  x  x\n" +
                "0  0  x  x  x\n" +
                "0  0h x  x  x\n" +
                "0  x  0  0  0\n" +
                "0  0  x  x  x\n");
        BruteForceEndGame bfDriver = new BruteForceEndGame();
        Direction move = bfDriver.move(b);
        assertEquals(Direction.RIGHT, move);
    }

    @Test
    public void moveWideCornerTest() {
        Board b = Board.fromString(
                "0  x  x  x  x\n" +
                "0  x  x  x  x\n" +
                "0  x  x  x  x\n" +
                "0h x  x  x  x\n" +
                "x  x  1  1  1\n" +
                "x  x  1  x  x\n" +
                "x  x  1  x  x\n");
        BruteForceEndGame driver = new BruteForceEndGame();
        Direction move = driver.move(b);
        assertEquals(Direction.DOWN, move);
        b.move(0, b.tileFrom(b.ourTile(), move));
        move = driver.move(b);
        assertEquals(Direction.DOWN, move);
        b.move(0, b.tileFrom(b.ourTile(), move));
        move = driver.move(b);
        assertEquals(Direction.DOWN, move);
        b.move(0, b.tileFrom(b.ourTile(), move));
        move = driver.move(b);
        assertEquals(Direction.RIGHT, move);
        b.move(0, b.tileFrom(b.ourTile(), move));
        move = driver.move(b);
        assertEquals(Direction.UP, move);
    }

    @Test
    public void moveWideCornerTest2() {
        // Real example where bad decision was observed.
        Board b = Board.fromString(
                "x  x  x  x  x  1  1  1  1  1  1\n" +
                "x  x  x  1  1  1  x  x  x  x  x\n" +
                "x  x  x  1  1  x  x  x  x  x  x\n" +
                "x  x  x  1  1  x  x  x  x  x  x\n" +
                "x  1h x  1  x  x  x  x  x  x  x\n" +
                "0  1  1  1  x  0  0  0  0  0  0\n" +
                "0  x  x  x  x  0  0  0  0  0  0\n" +
                "0  x  x  x  x  x  0h 0  0  0  0\n" +
                "0  x  x  x  x  x  x  x  x  x  0\n" +
                "0  0  0  0  0  x  x  x  x  x  0\n" +
                "x  x  x  x  0  x  x  x  x  x  0\n" +
                "x  x  x  0  0  x  x  x  x  x  0\n" +
                "x  x  x  0  x  x  x  x  x  0  0\n" +
                "x  x  x  0  0  0  0  0  0  0  x\n");
        Driver driver = new BruteForceEndGame();
        assertEquals(Direction.DOWN, driver.move(b));
    }
}
