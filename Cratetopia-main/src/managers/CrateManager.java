package managers;

import accounts.PlayerAcc;
import items.Item;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    // Using constants avoids typos like "wooden crate" vs "Wooden Crate".
    public static final String WOODEN  = "Wooden Crate";
    public static final String SILVER  = "Silver Crate";
    public static final String GOLD    = "Gold Crate";
    public static final String DIAMOND = "Diamond Crate";

    // Coin compensation for duplicate items (private = only used inside this class)
    private static final int DUP_COINS_COMMON    = 500;
    private static final int DUP_COINS_RARE      = 1_000;
    private static final int DUP_COINS_EPIC      = 2_000;
    private static final int DUP_COINS_LEGENDARY = 10_000;

    public static final String[] TIERS = { WOODEN, SILVER, GOLD, DIAMOND };

    // -------------------------------------------------------------------------
    // Reward tables (the admin can edit these; they are saved to data/crate_config.csv)
    // -------------------------------------------------------------------------

    // Index of each editable number inside CrateConfig. Chances are in PERMILLE
    // (1000 = 100%, 150 = 15%); coins and shards are plain amounts.
    public static final int F_TIER_CHANCE   = 0;  // chance this tier is picked when a crate is opened
    public static final int F_COINS_MIN     = 1;
    public static final int F_COINS_MAX     = 2;
    public static final int F_WEAPON_CHANCE = 3;  // chance to receive a weapon
    public static final int F_ARMOR_CHANCE  = 4;  // chance to receive an armor piece (accessory if none exist)
    public static final int F_BONUS_CHANCE  = 5;  // chance for one extra piece of any equipment
    public static final int F_SHARD_CHANCE  = 6;
    public static final int F_SHARD_MIN     = 7;
    public static final int F_SHARD_MAX     = 8;

    public static final String[] FIELD_LABELS = {
            "Tier chance", "Coins (min)", "Coins (max)", "Weapon chance", "Armor chance",
            "Bonus equipment chance", "Shard chance", "Shards (min)", "Shards (max)"
    };

    /** true = the field is a chance (shown/typed as a percent), false = a plain amount. */
    public static boolean isChanceField(int field) {
        return field == F_TIER_CHANCE || field == F_WEAPON_CHANCE || field == F_ARMOR_CHANCE
                || field == F_BONUS_CHANCE || field == F_SHARD_CHANCE;
    }

    /** One row of the reward table: everything that decides what one crate tier gives. */
    public static class CrateConfig {
        private final String tier;         // e.g. "Wooden Crate"
        private final String itemRarity;   // rarity of the weapon / armor rolls (fixed per tier)
        private final String bonusRarity;  // rarity of the bonus-equipment roll (fixed per tier)
        private final int[] values = new int[FIELD_LABELS.length];

        CrateConfig(String tier, String itemRarity, String bonusRarity, int... startValues) {
            this.tier = tier;
            this.itemRarity = itemRarity;
            this.bonusRarity = bonusRarity;
            System.arraycopy(startValues, 0, values, 0, values.length);
        }

        public String getTier()        { return tier; }
        public String getItemRarity()  { return itemRarity; }
        public String getBonusRarity() { return bonusRarity; }
        public int get(int field)      { return values[field]; }
        private void set(int field, int value) { values[field] = value; }
    }

    private static final String CONFIG_CSV = "data/crate_config.csv";

    // Same order as TIERS. Values follow the F_* order above.
    private static LinkedList<CrateConfig> defaultConfigs() {
        LinkedList<CrateConfig> list = new LinkedList<>();
        //                                tier    rarity       bonus      tier  cMin  cMax  weap  armor bonus shard sMin sMax
        list.add(new CrateConfig(WOODEN,  "Common",    "Rare",      750,  200,  700, 1000, 1000, 150,   20, 1, 1));
        list.add(new CrateConfig(SILVER,  "Rare",      "Epic",      180,  800, 1000,  200,  250,   0,  100, 1, 3));
        list.add(new CrateConfig(GOLD,    "Epic",      "Legendary",  60, 1500, 2500,  500,  450,   0,  400, 3, 5));
        list.add(new CrateConfig(DIAMOND, "Legendary", "Legendary",  10, 5000,10000,  600,  500,   0,  500, 5, 9));
        return list;
    }

    private LinkedList<CrateConfig> configs = defaultConfigs();

    private final ItemDataManager itemData; // source of all item pools
    private final Random rng = new Random(); // shared RNG for all rolls

    /**
     * Constructor – injects the ItemDataManager dependency.
     *
     * @param itemData  The shared item data source.
     */
    public CrateManager(ItemDataManager itemData) {
        this.itemData = itemData;
        loadConfig();   // use the admin's saved reward tables if data/crate_config.csv exists
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Rolls which crate tier the player gets, using each tier's "Tier chance".
     * We walk from the rarest tier (Diamond) down to Wooden, subtracting each
     * chance from the roll until it lands inside a tier's bucket.
     */
    public String rollCrateTier() {
        int total = 0;
        for (CrateConfig c : configs) total += c.get(F_TIER_CHANCE);
        if (total <= 0) return WOODEN;

        int roll = rng.nextInt(total);
        for (int i = configs.size() - 1; i >= 0; i--) {
            CrateConfig c = configs.get(i);
            if (roll < c.get(F_TIER_CHANCE)) return c.getTier();
            roll -= c.get(F_TIER_CHANCE);
        }
        return WOODEN;
    }

    /**
     * Opens one crate of the given tier and awards everything to the player.
     * What can drop (and how often) comes from that tier's CrateConfig row,
     * so the admin can rebalance crates without touching this code.
     */
    public CrateResult openCrate(String crateTier, PlayerAcc player) {
        CrateResult result = new CrateResult(crateTier);
        CrateConfig cfg = getConfig(crateTier);
        if (cfg == null) cfg = getConfig(WOODEN);   // unknown tier -> treat as Wooden

        awardCoins(player, result, randInt(cfg.get(F_COINS_MIN), cfg.get(F_COINS_MAX)));

        if (chance(cfg.get(F_WEAPON_CHANCE))) {
            awardItem(player, result, pickRandom(itemData.getWeaponsByRarity(cfg.getItemRarity())));
        }
        if (chance(cfg.get(F_ARMOR_CHANCE))) {
            Item equip = pickRandom(itemData.getArmorsByRarity(cfg.getItemRarity()));
            if (equip == null) equip = pickRandom(itemData.getAccessoriesByRarity(cfg.getItemRarity()));
            awardItem(player, result, equip);
        }
        if (chance(cfg.get(F_BONUS_CHANCE))) {
            awardItem(player, result, pickRandom(itemData.getAllEquipmentByRarity(cfg.getBonusRarity())));
        }
        if (chance(cfg.get(F_SHARD_CHANCE))) {
            awardShards(player, result, randInt(cfg.get(F_SHARD_MIN), cfg.get(F_SHARD_MAX)));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Admin: reward table editing
    // -------------------------------------------------------------------------

    public LinkedList<CrateConfig> getConfigs() {
        return configs;
    }

    public CrateConfig getConfig(String tier) {
        for (CrateConfig c : configs) {
            if (c.getTier().equalsIgnoreCase(tier)) return c;
        }
        return null;
    }

    /**
     * Sets the four tier chances at once (order: Wooden, Silver, Gold, Diamond).
     * They must add up to exactly 100% (1000 permille), otherwise nothing changes.
     */
    public boolean setTierChances(int[] chances) {
        if (chances == null || chances.length != TIERS.length) return false;
        int sum = 0;
        for (int c : chances) {
            if (c < 0) return false;
            sum += c;
        }
        if (sum != 1000) return false;
        for (int i = 0; i < TIERS.length; i++) {
            getConfig(TIERS[i]).set(F_TIER_CHANCE, chances[i]);
        }
        saveConfig();
        return true;
    }

    /**
     * Changes ONE number in a tier's row. Rejected (returns false) when the value
     * makes no sense: a chance outside 0-100%, a negative amount, or a minimum
     * above its maximum. Tier chances are changed with setTierChances() instead.
     */
    public boolean setValue(String tier, int field, int value) {
        CrateConfig cfg = getConfig(tier);
        if (cfg == null || field == F_TIER_CHANCE || value < 0) return false;
        if (isChanceField(field) && value > 1000) return false;
        if (field == F_COINS_MIN  && value > cfg.get(F_COINS_MAX)) return false;
        if (field == F_COINS_MAX  && value < cfg.get(F_COINS_MIN)) return false;
        if (field == F_SHARD_MIN  && value > cfg.get(F_SHARD_MAX)) return false;
        if (field == F_SHARD_MAX  && value < cfg.get(F_SHARD_MIN)) return false;
        cfg.set(field, value);
        saveConfig();
        return true;
    }

    public void resetToDefaults() {
        configs = defaultConfigs();
        saveConfig();
    }

    private void saveConfig() {
        try (PrintWriter out = new PrintWriter(new FileWriter(CONFIG_CSV, false))) {
            out.println("tier,tier_chance,coins_min,coins_max,weapon_chance,armor_chance,bonus_chance,shard_chance,shard_min,shard_max");
            for (CrateConfig c : configs) {
                StringBuilder row = new StringBuilder(c.getTier());
                for (int f = 0; f < FIELD_LABELS.length; f++) row.append(",").append(c.get(f));
                out.println(row);
            }
        } catch (IOException e) {
            System.err.println("[CrateManager] Failed to save crate config: " + e.getMessage());
        }
    }

    private void loadConfig() {
        File file = new File(CONFIG_CSV);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();   // skip header
            while ((line = reader.readLine()) != null) {
                String[] p = line.trim().split(",");
                CrateConfig cfg = (p.length == FIELD_LABELS.length + 1) ? getConfig(p[0].trim()) : null;
                if (cfg == null) continue;
                for (int f = 0; f < FIELD_LABELS.length; f++) {
                    cfg.set(f, Integer.parseInt(p[f + 1].trim()));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("[CrateManager] Could not read crate config, using defaults: " + e.getMessage());
            configs = defaultConfigs();
        }
    }

    /**
     * Returns true with the given probability, written in permille.
     * Example: chance(150) -> true about 15% of the time (150 out of 1000).
     */
    private boolean chance(int permille) {
        return rng.nextInt(1000) < permille;
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
