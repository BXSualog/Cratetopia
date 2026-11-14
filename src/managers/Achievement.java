package managers;

import accounts.PlayerAcc;
import characters.Hero;

/**
 * Achievement – represents a milestone reward with unlock condition and claimable rewards.
 *
 * OOP Concepts demonstrated here
 * --------------------------------
 * ENCAPSULATION: All attributes private with getters and mutators for unlock / claim state.
 */
public class Achievement {

    public enum Category {
        CRATES("Crate Mastery"),
        SUMMONS("Hero Summoning"),
        HEROES("Hero Collection"),
        ITEMS("Gear & Armory"),
        ECONOMY("Wealth & Trading"),
        EQUIPMENT("Equipment Mastery");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String id;
    private final String title;
    private final String description;
    private final Category category;
    private final int targetCount;
    private final int rewardCoins;
    private final int rewardShards;

    // Per-player state (cloned or loaded for specific player)
    private boolean unlocked;
    private String unlockedAt = "";
    private boolean rewardClaimed;

    public Achievement(String id, String title, String description, Category category,
                       int targetCount, int rewardCoins, int rewardShards) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.targetCount = targetCount;
        this.rewardCoins = rewardCoins;
        this.rewardShards = rewardShards;
        this.unlocked = false;
        this.unlockedAt = "";
        this.rewardClaimed = false;
    }

    /**
     * Copy constructor for per-player tracking.
     */
    public Achievement(Achievement template) {
        this.id = template.id;
        this.title = template.title;
        this.description = template.description;
        this.category = template.category;
        this.targetCount = template.targetCount;
        this.rewardCoins = template.rewardCoins;
        this.rewardShards = template.rewardShards;
        this.unlocked = false;
        this.unlockedAt = "";
        this.rewardClaimed = false;
    }

    // -------------------------------------------------------------------------
    // Progress Calculation
    // -------------------------------------------------------------------------

    public int getProgress(PlayerAcc player) {
        if (player == null) return 0;
        switch (id) {
            case "CRATE_001":
            case "CRATE_025":
            case "CRATE_050":
            case "CRATE_100":
                return player.getStats().getCratesOpened();

            case "SUMMON_001":
            case "SUMMON_010":
            case "SUMMON_050":
                return player.getStats().getSummonsPerformed();

            case "HERO_5STAR": {
                int highest = player.getStats().getHighestHeroRarity();
                for (Hero h : player.getRoster()) {
                    if (h.getStars() > highest) highest = h.getStars();
                }
                return Math.min(targetCount, highest >= 5 ? 5 : highest);
            }

            case "HERO_6STAR": {
                int highest = player.getStats().getHighestHeroRarity();
                for (Hero h : player.getRoster()) {
                    if (h.getStars() > highest) highest = h.getStars();
                }
                return Math.min(targetCount, highest >= 6 ? 6 : highest);
            }

            case "ROSTER_005":
            case "ROSTER_010":
                return player.getRoster().size();

            case "ITEMS_010":
            case "ITEMS_030":
                return Math.max(player.getStats().getItemsObtained(), player.getItemsOwned());

            case "COINS_10K":
            case "COINS_50K":
                return player.getStats().getCoinsEarned();

            case "FULL_SET": {
                for (Hero h : player.getRoster()) {
                    if (h.hasFullSetBonus()) return 1;
                }
                return 0;
            }

            case "FULL_GEAR": {
                for (Hero h : player.getRoster()) {
                    if (h.getEquippedWeapon() != null &&
                        h.getEquippedArmor(Hero.SLOT_HELMET) != null &&
                        h.getEquippedArmor(Hero.SLOT_CHEST) != null &&
                        h.getEquippedArmor(Hero.SLOT_LEGGINGS) != null &&
                        h.getEquippedAccessory() != null) {
                        return 1;
                    }
                }
                return 0;
            }

            default:
                return unlocked ? targetCount : 0;
        }
    }

    public boolean isConditionMet(PlayerAcc player) {
        return getProgress(player) >= targetCount;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public String getId()            { return id; }
    public String getTitle()         { return title; }
    public String getDescription()   { return description; }
    public Category getCategory()    { return category; }
    public int getTargetCount()      { return targetCount; }
    public int getRewardCoins()      { return rewardCoins; }
    public int getRewardShards()     { return rewardShards; }

    public boolean isUnlocked()                 { return unlocked; }
    public void setUnlocked(boolean unlocked)   { this.unlocked = unlocked; }

    public String getUnlockedAt()               { return unlockedAt; }
    public void setUnlockedAt(String unlockedAt){ this.unlockedAt = unlockedAt; }

    public boolean isRewardClaimed()            { return rewardClaimed; }
    public void setRewardClaimed(boolean claimed){ this.rewardClaimed = claimed; }

    public String getRewardsDisplay() {
        StringBuilder sb = new StringBuilder();
        if (rewardCoins > 0) sb.append(String.format("%,d Coins", rewardCoins));
        if (rewardShards > 0) {
            if (sb.length() > 0) sb.append(" + ");
            sb.append(rewardShards).append(" Shard").append(rewardShards > 1 ? "s" : "");
        }
        return sb.toString();
    }
}

