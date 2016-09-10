import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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
        ByteArrayInputStream stream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        this.board = parser.init(width, height, stream);
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Direction next(String input) {
        ByteArrayInputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        parser.update(board, stream);
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Direction nextMove = d.move(board);
        int tile = board.tileFrom(board.ourTile(), nextMove);
        if(!board.isFree(tile)) {
            throw new RuntimeException("Shouldn't be moving to invalid tile.");
        }
        return nextMove;
    }
}

