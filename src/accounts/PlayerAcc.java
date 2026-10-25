package accounts;

import characters.Hero;
import items.Item;
import java.util.LinkedList;

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

    // --- Currency ---
    private int coins;   // in-game gold used in the shop
    private int shards;  // premium currency used for hero gacha pulls

    // --- Statistics (tracked for display on the home screen) ---
    private int heroesCollected; // total heroes ever added
    private int itemsOwned;      // total items ever added

    // --- DSA: LinkedList for Inventory & Roster ---
    // LinkedList<Item>  → stores all unequipped gear the player owns
    // LinkedList<Hero>  → stores all heroes the player has summoned
    private final LinkedList<Item> inventory = new LinkedList<>();
    private final LinkedList<Hero> roster    = new LinkedList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a brand-new player account with default starting values:
     *   500 coins, 10 shards, 0 heroes, 0 items.
     *
     * @param username  Chosen username.
     * @param password  Chosen password.
     */
    public PlayerAcc(String username, String password) {
        // Delegate to the full constructor with default values.
        this(username, password, 500, 10, 0, 0);
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
        super(username, password); // pass username & password up to Account
        this.coins           = coins;
        this.shards          = shards;
        this.heroesCollected = heroesCollected;
        this.itemsOwned      = itemsOwned;
    }

    // -------------------------------------------------------------------------
    // Currency Getters & Setters
    // -------------------------------------------------------------------------

    public int getCoins()           { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public int getShards()              { return shards; }
    public void setShards(int shards)   { this.shards = shards; }

    // -------------------------------------------------------------------------
    // Statistics Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * Returns the total heroes collected.
     * Math.max ensures the count is never less than the actual roster size
     * (in case saved data is out of sync with the live list).
     */
    public int getHeroesCollected() {
        return Math.max(heroesCollected, roster.size());
    }

    public void setHeroesCollected(int heroesCollected) {
        this.heroesCollected = heroesCollected;
    }

    /**
     * Returns the total items owned.
     * Math.max keeps the count accurate even if saved data lags behind.
     */
    public int getItemsOwned() {
        return Math.max(itemsOwned, inventory.size());
    }

    public void setItemsOwned(int itemsOwned) {
        this.itemsOwned = itemsOwned;
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
            this.itemsOwned = inventory.size(); // keep the count in sync
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
            this.itemsOwned = inventory.size();
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
     * Adds a hero to the roster ONLY if the player does not already have a
     * hero with the same ID.
     *
     * @param hero  The hero to add.
     * @return      true if the hero was added; false if duplicate.
     */
    public boolean addHero(Hero hero) {
        if (hero != null && !hasHero(hero.getId())) {
            roster.add(hero);                        // add to tail of the LinkedList
            this.heroesCollected = roster.size();    // keep the count in sync
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
            this.heroesCollected = roster.size();
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
