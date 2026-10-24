package accounts;

import characters.Hero;
import items.Item;
import java.util.LinkedList;

public class PlayerAcc extends Account {
    private int coins;
    private int shards;
    private int heroesCollected;
    private int itemsOwned;

    // Player inventory and Hero roster using DSA LinkedList
    private final LinkedList<Item> inventory = new LinkedList<>();
    private final LinkedList<Hero> roster = new LinkedList<>();

    public PlayerAcc(String username, String password) {
        this(username, password, 500, 10, 0, 0);
    }

    public PlayerAcc(String username, String password, int coins, int shards, int heroesCollected, int itemsOwned) {
        super(username, password);
        this.coins = coins;
        this.shards = shards;
        this.heroesCollected = heroesCollected;
        this.itemsOwned = itemsOwned;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getShards() {
        return shards;
    }

    public void setShards(int shards) {
        this.shards = shards;
    }

    public int getHeroesCollected() {
        return Math.max(heroesCollected, roster.size());
    }

    public void setHeroesCollected(int heroesCollected) {
        this.heroesCollected = heroesCollected;
    }

    public int getItemsOwned() {
        return Math.max(itemsOwned, inventory.size());
    }

    public void setItemsOwned(int itemsOwned) {
        this.itemsOwned = itemsOwned;
    }

    public LinkedList<Item> getInventory() {
        return inventory;
    }

    public LinkedList<Hero> getRoster() {
        return roster;
    }

    /**
     * Returns true if the player already owns an item with the given ID.
     */
    public boolean hasItem(String itemId) {
        if (itemId == null) return false;
        for (Item it : inventory) {
            if (it.getId().equalsIgnoreCase(itemId)) return true;
        }
        return false;
    }

    /**
     * Adds the item to the inventory only if no item with the same ID is already present.
     * Returns true if the item was added, false if it was a duplicate and skipped.
     */
    public boolean addItem(Item item) {
        if (item != null && !hasItem(item.getId())) {
            inventory.add(item);
            this.itemsOwned = inventory.size();
            return true;
        }
        return false;
    }

    public boolean removeItem(Item item) {
        boolean removed = inventory.remove(item);
        if (removed) {
            this.itemsOwned = inventory.size();
        }
        return removed;
    }

    /**
     * Returns true if the player already owns a hero with the given ID.
     */
    public boolean hasHero(String heroId) {
        if (heroId == null) return false;
        for (Hero h : roster) {
            if (h.getId().equalsIgnoreCase(heroId)) return true;
        }
        return false;
    }

    /**
     * Adds the hero to the roster only if no hero with the same ID is already present.
     * Returns true if the hero was added, false if it was a duplicate and skipped.
     */
    public boolean addHero(Hero hero) {
        if (hero != null && !hasHero(hero.getId())) {
            roster.add(hero);
            this.heroesCollected = roster.size();
            return true;
        }
        return false;
    }

    public boolean removeHero(Hero hero) {
        boolean removed = roster.remove(hero);
        if (removed) {
            this.heroesCollected = roster.size();
        }
        return removed;
    }

    /**
     * Returns all items currently in player inventory that can be equipped by the given hero.
     */
    public LinkedList<Item> getCompatibleItemsForHero(Hero hero) {
        LinkedList<Item> result = new LinkedList<>();
        if (hero == null) return result;
        for (Item it : inventory) {
            if (hero.canEquip(it)) {
                result.add(it);
            }
        }
        return result;
    }
}
