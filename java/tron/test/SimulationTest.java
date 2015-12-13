import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Created by Kevin on 2015/12/13.
 */
public class SimulationTest {

    private SimPlayer[] players;
    private Board board;
    Position[] initialPositions;

    @Test
    public void simulation() {
        int width = 30;
        int height = 20;
        int playerCount = 4;
        players = new SimPlayer[playerCount];
        int us = 0;
        Random rn = new Random();
        board = new Board(30, 20, 4, 0);
        // P0 is the test sim.
        players[0] = new SimPlayer(new VoronoiMinMax());
        players[1] = new SimPlayer(new WallHuggingDriver());
        players[2] = new SimPlayer(new StaySafeDriver());
        players[3] = new SimPlayer(new StaySafeDriver());
        initialPositions = new Position[playerCount];
        for(int p = 0; p < playerCount; p++) {
            int x;
            int y;
            boolean conflicts = false;
            do {
                x = Math.abs(rn.nextInt()) % width;
                y = Math.abs(rn.nextInt()) % height;
                for(int p2 = 0; p2 < p; p2++) {
                    if(initialPositions[p2].getX() == x && initialPositions[p2].getY() == y) {
                        conflicts = true;
                    }
                }
            } while(conflicts);
            initialPositions[p] = new Position(x, y);
            board.move(p, initialPositions[p]);
        }
        for(int p = 0; p < playerCount; p++) {
            players[p].init(width, height, boardToText(board, initialPositions, p));
        }
        runSim();
    }

    private void runSim() {
        while(board.getPlayerCount() > 1) {
            for(int p = 0; p < players.length; p++) {
                if (board.playerTile(p) == Board.DEAD) {
                    continue;
                }
                String message = boardToText(board, initialPositions, p);
                Position position;
                try {
                    Direction d = players[p].next(message);
                    int tile = board.tileFrom(board.playerTile(p), d);
                    position = board.tileToPos(tile);
                } catch(Exception e) {
                    //System.out.println(String.format("Exception from player %d: " + e.toString(), p));
                    throw e;
//                    board.clear(p);
//                    continue;
                }
                if(board.isValid(position)&& board.move(p, position) != -1) {
                    System.out.println(String.format("Player %d moved to invalid position.", p));
                    board.clear(p);
                }
            }
        }
    }

    private class SimPlayer {
        private Driver d;
        private boolean isAlive;
        private Board board;
        private InputParser parser = new InputParser();

        public SimPlayer(Driver driver) {
            this.d = driver;
        }

        public void init(int width, int height, String message) {
            System.setIn(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)));
            board = parser.init(width, height);
        }

        public Direction next(String input) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            Direction nextMove = d.move(board);
            int tile = board.tileFrom(board.ourTile(), nextMove);
            parser.update(board);
            return nextMove;
        }
    }

    private String boardToText(Board b, Position[] initialPositions, int player) {
        StringBuilder sb = new StringBuilder();
        sb.append(b.getPlayerCount()).append(" ").append(player).append("\n");
        for(int p = 0; p < b.getPlayerCount(); p++) {
            String initialPos = "" + initialPositions[p].getX() +
                    initialPositions[p].getY();
            sb.append(initialPos).append(" ");
            if(b.playerTile(p) == b.DEAD) {
                sb.append(initialPos).append("\n");
            } else {
                sb.append(b.tileToPos(b.playerTile(p)).getX())
                        .append(b.tileToPos(b.playerTile(p))).append("\n");
            }
        }
        return sb.toString();
    }
}
