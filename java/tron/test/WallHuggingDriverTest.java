import org.junit.Test;

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
        while(b.ourTile() != b.DEAD) {
            System.out.println("Move");
            Direction move = driver.move(b);
            b.move(b.US, b.tileFrom(b.ourTile(), move));
        }
    }
}
