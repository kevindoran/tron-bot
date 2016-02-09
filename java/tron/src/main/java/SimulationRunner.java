import com.beust.jcommander.JCommander;
import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.beust.jcommander.Parameter;

/**
 * Created by Kevin on 2016/02/07.
 */
public class SimulationRunner {

    private final int BOARD_WIDTH = 30;
    private final int BOARD_HEIGHT = 20;
    private final Map<String, Driver> drivers = new HashMap<>();

    public SimulationRunner() {
        drivers.put("voronoiMinMax", new VoronoiMinMax());
        drivers.put("voronoi", new Voronoi());
        drivers.put("wallHugging", new WallHuggingDriver());
        drivers.put("staySafe", new StaySafeDriver());
        // chamberTreeMinMax
        // voronoiNMax
        // chamberTreeNMax
        // Regression a,b,c,d etc
    }
    private static class Arguments {
        @Parameter(names = "-c")
        private Integer gamesToPlay;

        @Parameter(names = "-o")
        private String outputFileName;

        @Parameter(names = "-bots", description = "Comma separated list of bot names.")
        private List<String> botNames = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException {
        Arguments parsedArgs = new Arguments();
        new JCommander(parsedArgs, args);
        SimulationRunner sr = new SimulationRunner();
        sr.run(parsedArgs.gamesToPlay, parsedArgs.outputFileName, parsedArgs.botNames);
    }

    public void run(int gamesToPlay, String fileName, List<String> botNames) throws IOException {
        List<SimPlayer> players = new ArrayList<>();
        for(int i = 0; i < botNames.size(); i++) {
            players.add(new SimPlayer(String.format("%d: %s", i, botNames.get(i)), drivers.get(botNames.get(i))));
        }
//        List<SimPlayer> players = IntStream.range(0, botNames.size()).mapToObj(
//                i -> new SimPlayer(String.format("%d: %s", i, botNames.get(i)), drivers.get(botNames.get(i))))
//                .collect(Collectors.toList());
        CSVWriter wr = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
        IntStream.range(0, gamesToPlay).forEach(i -> {
            System.out.println("Game no: " + i);
            Collections.shuffle(players);
            Simulation sim = new Simulation(BOARD_WIDTH, BOARD_HEIGHT, players);
            GameResult singleResult = sim.run();
            if (i == 0) {
                wr.writeNext(singleResult.csvHeader());
            }
            wr.writeNext(singleResult.toArray());
        });
        wr.close();
    }
}

