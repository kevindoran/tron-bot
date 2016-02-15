import com.beust.jcommander.JCommander;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

import com.beust.jcommander.Parameter;

/**
 * Created by Kevin on 2016/02/07.
 */
public class SimulationRunner {

    private final int BOARD_WIDTH = 30;
    private final int BOARD_HEIGHT = 20;
    private final Map<String, Class<? extends Driver>> drivers = new HashMap<>();

    public SimulationRunner() {
        drivers.put("voronoiMaxN", VoronoiMaxN.class);
        drivers.put("voronoiMinMax", VoronoiMinMax.class);
        drivers.put("voronoi", Voronoi.class);
        drivers.put("wallHugging", WallHuggingDriver.class);
        drivers.put("staySafe", StaySafeDriver.class);
        drivers.put("avoidEnemy", AvoidEnemyDriver.class);
        drivers.put("voronoiMinMax7", VoronoiMinMax7.class);
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
        sr.run2(parsedArgs.gamesToPlay, parsedArgs.outputFileName, parsedArgs.botNames);
//        sr.runBatch(parsedArgs.gamesToPlay, parsedArgs.botNames);
    }

    public void runBatch(int gamesToPlay, List<String> botNames) throws IOException {
        List<SimPlayerFactory> players = createPlayers(botNames);
        IntStream.range(0, players.size()).parallel().forEach(i -> {
            IntStream.range(0, players.size()).filter(j -> j != i).forEach(j -> {
                // 2 player game.
                SimPlayerFactory subject = new SimPlayerFactory(botNames.get(i), "");
                SimPlayerFactory enemy = new SimPlayerFactory(botNames.get(j), "");
                SimPlayerFactory[] pz = new SimPlayerFactory[]{subject, enemy};
                try {
                    run(gamesToPlay, String.format("2p_%s_vs_%s.csv", botNames.get(i), botNames.get(j)), Arrays.asList(pz));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // 4 player game.
                pz = new SimPlayerFactory[]{
                        new SimPlayerFactory(botNames.get(i), ""),
                        new SimPlayerFactory(botNames.get(j), "1"),
                        new SimPlayerFactory(botNames.get(j), "2"),
                        new SimPlayerFactory(botNames.get(j), "3"),
                };
                try {
                    run(gamesToPlay, String.format("4p_%s_vs_%s.csv", botNames.get(i), botNames.get(j)), Arrays.asList(pz));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

    }

    // Type erasure prevents method overloading.
    public void run2(int gamesToPlay, String fileName, List<String> driverNames) throws IOException {
        List<SimPlayerFactory> playerFactories = createPlayers(driverNames);
        run(gamesToPlay, fileName, playerFactories);
    }

    public void run(int gamesToPlay, String fileName, List<SimPlayerFactory> playerFactories) throws IOException {
        CSVWriter wr = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
        IntStream.range(0, gamesToPlay).forEach(i -> {
            Collections.shuffle(playerFactories);
            Simulation sim = new Simulation(BOARD_WIDTH, BOARD_HEIGHT, playerFactories);
            GameResult singleResult = sim.run();
            if (i == 0) {
                wr.writeNext(singleResult.csvHeader());
            }
            wr.writeNext(singleResult.toArray());
            System.out.println("Game no: " + i); //+ "\n" + String.join(",", singleResult.toArray()));
        });
        wr.close();
    }

    private List<SimPlayerFactory> createPlayers(List<String> driverNames) {
//        List<SimPlayer> playerFactories = IntStream.range(0, driverNames.size()).mapToObj(
//                i -> new SimPlayer(String.format("%d: %s", i, driverNames.get(i)), drivers.get(driverNames.get(i))))
//                .collect(Collectors.toList());
        List<SimPlayerFactory> playerFactories = new ArrayList<>();
        for (int i = 0; i < driverNames.size(); i++) {
            playerFactories.add(new SimPlayerFactory(driverNames.get(i), "" + i));
        }
        return playerFactories;
    }

    public class SimPlayerFactory {
        private String driverName;
        private String playerNamePosfix;

        public SimPlayerFactory(String driverName, String playerNamePosfix) {
            this.driverName = driverName;
            this.playerNamePosfix = playerNamePosfix;
        }

        public SimPlayer create() {
            Class<? extends Driver> d = drivers.get(driverName);
            if(d == null) {
                throw new RuntimeException(String.format("Driver with name '%s' cannot be found", driverName));
            }
            try {
                return new SimPlayer(String.format("%s%s", driverName, playerNamePosfix), d.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}