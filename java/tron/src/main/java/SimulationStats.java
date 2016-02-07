import com.beust.jcommander.JCommander;
import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.beust.jcommander.Parameter;

/**
 * Created by Kevin on 2016/02/07.
 */
public class SimulationStats {

    private final int BOARD_WIDTH = 30;
    private final int BOARD_HEIGHT = 20;
    private static final Map<String, SimPlayer> simPlayers = new HashMap<>();
    private final SimPlayer voronoiMinMax = new SimPlayer("voronoiMinMax", new VoronoiMinMax());
    private final SimPlayer toTheBattlefield = new SimPlayer("toTheBattlefield", new Voronoi());
    private final SimPlayer wallHugging = new SimPlayer("wallHugging", new WallHuggingDriver());
    private final SimPlayer staySafe = new SimPlayer("staySafe", new StaySafeDriver());
    // chamberTreeMinMax
    // voronoiNMax
    // chamberTreeNMax
    // Regression a,b,c,d etc

    private static class Arguments {
        @Parameter
        private Integer gamesToPlay;

        @Parameter
        private String outputFileName;

        @Parameter(names = "-bots", description = "Space separated list of bot names.")
        private List<String> botNames = new ArrayList<>();
    }

    public void main(String[] args) throws IOException {
        Arguments parsedArgs = new Arguments();
        new JCommander(parsedArgs, args);
        run(parsedArgs.gamesToPlay, parsedArgs.outputFileName, parsedArgs.botNames);
    }

    public void run(int gamesToPlay, String fileName, List<String> botNames) throws IOException {
        List<SimPlayer> players = botNames.stream().map(s -> simPlayers.get(botNames)).collect(Collectors.toList());
        Simulation sim = new Simulation(BOARD_WIDTH, BOARD_HEIGHT, players);
        List<GameResult> gameResults = new ArrayList<>();
        for (int i = 0; i < gamesToPlay; i++) {
            GameResult singleResult = sim.run();
            gameResults.add(singleResult);
        }
        CSVWriter wr = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
        wr.writeNext(gameResults.get(0).csvHeader());
        for (GameResult gr : gameResults) {
            wr.writeNext(gr.toArray());
        }
        wr.close();
    }
}

