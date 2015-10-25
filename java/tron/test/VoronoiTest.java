import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
}
