import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

class SimPlayer {
    private Driver d;
    private Board board;
    private InputParser parser = new InputParser();
    private String name;

    public SimPlayer(String name, Driver driver) {
        this.d = driver;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void init(int width, int height, String message) {
        System.out.println(message);
        System.setIn(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)));
        board = parser.init(width, height);
    }

    public Direction next(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        parser.update(board);
        Direction nextMove = d.move(board);
        int tile = board.tileFrom(board.ourTile(), nextMove);
        if(!board.isFree(tile)) {
            throw new RuntimeException("Shouldn't be moving to invalid tile.");
        }
        return nextMove;
    }
}

