package co.kevindoran.tron;

import java.util.*;

class TronBot {

    public static void main(String[] args) {
        int width = 30;
        int height = 20;
        Driver driver = new DeadDriver();
        InputParser p = new InputParser();
        Board board = p.init(width, height);
        while (true) {
            p.update(board);
            Direction nextMove = driver.move(board);
            System.out.println(nextMove.toString());
        }
    }

    private enum Direction {
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

    private static class InputParser {
        private Scanner in = new Scanner(System.in);

        public InputParser() {
        }

        private Board init(int width, int height) {
            int playerCount = in.nextInt();
            int us = in.nextInt();
            Board b = new Board(width, height, playerCount, us);
            boolean startup = true;
            update(b, startup);
            return b;
        }

        private void update(Board current) {
            boolean startup = false;
            update(current, startup);
        }

        private void update(Board current, boolean startup) {
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
                    current.move(i, x1, y1);
                }
            }
        }
    }

    private static class Board {
        private final int US;
        private int[] floor;
        private int[] playerPos;
        private int width;
        private int height;

        private Board(int width, int height, int playerCount, int us) {
            this.width = width;
            this.height = height;
            floor = new int[width * height];
            playerPos = new int[playerCount];
            US = us;
        }

        public int getPlayerCount() {
            return playerPos.length;
        }

        private void clear(int player) {
            for (int i = 0; i < floor.length; i++) {
                if(floor[i] == player) {
                    floor[i] = 0;
                }
            }
        }

        private void move(int player, int x, int y) {
            // Could calculate history of moves here.
            int tile = xyToTile(x, y);
            playerPos[player] = tile;
            floor[tile] = player;
        }

        private boolean isValid(int x, int y) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }

        private boolean isValid(Position p) {
            return isValid(p.getX(), p.getY());
        }

        private int tileFrom(int tile, Direction d) {
            Position now = tileToPos(tile);
            Position next = new Position(now.getX() + d.getDx(), now.getY() + d.getDy());
            if (!isValid(next)) {
                return -1;
            }
            return posToTile(next);
        }

        private Set<Integer> neighbours(int tile) {
            Position pos = tileToPos(tile);
            return neighbours(pos.getX(), pos.getY());
        }

        private Set<Integer> neighbours(int x, int y) {
            Set<Integer> neighbours = new HashSet<>();
            if(x - 1 >= 0) {
                neighbours.add(xyToTile(x - 1, y));
            }
            if(x + 1 < width) {
                neighbours.add(xyToTile(x + 1, y));
            }
            if(y - 1 >= 0) {
                neighbours.add(xyToTile(x, y - 1));
            }
            if(y + 1 < height) {
                neighbours.add(xyToTile(x, y + 1));
            }
            return neighbours;
        }

        private Position tileToPos(int tile) {
            if(tile > width*height - 1) {
                throw new IndexOutOfBoundsException();
            }
            int x = tile % width;
            int y = tile / width;
            return new Position(x, y);
        }

        private int xyToTile(int x, int y) {
            return floor[x + height * y];
        }

        private int posToTile(Position p) {
            return xyToTile(p.getX(), p.getY());
        }
    }

    private static class Position {
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

    private interface Driver {
        Direction move(Board board);
    }

    private static class DeadDriver implements Driver {
        @Override
        public Direction move(Board board) {
            return Direction.LEFT;
        }
    }

    private static class StaySafeDriver implements Driver {
        @Override
        public Direction move(Board board) {
            // Choose a default in case there are no safe directions.
            Direction direction = Direction.LEFT;
            for(Direction d : Direction.values()) {
                if(board.tileFrom(board.US, d) != -1) {
                    direction = d;
                    break;
                }
            }
            return direction;
        }
    }
}
