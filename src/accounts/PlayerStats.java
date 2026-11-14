package accounts;

/**
 * PlayerStats – tracks detailed player statistics for Crateopia.
 *
 * OOP Concepts demonstrated here
 * --------------------------------
 * ENCAPSULATION:
 *   All statistics fields are private. State can only be modified through
 *   controlled incrementors and setters.
 *
 * Persisted in data/player_statistics.csv.
 */
public class PlayerStats {

    // --- Crate Statistics ---
    private int cratesOpened;
    private int woodenCrates;
    private int silverCrates;
    private int goldCrates;
    private int diamondCrates;

    // --- Summons & Heroes ---
    private int summonsPerformed;
    private int highestHeroRarity; // 3 to 6

    // --- Items & Equipment ---
    private int itemsObtained;
    private int equipmentEquipped;

    // --- Currencies & Economy ---
    private int coinsEarned;
    private int coinsSpent;
    private int shardsEarned;
    private int shardsSpent;

    /**
     * Default constructor with zeroed counters.
     */
    public PlayerStats() {
        this.cratesOpened = 0;
        this.woodenCrates = 0;
        this.silverCrates = 0;
        this.goldCrates = 0;
        this.diamondCrates = 0;
        this.summonsPerformed = 0;
        this.highestHeroRarity = 0;
        this.itemsObtained = 0;
        this.equipmentEquipped = 0;
        this.coinsEarned = 0;
        this.coinsSpent = 0;
        this.shardsEarned = 0;
        this.shardsSpent = 0;
    }

    /**
     * Full constructor for loading from CSV.
     */
    public PlayerStats(int cratesOpened, int summonsPerformed, int itemsObtained,
                       int coinsEarned, int coinsSpent, int shardsEarned, int shardsSpent,
                       int woodenCrates, int silverCrates, int goldCrates, int diamondCrates,
                       int highestHeroRarity, int equipmentEquipped) {
        this.cratesOpened = cratesOpened;
        this.summonsPerformed = summonsPerformed;
        this.itemsObtained = itemsObtained;
        this.coinsEarned = coinsEarned;
        this.coinsSpent = coinsSpent;
        this.shardsEarned = shardsEarned;
        this.shardsSpent = shardsSpent;
        this.woodenCrates = woodenCrates;
        this.silverCrates = silverCrates;
        this.goldCrates = goldCrates;
        this.diamondCrates = diamondCrates;
        this.highestHeroRarity = highestHeroRarity;
        this.equipmentEquipped = equipmentEquipped;
    }

    // -------------------------------------------------------------------------
    // Stat Tracking Helpers
    // -------------------------------------------------------------------------

    public void recordCrateOpened(String tier, int coins, int shards, int itemsCount) {
        this.cratesOpened++;
        if (tier != null) {
            String lower = tier.toLowerCase();
            if (lower.contains("wood")) this.woodenCrates++;
            else if (lower.contains("silver")) this.silverCrates++;
            else if (lower.contains("gold")) this.goldCrates++;
            else if (lower.contains("diamond")) this.diamondCrates++;
        }
        if (coins > 0) this.coinsEarned += coins;
        if (shards > 0) this.shardsEarned += shards;
        if (itemsCount > 0) this.itemsObtained += itemsCount;
    }

    public void recordSummon(int pullCount, int shardsCost, int highestStarsInPull) {
        this.summonsPerformed += pullCount;
        this.shardsSpent += shardsCost;
        if (highestStarsInPull > this.highestHeroRarity) {
            this.highestHeroRarity = highestStarsInPull;
        }
    }

    public void recordCoinsEarned(int amount) {
        if (amount > 0) this.coinsEarned += amount;
    }

    public void recordCoinsSpent(int amount) {
        if (amount > 0) this.coinsSpent += amount;
    }

    public void recordShardsEarned(int amount) {
        if (amount > 0) this.shardsEarned += amount;
    }

    public void recordShardsSpent(int amount) {
        if (amount > 0) this.shardsSpent += amount;
    }

    public void recordItemsObtained(int count) {
        if (count > 0) this.itemsObtained += count;
    }

    public void recordEquipmentEquipped() {
        this.equipmentEquipped++;
    }

    public void recordHeroRarity(int stars) {
        if (stars > this.highestHeroRarity) {
            this.highestHeroRarity = stars;
        }
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public int getCratesOpened()       { return cratesOpened; }
    public void setCratesOpened(int v) { this.cratesOpened = v; }

    public int getWoodenCrates()       { return woodenCrates; }
    public void setWoodenCrates(int v) { this.woodenCrates = v; }

    public int getSilverCrates()       { return silverCrates; }
    public void setSilverCrates(int v) { this.silverCrates = v; }

    public int getGoldCrates()         { return goldCrates; }
    public void setGoldCrates(int v)   { this.goldCrates = v; }

    public int getDiamondCrates()       { return diamondCrates; }
    public void setDiamondCrates(int v) { this.diamondCrates = v; }

    public int getSummonsPerformed()       { return summonsPerformed; }
    public void setSummonsPerformed(int v) { this.summonsPerformed = v; }

    public int getHighestHeroRarity()       { return highestHeroRarity; }
    public void setHighestHeroRarity(int v) { this.highestHeroRarity = v; }

    public int getItemsObtained()       { return itemsObtained; }
    public void setItemsObtained(int v) { this.itemsObtained = v; }

    public int getEquipmentEquipped()       { return equipmentEquipped; }
    public void setEquipmentEquipped(int v) { this.equipmentEquipped = v; }

    public int getCoinsEarned()       { return coinsEarned; }
    public void setCoinsEarned(int v) { this.coinsEarned = v; }

    public int getCoinsSpent()       { return coinsSpent; }
    public void setCoinsSpent(int v) { this.coinsSpent = v; }

    public int getShardsEarned()       { return shardsEarned; }
    public void setShardsEarned(int v) { this.shardsEarned = v; }

    public int getShardsSpent()       { return shardsSpent; }
    public void setShardsSpent(int v) { this.shardsSpent = v; }
}

