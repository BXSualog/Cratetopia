package data;

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
                if (isFirstLine && line.equalsIgnoreCase("username,password")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    playerList.add(new PlayerAcc(username, password));
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

    private void savePlayerToCsv(PlayerAcc player) {
        File file = new File(CSV_FILE_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        boolean writeHeader = !file.exists() || file.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                writer.write("username,password");
                writer.newLine();
            }
            writer.write(player.getUsername() + "," + player.getPassword());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving player to CSV: " + e.getMessage());
        }
    }
}
