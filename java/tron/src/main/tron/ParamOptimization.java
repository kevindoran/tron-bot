import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

/**
 * Created by Kevin on 9/09/2016.
 */
public class ParamOptimization {
    private Random random = new Random();

    private String printScoreFactors(ScoreFactors sf) {
        StringBuilder sb = new StringBuilder();
        sb.append("Player space: ").append(sf.playerSpace).append("\n");
        sb.append("Player edges: ").append(sf.playerEdges).append("\n");
        sb.append("Shared space: ").append(sf.sharedSpace).append("\n");
        sb.append("Bordering players: ").append(sf.borderingPlayers).append("\n");
        sb.append("Backstabbing players: ").append(sf.backstabbingPlayers).append("\n");
        sb.append("Enemies: ").append(sf.enemies).append("\n");
        sb.append("Enemy space: ").append(sf.enemySpace).append("\n");
        sb.append("Death penalty: ").append(sf.deathPenalty).append("\n");
        return sb.toString();
    }

    private String printStats() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nQuantity stats:\n");
        sb.append(String.format("Player space: m(%f) sd(%f)\n", RegressionResult.playerSpaceStat.mean(), RegressionResult.playerSpaceStat.sd()));
        sb.append(String.format("Player edges: m(%f) sd(%f)\n", RegressionResult.playerEdgesStat.mean(), RegressionResult.playerEdgesStat.sd()));
        sb.append(String.format("Shared space: m(%f) sd(%f)\n", RegressionResult.sharedSpaceStat.mean(), RegressionResult.sharedSpaceStat.sd()));
        sb.append(String.format("Bordering players: m(%f) sd(%f)\n", RegressionResult.borderingPlayersStat.mean(), RegressionResult.borderingPlayersStat.sd()));
        sb.append(String.format("Backstabbing players: m(%f) sd(%f)\n", RegressionResult.backstabbingPlayersStat.mean(), RegressionResult.backstabbingPlayersStat.sd()));
        sb.append(String.format("Enemies: m(%f) sd(%f)\n", RegressionResult.enemiesStat.mean(), RegressionResult.enemiesStat.sd()));
        sb.append(String.format("Enemy space: m(%f) sd(%f)\n", RegressionResult.enemySpaceStat.mean(), RegressionResult.enemySpaceStat.sd()));
        return sb.toString();
    }

    private ScoreFactors generateScoreFactors() {
        ScoreFactors sf = new ScoreFactors();
        // Mean 119, SD: 71.2
        sf.playerSpace = (int) ((100.0/71.2) * (100 - random.nextInt(201)));
        // Mean: 438, SD: 273
        sf.playerEdges = (int) ((100.0/273.0) * (100 - random.nextInt(201)));
        // Mean: 401, SD: 149
        sf.sharedSpace = (int) ((100.0/149.0) * (100 - random.nextInt(201)));
        // Mean: 1.98, SD: 0.75
        sf.borderingPlayers = (int) ((100.0/0.75) * (100 - random.nextInt(201)));
        // Mean: 0.194, SD: 0.493 (clearly not normally distributed).
        sf.backstabbingPlayers = (int) ((100.0/0.493) * (100 - random.nextInt(201)));
        // Mean: 2.67, SD: 0.799
        sf.enemies = (int) ((100.0/0.779) * (100 - random.nextInt(201)));
        // Mean: 405, SD: 94.5
        sf.enemySpace = (int) ((100.0/94.5) * (100 - random.nextInt(201)));
        sf.deathPenalty = (int) random.nextInt(100000001);// Ten million.
        return sf;
    }

    private ScoreFactors startingFactors() {
        ScoreFactors sf = new ScoreFactors();
        sf.playerSpace = 98;
        sf.playerEdges = -13;
        sf.sharedSpace = 0;
        sf.borderingPlayers = -4400;
        sf.backstabbingPlayers = 2434;
        sf.enemies = -12066;
        sf.enemySpace = -41;
        sf.deathPenalty = 87461102;
        return sf;
    }

    private ScoreFactors randomAlter(ScoreFactors sf) {
        float sw = random.nextFloat();
        if(sw < 1.0/8.0) {
            sf.playerSpace = (int) ((100.0/71.2) * (100 - random.nextInt(201)));
        } else if(sw < 2.0/8.0) {
            sf.playerEdges = (int) ((100.0/273.0) * (100 - random.nextInt(201)));
        } else if(sw < 3.0/8.0) {
            sf.sharedSpace = (int) ((100.0/149.0) * (100 - random.nextInt(201)));
        } else if(sw < 4.0/8.0) {
            sf.borderingPlayers = (int) ((100.0/0.75) * (100 - random.nextInt(201)));
        } else if(sw < 5.0/8.0) {
            sf.backstabbingPlayers = (int) ((100.0/0.493) * (100 - random.nextInt(201)));
        } else if(sw < 6.0/8.0) {
            sf.enemies = (int) ((100.0/0.779) * (100 - random.nextInt(201)));
        } else if(sw < 7.0/8.0) {
            sf.enemySpace = (int) ((100.0/94.5) * (100 - random.nextInt(201)));
        } else {
            sf.deathPenalty = (int) random.nextInt(100000001);// Ten million.
        }
        return sf;
    }

    private double runGame(int gameCount, ScoreFactors sf) {
        List<SimPlayer> simPlayers = new ArrayList<>();
        simPlayers.add(new SimPlayer("US", new RegressionMaxN(sf)));
        simPlayers.add(new SimPlayer("E1", new VoronoiMinMax()));
        simPlayers.add(new SimPlayer("E2", new VoronoiMinMax()));
        simPlayers.add(new SimPlayer("E3", new VoronoiMinMax()));
        final int BOARD_WIDTH = 30;
        final int BOARD_HEIGHT = 20;
        double score = IntStream.range(0, gameCount).parallel().mapToDouble(c -> {
            Simulation sim = new Simulation(BOARD_WIDTH, BOARD_HEIGHT,
                    simPlayers);
            GameResult result = sim.run();
            double ourScore = (4+1) - result.getPlayerPositions().get("US");
            return ourScore;
        }).average().getAsDouble();
        return score;
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            while (true) {
                Job j = null;
                try {
                    j = jobQueue.take();
                } catch (InterruptedException e) {
                    System.err.println("Thread shutting down.");
                    return;
                }
                if(j.shutdown) {
                    System.err.println("Thread shutting down.");
                    return;
                }
                System.err.println("Starting job.");
                double score = runGame(gamesToAverage, j.config);
                double scoreDiff = -(score - j.currentScore);
                double flip = random.nextDouble();
                double acc = Math.exp(-scoreDiff / j.temp);
                boolean accepted = scoreDiff < 0 || acc > flip;
                Result res = new Result(j.config, score, accepted);
                resultQueue.offer(res);
                System.err.println("Finishing job.");
            }
        }
    }

    private static class Job {
        boolean shutdown;
        ScoreFactors config;
        double temp;
        double currentScore;

        public Job(boolean shutdown, ScoreFactors config, double temp, double
                currentScore) {
            this.shutdown = shutdown;
            this.config = config;
            this.temp = temp;
            this.currentScore = currentScore;
        }

        public static Job shutdownJob() {
            return new Job(true, null, 0, 0);
        }
    }

    private class Result {
        ScoreFactors config;
        double score;
        boolean accepted;

        public Result(ScoreFactors config, double score, boolean accepted) {
            this.config = config;
            this.score = score;
            this.accepted = accepted;
        }
    }

    private BlockingQueue<Result> resultQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Job> jobQueue = new LinkedBlockingQueue<>();

    double nextTemperature(double currentTemp, double kthSD, double lambda) {
        return currentTemp * Math.exp((-lambda * currentTemp) / kthSD);
    }

    private final int gamesToAverage = 5;
    private final int K = 10;
    private final double initialSD = 1;
    private final int neighborhoodSize = 20;//150;
    private final int tempReductions = 600;
    private final double lambda = 0.4f;
    private final double initialTemp = K * initialSD;
    private final double smoothingFactor = 0.7;
    private double finalScore;
    List<Thread> threads = new ArrayList<>();
    int threadCount = 32;
    ScoreFactors optimize() throws InterruptedException {
        int workerCount = (threadCount + 1) / gamesToAverage;
        for(int i = 0; i < threadCount; i++) {
            threads.add(new Thread(new Worker()));
        }
        for(int i = 0; i < threadCount; i++) {
            threads.get(i).start();
        }
        ScoreFactors current = generateScoreFactors();
        double currentScore = runGame(gamesToAverage, current);
        double bestScore = currentScore;
        ScoreFactors bestFactors = current;
        double prevTemp = initialTemp;
        double temp = initialTemp;
        double sdPrev = initialSD;
        for(int i = 0; i < tempReductions; i++) {
            int acceptCount = 0;
            int count = 1;
            double mean = currentScore;
            double delta = currentScore - 0.0;
            double M2 = 0.0 + delta*(currentScore - mean);
            List<Result> accepted = new ArrayList<>();
            for(int j = 0; j < neighborhoodSize;) {
                // Feed the workers.
                for(int y = 0; y < workerCount; y++) {
                    ScoreFactors altered = randomAlter(current);
                    Job job = new Job(false, altered, temp, currentScore);
                    jobQueue.offer(job);
                }
                for(int y = 0; y < workerCount; y++) {
                    Result res = resultQueue.take();
                    if(res.accepted) {
                        acceptCount++;
                        System.err.println("Master accepted update.");
                        accepted.add(res);
                    }
                    if(accepted.isEmpty()) {
                        ScoreFactors altered = randomAlter(current);
                        Job job = new Job(false, altered, temp, currentScore);
                        jobQueue.offer(job);
                    }
                    count++;
                    delta = res.score - mean;
                    mean += delta/count;
                    M2 += delta*(res.score - mean);
                    j++;
                }
                if(!accepted.isEmpty()) {
                    int randomIdx = random.nextInt(accepted.size());
                    current = accepted.get(randomIdx).config;
                    currentScore = accepted.get(randomIdx).score;
                    if(currentScore > bestScore) {
                        bestScore = currentScore;
                        bestFactors = current;
                    }
                    accepted.clear();
                }
                System.err.println("Round " + j + " of " + i);
            }
            System.err.println("M2: " + M2);
            double sd = Math.sqrt(M2) / (count - 1);
            System.err.println("Accept %: " + (double) acceptCount / (double) (count - 1) + " at temperature: " + temp + " and SD: " + sd);
            sd = (1-smoothingFactor) * sd + smoothingFactor * sdPrev*(temp / prevTemp);
            sdPrev = sd;
            prevTemp = temp;
            temp = nextTemperature(temp, sd, lambda);
            System.err.println("Current score: " + currentScore);
            System.err.println("Current score factors: " + printScoreFactors(current));
            System.err.println("Best score: " + bestScore);
            System.err.println("Best factors: " + printScoreFactors(bestFactors));
            System.err.println(printStats());
        }
        for(int i = 0; i < workerCount; i++) {
            jobQueue.offer(Job.shutdownJob());
        }
        for(int i = 0; i < workerCount; i++) {
            threads.get(i).join();
        }
        finalScore = currentScore;
        return current;
    }

    public static void main(String[] args) throws IOException,
            InterruptedException {
        ParamOptimization po = new ParamOptimization();
        ScoreFactors best = po.optimize();
        if (args.length > 1) {
            Files.write(Paths.get(args[1]), po.printScoreFactors(best)
                    .getBytes());
        } else {
            System.out.println("Final score: \n" + po.printScoreFactors(best));
        }
    }
}
