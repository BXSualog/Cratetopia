package managers;

import accounts.PlayerAcc;
import characters.Hero;
import items.Item;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Manages player accounts and persists progress (currencies, heroes, items, equipment).
 * Adheres strictly to DSA constraints using LinkedList collections.
 */
public class PlayerDataManager {
    private static final String CSV_FILE_PATH      = "data/players.csv";
    private static final String INVENTORY_CSV_PATH = "data/player_inventory.csv";
    private static final String HEROES_CSV_PATH    = "data/player_heroes.csv";

    private final LinkedList<PlayerAcc> playerList = new LinkedList<>();

    private CharacterDataManager charData;
    private ItemDataManager itemData;

    public PlayerDataManager() {
        this(null, null);
    }

    public PlayerDataManager(CharacterDataManager charData, ItemDataManager itemData) {
        this.charData = charData;
        this.itemData = itemData;
        loadAllData();
    }

    public void setManagers(CharacterDataManager charData, ItemDataManager itemData) {
        this.charData = charData;
        this.itemData = itemData;
        loadPlayerInventoriesFromCsv();
        loadPlayerHeroesFromCsv();
    }

    public LinkedList<PlayerAcc> getPlayerList() {
        return playerList;
    }

    public void loadAllData() {
        loadPlayersFromCsv();
        loadPlayerInventoriesFromCsv();
        loadPlayerHeroesFromCsv();
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
                    int coins = parts.length > 2 ? parseIntOrDefault(parts[2].trim(), 500) : 500;
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

    public void loadPlayerInventoriesFromCsv() {
        if (itemData == null) return;
        File file = new File(INVENTORY_CSV_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (isFirstLine && line.toLowerCase().startsWith("username,item_id")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String itemId = parts[1].trim();
                    PlayerAcc player = getPlayer(username);
                    if (player != null) {
                        Item item = itemData.createNewItemInstance(itemId);
                        if (item != null) {
                            player.addItem(item);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading player inventory CSV: " + e.getMessage());
        }
    }

    public void loadPlayerHeroesFromCsv() {
        if (charData == null) return;
        File file = new File(HEROES_CSV_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (isFirstLine && line.toLowerCase().startsWith("username,hero_id")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                // username,hero_id,weapon_id,helmet_id,chest_id,leggings_id,accessory_id
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String heroId = parts[1].trim();
                    PlayerAcc player = getPlayer(username);
                    if (player != null) {
                        Hero hero = charData.createNewHeroInstance(heroId);
                        if (hero != null) {
                            if (itemData != null) {
                                if (parts.length > 2 && !isNoneOrEmpty(parts[2])) {
                                    Item w = itemData.createNewItemInstance(parts[2].trim());
                                    if (w != null) hero.equip(w);
                                }
                                if (parts.length > 3 && !isNoneOrEmpty(parts[3])) {
                                    Item h = itemData.createNewItemInstance(parts[3].trim());
                                    if (h != null) hero.equip(h);
                                }
                                if (parts.length > 4 && !isNoneOrEmpty(parts[4])) {
                                    Item c = itemData.createNewItemInstance(parts[4].trim());
                                    if (c != null) hero.equip(c);
                                }
                                if (parts.length > 5 && !isNoneOrEmpty(parts[5])) {
                                    Item l = itemData.createNewItemInstance(parts[5].trim());
                                    if (l != null) hero.equip(l);
                                }
                                if (parts.length > 6 && !isNoneOrEmpty(parts[6])) {
                                    Item a = itemData.createNewItemInstance(parts[6].trim());
                                    if (a != null) hero.equip(a);
                                }
                            }
                            player.addHero(hero);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading player heroes CSV: " + e.getMessage());
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
        saveAllPlayersToCsv();
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
        if (username == null) return null;
        for (PlayerAcc player : playerList) {
            if (player.getUsername().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }

    public void saveAllPlayersToCsv() {
        savePlayersFile();
        savePlayerInventoriesFile();
        savePlayerHeroesFile();
    }

    private void savePlayersFile() {
        File file = new File(CSV_FILE_PATH);
        ensureParentDir(file);

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

    private void savePlayerInventoriesFile() {
        File file = new File(INVENTORY_CSV_PATH);
        ensureParentDir(file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("username,item_id");
            writer.newLine();
            for (PlayerAcc player : playerList) {
                for (Item item : player.getInventory()) {
                    writer.write(player.getUsername() + "," + item.getId());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving player inventories to CSV: " + e.getMessage());
        }
    }

    private void savePlayerHeroesFile() {
        File file = new File(HEROES_CSV_PATH);
        ensureParentDir(file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("username,hero_id,weapon_id,helmet_id,chest_id,leggings_id,accessory_id");
            writer.newLine();
            for (PlayerAcc player : playerList) {
                for (Hero hero : player.getRoster()) {
                    String weaponId = hero.getEquippedWeapon() != null ? hero.getEquippedWeapon().getId() : "none";
                    String helmetId = hero.getEquippedArmor(Hero.SLOT_HELMET) != null ? hero.getEquippedArmor(Hero.SLOT_HELMET).getId() : "none";
                    String chestId = hero.getEquippedArmor(Hero.SLOT_CHEST) != null ? hero.getEquippedArmor(Hero.SLOT_CHEST).getId() : "none";
                    String leggingsId = hero.getEquippedArmor(Hero.SLOT_LEGGINGS) != null ? hero.getEquippedArmor(Hero.SLOT_LEGGINGS).getId() : "none";
                    String accessoryId = hero.getEquippedAccessory() != null ? hero.getEquippedAccessory().getId() : "none";

                    writer.write(player.getUsername() + "," +
                                 hero.getId() + "," +
                                 weaponId + "," +
                                 helmetId + "," +
                                 chestId + "," +
                                 leggingsId + "," +
                                 accessoryId);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving player heroes to CSV: " + e.getMessage());
        }
    }

    private void ensureParentDir(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    private boolean isNoneOrEmpty(String str) {
        return str == null || str.trim().isEmpty() || str.trim().equalsIgnoreCase("none");
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
