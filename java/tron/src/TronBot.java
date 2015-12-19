import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Everything must be in the same file for- submission.
class Player {

    public static void main(String[] args) {
        int width = 30;
        int height = 20;
//        Driver driver = new DeadDriver();
//        Driver driver = new StaySafeDriver();
//        Driver driver  = new WallHuggingDriver();
        Driver driver = new VoronoiMinMax();
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
    private Scanner in;

    public InputParser() {
    }

    public Board init(int width, int height) {
        in = new Scanner(System.in);
        int playerCount = in.nextInt();
        int us = in.nextInt();
        Board b = new Board(width, height, playerCount, us);
        boolean startup = true;
        update(b, startup);
        return b;
    }

    public void update(Board current) {
        if(!in.hasNext()) {
            in = new Scanner(System.in);
        }
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
//                System.err.println("Moving: " + i + " to: (" + x1 + ", " + y1 + ")");
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

    public static boolean isAloneInComponent(Board b) {
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> marked = new HashSet<>();
        queue.add(b.ourTile());
        boolean foundOpponent = false;
        while(!queue.isEmpty() && !foundOpponent) {
            int next = queue.poll();
            System.err.println(next);
            for(int n : b.neighbours(next).boxed().collect(Collectors.toList())) {
                if(!marked.contains(n)) {
                    for(int i = 0; i < b.getPlayerCount(); i++) {
                        if(i != b.US && n == b.playerTile(i)) {
                            foundOpponent = true;
                            break;
                        }
                    }
                    if(b.isFree(n)) {
                        queue.add(n);
                        marked.add(n);
                    }
                }
            }
        }
        return !foundOpponent;
    }

    public static boolean[] battlefield(Board b) {
        return battlefield(b, true);
    }

    public static boolean[] battlefield(Board b, boolean inclusive) {
        boolean[] battlefield = new boolean[b.height*b.width];
        int[][] playerDistances = playerDistances(b);
        int battlefieldCount = 0;
        for(int i = 0; i < b.width*b.height; i++) {
            if(!b.isFree(i)) {
                continue;
            }
            int ourDist = playerDistances[b.US][i];
            for(int p : b.getAlivePlayers()) {
                if(p == b.US) continue;
                // Zero distance means it is not possible to reach that tile.
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
                int threshold = inclusive ? 0 : 1;
                if(diff >= threshold && diff <= 1) {
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
        for(int p : b.getAlivePlayers()) {
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
            b.freeNeighbours(next).filter(n -> distances[n] == 0)
                    .forEach(n -> {
                        distances[n] = distances[next] + 1;
                        q.add(n);
                    });
            }
        return distances;
    }

    private static class PlayerTilePair {
        int player;

        public PlayerTilePair(int player, int tile) {
            this.player = player;
            this.tile = tile;
        }

        int tile;
    }

    /**
     * Calculates the space reachable by each player without crossing the battlefield. Faster than previous method
     * as it calculates the battlefield implicitly while counting spaces.
     */
    public static int[] playerZoneCounts(Board b) {
        int[][] playerDistances = new int[b.getPlayerCount() + 1][b.width*b.height];
        int[] positionCounts = new int[b.getPlayerCount()];
        final int MIN_INDEX = b.getPlayerCount();
        int longestDistance = 0;
        Queue<PlayerTilePair> queue = new LinkedList<>();
        for(int p : b.getAlivePlayers()) {
            queue.add(new PlayerTilePair(p, b.playerTile(p)));
        }
        while(!queue.isEmpty()) {
            PlayerTilePair playerPos = queue.poll();
            b.freeNeighbours(playerPos.tile).filter((t) -> playerDistances[playerPos.player][t] == 0).forEach((t) -> {
                int dist = playerDistances[playerPos.player][playerPos.tile] + 1;
                // Note: The MIN_INDEX values will be overwitten if two players are equal distances. This allows us to count
                // the space once for each player. Thus this is a battlefield inclusive algorithm.
                if (playerDistances[MIN_INDEX][t] == 0 || dist <= playerDistances[MIN_INDEX][t]) {
                    playerDistances[playerPos.player][t] = dist;
                    playerDistances[MIN_INDEX][t] = dist;
                    positionCounts[playerPos.player]++;
                    queue.add(new PlayerTilePair(playerPos.player, t));
                }
            });
        }
        return positionCounts;
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
            for(int neighbour : b.freeNeighbours(fromPosition).boxed().collect(Collectors.toList())) {
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
                    final int tempCCID = ccID;
                    b.freeNeighbours(node).filter( n -> !(outOfBounds[n] || connectedComponents[n] != 0))
                            .forEach(n -> {
                            connectedComponents[n] = tempCCID;
                            bfsQueue.add(n);
                    });
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
    public static final int NOT_ON_BOARD = -1;
    public static final int DEAD = -2;
    private int[] floor;
    private int[] playerTile;
    private final int playerCount;
    private int aliveCount;
    private Stack<Move> moveHistory = new Stack<>();

    private class Move {
        private int player;
        private int tile;
        private int deadPlayerTile;
        private List<Integer> deadPlayerTrail;

        public Move(int player, int tile) {
            this(player, tile, false);
        }

        public Move(int player, int tile, boolean isDead) {
            this.player = player;
            this.tile = tile;
            if(isDead) {
                this.deadPlayerTile = playerTile(player);
                deadPlayerTrail = new ArrayList<>();
                for(int i = 0; i < getSize(); i++) {
                    if (getTileValue(i) == player) {
                        deadPlayerTrail.add(i);
                    }
                }
            }
        }

        public boolean wasKilled() {
            return deadPlayerTrail != null;
        }

        public List<Integer> getDeadPlayerTrial() {
            return deadPlayerTrail;
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
        this.playerCount = playerCount;
        this.aliveCount = playerCount;
        floor = new int[width * height];
        for(int i = 0; i < floor.length; i++) {
            floor[i] = EMPTY;
        }
        playerTile = new int[playerCount];
        // Set all players off the board.
        Arrays.fill(playerTile, NOT_ON_BOARD);
        US = us;
    }

    public Board(Board toCopy) {
        this(toCopy.width, toCopy.height, toCopy.playerCount, toCopy.US);
        this.floor = toCopy.floor;
        this.aliveCount = toCopy.aliveCount;
        this.playerTile = toCopy.playerTile;
        this.moveHistory = new Stack<>();
        this.moveHistory.addAll(toCopy.moveHistory);
    }


    public static Board fromString(String str) {
        String[] lines = str.split("\n");
        Scanner sc = new Scanner(str);
        sc.useDelimiter("(\\s+|h\\s+)");
        Set<Integer> players = new HashSet<>();
        int count = 0;
        while(sc.hasNext()) {
            if(!sc.hasNextInt()) {
                sc.next();
            } else {
                players.add(sc.nextInt());
            }
            count++;
        }
        int height = lines.length;
        int width = count / height;
        // We don't care which player number we are when displaying. So just use 0.
        Board b = new Board(width, height, players.size(), 0);
        Map<Integer, Integer> endPositions = new HashMap<>();
        sc = new Scanner(str);
        int tile = 0;
        while (sc.hasNext()) {
            if(sc.hasNextInt()) {
                b.move(sc.nextInt(), tile);
            } else {
                String next = sc.next();
                if(Character.isDigit(next.charAt(0))) {
                    int head = Integer.parseInt(next.substring(0, next.length()-1));
                    endPositions.put(head, tile);
                }
            }
            tile++;
        }
        for(Map.Entry<Integer, Integer> pos : endPositions.entrySet()) {
            b.move(pos.getKey(), pos.getValue());
        }
        return b;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getAliveCount() {
        return aliveCount;
    }

    public List<Integer> getAlivePlayers() {
        List<Integer> alivePlayers = new ArrayList<>();
        boolean finished = false;
        int p = US;
        while(!finished) {
            if(isAlive(p)) {
                alivePlayers.add(p);
            }
            p = (p + 1) % playerCount;
            if(p == US) {
                finished = true;
            }
        }
        return alivePlayers;
    }

    public boolean isAlive(int player) {
        return playerTile[player] != DEAD;
    }

    public void clear(int player) {
        for (int i = 0; i < floor.length; i++) {
            if (floor[i] == player) {
                floor[i] = EMPTY;
            }
        }
        playerTile[player] = DEAD;
        aliveCount--;
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
        System.err.println(String.format("Move to %s %s", x, y));
        int tile = xyToTile(x, y);
        if(playerTile(player) == tile) {
            return;
        }
        if(!isFree(tile)) {
            boolean isDead = true;
            moveHistory.push(new Move(player, playerTile(player), isDead));
            System.err.println("Cleared");
            clear(player);
        } else {
            moveHistory.push(new Move(player, playerTile(player)));
            playerTile[player] = tile;
            floor[tile] = player;
        }
    }

    public void move(int player, Position p) {
        move(player, p.getX(), p.getY());
    }

    public void move(int player, int tile) {
        move(player, tileToPos(tile));
    }

    public void undoMove() {
        Move lastMove = moveHistory.pop();
        if(lastMove.wasKilled()) {
            for(int i : lastMove.deadPlayerTrail) {
                floor[i] = lastMove.getPlayer();
            }
            aliveCount++;
        } else {
            int currentTile = playerTile(lastMove.getPlayer());
            floor[currentTile] = EMPTY;
        }
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
//            System.err.printf("Invalid tile: %d", tile);
            return -1;
        }
        return posToTile(next);
    }

    public IntStream neighbours(int tile) {
        Position pos = tileToPos(tile);
        return neighbours(pos.getX(), pos.getY());
    }

    public IntStream freeNeighbours(int tile) {
        Position pos = tileToPos(tile);
        return freeNeighbours(pos.getX(), pos.getY());
    }

    public IntStream freeNeighbours(int x, int y) {
        IntStream neighbours = neighbours(x, y);
        return neighbours.filter((i)->isFree(i));
    }

    public IntStream neighbours(int x, int y) {
        return IntStream.range(0, 4).map((i) -> {
            if (i == 0 && x - 1 >= 0) {
                return xyToTile(x - 1, y);
            } else if(i == 1 && x + 1 < width) {
                return xyToTile(x + 1, y);
            } else if(i == 2 && y - 1 >= 0) {
                return xyToTile(x, y - 1);
            } else if(i == 3 && y + 1 < height) {
                return xyToTile(x, y + 1);
            } else {
                return -1;
            }
        }).filter((i) -> i != -1);
    }

    public Position tileToPos(int tile) {
        if (tile > width * height - 1 || tile < 0) {
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
            if(!isValid) {
                System.err.println("A");
            }
            if(isValid && board.isFree(neighbourTile)) {
//                System.err.print("Moving to the " + d);
                System.err.println("B");
                direction = d;
                break;
            }
        }
        System.err.println(direction);
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
                    final int tempCCID = ccID;
                    board.freeNeighbours(next).filter(n -> board.isFree(n) && connectedComponents[n] == 0)
                            .forEach(n ->  {
                            connectedComponents[n] = tempCCID;
                            queue.add(n);
                        });
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
    private Filter deadEndFilter = new AvoidSmallComponents();
    @Override
    public Direction move(Board board) {
        Direction direction;
        Set<Direction> okayDirections = deadEndFilter.filterBadMoves(board, new HashSet<>(Arrays.asList(Direction.values())));
        for(Direction d : Direction.values()) {
            if(!okayDirections.contains(d)) {
                continue;
            }
            int tile = board.tileFrom(board.ourTile(), d);
            boolean isValid = tile != -1;
            if (isValid && board.isFree(tile)) {
                long neighbours = board.freeNeighbours(tile).count();
                if (neighbours < 3) {
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
            for(int n : board.freeNeighbours(next).boxed().collect(Collectors.toList())) {
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

    private Driver backupDriver = new WallHuggingDriver();
    private boolean backupEnabled = false;
    private int playerCount = 0;

    @Override
    public Direction move(Board board) {
        int depth = 4;
        Direction bestMove;
        // If someone dies, the board can open up with new space.
        if(playerCount != board.getAliveCount()) {
            backupEnabled = false;
            playerCount = board.getAliveCount();
        }
        if(backupEnabled || BoardUtil.isAloneInComponent(board)) {
            backupEnabled = true;
            bestMove = backupDriver.move(board);
        } else {
            bestMove = MinMax.minMax(board, countAvailableSpaces, depth);
        }
        return bestMove;
    }

    private MinMax.Score countAvailableSpaces = new MinMax.Score() {
        public int eval(Board b, int player) {
            int spaceCount;
            spaceCount = BoardUtil.playerZoneCounts(b)[b.US];
            return spaceCount;
        }

        public int eval2(Board b) {
            int[] playerSpaces = new int[b.getAliveCount()];
            boolean inclusive = false;
            boolean[] outOfBounds = BoardUtil.battlefield(b, inclusive);
            for(int p : b.getAlivePlayers()) {
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

}

class MinMax {

    public interface Score {
        int eval(Board b, int player);
    }

    private static class PosScore {
        int pos;
        int score;
        Board board;

        public PosScore(int pos, int score, Board b) {
            this.pos = pos;
            this.score = score;
            this.board = b;
        }
    }

    public static Direction minMax(Board b, Score score, int depth) {
        int currentStep = 0;
        int player = b.US;
        int position =  _minMax(b, score, depth, player, currentStep).pos;
        Direction bestDirection = b.tileToPos(b.ourTile()).directionTo(b.tileToPos(position));
        return bestDirection;
    }


    private static PosScore _minMax(Board b, Score score, int depth, int player, int currentStep) {
        int attempts = 0;
        while(!b.isAlive(player)) {
            player = (player + 1) % b.getPlayerCount();
            attempts++;
            assert attempts < b.getPlayerCount();
        }
        List<Integer> freeNeighbours = b.freeNeighbours(b.playerTile(player)).boxed().collect(Collectors.toList());
        if (currentStep == depth || freeNeighbours.isEmpty()) {
            return new PosScore(b.ourTile(), score.eval(b, player), new Board(b));
        }
        int max = 0;
        int maxPos = -1;
        int min = -1;
        int minPos = -1;
        Board maxBoard = null;
        boolean firstRun = true;
        for (int n : freeNeighbours) {
            b.move(player, n);
            // Adding one favours living longer in case all options end in death.
            PosScore posScore = _minMax(b, score, depth, (player + 1) % b.getPlayerCount(), currentStep + 1);
            int s = posScore.score;
            Board leafBoard = posScore.board;
//            int s = score.eval(leafBoard, player);
            b.undoMove();
            if (s < min || firstRun) {
                min = s;
                minPos = n;
            }
            if (s > max || firstRun) {
                max = s;
                maxPos = n;
                maxBoard = leafBoard;
            }
            firstRun = false;
        }
        boolean maximize = player == b.US;
        if (maximize) {
            assert maxPos != -1;
            return new PosScore(maxPos, max, maxBoard);
        } else {
            assert minPos != -1;
            return new PosScore(minPos, min, maxBoard);
        }
    }
}
