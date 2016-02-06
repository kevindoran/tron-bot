import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.EnumUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * Created by Kevin on 2016/01/29.
 */
public class Replay {

    public static final String USERNAME = "CloudLeaper";
    public static class MoveStat {
        List<Integer> counts = new ArrayList<>();
        List<Integer> wideCounts = new ArrayList<>();
        int ourCount = 0;
        int ourWideCount = 0;
        int ourEdges;
        int borderingPlayerCount;
        int turnsRemaining;
        int avrEnemyTurnsRemaining;
        int alivePlayers;
        int backstabbingPlayerCount;
        int enemySpace;
        int endPoints;
        static final int DATA_COUNT = 11;

        public  MoveStat(Board b, int[] movesLeft, int endPoints) {
            this.endPoints = endPoints;
            turnsRemaining = movesLeft[b.US];
            alivePlayers = b.getAliveCount();
            for(int p = 0; p < b.getPlayerCount(); p++) {
                if(p != b.US) {
                    avrEnemyTurnsRemaining += movesLeft[p];
                }
            }
            avrEnemyTurnsRemaining /= (b.getPlayerCount() - 1);
            BoardUtil.BoardZones bz = new BoardUtil.BoardZones(b, b.US);
            borderingPlayerCount = bz.borderingPlayerCount(b.US);
            backstabbingPlayerCount = bz.backstabbingPlayerCount(b.US);
            ourEdges = bz.getEdgeCount(b.US);
            for(int p = 0; p < b.getPlayerCount(); p++) {
                if(p == b.US) {
//                    ourCount = bz.getPlayerTileCount(p);
                    BoardUtil.AvailableSpace as = new BoardUtil.AvailableSpace(b, bz, b.US);
                    ourCount = as.getMaxMoves();
                    ourWideCount = bz.getSpaceIncNeighbourSpace(b.US);
                } else {
                    bz.getPlayerTileCount(p);
                    counts.add(bz.getPlayerTileCount(p));
//                    BoardUtil.AvailableSpace as = new BoardUtil.AvailableSpace(b, bz, p);
//                    counts.add(as.getMaxMoves());
                    wideCounts.add(bz.getSpaceIncNeighbourSpace(p));
                }
            }
            while(counts.size() < 3) {
                counts.add(0);
                wideCounts.add(0);
            }
            for(int c : counts) {
                enemySpace += c;
            }
        }

        public static String[]  csvHeader() {
            return new String[] {"turns", "enemyTurns", "space", "edges", "wide", "bordering", "backstabbing", "alive", "enemySpace", "endPoints"};
        }

        public String[] toArray() {
            String[] array = new String[DATA_COUNT];
            array[0] = Integer.toString(turnsRemaining);
            array[1] = Integer.toString(avrEnemyTurnsRemaining);
            array[2] = Integer.toString(ourCount);
            array[3] = Integer.toString(ourEdges);
            array[4] = Integer.toString(ourWideCount);
            array[5] = Integer.toString(borderingPlayerCount);
            array[6] = Integer.toString(backstabbingPlayerCount);
            array[7] = Integer.toString(alivePlayers);
            array[8] = Integer.toString(enemySpace);
            array[9] = Integer.toString(endPoints);
//            Collections.sort(counts);
//            Collections.sort(wideCounts);
//            counts.addAll(wideCounts);
//            for(int i = 0; i < counts.size(); i++) {
//                array[DATA_COUNT - i - 1] = Integer.toString(counts.get(i));
//            }
            return array;
        }
    }

    public static List<MoveStat> statsForGame(JsonNode singleGame) {
        int width = 30;
        int height = 20;
        singleGame = singleGame.get("success");
        int playerCount = singleGame.get("agents").size();
        int us = 0;
        for(JsonNode n : singleGame.get("agents")) {
            if(n.get("codingamer").get("userValid").asBoolean() && n.get("codingamer").get("pseudo").asText().equals(USERNAME)) {
                us = n.get("index").asInt();
            }
        }
        Board b = new Board(width, height, playerCount, us);
        String initialPositions = singleGame.get("frames").get(0).get("view").asText();
        String[] initial = initialPositions.split("\n");
        for(int p = 0; p < playerCount; p++) {
            String[] xy = initial[3*p+3].split(" ");
            b.move(p, Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
        }
        int currentPlayer = -1;
        boolean first = true;
        int[] moveCounts = new int[playerCount];
        int totalMoves = 0;
        int placement = playerCount;
        boolean gameOver = false;
        for(JsonNode n : singleGame.get("frames")) {
            boolean deathOccured = false;
            if(first) {
                // Need to skip the first one.
                first = false;
                continue;
            }
            currentPlayer  = n.get("agentId").asInt();
            moveCounts[currentPlayer]++;
            if(n.get("stdout") == null) {
                b.suicide(currentPlayer);
                deathOccured = true;
            } else {
                String stdout = n.get("stdout").asText().trim();
                if(!EnumUtils.isValidEnum(Direction.class, stdout)) {
                    b.suicide(currentPlayer);
                    deathOccured = true;
                } else {
                    Direction direction = Direction.valueOf(stdout);
                    try {
                        b.move(currentPlayer, b.tileFrom(b.playerTile(currentPlayer), direction));
                    } catch(IndexOutOfBoundsException e) {
                        System.out.println("Invalid move. Caught exception");
                        b.suicide(currentPlayer);
                        deathOccured = true;
                    }
                }
            }
            if(deathOccured) {
                if(currentPlayer == b.US) {
                    gameOver = true;
                } else if(!gameOver) {
                    placement--;
                }
            }
            totalMoves++;
        }
        int gameScore = 110 - placement * 10;
        List<MoveStat> ms = new ArrayList<>();
        b.undoMove();
        int step = 0;
        Board.Move m;
        int[] movesLeft = new int[playerCount];
        assert currentPlayer != -1 : "There should be at least one frame in the game.";
//        while(step < moveCounts[b.US]) {
        while(movesLeft[b.US] < moveCounts[b.US]) {
            if (currentPlayer == b.US) {
//                System.out.println("Step/Count: " + step + "/" + ourMoveCount);
                MoveStat s = new MoveStat(b, movesLeft, gameScore);
                ms.add(s);
//                s.turnsRemaining = step++;
            }
            movesLeft[currentPlayer]++;
            m = b.undoMove();
            if (m != null) {
                currentPlayer = m.getPlayer();
            }
        }
        return ms;
    }

    public static void generateStats(InputStream input, OutputStream out) throws IOException {
        ObjectMapper m = new ObjectMapper();
        m.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        JsonNode gameList = m.readTree(input);
        assert gameList.isArray();
        List<MoveStat> moveStats = new ArrayList<>();
        for(JsonNode game : gameList) {
            List<MoveStat> movesInOneGame = statsForGame(game);
            moveStats.addAll(movesInOneGame);
        }
//        m.writeValue(out, moveStats);
        CSVWriter wr = new CSVWriter(new OutputStreamWriter(out));
        wr.writeNext(MoveStat.csvHeader());
        for(MoveStat ms : moveStats) {
//            if(ms.borderingPlayerCount > 0) {
                wr.writeNext(ms.toArray());
//            }
        }
        wr.close();
    }

    public static void main(String[] args) throws IOException {
        InputStream inStream = Replay.class.getClassLoader().getResource("out3.json").openStream();
//        InputStream inStream = Files.newInputStream(p);
//        Path po = Paths.get(args[1]);
        OutputStream outStream = Files.newOutputStream(Paths.get("dataout.json"), StandardOpenOption.CREATE);
        generateStats(inStream, outStream);
    }
}
