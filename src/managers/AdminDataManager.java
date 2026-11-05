package managers;

import accounts.AdminAcc;
import accounts.PlayerAcc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * AdminDataManager – manages administrator accounts, credentials, and data operations.
 *
 * OOP Concepts demonstrated here
 * --------------------------------
 * ENCAPSULATION      : The admin list and CSV handling are encapsulated within this class.
 * DELEGATION         : Coordinates operations across PlayerDataManager, ItemDataManager,
 *                      and CharacterDataManager to empower the Admin Dashboard.
 *
 * DSA Concepts demonstrated here
 * --------------------------------
 * LinkedList<AdminAcc> adminList:
 *   Adheres strictly to DSA constraints using LinkedList collections.
 * Linear Search:
 *   O(n) linear search for admin authentication, lookups, and duplicate checking.
 */
public class AdminDataManager {

    private static final String ADMINS_CSV_PATH = "data/admins.csv";

    // Hardcoded fallback credentials (used when CSV is missing or empty)
    private static final String DEFAULT_ADMIN_USER = "admin";
    private static final String DEFAULT_ADMIN_PASS = "admin123";
    private static final String DEFAULT_ADMIN_ROLE = "Administrator";

    private static final String DEFAULT_SUPERADMIN_USER = "superadmin";
    private static final String DEFAULT_SUPERADMIN_PASS = "cratetopia2026";
    private static final String DEFAULT_SUPERADMIN_ROLE = "Super Administrator";

    // DSA: LinkedList to hold registered administrators
    private final LinkedList<AdminAcc> adminList = new LinkedList<>();

    private PlayerDataManager playerDataManager;
    private ItemDataManager itemDataManager;
    private CharacterDataManager charDataManager;

    /**
     * Default constructor. Loads admin credentials from CSV or hardcoded fallback.
     */
    public AdminDataManager() {
        this(null, null, null);
    }

    /**
     * Full constructor with references to other game managers.
     *
     * @param playerDataManager  Manager for player accounts and progress.
     * @param itemDataManager    Manager for weapons, armor, accessories.
     * @param charDataManager    Manager for character hero templates.
     */
    public AdminDataManager(PlayerDataManager playerDataManager,
                            ItemDataManager itemDataManager,
                            CharacterDataManager charDataManager) {
        this.playerDataManager = playerDataManager;
        this.itemDataManager   = itemDataManager;
        this.charDataManager   = charDataManager;
        loadAdminsFromCsv();
    }

    // -------------------------------------------------------------------------
    // Manager Setters / Getters
    // -------------------------------------------------------------------------

    public void setManagers(PlayerDataManager playerDataManager,
                            ItemDataManager itemDataManager,
                            CharacterDataManager charDataManager) {
        this.playerDataManager = playerDataManager;
        this.itemDataManager   = itemDataManager;
        this.charDataManager   = charDataManager;
    }

    public PlayerDataManager getPlayerDataManager()     { return playerDataManager; }
    public ItemDataManager getItemDataManager()         { return itemDataManager; }
    public CharacterDataManager getCharDataManager()   { return charDataManager; }

    public LinkedList<AdminAcc> getAdminList() {
        return adminList;
    }

    // -------------------------------------------------------------------------
    // Admin Credential Management & Persistence
    // -------------------------------------------------------------------------

    /**
     * Loads admin credentials from data/admins.csv.
     * If the file does not exist or is empty, creates it and seeds default credentials.
     */
    public void loadAdminsFromCsv() {
        adminList.clear();
        File file = new File(ADMINS_CSV_PATH);

        if (!file.exists()) {
            seedDefaultAdmins();
            saveAdminsToCsv();
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

                // Skip CSV header if present
                if (isFirstLine && line.toLowerCase().startsWith("username,password")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String role     = (parts.length >= 3 && !parts[2].trim().isEmpty())
                            ? parts[2].trim()
                            : DEFAULT_ADMIN_ROLE;

                    if (!username.isEmpty() && !password.isEmpty()) {
                        adminList.add(new AdminAcc(username, password, role));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading admin credentials from CSV: " + e.getMessage());
        }

        // If file was empty, re-seed defaults
        if (adminList.isEmpty()) {
            seedDefaultAdmins();
            saveAdminsToCsv();
        }
    }

    /**
     * Seeds the hardcoded default admin credentials.
     */
    private void seedDefaultAdmins() {
        adminList.add(new AdminAcc(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_PASS, DEFAULT_ADMIN_ROLE));
        adminList.add(new AdminAcc(DEFAULT_SUPERADMIN_USER, DEFAULT_SUPERADMIN_PASS, DEFAULT_SUPERADMIN_ROLE));
    }

    /**
     * Saves all current administrator accounts to data/admins.csv.
     */
    public void saveAdminsToCsv() {
        File file = new File(ADMINS_CSV_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("username,password,role");
            writer.newLine();

            for (AdminAcc admin : adminList) {
                writer.write(admin.getUsername() + "," +
                             admin.getPassword() + "," +
                             admin.getRole());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving admins to CSV: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Admin Authentication & Lookup (Linear Search O(n))
    // -------------------------------------------------------------------------

    /**
     * Checks if an admin username is already registered.
     * Uses DSA linear search (O(n)) through the LinkedList.
     *
     * @param username The username to check.
     * @return true if taken, false otherwise.
     */
    public boolean isAdminUsernameTaken(String username) {
        if (username == null) return false;
        for (AdminAcc admin : adminList) {
            if (admin.getUsername().equalsIgnoreCase(username.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds and returns an AdminAcc by username using linear search.
     *
     * @param username Admin username.
     * @return AdminAcc if found; null otherwise.
     */
    public AdminAcc getAdmin(String username) {
        if (username == null) return null;
        for (AdminAcc admin : adminList) {
            if (admin.getUsername().equalsIgnoreCase(username.trim())) {
                return admin;
            }
        }
        return null;
    }

    /**
     * Authenticates an admin against stored accounts.
     * If matched, returns the authenticated AdminAcc object; otherwise returns null.
     * Also checks hardcoded fallback credentials if list is empty or file access failed.
     *
     * @param username Entered admin username.
     * @param password Entered admin password.
     * @return AdminAcc on success, null on failure.
     */
    public AdminAcc loginAdmin(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        // 1. Check loaded admin accounts
        for (AdminAcc admin : adminList) {
            if (admin.validateCredentials(username, password)) {
                return admin;
            }
        }

        // 2. Hardcoded fallback check (ensures access even if file is missing/corrupted)
        if (DEFAULT_ADMIN_USER.equalsIgnoreCase(username.trim()) && DEFAULT_ADMIN_PASS.equals(password)) {
            AdminAcc fallback = new AdminAcc(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_PASS, DEFAULT_ADMIN_ROLE);
            adminList.add(fallback);
            saveAdminsToCsv();
            return fallback;
        }
        if (DEFAULT_SUPERADMIN_USER.equalsIgnoreCase(username.trim()) && DEFAULT_SUPERADMIN_PASS.equals(password)) {
            AdminAcc fallback = new AdminAcc(DEFAULT_SUPERADMIN_USER, DEFAULT_SUPERADMIN_PASS, DEFAULT_SUPERADMIN_ROLE);
            adminList.add(fallback);
            saveAdminsToCsv();
            return fallback;
        }

        return null;
    }

    /**
     * Registers a new administrator account and persists to CSV.
     *
     * @param username Chosen admin username.
     * @param password Chosen admin password.
     * @param role     Admin role / designation.
     * @return true if created successfully; false if username is taken or invalid.
     */
    public boolean registerAdmin(String username, String password, String role) {
        if (username == null || password == null) return false;
        username = username.trim();
        password = password.trim();
        if (username.isEmpty() || password.isEmpty()) return false;
        if (isAdminUsernameTaken(username)) return false;

        AdminAcc newAdmin = new AdminAcc(username, password, role);
        adminList.add(newAdmin);
        saveAdminsToCsv();
        return true;
    }

    // -------------------------------------------------------------------------
    // Player Management Helpers (Dashboard Support)
    // -------------------------------------------------------------------------

    /**
     * Finds a player account by username via PlayerDataManager.
     */
    public PlayerAcc findPlayer(String username) {
        if (playerDataManager == null || username == null) return null;
        return playerDataManager.getPlayer(username);
    }

    /**
     * Searches for players whose usernames contain the search query.
     * Uses DSA Linear Search (O(n)) through the LinkedList.
     *
     * @param query Search query substring.
     * @return LinkedList of matching PlayerAcc objects.
     */
    public LinkedList<PlayerAcc> searchPlayers(String query) {
        LinkedList<PlayerAcc> results = new LinkedList<>();
        if (playerDataManager == null || query == null) return results;
        String q = query.trim().toLowerCase();
        for (PlayerAcc p : playerDataManager.getPlayerList()) {
            if (p.getUsername().toLowerCase().contains(q)) {
                results.add(p);
            }
        }
        return results;
    }

    /**
     * Bans a player account from logging into the game.
     *
     * @param username The player's username.
     * @return true if player was found and banned; false otherwise.
     */
    public boolean banPlayer(String username) {
        PlayerAcc player = findPlayer(username);
        if (player == null) return false;
        player.setBanned(true);
        playerDataManager.saveAllPlayersToCsv();
        return true;
    }

    /**
     * Unbans a player account, restoring their ability to log in.
     *
     * @param username The player's username.
     * @return true if player was found and unbanned; false otherwise.
     */
    public boolean unbanPlayer(String username) {
        PlayerAcc player = findPlayer(username);
        if (player == null) return false;
        player.setBanned(false);
        playerDataManager.saveAllPlayersToCsv();
        return true;
    }

    /**
     * Toggles the ban status of a player account.
     *
     * @param username The player's username.
     * @return true if player was found and status toggled; false otherwise.
     */
    public boolean togglePlayerBan(String username) {
        PlayerAcc player = findPlayer(username);
        if (player == null) return false;
        player.setBanned(!player.isBanned());
        playerDataManager.saveAllPlayersToCsv();
        return true;
    }

    /**
     * Modifies a player's coins balance by a delta amount (+ or -).
     */
    public boolean modifyPlayerCoins(String username, int coinDelta) {
        PlayerAcc player = findPlayer(username);
        if (player == null) return false;
        int updated = Math.max(0, player.getCoins() + coinDelta);
        player.setCoins(updated);
        playerDataManager.saveAllPlayersToCsv();
        return true;
    }

    /**
     * Modifies a player's shards balance by a delta amount (+ or -).
     */
    public boolean modifyPlayerShards(String username, int shardDelta) {
        PlayerAcc player = findPlayer(username);
        if (player == null) return false;
        int updated = Math.max(0, player.getShards() + shardDelta);
        player.setShards(updated);
        playerDataManager.saveAllPlayersToCsv();
        return true;
    }

    /**
     * Resets a player's progress back to default starting values:
     * 500 coins, 10 shards, empty inventory, and empty hero roster.
     */
    public boolean resetPlayerProgress(String username) {
        PlayerAcc player = findPlayer(username);
        if (player == null) return false;
        player.setCoins(500);
        player.setShards(10);
        player.getInventory().clear();
        player.getRoster().clear();
        player.setHeroesCollected(0);
        player.setItemsOwned(0);
        playerDataManager.saveAllPlayersToCsv();
        return true;
    }

    /**
     * Permanently deletes a player account from the system.
     */
    public boolean deletePlayer(String username) {
        if (playerDataManager == null || username == null) return false;
        PlayerAcc target = null;
        for (PlayerAcc p : playerDataManager.getPlayerList()) {
            if (p.getUsername().equalsIgnoreCase(username.trim())) {
                target = p;
                break;
            }
        }
        if (target != null) {
            playerDataManager.getPlayerList().remove(target);
            playerDataManager.saveAllPlayersToCsv();
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // System Statistics & Analytics
    // -------------------------------------------------------------------------

    public int getTotalPlayersCount() {
        return (playerDataManager != null) ? playerDataManager.getPlayerList().size() : 0;
    }

    public int getActivePlayersCount() {
        if (playerDataManager == null) return 0;
        int count = 0;
        for (PlayerAcc p : playerDataManager.getPlayerList()) {
            if (!p.isBanned()) count++;
        }
        return count;
    }

    public int getBannedPlayersCount() {
        if (playerDataManager == null) return 0;
        int count = 0;
        for (PlayerAcc p : playerDataManager.getPlayerList()) {
            if (p.isBanned()) count++;
        }
        return count;
    }

    public long getTotalEconomyCoins() {
        if (playerDataManager == null) return 0;
        long total = 0;
        for (PlayerAcc p : playerDataManager.getPlayerList()) {
            total += p.getCoins();
        }
        return total;
    }

    public long getTotalEconomyShards() {
        if (playerDataManager == null) return 0;
        long total = 0;
        for (PlayerAcc p : playerDataManager.getPlayerList()) {
            total += p.getShards();
        }
        return total;
    }

    public int getTotalHeroesCatalogCount() {
        return (charDataManager != null) ? charDataManager.getAllHeroes().size() : 0;
    }

    public int getTotalItemsCatalogCount() {
        if (itemDataManager == null) return 0;
        return itemDataManager.getWeapons().size() +
               itemDataManager.getArmors().size() +
               itemDataManager.getAccessories().size();
    }

    /**
     * Saves both admin credentials and player game progress.
     */
    public void saveAllSystemData() {
        saveAdminsToCsv();
        if (playerDataManager != null) {
            playerDataManager.saveAllPlayersToCsv();
        }
    }
}

