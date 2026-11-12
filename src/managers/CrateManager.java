package managers;

import accounts.PlayerAcc;
import items.Item;

import java.util.LinkedList;
import java.util.Random;

/**
 * CrateManager – handles all crate opening logic for Crateopia.
 *
 * OOP Concepts demonstrated here
 * --------------------------------
 * ENCAPSULATION:
 *   The per-crate loot tables (rollWoodenCrate, rollSilverCrate, …) are
 *   private methods.  Callers only interact with the public openCrate() and
 *   rollCrateTier() methods — they never need to know how loot is decided.
 *
 * INNER CLASSES:
 *   CrateResult and DuplicateEntry are public static nested classes so the
 *   result data can be read by Main without exposing internal implementation.
 *
 * DSA Concepts demonstrated here
 * --------------------------------
 * LinkedList<Item> items (inside CrateResult):
 *   Stores items earned from one crate opening in the order they were awarded.
 *
 * LinkedList<DuplicateEntry> duplicates (inside CrateResult):
 *   Stores items that were duplicates (already owned) and were converted to coins.
 *
 * Crate Tier Probabilities (rolled once per open):
 *   Diamond - 1%   | Gold - 6%   | Silver - 18%   | Wooden - 75%
 *
 * Duplicate item coin compensation (by rarity):
 *   Common → 500 coins   | Rare → 1 000 coins
 *   Epic   → 2 000 coins | Legendary → 10 000 coins
 */
public class CrateManager {

    // Crate tier name constants — used as both display strings and switch keys.
    public static final String WOODEN  = "Wooden Crate";
    public static final String SILVER  = "Silver Crate";
    public static final String GOLD    = "Gold Crate";
    public static final String DIAMOND = "Diamond Crate";

    // Default crate tier drop rates (percentage out of 100)
    public static final int DEFAULT_WOODEN_RATE  = 75;
    public static final int DEFAULT_SILVER_RATE  = 18;
    public static final int DEFAULT_GOLD_RATE    = 6;
    public static final int DEFAULT_DIAMOND_RATE = 1;

    // Configurable crate tier drop rates
    private int woodenRate  = DEFAULT_WOODEN_RATE;
    private int silverRate  = DEFAULT_SILVER_RATE;
    private int goldRate    = DEFAULT_GOLD_RATE;
    private int diamondRate = DEFAULT_DIAMOND_RATE;

    // Configurable Wooden Crate loot parameters
    private int woodenMinCoins = 200;
    private int woodenMaxCoins = 700;
    private int woodenRareChance = 15;
    private int woodenShardChance = 2;

    // Configurable Silver Crate loot parameters
    private int silverMinCoins = 800;
    private int silverMaxCoins = 1000;
    private int silverRareWeaponChance = 20;
    private int silverRareArmorChance = 25;
    private int silverShardChance = 10;

    // Configurable Gold Crate loot parameters
    private int goldMinCoins = 1500;
    private int goldMaxCoins = 2500;
    private int goldEpicArmorChance = 45;
    private int goldEpicWeaponChance = 50;
    private int goldShardChance = 40;

    // Configurable Diamond Crate loot parameters
    private int diamondMinCoins = 5000;
    private int diamondMaxCoins = 10000;
    private int diamondLegArmorChance = 50;
    private int diamondLegWeaponChance = 60;
    private int diamondShardChance = 50;

    // Coin compensation for duplicate items (private = only used inside this class)
    private static final int DUP_COINS_COMMON    = 500;
    private static final int DUP_COINS_RARE      = 1_000;
    private static final int DUP_COINS_EPIC      = 2_000;
    private static final int DUP_COINS_LEGENDARY = 10_000;

    private final ItemDataManager itemData; // source of all item pools
    private final Random rng = new Random(); // shared RNG for all rolls

    /**
     * Constructor – injects the ItemDataManager dependency.
     *
     * @param itemData  The shared item data source.
     */
    public CrateManager(ItemDataManager itemData) {
        this.itemData = itemData;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Rolls which crate tier the player gets when they open a crate.
     *
     * Probability table (cumulative):
     *   roll 0         → Diamond (1%)
     *   roll 1–6       → Gold    (6%)
     *   roll 7–24      → Silver  (18%)
     *   roll 25–99     → Wooden  (75%)
     *
     * DSA note – similar to gacha star-tier rolling: we generate one random
     * integer and check cumulative buckets from rarest to most common.
     *
     * @return  One of DIAMOND, GOLD, SILVER, or WOODEN.
     */
    public String rollCrateTier() {
        int roll = rng.nextInt(100); // 0–99
        if (roll < diamondRate) return DIAMOND;
        if (roll < diamondRate + goldRate) return GOLD;
        if (roll < diamondRate + goldRate + silverRate) return SILVER;
        return WOODEN;
    }

    // -------------------------------------------------------------------------
    // Crate Drop Rate & Loot Table Configuration (Admin)
    // -------------------------------------------------------------------------

    public int getWoodenRate()  { return woodenRate; }
    public int getSilverRate()  { return silverRate; }
    public int getGoldRate()    { return goldRate; }
    public int getDiamondRate() { return diamondRate; }

    /**
     * Updates the crate tier drop rates. The rates must sum to 100.
     */
    public boolean setCrateTierRates(int wooden, int silver, int gold, int diamond) {
        if (wooden < 0 || silver < 0 || gold < 0 || diamond < 0) return false;
        if (wooden + silver + gold + diamond != 100) return false;
        this.woodenRate  = wooden;
        this.silverRate  = silver;
        this.goldRate    = gold;
        this.diamondRate = diamond;
        return true;
    }

    public int getWoodenMinCoins() { return woodenMinCoins; }
    public int getWoodenMaxCoins() { return woodenMaxCoins; }
    public int getWoodenRareChance() { return woodenRareChance; }
    public int getWoodenShardChance() { return woodenShardChance; }
    public void setWoodenLoot(int minCoins, int maxCoins, int rareChance, int shardChance) {
        this.woodenMinCoins = Math.max(0, minCoins);
        this.woodenMaxCoins = Math.max(this.woodenMinCoins, maxCoins);
        this.woodenRareChance = Math.max(0, Math.min(100, rareChance));
        this.woodenShardChance = Math.max(0, Math.min(100, shardChance));
    }

    public int getSilverMinCoins() { return silverMinCoins; }
    public int getSilverMaxCoins() { return silverMaxCoins; }
    public int getSilverRareWeaponChance() { return silverRareWeaponChance; }
    public int getSilverRareArmorChance() { return silverRareArmorChance; }
    public int getSilverShardChance() { return silverShardChance; }
    public void setSilverLoot(int minCoins, int maxCoins, int weaponChance, int armorChance, int shardChance) {
        this.silverMinCoins = Math.max(0, minCoins);
        this.silverMaxCoins = Math.max(this.silverMinCoins, maxCoins);
        this.silverRareWeaponChance = Math.max(0, Math.min(100, weaponChance));
        this.silverRareArmorChance = Math.max(0, Math.min(100, armorChance));
        this.silverShardChance = Math.max(0, Math.min(100, shardChance));
    }

    public int getGoldMinCoins() { return goldMinCoins; }
    public int getGoldMaxCoins() { return goldMaxCoins; }
    public int getGoldEpicArmorChance() { return goldEpicArmorChance; }
    public int getGoldEpicWeaponChance() { return goldEpicWeaponChance; }
    public int getGoldShardChance() { return goldShardChance; }
    public void setGoldLoot(int minCoins, int maxCoins, int armorChance, int weaponChance, int shardChance) {
        this.goldMinCoins = Math.max(0, minCoins);
        this.goldMaxCoins = Math.max(this.goldMinCoins, maxCoins);
        this.goldEpicArmorChance = Math.max(0, Math.min(100, armorChance));
        this.goldEpicWeaponChance = Math.max(0, Math.min(100, weaponChance));
        this.goldShardChance = Math.max(0, Math.min(100, shardChance));
    }

    public int getDiamondMinCoins() { return diamondMinCoins; }
    public int getDiamondMaxCoins() { return diamondMaxCoins; }
    public int getDiamondLegArmorChance() { return diamondLegArmorChance; }
    public int getDiamondLegWeaponChance() { return diamondLegWeaponChance; }
    public int getDiamondShardChance() { return diamondShardChance; }
    public void setDiamondLoot(int minCoins, int maxCoins, int armorChance, int weaponChance, int shardChance) {
        this.diamondMinCoins = Math.max(0, minCoins);
        this.diamondMaxCoins = Math.max(this.diamondMinCoins, maxCoins);
        this.diamondLegArmorChance = Math.max(0, Math.min(100, armorChance));
        this.diamondLegWeaponChance = Math.max(0, Math.min(100, weaponChance));
        this.diamondShardChance = Math.max(0, Math.min(100, shardChance));
    }

    /**
     * Resets all crate tier rates and loot tables back to factory default values.
     */
    public void resetAllCrateRatesToDefault() {
        this.woodenRate  = DEFAULT_WOODEN_RATE;
        this.silverRate  = DEFAULT_SILVER_RATE;
        this.goldRate    = DEFAULT_GOLD_RATE;
        this.diamondRate = DEFAULT_DIAMOND_RATE;

        this.woodenMinCoins = 200;
        this.woodenMaxCoins = 700;
        this.woodenRareChance = 15;
        this.woodenShardChance = 2;

        this.silverMinCoins = 800;
        this.silverMaxCoins = 1000;
        this.silverRareWeaponChance = 20;
        this.silverRareArmorChance = 25;
        this.silverShardChance = 10;

        this.goldMinCoins = 1500;
        this.goldMaxCoins = 2500;
        this.goldEpicArmorChance = 45;
        this.goldEpicWeaponChance = 50;
        this.goldShardChance = 40;

        this.diamondMinCoins = 5000;
        this.diamondMaxCoins = 10000;
        this.diamondLegArmorChance = 50;
        this.diamondLegWeaponChance = 60;
        this.diamondShardChance = 50;
    }

    /**
     * Opens a crate of the given tier and applies all rewards to the player.
     * Returns a CrateResult describing every reward (coins, shards, items).
     *
     * The switch statement dispatches to the correct private loot method.
     * Unknown tiers default to the Wooden crate loot table.
     *
     * @param crateTier  One of the WOODEN / SILVER / GOLD / DIAMOND constants.
     * @param player     The player who receives the rewards.
     * @return           A CrateResult with all reward details.
     */
    public CrateResult openCrate(String crateTier, PlayerAcc player) {
        CrateResult result = new CrateResult(crateTier);

        switch (crateTier) {
            case WOODEN:  rollWoodenCrate(result, player);  break;
            case SILVER:  rollSilverCrate(result, player);  break;
            case GOLD:    rollGoldCrate(result, player);    break;
            case DIAMOND: rollDiamondCrate(result, player); break;
            default:
                // Unknown tier — fall back to Wooden as a safe default
                rollWoodenCrate(result, player);
        }

        return result;
    }

    // =========================================================================
    // Per-crate Loot Tables (private — hidden from outside)
    // =========================================================================

    /**
     * Wooden Crate loot table.
     *   – Always: 200–700 coins
     *   – Always: 1 Common weapon + 1 Common armor
     *   – 15% chance: 1 Rare item (weapon/armor/accessory)
     *   – 2% chance:  1 shard
     */
    private void rollWoodenCrate(CrateResult result, PlayerAcc player) {
        // Coins — always awarded
        int coins = randInt(woodenMinCoins, woodenMaxCoins);
        awardCoins(player, result, coins);

        // Common weapon — always awarded
        Item weapon = pickRandom(itemData.getWeaponsByRarity("Common"));
        awardItem(player, result, weapon);

        // Common armor — always awarded
        Item armor = pickRandom(itemData.getArmorsByRarity("Common"));
        awardItem(player, result, armor);

        // Rare item
        if (chance(woodenRareChance)) {
            Item rare = pickRandom(itemData.getAllEquipmentByRarity("Rare"));
            awardItem(player, result, rare);
        }

        // Shard bonus
        if (chance(woodenShardChance)) {
            awardShards(player, result, 1);
        }
    }

    /**
     * Silver Crate loot table.
     */
    private void rollSilverCrate(CrateResult result, PlayerAcc player) {
        int coins = randInt(silverMinCoins, silverMaxCoins);
        awardCoins(player, result, coins);

        // Rare weapon
        if (chance(silverRareWeaponChance)) {
            Item weapon = pickRandom(itemData.getWeaponsByRarity("Rare"));
            awardItem(player, result, weapon);
        }

        // Rare armor or accessory
        if (chance(silverRareArmorChance)) {
            Item equip = pickRandom(itemData.getArmorsByRarity("Rare"));
            if (equip == null) {
                // If no Rare armors exist, try accessories as fallback
                equip = pickRandom(itemData.getAccessoriesByRarity("Rare"));
            }
            awardItem(player, result, equip);
        }

        // Shards bonus
        if (chance(silverShardChance)) {
            awardShards(player, result, randInt(1, 3));
        }
    }

    /**
     * Gold Crate loot table.
     */
    private void rollGoldCrate(CrateResult result, PlayerAcc player) {
        int coins = randInt(goldMinCoins, goldMaxCoins);
        awardCoins(player, result, coins);

        if (chance(goldEpicArmorChance)) {
            Item equip = pickRandom(itemData.getArmorsByRarity("Epic"));
            if (equip == null) equip = pickRandom(itemData.getAccessoriesByRarity("Epic"));
            awardItem(player, result, equip);
        }

        if (chance(goldEpicWeaponChance)) {
            Item weapon = pickRandom(itemData.getWeaponsByRarity("Epic"));
            awardItem(player, result, weapon);
        }

        if (chance(goldShardChance)) {
            awardShards(player, result, randInt(3, 5));
        }
    }

    /**
     * Diamond Crate loot table.
     */
    private void rollDiamondCrate(CrateResult result, PlayerAcc player) {
        int coins = randInt(diamondMinCoins, diamondMaxCoins);
        awardCoins(player, result, coins);

        if (chance(diamondLegArmorChance)) {
            Item equip = pickRandom(itemData.getArmorsByRarity("Legendary"));
            if (equip == null) equip = pickRandom(itemData.getAccessoriesByRarity("Legendary"));
            awardItem(player, result, equip);
        }

        if (chance(diamondLegWeaponChance)) {
            Item weapon = pickRandom(itemData.getWeaponsByRarity("Legendary"));
            awardItem(player, result, weapon);
        }

        if (chance(diamondShardChance)) {
            awardShards(player, result, randInt(5, 9));
        }
    }

    // =========================================================================
    // Private Helper Utilities
    // =========================================================================

    /**
     * Returns true with the given percentage probability.
     * Example: chance(15) → true approximately 15% of the time.
     *
     * @param percent  Integer probability 0–100.
     * @return         true if the random roll is below the threshold.
     */
    private boolean chance(int percent) {
        return rng.nextInt(100) < percent;
    }

    /**
     * Returns a random integer between min and max (inclusive).
     * Example: randInt(200, 700) can return any value from 200 to 700.
     *
     * @param min  Minimum value (inclusive).
     * @param max  Maximum value (inclusive).
     * @return     A random int in [min, max].
     */
    private int randInt(int min, int max) {
        return min + rng.nextInt(max - min + 1);
    }

    /**
     * Picks a uniformly random element from a LinkedList.
     * Returns null if the list is null or empty (safe — callers handle null).
     *
     * DSA note – LinkedList.get(index) is O(n) because it walks node pointers.
     * This is acceptable here because item pools are typically small (< 50 items).
     *
     * @param list  The list to pick from.
     * @param <T>   The element type.
     * @return      A random element, or null if the list is empty.
     */
    private <T> T pickRandom(LinkedList<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(rng.nextInt(list.size())); // O(n) walk to random index
    }

    /**
     * Adds coins to the player's balance and records them in the result.
     *
     * @param player  The player receiving the coins.
     * @param result  The CrateResult to record coins in.
     * @param amount  The number of coins to award.
     */
    private void awardCoins(PlayerAcc player, CrateResult result, int amount) {
        player.setCoins(player.getCoins() + amount);
        result.setCoinsEarned(result.getCoinsEarned() + amount); // accumulate (may be called multiple times)
    }

    /**
     * Adds shards to the player's balance and records them in the result.
     *
     * @param player  The player receiving the shards.
     * @param result  The CrateResult to record shards in.
     * @param amount  The number of shards to award.
     */
    private void awardShards(PlayerAcc player, CrateResult result, int amount) {
        player.setShards(player.getShards() + amount);
        result.setShardsEarned(amount);
    }

    /**
     * Awards an item to the player.
     * If the player already owns an item with the same ID (duplicate),
     * the item is NOT added; instead the player receives coin compensation
     * based on the item's rarity.
     *
     * @param player  The player to give the item to.
     * @param result  The CrateResult to record the outcome in.
     * @param item    The item to award (may be null — handled gracefully).
     */
    private void awardItem(PlayerAcc player, CrateResult result, Item item) {
        if (item == null) return; // nothing to award

        // PlayerAcc.addItem() returns false when the item is a duplicate
        boolean added = player.addItem(item);
        if (added) {
            result.addItem(item); // record as a normal item reward
        } else {
            // Duplicate — compensate with coins instead
            int compensation = dupCoinsForRarity(item.getRarity());
            awardCoins(player, result, compensation);
            result.addDuplicateItem(item, compensation); // record the conversion
        }
    }

    /**
     * Returns the coin compensation amount for a duplicate item of the given rarity.
     * Defaults to Common compensation for unrecognised rarity strings.
     *
     * @param rarity  Rarity string ("Common", "Rare", "Epic", "Legendary").
     * @return        Coin compensation amount.
     */
    private int dupCoinsForRarity(String rarity) {
        if (rarity == null) return DUP_COINS_COMMON;
        switch (rarity.toLowerCase()) {
            case "legendary": return DUP_COINS_LEGENDARY;
            case "epic":      return DUP_COINS_EPIC;
            case "rare":      return DUP_COINS_RARE;
            default:          return DUP_COINS_COMMON;
        }
    }

    // =========================================================================
    // Inner class: CrateResult
    // =========================================================================

    /**
     * CrateResult – describes everything that came out of one crate opening.
     *
     * OOP Concept – STATIC NESTED CLASS:
     *   Declared inside CrateManager.  It is "static" meaning it can be used
     *   without creating a CrateManager instance (Main just reads the data).
     *
     * DSA: Both items and duplicates are LinkedLists, which gives O(1) add
     *      and allows the UI to iterate in the order rewards were discovered.
     */
    public static class CrateResult {

        private final String crateTier;   // which crate was opened (for display)
        private int coinsEarned  = 0;     // total coins earned this open
        private int shardsEarned = 0;     // total shards earned this open

        // DSA: LinkedList — rewards in the order they were rolled
        private final java.util.LinkedList<Item> items =
                new java.util.LinkedList<>();

        // DSA: LinkedList — items that were duplicates and converted to coins
        private final java.util.LinkedList<DuplicateEntry> duplicates =
                new java.util.LinkedList<>();

        /**
         * @param crateTier  The tier name (e.g. "Gold Crate").
         */
        public CrateResult(String crateTier) {
            this.crateTier = crateTier;
        }

        // --- Setters (called by CrateManager helpers) ---

        /** Sets (replaces) the total coins earned so far in this result. */
        public void setCoinsEarned(int coins)   { this.coinsEarned  = coins; }

        /** Sets (replaces) the total shards earned in this result. */
        public void setShardsEarned(int shards) { this.shardsEarned = shards; }

        /** Adds an item to the rewards list. */
        public void addItem(Item item)          { items.add(item); }

        /**
         * Records a duplicate item that was converted to coins.
         *
         * @param item         The item that was a duplicate.
         * @param coinsAwarded Coins given in place of the item.
         */
        public void addDuplicateItem(Item item, int coinsAwarded) {
            duplicates.add(new DuplicateEntry(item, coinsAwarded));
        }

        // --- Getters ---

        public String getCrateTier()                                 { return crateTier; }
        public int    getCoinsEarned()                               { return coinsEarned; }
        public int    getShardsEarned()                              { return shardsEarned; }
        public java.util.LinkedList<Item> getItems()                 { return items; }
        public java.util.LinkedList<DuplicateEntry> getDuplicates()  { return duplicates; }

        /** Returns true if any item in this result was a duplicate. */
        public boolean hasDuplicates()  { return !duplicates.isEmpty(); }

        /** Returns true if shards were awarded (shard amount > 0). */
        public boolean hasShards()      { return shardsEarned > 0; }

        // =====================================================================
        // Inner class: DuplicateEntry
        // =====================================================================

        /**
         * DuplicateEntry – an immutable snapshot of one item-to-coins conversion.
         *
         * OOP Concept – IMMUTABILITY:
         *   All fields are private & final.  Once created, the record cannot be
         *   changed — it safely describes exactly what happened.
         */
        public static class DuplicateEntry {

            private final Item item;         // the duplicate item
            private final int  coinsAwarded; // coins given instead

            /**
             * @param item         The duplicate item.
             * @param coinsAwarded Coins awarded as compensation.
             */
            public DuplicateEntry(Item item, int coinsAwarded) {
                this.item         = item;
                this.coinsAwarded = coinsAwarded;
            }

            public Item getItem()          { return item; }
            public int  getCoinsAwarded()  { return coinsAwarded; }
        }
    }
}
