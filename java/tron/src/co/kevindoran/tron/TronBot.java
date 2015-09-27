package co.kevindoran.tron;

public class TronBot {

	private class InputParser {
		private Scanner in = Scanner(System.in);
		private boolean firstRun = true;
		private void update(Board current) {
			int playerCount = in.nextInt();
			int us = in.nextInt();
			for(int i = 0; i < playerCount; i++) {
				

	}

	private class Board {
		private int[] floor;
		private int[] playerPos;
		private final int US;
		
		private Board(int width, int height, int playerCount, int us) {
			floor = new int[width*height];
			playerPos = new int[playerCount];
			US = us;
		}

		private int clear(int player) {
			for(int i = 0; i < floor.length; i++) {
				

		private int at(int x, int y) {
			return floor(x + height*y);
		}

		private int at(Positon p) {
			return at(p.getX(), p.getY());
		}
	}

	private class Position {
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
		
	}
