import java.util.*;

class Player {

    public static void main(String[] args) {
        int width = 30;
        int height = 20;
//        Driver driver = new DeadDriver();
//        Driver driver = new StaySafeDriver();
        Driver driver  = new WallHuggingDriver();
        InputParser p = new InputParser();
        Board board = p.init(width, height);
        while (true) {
            Direction nextMove = driver.move(board);
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

class Board {
    public final int US;
    public final int width;
    public final int height;
    // Zero marks empty tiles;
    public static final int EMPTY = -2;
    private int[] floor;
    private int[] playerTile;

    public Board(int width, int height, int playerCount, int us) {
        this.width = width;
        this.height = height;
        floor = new int[width * height];
        for(int i = 0; i < floor.length; i++) {
            floor[i] = EMPTY;
        }
        playerTile = new int[playerCount];
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
        // Could calculate history of moves here.
        int tile = xyToTile(x, y);
        playerTile[player] = tile;
        System.err.printf("Player %d moving to (%d, %d) aka %d\n", player, x, y, tile);
        floor[tile] = player;
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

class WallHuggingDriver implements Driver {
    private StaySafeDriver backupDriver = new StaySafeDriver();

    @Override
    public Direction move(Board board) {
        Direction direction;
        for(Direction d : Direction.values()) {
            int tile = board.tileFrom(board.ourTile(), d);
            boolean isValid = tile != -1;
            if (isValid && board.isFree(tile)) {
                Set<Integer> neighbours = board.neighbours(tile);
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
