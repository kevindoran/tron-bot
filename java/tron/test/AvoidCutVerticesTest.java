import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Kevin on 2015/12/20.
 */
public class AvoidCutVerticesTest {

    @Test
    public void testFilter() {
        Board b = Board.fromString(
                "x  x  x  x  x\n" +
                "x  x  x  x  x\n" +
                "0  0  0  0h x\n" +
                "x  x  x  x  x\n" +
                "x  x  x  x  x");
        AvoidCutVertices filter = new AvoidCutVertices();
        Set<Direction> directions = new HashSet<>();
        directions.addAll(Arrays.asList(Direction.values()));
        Set<Direction> filtered = filter.filterBadMoves(b, directions);
        assertEquals(2, filtered.size());
        assertFalse(filtered.contains(19));
    }
}
