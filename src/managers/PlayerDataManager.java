package managers;

import accounts.PlayerAcc;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class PlayerDataManager {
    private static final String CSV_FILE_PATH = "data/players.csv";
    private final LinkedList<PlayerAcc> playerList = new LinkedList<>();

    public PlayerDataManager() {
        loadPlayersFromCsv();
    }

    public LinkedList<PlayerAcc> getPlayerList() {
        return playerList;
    }

    public void loadPlayersFromCsv() {
        playerList.clear();
        File file = new File(CSV_FILE_PATH);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // Skip header if present
                if (isFirstLine && line.toLowerCase().startsWith("username,password")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    int coins = parts.length > 2 ? parseIntOrDefault(parts[2].trim(), 100) : 100;
                    int shards = parts.length > 3 ? parseIntOrDefault(parts[3].trim(), 10) : 10;
                    int heroesCollected = parts.length > 4 ? parseIntOrDefault(parts[4].trim(), 0) : 0;
                    int itemsOwned = parts.length > 5 ? parseIntOrDefault(parts[5].trim(), 0) : 0;
                    playerList.add(new PlayerAcc(username, password, coins, shards, heroesCollected, itemsOwned));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    public boolean isUsernameTaken(String username) {
        for (PlayerAcc player : playerList) {
            if (player.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean registerPlayer(String username, String password) {
        if (isUsernameTaken(username)) {
            return false;
        }

        PlayerAcc newPlayer = new PlayerAcc(username, password);
        playerList.add(newPlayer);
        savePlayerToCsv(newPlayer);
        return true;
    }

    public boolean authenticate(String username, String password) {
        for (PlayerAcc player : playerList) {
            if (player.getUsername().equalsIgnoreCase(username) && player.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public boolean loginPlayer(String username, String password) {
        return authenticate(username, password);
    }

    public PlayerAcc getPlayer(String username) {
        for (PlayerAcc player : playerList) {
            if (player.getUsername().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }

    public void saveAllPlayersToCsv() {
        File file = new File(CSV_FILE_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("username,password,coins,shards,heroesCollected,itemsOwned");
            writer.newLine();
            for (PlayerAcc player : playerList) {
                writer.write(player.getUsername() + "," +
                             player.getPassword() + "," +
                             player.getCoins() + "," +
                             player.getShards() + "," +
                             player.getHeroesCollected() + "," +
                             player.getItemsOwned());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving players to CSV: " + e.getMessage());
        }
    }

    private void savePlayerToCsv(PlayerAcc player) {
        saveAllPlayersToCsv();
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
