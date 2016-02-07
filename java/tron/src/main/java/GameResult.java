import java.util.Map;
import java.util.TreeMap;

class GameResult {
    private TreeMap<SimPlayer, Integer> playerPositions = new TreeMap<>();
    private final int playerCount;

    GameResult(int playerCount) {
        this.playerCount = playerCount;
    }

    void addNextDead(SimPlayer player) {
        playerPositions.put(player, playerCount - playerPositions.size());
    }

    Map<SimPlayer, Integer> getPlayerPositions() {
        return playerPositions;
    }

    String[] csvHeader() {
        String[] array = new String[playerPositions.size()];
        int i = 0;
        for(Map.Entry<SimPlayer, Integer> entry : playerPositions.entrySet()) {
            array[i++] = entry.getKey().getName();
        }
        return array;
    }

    String[] toArray() {
        String[] array = new String[playerPositions.size()];
        int i = 0;
        for(Map.Entry<SimPlayer, Integer> entry : playerPositions.entrySet()) {
            array[i++] = Integer.toString(entry.getValue());
        }
        return array;
    }
}
