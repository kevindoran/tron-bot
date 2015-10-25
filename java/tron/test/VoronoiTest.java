import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Kevin on 2015/10/24.
 */
public class VoronoiTest {
    private Board board;

    @Before
    public void setup() {
        int width = 5;
        int height = 5;
        board = new Board(width, height, 2, 0);
    }

    @Test
    public void pathToTest() {
        board.move(0, 1, 0);
        board.move(1, 1, 4);
        Voronoi voronoiDriver = new Voronoi();
        int expectedNextTile = 6;
        List<Integer> pathToBattle = voronoiDriver.pathToBattle(board);
        assertEquals(expectedNextTile, (int)pathToBattle.get(0));
    }

    @Test
    public void situationTest() {
        Board b = new Board(30, 20, 4, 3);
        b.move(0, 25, 12);
        b.move(0, 26, 11);
        b.move(1, 25, 0);
        b.move(1, 24, 0);
        b.move(2, 25, 2);
        b.move(2, 25, 1);
        b.move(3, 26, 2);
        Voronoi driver = new Voronoi();
        Direction dir = driver.move(b);
        assertTrue(dir != Direction.LEFT);
    }
}
