package managers;

import accounts.AdminAcc;
import accounts.PlayerAcc;
import characters.Hero;
import items.Item;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * AdminDataManager – everything the admin side can DO to the game data.
 *
 * It does no printing and asks no questions (that is the menus' job in Main).
 * It only changes data and saves it, so the menus stay short and this logic
 * can be reused. It sits on top of the other managers:
 *   PlayerDataManager (players), ItemDataManager (items), CharacterDataManager (heroes).
 *
 * Admin accounts are loaded from data/admins.csv (username,password).
 * If that file does not exist, it is created with one default admin.
 */
public class AdminDataManager {

    private static final String ADMINS_CSV = "data/admins.csv";
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    /** Largest coin / shard balance an admin can set (keeps int math safe). */
    public static final int MAX_BALANCE = 1_000_000_000;

    private final LinkedList<AdminAcc> admins = new LinkedList<>();
    private final PlayerDataManager playerData;
    private final ItemDataManager itemData;
    private final CharacterDataManager charData;

    public AdminDataManager(PlayerDataManager playerData, ItemDataManager itemData,
                            CharacterDataManager charData) {
        this.playerData = playerData;
        this.itemData = itemData;
        this.charData = charData;
        loadAdmins();
    }

    // -------------------------------------------------------------------------
    // Admin credentials & login
    // -------------------------------------------------------------------------

    private void loadAdmins() {
        File file = new File(ADMINS_CSV);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();   // skip header
                while ((line = reader.readLine()) != null) {
                    String[] p = line.trim().split(",", -1);
                    if (p.length >= 2 && !p[0].trim().isEmpty()) {
                        admins.add(new AdminAcc(p[0].trim(), p[1].trim()));
                    }
                }
            } catch (IOException e) {
                System.err.println("[AdminDataManager] Could not read admins.csv: " + e.getMessage());
            }
        }
        if (admins.isEmpty()) {   // first run (or empty file): create the default admin
            admins.add(new AdminAcc(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD));
            saveAdmins();
        }
    }

    private void saveAdmins() {
        File dir = new File(ADMINS_CSV).getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        try (PrintWriter out = new PrintWriter(new FileWriter(ADMINS_CSV, false))) {
            out.println("username,password");
            for (AdminAcc a : admins) out.println(a.getUsername() + "," + a.getPassword());
        } catch (IOException e) {
            System.err.println("[AdminDataManager] Could not save admins.csv: " + e.getMessage());
        }
    }

    /** @return the matching AdminAcc, or null if the username/password is wrong. */
    public AdminAcc login(String username, String password) {
        for (AdminAcc admin : admins) {
            if (admin.login(username, password)) return admin;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Players: view / search
    // -------------------------------------------------------------------------

    public LinkedList<PlayerAcc> getAllPlayers() {
        return playerData.getPlayerList();
    }

    public PlayerAcc getPlayer(String username) {
        return playerData.getPlayer(username);
    }

    /** Players whose username CONTAINS the text (case-insensitive). */
    public LinkedList<PlayerAcc> searchPlayers(String text) {
        LinkedList<PlayerAcc> matches = new LinkedList<>();
        String needle = text.toLowerCase();
        for (PlayerAcc p : playerData.getPlayerList()) {
            if (p.getUsername().toLowerCase().contains(needle)) matches.add(p);
        }
        return matches;
    }

    // -------------------------------------------------------------------------
    // Players: edit balances (set = replace, grant = add on top)
    // -------------------------------------------------------------------------

    private int clampBalance(long value) {
        return (int) Math.max(0, Math.min(MAX_BALANCE, value));
    }

    public void setCoins(PlayerAcc p, int amount)   { p.setCoins(clampBalance(amount));   save(); }
    public void setShards(PlayerAcc p, int amount)  { p.setShards(clampBalance(amount));  save(); }
    public void grantCoins(PlayerAcc p, int amount) { p.setCoins(clampBalance((long) p.getCoins() + amount));   save(); }
    public void grantShards(PlayerAcc p, int amount){ p.setShards(clampBalance((long) p.getShards() + amount)); save(); }

    // -------------------------------------------------------------------------
    // Players: edit heroes and items
    // -------------------------------------------------------------------------

    /** Gives the player a fresh copy of a hero. False if the hero ID is unknown or already owned. */
    public boolean addHeroToPlayer(PlayerAcc p, String heroId) {
        Hero hero = charData.createNewHeroInstance(heroId);
        if (hero == null || !p.addHero(hero)) return false;
        save();
        return true;
    }

    public void removeHeroFromPlayer(PlayerAcc p, Hero hero) {
        detachHero(p, hero);
        save();
    }

    /** Removes the hero; whatever it was wearing goes back into the player's inventory. */
    private void detachHero(PlayerAcc p, Hero hero) {
        for (String slot : new String[]{"weapon", "helmet", "chest", "leggings", "accessory"}) {
            Item gear = hero.unequip(slot);
            if (gear != null) p.addItem(gear);
        }
        p.removeHero(hero);
    }

    /** Gives the player a fresh copy of an item. False if the item ID is unknown or already owned. */
    public boolean addItemToPlayer(PlayerAcc p, String itemId) {
        Item item = itemData.createNewItemInstance(itemId);
        if (item == null || !p.addItem(item)) return false;
        save();
        return true;
    }

    public void removeItemFromPlayer(PlayerAcc p, Item item) {
        p.removeItem(item);
        save();
    }

    // -------------------------------------------------------------------------
    // Players: ban, reset password, delete
    // -------------------------------------------------------------------------

    public void setBanned(PlayerAcc p, boolean banned) {
        p.setBanned(banned);
        save();
    }

    public void resetPassword(PlayerAcc p, String newPassword) {
        p.setPassword(newPassword);
        save();
    }

    public boolean deletePlayer(String username) {
        return playerData.deletePlayer(username);
    }

    // -------------------------------------------------------------------------
    // Catalog: items and heroes
    // (after an edit/delete the players are rebuilt so they get the new stats)
    // -------------------------------------------------------------------------

    public boolean addItem(Item item) {
        return itemData.addItem(item);
    }

    public boolean updateItem(Item item) {
        boolean ok = itemData.updateItem(item);
        if (ok) syncPlayersWithCatalog();
        return ok;
    }

    /** Deletes the item from the catalog AND from every player's inventory / equipped gear. */
    public boolean deleteItem(String itemId) {
        boolean ok = itemData.deleteItem(itemId);
        if (ok) syncPlayersWithCatalog();
        return ok;
    }

    public boolean addHero(Hero hero) {
        return charData.addHero(hero);
    }

    public boolean updateHero(Hero hero) {
        boolean ok = charData.updateHero(hero);
        if (ok) syncPlayersWithCatalog();
        return ok;
    }

    /** Deletes the hero template; owners lose the hero but keep the gear it was wearing. */
    public boolean deleteHero(String heroId) {
        for (PlayerAcc p : playerData.getPlayerList()) {
            LinkedList<Hero> owned = new LinkedList<>();
            for (Hero h : p.getRoster()) {
                if (h.getId().equalsIgnoreCase(heroId)) owned.add(h);   // collect first, remove after
            }
            for (Hero h : owned) detachHero(p, h);
        }
        boolean ok = charData.deleteHero(heroId);
        if (ok) syncPlayersWithCatalog();
        return ok;
    }

    /**
     * Players hold their own copies of items/heroes, so after the catalog changes
     * we save, reload everything from the CSV files (which builds fresh copies from
     * the NEW catalog, dropping anything that no longer exists), and save again.
     */
    private void syncPlayersWithCatalog() {
        playerData.saveAllPlayersToCsv();
        playerData.loadAllData();
        playerData.saveAllPlayersToCsv();
    }

    // -------------------------------------------------------------------------
    // Statistics
    // -------------------------------------------------------------------------

    public int getTotalPlayers() { return playerData.getPlayerList().size(); }

    public int getBannedCount() {
        int count = 0;
        for (PlayerAcc p : playerData.getPlayerList()) if (p.isBanned()) count++;
        return count;
    }

    public long getTotalCoins() {
        long sum = 0;
        for (PlayerAcc p : playerData.getPlayerList()) sum += p.getCoins();
        return sum;
    }

    public long getTotalShards() {
        long sum = 0;
        for (PlayerAcc p : playerData.getPlayerList()) sum += p.getShards();
        return sum;
    }

    public int getTotalHeroesOwned() {
        int sum = 0;
        for (PlayerAcc p : playerData.getPlayerList()) sum += p.getRoster().size();
        return sum;
    }

    public int getTotalItemsOwned() {
        int sum = 0;
        for (PlayerAcc p : playerData.getPlayerList()) sum += p.getInventory().size();
        return sum;
    }

    /**
     * The best 'n' players according to a comparator ("best" = comes first).
     *
     * DSA note – PriorityQueue: a heap that always hands back the "smallest"
     * element by the comparator first, so poll() gives us the best player each time.
     */
    public LinkedList<PlayerAcc> getTopPlayers(Comparator<PlayerAcc> bestFirst, int n) {
        PriorityQueue<PlayerAcc> heap = new PriorityQueue<>(bestFirst);
        heap.addAll(playerData.getPlayerList());
        LinkedList<PlayerAcc> top = new LinkedList<>();
        while (!heap.isEmpty() && top.size() < n) top.add(heap.poll());
        return top;
    }

    private void save() {
        playerData.saveAllPlayersToCsv();
    }
}
