package managers;

import accounts.PlayerAcc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * AchievementManager – manages achievement definitions, unlock evaluations,
 * reward disbursements, and CSV persistence for player_achievements.csv.
 *
 * OOP Concepts: ENCAPSULATION, DELEGATION
 * DSA Concepts: TreeMap (BST) for O(log n) player lookups, LinkedList for
 * collections.
 */
public class AchievementManager {

    public static final String ACHIEVEMENTS_CSV_PATH = "data/player_achievements.csv";

    // Master list of all registered achievement definitions
    private final LinkedList<Achievement> masterTemplates = new LinkedList<>();

    // DSA: TreeMap (Red-Black BST) indexed by case-insensitive username
    private final TreeMap<String, LinkedList<Achievement>> playerAchievements = new TreeMap<>(
            String.CASE_INSENSITIVE_ORDER);

    public AchievementManager() {
        initMasterTemplates();
        loadAchievementsFromCsv(ACHIEVEMENTS_CSV_PATH);
    }

    private void initMasterTemplates() {
        // =====================================================================
        // TIER 1: VERY EASY (Tutorial & First Actions)
        // =====================================================================
        masterTemplates.add(new Achievement("CRATE_001", "First Steps", "Open a crate.",
                Achievement.Category.CRATES, 1, 200, 1));
        masterTemplates.add(new Achievement("SUMMON_001", "A New Beginning", "Unlock a character.",
                Achievement.Category.HEROES, 1, 500, 0));
        masterTemplates.add(new Achievement("GEAR_UP", "Gear Up", "Change a character's equipped weapon.",
                Achievement.Category.EQUIPMENT, 1, 300, 1));
        masterTemplates.add(new Achievement("POTION_FIRST", "A New Taste", "Consume a potion.",
                Achievement.Category.CONSUMABLES, 1, 250, 1));

        // =====================================================================
        // TIER 2: EASY (Early Milestones)
        // =====================================================================
        masterTemplates.add(new Achievement("LUCKY_CHARM", "Lucky Charm", "Get a legendary tier character.",
                Achievement.Category.HEROES, 1, 2500, 5));
        masterTemplates.add(new Achievement("JACKPOT", "Jackpot", "Get a legendary tier weapon.",
                Achievement.Category.ITEMS, 1, 2500, 5));
        masterTemplates.add(new Achievement("COINS_250K", "Golden Shower", "Amass 250,000 coins.",
                Achievement.Category.ECONOMY, 250000, 5000, 10));
        masterTemplates.add(new Achievement("VARIETY_DRINKER", "Variety Drinker", "Consume all available potions.",
                Achievement.Category.CONSUMABLES, 24, 5000, 10));

        // =====================================================================
        // TIER 3: MEDIUM (Mid-Game Progression & Stat Targets)
        // =====================================================================
        masterTemplates.add(new Achievement("COINS_500K", "Half-way Millionaire", "Amass 500,000 coins.",
                Achievement.Category.ECONOMY, 500000, 10000, 15));
        masterTemplates.add(new Achievement("PARTY_ASSEMBLE", "Party Assemble!!", "Get a character for each class.",
                Achievement.Category.HEROES, 6, 6000, 12));
        masterTemplates.add(new Achievement("THE_AVATAR", "The Avatar", "Have a character for each element.",
                Achievement.Category.HEROES, 6, 6000, 12));
        masterTemplates.add(new Achievement("STAT_ATK_100", "Unstoppable Force", "Reach 100 damage points on a hero.",
                Achievement.Category.STATS, 100, 3000, 5));
        masterTemplates.add(new Achievement("STAT_DEF_100", "Immovable Object", "Reach 100 defense points on a hero.",
                Achievement.Category.STATS, 100, 3000, 5));
        masterTemplates.add(new Achievement("STAT_REG_100", "Extreme Healing Factor", "Reach 100 regeneration points.",
                Achievement.Category.STATS, 100, 4000, 8));

        // =====================================================================
        // TIER 4: HARD (Late-Game Stat Caps & High Investments)
        // =====================================================================
        masterTemplates.add(new Achievement("STAT_HP_1000", "Cushion Body", "Reach 1,000 health points on a hero.",
                Achievement.Category.STATS, 1000, 5000, 10));
        masterTemplates.add(new Achievement("STAT_SPD_MAX", "Crazy Rapid Boots", "Reach max movement speed.",
                Achievement.Category.STATS, 50, 5000, 10));
        masterTemplates.add(new Achievement("COINS_1M", "Filthy Rich", "Amass a million coins.",
                Achievement.Category.ECONOMY, 1000000, 25000, 30));
        masterTemplates.add(new Achievement("FULL_SET", "Last Piece", "Unlock a full set bonus.",
                Achievement.Category.EQUIPMENT, 1, 8000, 15));

        // =====================================================================
        // TIER 5: VERY HARD / RNG EXTREME (Endgame & Pure Luck)
        // =====================================================================
        masterTemplates.add(new Achievement("CHOSEN_ONE", "The Chosen One", "Get an iridescent tier weapon.",
                Achievement.Category.ITEMS, 1, 15000, 25));
        masterTemplates.add(
                new Achievement("RED_RED_RED", "RED RED RED", "Get an iridescent tier character for the first time.",
                        Achievement.Category.HEROES, 1, 20000, 30));
        masterTemplates.add(new Achievement("GENIUS_IN_DISGUISE", "A Genius In Disguise",
                "Get an iridescent tier character without hitting pity.",
                Achievement.Category.HEROES, 1, 30000, 50));

        // Additional supporting achievements
        masterTemplates.add(new Achievement("CRATE_025", "Crate Enthusiast", "Open a total of 25 crates.",
                Achievement.Category.CRATES, 25, 1000, 3));
        masterTemplates.add(new Achievement("CRATE_050", "Crate Hoarder", "Open a total of 50 crates.",
                Achievement.Category.CRATES, 50, 2500, 5));
        masterTemplates.add(new Achievement("CRATE_100", "Centurion Opener", "Open a total of 100 crates.",
                Achievement.Category.CRATES, 100, 5000, 10));
        masterTemplates
                .add(new Achievement("ROSTER_005", "Squad Formation", "Recruit at least 5 heroes into your roster.",
                        Achievement.Category.HEROES, 5, 1000, 2));
        masterTemplates
                .add(new Achievement("ROSTER_010", "Grand Legion", "Recruit at least 10 heroes into your roster.",
                        Achievement.Category.HEROES, 10, 3000, 5));
        masterTemplates
                .add(new Achievement("COLLECT_ALL_HEROES", "Collect all Heroes", "Collect all 35 heroes across all classes.",
                        Achievement.Category.HEROES, 35, 50000, 50));
        masterTemplates.add(new Achievement("ITEMS_010", "Armory Recruit", "Obtain at least 10 total items.",
                Achievement.Category.ITEMS, 10, 1000, 0));
        masterTemplates.add(new Achievement("ITEMS_030", "Master Armorer", "Obtain at least 30 total items.",
                Achievement.Category.ITEMS, 30, 3000, 3));
        masterTemplates.add(new Achievement("FULL_GEAR", "Fully Geared Champion", "Equip a hero with all 5 gear slots.",
                Achievement.Category.EQUIPMENT, 1, 2000, 3));
    }

    public LinkedList<Achievement> getMasterTemplates() {
        return masterTemplates;
    }

    public synchronized LinkedList<Achievement> getAchievementsForPlayer(String username) {
        if (username == null)
            return new LinkedList<>();
        LinkedList<Achievement> list = playerAchievements.get(username);
        if (list == null) {
            list = new LinkedList<>();
            for (Achievement tmpl : masterTemplates) {
                list.add(new Achievement(tmpl));
            }
            playerAchievements.put(username, list);
        }
        return list;
    }

    public LinkedList<Achievement> getAchievementsForPlayer(PlayerAcc player) {
        if (player == null)
            return new LinkedList<>();
        return getAchievementsForPlayer(player.getUsername());
    }

    /**
     * Checks all achievements for the given player and unlocks any that satisfy
     * their conditions.
     * Returns a list of newly unlocked achievements.
     */
    public synchronized LinkedList<Achievement> checkAndUnlockAchievements(PlayerAcc player) {
        LinkedList<Achievement> newlyUnlocked = new LinkedList<>();
        if (player == null)
            return newlyUnlocked;

        LinkedList<Achievement> list = getAchievementsForPlayer(player.getUsername());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowStr = sdf.format(new Date());

        for (Achievement ach : list) {
            if (!ach.isUnlocked() && ach.isConditionMet(player)) {
                ach.setUnlocked(true);
                ach.setUnlockedAt(nowStr);
                newlyUnlocked.add(ach);
            }
        }

        if (!newlyUnlocked.isEmpty()) {
            saveAchievementsToCsv(ACHIEVEMENTS_CSV_PATH);
        }

        return newlyUnlocked;
    }

    /**
     * Claims the rewards of a specific unlocked achievement for a player.
     *
     * @return true if rewards were successfully claimed.
     */
    public synchronized boolean claimReward(PlayerAcc player, String achievementId) {
        if (player == null || achievementId == null)
            return false;
        LinkedList<Achievement> list = getAchievementsForPlayer(player.getUsername());
        for (Achievement ach : list) {
            if (ach.getId().equalsIgnoreCase(achievementId)) {
                if (ach.isUnlocked() && !ach.isRewardClaimed()) {
                    if (ach.getRewardCoins() > 0) {
                        player.setCoins(player.getCoins() + ach.getRewardCoins());
                        if (player.getStats() != null) {
                            player.getStats().recordCoinsEarned(ach.getRewardCoins());
                        }
                    }
                    if (ach.getRewardShards() > 0) {
                        player.setShards(player.getShards() + ach.getRewardShards());
                        if (player.getStats() != null) {
                            player.getStats().recordShardsEarned(ach.getRewardShards());
                        }
                    }
                    ach.setRewardClaimed(true);
                    saveAchievementsToCsv(ACHIEVEMENTS_CSV_PATH);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Claims all pending unlocked achievement rewards for the given player.
     *
     * @return count of achievements claimed.
     */
    public synchronized int claimAllRewards(PlayerAcc player) {
        if (player == null)
            return 0;
        int count = 0;
        LinkedList<Achievement> list = getAchievementsForPlayer(player.getUsername());
        for (Achievement ach : list) {
            if (ach.isUnlocked() && !ach.isRewardClaimed()) {
                if (ach.getRewardCoins() > 0) {
                    player.setCoins(player.getCoins() + ach.getRewardCoins());
                    if (player.getStats() != null) {
                        player.getStats().recordCoinsEarned(ach.getRewardCoins());
                    }
                }
                if (ach.getRewardShards() > 0) {
                    player.setShards(player.getShards() + ach.getRewardShards());
                    if (player.getStats() != null) {
                        player.getStats().recordShardsEarned(ach.getRewardShards());
                    }
                }
                ach.setRewardClaimed(true);
                count++;
            }
        }
        if (count > 0) {
            saveAchievementsToCsv(ACHIEVEMENTS_CSV_PATH);
        }
        return count;
    }

    public int getUnlockedCount(PlayerAcc player) {
        if (player == null)
            return 0;
        int count = 0;
        for (Achievement ach : getAchievementsForPlayer(player.getUsername())) {
            if (ach.isUnlocked())
                count++;
        }
        return count;
    }

    public int getClaimedCount(PlayerAcc player) {
        if (player == null)
            return 0;
        int count = 0;
        for (Achievement ach : getAchievementsForPlayer(player.getUsername())) {
            if (ach.isRewardClaimed())
                count++;
        }
        return count;
    }

    public int getUnclaimedCount(PlayerAcc player) {
        if (player == null)
            return 0;
        int count = 0;
        for (Achievement ach : getAchievementsForPlayer(player.getUsername())) {
            if (ach.isUnlocked() && !ach.isRewardClaimed())
                count++;
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // Persistence: player_achievements.csv
    // -------------------------------------------------------------------------

    public synchronized void removePlayer(String username) {
        if (username != null) {
            playerAchievements.remove(username);
        }
    }

    public synchronized void loadAchievementsFromCsv(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (isFirstLine && line.toLowerCase().startsWith("username,achievement_id")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                // Format: username,achievement_id,unlocked,unlocked_at,reward_claimed
                String[] parts = line.split(",", -1);
                if (parts.length >= 5) {
                    String username = parts[0].trim();
                    String achId = parts[1].trim();
                    boolean unlocked = Boolean.parseBoolean(parts[2].trim());
                    String unlockedAt = parts[3].trim();
                    boolean claimed = Boolean.parseBoolean(parts[4].trim());

                    LinkedList<Achievement> list = getAchievementsForPlayer(username);
                    for (Achievement ach : list) {
                        if (ach.getId().equalsIgnoreCase(achId)) {
                            ach.setUnlocked(unlocked);
                            ach.setUnlockedAt(unlockedAt);
                            ach.setRewardClaimed(claimed);
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading player achievements CSV: " + e.getMessage());
        }
    }

    public synchronized void saveAchievementsToCsv(String filePath) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("username,achievement_id,unlocked,unlocked_at,reward_claimed");
            writer.newLine();

            for (Map.Entry<String, LinkedList<Achievement>> entry : playerAchievements.entrySet()) {
                String username = entry.getKey();
                for (Achievement ach : entry.getValue()) {
                    writer.write(username + "," +
                            ach.getId() + "," +
                            ach.isUnlocked() + "," +
                            ach.getUnlockedAt() + "," +
                            ach.isRewardClaimed());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving player achievements to CSV: " + e.getMessage());
        }
    }
}
