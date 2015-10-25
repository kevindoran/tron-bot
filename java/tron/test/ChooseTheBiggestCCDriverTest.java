import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Kevin on 2015/10/25.
 */
public class ChooseTheBiggestCCDriverTest {

    @Test
    public void testMove() {
        int width = 5;
        int height = 6;
        Board b = new Board(width, height, 2, 0);
        b.move(0, 0, 2);
        b.move(0, 1, 2);
        b.move(1, 4, 2);
        b.move(1, 3, 2);
        ChooseTheBiggestCCDriver driver = new ChooseTheBiggestCCDriver();
        assertEquals(null, driver.move(b));
    }
}
