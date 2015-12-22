import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Kevin on 2015/12/20.
 */
public class WallHuggingDriverTest {
    @Test
    public void noExceptionsTest() {
        WallHuggingDriver driver = new WallHuggingDriver();
        Board b = Board.fromString(
                "0  x  x  x  x\n" +
                "0  x  x  x  x\n" +
                "0  x  x  x  x\n" +
                "0  x  x  x  x\n" +
                "0  0h x  x  x");
        int count = 0;
        while(b.ourTile() != b.DEAD) {
            count++;
            System.out.println("Move");
            Direction move = driver.move(b);
            b.move(b.US, b.tileFrom(b.ourTile(), move));
        }
        assertEquals(20, count);
    }
}
