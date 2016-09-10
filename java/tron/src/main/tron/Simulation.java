import com.opencsv.CSVWriter;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by Kevin on 2015/12/17.
 */
public class Simulation {
    private Board board;
    private Position[] initialPositions;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<SimPlayer> players;
    private GameResult result;

    public Simulation(int width, int height, List<SimPlayer> simPlayers) {
        players = simPlayers;
        int playerCount = players.size();
        this.result = new GameResult(playerCount);
        Random rn = new Random();
        int us = 0;
        board = new Board(width, height, playerCount, us);
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
                    } else {
                        conflicts = false;
                    }
                }
            } while(conflicts);
//            System.out.println(players.get(p).getName() + " (x,y) (" + x + "," + y + ")");
            initialPositions[p] = new Position(x, y);
            board.move(p, initialPositions[p]);
        }
        for(int p = 0; p < playerCount; p++) {
            players.get(p).init(width, height, boardToText(board, initialPositions, p));
        }
    }

    public GameResult run() {
        while(board.getAliveCount() > 1) {
            for(int p = 0; p < players.size() && board.getAliveCount() > 1; p++) {
                if (board.playerTile(p) == Board.DEAD) {
                    continue;
                }
                String message = boardToText(board, initialPositions, p);
                Position position;
                try {
                    Direction d;
                    try {
                        final int pTemp = p;
                        Future<Direction> future = executor.submit(() -> players.get(pTemp).next(message));
                        d = future.get(20000, TimeUnit.MILLISECONDS);
                    } catch(TimeoutException e) {
                        System.err.println("Received timeout for player " + players.get(p).getName());
                        markDead(p);
                        continue;
                    }
                    int tile = board.tileFrom(board.playerTile(p), d);
                    position = board.tileToPos(tile);
                    if(!board.isValid(position) || !board.isFree(tile)) {
                        markDead(p);
                        continue;
                    }
                    board.move(p, position);
                } catch(Exception e) {
                    markDead(p);
                    continue;
                }
            }
        }
        markDead(board.getAlivePlayers().get(0));
        executor.shutdown();
        return result;
    }

    private void markDead(int player) {
        board.clear(player);
        result.addNextDead(players.get(player));
    }

    private String boardToText(Board b, Position[] initialPositions, int player) {
        StringBuilder sb = new StringBuilder();
        sb.append(b.getPlayerCount()).append(" ").append(player).append("\n");
        for(int p = 0; p < b.getPlayerCount(); p++) {
            String initialPos = "" + initialPositions[p].getX() + " " +
                    initialPositions[p].getY();
            sb.append(initialPos).append(" ");
            if(b.playerTile(p) == b.DEAD) {
                sb.append(initialPos).append("\n");
            } else {
                sb.append(b.tileToPos(b.playerTile(p)).getX()).append(" ")
                        .append(b.tileToPos(b.playerTile(p)).getY()).append("\n");
            }
        }
        return sb.toString();
    }
}
