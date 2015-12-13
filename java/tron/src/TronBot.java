import java.util.*;
import java.util.stream.Collectors;

// Everything must be in the same file for- submission.
class Player {

    public static void main(String[] args) {
        int width = 30;
        int height = 20;
        // Driver driver = new DeadDriver();
        // Driver driver = new StaySafeDriver();
        // Driver driver  = new Voronoi();
        Driver driver  = new VoronoiMinMax();
        InputParser p = new InputParser();
        Board board = p.init(width, height);
        while (true) {
            Direction nextMove = driver.move(board);
            int tile = board.tileFrom(board.ourTile(), nextMove);
            assert board.isFree(tile);
            System.out.println(nextMove.toString());
            p.update(board);
        }
    }
}

enum Direction {
    RIGHT(1, 0),
    LEFT(-1, 0),
    UP(0, -1),
    DOWN(0, 1);

    private int dx;
    private int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public static Direction toDirection(int dx, int dy) {
        assert Math.abs(dx) <= 1 && Math.abs(dy) <= 1;
        if(dx == 1 && dy == 0) {
            return RIGHT;
        } else if(dx == -1 && dy == 0) {
            return LEFT;
        } else if (dx == 0 && dy == 1) {
            return DOWN;
        } else {
            return UP;
        }
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    @Override
    public String toString() {
        return this.name();
    }
}

class InputParser {
    private Scanner in = new Scanner(System.in);

    public InputParser() {
    }

    public Board init(int width, int height) {
        int playerCount = in.nextInt();
        int us = in.nextInt();
        Board b = new Board(width, height, playerCount, us);
        boolean startup = true;
        update(b, startup);
        return b;
    }

    public void update(Board current) {
        boolean startup = false;
        update(current, startup);
    }

    public void update(Board current, boolean startup) {
        // On startup, the first two integer inputs have already been consumed. If not startup, throw them away.
        if (!startup) {
            // These are never used after startup.
            // TODO: does the player count decrease when a player dies?
            int playerCount = in.nextInt();
            int us = in.nextInt();
        }
        for (int i = 0; i < current.getPlayerCount(); i++) {
            int x0 = in.nextInt();
            int y0 = in.nextInt();
            int x1 = in.nextInt();
            int y1 = in.nextInt();
            // People may have already moved since your turn, so best to mark their origins before moving them again.
            if (startup) {
                current.move(i, x0, y0);
            }
            // A player is dead if it has -1 for a coordinate.
            if (x0 == -1) {
                current.clear(i);
            } else {
                System.err.println("Moving: " + i + " to: (" + x1 + ", " + y1 + ")");
                // Board counts players starting at 1.
                current.move(i, x1, y1);
            }
        }
    }
}

class BoardUtil {

    public static int availableSpaces(Board b, int player) {
        boolean [] noOutOfBounds = new boolean[b.getSize()];
        return availableSpaces(b, player, noOutOfBounds);
    }

    public static int availableSpaces(Board b, int player, boolean[] outOfBounds) {
        ConnectedComponents cc = new ConnectedComponents(b, b.playerTile(player), outOfBounds);
        return cc.getMaxMoves();
    }

    public static boolean[] battlefield(Board b) {
        boolean[] battlefield = new boolean[b.height*b.width];
        int[][] playerDistances = playerDistances(b);
        int battlefieldCount = 0;
        for(int i = 0; i < b.width*b.height; i++) {
            if(!b.isFree(i)) {
                continue;
            }
            int ourDist = playerDistances[b.US][i];
            for(int p = 0; p < b.getPlayerCount(); p++) {
                if(p == b.US) continue;
                // Zero distance means it is not possible to reach that position.
                if(ourDist == 0 || playerDistances[p][i] == 0) {
                    continue;
                }
                // If the player distances are the same or differ by one. The difference of one is needed when
                // the players are on opposite type squares (imagine a checker board), then there are no squares they
                // can both reach in the same number of moves.
                // Edit, choose the further edge in the case when players are on different color
                // tiles.
//                if(Math.abs(ourDist - playerDistances[p][i]) <= 1) {
                int diff = ourDist - playerDistances[p][i];
                if(diff == 0 || diff == 1) {
                    battlefieldCount++;
                    battlefield[i] = true;
                }
            }
        }
        if(battlefieldCount == 0) {
            return null;
        }
        return battlefield;
    }

    public static int[][] playerDistances(Board b) {
        int[][] playerDistances = new int[b.getPlayerCount()][b.width*b.height];
        for(int p = 0; p < b.getPlayerCount(); p++) {
            playerDistances[p] = getDistancesFrom(b, p);
        }
        return playerDistances;
    }

    public static int[] getDistancesFrom(Board b, int player) {
        int[] distances = new int[b.height*b.width];
        Queue<Integer> q = new LinkedList<>();
        q.add(b.playerTile(player));
        while(!q.isEmpty()) {
            int next = q.poll();
            for(int n : b.freeNeighbours(next)) {
                if(distances[n] == 0) {
                    distances[n] = distances[next] + 1;
                    q.add(n);
                }
            }
        }
        return distances;
    }

    public static class ConnectedComponents {
        private int connectedComponents[];
        private Map<Integer, Integer> whiteCount = new HashMap<>();
        private Map<Integer, Integer> blackCount = new HashMap<>();
        private int ccCount = 0;
        private Board b;

        public ConnectedComponents(Board b, int fromPosition,  boolean[] outOfBounds) {
            this.b = b;
            connectedComponents = new int[b.getSize()];
            int ccBlackCount = 0;
            int ccWhiteCount = 0;
            int ccID = 1;
            for(int neighbour : b.freeNeighbours(fromPosition)) {
                // If node is not free or already part of another component, skip.
                if(outOfBounds[neighbour] || connectedComponents[neighbour] != 0) {
                    continue;
                }
                Queue<Integer> bfsQueue = new LinkedList<>();
                connectedComponents[neighbour] = ccID;
                bfsQueue.add(neighbour);
                while(!bfsQueue.isEmpty()) {
                    int node = bfsQueue.poll();
                    if(b.tileToPos(node).isBlack()) {
                        ccBlackCount++;
                    }
                    else {
                        ccWhiteCount++;
                    }
                    for(int n : b.freeNeighbours(node)) {
                        if(outOfBounds[n] || connectedComponents[n] != 0) {
                            continue;
                        }
                        connectedComponents[n] = ccID;
                        bfsQueue.add(n);
                    }
                }
                whiteCount.put(ccID, ccWhiteCount);
                blackCount.put(ccID, ccBlackCount);
                ccWhiteCount = 0;
                ccBlackCount = 0;
                ccID++;
            }
            ccCount = ccID - 1;
        }

        public int[] getConnectedComponents() {
            return connectedComponents;
        }

        public int getMaxMoves() {
            int max = 0;
            for(int i = 1; i <=getComponentCount(); i++) {
                int ccMax = getMaxMoves(i);
                if(ccMax > max) {
                    max = ccMax;
                }
            }
            return max;
        }

        public int getMaxMoves(int componentID) {
            int wc = whiteCount.get(componentID);
            int bc = blackCount.get(componentID);
            int min = Math.min(wc, bc);
            int maxMoves = min*  2;
            if(wc < bc && !b.tileToPos(b.ourTile()).isBlack()) {
                maxMoves++;
            } else if(bc < wc && b.tileToPos(b.ourTile()).isBlack()) {
                maxMoves++;
            }
            return maxMoves;
        }

        public int getComponentCount() {
            return ccCount;
        }
    }
}

class Board {
    public final int US;
    public final int width;
    public final int height;
    // Zero marks empty tiles;
    public static final int EMPTY = -2;
    private static final int NOT_ON_BOARD = -1;
    private int[] floor;
    private int[] playerTile;
    private Stack<Move> moveHistory = new Stack<>();

    private class Move {
        private int player;
        private int tile;

        public Move(int player, int tile) {
            this.player = player;
            this.tile = tile;
        }

        public int getPlayer() {
            return player;
        }

        public int getTile() {
            return tile;
        }
    }

    public Board(int width, int height, int playerCount, int us) {
        this.width = width;
        this.height = height;
        floor = new int[width * height];
        for(int i = 0; i < floor.length; i++) {
            floor[i] = EMPTY;
        }
        playerTile = new int[playerCount];
        // Set all players off the board.
        Arrays.fill(playerTile, NOT_ON_BOARD);
        US = us;
    }

    public int getPlayerCount() {
        return playerTile.length;
    }

    public void clear(int player) {
        for (int i = 0; i < floor.length; i++) {
            if (floor[i] == player) {
                floor[i] = EMPTY;
            }
        }
    }

    public int playerTile(int player) {
        return playerTile[player];
    }

    public int ourTile() {
        return playerTile[this.US];
    }

    public int getTileValue(int tile) {
        return floor[tile];
    }

    public void move(int player, int x, int y) {
        moveHistory.push(new Move(player, playerTile(player)));
        int tile = xyToTile(x, y);
        playerTile[player] = tile;
        System.err.printf("Player %d moving to (%d, %d) aka %d\n", player, x, y, tile);
        floor[tile] = player;
    }

    public void move(int player, Position p) {
        move(player, p.getX(), p.getY());
    }

    public void move(int player, int tile) {
        move(player, tileToPos(tile));
    }
    public void undoMove() {
        Move lastMove = moveHistory.pop();
        int currentTile = playerTile(lastMove.getPlayer());
        floor[currentTile] = EMPTY;
        playerTile[lastMove.getPlayer()] = lastMove.getTile();
    }

    public boolean isValid(int x, int y) {
        boolean isValid = x >= 0 && x < width && y >= 0 && y < height;
        return isValid;
    }

    public boolean isValid(Position p) {
        return isValid(p.getX(), p.getY());
    }

    public boolean isFree(int tile) {
        return getTileValue(tile) == EMPTY;
    }

    public int tileFrom(int tile, Direction d) {
        Position now = tileToPos(tile);
        Position next = new Position(now.getX() + d.getDx(), now.getY() + d.getDy());
        if (!isValid(next)) {
            System.err.printf("Invalid tile: %d", tile);
            return -1;
        }
        return posToTile(next);
    }

    public Set<Integer> neighbours(int tile) {
        Position pos = tileToPos(tile);
        return neighbours(pos.getX(), pos.getY());
    }

    public Set<Integer> freeNeighbours(int tile) {
        Position pos = tileToPos(tile);
        return freeNeighbours(pos.getX(), pos.getY());
    }

    public Set<Integer> freeNeighbours(int x, int y) {
        Set<Integer> allNeighbours = neighbours(x, y);
        Iterator<Integer> it = allNeighbours.iterator();
        while(it.hasNext()) {
            Integer next = it.next();
            if(!isFree(next)) {
                it.remove();
            }
        }
        return allNeighbours;
    }

    public Set<Integer> neighbours(int x, int y) {
        Set<Integer> neighbours = new HashSet<>();
        if (x - 1 >= 0) {
            neighbours.add(xyToTile(x - 1, y));
        }
        if (x + 1 < width) {
            neighbours.add(xyToTile(x + 1, y));
        }
        if (y - 1 >= 0) {
            neighbours.add(xyToTile(x, y - 1));
        }
        if (y + 1 < height) {
            neighbours.add(xyToTile(x, y + 1));
        }
        return neighbours;
    }

    public Position tileToPos(int tile) {
        if (tile > width * height - 1) {
            throw new IndexOutOfBoundsException();
        }
        int x = tile % width;
        int y = tile / width;
        return new Position(x, y);
    }

    public int xyToTile(int x, int y) {
        if(!isValid(x, y)) {
            throw new IndexOutOfBoundsException();
        }
        int tile = x + width * y;
        return tile;
    }

    public int getSize() {
        return height * width;
    }

    public int posToTile(Position p) {
        return xyToTile(p.getX(), p.getY());
    }
}

class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Direction directionTo(Position p) {
        // Throw exception instead.
        assert Math.abs(this.x - p.x) <= 1 && Math.abs(this.y - p.y) <= 1;
        int dx = p.getX() - this.x;
        int dy = p.getY() - this.y;
        return Direction.toDirection(dx, dy);
    }

    /**
     * Squares are either black or white. The first square (0, 0) is black. The color is used for
     * such things like finding out how many possible moves their are left in an area of the board.
     */
    public boolean isBlack() {
        boolean isBlack = (x + y) % 2 == 0;
        return isBlack;
    }
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

interface Driver {
    Direction move(Board board);
}

class DeadDriver implements Driver {
    @Override
    public Direction move(Board board) {
        return Direction.LEFT;
    }
}

// Places around 700
class StaySafeDriver implements Driver {
    @Override
    public Direction move(Board board) {
        // Choose a default in case there are no safe directions.
        Direction direction = Direction.LEFT;
        for (Direction d : Direction.values()) {
            int neighbourTile = board.tileFrom(board.ourTile(), d);
            boolean isValid = neighbourTile != -1;
            if(isValid && board.isFree(neighbourTile)) {
                System.err.print("Moving to the " + d);
                direction = d;
                break;
            }
        }
        return direction;
    }
}

interface Filter {
    Set<Direction> filterBadMoves(Board b, Set<Direction> moves);
}

class AvoidSmallComponents implements Filter {

    @Override
    public Set<Direction> filterBadMoves(Board board, Set<Direction> moves) {
        int[] connectedComponents = new int[board.width*board.height];
        Map<Integer, Integer> componentSize = new HashMap<>();
        componentSize.put(0, 0);
        int maxComponentSize = 0;
        int ccID = 1;
        Set<Integer> possibleNeighbours = moves.stream().map(
                (m) -> board.tileFrom(board.ourTile(), m)).filter(
                (tile) -> tile != -1).collect(Collectors.toSet());
        for(int i : possibleNeighbours) {
            if (board.isFree(i) && connectedComponents[i] == 0) {
                Queue<Integer> queue = new LinkedList<>();
                componentSize.put(ccID, 0);
                queue.add(i);
                connectedComponents[i] = ccID;
                while (!queue.isEmpty()) {
                    int next = queue.poll();
                    componentSize.put(ccID, componentSize.get(ccID) + 1);
                    for (int n : board.freeNeighbours(next)) {
                        if (board.isFree(n) && connectedComponents[n] == 0) {
                            connectedComponents[n] = ccID;
                            queue.add(n);
                        }
                    }
                }
                assert  componentSize.get(ccID) != 0;
                if(componentSize.get(ccID) > maxComponentSize) {
                    maxComponentSize = componentSize.get(ccID);
                }
                ccID++;
            }
        }
        // Lambdas require variables to be declared final.
        final int maxSize = maxComponentSize;
        moves = moves.stream().filter((e) ->  {
            int toTile = board.tileFrom(board.ourTile(), e);
            // The tile is not on the board if toTile == -1.
            if(toTile == -1) {
                return false;
            }
            boolean inLargestComponent = componentSize.get(connectedComponents[toTile]) == maxSize;
            return inLargestComponent;
        }).collect(Collectors.toSet());
        return moves;
    }
}

class WallHuggingDriver implements Driver {
    private StaySafeDriver backupDriver = new StaySafeDriver();

    @Override
    public Direction move(Board board) {
        Direction direction;
        for(Direction d : Direction.values()) {
            int tile = board.tileFrom(board.ourTile(), d);
            boolean isValid = tile != -1;
            if (isValid && board.isFree(tile)) {
                Set<Integer> neighbours = board.freeNeighbours(tile);
                if (neighbours.size() < 3) {
                    // It's next to a wall!
                    return d;
                }
            }
        }
        // No free wall tiles. Revert to StaySafeDriver.
        return backupDriver.move(board);
    }
}

// Places around 350
class Voronoi implements Driver {
    private Driver backupDriver = new WallHuggingDriver();
    private Filter connectedComponentChooser = new AvoidSmallComponents();
    private boolean backupEnabled = false;
    private Random rand = new Random();
    @Override
    public Direction move(Board board) {
        Set<Direction> allDirections = new HashSet<>();
        Collections.addAll(allDirections, Direction.values());
        Set<Direction> directions = connectedComponentChooser.filterBadMoves(board, allDirections);
        List<Integer> path = pathToBattle(board);
        if(backupEnabled || path.size() == 0) {
            System.err.println("Backup enabled");
            backupEnabled = true;
            return backupDriver.move(board);
        }
        Position nextPoint = board.tileToPos(path.get(0));
        Direction move =  board.tileToPos(board.ourTile()).directionTo(nextPoint);
        if(!directions.contains(move)) {
            move = new ArrayList<>(directions).get(rand.nextInt(directions.size()));
        }
        return move;
    }

    // Depth first search is currently done a second time in this algorithm. Room for improvement.
    public List<Integer> pathToBattle(Board board) {
        boolean[] marked = new boolean[board.height*board.width];
        int[] pathTo = new int[board.height*board.width];
        List<Integer> pathToBF = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        boolean[] battlefield = BoardUtil.battlefield(board);
        if(battlefield == null) {
            return pathToBF;
        }
        marked[board.ourTile()] = true;
        queue.add(board.ourTile());
        int closestBattlefield = -1;
        while(!queue.isEmpty() && closestBattlefield == -1) {
            int next = queue.poll();
            for(int n : board.freeNeighbours(next)) {
                if(!marked[n]) {
                    marked[n] = true;
                    pathTo[n] = next;
                    if(battlefield[n]) {
                        closestBattlefield = n;
                        break;
                    }
                    queue.add(n);
                }
            }
        }
        assert closestBattlefield != -1;
        for(int i = closestBattlefield; i != board.ourTile(); i = pathTo[i]) {
            pathToBF.add(i);
        }
        Collections.reverse(pathToBF);
        return pathToBF;
    }
}

class VoronoiMinMax implements Driver {

    private MinMax.Score countAvailableSpaces = new MinMax.Score() {
        public int eval(Board b) {
            boolean[] outOfBounds = BoardUtil.battlefield(b);
            int spaceCount;
            if(outOfBounds == null) {
                spaceCount = BoardUtil.availableSpaces(b, b.US);
            } else {
                spaceCount = BoardUtil.availableSpaces(b, b.US, outOfBounds);
            }
            return spaceCount;
        }

        public int eval2(Board b) {
            int[] playerSpaces = new int[b.getPlayerCount()];
            boolean[] outOfBounds = BoardUtil.battlefield(b);
            for(int p = 0; p < b.getPlayerCount(); p++) {
                int spaceCount;
                if(outOfBounds == null) {
                    spaceCount = BoardUtil.availableSpaces(b, p);
                } else {
                    spaceCount = BoardUtil.availableSpaces(b, p, outOfBounds);
                }
            }
            return comparativeSurplus(b, playerSpaces);
        }

        public int comparativeSurplus(Board b, int[] playerSpaces) {
            int ourSpaces = playerSpaces[b.US];
            int surplus = 0;
            for(int p : playerSpaces) {
                if(p == b.US) {
                    continue;
                }
                surplus += ourSpaces - playerSpaces[p];
            }
            return surplus;
        }
    };

    @Override
    public Direction move(Board board) {
        int depth = 3;
        Direction bestMove = MinMax.minMax(board, countAvailableSpaces, depth);
        return bestMove;
    }
}

class MinMax {

    public interface Score {
        int eval(Board b);
    }

    private static class PosScore {
        int pos;
        int score;

        public PosScore(int pos, int score) {
            this.pos = pos;
            this.score = score;
        }
    }

    public static Direction minMax(Board b, Score score, int depth) {
        int position =  _minMax(b, score, depth).pos;
        Direction bestDirection = b.tileToPos(b.ourTile()).directionTo(b.tileToPos(position));
        return bestDirection;
    }

    private static PosScore _minMax(Board b, Score score, int depth) {
        int currentStep = 0;
        return _minMax(b, score, depth, currentStep);
    }

    private static PosScore _minMax(Board b, Score score, int depth, int currentStep) {
        int player = b.US + currentStep % b.getPlayerCount();
        Set<Integer> freeNeighbours = b.freeNeighbours(b.playerTile(player));
        if (currentStep == depth || freeNeighbours.isEmpty()) {
            return new PosScore(b.ourTile(), score.eval(b));
        }
        int max = 0;
        int maxPos = -1;
        int min = -1;
        int minPos = -1;
        boolean firstRun = true;
        for (int n : freeNeighbours) {
            b.move(player, n);
            int s = _minMax(b, score, depth, currentStep + 1).score;
            b.undoMove();
            if (s < min || firstRun) {
                min = s;
                minPos = n;
            }
            if (s > max || firstRun) {
                max = s;
                maxPos = n;
            }
        }
        boolean maximize = player == b.US;
        if (maximize) {
            assert maxPos != -1;
            return new PosScore(maxPos, max);
        } else {
            assert minPos != -1;
            return new PosScore(minPos, min);
        }
    }
}
