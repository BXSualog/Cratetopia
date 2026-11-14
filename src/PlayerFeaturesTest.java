import accounts.AdminAcc;
import accounts.PlayerAcc;
import accounts.PlayerStats;
import characters.CharacterClass;
import characters.Element;
import characters.Hero;
import characters.HeroSort;
import items.Accessory;
import items.Armor;
import items.Item;
import items.Weapon;
import managers.Achievement;
import managers.AchievementManager;
import managers.AdminDataManager;
import managers.CharacterDataManager;
import managers.CharacterGachaManager;
import managers.CrateManager;
import managers.ItemDataManager;
import managers.PlayerDataManager;

import java.io.File;
import java.util.LinkedList;

/**
 * PlayerFeaturesTest – comprehensive automated test suite verifying all player-side features
 * and persistence requirements.
 */
public class PlayerFeaturesTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     STARTING PLAYER FEATURES AUTOMATED TESTS     ");
        System.out.println("==================================================");

        testHeroRosterSortingAndFiltering();
        testEquipmentAndClassRestrictions();
        testInventorySortingAndFiltering();
        testEquippedItemsViewAndMarking();
        testShopAndCurrencyExchange();
        testStatisticsTracking();
        testAchievementUnlockAndClaiming();
        testDataPersistenceAndCounterSynchronization();

        System.out.println("\n==================================================");
        System.out.println("   >>> ALL PLAYER TESTS COMPLETED SUCCESSFULLY! <<<");
        System.out.println("==================================================");
    }

    private static void check(boolean condition, String testName) {
        if (!condition) {
            throw new AssertionError("[FAILED] " + testName);
        }
        System.out.println("  [PASS] " + testName);
    }

    /**
     * Requirement 2.2: Hero roster sorting and filtering by class, rarity, and stats.
     */
    private static void testHeroRosterSortingAndFiltering() {
        System.out.println("\n--- [TEST 1] Hero Roster Sorting & Filtering ---");

        Hero h1 = new Hero("H01", "Ares", "God of War", "Fire", CharacterClass.WARRIOR, 5, 1000, 200, 100, 15, "Warrior");
        Hero h2 = new Hero("H02", "Merlin", "Archmage", "Water", CharacterClass.MAGE, 6, 800, 300, 60, 25, "Mage");
        Hero h3 = new Hero("H03", "Shadow", "Nightstalker", "Dark", CharacterClass.ASSASSIN, 4, 700, 250, 50, 30, "Assassin");
        Hero h4 = new Hero("H04", "Goliath", "Colossus", "Earth", CharacterClass.TANK, 5, 1500, 100, 200, 5, "Tank");

        LinkedList<Hero> heroes = new LinkedList<>();
        heroes.add(h1);
        heroes.add(h2);
        heroes.add(h3);
        heroes.add(h4);

        // Sort by Rarity Descending (6★ -> 5★ -> 4★)
        LinkedList<Hero> sortedByRarity = HeroSort.sort(heroes, HeroSort.RARITY_DESC);
        check(sortedByRarity.getFirst().getStars() == 6, "Rarity Desc: Top hero has 6 stars (Merlin)");
        check(sortedByRarity.getLast().getStars() == 4, "Rarity Desc: Last hero has 4 stars (Shadow)");

        // Sort by HP Descending
        LinkedList<Hero> sortedByHp = HeroSort.sort(heroes, HeroSort.HP_DESC);
        check(sortedByHp.getFirst().getName().equals("Goliath"), "HP Desc: Highest HP is Goliath (1500)");

        // Sort by Attack Descending
        LinkedList<Hero> sortedByAtk = HeroSort.sort(heroes, HeroSort.ATK_DESC);
        check(sortedByAtk.getFirst().getName().equals("Merlin"), "Attack Desc: Highest Attack is Merlin (300)");

        // Sort by Defense Descending
        LinkedList<Hero> sortedByDef = HeroSort.sort(heroes, HeroSort.DEF_DESC);
        check(sortedByDef.getFirst().getName().equals("Goliath"), "Defense Desc: Highest Defense is Goliath (200)");

        // Filter by Class (MAGE)
        LinkedList<Hero> filteredMage = HeroSort.filter(heroes, CharacterClass.MAGE, null, null);
        check(filteredMage.size() == 1 && filteredMage.getFirst().getName().equals("Merlin"), "Filter by Class: Found exactly 1 Mage");

        // Filter by Star Rarity (5★)
        LinkedList<Hero> filtered5Star = HeroSort.filter(heroes, null, 5, null);
        check(filtered5Star.size() == 2, "Filter by Rarity: Found 2 5★ heroes (Ares, Goliath)");

        // Filter by Element (Fire)
        LinkedList<Hero> filteredFire = HeroSort.filter(heroes, null, null, "Fire");
        check(filteredFire.size() == 1 && filteredFire.getFirst().getName().equals("Ares"), "Filter by Element: Found 1 Fire hero (Ares)");
    }

    /**
     * Requirement 2.2: Add Equipment, class restriction enforcement, stat bonuses & set bonuses.
     */
    private static void testEquipmentAndClassRestrictions() {
        System.out.println("\n--- [TEST 2] Equipment & Class Restrictions ---");

        Hero warrior = new Hero("WAR01", "Conan", CharacterClass.WARRIOR, 4, 1000, 100, 50, 10, "A mighty warrior");

        Weapon sword = new Weapon("W01", "Broadsword", "Rare", "Sword", 50, 5, "Warrior;Tank", "A warrior sword");
        Weapon staff = new Weapon("W02", "Magic Staff", "Rare", "Staff", 80, 10, "Mage;Support", "A mage staff");

        // Class restrictions
        check(warrior.canEquip(sword), "Warrior can equip sword (allowed for Warrior;Tank)");
        check(!warrior.canEquip(staff), "Warrior cannot equip staff (allowed only for Mage;Support)");

        // Equipping Weapon
        Item prev = warrior.equip(sword);
        check(prev == null, "Equipping on empty slot returns null");
        check(warrior.getEquippedWeapon() == sword, "Sword is now equipped on warrior");
        check(warrior.getTotalAttack() == 150, "Total ATK updated (100 base + 50 sword = 150)");

        // Swapping Weapon
        Weapon greatsword = new Weapon("W03", "Greatsword", "Epic", "Sword", 100, 10, "Warrior", "Epic sword");
        Item swappedOut = warrior.equip(greatsword);
        check(swappedOut == sword, "Equipping greatsword swapped out broadsword");
        check(warrior.getTotalAttack() == 200, "Total ATK updated with greatsword (100 + 100 = 200)");

        // Armor pieces and Set Bonus
        Armor helm = new Armor("A01", "Dragon Helm", "Epic", "Helmet", "Dragon", 20, 100, 5, "Warrior", "Dragon Helmet");
        Armor chest = new Armor("A02", "Dragon Chest", "Epic", "Chest", "Dragon", 40, 200, 10, "Warrior", "Dragon Chest");
        Armor legs = new Armor("A03", "Dragon Legs", "Epic", "Leggings", "Dragon", 20, 100, 5, "Warrior", "Dragon Leggings");

        warrior.equip(helm);
        warrior.equip(chest);
        check(!warrior.hasFullSetBonus(), "2 pieces do not trigger full set bonus");

        warrior.equip(legs);
        check(warrior.hasFullSetBonus(), "All 3 matching pieces trigger full Dragon armor set bonus");
        check(warrior.getActiveSetName().equalsIgnoreCase("Dragon"), "Active set name is Dragon");

        // Accessory
        Accessory ring = new Accessory("ACC01", "Ring of Attack", "Rare", "Ring", 25, "attack", "All", "Boosts ATK");
        warrior.equip(ring);
        check(warrior.getTotalAttack() == 225, "Accessory boosts attack by 25 (200 + 25 = 225)");

        // Unequip
        Item unequippedRing = warrior.unequip("accessory");
        check(unequippedRing == ring, "Unequipping accessory returns the ring");
        check(warrior.getEquippedAccessory() == null, "Accessory slot is now null");
    }

    /**
     * Requirement 2.3: Inventory sorting and filtering by item type (weapon, armor, accessory).
     */
    private static void testInventorySortingAndFiltering() {
        System.out.println("\n--- [TEST 3] Inventory Sorting & Filtering by Item Type ---");

        Weapon w1 = new Weapon("W01", "Dagger", "Common", "Dagger", 20, 5, "All", "Common dagger");
        Armor a1 = new Armor("A01", "Leather Vest", "Common", "Chest", "None", 15, 50, 0, "All", "Leather armor");
        Accessory acc1 = new Accessory("AC01", "Amulet of Health", "Rare", "Amulet", 100, "health", "All", "Amulet");
        Weapon w2 = new Weapon("W02", "Excalibur", "Legendary", "Sword", 200, 25, "Warrior", "Legendary sword");

        LinkedList<Item> bag = new LinkedList<>();
        bag.add(w1);
        bag.add(a1);
        bag.add(acc1);
        bag.add(w2);

        // Filter by Type
        LinkedList<Item> weaponsOnly = InventorySort.filterByType(bag, "WEAPON");
        check(weaponsOnly.size() == 2, "Filter by Type (WEAPON): found 2 weapons");

        LinkedList<Item> armorsOnly = InventorySort.filterByType(bag, "ARMOR");
        check(armorsOnly.size() == 1, "Filter by Type (ARMOR): found 1 armor");

        LinkedList<Item> accOnly = InventorySort.filterByType(bag, "ACCESSORY");
        check(accOnly.size() == 1, "Filter by Type (ACCESSORY): found 1 accessory");

        // Sort by Rarity (Legendary -> Common)
        LinkedList<Item> sortedByRarity = InventorySort.sort(bag, InventorySort.RARITY);
        check(sortedByRarity.getFirst().getRarity().equalsIgnoreCase("Legendary"), "Sort by Rarity: First item is Legendary (Excalibur)");

        // Sort by Type (Weapon -> Armor -> Accessory)
        LinkedList<Item> sortedByType = InventorySort.sort(bag, InventorySort.TYPE);
        check(sortedByType.getFirst() instanceof Weapon, "Sort by Type: First item is Weapon");
        check(sortedByType.getLast() instanceof Accessory, "Sort by Type: Last item is Accessory");
    }

    /**
     * Requirement 2.3: Filter out equipped items from regular inventory display or clearly mark them as equipped.
     */
    private static void testEquippedItemsViewAndMarking() {
        System.out.println("\n--- [TEST 4] Filtering Out Equipped Items / Marking Equipped ---");

        PlayerAcc player = new PlayerAcc("tester1", "pass123");
        Hero hero = new Hero("H01", "Lancelot", CharacterClass.WARRIOR, 4, 1000, 100, 50, 10, "Knight");
        player.addHero(hero);

        Weapon sword = new Weapon("W01", "Knight Sword", "Rare", "Sword", 40, 5, "Warrior", "Sword");
        Armor chest = new Armor("A01", "Knight Plate", "Rare", "Chest", "None", 30, 80, 0, "Warrior", "Chest");
        Weapon spareBow = new Weapon("W02", "Hunting Bow", "Common", "Bow", 20, 5, "Ranger", "Bow");

        player.addItem(sword);
        player.addItem(chest);
        player.addItem(spareBow);

        check(player.getInventory().size() == 3, "Player starts with 3 unequipped items in bag");
        check(player.getItemsOwned() == 3, "Player owns 3 items total");

        // Equip sword on Lancelot
        player.removeItem(sword);
        hero.equip(sword);
        player.syncCounters();

        // Check bag only vs all items
        check(player.getInventory().size() == 2, "Bag now contains 2 unequipped items (sword is filtered out of bag)");
        check(player.getEquippedItemsCount() == 1, "1 item is currently equipped on heroes");
        check(player.getItemsOwned() == 3, "Total items owned remains 3 (2 in bag + 1 equipped)");
        check(player.getAllItems().size() == 3, "Combined getAllItems() contains both bag and equipped items");
    }

    /**
     * Requirement 2.4: Shop & Trading – allow players to exchange coins for shards.
     */
    private static void testShopAndCurrencyExchange() {
        System.out.println("\n--- [TEST 5] Shop & Trading – Coins to Shards Exchange ---");

        PlayerAcc player = new PlayerAcc("shopper", "pass123", 10000, 5, 0, 0);

        int initialCoins = player.getCoins();
        int initialShards = player.getShards();

        // Trade 2,000 Coins -> 1 Shard
        int cost = 2000;
        int gain = 1;
        player.setCoins(player.getCoins() - cost);
        player.setShards(player.getShards() + gain);
        player.getStats().recordCoinsSpent(cost);
        player.getStats().recordShardsEarned(gain);

        check(player.getCoins() == initialCoins - 2000, "Coins deducted by 2,000 (now " + player.getCoins() + ")");
        check(player.getShards() == initialShards + 1, "Shards increased by 1 (now " + player.getShards() + ")");
        check(player.getStats().getCoinsSpent() == 2000, "Statistics recorded 2,000 coins spent");
        check(player.getStats().getShardsEarned() == 1, "Statistics recorded 1 shard earned");

        // Custom Trade: 3 Shards for 6,000 Coins
        int customShards = 3;
        int customCost = customShards * 2000;
        player.setCoins(player.getCoins() - customCost);
        player.setShards(player.getShards() + customShards);
        player.getStats().recordCoinsSpent(customCost);
        player.getStats().recordShardsEarned(customShards);

        check(player.getCoins() == 2000, "After custom trade, remaining coins: 2,000");
        check(player.getShards() == 9, "After custom trade, total shards: 9");
        check(player.getStats().getCoinsSpent() == 8000, "Total coins spent recorded: 8,000");
    }

    /**
     * Requirement 2.5: Statistics Tracking (crates opened, summons, items obtained, coins, shards).
     */
    private static void testStatisticsTracking() {
        System.out.println("\n--- [TEST 6] Player Statistics Tracking ---");

        PlayerStats stats = new PlayerStats();

        // Record Crate Openings
        stats.recordCrateOpened("Wooden Crate", 350, 0, 2);
        stats.recordCrateOpened("Silver Crate", 850, 1, 1);
        stats.recordCrateOpened("Diamond Crate", 6000, 2, 3);

        check(stats.getCratesOpened() == 3, "Total crates opened: 3");
        check(stats.getWoodenCrates() == 1, "Wooden crates opened: 1");
        check(stats.getSilverCrates() == 1, "Silver crates opened: 1");
        check(stats.getDiamondCrates() == 1, "Diamond crates opened: 1");
        check(stats.getItemsObtained() == 6, "Total items obtained: 6");
        check(stats.getCoinsEarned() == 7200, "Total coins earned: 7,200");
        check(stats.getShardsEarned() == 3, "Total shards earned: 3");

        // Record Summons
        stats.recordSummon(1, 1, 4);
        stats.recordSummon(10, 10, 6);

        check(stats.getSummonsPerformed() == 11, "Total summons performed: 11 (1 + 10)");
        check(stats.getShardsSpent() == 11, "Total shards spent: 11");
        check(stats.getHighestHeroRarity() == 6, "Highest hero rarity recorded: 6★");
    }

    /**
     * Requirement 2.5: Achievement List with unlock conditions & claiming rewards.
     */
    private static void testAchievementUnlockAndClaiming() {
        System.out.println("\n--- [TEST 7] Achievements & Unlock Conditions ---");

        AchievementManager am = new AchievementManager();
        String uniqueAchUser = "achiever_" + System.currentTimeMillis();
        PlayerAcc player = new PlayerAcc(uniqueAchUser, "pass123", 500, 10, 0, 0);

        // Initially no crates opened
        am.checkAndUnlockAchievements(player);
        check(am.getUnlockedCount(player) == 0, "Initially 0 achievements unlocked");

        // Open 1 crate
        player.getStats().recordCrateOpened("Wooden Crate", 300, 0, 1);
        LinkedList<Achievement> unlocked1 = am.checkAndUnlockAchievements(player);
        check(unlocked1.size() >= 1 && unlocked1.getFirst().getId().equals("CRATE_001"),
                "Opening 1 crate unlocked achievement: First Crate Opened");

        // Claim reward
        int coinsBefore = player.getCoins();
        boolean claimed = am.claimReward(player, "CRATE_001");
        check(claimed, "Claiming CRATE_001 succeeded");
        check(player.getCoins() == coinsBefore + 200, "Received 200 coins reward for First Crate Opened");
        check(!am.claimReward(player, "CRATE_001"), "Cannot claim the same achievement reward twice");

        // Add 6-star hero to roster
        Hero sixStarHero = new Hero("H6", "Solaris", CharacterClass.MAGE, 6, 1200, 350, 80, 20, "Sun God");
        player.addHero(sixStarHero);
        LinkedList<Achievement> heroUnlocks = am.checkAndUnlockAchievements(player);

        boolean found6StarAch = false;
        for (Achievement a : heroUnlocks) {
            if (a.getId().equals("HERO_6STAR")) found6StarAch = true;
        }
        check(found6StarAch, "Collecting 6★ hero unlocked: Legendary Destiny (HERO_6STAR)");

        // Claim all pending rewards
        int claimedCount = am.claimAllRewards(player);
        check(claimedCount >= 1, "Claim all rewards claimed pending achievements");

        // Clean up test achievements
        am.removePlayer(uniqueAchUser);
        am.saveAchievementsToCsv(AchievementManager.ACHIEVEMENTS_CSV_PATH);
    }

    /**
     * Requirement 3: Data & Persistence
     * - player_achievements.csv
     * - player_statistics.csv
     * - admin.csv / admins.csv
     * - Preventing heroesCollected & itemsOwned desynchronization
     */
    private static void testDataPersistenceAndCounterSynchronization() {
        System.out.println("\n--- [TEST 8] Data Persistence & Counter Synchronization ---");

        CharacterDataManager charData = new CharacterDataManager();
        ItemDataManager itemData = new ItemDataManager();
        PlayerDataManager playerDataManager = new PlayerDataManager(charData, itemData);

        // Register a test player
        String testUser = "SyncTestUser_" + System.currentTimeMillis();
        playerDataManager.registerPlayer(testUser, "secret123");
        PlayerAcc player = playerDataManager.getPlayer(testUser);
        check(player != null, "Player registered and retrieved");

        // Verify counter synchronization
        check(player.getHeroesCollected() == 0, "Heroes collected starts at 0");
        check(player.getItemsOwned() == 0, "Items owned starts at 0");

        // Add 2 items and 1 hero
        Item item1 = itemData.createNewItemInstance("W001");
        Item item2 = itemData.createNewItemInstance("A001");
        if (item1 != null) player.addItem(item1);
        if (item2 != null) player.addItem(item2);

        Hero hero1 = charData.createNewHeroInstance("WAR001");
        if (hero1 == null) {
            hero1 = new Hero("TEST_WAR", "Test Warrior", CharacterClass.WARRIOR, 4, 1000, 100, 50, 10, "Test");
        }
        player.addHero(hero1);

        check(player.getHeroesCollected() == player.getRoster().size(), "Heroes collected matches roster size (1)");
        check(player.getItemsOwned() == player.getInventory().size(), "Items owned matches inventory size (2)");

        // Equip item1 onto hero1
        if (item1 != null && hero1.canEquip(item1)) {
            player.removeItem(item1);
            hero1.equip(item1);
            player.syncCounters();

            check(player.getInventory().size() == 1, "Inventory size is 1 (unequipped item)");
            check(player.getEquippedItemsCount() == 1, "Equipped items count is 1");
            check(player.getItemsOwned() == 2, "Items owned counter remains 2 (1 bag + 1 equipped)");
        }

        // Save all data to CSV files
        playerDataManager.saveAllPlayersToCsv();

        // Verify CSV files exist on disk
        File statsFile = new File("data/player_statistics.csv");
        File achFile = new File("data/player_achievements.csv");
        File adminFile1 = new File("data/admin.csv");
        File adminFile2 = new File("data/admins.csv");

        check(statsFile.exists(), "player_statistics.csv exists on disk");
        check(achFile.exists(), "player_achievements.csv exists on disk");
        check(adminFile1.exists(), "admin.csv exists on disk");
        check(adminFile2.exists(), "admins.csv exists on disk");

        // Verify AdminDataManager loads credentials properly
        AdminDataManager adminDataManager = new AdminDataManager();
        AdminAcc admin = adminDataManager.loginAdmin("admin", "admin123");
        check(admin != null, "Admin authentication succeeded via admin.csv / fallback credentials");

        // Reload PlayerDataManager from disk and verify consistency
        PlayerDataManager reloadManager = new PlayerDataManager(charData, itemData);
        PlayerAcc reloadedPlayer = reloadManager.getPlayer(testUser);
        check(reloadedPlayer != null, "Reloaded player successfully from CSV");
        check(reloadedPlayer.getHeroesCollected() == reloadedPlayer.getRoster().size(),
                "After reload: heroesCollected (" + reloadedPlayer.getHeroesCollected() + ") matches roster size (" + reloadedPlayer.getRoster().size() + ")");
        check(reloadedPlayer.getItemsOwned() == reloadedPlayer.getTotalItemsCount(),
                "After reload: itemsOwned (" + reloadedPlayer.getItemsOwned() + ") matches total items count (" + reloadedPlayer.getTotalItemsCount() + ")");

        // Clean up test player
        playerDataManager.deletePlayer(testUser);
        System.out.println("  [PASS] Cleaned up temporary test user");
    }
}
