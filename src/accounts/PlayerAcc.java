package accounts;

import characters.Hero;
import items.Armor;
import items.Item;
import java.util.LinkedList;
import java.util.Stack;

/**
 * PlayerAcc – the player account type.
 *
 * OOP Concepts demonstrated here
 * --------------------------------
 * INHERITANCE  : PlayerAcc extends Account, so it inherits username & password.
 * ENCAPSULATION: All fields are private; only the public methods below can
 *                change them.  This prevents other classes from accidentally
 *                setting coins to a negative value, for example.
 *
 * DSA Concepts demonstrated here
 * --------------------------------
 * LinkedList (inventory & roster):
 *   A LinkedList is a chain of "nodes" where each node holds one item and a
 *   pointer to the next node.  It is great for collections that grow and
 *   shrink often (adding/removing heroes & items) because no shifting is
 *   needed — just re-link the pointers.
 */
public class PlayerAcc extends Account {

    // --- Account Status ---
    private boolean isBanned; // whether the account has been banned by an administrator

    // --- Currency ---
    private int coins;   // in-game gold used in the shop
    private int shards;  // premium currency used for hero gacha pulls

    // --- Statistics (tracked for display on the home screen) ---
    private int heroesCollected; // total heroes in roster
    private int itemsOwned;      // total items owned (inventory + equipped gear)

    // Detailed player statistics (crates, summons, economy, achievements)
    private PlayerStats stats = new PlayerStats();

    // --- DSA: LinkedList for Inventory & Roster ---
    // LinkedList<Item>  → stores all unequipped gear the player owns
    // LinkedList<Hero>  → stores all heroes the player has summoned
    private final LinkedList<Item> inventory = new LinkedList<>();
    private final LinkedList<Hero> roster    = new LinkedList<>();

    // --- DSA: Stack (LIFO) for Player Activity History ---
    // Tracks recent gameplay actions in Last-In, First-Out order.
    private final Stack<String> activityHistory = new Stack<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a brand-new player account with default starting values:
     *   500 coins, 10 shards, 0 heroes, 0 items, active (not banned).
     *
     * @param username  Chosen username.
     * @param password  Chosen password.
     */
    public PlayerAcc(String username, String password) {
        // Delegate to the full constructor with default values.
        this(username, password, 500, 10, 0, 0, false);
    }

    /**
     * Creates a player account with specific saved values (used when loading
     * from the CSV save file).
     *
     * @param username         The player's username.
     * @param password         The player's password.
     * @param coins            Current coin balance.
     * @param shards           Current shard balance.
     * @param heroesCollected  Number of heroes collected so far.
     * @param itemsOwned       Number of items owned so far.
     */
    public PlayerAcc(String username, String password,
                     int coins, int shards,
                     int heroesCollected, int itemsOwned) {
        this(username, password, coins, shards, heroesCollected, itemsOwned, false);
    }

    /**
     * Full constructor including ban status.
     *
     * @param username         The player's username.
     * @param password         The player's password.
     * @param coins            Current coin balance.
     * @param shards           Current shard balance.
     * @param heroesCollected  Number of heroes collected so far.
     * @param itemsOwned       Number of items owned so far.
     * @param isBanned         Whether this account is banned.
     */
    public PlayerAcc(String username, String password,
                     int coins, int shards,
                     int heroesCollected, int itemsOwned,
                     boolean isBanned) {
        super(username, password); // pass username & password up to Account
        this.coins           = coins;
        this.shards          = shards;
        this.heroesCollected = heroesCollected;
        this.itemsOwned      = itemsOwned;
        this.isBanned        = isBanned;
    }

    // -------------------------------------------------------------------------
    // Status Getters & Setters
    // -------------------------------------------------------------------------

    public boolean isBanned()               { return isBanned; }
    public void setBanned(boolean isBanned) { this.isBanned = isBanned; }

    // -------------------------------------------------------------------------
    // Currency Getters & Setters
    // -------------------------------------------------------------------------

    public int getCoins()           { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public int getShards()              { return shards; }
    public void setShards(int shards)   { this.shards = shards; }

    // -------------------------------------------------------------------------
    // Statistics & Counters Getters & Setters
    // -------------------------------------------------------------------------

    public PlayerStats getStats() {
        return stats;
    }

    public void setStats(PlayerStats stats) {
        if (stats != null) {
            this.stats = stats;
        }
    }

    // -------------------------------------------------------------------------
    // DSA: Stack (LIFO) Methods for Activity Log
    // -------------------------------------------------------------------------

    public Stack<String> getActivityHistory() {
        return activityHistory;
    }

    public void pushActivity(String activity) {
        if (activity != null && !activity.trim().isEmpty()) {
            activityHistory.push(activity.trim());
        }
    }

    public String peekRecentActivity() {
        return activityHistory.isEmpty() ? "No recent activity" : activityHistory.peek();
    }

    /**
     * Returns all equipment currently equipped across all heroes in the roster.
     */
    public LinkedList<Item> getEquippedItems() {
        LinkedList<Item> equipped = new LinkedList<>();
        for (Hero h : roster) {
            if (h.getEquippedWeapon() != null) {
                equipped.add(h.getEquippedWeapon());
            }
            for (Armor a : h.getEquippedArmors()) {
                if (a != null) {
                    equipped.add(a);
                }
            }
            if (h.getEquippedAccessory() != null) {
                equipped.add(h.getEquippedAccessory());
            }
        }
        return equipped;
    }

    /**
     * Returns the total count of gear items currently equipped across all heroes.
     */
    public int getEquippedItemsCount() {
        return getEquippedItems().size();
    }

    /**
     * Returns the grand total of items owned (both unequipped in bag and equipped on heroes).
     */
    public int getTotalItemsCount() {
        return inventory.size() + getEquippedItemsCount();
    }

    /**
     * Returns a combined list of all items owned by the player (inventory bag + hero equipment).
     */
    public LinkedList<Item> getAllItems() {
        LinkedList<Item> all = new LinkedList<>(inventory);
        all.addAll(getEquippedItems());
        return all;
    }

    /**
     * Synchronizes heroesCollected and itemsOwned counters directly with live
     * roster and inventory + equipment collections to completely prevent any desynchronization.
     */
    public void syncCounters() {
        this.heroesCollected = roster.size();
        this.itemsOwned = inventory.size() + getEquippedItemsCount();
    }

    /**
     * Returns the total heroes in roster.
     * Guaranteed to match roster.size() perfectly.
     */
    public int getHeroesCollected() {
        this.heroesCollected = roster.size();
        return this.heroesCollected;
    }

    public void setHeroesCollected(int heroesCollected) {
        this.heroesCollected = roster.size();
    }

    /**
     * Returns the total items owned (inventory bag + equipped items).
     * Guaranteed to match actual inventory + equipment sizes.
     */
    public int getItemsOwned() {
        this.itemsOwned = inventory.size() + getEquippedItemsCount();
        return this.itemsOwned;
    }

    public void setItemsOwned(int itemsOwned) {
        this.itemsOwned = inventory.size() + getEquippedItemsCount();
    }

    // -------------------------------------------------------------------------
    // DSA: LinkedList access for Inventory & Roster
    // -------------------------------------------------------------------------

    /**
     * Returns the full inventory LinkedList.
     * Other classes (e.g. Main) can iterate over it with a for-each loop.
     */
    public LinkedList<Item> getInventory() {
        return inventory;
    }

    /**
     * Returns the full hero roster LinkedList.
     */
    public LinkedList<Hero> getRoster() {
        return roster;
    }

    // -------------------------------------------------------------------------
    // Item management helpers
    // -------------------------------------------------------------------------

    /**
     * Checks whether the player already owns an item with the given ID.
     * Uses a simple linear search (O(n)) through the LinkedList.
     *
     * DSA Note – Linear Search:
     *   We start at the head of the list and check each node one by one until
     *   we find a match or reach the end.  Because items in a LinkedList have
     *   no guaranteed order, this is the correct approach.
     *
     * @param itemId  The unique ID to search for.
     * @return        true if a matching item was found, false otherwise.
     */
    public boolean hasItem(String itemId) {
        if (itemId == null) return false;
        // Linear search through the inventory LinkedList
        for (Item it : inventory) {
            if (it.getId().equalsIgnoreCase(itemId)) {
                return true; // found it — stop early
            }
        }
        return false; // reached the end without finding it
    }

    /**
     * Retrieves an item from the inventory by its ID.
     * Uses DSA Linear Search (O(n)) through the inventory LinkedList.
     *
     * @param itemId  The unique item ID to find.
     * @return        The matching Item object, or null if not found.
     */
    public Item getItem(String itemId) {
        if (itemId == null) return null;
        for (Item it : inventory) {
            if (it.getId().equalsIgnoreCase(itemId)) {
                return it;
            }
        }
        return null;
    }

    /**
     * Adds an item to the inventory ONLY if the player does not already have
     * an item with the same ID (prevents duplicates in the list).
     *
     * @param item  The item to add.
     * @return      true if the item was added; false if it was a duplicate.
     */
    public boolean addItem(Item item) {
        // Guard: item must exist and must not already be in the inventory
        if (item != null && !hasItem(item.getId())) {
            inventory.add(item);               // LinkedList.add() = add to tail, O(1)
            syncCounters();                    // keep counters perfectly synced
            return true;
        }
        return false; // duplicate — skip it
    }

    /**
     * Removes an item from the inventory (e.g. when the player equips it).
     *
     * @param item  The exact item object to remove.
     * @return      true if the item was found and removed.
     */
    public boolean removeItem(Item item) {
        boolean removed = inventory.remove(item); // LinkedList.remove() = O(n) search then pointer re-link
        if (removed) {
            syncCounters();
        }
        return removed;
    }

    // -------------------------------------------------------------------------
    // Hero management helpers
    // -------------------------------------------------------------------------

    /**
     * Checks whether the player already owns a hero with the given ID.
     * Uses a linear search (O(n)) through the roster LinkedList.
     *
     * @param heroId  The unique ID to search for.
     * @return        true if the hero is already in the roster.
     */
    public boolean hasHero(String heroId) {
        if (heroId == null) return false;
        for (Hero h : roster) {
            if (h.getId().equalsIgnoreCase(heroId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a hero from the roster by unique ID.
     * Uses DSA Linear Search (O(n)) through the roster LinkedList.
     *
     * @param heroId  The unique hero ID to find.
     * @return        The matching Hero object, or null if not found.
     */
    public Hero getHero(String heroId) {
        if (heroId == null) return null;
        for (Hero h : roster) {
            if (h.getId().equalsIgnoreCase(heroId)) {
                return h;
            }
        }
        return null;
    }

    /**
     * Adds a hero to the roster ONLY if the player does not already have a
     * hero with the same ID.
     *
     * @param hero  The hero to add.
     * @return      true if the hero was added; false if duplicate.
     */
    public boolean addHero(Hero hero) {
        if (hero != null && !hasHero(hero.getId())) {
            roster.add(hero);                        // add to tail of the LinkedList
            syncCounters();                          // keep the count in sync
            return true;
        }
        return false;
    }

    /**
     * Removes a hero from the roster.
     *
     * @param hero  The exact hero object to remove.
     * @return      true if the hero was found and removed.
     */
    public boolean removeHero(Hero hero) {
        boolean removed = roster.remove(hero);
        if (removed) {
            syncCounters();
        }
        return removed;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Returns all inventory items that the given hero is allowed to equip.
     * Useful when showing the equip-item selection screen.
     *
     * @param hero  The hero whose class restrictions are checked.
     * @return      A new LinkedList containing only compatible items.
     */
    public LinkedList<Item> getCompatibleItemsForHero(Hero hero) {
        LinkedList<Item> result = new LinkedList<>();
        if (hero == null) return result;

        // Linear search: keep items that pass the hero's class check
        for (Item it : inventory) {
            if (hero.canEquip(it)) {
                result.add(it);
            }
        }
        return result;
    }
}
