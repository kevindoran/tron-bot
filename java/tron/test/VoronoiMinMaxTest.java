import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
