package managers;

import accounts.PlayerAcc;
import items.Item;

import java.util.LinkedList;
import java.util.Random;

/**
 * CrateManager handles all crate opening logic for Crateopia.
 *
 * Crate Tier Probabilities (rolled once per open):
 *   Wooden  - 75%  |  shard bonus chance 0.5%
 *   Silver  - 18%  |  shard bonus chance 2%
 *   Gold    -  6%  |  shard bonus chance 5%
 *   Diamond -  1%  |  shard bonus chance 12%
 */
public class CrateManager {

    // Crate names (for display)
    public static final String WOODEN  = "Wooden Crate";
    public static final String SILVER  = "Silver Crate";
    public static final String GOLD    = "Gold Crate";
    public static final String DIAMOND = "Diamond Crate";

    private final ItemDataManager itemData;
    private final Random rng = new Random();

    public CrateManager(ItemDataManager itemData) {
        this.itemData = itemData;
    }

    // -------------------------------------------------------------------------
    // Tier roll
    // -------------------------------------------------------------------------

    /**
     * Rolls a random crate tier based on the probability table:
     *   Diamond 1%, Gold 6%, Silver 18%, Wooden 75%.
     */
    public String rollCrateTier() {
        int roll = rng.nextInt(100); // 0–99
        if (roll < 1)        return DIAMOND;  // 1%
        else if (roll < 7)   return GOLD;     // 6%
        else if (roll < 25)  return SILVER;   // 18%
        else                 return WOODEN;   // 75%
    }

    // -------------------------------------------------------------------------
    // Open crate – returns a CrateResult describing all rewards
    // -------------------------------------------------------------------------

    /**
     * Opens a crate of the given tier and applies all rewards to the player.
     *
     * @param crateTier  One of WOODEN, SILVER, GOLD, DIAMOND constants.
     * @param player     The player who receives the rewards.
     * @return           A CrateResult describing every reward gained.
     */
    public CrateResult openCrate(String crateTier, PlayerAcc player) {
        CrateResult result = new CrateResult(crateTier);

        switch (crateTier) {
            case WOODEN:
                rollWoodenCrate(result, player);
                break;
            case SILVER:
                rollSilverCrate(result, player);
                break;
            case GOLD:
                rollGoldCrate(result, player);
                break;
            case DIAMOND:
                rollDiamondCrate(result, player);
                break;
            default:
                rollWoodenCrate(result, player);
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // Per-crate loot logic
    // -------------------------------------------------------------------------

    /** Wooden Crate loot table */
    private void rollWoodenCrate(CrateResult result, PlayerAcc player) {
        // Coins: always 200-700
        int coins = randInt(200, 700);
        awardCoins(player, result, coins);

        // Common weapon (always)
        Item weapon = pickRandom(itemData.getWeaponsByRarity("Common"));
        if (weapon != null) { result.addItem(weapon); player.addItem(weapon); }

        // Common armor (always)
        Item armor = pickRandom(itemData.getArmorsByRarity("Common"));
        if (armor != null) { result.addItem(armor); player.addItem(armor); }

        // Rare equipment: 15% chance
        if (chance(15)) {
            Item rare = pickRandom(itemData.getAllEquipmentByRarity("Rare"));
            if (rare != null) { result.addItem(rare); player.addItem(rare); }
        }

        // Shards: 2% chance → 1 shard
        if (chance(2)) {
            int shards = 1;
            awardShards(player, result, shards);
        }
    }

    /** Silver Crate loot table */
    private void rollSilverCrate(CrateResult result, PlayerAcc player) {
        // Coins: always 800-1000
        int coins = randInt(800, 1000);
        awardCoins(player, result, coins);

        // Rare weapon: 20% chance
        if (chance(20)) {
            Item weapon = pickRandom(itemData.getWeaponsByRarity("Rare"));
            if (weapon != null) { result.addItem(weapon); player.addItem(weapon); }
        }

        // Rare equipment (armor/accessory): 25% chance
        if (chance(25)) {
            // Pool is armor + accessories only (weapons handled separately above)
            Item equip = pickRandom(itemData.getArmorsByRarity("Rare"));
            if (equip == null) equip = pickRandom(itemData.getAccessoriesByRarity("Rare"));
            if (equip != null) { result.addItem(equip); player.addItem(equip); }
        }

        // Shards: 10% chance → 1-3 shards
        if (chance(10)) {
            int shards = randInt(1, 3);
            awardShards(player, result, shards);
        }
    }

    /** Gold Crate loot table */
    private void rollGoldCrate(CrateResult result, PlayerAcc player) {
        // Coins: always 1500-2500
        int coins = randInt(1500, 2500);
        awardCoins(player, result, coins);

        // Epic equipment (armor/accessory): 45% chance
        if (chance(45)) {
            Item equip = pickRandom(itemData.getArmorsByRarity("Epic"));
            if (equip == null) equip = pickRandom(itemData.getAccessoriesByRarity("Epic"));
            if (equip != null) { result.addItem(equip); player.addItem(equip); }
        }

        // Epic weapon: 50% chance
        if (chance(50)) {
            Item weapon = pickRandom(itemData.getWeaponsByRarity("Epic"));
            if (weapon != null) { result.addItem(weapon); player.addItem(weapon); }
        }

        // Shards: 40% chance → 3-5 shards
        if (chance(40)) {
            int shards = randInt(3, 5);
            awardShards(player, result, shards);
        }
    }

    /** Diamond Crate loot table */
    private void rollDiamondCrate(CrateResult result, PlayerAcc player) {
        // Coins: always 5000-10000
        int coins = randInt(5000, 10000);
        awardCoins(player, result, coins);

        // Legendary equipment (armor/accessory): 50% chance
        if (chance(50)) {
            Item equip = pickRandom(itemData.getArmorsByRarity("Legendary"));
            if (equip == null) equip = pickRandom(itemData.getAccessoriesByRarity("Legendary"));
            if (equip != null) { result.addItem(equip); player.addItem(equip); }
        }

        // Legendary weapon: 60% chance
        if (chance(60)) {
            Item weapon = pickRandom(itemData.getWeaponsByRarity("Legendary"));
            if (weapon != null) { result.addItem(weapon); player.addItem(weapon); }
        }

        // Shards: 50% chance → 5-9 shards
        if (chance(50)) {
            int shards = randInt(5, 9);
            awardShards(player, result, shards);
        }
    }

    // -------------------------------------------------------------------------
    // Helper utilities
    // -------------------------------------------------------------------------

    /** Returns true with the given integer percentage probability (0-100). */
    private boolean chance(int percent) {
        return rng.nextInt(100) < percent;
    }

    /** Returns a random int between min and max (inclusive). */
    private int randInt(int min, int max) {
        return min + rng.nextInt(max - min + 1);
    }

    /** Picks a random element from the linked list. Returns null if list is empty. */
    private <T> T pickRandom(LinkedList<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(rng.nextInt(list.size()));
    }

    /** Adds coins to the player and records them in the result. */
    private void awardCoins(PlayerAcc player, CrateResult result, int amount) {
        player.setCoins(player.getCoins() + amount);
        result.setCoinsEarned(amount);
    }

    /** Adds shards to the player and records them in the result. */
    private void awardShards(PlayerAcc player, CrateResult result, int amount) {
        player.setShards(player.getShards() + amount);
        result.setShardsEarned(amount);
    }

    // =========================================================================
    // Inner class: CrateResult
    // =========================================================================

    /**
     * Value object describing everything that came out of one crate open.
     * Uses LinkedList adhering to DSA requirements.
     */
    public static class CrateResult {
        private final String crateTier;
        private int coinsEarned  = 0;
        private int shardsEarned = 0;
        private final java.util.LinkedList<Item> items = new java.util.LinkedList<>();

        public CrateResult(String crateTier) {
            this.crateTier = crateTier;
        }

        public void setCoinsEarned(int coins)   { this.coinsEarned  = coins;  }
        public void setShardsEarned(int shards) { this.shardsEarned = shards; }
        public void addItem(Item item)           { items.add(item); }

        public String    getCrateTier()   { return crateTier; }
        public int       getCoinsEarned() { return coinsEarned; }
        public int       getShardsEarned(){ return shardsEarned; }
        public java.util.LinkedList<Item> getItems() { return items; }

        /** Returns true if the result contains at least one shard reward. */
        public boolean hasShards()        { return shardsEarned > 0; }
    }
}
