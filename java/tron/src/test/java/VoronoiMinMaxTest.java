import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Kevin on 2015/12/13.
 */
public class VoronoiMinMaxTest {

    @Test
    public void testMove() {
        Board b = new Board(4, 4, 2, 0);
        b.move(0, 12);
        b.move(0, 8);
        b.move(0, 9);
        b.move(1, 3);
        b.move(1, 7);
        b.move(1, 11);
        VoronoiMinMax mmDriver = new VoronoiMinMax();
        Direction move = mmDriver.move(b);
        assertEquals(Direction.RIGHT, move);
    }

    /**
     *  x  x  1  x  x
     *  x  x  1  x  x
     *  x  x  1  0  0
     *  x  x  x  0  x
     *  x  0  0  0  x
     */
    @Test
    public void testMoveCornered() {
        String boardStr =
                "2h x  0  x  x\n" +
                "x  x  0  x  x\n" +
                "x  x  0h 1  1\n" +
                "x  c  x  1  x\n" +
                "x  1h 1  1  x";
        Board b = Board.fromString(boardStr);
        Driver d = new VoronoiMinMax();
        Direction move = d.move(b);
        assertEquals(Direction.LEFT, move);
    }

    @Test
    public void testMoveCutOff() {
        String boardStr =
                "x  x  x  0  1\n" +
                "x  x  x  0 1h\n" +
                "x  x  x  0h x\n" +
                "x  x  x  x  x\n" +
                "x  x  x  x  x";
        Board b = Board.fromString(boardStr);
        Driver d = new VoronoiMinMax();
        Direction move = d.move(b);
        assertEquals(Direction.RIGHT, move);
        b.move(b.US, b.tileFrom(b.ourTile(), move));
    }

    @Test
    public void killOffTest() {
        String boardStr =
                "x  0  0  0  0  x  x  x\n" + // 0
                "0  0  x  x  0  x  x  x\n" + // 1
                "0  x  x  x  0  0  x  x\n" +
                "0  x  x  x  x  0  x  x\n" +
                "0  x  x  x  x  0  x  x\n" +
                "0  0  0  x  x  0  x  x\n" + // 5
                "x  0  0  x  0  x  x  x\n" + // 6
                "0  0  x  x  0  x  x  x\n" + // 7
                "0  1  1h x  0  x  x  x\n" + // 8
                "0  1  x  x  0  0  x  x\n" + // 9
                "1  1  x  0h 0  0  x  x\n" + // 10
                "1  1  x  0  0  x  x  x\n" + // 11
                "1  1  x  x  x  x  x  x\n";
        Board b = Board.fromString(boardStr);
        Driver d = new VoronoiMinMax();
        Direction move = d.move(b);
        assertEquals(Direction.LEFT, move);
    }

//    @Test
//    public void dontLetEnemyEscape() {
//        String boardStr =
//                "1  a  a  a  a  a  a  a  a\n" +
//                "1  a  a  a  a  a  a  a  a\n" +
//                "1  a  a  a  a  a  a  a  a\n" +
//                "1  a  a  a  a  a  a  a  a\n" +
//                "1  a  a  a  a  a  a  a  a\n" +
//                "1  ax 0h 0  0  0  0  a  a\n" +
//                "1  ax a  a  a  a  a  a  a\n" +
//                "1h b  a  a  a  c  c  c  c\n" +
//                "b  b  a  a  c  c  c  2  c\n" +
//                "b  b  a  c  c  c  c  2  c\n" +
//                "b  b  c  c  c  2h 2  2  c\n";
//        Board b = Board.fromString(boardStr);
//        Driver d = new VoronoiMinMax();
//        Direction move = d.move(b);
//        assertNotEquals(Direction.UP, move);
//    }
}
