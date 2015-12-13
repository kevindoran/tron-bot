import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Kevin on 2015/12/13.
 */
public class PositionTest {

    @Test
    public void isBlackTest() {
        Board b = new Board(4, 4, 2, 0);
        Position p1 = new Position(0, 2);
        assertEquals(true, p1.isBlack());
        Position p2 = new Position(0, 3);
        assertEquals(false, p2.isBlack());
        Position p3 = new Position(2, 2);
        assertEquals(true, p3.isBlack());
    }
}
