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
        EQUIPMENT("Equipment Mastery"),
        CONSUMABLES("Potions & Alchemy"),
        STATS("Combat Prowess");

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
            case "COLLECT_ALL_HEROES":
                return Math.min(targetCount, player.getRoster().size());

            case "ITEMS_010":
            case "ITEMS_030":
                return Math.max(player.getStats().getItemsObtained(), player.getItemsOwned());

            case "COINS_10K":
            case "COINS_50K":
            case "COINS_250K":
            case "COINS_500K":
            case "COINS_1M":
                return Math.min(targetCount, Math.max(player.getCoins(), player.getStats().getCoinsEarned()));

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

            // --- Tier 1: Very Easy ---
            case "GEAR_UP":
                return Math.min(targetCount, player.getStats().getWeaponsSwapped());

            case "POTION_FIRST":
                return Math.min(targetCount, player.getStats().getPotionsConsumed());

            // --- Tier 2: Easy ---
            case "LUCKY_CHARM": {
                boolean hasLeg = player.getStats().isGotLegendaryCharacter();
                if (!hasLeg) {
                    for (Hero h : player.getRoster()) {
                        if (h.getStars() >= 5) { hasLeg = true; break; }
                    }
                }
                return hasLeg ? 1 : 0;
            }

            case "JACKPOT": {
                boolean hasLegWep = player.getStats().isGotLegendaryWeapon();
                if (!hasLegWep) {
                    for (items.Item it : player.getAllItems()) {
                        if (it instanceof items.Weapon && "Legendary".equalsIgnoreCase(it.getRarity())) {
                            hasLegWep = true;
                            break;
                        }
                    }
                }
                return hasLegWep ? 1 : 0;
            }

            case "VARIETY_DRINKER":
                return Math.min(targetCount, player.getStats().getConsumedPotionIds().size());

            // --- Tier 3: Medium ---
            case "PARTY_ASSEMBLE": {
                java.util.HashSet<characters.CharacterClass> classes = new java.util.HashSet<>();
                for (Hero h : player.getRoster()) {
                    if (h.getCharacterClass() != null) {
                        classes.add(h.getCharacterClass());
                    }
                }
                return Math.min(targetCount, classes.size());
            }

            case "THE_AVATAR": {
                java.util.HashSet<characters.Element> elements = new java.util.HashSet<>();
                for (Hero h : player.getRoster()) {
                    characters.Element elem = h.getElement();
                    if (elem != null && elem != characters.Element.NONE) {
                        elements.add(elem);
                    }
                }
                return Math.min(targetCount, elements.size());
            }

            case "STAT_ATK_100": {
                int maxAtk = 0;
                for (Hero h : player.getRoster()) {
                    if (h.getTotalAttack() > maxAtk) maxAtk = h.getTotalAttack();
                }
                return Math.min(targetCount, maxAtk);
            }

            case "STAT_DEF_100": {
                int maxDef = 0;
                for (Hero h : player.getRoster()) {
                    if (h.getTotalDefense() > maxDef) maxDef = h.getTotalDefense();
                }
                return Math.min(targetCount, maxDef);
            }

            case "STAT_REG_100": {
                // Regeneration points milestone (from gear or hero potential)
                int maxReg = 0;
                for (Hero h : player.getRoster()) {
                    int reg = h.getBaseDefense() / 2; // intrinsic recovery base
                    if (h.getEquippedAccessory() != null && "recovery".equalsIgnoreCase(h.getEquippedAccessory().getBonusType())) {
                        reg += h.getEquippedAccessory().getStatBonus();
                    }
                    if (reg > maxReg) maxReg = reg;
                }
                return Math.min(targetCount, maxReg);
            }

            // --- Tier 4: Hard ---
            case "STAT_HP_1000": {
                int maxHp = 0;
                for (Hero h : player.getRoster()) {
                    if (h.getTotalHp() > maxHp) maxHp = h.getTotalHp();
                }
                return Math.min(targetCount, maxHp);
            }

            case "STAT_SPD_MAX": {
                // Max movement speed / attack haste
                int maxSpd = 0;
                for (Hero h : player.getRoster()) {
                    int spd = 20 + h.getTotalCritChance(); // dynamic speed indicator
                    if (h.getEquippedAccessory() != null && "speed".equalsIgnoreCase(h.getEquippedAccessory().getBonusType())) {
                        spd += h.getEquippedAccessory().getStatBonus();
                    }
                    if (spd > maxSpd) maxSpd = spd;
                }
                return Math.min(targetCount, maxSpd);
            }

            // --- Tier 5: Very Hard / RNG Extreme ---
            case "CHOSEN_ONE": {
                boolean hasIriWep = player.getStats().isGotIridescentWeapon();
                if (!hasIriWep) {
                    for (items.Item it : player.getAllItems()) {
                        if (it instanceof items.Weapon && ("Mythic".equalsIgnoreCase(it.getRarity()) || "Iridescent".equalsIgnoreCase(it.getRarity()))) {
                            hasIriWep = true;
                            break;
                        }
                    }
                }
                return hasIriWep ? 1 : 0;
            }

            case "RED_RED_RED": {
                boolean hasIriChar = player.getStats().isGotIridescentCharacter();
                if (!hasIriChar) {
                    for (Hero h : player.getRoster()) {
                        if (h.getStars() >= 6) { hasIriChar = true; break; }
                    }
                }
                return hasIriChar ? 1 : 0;
            }

            case "GENIUS_IN_DISGUISE": {
                return player.getStats().isGotIridescentCharacterNoPity() ? 1 : 0;
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

