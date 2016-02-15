import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by Kevin on 2015/10/25.
 */
public class AvoidSmallComponentsTest {

    @Test
    public void testMove() {
        String boardStr =
                "x  1h x  x  x\n" +
                "x  1  x  1  1\n" +
                "x  1  1  x  1\n" +
                "x  x  x  0h 0\n" +
                "x  x  x  0  0\n";
        Board b = Board.fromString(boardStr);
        AvoidSmallComponents filter = new AvoidSmallComponents();
        Set<Direction> filtered = filter.filterBadMoves(b, new HashSet<>(Arrays.asList(Direction.values())));
        assertEquals(1, filtered.size());


        int width = 5;
        int height = 6;
        b = new Board(width, height, 2, 0);
        b.move(0, 0, 2);
        b.move(0, 1, 2);
        b.move(1, 4, 2);
        b.move(1, 3, 2);
        AvoidSmallComponents driver = new AvoidSmallComponents();
        Set<Direction> allDirections = new HashSet<>();
        // The current test has no connected components, but the filter still removes unsafe positions as these are
        // considered to be part of a component with size 0.
        allDirections.add(Direction.DOWN);
        allDirections.add(Direction.RIGHT);
        allDirections.add(Direction.UP);
        assertEquals(allDirections, driver.filterBadMoves(b, allDirections));
    }
}
