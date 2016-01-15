import java.util.*;
import java.util.function.BiFunction;
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

    private class Input {
        private int x0;
        private int x1;
        private int y0;
        private int y1;
        private int player;

        public Input(int player, int x0, int y0, int x1, int y1) {
            this.player = player;
            this.x0 = x0;
            this.x1 = x1;
            this.y0 = y0;
            this.y1 = y1;
        }

        @Override
        public String toString() {
            return String.format("%d, %d, %d, %d", x0, y0, x1, y1);
        }
    }

    public void update(Board current, boolean startup) {
        // On startup, the first two integer inputs have already been consumed. If not startup, throw them away.
        if (!startup) {
            // These are never used after startup.
            // TODO: does the player count decrease when a player dies?
            int playerCount = in.nextInt();
            int us = in.nextInt();
        }
        Input[] input = new Input[current.getPlayerCount()];
        for (int i = 0; i < current.getPlayerCount(); i++) {
            int x0 = in.nextInt();
            int y0 = in.nextInt();
            int x1 = in.nextInt();
            int y1 = in.nextInt();
            input[i] = new Input(i, x0, y0, x1, y1);
        }
        // Need to clear any player first, as another player may be moving into their place, which must be marked as
        // 'free' ASAP.
        for(Input i : input) {
            if (i.x1 == -1) {
                current.clear(i.player);
            }
        }
        for(Input i : input) {
            if(i.x1 == -1) {
                continue;
            }
            // People may have already moved since your turn, so best to mark their origins before moving them again.
            if (startup) {
                current.move(i.player, i.x0, i.y0);
            }
            // A player is dead if it has -1 for a coordinate.
            if (i.x0 == -1) {
                current.clear(i.player);
            } else {
//                System.err.println("Moving: " + i + " to: (" + x1 + ", " + y1 + ")");
                // Board counts players starting at 1.
                current.move(i.player, i.x1, i.y1);
            }
        }
    }
}

class BoardUtil {

    public static int availableSpaces(Board b, int tile) {
        boolean [] noOutOfBounds = new boolean[b.getSize()];
        return availableSpaces(b, tile, noOutOfBounds);
    }

    public static int availableSpaces(Board b, int tile, boolean[] outOfBounds) {
        ConnectedComponents cc = new ConnectedComponents(b, tile, outOfBounds);
        return cc.getMaxMoves();
    }

    public static boolean isAloneInComponent(Board b) {
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> marked = new HashSet<>();
        queue.add(b.ourTile());
        boolean foundOpponent = false;
        while(!queue.isEmpty() && !foundOpponent) {
            int next = queue.poll();
            for(int n : b.neighbours(next)) {
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
            for (int n : b.freeNeighbours(next)) {
                if (distances[n] == 0) {
                    distances[n] = distances[next] + 1;
                    q.add(n);
                }
            }
        }
        return distances;
    }

    private static class PlayerTilePair {
        int player;
        // Take both tile and pos. They are both used frequently, to save converting between them by saving both.
        int tile;

        public PlayerTilePair(int player, int tile) {
            this.player = player;
            this.tile = tile;
        }

    }
    // Algorithm taken from
    // https://kartikkukreja.wordpress.com/2013/11/09/articulation-points-or-cut-vertices-in-a-graph/
//    public static int[] findCutVertices(Board b) {
    public static class CutVertices {

        private int[] parent;
        private int[] low;
        private int[] visitOrder;
        private int visited = 0;
        private boolean[] cutVirtices;
        private BoardZones boardZones;
//        private boolean[] battlefield;
        private Board b;

        public CutVertices(Board b, BoardZones zones) {
            this.b = b;
            boardZones = zones;
            low = new int[b.getSize()];
            parent = new int[b.getSize()];
//            battlefield = new boolean[b.getSize()];
            cutVirtices = new boolean[b.getSize()];
            visitOrder = new int[b.getSize()];
            assignLow();
            // Sometimes gets asserted, however, i think there are cases where it is okay, Such as neighbor is on
            // battlefront.
//            assert cutVirtices[b.ourTile()] == true || b.freeNeighbours(b.ourTile()).count() <= 1;
        }

        private void assignLow() {
            Stack<Integer> stack = new Stack<>();
            Stack<Integer> dfsStack = new Stack<>();
            stack.push(b.ourTile());
            parent[b.ourTile()] = b.ourTile();
            while(!stack.isEmpty()) {
                int node = stack.pop();
                if(visitOrder[node] != 0) {
                    continue;
                }
                visitOrder[node] = ++visited;
                low[node] = visitOrder[node];
                List<Integer> children = b.neighbours(node);
                for(int child : children) {
                    if((b.isFree(child) && boardZones.isInPlayerZone(b.US, child)) || child == b.ourTile()) {
                        if (visitOrder[child] == 0) {
                            parent[child] = node;
                            stack.push(child);
                            dfsStack.push(child);
                        } else if (parent[node] != child) {
                            low[node] = Math.min(low[child], low[node]);
                        }
                    }
                }
            }
            while(!dfsStack.isEmpty()) {
                Integer node = dfsStack.pop();
                assert low[node] > 0;
                low[parent[node]] = Math.min(low[parent[node]], low[node]);
                if (low[node] >= visitOrder[parent[node]]) {
                    cutVirtices[parent[node]] = true;
                }
            }
        }

        public boolean[] getCutVirtices() {
            return cutVirtices;
        }
    }

    /**
     * Calculates the space reachable by each player without crossing the battlefield. Faster than previous method
     * as it calculates the battlefield implicitly while counting spaces.
     */
    public  static class BoardZones {
        int[][] playerDistances;
        int[] playerTileCount;

        public BoardZones(Board b, int playersTurn) {
            playerDistances = new int[b.getPlayerCount()][b.getSize()];
            int[] whiteCount = new int[b.getPlayerCount()];
            int[] blackCount = new int[b.getPlayerCount()];
            int[] minDistance = new int[b.getSize()];
            Queue<PlayerTilePair> queue = new LinkedList<>();
            for (int p : b.getAlivePlayers(playersTurn)) {
                queue.add(new PlayerTilePair(p, b.playerTile(p)));
            }
            while (!queue.isEmpty()) {
                PlayerTilePair playerPos = queue.poll();
                for(int t : b.freeNeighbours(playerPos.tile)) {
                    if (minDistance[t] == 0) {
                        Position pos = b.tileToPos(t);
                        int dist = playerDistances[playerPos.player][playerPos.tile] + 1;
                        // If two players are the same distance, the one who's turn is first will claim the tile first. The tile
                        // will be within the reachable bound of the first player, but not the second. An explicit check isn't
                        // needed due to the order of the BFS. Assert here just to be sure.
//                boolean beatenByTurnOrder = dist == minDistance[t] && firstReachingPlayer[t] < playerPos.player;
//                if(beatenByTurnOrder) {
//                    throw new RuntimeException();
//                }
                        playerDistances[playerPos.player][t] = dist;
                        minDistance[t] = dist;
                        if (pos.isBlack()) {
                            blackCount[playerPos.player]++;
                        } else {
                            whiteCount[playerPos.player]++;
                        }
                        queue.add(new PlayerTilePair(playerPos.player, t));
                    }
                }
            }
            for (int player : b.getAlivePlayers(playersTurn)) {
                int wc = whiteCount[player];
                int bc = blackCount[player];
                int min = Math.min(wc, bc);
                int maxMoves = min * 2;
                if (wc < bc && !b.tileToPos(b.playerTile(player)).isBlack()) {
                    maxMoves++;
                } else if (bc < wc && b.tileToPos(b.playerTile(player)).isBlack()) {
                    maxMoves++;
                }
                // Reused the whitecount array to save space.
                whiteCount[player] = maxMoves;
            }
            playerTileCount = whiteCount;
        }

        public int getPlayerTileCount(int player) {
            return playerTileCount[player];
        }

        public boolean isInPlayerZone(int player, int tile) {
            return playerDistances[player][tile] != 0;
        }
    }

    public static class AvailableSpace {
        private Board board;
        private boolean[] visited;
        private boolean[] cutVertices;
        private Chamber rootChamber;
        private int maxMoves;
        private BoardUtil.BoardZones boardZones;

        private static class Chamber {
            private int id;
            private CheckerCount size;
            private Chamber parent;
            private List<Chamber> children = new ArrayList<>();
            private static int nextId = 0;
            private CheckerCount count;

            private Chamber() {}

            public static Chamber root(Board b) {
                Chamber c = new Chamber();
                c.id = nextId++;
                c.size = new CheckerCount(b);
                return c;
            }

            public Chamber(CheckerCount size, Chamber parent) {
                this.id = nextId++;
                parent.addChild(this);
                this.parent = parent;
                this.size = size;
            }

            public int getId() {
                return id;
            }

            public CheckerCount getSize() {
                return size;
            }

            public void addChild(Chamber c) {
                children.add(c);
            }

            public List<Chamber> getChildren() {
                return children;
            }

            public Chamber getParent() {
                return parent;
            }

            public void setCount(CheckerCount count) {
                this.count = count;
            }
        }

        public AvailableSpace(Board board, int playersTurn) {
            this.board = board;
            rootChamber = Chamber.root(board);
            boardZones = new BoardZones(board, playersTurn);
            visited = new boolean[board.getSize()];
            cutVertices = new boolean[board.getSize()];
            if(board.freeNeighbours(board.ourTile()).size() == 0) {
                maxMoves = 0;
            } else {
                CutVertices cv = new CutVertices(board, boardZones);
                cutVertices = cv.getCutVirtices();
                dfsCount(board.ourTile());
                maxMoves = chamberDfs(rootChamber);
                // An extra one is counted due to the use of a root chamber which is just a placeholder but adds one space.
//                maxMoves--;
            }
        }

        private int chamberDfs(Chamber c) {
            int count = 0;
            for(Chamber child : c.getChildren()) {
                count = Math.max(count, chamberDfs(child));
            }
            // All cut vertex points can be reached.
            count += c.getChildren().size();
            return count + c.getSize().getMaxMoves();
        }

        private static class CheckerCount {
            private int black;
            private int white;
            private Board board;

            public CheckerCount(Board board) {
                this.board = board;
            }

            public void add(CheckerCount c) {
                this.black+= c.black;
                this.white+= c.white;
            }

            public void add(int tile) {
                if(board.tileToPos(tile).isBlack()) {
                    black++;
                } else {
                    white++;
                }
            }

            public int getMaxMoves() {
                int min = Math.min(white, black);
                int maxMoves = min*  2;
                if(white < black && !board.tileToPos(board.ourTile()).isBlack()) {
                    maxMoves++;
                } else if(black < white && board.tileToPos(board.ourTile()).isBlack()) {
                    maxMoves++;
                }
                return maxMoves;
            }
        }

        private static class StackScope {
            private int node;
            private Chamber currentChamber;

            public StackScope(int node, Chamber currentChamber) {
                this.node = node;
                this.currentChamber = currentChamber;
            }

            public int getNode() {
                return node;
            }

            public Chamber getCurrentChamber() {
                return currentChamber;
            }
        }

        private void dfsCount(int node) {
            Stack<StackScope> stack = new Stack<>();
            stack.push(new StackScope(node, rootChamber));
            while (!stack.isEmpty()) {
                StackScope sc = stack.pop();
                node = sc.getNode();
                if(visited[node]) {
                    continue;
                }
                Chamber currentChamber = sc.getCurrentChamber();
                visited[node] = true;
                if (cutVertices[node]) {
                    for (int child : board.freeNeighbours(node)) {
                        if(!visited[child] && boardZones.isInPlayerZone(board.US, child)) {
                            // If a cut vertex has multiple children, and this loop runs twice, both must be chambers.
                            CheckerCount count = new CheckerCount(board);
                            currentChamber = new Chamber(count, currentChamber);
                            stack.push(new StackScope(child, currentChamber));
                        }
                    }
//                count.add(node);
                } else {
                    for (int child : board.freeNeighbours(node)) {
                        if(!visited[child] && boardZones.isInPlayerZone(board.US, child)) {
                            // possibly only need first filtered child, as otherwise it would be a cutvertex.
                            stack.push(new StackScope(child, currentChamber));
                        }
                    }
                    currentChamber.getSize().add(node);
                }
            }
        }

        public int getMaxMoves() {
            return maxMoves;
        }
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
                    final int tempCCID = ccID;
                    for(int n : b.freeNeighbours(node)) {
                        if(outOfBounds[n] || connectedComponents[n] != 0) {
                            continue;
                        }
                            connectedComponents[n] = tempCCID;
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
        this.floor = Arrays.copyOf(toCopy.floor, toCopy.floor.length);
        this.aliveCount = toCopy.aliveCount;
        this.playerTile = Arrays.copyOf(toCopy.playerTile, toCopy.playerTile.length);
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
        return getAlivePlayers(US);
    }
    public List<Integer> getAlivePlayers(int startingWithPlayer) {
        List<Integer> alivePlayers = new ArrayList<>();
        boolean finished = false;
        int p = startingWithPlayer;
        while(!finished) {
            if(isAlive(p)) {
                alivePlayers.add(p);
            }
            p = (p + 1) % playerCount;
            if(p == startingWithPlayer) {
                finished = true;
            }
        }
        return alivePlayers;
    }

    public boolean isAlive(int player) {
        return playerTile[player] != DEAD;
    }

    public void clear(int player) {
        // Don't delete twice.
        if(playerTile[player] == DEAD) {
            return;
        }
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
        int tile = xyToTile(x, y);
        if(playerTile(player) == tile) {
            return;
        }
        if(!isFree(tile)) {
            boolean isDead = true;
            moveHistory.push(new Move(player, playerTile(player), isDead));
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

    public List<Integer> neighbours(int tile) {
        List<Integer> neighbours = new ArrayList<>(4);
        if (tile % width != 0) {
            neighbours.add(tile -1);
        }
        if (tile % width != width -1) {
            neighbours.add(tile + 1);
        }
        if (tile >= width) {
            neighbours.add(tile - width);
        }
        if(tile < width * (height -1)){
            neighbours.add(tile + width);
        }
        return neighbours;
    }

    public List<Integer> freeNeighbours(int tile) {
        List<Integer> neighbours = new ArrayList<>(4);
        if (tile % width != 0) {
            if(floor[tile -1] == EMPTY) {
                neighbours.add(tile - 1);
            }
        }
        if (tile % width != width -1) {
            if(floor[tile +1] == EMPTY) {
                neighbours.add(tile + 1);
            }
        }
        if (tile >= width) {
            if(floor[tile - width] == EMPTY) {
                neighbours.add(tile - width);
            }
        }
        if(tile < width * (height -1)){
            if(floor[tile + width] == EMPTY) {
                neighbours.add(tile + width);
            }
        }
        return neighbours;
    }


    private IntStream freeNeighbours(Position p) {
        IntStream neighbours = neighbours(p.getX(), p.getY());
        return neighbours.filter((i)->isFree(i));
    }

    public IntStream neighbours(int x, int y) {
        return Arrays.stream(new int[]{xyToTileUnsafe(x - 1, y), xyToTileUnsafe(x + 1, y),
                xyToTileUnsafe(x, y - 1), xyToTileUnsafe(x, y + 1)})
            .filter((i) -> i != -1);
    }

    // Used by neighbours to shave some time off by allowing easy stream usage.
    private int xyToTileUnsafe(int x, int y) {
        if(!isValid(x, y)) {
            return -1;
        }
        int tile = x + width * y;
        return tile;
    }

    public int xyToTile(int x, int y) {
        if(!isValid(x, y)) {
            throw new IndexOutOfBoundsException();
        }
        int tile = x + width * y;
        return tile;
    }

    public Position tileToPosUnsafe(int tile) {
        return new Position(tile % width, tile / width);
    }

    public Position tileToPos(int tile) {
        if (tile > width * height - 1 || tile < 0) {
            throw new IndexOutOfBoundsException();
        }
        int x = tile % width;
        int y = tile / width;
        return new Position(x, y);
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
        boolean isBlack = ((x^y)&1) == 0;
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
//                System.err.print("Moving to the " + d);
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

class AvoidCutVertices implements Filter {
    @Override
    public Set<Direction> filterBadMoves(Board b, Set<Direction> moves) {
        Map<Integer, Integer> tileToScores = new HashMap<>();
        int max = -1;
        for(int n : b.freeNeighbours(b.ourTile())) {
            tileToScores.put(n, -1);
            b.move(b.US, n);
            int localMax = -1;
            for(int n2 : b.freeNeighbours(b.ourTile())) {
                b.move(b.US, n2);
                int spaces = BoardUtil.availableSpaces(b, b.ourTile());
                if(spaces > localMax) {
                    localMax = spaces;
                }
                b.undoMove();
            }
            if(localMax > max) {
                max = localMax;
            }
            tileToScores.put(n, localMax);

            b.undoMove();
        }
        final int tempMax = max;
        Set<Direction> filtered = moves.stream().filter(m -> {
            int tile = b.tileFrom(b.ourTile(), m);
            // The tile may not be in the map if it was not a free neighbour. Thus, death moves
            // are also filtered here.
            return tileToScores.containsKey(tile) && tileToScores.get(tile) == tempMax;
        }).collect(Collectors.toSet());
        return filtered;
    }
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
                    for(int n : board.freeNeighbours(next)) {
                        if (board.isFree(n) && connectedComponents[n] == 0) {
                            connectedComponents[n] = tempCCID;
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

class BruteForceEndGame implements Driver {
    private final int MAX_DEPTH = 10;
    private boolean[] marked;
    private int currentMax = 0;

    @Override
    public Direction move(final Board board) {
        marked = new boolean[board.getSize()];
        marked[board.ourTile()] = true;
        int maxPossible = BoardUtil.availableSpaces(board, board.ourTile());
        int runToDepth = Math.min(maxPossible, MAX_DEPTH);
        int max = 0;
        Position maxPos = null;
        Comparator<Integer> sortNeighbours = Comparator.comparing(a -> board.freeNeighbours(a).size());
        for(int n : board.freeNeighbours(board.ourTile())) {
            int consumed = dfs(board, n, 0, runToDepth);
            if(consumed  > maxPossible + 1) {
                System.err.println(String.format("Consumed (%d) is greater than max possible(%d)", consumed, maxPossible));
            }
            assert (consumed <= maxPossible + 1):
                    String.format("Consumed (%d) is greater than max possible(%d)", consumed, maxPossible);
            if (consumed == maxPossible) {
                return board.tileToPos(board.ourTile()).directionTo(board.tileToPos(n));
            }
            if(consumed > max) {
                max = consumed;
                maxPos = board.tileToPos(n);
            }
        }
        // If no free direction, choose any.
        if(maxPos == null) {
            return board.tileToPos(board.ourTile()).directionTo(board.tileToPos(board.neighbours(board.ourTile()).get(0)));
        }
        return board.tileToPos(board.ourTile()).directionTo(maxPos);
    }

    private int dfs(Board b, int tile, int step, int maxDepth) {
        marked[tile] = true;
        int max = 0;
        int maxPossible = BoardUtil.availableSpaces(b, tile, marked);
        if(step == maxDepth || maxPossible == 0 || (maxPossible + 1 + step) <= currentMax) {
            // Add 1 to include this tile.
            max = 1 + step + BoardUtil.availableSpaces(b, tile, marked);
        }
        else {
            Comparator<Integer> sortNeighbours = Comparator.comparing(a -> b.freeNeighbours(a).size());
            List<Integer> neighbours = b.freeNeighbours(tile);
            neighbours.sort(sortNeighbours);
            for(int n : neighbours) {
                if(!marked[n]) {
                    int consumed = dfs(b, n, step + 1, maxDepth);
                    max = Math.max(max, consumed);
                    currentMax = Math.max(max, currentMax);
                }
            }
        }
        marked[tile] = false;
        return max;
    }
}

class WallHuggingDriver implements Driver {
    private StaySafeDriver backupDriver = new StaySafeDriver();
    private Filter deadEndFilter = new AvoidSmallComponents();
    private Filter avoidCutVerticesFilter = new AvoidCutVertices();
    @Override
    public Direction move(Board board) {
        Set<Direction> okayDirections = avoidCutVerticesFilter.filterBadMoves(board,
                new HashSet<>(Arrays.asList(Direction.values())));
        // If there are no options but to enter a cut vertex, choose the best component.
        if(okayDirections.size() == 0) {
            okayDirections = new HashSet<>(Arrays.asList(Direction.values()));
        }
        Set<Direction> okayDirections2 = deadEndFilter.filterBadMoves(board, new HashSet<>(Arrays.asList(Direction.values())));
        okayDirections.retainAll(okayDirections2);
        if(okayDirections.size() == 0) {
            okayDirections = new HashSet<>(Arrays.asList(Direction.values()));
            okayDirections = deadEndFilter.filterBadMoves(board, new HashSet<>(okayDirections));
        }
        if(okayDirections.size() == 0) {
            okayDirections = new HashSet<>(Arrays.asList(Direction.values()));
        }
        for(Direction d : Direction.values()) {
            if(!okayDirections.contains(d)) {
                continue;
            }
            int tile = board.tileFrom(board.ourTile(), d);
            boolean isValid = tile != -1;
            if (isValid && board.isFree(tile)) {
                long neighbours = board.freeNeighbours(tile).size();
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

// Always move towards the battlefield.
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
//    private Driver backupDriver = new WallHuggingDriver();
    private Driver backupDriver = new BruteForceEndGame();
    private boolean backupEnabled = false;
    private int playerCount = 0;

    @Override
    public Direction move(Board board) {
        int depth = 5;
        Direction bestMove;
        // If someone dies, the board can open up with new space.
        if(playerCount != board.getAliveCount()) {
            backupEnabled = false;
            playerCount = board.getAliveCount();
        }
        if(backupEnabled || BoardUtil.isAloneInComponent(board)) {
            System.err.println("Backup enabled");
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
            BoardUtil.AvailableSpace availableSpace = new BoardUtil.AvailableSpace(b, player);
            spaceCount = availableSpace.getMaxMoves(); //BoardUtil.playerZoneCounts(b, player)[b.US];
            return spaceCount;
        }

        public int eval2(Board b, int player) {
            int[] playerSpaces = new int[b.getAliveCount()];
            boolean inclusive = false;
            boolean[] outOfBounds = BoardUtil.battlefield(b, inclusive);
            for(int p : b.getAlivePlayers(player)) {
                int spaceCount;
                if(outOfBounds == null) {
                    spaceCount = BoardUtil.availableSpaces(b, b.playerTile(p));
                } else {
                    spaceCount = BoardUtil.availableSpaces(b, b.playerTile(p), outOfBounds);
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

        public PosScore(int pos, int score) {
            this.pos = pos;
            this.score = score;
        }

        public String print(Board b) {
            return String.format("(%d, %d) %d", b.tileToPos(pos).getX(), b.tileToPos(pos).getY(), score);
        }
    }

    public static Direction minMax(Board b, Score score, int depth) {
        int currentStep = 0;
        int player = b.US;
        int position = _minMax(b, score, depth, player, currentStep, -1, -1).pos;
        Direction bestDirection = b.tileToPos(b.ourTile()).directionTo(b.tileToPos(position));
        return bestDirection;
    }


    private static PosScore _minMax(Board b, Score score, int depth, int player, int currentStep, int alpha, int beta) {
        int attempts = 0;
        while (!b.isAlive(player)) {
            player = (player + 1) % b.getPlayerCount();
            attempts++;
            assert attempts < b.getPlayerCount();
        }
        if(b.getAliveCount() == 1) {
            assert player == b.US;
            return new PosScore(b.ourTile(), new Double(BoardUtil.availableSpaces(b, b.ourTile()) * 200).intValue());
        }
        if (currentStep == depth) {
            return new PosScore(b.ourTile(), score.eval(b, player));
        }
        List<Integer> freeNeighbours = b.freeNeighbours(b.playerTile(player));
        List<Integer> toTry;
        // If there are no free neighbours, if it is not us,
        // pick a random move so that the player can die and continue the minmax.
        if(freeNeighbours.isEmpty()) {
            if(player == b.US) {
                return new PosScore(b.ourTile(), 0);
            }
            toTry = new ArrayList<>();
            toTry.add(b.neighbours(b.playerTile(player)).get(0));
        } else {
            toTry = freeNeighbours;
        }
        boolean maximize = player == b.US;
        if (maximize) {
            PosScore localMax = null;
            assert !freeNeighbours.isEmpty();
            freeNeighbours.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return new Long(b.freeNeighbours(o2).size()).compareTo(new Long(b.freeNeighbours(o1).size()));
                }
            });
            for (int n : freeNeighbours) {
                b.move(player, n);
                PosScore posScore = _minMax(b, score, depth, (player + 1) % b.getPlayerCount(), currentStep + 1, alpha, beta);
                // Adding one favours living longer in case all options end in death.
//                posScore.score++;
                b.undoMove();
                if (localMax == null || posScore.score > localMax.score) {
                    localMax = new PosScore(n, posScore.score);
                }
                if (alpha == -1 || localMax.score > alpha) {
                    alpha = localMax.score;
                }
                if (beta != -1 && beta <= alpha) {
                    break;
                }
            }
            return localMax;
        } else {
            // Minimize.
            PosScore localMin = null;
            toTry.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return new Long(b.freeNeighbours(o1).size()).compareTo(new Long(b.freeNeighbours(o2).size()));
                }
            });
            for (int n : toTry) {
                b.move(player, n);
                PosScore posScore = _minMax(b, score, depth, (player + 1) % b.getPlayerCount(), currentStep + 1, alpha, beta);
                // Magnify the effect of a death of another player.
                if(freeNeighbours.isEmpty()) {
                    posScore.score = new Double(posScore.score * 1.2).intValue();
                }
                b.undoMove();
                if (localMin == null || posScore.score < localMin.score) {
                    localMin = new PosScore(n, posScore.score);
                }
                if (beta == -1 || localMin.score < beta) {
                    beta = localMin.score;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return localMin;
        }
    }
}
