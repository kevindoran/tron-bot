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
import java.util.Collections;
import java.util.List;
/**
 * Created by Kevin on 2016/01/29.
 */
public class Replay {

    public static final String USERNAME = "CloudLeaper";
    public static class MoveStat {
        List<Integer> counts = new ArrayList<>();
        int ourCount = 0;
        int ourEdges;
        int borderingPlayerCount;
        int turnsRemaining;
        static final int DATA_COUNT = 7;

        public  MoveStat(Board b) {
            BoardUtil.BoardZones bz = new BoardUtil.BoardZones(b, b.US);
            borderingPlayerCount = bz.borderingPlayerCount(b.US);
            ourEdges = bz.getEdgeCount(b.US);
            for(int p = 0; p < b.getPlayerCount(); p++) {
                if(p == b.US) {
                    ourCount = bz.getPlayerTileCount(p);
                } else {
                    bz.getPlayerTileCount(p);
                    counts.add(bz.getPlayerTileCount(p));
                }
            }
            while(counts.size() < 3) {
                counts.add(0);
            }
        }

        public static String[]  csvHeader() {
            return new String[] {"turns", "space", "edges", "bordering", "enemy1", "enemy2", "enemy3"};
        }

        public String[] toArray() {
            String[] array = new String[DATA_COUNT];
            array[0] = Integer.toString(turnsRemaining);
            array[1] = Integer.toString(ourCount);
            array[2] = Integer.toString(ourEdges);
            array[3] = Integer.toString(borderingPlayerCount);
            Collections.sort(counts);
            for(int i = 0; i < counts.size(); i++) {
                array[DATA_COUNT - i - 1] = Integer.toString(counts.get(i));
            }
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
        int ourMoveCount = 0;
        int currentPlayer = -1;
        boolean first = true;
        int moveCount = 0;
        for(JsonNode n : singleGame.get("frames")) {
            if(first) {
                // Need to skip the first one.
                first = false;
                continue;
            }
            currentPlayer  = n.get("agentId").asInt();
            if(currentPlayer == b.US) {
                ourMoveCount++;
            }
            if(n.get("stdout") == null) {
                b.suicide(currentPlayer);
            } else {
                String stdout = n.get("stdout").asText().trim();
                if(!EnumUtils.isValidEnum(Direction.class, stdout)) {
                    b.suicide(currentPlayer);
                } else {
                    Direction direction = Direction.valueOf(stdout);
                    try {
                        b.move(currentPlayer, b.tileFrom(b.playerTile(currentPlayer), direction));
                    } catch(IndexOutOfBoundsException e) {
                        System.out.println("Invalid move. Caught exception");
                        b.suicide(currentPlayer);
                    }
                }
            }
            moveCount++;
        }

        List<MoveStat> ms = new ArrayList<>();
        b.undoMove();
        int step = 0;
        Board.Move m;
        assert currentPlayer != -1 : "There should be at least one frame in the game.";
        while(step < ourMoveCount) {
            if (currentPlayer == b.US) {
                System.out.println("Step/Count: " + step + "/" + ourMoveCount);
                MoveStat s = new MoveStat(b);
                ms.add(s);
                s.turnsRemaining = step++;
            }
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
            if(ms.borderingPlayerCount > 0) {
                wr.writeNext(ms.toArray());
            }
        }
        wr.close();
    }

    public static void main(String[] args) throws IOException {
        InputStream inStream = Replay.class.getClassLoader().getResource("out.json").openStream();
//        InputStream inStream = Files.newInputStream(p);
//        Path po = Paths.get(args[1]);
        OutputStream outStream = Files.newOutputStream(Paths.get("dataout.json"), StandardOpenOption.CREATE);
        generateStats(inStream, outStream);
    }
}
