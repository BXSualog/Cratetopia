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
import java.util.TreeMap;

/**
 * AchievementManager – manages achievement definitions, unlock evaluations,
 * reward disbursements, and CSV persistence for player_achievements.csv.
 *
 * OOP Concepts: ENCAPSULATION, DELEGATION
 * DSA Concepts: TreeMap (BST) for O(log n) player lookups, LinkedList for collections.
 */
public class AchievementManager {

    public static final String ACHIEVEMENTS_CSV_PATH = "data/player_achievements.csv";

    // Master list of all registered achievement definitions
    private final LinkedList<Achievement> masterTemplates = new LinkedList<>();

    // DSA: TreeMap (Red-Black BST) indexed by case-insensitive username
    private final TreeMap<String, LinkedList<Achievement>> playerAchievements = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public AchievementManager() {
        initMasterTemplates();
        loadAchievementsFromCsv(ACHIEVEMENTS_CSV_PATH);
    }

    private void initMasterTemplates() {
        // --- Crates ---
        masterTemplates.add(new Achievement("CRATE_001", "First Crate Opened", "Open your very first crate.",
                Achievement.Category.CRATES, 1, 200, 1));
        masterTemplates.add(new Achievement("CRATE_025", "Crate Enthusiast", "Open a total of 25 crates.",
                Achievement.Category.CRATES, 25, 1000, 3));
        masterTemplates.add(new Achievement("CRATE_050", "Crate Hoarder", "Open a total of 50 crates.",
                Achievement.Category.CRATES, 50, 2500, 5));
        masterTemplates.add(new Achievement("CRATE_100", "Centurion Opener", "Open a total of 100 crates.",
                Achievement.Category.CRATES, 100, 5000, 10));

        // --- Summons ---
        masterTemplates.add(new Achievement("SUMMON_001", "First Summon", "Perform your first hero summon at the Portal of Legends.",
                Achievement.Category.SUMMONS, 1, 500, 0));
        masterTemplates.add(new Achievement("SUMMON_010", "Portal Seeker", "Perform 10 total hero summons.",
                Achievement.Category.SUMMONS, 10, 1500, 2));
        masterTemplates.add(new Achievement("SUMMON_050", "Master Summoner", "Perform 50 total hero summons.",
                Achievement.Category.SUMMONS, 50, 5000, 10));

        // --- Heroes ---
        masterTemplates.add(new Achievement("HERO_5STAR", "Heroic Ascendance", "Summon or own a 5★ hero in your roster.",
                Achievement.Category.HEROES, 5, 2500, 5));
        masterTemplates.add(new Achievement("HERO_6STAR", "Legendary Destiny", "Summon or own a 6★ hero in your roster.",
                Achievement.Category.HEROES, 6, 10000, 15));
        masterTemplates.add(new Achievement("ROSTER_005", "Squad Formation", "Recruit at least 5 heroes into your roster.",
                Achievement.Category.HEROES, 5, 1000, 2));
        masterTemplates.add(new Achievement("ROSTER_010", "Grand Legion", "Recruit at least 10 heroes into your roster.",
                Achievement.Category.HEROES, 10, 3000, 5));

        // --- Items ---
        masterTemplates.add(new Achievement("ITEMS_010", "Armory Recruit", "Obtain at least 10 total items.",
                Achievement.Category.ITEMS, 10, 1000, 0));
        masterTemplates.add(new Achievement("ITEMS_030", "Master Armorer", "Obtain at least 30 total items.",
                Achievement.Category.ITEMS, 30, 3000, 3));

        // --- Economy ---
        masterTemplates.add(new Achievement("COINS_10K", "Coin Hoarder", "Earn 10,000 total coins in your journey.",
                Achievement.Category.ECONOMY, 10000, 2000, 2));
        masterTemplates.add(new Achievement("COINS_50K", "Midas Fortune", "Earn 50,000 total coins in your journey.",
                Achievement.Category.ECONOMY, 50000, 10000, 10));

        // --- Equipment ---
        masterTemplates.add(new Achievement("FULL_SET", "Armor Harmonizer", "Equip any hero with a complete 3-piece armor set bonus.",
                Achievement.Category.EQUIPMENT, 1, 3000, 5));
        masterTemplates.add(new Achievement("FULL_GEAR", "Fully Geared Champion", "Equip a hero with all 5 gear slots (Weapon, Helmet, Chest, Leggings, Accessory).",
                Achievement.Category.EQUIPMENT, 1, 2000, 3));
    }

    public LinkedList<Achievement> getMasterTemplates() {
        return masterTemplates;
    }

    public synchronized LinkedList<Achievement> getAchievementsForPlayer(String username) {
        if (username == null) return new LinkedList<>();
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
        if (player == null) return new LinkedList<>();
        return getAchievementsForPlayer(player.getUsername());
    }

    /**
     * Checks all achievements for the given player and unlocks any that satisfy their conditions.
     * Returns a list of newly unlocked achievements.
     */
    public synchronized LinkedList<Achievement> checkAndUnlockAchievements(PlayerAcc player) {
        LinkedList<Achievement> newlyUnlocked = new LinkedList<>();
        if (player == null) return newlyUnlocked;

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
        if (player == null || achievementId == null) return false;
        LinkedList<Achievement> list = getAchievementsForPlayer(player.getUsername());
        for (Achievement ach : list) {
            if (ach.getId().equalsIgnoreCase(achievementId)) {
                if (ach.isUnlocked() && !ach.isRewardClaimed()) {
                    player.setCoins(player.getCoins() + ach.getRewardCoins());
                    player.setShards(player.getShards() + ach.getRewardShards());
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
        if (player == null) return 0;
        int count = 0;
        LinkedList<Achievement> list = getAchievementsForPlayer(player.getUsername());
        for (Achievement ach : list) {
            if (ach.isUnlocked() && !ach.isRewardClaimed()) {
                player.setCoins(player.getCoins() + ach.getRewardCoins());
                player.setShards(player.getShards() + ach.getRewardShards());
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
        if (player == null) return 0;
        int count = 0;
        for (Achievement ach : getAchievementsForPlayer(player.getUsername())) {
            if (ach.isUnlocked()) count++;
        }
        return count;
    }

    public int getClaimedCount(PlayerAcc player) {
        if (player == null) return 0;
        int count = 0;
        for (Achievement ach : getAchievementsForPlayer(player.getUsername())) {
            if (ach.isRewardClaimed()) count++;
        }
        return count;
    }

    public int getUnclaimedCount(PlayerAcc player) {
        if (player == null) return 0;
        int count = 0;
        for (Achievement ach : getAchievementsForPlayer(player.getUsername())) {
            if (ach.isUnlocked() && !ach.isRewardClaimed()) count++;
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

            for (java.util.Map.Entry<String, LinkedList<Achievement>> entry : playerAchievements.entrySet()) {
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
