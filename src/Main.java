import accounts.AdminAcc;
import accounts.PlayerAcc;
import characters.CharacterClass;
import characters.Hero;
import characters.HeroSort;
import items.Accessory;
import items.Armor;
import items.ArmorSet;
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

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        InputHandling input = new InputHandling();
        ItemDataManager itemDataManager = new ItemDataManager();
        CharacterDataManager charDataManager = new CharacterDataManager();
        PlayerDataManager playerDataManager = new PlayerDataManager(charDataManager, itemDataManager);
        CharacterGachaManager gachaManager = new CharacterGachaManager(charDataManager);
        CrateManager crateManager = new CrateManager(itemDataManager);
        AdminDataManager adminDataManager = new AdminDataManager(playerDataManager, itemDataManager, charDataManager, crateManager, gachaManager);

        // Auto-save on JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            adminDataManager.saveAllSystemData();
        }));

        String choice;

        do {
            System.out.println("\n============================================");
            System.out.println("             Cratetopia");
            System.out.println("============================================");
            System.out.println("1. Register Player Account");
            System.out.println("2. Player Login");
            System.out.println("3. Admin Login");
            System.out.println("0. Exit");
            System.out.println("============================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    registerUser(input, playerDataManager);
                    break;

                case "2":
                    loginUser(input, playerDataManager, crateManager, gachaManager);
                    break;

                case "3":
                    loginAdmin(input, adminDataManager);
                    break;

                case "0":
                    playerDataManager.saveAllPlayersToCsv();
                    adminDataManager.saveAdminsToCsv();
                    System.out.println("\nThank you for playing Cratetopia!");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }

            if (!choice.equals("0") && !choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
                input.readLine("\nPress Enter to continue...");
            }

        } while (!choice.equals("0"));

        input.close();
    }

    public static void registerUser(InputHandling input, PlayerDataManager playerDataManager) {
        System.out.println("===========================================");
        System.out.println("          REGISTER PLAYER ACCOUNT");
        System.out.println("===========================================");

        String username;
        while (true) {
            username = input.readLine("Enter username: ");
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty. Please try again.");
            } else if (username.contains(",")) {
                System.out.println("Username cannot contain commas. Please try again.");
            } else if (playerDataManager.isUsernameTaken(username)) {
                System.out.println("Username is already taken. Please choose another username.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            password = input.readLine("Enter password: ");
            if (password.isEmpty()) {
                System.out.println("Password cannot be empty. Please try again.");
            } else if (password.contains(",")) {
                System.out.println("Password cannot contain commas. Please try again.");
            } else {
                break;
            }
        }

        boolean success = playerDataManager.registerPlayer(username, password);
        if (success) {
            System.out.println("Account registered successfully!\nYou can now log in with your credentials.");
        } else {
            System.out.println("Failed to register account. Please try again.");
        }

        input.readLine("\nPress Enter to return to the Login Menu...");
    }

    public static void loginUser(InputHandling input, PlayerDataManager playerDataManager,
                                 CrateManager crateManager, CharacterGachaManager gachaManager) {
        while (true) {
            System.out.println("===========================================");
            System.out.println("               PLAYER LOGIN");
            System.out.println("===========================================");

            String username = input.readLine("Enter username: ");
            if (username.equals("0")) {
                break;
            }
            String password = input.readLine("Enter password: ");

            boolean success = playerDataManager.loginPlayer(username, password);
            if (success) {
                PlayerAcc player = playerDataManager.getPlayer(username);
                if (player.isBanned()) {
                    System.out.println("\n===========================================");
                    System.out.println("              ACCESS DENIED");
                    System.out.println("===========================================");
                    System.out.println("  Your account has been SUSPENDED / BANNED");
                    System.out.println("  by an administrator.");
                    System.out.println("  Please contact administration if you believe");
                    System.out.println("  this was done in error.");
                    System.out.println("===========================================");
                    input.readLine("\nPress Enter to return to Login Menu...");
                    break;
                }
                System.out.println("Login successful! Welcome, " + player.getUsername() + "!");
                playerHomescreen(input, playerDataManager, crateManager, gachaManager, player);
                break;
            } else {
                System.out.println("Invalid username or password. Please try again.");
                String opt = input.readLine("\nPress Enter to try again (or '0' to return)...");
                if (opt.equals("0")) {
                    break;
                }
            }
        }
    }

    // =========================================================================
    // Admin Login & Dashboard Implementation
    // =========================================================================

    /**
     * Admin login handler executed from case "3" in the start menu.
     * Authenticates administrator credentials using AdminDataManager.
     *
     * @param input             Input handler for reading console inputs.
     * @param adminDataManager  Administrator manager for authentication & operations.
     */
    public static void loginAdmin(InputHandling input, AdminDataManager adminDataManager) {
        while (true) {
            System.out.println("===========================================");
            System.out.println("               ADMIN LOGIN");
            System.out.println("===========================================");

            String username = input.readLine("Enter admin username (or '0' to return): ");
            if (username.equals("0")) {
                break;
            }
            String password = input.readLine("Enter admin password: ");

            AdminAcc admin = adminDataManager.loginAdmin(username, password);
            if (admin != null) {
                System.out.println("\n[SUCCESS] Login successful! Welcome, Administrator "
                        + admin.getUsername() + " [" + admin.getRole() + "]!");
                adminHomescreen(input, adminDataManager, admin);
                break;
            } else {
                System.out.println("\n[!] Invalid admin credentials. Access denied.");
                String opt = input.readLine("\nPress Enter to try again (or '0' to return)...");
                if (opt.equals("0")) {
                    break;
                }
            }
        }
    }

    /**
     * Administrator Dashboard / Home Menu interface.
     * Provides system metrics, player account management, catalogs inspection,
     * admin account registration, and persistent data saves.
     *
     * @param input             Input handler.
     * @param adminDataManager  Administrator data manager.
     * @param admin             The currently authenticated admin account.
     */
    public static void adminHomescreen(InputHandling input, AdminDataManager adminDataManager, AdminAcc admin) {
        String choice;

        do {
            System.out.println("\n==================================================");
            System.out.println("              CRATETOPIA - ADMIN DASHBOARD");
            System.out.println("==================================================");
            System.out.println("  Logged in as : " + admin.getUsername() + " (" + admin.getRole() + ")");
            System.out.println("--------------------------------------------------");
            System.out.println("  Registered Players : " + adminDataManager.getTotalPlayersCount()
                    + " (Active: " + adminDataManager.getActivePlayersCount()
                    + " | Banned: " + adminDataManager.getBannedPlayersCount() + ")");
            System.out.println("  Catalog Heroes     : " + adminDataManager.getTotalHeroesCatalogCount());
            System.out.println("  Catalog Items      : " + adminDataManager.getTotalItemsCatalogCount());
            System.out.println("--------------------------------------------------");
            System.out.println("1. Statistics & Player Activity Reports (1.6)");
            System.out.println("2. Player Account Management (1.3)");
            System.out.println("3. Item & Catalog Management (1.4)");
            System.out.println("4. Game Economy Management (1.5)");
            System.out.println("5. Admin Credential Management");
            System.out.println("6. Save All Game Data");
            System.out.println("0. Logout");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    adminStatisticsReportsMenu(input, adminDataManager);
                    break;

                case "2":
                    adminPlayerManagement(input, adminDataManager);
                    break;

                case "3":
                    adminCatalogsMenu(input, adminDataManager);
                    break;

                case "4":
                    adminEconomyManagementMenu(input, adminDataManager);
                    break;

                case "5":
                    adminAccountsMenu(input, adminDataManager, admin);
                    break;

                case "6":
                    System.out.print("\nSaving all system data");
                    for (int i = 0; i < 5; i++) {
                        System.out.print(".");
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    System.out.println();
                    adminDataManager.saveAllSystemData();
                    System.out.println("[SUCCESS] All system, player, catalog, and administrator records saved!");
                    input.readLine("\nPress Enter to continue...");
                    break;

                case "0":
                    System.out.println("\nLogging out from Admin Dashboard. Returning to Login Menu...");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    // =========================================================================
    // 1.6 Statistics & Reports (Admin)
    // =========================================================================

    /**
     * Submenu: Statistics / Reports (Admin)
     * Fulfills Requirement 1.6:
     * - Display the total number of registered players.
     * - Provide an overview of player activity.
     */
    private static void adminStatisticsReportsMenu(InputHandling input, AdminDataManager adminDataManager) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("          STATISTICS & REPORTS (ADMIN - 1.6)");
            System.out.println("==================================================");
            System.out.println("1. Total Registered Players & Status Breakdown");
            System.out.println("2. Overview of Player Activity & Economy Assets");
            System.out.println("3. Player Leaderboards (Wealth & Asset Collectors)");
            System.out.println("4. Hero Ownership & Popularity Distribution");
            System.out.println("0. Back to Admin Dashboard");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    adminDisplayRegisteredPlayers(input, adminDataManager);
                    break;
                case "2":
                    adminPlayerActivityOverview(input, adminDataManager);
                    break;
                case "3":
                    adminPlayerLeaderboards(input, adminDataManager);
                    break;
                case "4":
                    adminHeroPopularityReport(input, adminDataManager);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    /**
     * Requirement 1.6: Display the total number of registered players.
     */
    private static void adminDisplayRegisteredPlayers(InputHandling input, AdminDataManager adminDataManager) {
        int total = adminDataManager.getTotalPlayersCount();
        int active = adminDataManager.getActivePlayersCount();
        int banned = adminDataManager.getBannedPlayersCount();
        double activePct = (total > 0) ? ((double) active / total * 100.0) : 0.0;
        double bannedPct = (total > 0) ? ((double) banned / total * 100.0) : 0.0;

        System.out.println("\n==================================================");
        System.out.println("          TOTAL REGISTERED PLAYERS REPORT");
        System.out.println("==================================================");
        System.out.printf("  Total Registered Players : %,d\n", total);
        System.out.printf("  Active Accounts          : %,d (%.1f%%)\n", active, activePct);
        System.out.printf("  Banned / Suspended       : %,d (%.1f%%)\n", banned, bannedPct);
        System.out.println("--------------------------------------------------");

        LinkedList<PlayerAcc> players = (adminDataManager.getPlayerDataManager() != null)
                ? adminDataManager.getPlayerDataManager().getPlayerList()
                : new LinkedList<>();

        if (players.isEmpty()) {
            System.out.println("  No player accounts registered yet.");
        } else {
            System.out.printf("  %-4s %-16s %-10s %-10s %-8s %-7s %-6s\n",
                    "#", "Username", "Status", "Coins", "Shards", "Heroes", "Items");
            System.out.println("  --------------------------------------------------------------------------");
            int idx = 1;
            for (PlayerAcc p : players) {
                String status = p.isBanned() ? "[BANNED]" : "[ACTIVE]";
                System.out.printf("  [%2d] %-16s %-10s %-10s %-8s %-7d %-6d\n",
                        idx++,
                        p.getUsername(),
                        status,
                        String.format("%,d", p.getCoins()),
                        String.format("%,d", p.getShards()),
                        p.getRoster().size(),
                        p.getInventory().size());
            }
        }
        System.out.println("==================================================");
        input.readLine("\nPress Enter to return...");
    }

    /**
     * Requirement 1.6: Provide an overview of player activity.
     */
    private static void adminPlayerActivityOverview(InputHandling input, AdminDataManager adminDataManager) {
        int totalPlayers = adminDataManager.getTotalPlayersCount();
        int activePlayers = adminDataManager.getActivePlayersCount();
        int bannedPlayers = adminDataManager.getBannedPlayersCount();

        long totalCoins = adminDataManager.getTotalEconomyCoins();
        long totalShards = adminDataManager.getTotalEconomyShards();
        int totalHeroes = adminDataManager.getTotalHeroesOwnedAcrossPlayers();
        int totalItems = adminDataManager.getTotalItemsOwnedAcrossPlayers();

        long avgCoins = (totalPlayers > 0) ? (totalCoins / totalPlayers) : 0;
        long avgShards = (totalPlayers > 0) ? (totalShards / totalPlayers) : 0;
        double avgHeroes = (totalPlayers > 0) ? ((double) totalHeroes / totalPlayers) : 0.0;
        double avgItems = (totalPlayers > 0) ? ((double) totalItems / totalPlayers) : 0.0;

        System.out.println("\n==================================================");
        System.out.println("             PLAYER ACTIVITY OVERVIEW");
        System.out.println("==================================================");
        System.out.printf("  Total Registered Players     : %,d\n", totalPlayers);
        System.out.printf("  Active Engagement            : %,d active (%,d suspended)\n", activePlayers, bannedPlayers);
        System.out.println("--------------------------------------------------");
        System.out.println("  [CURRENCY & ECONOMY ACTIVITY]");
        System.out.printf("  Total Coins in Circulation   : %,d coins\n", totalCoins);
        System.out.printf("  Average Coins per Player     : %,d coins\n", avgCoins);
        System.out.printf("  Total Shards in Circulation  : %,d shards\n", totalShards);
        System.out.printf("  Average Shards per Player    : %,d shards\n", avgShards);
        System.out.println("--------------------------------------------------");
        System.out.println("  [COLLECTION & INVENTORY ACTIVITY]");
        System.out.printf("  Total Heroes Summoned        : %,d heroes\n", totalHeroes);
        System.out.printf("  Average Heroes per Player    : %.2f heroes\n", avgHeroes);
        System.out.printf("  Total Items Acquired         : %,d items\n", totalItems);
        System.out.printf("  Average Items per Player     : %.2f items\n", avgItems);
        System.out.println("--------------------------------------------------");
        System.out.printf("  Hero Catalog Templates       : %,d templates\n", adminDataManager.getTotalHeroesCatalogCount());
        System.out.printf("  Item Catalog Templates       : %,d templates\n", adminDataManager.getTotalItemsCatalogCount());
        System.out.println("==================================================");
        input.readLine("\nPress Enter to return...");
    }

    /**
     * Requirement 1.6 helper: Player Leaderboards (Wealth & Collectors)
     */
    private static void adminPlayerLeaderboards(InputHandling input, AdminDataManager adminDataManager) {
        System.out.println("\n==================================================");
        System.out.println("                PLAYER LEADERBOARDS");
        System.out.println("==================================================");

        System.out.println("\n--- [TOP WEALTHIEST PLAYERS (COINS)] ---");
        LinkedList<PlayerAcc> topCoins = adminDataManager.getTopPlayersByCoins(5);
        if (topCoins.isEmpty()) {
            System.out.println("  No player records.");
        } else {
            int rank = 1;
            for (PlayerAcc p : topCoins) {
                System.out.printf("  #%d  %-16s : %,d Coins %s\n",
                        rank++, p.getUsername(), p.getCoins(), (p.isBanned() ? "[BANNED]" : ""));
            }
        }

        System.out.println("\n--- [TOP SHARD HOLDERS] ---");
        LinkedList<PlayerAcc> topShards = adminDataManager.getTopPlayersByShards(5);
        if (topShards.isEmpty()) {
            System.out.println("  No player records.");
        } else {
            int rank = 1;
            for (PlayerAcc p : topShards) {
                System.out.printf("  #%d  %-16s : %,d Shards %s\n",
                        rank++, p.getUsername(), p.getShards(), (p.isBanned() ? "[BANNED]" : ""));
            }
        }

        System.out.println("\n--- [TOP HERO COLLECTORS (ROSTER SIZE)] ---");
        LinkedList<PlayerAcc> topHeroes = adminDataManager.getTopPlayersByHeroes(5);
        if (topHeroes.isEmpty()) {
            System.out.println("  No player records.");
        } else {
            int rank = 1;
            for (PlayerAcc p : topHeroes) {
                System.out.printf("  #%d  %-16s : %d Heroes in Roster\n",
                        rank++, p.getUsername(), p.getRoster().size());
            }
        }

        System.out.println("\n--- [TOP ITEM COLLECTORS (INVENTORY SIZE)] ---");
        LinkedList<PlayerAcc> topItems = adminDataManager.getTopPlayersByItems(5);
        if (topItems.isEmpty()) {
            System.out.println("  No player records.");
        } else {
            int rank = 1;
            for (PlayerAcc p : topItems) {
                System.out.printf("  #%d  %-16s : %d Items in Inventory\n",
                        rank++, p.getUsername(), p.getInventory().size());
            }
        }

        System.out.println("==================================================");
        input.readLine("\nPress Enter to return...");
    }

    /**
     * Requirement 1.6 helper: Hero Popularity / Ownership Analysis
     */
    private static void adminHeroPopularityReport(InputHandling input, AdminDataManager adminDataManager) {
        System.out.println("\n==================================================");
        System.out.println("         HERO POPULARITY & OWNERSHIP REPORT");
        System.out.println("==================================================");
        java.util.TreeMap<String, Integer> dist = adminDataManager.getHeroOwnershipDistribution();

        if (dist.isEmpty()) {
            System.out.println("  No heroes have been unlocked by any players yet.");
        } else {
            // Sort by count descending
            java.util.List<java.util.Map.Entry<String, Integer>> sorted = new java.util.ArrayList<>(dist.entrySet());
            sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            System.out.printf("  %-4s %-25s %-12s\n", "#", "Hero Name", "Players Owned");
            System.out.println("  --------------------------------------------------");
            int rank = 1;
            for (java.util.Map.Entry<String, Integer> entry : sorted) {
                System.out.printf("  [%2d] %-25s : %d player(s)\n", rank++, entry.getKey(), entry.getValue());
            }
        }
        System.out.println("==================================================");
        input.readLine("\nPress Enter to return...");
    }

    /**
     * Submenu 2: Player Account Management (1.3 Requirement)
     */
    private static void adminPlayerManagement(InputHandling input, AdminDataManager adminDataManager) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("             PLAYER ACCOUNT MANAGEMENT");
            System.out.println("==================================================");
            System.out.println("1. View All Registered Players & Statistics");
            System.out.println("2. Search Player by Username");
            System.out.println("3. Ban / Unban Player Account");
            System.out.println("4. Delete Player Account");
            System.out.println("5. Modify Player Currencies (Coins / Shards)");
            System.out.println("6. Reset Player Account Progress");
            System.out.println("0. Back to Admin Dashboard");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    adminViewAllPlayers(input, adminDataManager);
                    break;
                case "2":
                    adminSearchPlayer(input, adminDataManager);
                    break;
                case "3":
                    adminBanUnbanPlayer(input, adminDataManager);
                    break;
                case "4":
                    adminDeletePlayer(input, adminDataManager);
                    break;
                case "5":
                    adminModifyCurrencyMenu(input, adminDataManager);
                    break;
                case "6":
                    adminResetPlayer(input, adminDataManager);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    /**
     * Requirement: View all registered players and their statistics.
     */
    private static void adminViewAllPlayers(InputHandling input, AdminDataManager adminDataManager) {
        LinkedList<PlayerAcc> players = (adminDataManager.getPlayerDataManager() != null)
                ? adminDataManager.getPlayerDataManager().getPlayerList()
                : new LinkedList<>();

        System.out.println("\n==========================================================================");
        System.out.println("                   ALL REGISTERED PLAYERS & STATISTICS");
        System.out.println("==========================================================================");
        if (players.isEmpty()) {
            System.out.println("  No registered players found in the system.");
        } else {
            System.out.printf("  %-4s %-16s %-10s %-10s %-8s %-7s %-6s\n",
                    "#", "Username", "Status", "Coins", "Shards", "Heroes", "Items");
            System.out.println("  --------------------------------------------------------------------------");
            int idx = 1;
            for (PlayerAcc p : players) {
                String status = p.isBanned() ? "[BANNED]" : "[ACTIVE]";
                System.out.printf("  [%2d] %-16s %-10s %-10s %-8s %-7d %-6d\n",
                        idx++,
                        p.getUsername(),
                        status,
                        String.format("%,d", p.getCoins()),
                        String.format("%,d", p.getShards()),
                        p.getRoster().size(),
                        p.getInventory().size());
            }
            System.out.println("  --------------------------------------------------------------------------");
            System.out.printf("  Total: %d Players (Active: %d | Banned: %d)\n",
                    players.size(),
                    adminDataManager.getActivePlayersCount(),
                    adminDataManager.getBannedPlayersCount());
            System.out.printf("  Total Economy: %,d Coins | %,d Shards\n",
                    adminDataManager.getTotalEconomyCoins(),
                    adminDataManager.getTotalEconomyShards());
        }
        System.out.println("==========================================================================");
        input.readLine("\nPress Enter to return...");
    }

    /**
     * Requirement: Search for a player by username.
     */
    private static void adminSearchPlayer(InputHandling input, AdminDataManager adminDataManager) {
        System.out.println("\n==================================================");
        System.out.println("            SEARCH PLAYER BY USERNAME");
        System.out.println("==================================================");
        String query = input.readLine("Enter username or search query (or '0' to cancel): ");
        if (query.equals("0") || query.trim().isEmpty()) return;

        LinkedList<PlayerAcc> matches = adminDataManager.searchPlayers(query.trim());
        if (matches.isEmpty()) {
            System.out.println("\n[!] No player found matching '" + query + "'.");
            input.readLine("Press Enter to continue...");
            return;
        }

        PlayerAcc selectedPlayer;
        if (matches.size() == 1) {
            selectedPlayer = matches.getFirst();
        } else {
            System.out.println("\nFound " + matches.size() + " matching players:");
            int idx = 1;
            for (PlayerAcc p : matches) {
                String status = p.isBanned() ? "[BANNED]" : "[ACTIVE]";
                System.out.printf("  [%d] %-16s %-10s (Coins: %,d | Shards: %,d)\n",
                        idx++, p.getUsername(), status, p.getCoins(), p.getShards());
            }
            String choice = input.readLine("\nSelect player number to inspect (or '0' to cancel): ");
            if (choice.equals("0")) return;
            try {
                int selectedIdx = Integer.parseInt(choice) - 1;
                if (selectedIdx >= 0 && selectedIdx < matches.size()) {
                    selectedPlayer = matches.get(selectedIdx);
                } else {
                    System.out.println("Invalid selection.");
                    input.readLine("Press Enter to continue...");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
                input.readLine("Press Enter to continue...");
                return;
            }
        }

        displayPlayerFullProfile(input, adminDataManager, selectedPlayer);
    }

    /**
     * Displays a full player profile and offers interactive admin actions.
     */
    private static void displayPlayerFullProfile(InputHandling input, AdminDataManager adminDataManager, PlayerAcc player) {
        String choice;
        do {
            String status = player.isBanned() ? "[BANNED / SUSPENDED]" : "[ACTIVE]";
            System.out.println("\n==================================================");
            System.out.println("              PLAYER PROFILE: " + player.getUsername());
            System.out.println("==================================================");
            System.out.println("  Account Status   : " + status);
            System.out.printf("  Coins            : %,d\n", player.getCoins());
            System.out.printf("  Shards           : %,d\n", player.getShards());
            System.out.printf("  Heroes Summoned  : %d\n", player.getRoster().size());
            System.out.printf("  Inventory Items  : %d\n", player.getInventory().size());
            System.out.println("--------------------------------------------------");

            System.out.println("  [HERO ROSTER]");
            if (player.getRoster().isEmpty()) {
                System.out.println("    (No heroes in roster)");
            } else {
                int hIdx = 1;
                for (Hero h : player.getRoster()) {
                    System.out.printf("    [%d] %s (%s | %s) HP:%d ATK:%d DEF:%d\n",
                            hIdx++, h.getName(), h.getCharacterClass().getDisplayName(),
                            h.getStarsDisplay(), h.getTotalHp(), h.getTotalAttack(), h.getTotalDefense());
                    String weaponName = (h.getEquippedWeapon() != null) ? h.getEquippedWeapon().getName() : "None";
                    String helmetName = (h.getEquippedArmor(Hero.SLOT_HELMET) != null) ? h.getEquippedArmor(Hero.SLOT_HELMET).getName() : "None";
                    String chestName = (h.getEquippedArmor(Hero.SLOT_CHEST) != null) ? h.getEquippedArmor(Hero.SLOT_CHEST).getName() : "None";
                    String leggingsName = (h.getEquippedArmor(Hero.SLOT_LEGGINGS) != null) ? h.getEquippedArmor(Hero.SLOT_LEGGINGS).getName() : "None";
                    String accName = (h.getEquippedAccessory() != null) ? h.getEquippedAccessory().getName() : "None";
                    System.out.printf("        Gear: [W: %s | H: %s | C: %s | L: %s | Acc: %s]\n",
                            weaponName, helmetName, chestName, leggingsName, accName);
                }
            }

            System.out.println("--------------------------------------------------");
            System.out.println("  [INVENTORY ITEMS]");
            if (player.getInventory().isEmpty()) {
                System.out.println("    (Inventory is empty)");
            } else {
                int iIdx = 1;
                for (Item item : player.getInventory()) {
                    System.out.printf("    [%d] %s (%s, Qty: %d)\n",
                            iIdx++, item.toString(), item.getType(), item.getQuantity());
                }
            }
            System.out.println("==================================================");
            System.out.println("Actions: [1] " + (player.isBanned() ? "Unban Account" : "Ban Account")
                    + " | [2] Delete Account | [3] Modify Coins/Shards | [0] Return");
            System.out.println("==================================================");

            choice = input.readLine("Select action: ");
            switch (choice) {
                case "1":
                    adminDataManager.togglePlayerBan(player.getUsername());
                    System.out.println("\n[SUCCESS] Player '" + player.getUsername() + "' status changed to: "
                            + (player.isBanned() ? "[BANNED]" : "[ACTIVE]"));
                    input.readLine("Press Enter to continue...");
                    break;
                case "2":
                    System.out.println("\n[DANGER] This will PERMANENTLY delete account '" + player.getUsername() + "'!");
                    String confirm = input.readLine("Type 'DELETE' to confirm: ");
                    if (confirm.equals("DELETE")) {
                        adminDataManager.deletePlayer(player.getUsername());
                        System.out.println("\n[SUCCESS] Player account permanently deleted.");
                        input.readLine("Press Enter to return...");
                        return;
                    } else {
                        System.out.println("\nOperation cancelled.");
                        input.readLine("Press Enter to continue...");
                    }
                    break;
                case "3":
                    adminModifyCurrencyMenu(input, adminDataManager, player.getUsername());
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option.");
                    input.readLine("Press Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    /**
     * Requirement: Ban or unban a player account.
     */
    private static void adminBanUnbanPlayer(InputHandling input, AdminDataManager adminDataManager) {
        System.out.println("\n==================================================");
        System.out.println("             BAN / UNBAN PLAYER ACCOUNT");
        System.out.println("==================================================");
        String username = input.readLine("Enter player username (or '0' to cancel): ");
        if (username.equals("0") || username.trim().isEmpty()) return;

        PlayerAcc player = adminDataManager.findPlayer(username);
        if (player == null) {
            System.out.println("\n[!] Player '" + username + "' not found.");
            input.readLine("Press Enter to continue...");
            return;
        }

        boolean currentStatus = player.isBanned();
        System.out.println("\nPlayer: " + player.getUsername() + " | Current Status: "
                + (currentStatus ? "[BANNED]" : "[ACTIVE]"));

        if (!currentStatus) {
            String confirm = input.readLine("Do you want to BAN player '" + player.getUsername() + "'? (y/N): ");
            if (confirm.equalsIgnoreCase("y")) {
                adminDataManager.banPlayer(player.getUsername());
                System.out.println("\n[SUCCESS] Player '" + player.getUsername() + "' has been BANNED. Login access is now revoked.");
            } else {
                System.out.println("\nOperation cancelled.");
            }
        } else {
            String confirm = input.readLine("Do you want to UNBAN player '" + player.getUsername() + "'? (y/N): ");
            if (confirm.equalsIgnoreCase("y")) {
                adminDataManager.unbanPlayer(player.getUsername());
                System.out.println("\n[SUCCESS] Player '" + player.getUsername() + "' has been UNBANNED. Login access restored.");
            } else {
                System.out.println("\nOperation cancelled.");
            }
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement: Delete a player account.
     */
    private static void adminDeletePlayer(InputHandling input, AdminDataManager adminDataManager) {
        System.out.println("\n==================================================");
        System.out.println("             DELETE PLAYER ACCOUNT");
        System.out.println("==================================================");
        String username = input.readLine("Enter player username to PERMANENTLY DELETE (or '0' to cancel): ");
        if (username.equals("0") || username.trim().isEmpty()) return;

        PlayerAcc player = adminDataManager.findPlayer(username);
        if (player == null) {
            System.out.println("\n[!] Player '" + username + "' not found.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\n[DANGER] This will PERMANENTLY delete account '" + player.getUsername()
                + "' and remove all associated hero roster and inventory data!");
        String confirm = input.readLine("Type 'DELETE' to confirm: ");
        if (confirm.equals("DELETE")) {
            boolean success = adminDataManager.deletePlayer(player.getUsername());
            if (success) {
                System.out.println("\n[SUCCESS] Player account '" + player.getUsername() + "' was permanently deleted.");
            } else {
                System.out.println("\n[!] Failed to delete player account.");
            }
        } else {
            System.out.println("\nConfirmation mismatch. Operation cancelled.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    private static void adminModifyCurrencyMenu(InputHandling input, AdminDataManager adminDataManager) {
        String username = input.readLine("\nEnter player username to modify currencies (or '0' to cancel): ");
        if (username.equals("0") || username.trim().isEmpty()) return;
        adminModifyCurrencyMenu(input, adminDataManager, username);
    }

    private static void adminModifyCurrencyMenu(InputHandling input, AdminDataManager adminDataManager, String username) {
        PlayerAcc player = adminDataManager.findPlayer(username);
        if (player == null) {
            System.out.println("\n[!] Player '" + username + "' not found.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\nSelect currency to modify for '" + player.getUsername() + "':");
        System.out.println("1. Coins (Current: " + String.format("%,d", player.getCoins()) + ")");
        System.out.println("2. Shards (Current: " + String.format("%,d", player.getShards()) + ")");
        System.out.println("0. Cancel");
        String opt = input.readLine("Select: ");

        if (opt.equals("1")) {
            adminModifyCurrency(input, adminDataManager, player.getUsername(), false);
        } else if (opt.equals("2")) {
            adminModifyCurrency(input, adminDataManager, player.getUsername(), true);
        }
    }

    private static void adminModifyCurrency(InputHandling input, AdminDataManager adminDataManager,
                                            String username, boolean isShards) {
        String currencyLabel = isShards ? "Shards" : "Coins";
        PlayerAcc player = adminDataManager.findPlayer(username);
        if (player == null) return;

        int currentBalance = isShards ? player.getShards() : player.getCoins();
        System.out.printf("\nCurrent %s for %s: %,d\n", currencyLabel, player.getUsername(), currentBalance);
        String amountStr = input.readLine("Enter amount to ADD (positive) or DEDUCT (negative): ");

        try {
            int delta = Integer.parseInt(amountStr);
            boolean success = isShards
                    ? adminDataManager.modifyPlayerShards(username, delta)
                    : adminDataManager.modifyPlayerCoins(username, delta);

            if (success) {
                int newBalance = isShards ? player.getShards() : player.getCoins();
                System.out.printf("\n[SUCCESS] Updated %s for %s! New Balance: %,d %s\n",
                        currencyLabel, player.getUsername(), newBalance, currencyLabel);
            } else {
                System.out.println("\n[!] Failed to update currency.");
            }
        } catch (NumberFormatException e) {
            System.out.println("\n[!] Invalid number format.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    private static void adminResetPlayer(InputHandling input, AdminDataManager adminDataManager) {
        String username = input.readLine("\nEnter player username to RESET progress (or '0' to cancel): ");
        if (username.equals("0") || username.trim().isEmpty()) return;

        PlayerAcc player = adminDataManager.findPlayer(username);
        if (player == null) {
            System.out.println("\n[!] Player '" + username + "' not found.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\n[WARNING] This will reset coins to 500, shards to 10, and wipe all items & heroes!");
        String confirm = input.readLine("Are you sure you want to proceed? (y/N): ");
        if (confirm.equalsIgnoreCase("y")) {
            boolean success = adminDataManager.resetPlayerProgress(username);
            if (success) {
                System.out.println("\n[SUCCESS] Player '" + username + "' progress has been reset.");
            } else {
                System.out.println("\n[!] Failed to reset player progress.");
            }
        } else {
            System.out.println("\nOperation cancelled.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    // =========================================================================
    // 1.4 Item / Catalog Management (Admin)
    // =========================================================================

    /**
     * Submenu 3: Item / Catalog Management (Admin - 1.4)
     * Fulfills Requirement 1.4:
     * - View all catalog items, including weapons, armor, and accessories.
     * - Add new items to the CSV catalog.
     * - Edit or update existing item statistics.
     * - Delete items from the catalog.
     * - View all heroes from all class CSV files.
     * - Add, edit, or remove heroes from the catalog.
     */
    private static void adminCatalogsMenu(InputHandling input, AdminDataManager adminDataManager) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("       ITEM / CATALOG MANAGEMENT (ADMIN - 1.4)");
            System.out.println("==================================================");
            System.out.println("--- [ITEM CATALOG] ---");
            System.out.println("1. View All Catalog Items (Weapons, Armor, Accessories)");
            System.out.println("2. Add New Item to CSV Catalog");
            System.out.println("3. Edit Existing Item Statistics");
            System.out.println("4. Delete Item from Catalog");
            System.out.println("--- [HERO CATALOG] ---");
            System.out.println("5. View All Heroes from Class CSV Files");
            System.out.println("6. Add New Hero to Catalog");
            System.out.println("7. Edit Existing Hero Statistics");
            System.out.println("8. Remove Hero from Catalog");
            System.out.println("--------------------------------------------------");
            System.out.println("0. Back to Admin Dashboard");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    adminViewItemsCatalog(input, adminDataManager);
                    break;
                case "2":
                    adminAddNewItem(input, adminDataManager);
                    break;
                case "3":
                    adminEditItem(input, adminDataManager);
                    break;
                case "4":
                    adminDeleteItem(input, adminDataManager);
                    break;
                case "5":
                    adminViewHeroesCatalog(input, adminDataManager);
                    break;
                case "6":
                    adminAddNewHero(input, adminDataManager);
                    break;
                case "7":
                    adminEditHero(input, adminDataManager);
                    break;
                case "8":
                    adminRemoveHero(input, adminDataManager);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    /**
     * Requirement 1.4: View all catalog items, including weapons, armor, and accessories.
     */
    private static void adminViewItemsCatalog(InputHandling input, AdminDataManager adminDataManager) {
        ItemDataManager idm = adminDataManager.getItemDataManager();
        if (idm == null) {
            System.out.println("\n[!] Item Data Manager is not available.");
            input.readLine("Press Enter to return...");
            return;
        }

        System.out.println("\nSelect Item Category to view:");
        System.out.println("1. Weapons (" + idm.getWeapons().size() + ")");
        System.out.println("2. Armor Pieces (" + idm.getArmors().size() + ")");
        System.out.println("3. Accessories (" + idm.getAccessories().size() + ")");
        System.out.println("4. All Items Combined (" + (idm.getWeapons().size() + idm.getArmors().size() + idm.getAccessories().size()) + ")");
        System.out.println("0. Cancel");
        String cat = input.readLine("Select: ");

        if (cat.equals("1")) {
            System.out.println("\n==========================================================================");
            System.out.println("             WEAPON CATALOG (" + idm.getWeapons().size() + " Items)");
            System.out.println("==========================================================================");
            System.out.printf("  %-6s %-22s %-10s %-8s %-5s %-6s %-16s\n",
                    "ID", "Name", "Rarity", "Type", "ATK", "CRIT", "Classes");
            System.out.println("  --------------------------------------------------------------------------");
            for (Weapon w : idm.getWeapons()) {
                System.out.printf("  %-6s %-22s %-10s %-8s %-5d %-6s %-16s\n",
                        w.getId(), w.getName(), w.getRarity(), w.getType(),
                        w.getAttack(), w.getCritChance() + "%", w.getEquippableClassesDisplay());
                System.out.println("         Description: " + w.getDescription());
            }
        } else if (cat.equals("2")) {
            System.out.println("\n==========================================================================");
            System.out.println("              ARMOR CATALOG (" + idm.getArmors().size() + " Items)");
            System.out.println("==========================================================================");
            System.out.printf("  %-6s %-22s %-10s %-9s %-10s %-5s %-6s %-5s\n",
                    "ID", "Name", "Rarity", "Slot", "Set", "DEF", "HP+", "HP%");
            System.out.println("  --------------------------------------------------------------------------");
            for (Armor a : idm.getArmors()) {
                System.out.printf("  %-6s %-22s %-10s %-9s %-10s %-5d +%-5d %-5s\n",
                        a.getId(), a.getName(), a.getRarity(), a.getType(), a.getSetName(),
                        a.getDefense(), a.getHealthBonus(), a.getHealthPercentBonus() + "%");
                System.out.println("         Classes: " + a.getEquippableClassesDisplay() + " | " + a.getDescription());
            }
        } else if (cat.equals("3")) {
            System.out.println("\n==========================================================================");
            System.out.println("            ACCESSORY CATALOG (" + idm.getAccessories().size() + " Items)");
            System.out.println("==========================================================================");
            System.out.printf("  %-6s %-22s %-10s %-9s %-6s %-12s\n",
                    "ID", "Name", "Rarity", "Type", "Bonus", "Stat Type");
            System.out.println("  --------------------------------------------------------------------------");
            for (Accessory ac : idm.getAccessories()) {
                System.out.printf("  %-6s %-22s %-10s %-9s +%-5d %-12s\n",
                        ac.getId(), ac.getName(), ac.getRarity(), ac.getType(),
                        ac.getStatBonus(), ac.getBonusType().toUpperCase());
                System.out.println("         Classes: " + ac.getEquippableClassesDisplay() + " | " + ac.getDescription());
            }
        } else if (cat.equals("4")) {
            System.out.println("\n==========================================================================");
            System.out.println("                    ALL CATALOG EQUIPMENT ITEMS");
            System.out.println("==========================================================================");
            int idx = 1;
            for (Weapon w : idm.getWeapons()) {
                System.out.printf("  [%2d] [WEAPON]    %-6s %-22s [%-9s] ATK:%-3d CRIT:%d%%\n",
                        idx++, w.getId(), w.getName(), w.getRarity(), w.getAttack(), w.getCritChance());
            }
            for (Armor a : idm.getArmors()) {
                System.out.printf("  [%2d] [ARMOR]     %-6s %-22s [%-9s] Slot:%-8s DEF:%-3d HP:+%d\n",
                        idx++, a.getId(), a.getName(), a.getRarity(), a.getType(), a.getDefense(), a.getHealthBonus());
            }
            for (Accessory ac : idm.getAccessories()) {
                System.out.printf("  [%2d] [ACCESSORY] %-6s %-22s [%-9s] +%-3d %s\n",
                        idx++, ac.getId(), ac.getName(), ac.getRarity(), ac.getStatBonus(), ac.getBonusType().toUpperCase());
            }
        } else {
            return;
        }

        System.out.println("==========================================================================");
        input.readLine("\nPress Enter to return...");
    }

    /**
     * Requirement 1.4: Add new items to the CSV catalog.
     */
    private static void adminAddNewItem(InputHandling input, AdminDataManager adminDataManager) {
        ItemDataManager idm = adminDataManager.getItemDataManager();
        if (idm == null) return;

        System.out.println("\n==================================================");
        System.out.println("             ADD NEW ITEM TO CATALOG");
        System.out.println("==================================================");
        System.out.println("Select item category to create:");
        System.out.println("1. Weapon");
        System.out.println("2. Armor");
        System.out.println("3. Accessory");
        System.out.println("0. Cancel");
        String cat = input.readLine("Select category: ");

        if (!cat.equals("1") && !cat.equals("2") && !cat.equals("3")) return;

        String id;
        while (true) {
            id = input.readLine("Enter unique Item ID (e.g. W999, A999, AC999, or '0' to cancel): ");
            if (id.equals("0")) return;
            if (id.isEmpty()) {
                System.out.println("ID cannot be empty.");
            } else if (idm.getItemById(id) != null) {
                System.out.println("An item with ID '" + id + "' already exists!");
            } else {
                break;
            }
        }

        String name = input.readLine("Enter Item Name: ");
        if (name.isEmpty()) name = "Unnamed Item";

        System.out.println("Available Rarities: Common, Uncommon, Rare, Epic, Legendary");
        String rarity = input.readLine("Enter Rarity (default: Common): ");
        if (rarity.isEmpty()) rarity = "Common";

        String classes = input.readLine("Enter Equippable Classes (e.g. 'All' or 'Warrior;Tank'): ");
        if (classes.isEmpty()) classes = "All";

        String desc = input.readLine("Enter Description: ");

        if (cat.equals("1")) {
            String type = input.readLine("Enter Weapon Type (e.g. Sword, Axe, Bow, Dagger, Staff): ");
            if (type.isEmpty()) type = "Sword";
            int atk = readInt(input, "Enter Attack Bonus: ", 10);
            int crit = readInt(input, "Enter Crit Chance %: ", 5);

            Weapon weapon = new Weapon(id, name, rarity, type, atk, crit, classes, desc);
            boolean success = idm.addWeapon(weapon);
            if (success) {
                System.out.println("\n[SUCCESS] Weapon '" + name + "' [" + id + "] added to weapons.csv catalog!");
            } else {
                System.out.println("\n[!] Failed to save weapon to catalog.");
            }
        } else if (cat.equals("2")) {
            System.out.println("Available Slots: Helmet, Chest, Leggings");
            String type = input.readLine("Enter Slot Type (Helmet/Chest/Leggings): ");
            if (type.isEmpty()) type = "Chest";

            String setName = input.readLine("Enter Armor Set Name (or 'None'): ");
            if (setName.isEmpty()) setName = "None";

            int def = readInt(input, "Enter Defense Bonus: ", 10);
            int hpBonus = readInt(input, "Enter Flat HP Bonus: ", 20);
            int hpPct = readInt(input, "Enter Health % Bonus: ", 0);

            Armor armor = new Armor(id, name, rarity, type, setName, def, hpBonus, hpPct, classes, desc);
            boolean success = idm.addArmor(armor);
            if (success) {
                System.out.println("\n[SUCCESS] Armor '" + name + "' [" + id + "] added to armor.csv catalog!");
            } else {
                System.out.println("\n[!] Failed to save armor to catalog.");
            }
        } else if (cat.equals("3")) {
            String type = input.readLine("Enter Accessory Type (e.g. Ring, Amulet, Charm, Trinket): ");
            if (type.isEmpty()) type = "Ring";

            System.out.println("Bonus Types: attack, health, defense, crit_chance");
            String bonusType = input.readLine("Enter Bonus Stat Type: ");
            if (bonusType.isEmpty()) bonusType = "attack";

            int bonus = readInt(input, "Enter Stat Bonus Amount: ", 15);

            Accessory acc = new Accessory(id, name, rarity, type, bonus, bonusType, classes, desc);
            boolean success = idm.addAccessory(acc);
            if (success) {
                System.out.println("\n[SUCCESS] Accessory '" + name + "' [" + id + "] added to accessories.csv catalog!");
            } else {
                System.out.println("\n[!] Failed to save accessory to catalog.");
            }
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.4: Edit or update existing item statistics.
     */
    private static void adminEditItem(InputHandling input, AdminDataManager adminDataManager) {
        ItemDataManager idm = adminDataManager.getItemDataManager();
        if (idm == null) return;

        System.out.println("\n==================================================");
        System.out.println("            EDIT ITEM IN CSV CATALOG");
        System.out.println("==================================================");
        String id = input.readLine("Enter Item ID to edit (or '0' to cancel): ");
        if (id.equals("0") || id.isEmpty()) return;

        Item item = idm.getItemById(id);
        if (item == null) {
            System.out.println("\n[!] No item found with ID '" + id + "'.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\nEditing Item [" + item.getId() + "] " + item.getName());
        System.out.println("(Press Enter on any prompt to keep the current value)\n");

        String name = promptKeepCurrent(input, "Name", item.getName());
        String rarity = promptKeepCurrent(input, "Rarity", item.getRarity());
        String type = promptKeepCurrent(input, "Type", item.getType());
        String classes = promptKeepCurrent(input, "Equippable Classes", item.getEquippableClassesRaw());
        String desc = promptKeepCurrent(input, "Description", item.getDescription());

        boolean updated = false;

        if (item instanceof Weapon) {
            Weapon w = (Weapon) item;
            int atk = readInt(input, "Attack Bonus [" + w.getAttack() + "]: ", w.getAttack());
            int crit = readInt(input, "Crit Chance % [" + w.getCritChance() + "]: ", w.getCritChance());

            Weapon newWeapon = new Weapon(w.getId(), name, rarity, type, atk, crit, classes, desc);
            updated = idm.updateWeapon(w.getId(), newWeapon);
        } else if (item instanceof Armor) {
            Armor a = (Armor) item;
            String setName = promptKeepCurrent(input, "Set Name", a.getSetName());
            int def = readInt(input, "Defense Bonus [" + a.getDefense() + "]: ", a.getDefense());
            int hp = readInt(input, "Flat HP Bonus [" + a.getHealthBonus() + "]: ", a.getHealthBonus());
            int hpPct = readInt(input, "Health % Bonus [" + a.getHealthPercentBonus() + "]: ", a.getHealthPercentBonus());

            Armor newArmor = new Armor(a.getId(), name, rarity, type, setName, def, hp, hpPct, classes, desc);
            updated = idm.updateArmor(a.getId(), newArmor);
        } else if (item instanceof Accessory) {
            Accessory ac = (Accessory) item;
            String bonusType = promptKeepCurrent(input, "Bonus Stat Type", ac.getBonusType());
            int bonus = readInt(input, "Stat Bonus [" + ac.getStatBonus() + "]: ", ac.getStatBonus());

            Accessory newAcc = new Accessory(ac.getId(), name, rarity, type, bonus, bonusType, classes, desc);
            updated = idm.updateAccessory(ac.getId(), newAcc);
        }

        if (updated) {
            System.out.println("\n[SUCCESS] Item '" + id + "' updated successfully and saved to CSV!");
        } else {
            System.out.println("\n[!] Failed to update item.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.4: Delete items from the catalog.
     */
    private static void adminDeleteItem(InputHandling input, AdminDataManager adminDataManager) {
        ItemDataManager idm = adminDataManager.getItemDataManager();
        if (idm == null) return;

        System.out.println("\n==================================================");
        System.out.println("           DELETE ITEM FROM CATALOG");
        System.out.println("==================================================");
        String id = input.readLine("Enter Item ID to delete (or '0' to cancel): ");
        if (id.equals("0") || id.isEmpty()) return;

        Item item = idm.getItemById(id);
        if (item == null) {
            System.out.println("\n[!] No item found with ID '" + id + "'.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\nItem: [" + item.getId() + "] " + item.getName() + " (" + item.getType() + " - " + item.getRarity() + ")");
        String confirm = input.readLine("Are you sure you want to PERMANENTLY delete this item? (y/N): ");
        if (confirm.equalsIgnoreCase("y")) {
            boolean success = idm.deleteItem(id);
            if (success) {
                System.out.println("\n[SUCCESS] Item '" + id + "' was deleted from catalog and CSV file.");
            } else {
                System.out.println("\n[!] Failed to delete item.");
            }
        } else {
            System.out.println("\nOperation cancelled.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.4: View all heroes from all class CSV files.
     */
    private static void adminViewHeroesCatalog(InputHandling input, AdminDataManager adminDataManager) {
        CharacterDataManager cdm = adminDataManager.getCharDataManager();
        if (cdm == null) {
            System.out.println("\n[!] Character Data Manager is not available.");
            input.readLine("Press Enter to return...");
            return;
        }

        System.out.println("\nSelect Hero Class to filter:");
        System.out.println("1. All Heroes (" + cdm.getAllHeroes().size() + ")");
        System.out.println("2. Warrior (" + cdm.getHeroesByClass(CharacterClass.WARRIOR).size() + ")");
        System.out.println("3. Mage (" + cdm.getHeroesByClass(CharacterClass.MAGE).size() + ")");
        System.out.println("4. Assassin (" + cdm.getHeroesByClass(CharacterClass.ASSASSIN).size() + ")");
        System.out.println("5. Tank (" + cdm.getHeroesByClass(CharacterClass.TANK).size() + ")");
        System.out.println("6. Ranger (" + cdm.getHeroesByClass(CharacterClass.RANGER).size() + ")");
        System.out.println("7. Support (" + cdm.getHeroesByClass(CharacterClass.SUPPORT).size() + ")");
        System.out.println("8. Brawler (" + cdm.getHeroesByClass(CharacterClass.BRAWLER).size() + ")");
        System.out.println("0. Cancel");
        String opt = input.readLine("Select: ");

        LinkedList<Hero> heroes;
        String label;
        if (opt.equals("1")) {
            heroes = cdm.getAllHeroes();
            label = "ALL CLASSES";
        } else if (opt.equals("2")) {
            heroes = cdm.getHeroesByClass(CharacterClass.WARRIOR);
            label = "WARRIORS";
        } else if (opt.equals("3")) {
            heroes = cdm.getHeroesByClass(CharacterClass.MAGE);
            label = "MAGES";
        } else if (opt.equals("4")) {
            heroes = cdm.getHeroesByClass(CharacterClass.ASSASSIN);
            label = "ASSASSINS";
        } else if (opt.equals("5")) {
            heroes = cdm.getHeroesByClass(CharacterClass.TANK);
            label = "TANKS";
        } else if (opt.equals("6")) {
            heroes = cdm.getHeroesByClass(CharacterClass.RANGER);
            label = "RANGERS";
        } else if (opt.equals("7")) {
            heroes = cdm.getHeroesByClass(CharacterClass.SUPPORT);
            label = "SUPPORTS";
        } else if (opt.equals("8")) {
            heroes = cdm.getHeroesByClass(CharacterClass.BRAWLER);
            label = "BRAWLERS";
        } else {
            return;
        }

        System.out.println("\n==========================================================================");
        System.out.println("              HERO CATALOG: " + label + " (" + heroes.size() + " Found)");
        System.out.println("==========================================================================");
        int idx = 1;
        for (Hero h : heroes) {
            String titleStr = (h.getTitle() != null && !h.getTitle().isEmpty()) ? " - \"" + h.getTitle() + "\"" : "";
            System.out.printf("  [%2d] %-6s %-22s %-9s %s [Elem: %s]%s\n",
                    idx++, h.getId(), h.getName(), h.getCharacterClass().getDisplayName(),
                    h.getStarsDisplay(), h.getElementalPowerDisplay(), titleStr);
            System.out.printf("       Stats -> HP:%-5d | ATK:%-4d | DEF:%-4d | CRIT:%d%%\n",
                    h.getBaseHp(), h.getBaseAttack(), h.getBaseDefense(), h.getBaseCritChance());
            System.out.println("       Lore  -> " + h.getDescription());
        }
        System.out.println("==========================================================================");
        input.readLine("\nPress Enter to return...");
    }

    /**
     * Requirement 1.4: Add new hero to the catalog and CSV file.
     */
    private static void adminAddNewHero(InputHandling input, AdminDataManager adminDataManager) {
        CharacterDataManager cdm = adminDataManager.getCharDataManager();
        if (cdm == null) return;

        System.out.println("\n==================================================");
        System.out.println("             ADD NEW HERO TO CATALOG");
        System.out.println("==================================================");
        System.out.println("Select Character Class:");
        System.out.println("1. Warrior   2. Mage     3. Assassin  4. Tank");
        System.out.println("5. Ranger    6. Support  7. Brawler   0. Cancel");
        String cChoice = input.readLine("Select Class: ");

        CharacterClass cc = null;
        switch (cChoice) {
            case "1": cc = CharacterClass.WARRIOR; break;
            case "2": cc = CharacterClass.MAGE; break;
            case "3": cc = CharacterClass.ASSASSIN; break;
            case "4": cc = CharacterClass.TANK; break;
            case "5": cc = CharacterClass.RANGER; break;
            case "6": cc = CharacterClass.SUPPORT; break;
            case "7": cc = CharacterClass.BRAWLER; break;
            default: return;
        }

        String id;
        while (true) {
            id = input.readLine("Enter unique Hero ID (e.g. WAR010, BRW010, or '0' to cancel): ");
            if (id.equals("0")) return;
            if (id.isEmpty()) {
                System.out.println("ID cannot be empty.");
            } else if (cdm.getHeroTemplateById(id) != null) {
                System.out.println("A hero with ID '" + id + "' already exists!");
            } else {
                break;
            }
        }

        String name = input.readLine("Enter Hero Name: ");
        if (name.isEmpty()) name = "Unnamed Hero";

        String title = input.readLine("Enter Title / Designation (optional): ");
        String element = input.readLine("Enter Elemental Power (Fire, Water, Earth, Wind, Light, Dark, None): ");
        if (element.isEmpty()) element = "None";

        int stars = readInt(input, "Enter Star Rating (3 - 6): ", 3);
        int hp = readInt(input, "Enter Base HP: ", 500);
        int atk = readInt(input, "Enter Base Attack: ", 75);
        int def = readInt(input, "Enter Base Defense: ", 35);
        int crit = readInt(input, "Enter Base Crit Chance %: ", 10);
        String desc = input.readLine("Enter Lore / Description: ");

        Hero newHero = new Hero(id, name, title, element, cc, stars, hp, atk, def, crit, desc);
        boolean success = cdm.addHero(newHero);
        if (success) {
            System.out.println("\n[SUCCESS] Hero '" + name + "' [" + id + "] added to " + cc.getDisplayName() + " catalog and CSV!");
        } else {
            System.out.println("\n[!] Failed to add hero to catalog.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.4: Edit or update existing hero statistics.
     */
    private static void adminEditHero(InputHandling input, AdminDataManager adminDataManager) {
        CharacterDataManager cdm = adminDataManager.getCharDataManager();
        if (cdm == null) return;

        System.out.println("\n==================================================");
        System.out.println("             EDIT HERO IN CATALOG");
        System.out.println("==================================================");
        String id = input.readLine("Enter Hero ID to edit (or '0' to cancel): ");
        if (id.equals("0") || id.isEmpty()) return;

        Hero hero = cdm.getHeroTemplateById(id);
        if (hero == null) {
            System.out.println("\n[!] Hero with ID '" + id + "' not found.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\nEditing Hero [" + hero.getId() + "] " + hero.getName() + " (" + hero.getCharacterClass().getDisplayName() + ")");
        System.out.println("(Press Enter on any prompt to keep the current value)\n");

        String name = promptKeepCurrent(input, "Name", hero.getName());
        String title = promptKeepCurrent(input, "Title", hero.getTitle());
        String element = promptKeepCurrent(input, "Elemental Power", hero.getElementalPower());
        int stars = readInt(input, "Stars [" + hero.getStars() + "]: ", hero.getStars());
        int hp = readInt(input, "Base HP [" + hero.getBaseHp() + "]: ", hero.getBaseHp());
        int atk = readInt(input, "Base Attack [" + hero.getBaseAttack() + "]: ", hero.getBaseAttack());
        int def = readInt(input, "Base Defense [" + hero.getBaseDefense() + "]: ", hero.getBaseDefense());
        int crit = readInt(input, "Base Crit % [" + hero.getBaseCritChance() + "]: ", hero.getBaseCritChance());
        String desc = promptKeepCurrent(input, "Description", hero.getDescription());

        Hero updatedHero = new Hero(hero.getId(), name, title, element, hero.getCharacterClass(), stars, hp, atk, def, crit, desc);
        boolean success = cdm.updateHero(hero.getId(), updatedHero);
        if (success) {
            System.out.println("\n[SUCCESS] Hero '" + hero.getId() + "' updated successfully and saved to CSV!");
        } else {
            System.out.println("\n[!] Failed to update hero.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.4: Delete / remove hero from the catalog.
     */
    private static void adminRemoveHero(InputHandling input, AdminDataManager adminDataManager) {
        CharacterDataManager cdm = adminDataManager.getCharDataManager();
        if (cdm == null) return;

        System.out.println("\n==================================================");
        System.out.println("            REMOVE HERO FROM CATALOG");
        System.out.println("==================================================");
        String id = input.readLine("Enter Hero ID to remove (or '0' to cancel): ");
        if (id.equals("0") || id.isEmpty()) return;

        Hero hero = cdm.getHeroTemplateById(id);
        if (hero == null) {
            System.out.println("\n[!] Hero with ID '" + id + "' not found.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\nHero: [" + hero.getId() + "] " + hero.getName() + " (" + hero.getCharacterClass().getDisplayName() + " " + hero.getStarsDisplay() + ")");
        String confirm = input.readLine("Are you sure you want to PERMANENTLY delete this hero from catalog? (y/N): ");
        if (confirm.equalsIgnoreCase("y")) {
            boolean success = cdm.removeHero(id);
            if (success) {
                System.out.println("\n[SUCCESS] Hero '" + id + "' was deleted from catalog and CSV file.");
            } else {
                System.out.println("\n[!] Failed to delete hero.");
            }
        } else {
            System.out.println("\nOperation cancelled.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    // =========================================================================
    // 1.5 Game Economy Management (Admin)
    // =========================================================================

    /**
     * Submenu 4: Game Economy Management (Admin - 1.5)
     * Fulfills Requirement 1.5:
     * - Adjust crate reward tables or drop rates in CrateManager.
     * - Adjust gacha pull rates in CharacterGachaManager.
     * - Grant coins or shards to a specific player.
     */
    private static void adminEconomyManagementMenu(InputHandling input, AdminDataManager adminDataManager) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("       GAME ECONOMY MANAGEMENT (ADMIN - 1.5)");
            System.out.println("==================================================");
            System.out.println("1. Adjust Crate Tier Drop Rates (CrateManager)");
            System.out.println("2. Adjust Crate Reward Tables & Loot Probabilities");
            System.out.println("3. Adjust Gacha Pull Rates (CharacterGachaManager)");
            System.out.println("4. Grant Coins or Shards to a Specific Player");
            System.out.println("5. Reset Crate & Gacha Rates to Factory Defaults");
            System.out.println("6. Economy Circulation & Currency Analytics");
            System.out.println("0. Back to Admin Dashboard");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    adminAdjustCrateRates(input, adminDataManager);
                    break;
                case "2":
                    adminAdjustCrateLoot(input, adminDataManager);
                    break;
                case "3":
                    adminAdjustGachaRates(input, adminDataManager);
                    break;
                case "4":
                    adminGrantPlayerCurrencyPrompt(input, adminDataManager);
                    break;
                case "5":
                    adminResetEconomyRates(input, adminDataManager);
                    break;
                case "6":
                    adminEconomyOverview(input, adminDataManager);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    /**
     * Requirement 1.5: Adjust crate drop rates in CrateManager.
     */
    private static void adminAdjustCrateRates(InputHandling input, AdminDataManager adminDataManager) {
        CrateManager cm = adminDataManager.getCrateManager();
        if (cm == null) {
            System.out.println("\n[!] CrateManager is not available.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("       CRATE TIER DROP RATES (CRATEMANAGER)");
        System.out.println("==================================================");
        System.out.printf("  Current Rates (Must sum to 100%%):\n");
        System.out.printf("    - Wooden Crate  : %d%%\n", cm.getWoodenRate());
        System.out.printf("    - Silver Crate  : %d%%\n", cm.getSilverRate());
        System.out.printf("    - Gold Crate    : %d%%\n", cm.getGoldRate());
        System.out.printf("    - Diamond Crate : %d%%\n", cm.getDiamondRate());
        System.out.println("--------------------------------------------------");

        String opt = input.readLine("Do you want to adjust these rates? (y/N): ");
        if (!opt.equalsIgnoreCase("y")) return;

        int wooden = readInt(input, "Enter new Wooden Crate rate %: ", cm.getWoodenRate());
        int silver = readInt(input, "Enter new Silver Crate rate %: ", cm.getSilverRate());
        int gold = readInt(input, "Enter new Gold Crate rate %: ", cm.getGoldRate());
        int diamond = readInt(input, "Enter new Diamond Crate rate %: ", cm.getDiamondRate());

        int total = wooden + silver + gold + diamond;
        if (total != 100) {
            System.out.println("\n[!] Error: Rates must sum to exactly 100%. Current sum: " + total + "%. Rates not changed.");
        } else {
            boolean ok = cm.setCrateTierRates(wooden, silver, gold, diamond);
            if (ok) {
                System.out.println("\n[SUCCESS] Crate tier drop rates updated successfully!");
                System.out.printf("  Wooden: %d%% | Silver: %d%% | Gold: %d%% | Diamond: %d%%\n",
                        wooden, silver, gold, diamond);
            } else {
                System.out.println("\n[!] Failed to set crate rates.");
            }
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.5: Adjust crate reward tables in CrateManager.
     */
    private static void adminAdjustCrateLoot(InputHandling input, AdminDataManager adminDataManager) {
        CrateManager cm = adminDataManager.getCrateManager();
        if (cm == null) return;

        System.out.println("\n==================================================");
        System.out.println("           CRATE REWARD LOOT TABLES");
        System.out.println("==================================================");
        System.out.println("Select Crate to modify loot table:");
        System.out.println("1. Wooden Crate (Coins: " + cm.getWoodenMinCoins() + "-" + cm.getWoodenMaxCoins()
                + " | Rare Item: " + cm.getWoodenRareChance() + "% | Shard: " + cm.getWoodenShardChance() + "%)");
        System.out.println("2. Silver Crate (Coins: " + cm.getSilverMinCoins() + "-" + cm.getSilverMaxCoins()
                + " | Rare Wpn: " + cm.getSilverRareWeaponChance() + "% | Rare Arm: " + cm.getSilverRareArmorChance() + "% | Shards: " + cm.getSilverShardChance() + "%)");
        System.out.println("3. Gold Crate (Coins: " + cm.getGoldMinCoins() + "-" + cm.getGoldMaxCoins()
                + " | Epic Arm: " + cm.getGoldEpicArmorChance() + "% | Epic Wpn: " + cm.getGoldEpicWeaponChance() + "% | Shards: " + cm.getGoldShardChance() + "%)");
        System.out.println("4. Diamond Crate (Coins: " + cm.getDiamondMinCoins() + "-" + cm.getDiamondMaxCoins()
                + " | Leg Arm: " + cm.getDiamondLegArmorChance() + "% | Leg Wpn: " + cm.getDiamondLegWeaponChance() + "% | Shards: " + cm.getDiamondShardChance() + "%)");
        System.out.println("0. Cancel");
        String sel = input.readLine("Select crate: ");

        if (sel.equals("1")) {
            int minC = readInt(input, "Min Coins [" + cm.getWoodenMinCoins() + "]: ", cm.getWoodenMinCoins());
            int maxC = readInt(input, "Max Coins [" + cm.getWoodenMaxCoins() + "]: ", cm.getWoodenMaxCoins());
            int rare = readInt(input, "Rare Item Chance % [" + cm.getWoodenRareChance() + "]: ", cm.getWoodenRareChance());
            int shard = readInt(input, "Shard Bonus Chance % [" + cm.getWoodenShardChance() + "]: ", cm.getWoodenShardChance());
            cm.setWoodenLoot(minC, maxC, rare, shard);
            System.out.println("\n[SUCCESS] Wooden Crate reward table updated!");
        } else if (sel.equals("2")) {
            int minC = readInt(input, "Min Coins [" + cm.getSilverMinCoins() + "]: ", cm.getSilverMinCoins());
            int maxC = readInt(input, "Max Coins [" + cm.getSilverMaxCoins() + "]: ", cm.getSilverMaxCoins());
            int wpn = readInt(input, "Rare Weapon Chance % [" + cm.getSilverRareWeaponChance() + "]: ", cm.getSilverRareWeaponChance());
            int arm = readInt(input, "Rare Armor Chance % [" + cm.getSilverRareArmorChance() + "]: ", cm.getSilverRareArmorChance());
            int shard = readInt(input, "Shards Chance % [" + cm.getSilverShardChance() + "]: ", cm.getSilverShardChance());
            cm.setSilverLoot(minC, maxC, wpn, arm, shard);
            System.out.println("\n[SUCCESS] Silver Crate reward table updated!");
        } else if (sel.equals("3")) {
            int minC = readInt(input, "Min Coins [" + cm.getGoldMinCoins() + "]: ", cm.getGoldMinCoins());
            int maxC = readInt(input, "Max Coins [" + cm.getGoldMaxCoins() + "]: ", cm.getGoldMaxCoins());
            int arm = readInt(input, "Epic Armor Chance % [" + cm.getGoldEpicArmorChance() + "]: ", cm.getGoldEpicArmorChance());
            int wpn = readInt(input, "Epic Weapon Chance % [" + cm.getGoldEpicWeaponChance() + "]: ", cm.getGoldEpicWeaponChance());
            int shard = readInt(input, "Shards Chance % [" + cm.getGoldShardChance() + "]: ", cm.getGoldShardChance());
            cm.setGoldLoot(minC, maxC, arm, wpn, shard);
            System.out.println("\n[SUCCESS] Gold Crate reward table updated!");
        } else if (sel.equals("4")) {
            int minC = readInt(input, "Min Coins [" + cm.getDiamondMinCoins() + "]: ", cm.getDiamondMinCoins());
            int maxC = readInt(input, "Max Coins [" + cm.getDiamondMaxCoins() + "]: ", cm.getDiamondMaxCoins());
            int arm = readInt(input, "Legendary Armor Chance % [" + cm.getDiamondLegArmorChance() + "]: ", cm.getDiamondLegArmorChance());
            int wpn = readInt(input, "Legendary Weapon Chance % [" + cm.getDiamondLegWeaponChance() + "]: ", cm.getDiamondLegWeaponChance());
            int shard = readInt(input, "Shards Chance % [" + cm.getDiamondShardChance() + "]: ", cm.getDiamondShardChance());
            cm.setDiamondLoot(minC, maxC, arm, wpn, shard);
            System.out.println("\n[SUCCESS] Diamond Crate reward table updated!");
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.5: Adjust gacha pull rates in CharacterGachaManager.
     */
    private static void adminAdjustGachaRates(InputHandling input, AdminDataManager adminDataManager) {
        CharacterGachaManager gm = adminDataManager.getGachaManager();
        if (gm == null) {
            System.out.println("\n[!] CharacterGachaManager is not available.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("      GACHA PULL RATES (CHARACTERGACHAMANAGER)");
        System.out.println("==================================================");
        System.out.println("  [SINGLE PULL RATES]");
        System.out.printf("    - 3★ Hero : %.1f%% (%d / 1000)\n", gm.getRate3StarPercent(), gm.getRate3Star());
        System.out.printf("    - 4★ Hero : %.1f%% (%d / 1000)\n", gm.getRate4StarPercent(), gm.getRate4Star());
        System.out.printf("    - 5★ Hero : %.1f%% (%d / 1000)\n", gm.getRate5StarPercent(), gm.getRate5Star());
        System.out.printf("    - 6★ Hero : %.1f%% (%d / 1000)\n", gm.getRate6StarPercent(), gm.getRate6Star());
        System.out.println("--------------------------------------------------");
        System.out.println("  [10x MULTI-PULL GUARANTEE RATES]");
        System.out.printf("    - 4★ Hero Guarantee : %d%%\n", gm.getGuarantee4Star());
        System.out.printf("    - 5★ Hero Guarantee : %d%%\n", gm.getGuarantee5Star());
        System.out.printf("    - 6★ Hero Guarantee : %d%%\n", gm.getGuarantee6Star());
        System.out.println("--------------------------------------------------");
        System.out.println("1. Adjust Single Pull Rates (3★, 4★, 5★, 6★)");
        System.out.println("2. Adjust 10x Pull Guarantee Rates (4★, 5★, 6★)");
        System.out.println("0. Cancel");
        String opt = input.readLine("Select option: ");

        if (opt.equals("1")) {
            System.out.println("\nEnter new rates in per mille (out of 1000, e.g. 650 = 65.0%):");
            int r3 = readInt(input, "3★ Rate [Current: " + gm.getRate3Star() + "]: ", gm.getRate3Star());
            int r4 = readInt(input, "4★ Rate [Current: " + gm.getRate4Star() + "]: ", gm.getRate4Star());
            int r5 = readInt(input, "5★ Rate [Current: " + gm.getRate5Star() + "]: ", gm.getRate5Star());
            int r6 = readInt(input, "6★ Rate [Current: " + gm.getRate6Star() + "]: ", gm.getRate6Star());

            int sum = r3 + r4 + r5 + r6;
            if (sum != 1000) {
                System.out.println("\n[!] Error: Rates must sum to exactly 1000 (100%). Current sum: " + sum + ". Rates not changed.");
            } else {
                gm.setSinglePullRates(r3, r4, r5, r6);
                System.out.println("\n[SUCCESS] Single pull rates updated!");
                System.out.printf("  3★: %.1f%% | 4★: %.1f%% | 5★: %.1f%% | 6★: %.1f%%\n",
                        r3 / 10.0, r4 / 10.0, r5 / 10.0, r6 / 10.0);
            }
        } else if (opt.equals("2")) {
            System.out.println("\nEnter new guarantee rates in percent (out of 100%):");
            int g4 = readInt(input, "4★ Guarantee % [Current: " + gm.getGuarantee4Star() + "]: ", gm.getGuarantee4Star());
            int g5 = readInt(input, "5★ Guarantee % [Current: " + gm.getGuarantee5Star() + "]: ", gm.getGuarantee5Star());
            int g6 = readInt(input, "6★ Guarantee % [Current: " + gm.getGuarantee6Star() + "]: ", gm.getGuarantee6Star());

            int sum = g4 + g5 + g6;
            if (sum != 100) {
                System.out.println("\n[!] Error: Guarantee rates must sum to exactly 100%. Current sum: " + sum + ". Rates not changed.");
            } else {
                gm.setGuaranteeRates(g4, g5, g6);
                System.out.println("\n[SUCCESS] 10x Pull guarantee rates updated!");
                System.out.printf("  4★: %d%% | 5★: %d%% | 6★: %d%%\n", g4, g5, g6);
            }
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.5: Grant coins or shards to a specific player.
     */
    private static void adminGrantPlayerCurrencyPrompt(InputHandling input, AdminDataManager adminDataManager) {
        System.out.println("\n==================================================");
        System.out.println("           GRANT CURRENCY TO PLAYER");
        System.out.println("==================================================");
        String username = input.readLine("Enter player username (or '0' to cancel): ");
        if (username.equals("0") || username.isEmpty()) return;

        PlayerAcc player = adminDataManager.findPlayer(username);
        if (player == null) {
            System.out.println("\n[!] Player '" + username + "' not found.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\nPlayer: " + player.getUsername());
        System.out.printf("Current Balance: %,d Coins | %,d Shards\n", player.getCoins(), player.getShards());
        System.out.println("--------------------------------------------------");

        int coins = readInt(input, "Enter Coins to grant (0 for none): ", 0);
        int shards = readInt(input, "Enter Shards to grant (0 for none): ", 0);

        if (coins <= 0 && shards <= 0) {
            System.out.println("\nNo currencies specified. Operation cancelled.");
            input.readLine("Press Enter to continue...");
            return;
        }

        boolean success = adminDataManager.grantPlayerCurrency(player.getUsername(), coins, shards);
        if (success) {
            System.out.println("\n[SUCCESS] Currency grant applied!");
            System.out.printf("  Player '%s' new balance: %,d Coins (+%,d) | %,d Shards (+%,d)\n",
                    player.getUsername(), player.getCoins(), coins, player.getShards(), shards);
        } else {
            System.out.println("\n[!] Failed to grant currency.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.5 helper: Reset economy rates to default.
     */
    private static void adminResetEconomyRates(InputHandling input, AdminDataManager adminDataManager) {
        System.out.println("\n==================================================");
        System.out.println("        RESET ECONOMY RATES TO DEFAULTS");
        System.out.println("==================================================");
        String confirm = input.readLine("Reset all Crate drop tables and Gacha pull rates to defaults? (y/N): ");
        if (confirm.equalsIgnoreCase("y")) {
            if (adminDataManager.getCrateManager() != null) {
                adminDataManager.getCrateManager().resetAllCrateRatesToDefault();
            }
            if (adminDataManager.getGachaManager() != null) {
                adminDataManager.getGachaManager().resetAllGachaRatesToDefault();
            }
            System.out.println("\n[SUCCESS] Crate and Gacha probabilities restored to default values!");
        } else {
            System.out.println("\nOperation cancelled.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    /**
     * Requirement 1.5 helper: Economy circulation & analytics.
     */
    private static void adminEconomyOverview(InputHandling input, AdminDataManager adminDataManager) {
        int totalPlayers = adminDataManager.getTotalPlayersCount();
        long totalCoins = adminDataManager.getTotalEconomyCoins();
        long totalShards = adminDataManager.getTotalEconomyShards();
        long avgCoins = (totalPlayers > 0) ? (totalCoins / totalPlayers) : 0;
        long avgShards = (totalPlayers > 0) ? (totalShards / totalPlayers) : 0;

        System.out.println("\n==================================================");
        System.out.println("       ECONOMY CIRCULATION & CURRENCY ANALYTICS");
        System.out.println("==================================================");
        System.out.printf("  Total Coins in Economy     : %,d\n", totalCoins);
        System.out.printf("  Average Coins per Player   : %,d\n", avgCoins);
        System.out.printf("  Total Shards in Economy    : %,d\n", totalShards);
        System.out.printf("  Average Shards per Player  : %,d\n", avgShards);
        System.out.println("--------------------------------------------------");
        System.out.println("  Top Currency Holders:");
        LinkedList<PlayerAcc> topCoins = adminDataManager.getTopPlayersByCoins(3);
        for (int i = 0; i < topCoins.size(); i++) {
            PlayerAcc p = topCoins.get(i);
            System.out.printf("    #%d %-16s : %,d Coins | %,d Shards\n", (i + 1), p.getUsername(), p.getCoins(), p.getShards());
        }
        System.out.println("==================================================");
        input.readLine("\nPress Enter to return...");
    }

    private static String promptKeepCurrent(InputHandling input, String label, String currentVal) {
        String val = input.readLine(label + " [" + currentVal + "]: ");
        return val.isEmpty() ? currentVal : val;
    }

    private static int readInt(InputHandling input, String prompt, int defaultVal) {
        String s = input.readLine(prompt);
        if (s.isEmpty()) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Using " + defaultVal + ".");
            return defaultVal;
        }
    }

    /**
     * Submenu 4: Admin Accounts & Credentials
     */
    private static void adminAccountsMenu(InputHandling input, AdminDataManager adminDataManager, AdminAcc currentAdmin) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("             ADMIN CREDENTIAL SYSTEM");
            System.out.println("==================================================");
            System.out.println("1. View Administrator Accounts");
            System.out.println("2. Register New Administrator Account");
            System.out.println("0. Back to Admin Dashboard");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    System.out.println("\n==================================================");
                    System.out.println("             REGISTERED ADMINISTRATORS");
                    System.out.println("==================================================");
                    int idx = 1;
                    for (AdminAcc a : adminDataManager.getAdminList()) {
                        String currentTag = a.getUsername().equalsIgnoreCase(currentAdmin.getUsername()) ? " (YOU)" : "";
                        System.out.printf("  [%d] %-16s | Role: %s%s\n", idx++, a.getUsername(), a.getRole(), currentTag);
                    }
                    System.out.println("==================================================");
                    input.readLine("\nPress Enter to return...");
                    break;

                case "2":
                    adminRegisterAccount(input, adminDataManager);
                    break;

                case "0":
                    break;

                default:
                    System.out.println("Invalid option.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static void adminRegisterAccount(InputHandling input, AdminDataManager adminDataManager) {
        System.out.println("\n==================================================");
        System.out.println("          REGISTER NEW ADMINISTRATOR");
        System.out.println("==================================================");

        String username;
        while (true) {
            username = input.readLine("Enter new admin username (or '0' to cancel): ");
            if (username.equals("0")) return;
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
            } else if (username.contains(",")) {
                System.out.println("Username cannot contain commas.");
            } else if (adminDataManager.isAdminUsernameTaken(username)) {
                System.out.println("That admin username is already in use.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            password = input.readLine("Enter new admin password: ");
            if (password.isEmpty()) {
                System.out.println("Password cannot be empty.");
            } else if (password.contains(",")) {
                System.out.println("Password cannot contain commas.");
            } else {
                break;
            }
        }

        String role = input.readLine("Enter role / title [Default: Administrator]: ");
        if (role.isEmpty()) role = "Administrator";

        boolean success = adminDataManager.registerAdmin(username, password, role);
        if (success) {
            System.out.println("\n[SUCCESS] Administrator account '" + username + "' created successfully!");
        } else {
            System.out.println("\n[!] Failed to register administrator account.");
        }
        input.readLine("\nPress Enter to continue...");
    }

    public static void playerHomescreen(InputHandling input, PlayerDataManager playerDataManager,
                                        CrateManager crateManager, CharacterGachaManager gachaManager,
                                        PlayerAcc player) {
        String choice;

        do {
            System.out.println("\n==================================================");
            System.out.println("                 CRATEOPIA");
            System.out.println("==================================================");
            System.out.println();
            System.out.println("Welcome, " + player.getUsername());
            System.out.println();
            System.out.println("Coins: " + player.getCoins() + "          Shards: " + player.getShards());
            System.out.println("Heroes Collected: " + player.getHeroesCollected() + "  Items Owned: " + player.getItemsOwned());
            System.out.println();
            System.out.println("--------------------------------------------------");
            System.out.println("1. Open Crates");
            System.out.println("2. Characters & Equipment");
            System.out.println("3. Inventory");
            System.out.println("4. Shop & Trading");
            System.out.println("5. Achievements & Statistics");
            System.out.println("6. Account Settings");
            System.out.println("7. Save & Exit");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    openingCrates(input, playerDataManager, crateManager, player);
                    break;
                case "2":
                    charactersAndEquipmentMenu(input, playerDataManager, gachaManager, player);
                    break;
                case "3":
                    inventoryMenu(input, playerDataManager, player);
                    break;
                case "4":
                    shopAndTradingMenu(input, playerDataManager, player);
                    break;
                case "5":
                    achievementsAndStatisticsMenu(input, playerDataManager, player);
                    break;
                case "6":
                    // Account Settings
                    break;
                case "7":
                    System.out.print("\nSaving");
                    for (int i = 0; i < 5; i++) {
                        System.out.print(".");
                        try {
                            Thread.sleep(350);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    System.out.println();
                    playerDataManager.saveAllPlayersToCsv();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("Saved successfully! Returning to Login Menu...");
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }

            if (!choice.equals("7") && !choice.equals("1") && !choice.equals("2") && !choice.equals("3") && !choice.equals("4") && !choice.equals("5")) {
                input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("7"));
    }

    // =========================================================================
    // =========================================================================
    // Achievement Announcement Helper
    // =========================================================================

    public static void announceUnlockedAchievements(LinkedList<Achievement> newUnlocks) {
        if (newUnlocks == null || newUnlocks.isEmpty()) return;

        System.out.println("\n**************************************************");
        System.out.println("          ★ ACHIEVEMENT(S) UNLOCKED! ★");
        System.out.println("**************************************************");
        for (Achievement a : newUnlocks) {
            System.out.println("  [★] " + a.getTitle() + " (" + a.getCategory().getDisplayName() + ")");
            System.out.println("      " + a.getDescription());
            System.out.println("      Reward: " + a.getRewardsDisplay());
        }
        System.out.println("--------------------------------------------------");
        System.out.println("  Claim your rewards in Menu Option 5 (Achievements & Statistics)!");
        System.out.println("**************************************************\n");
    }

    // =========================================================================
    // Characters & Equipment Menu (2.2)
    // =========================================================================

    public static void charactersAndEquipmentMenu(InputHandling input, PlayerDataManager playerDataManager,
                                                   CharacterGachaManager gachaManager, PlayerAcc player) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("             CHARACTERS & EQUIPMENT");
            System.out.println("==================================================");
            System.out.println("  Manage your collected heroes and equip them with");
            System.out.println("  class-specific weapons, armors, and accessories.");
            System.out.println("--------------------------------------------------");
            System.out.println("  Heroes in Roster : " + player.getRoster().size());
            System.out.println("  Equipped Gear    : " + player.getEquippedItemsCount() + " items");
            System.out.println("  Shards Available : " + String.format("%,d", player.getShards()));
            System.out.println("--------------------------------------------------");
            System.out.println("1. View Character Roster & Equip Gear (Sort & Filter)");
            System.out.println("2. Manage Equipment & Quick Loadouts");
            System.out.println("3. Character Gacha Summon (Portal of Legends)");
            System.out.println("0. Back to Main Menu");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    viewRosterMenu(input, playerDataManager, player);
                    break;
                case "2":
                    manageEquipmentMenu(input, playerDataManager, player);
                    break;
                case "3":
                    characterGachaMenu(input, playerDataManager, gachaManager, player);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    // =========================================================================
    // Character Gacha Summon
    // =========================================================================

    private static void characterGachaMenu(InputHandling input, PlayerDataManager playerDataManager,
                                           CharacterGachaManager gachaManager, PlayerAcc player) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("            PORTAL OF LEGENDS (GACHA)");
            System.out.println("==================================================");
            System.out.println("  Summon legendary heroes across 7 classes!");
            System.out.println("  Classes : Warrior, Mage, Assassin, Tank, Ranger, Support, Brawler");
            System.out.println("  Rarities: 3★ to 6★ (Scaling stats & power)");
            System.out.println("--------------------------------------------------");
            System.out.println("  Current Shards: " + player.getShards());
            System.out.println();
            System.out.println("  Single Summon : 1 Shard  |  10x Multi-Summon: 10 Shards (Guaranteed 4★+)");
            System.out.println("--------------------------------------------------");
            System.out.println("1. Single Summon (1 Shard)");
            System.out.println("2. 10x Multi-Summon (10 Shards) [Guaranteed 4★+]");
            System.out.println("0. Back");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    performGacha(input, playerDataManager, gachaManager, player, false);
                    break;
                case "2":
                    performGacha(input, playerDataManager, gachaManager, player, true);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static void performGacha(InputHandling input, PlayerDataManager playerDataManager,
                                     CharacterGachaManager gachaManager, PlayerAcc player, boolean isMulti) {
        int cost = isMulti ? CharacterGachaManager.MULTI_PULL_SHARDS : CharacterGachaManager.SINGLE_PULL_SHARDS;
        if (player.getShards() < cost) {
            System.out.println("\n[!] Not enough shards! You need " + cost + " shards, but only have " + player.getShards() + ".");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.print("\n  Channeling Celestial Gacha Portal");
        for (int i = 0; i < 6; i++) {
            System.out.print(".");
            try { Thread.sleep(220); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        System.out.println();

        CharacterGachaManager.GachaResult result = isMulti ? gachaManager.drawTen(player) : gachaManager.drawSingle(player);

        if (!result.isSuccess()) {
            System.out.println("\n[!] " + result.getMessage());
            input.readLine("Press Enter to continue...");
            return;
        }

        // Track statistics: summons performed, shards spent, highest hero rarity
        int highestStars = 0;
        for (Hero h : result.getSummonedHeroes()) {
            if (h.getStars() > highestStars) highestStars = h.getStars();
        }
        for (CharacterGachaManager.GachaResult.DuplicateHeroEntry d : result.getDuplicateHeroes()) {
            if (d.getHero().getStars() > highestStars) highestStars = d.getHero().getStars();
        }
        player.getStats().recordSummon(isMulti ? 10 : 1, cost, highestStars);

        int dupeShardsGained = 0;
        for (CharacterGachaManager.GachaResult.DuplicateHeroEntry d : result.getDuplicateHeroes()) {
            dupeShardsGained += d.getShardsAwarded();
        }
        if (dupeShardsGained > 0) {
            player.getStats().recordShardsEarned(dupeShardsGained);
        }

        // Synchronize counters and check achievements
        player.syncCounters();
        LinkedList<Achievement> newUnlocks = playerDataManager.getAchievementManager().checkAndUnlockAchievements(player);

        // Auto-save shards spent & summoned heroes
        playerDataManager.saveAllPlayersToCsv();

        System.out.println("\n==================================================");
        System.out.println("              SUMMON RESULTS (" + (isMulti ? "10x" : "Single") + ")");
        System.out.println("==================================================");

        for (Hero h : result.getSummonedHeroes()) {
            System.out.println("  " + h.toString());
            System.out.printf("    Title: %-20s | Elemental Power: %s\n",
                    (h.getTitle().isEmpty() ? "None" : h.getTitle()), h.getElementalPowerDisplay());
            System.out.printf("    HP: %-4d | ATK: %-3d | DEF: %-3d | CRIT: %d%%\n",
                    h.getBaseHp(), h.getBaseAttack(), h.getBaseDefense(), h.getBaseCritChance());
            System.out.println("    \"" + h.getDescription() + "\"");
            System.out.println();
        }

        if (!result.getDuplicateHeroes().isEmpty()) {
            System.out.println("--------------------------------------------------");
            System.out.println("  [DUPLICATE HEROES CONVERTED TO SHARDS]");
            for (CharacterGachaManager.GachaResult.DuplicateHeroEntry d : result.getDuplicateHeroes()) {
                System.out.printf("   - %s -> Converted to +%d Shards\n",
                        d.getHero().getName() + " (" + d.getHero().getStarsDisplay() + ")",
                        d.getShardsAwarded());
            }
        }

        // Use PriorityQueue to announce highest tier summoned
        if (!result.getHighestRankPulls().isEmpty()) {
            Hero top = result.getHighestRankPulls().peek();
            System.out.println("--------------------------------------------------");
            System.out.println("  ★ Top Summon: " + top.getName() + " - \"" + top.getTitle() + "\" ("
                    + top.getStarsDisplay() + " " + top.getCharacterClass().getDisplayName() + " | " + top.getElementalPowerDisplay() + ")");
        }

        System.out.println("--------------------------------------------------");
        System.out.println("  Remaining Shards: " + player.getShards());
        System.out.println("==================================================");

        announceUnlockedAchievements(newUnlocks);

        input.readLine("\nPress Enter to continue...");
    }

    // =========================================================================
    // View Roster & Equipment (Sorting & Filtering)
    // =========================================================================

    private static void viewRosterMenu(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        LinkedList<Hero> rawRoster = player.getRoster();
        if (rawRoster.isEmpty()) {
            System.out.println("\n[!] Your hero roster is empty!");
            System.out.println("    Use Shards in the 'Portal of Legends' to summon your first hero.");
            input.readLine("\nPress Enter to return...");
            return;
        }

        // Active sorting and filtering state
        HeroSort currentSort = HeroSort.RARITY_DESC;
        CharacterClass filterClass = null;
        Integer filterStars = null;
        String filterElement = null;

        String choice;
        do {
            // Apply filtering then sorting
            LinkedList<Hero> displayList = HeroSort.filter(rawRoster, filterClass, filterStars, filterElement);
            displayList = HeroSort.sort(displayList, currentSort);

            String classBadge = (filterClass == null) ? "ALL" : filterClass.getDisplayName();
            String starBadge = (filterStars == null) ? "ALL" : (filterStars + "★");
            String elemBadge = (filterElement == null || filterElement.isEmpty()) ? "ALL" : filterElement;

            System.out.println("\n==================================================");
            System.out.println("                   HERO ROSTER");
            System.out.println("==================================================");
            System.out.println("  Filters : [Class: " + classBadge + " | Rarity: " + starBadge + " | Element: " + elemBadge + "]");
            System.out.println("  Sort By : [" + currentSort.getDisplayName() + "]");
            System.out.println("  Displaying " + displayList.size() + " of " + rawRoster.size() + " heroes");
            System.out.println("--------------------------------------------------");

            if (displayList.isEmpty()) {
                System.out.println("  No heroes match the active filter criteria.");
                System.out.println("  Type 'C' to clear all filters.");
            } else {
                int idx = 1;
                for (Hero h : displayList) {
                    System.out.printf("  [%d] %-32s | HP:%-4d ATK:%-3d DEF:%-3d CRIT:%d%%\n",
                            idx++, h.toString(), h.getTotalHp(), h.getTotalAttack(), h.getTotalDefense(), h.getTotalCritChance());

                    String weaponName = (h.getEquippedWeapon() != null) ? h.getEquippedWeapon().getName() : "None";
                    String helmName = (h.getEquippedArmor(Hero.SLOT_HELMET) != null) ? h.getEquippedArmor(Hero.SLOT_HELMET).getName() : "None";
                    String chestName = (h.getEquippedArmor(Hero.SLOT_CHEST) != null) ? h.getEquippedArmor(Hero.SLOT_CHEST).getName() : "None";
                    String legName = (h.getEquippedArmor(Hero.SLOT_LEGGINGS) != null) ? h.getEquippedArmor(Hero.SLOT_LEGGINGS).getName() : "None";
                    String accName = (h.getEquippedAccessory() != null) ? h.getEquippedAccessory().getName() : "None";
                    System.out.printf("      Gear: [W: %s | H: %s | C: %s | L: %s | Acc: %s]\n",
                            weaponName, helmName, chestName, legName, accName);
                    if (h.hasFullSetBonus()) {
                        System.out.println("      >>> Active Armor Set: " + h.getActiveSetName() + " Complete! <<<");
                    }
                }
            }

            System.out.println("--------------------------------------------------");
            System.out.println("Commands: [1-" + displayList.size() + "] Select Hero  |  [S] Change Sort  |  [F] Change Filter  |  [C] Clear Filters  |  [0] Return");
            System.out.println("==================================================");

            choice = input.readLine("Select: ");
            if (choice.equals("0")) break;

            if (choice.equalsIgnoreCase("S")) {
                currentSort = selectHeroSortOrder(input, currentSort);
            } else if (choice.equalsIgnoreCase("F")) {
                Object[] filters = selectHeroFilters(input, filterClass, filterStars, filterElement);
                filterClass = (CharacterClass) filters[0];
                filterStars = (Integer) filters[1];
                filterElement = (String) filters[2];
            } else if (choice.equalsIgnoreCase("C")) {
                filterClass = null;
                filterStars = null;
                filterElement = null;
                System.out.println("\n[✓] All roster filters cleared.");
            } else {
                try {
                    int heroIdx = Integer.parseInt(choice) - 1;
                    if (heroIdx >= 0 && heroIdx < displayList.size()) {
                        heroDetailAndEquipMenu(input, playerDataManager, player, displayList.get(heroIdx));
                    } else {
                        System.out.println("Invalid selection.");
                        input.readLine("Press Enter to continue...");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                    input.readLine("Press Enter to continue...");
                }
            }
        } while (!choice.equals("0"));
    }

    private static HeroSort selectHeroSortOrder(InputHandling input, HeroSort current) {
        System.out.println("\n==================================================");
        System.out.println("              HERO ROSTER SORTING");
        System.out.println("==================================================");
        HeroSort[] values = HeroSort.values();
        for (int i = 0; i < values.length; i++) {
            String marker = (values[i] == current) ? " [ACTIVE]" : "";
            System.out.printf("  [%d] %s%s\n", (i + 1), values[i].getDisplayName(), marker);
        }
        System.out.println("  [0] Keep Current");
        System.out.println("--------------------------------------------------");
        String opt = input.readLine("Select sort option: ");
        try {
            int idx = Integer.parseInt(opt) - 1;
            if (idx >= 0 && idx < values.length) {
                return values[idx];
            }
        } catch (NumberFormatException ignored) {}
        return current;
    }

    private static Object[] selectHeroFilters(InputHandling input, CharacterClass currentClass,
                                              Integer currentStars, String currentElem) {
        System.out.println("\n==================================================");
        System.out.println("              HERO ROSTER FILTERING");
        System.out.println("==================================================");
        System.out.println("1. Filter by Class");
        System.out.println("2. Filter by Star Rarity (3★ to 6★)");
        System.out.println("3. Filter by Elemental Power");
        System.out.println("4. Clear All Filters");
        System.out.println("0. Return");
        System.out.println("--------------------------------------------------");
        String opt = input.readLine("Select filter type: ");

        CharacterClass newClass = currentClass;
        Integer newStars = currentStars;
        String newElem = currentElem;

        if (opt.equals("1")) {
            System.out.println("\n--- [FILTER BY CLASS] ---");
            System.out.println("0. All Classes (Reset)");
            CharacterClass[] classes = CharacterClass.values();
            for (int i = 0; i < classes.length; i++) {
                System.out.printf("%d. %s\n", (i + 1), classes[i].getDisplayName());
            }
            String cOpt = input.readLine("Select class: ");
            if (cOpt.equals("0")) {
                newClass = null;
            } else {
                try {
                    int cIdx = Integer.parseInt(cOpt) - 1;
                    if (cIdx >= 0 && cIdx < classes.length) newClass = classes[cIdx];
                } catch (NumberFormatException ignored) {}
            }
        } else if (opt.equals("2")) {
            System.out.println("\n--- [FILTER BY STAR RARITY] ---");
            System.out.println("0. All Rarities (Reset)");
            System.out.println("3. 3★ Heroes");
            System.out.println("4. 4★ Heroes");
            System.out.println("5. 5★ Heroes");
            System.out.println("6. 6★ Heroes");
            String sOpt = input.readLine("Select star rating: ");
            if (sOpt.equals("0")) {
                newStars = null;
            } else {
                try {
                    int s = Integer.parseInt(sOpt);
                    if (s >= 3 && s <= 6) newStars = s;
                } catch (NumberFormatException ignored) {}
            }
        } else if (opt.equals("3")) {
            System.out.println("\n--- [FILTER BY ELEMENT] ---");
            System.out.println("0. All Elements (Reset)");
            characters.Element[] elements = characters.Element.values();
            for (int i = 0; i < elements.length; i++) {
                System.out.printf("%d. %s\n", (i + 1), elements[i].getDisplayName());
            }
            String eOpt = input.readLine("Select element: ");
            if (eOpt.equals("0")) {
                newElem = null;
            } else {
                try {
                    int eIdx = Integer.parseInt(eOpt) - 1;
                    if (eIdx >= 0 && eIdx < elements.length) newElem = elements[eIdx].getDisplayName();
                } catch (NumberFormatException ignored) {}
            }
        } else if (opt.equals("4")) {
            newClass = null;
            newStars = null;
            newElem = null;
        }

        return new Object[] { newClass, newStars, newElem };
    }

    // =========================================================================
    // Equipment Management Submenu (2.2)
    // =========================================================================

    private static void manageEquipmentMenu(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("           EQUIPMENT & LOADOUT MANAGEMENT");
            System.out.println("==================================================");
            System.out.println("  Manage gear across your entire hero roster.");
            System.out.println("--------------------------------------------------");
            System.out.println("  Total Items Owned : " + player.getItemsOwned());
            System.out.println("  Unequipped in Bag : " + player.getInventory().size());
            System.out.println("  Equipped on Heroes: " + player.getEquippedItemsCount());
            System.out.println("--------------------------------------------------");
            System.out.println("1. Select a Hero to Equip Gear");
            System.out.println("2. View All Equipped Gear Across All Heroes");
            System.out.println("3. Auto-Equip Best Gear on All Heroes");
            System.out.println("4. Unequip All Gear From a Hero");
            System.out.println("0. Back to Characters Menu");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    viewRosterMenu(input, playerDataManager, player);
                    break;
                case "2":
                    viewAllEquippedGear(input, player);
                    break;
                case "3":
                    autoEquipAllHeroes(input, playerDataManager, player);
                    break;
                case "4":
                    unequipAllHeroGearFlow(input, playerDataManager, player);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option.");
                    input.readLine("Press Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static void viewAllEquippedGear(InputHandling input, PlayerAcc player) {
        System.out.println("\n==================================================");
        System.out.println("        ALL EQUIPPED GEAR ACROSS HEROES");
        System.out.println("==================================================");
        if (player.getRoster().isEmpty()) {
            System.out.println("  No heroes in roster.");
        } else {
            boolean anyEquipped = false;
            for (Hero h : player.getRoster()) {
                String weaponName = (h.getEquippedWeapon() != null) ? h.getEquippedWeapon().getName() + " (" + h.getEquippedWeapon().getSummary().trim() + ")" : "Empty";
                String helmName = (h.getEquippedArmor(Hero.SLOT_HELMET) != null) ? h.getEquippedArmor(Hero.SLOT_HELMET).getName() + " (" + h.getEquippedArmor(Hero.SLOT_HELMET).getSummary().trim() + ")" : "Empty";
                String chestName = (h.getEquippedArmor(Hero.SLOT_CHEST) != null) ? h.getEquippedArmor(Hero.SLOT_CHEST).getName() + " (" + h.getEquippedArmor(Hero.SLOT_CHEST).getSummary().trim() + ")" : "Empty";
                String legName = (h.getEquippedArmor(Hero.SLOT_LEGGINGS) != null) ? h.getEquippedArmor(Hero.SLOT_LEGGINGS).getName() + " (" + h.getEquippedArmor(Hero.SLOT_LEGGINGS).getSummary().trim() + ")" : "Empty";
                String accName = (h.getEquippedAccessory() != null) ? h.getEquippedAccessory().getName() + " (" + h.getEquippedAccessory().getSummary().trim() + ")" : "Empty";

                boolean hasGear = (h.getEquippedWeapon() != null || h.getEquippedArmor(Hero.SLOT_HELMET) != null
                        || h.getEquippedArmor(Hero.SLOT_CHEST) != null || h.getEquippedArmor(Hero.SLOT_LEGGINGS) != null
                        || h.getEquippedAccessory() != null);

                if (hasGear) anyEquipped = true;

                System.out.println("  Hero: " + h.getName() + " (" + h.getCharacterClass().getDisplayName() + " " + h.getStarsDisplay() + ")");
                System.out.println("    Weapon    : " + weaponName);
                System.out.println("    Helmet    : " + helmName);
                System.out.println("    Chestplate: " + chestName);
                System.out.println("    Leggings  : " + legName);
                System.out.println("    Accessory : " + accName);
                if (h.hasFullSetBonus()) {
                    System.out.println("    >>> Set Bonus: " + h.getActiveSetName() + " Complete! <<<");
                }
                System.out.println("  ------------------------------------------------");
            }
            if (!anyEquipped) {
                System.out.println("  No gear is currently equipped on any heroes.");
            }
        }
        System.out.println("==================================================");
        input.readLine("\nPress Enter to return...");
    }

    private static void autoEquipAllHeroes(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        System.out.println("\n[!] Automatically equipping the best available gear onto all heroes...");
        int equippedCount = 0;
        for (Hero h : player.getRoster()) {
            equippedCount += autoEquipHero(player, h);
        }
        player.syncCounters();
        LinkedList<Achievement> newUnlocks = playerDataManager.getAchievementManager().checkAndUnlockAchievements(player);
        playerDataManager.saveAllPlayersToCsv();
        System.out.println("[SUCCESS] Auto-equip complete! " + equippedCount + " item(s) equipped across roster.");
        announceUnlockedAchievements(newUnlocks);
        input.readLine("Press Enter to continue...");
    }

    private static int autoEquipHero(PlayerAcc player, Hero hero) {
        int count = 0;
        // Weapon
        if (hero.getEquippedWeapon() == null) {
            Weapon best = null;
            for (Item item : player.getInventory()) {
                if (item instanceof Weapon && hero.canEquip(item)) {
                    Weapon w = (Weapon) item;
                    if (best == null || w.getAttack() > best.getAttack()) {
                        best = w;
                    }
                }
            }
            if (best != null) {
                player.removeItem(best);
                hero.equip(best);
                count++;
            }
        }
        // Helmet
        if (hero.getEquippedArmor(Hero.SLOT_HELMET) == null) {
            Armor best = null;
            for (Item item : player.getInventory()) {
                if (item instanceof Armor && hero.canEquip(item) && Hero.armorSlotFor((Armor) item) == Hero.SLOT_HELMET) {
                    Armor a = (Armor) item;
                    if (best == null || a.getDefense() > best.getDefense()) best = a;
                }
            }
            if (best != null) {
                player.removeItem(best);
                hero.equip(best);
                count++;
            }
        }
        // Chest
        if (hero.getEquippedArmor(Hero.SLOT_CHEST) == null) {
            Armor best = null;
            for (Item item : player.getInventory()) {
                if (item instanceof Armor && hero.canEquip(item) && Hero.armorSlotFor((Armor) item) == Hero.SLOT_CHEST) {
                    Armor a = (Armor) item;
                    if (best == null || a.getDefense() > best.getDefense()) best = a;
                }
            }
            if (best != null) {
                player.removeItem(best);
                hero.equip(best);
                count++;
            }
        }
        // Leggings
        if (hero.getEquippedArmor(Hero.SLOT_LEGGINGS) == null) {
            Armor best = null;
            for (Item item : player.getInventory()) {
                if (item instanceof Armor && hero.canEquip(item) && Hero.armorSlotFor((Armor) item) == Hero.SLOT_LEGGINGS) {
                    Armor a = (Armor) item;
                    if (best == null || a.getDefense() > best.getDefense()) best = a;
                }
            }
            if (best != null) {
                player.removeItem(best);
                hero.equip(best);
                count++;
            }
        }
        // Accessory
        if (hero.getEquippedAccessory() == null) {
            Accessory best = null;
            for (Item item : player.getInventory()) {
                if (item instanceof Accessory && hero.canEquip(item)) {
                    Accessory acc = (Accessory) item;
                    if (best == null || acc.getStatBonus() > best.getStatBonus()) best = acc;
                }
            }
            if (best != null) {
                player.removeItem(best);
                hero.equip(best);
                count++;
            }
        }
        return count;
    }

    private static void unequipAllHeroGearFlow(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        if (player.getRoster().isEmpty()) {
            System.out.println("\n[!] No heroes in roster.");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("              UNEQUIP ALL HERO GEAR");
        System.out.println("==================================================");
        int idx = 1;
        for (Hero h : player.getRoster()) {
            System.out.printf("  [%d] %s (%s)\n", idx++, h.getName(), h.getCharacterClass().getDisplayName());
        }
        System.out.println("  [0] Cancel");
        System.out.println("--------------------------------------------------");
        String choice = input.readLine("Select hero number: ");
        if (choice.equals("0")) return;

        try {
            int heroIdx = Integer.parseInt(choice) - 1;
            if (heroIdx >= 0 && heroIdx < player.getRoster().size()) {
                Hero hero = player.getRoster().get(heroIdx);
                int removed = 0;
                Item w = hero.unequip("weapon");
                if (w != null) { player.addItem(w); removed++; }
                Item h = hero.unequip("helmet");
                if (h != null) { player.addItem(h); removed++; }
                Item c = hero.unequip("chest");
                if (c != null) { player.addItem(c); removed++; }
                Item l = hero.unequip("leggings");
                if (l != null) { player.addItem(l); removed++; }
                Item acc = hero.unequip("accessory");
                if (acc != null) { player.addItem(acc); removed++; }

                player.syncCounters();
                playerDataManager.saveAllPlayersToCsv();
                System.out.println("\n[SUCCESS] Unequipped " + removed + " gear pieces from " + hero.getName() + ".");
            } else {
                System.out.println("Invalid hero selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
        input.readLine("Press Enter to continue...");
    }

    private static void heroDetailAndEquipMenu(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player, Hero hero) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("           HERO DETAILS & EQUIPMENT");
            System.out.println("==================================================");
            System.out.println("  Name            : " + hero.getName());
            System.out.println("  Title           : " + (hero.getTitle().isEmpty() ? "None" : hero.getTitle()));
            System.out.println("  Class           : " + hero.getCharacterClass().getDisplayName());
            System.out.println("  Star Level      : " + hero.getStarsDisplay());
            System.out.println("  Elemental Power : " + hero.getElementalPowerDisplay());
            System.out.println("  Description     : " + hero.getDescription());
            System.out.println("--------------------------------------------------");
            System.out.printf("  Base Stats  : HP: %-4d | ATK: %-3d | DEF: %-3d | CRIT: %d%%\n",
                    hero.getBaseHp(), hero.getBaseAttack(), hero.getBaseDefense(), hero.getBaseCritChance());
            System.out.printf("  Total Stats : HP: %-4d | ATK: %-3d | DEF: %-3d | CRIT: %d%%\n",
                    hero.getTotalHp(), hero.getTotalAttack(), hero.getTotalDefense(), hero.getTotalCritChance());
            System.out.println("--------------------------------------------------");
            System.out.println("  [Equipped Equipment]");
            System.out.println("  Weapon    : " + (hero.getEquippedWeapon() != null ? hero.getEquippedWeapon().getName() + " (" + hero.getEquippedWeapon().getSummary().trim() + ")" : "None"));
            System.out.println("  Helmet    : " + (hero.getEquippedArmor(Hero.SLOT_HELMET) != null ? hero.getEquippedArmor(Hero.SLOT_HELMET).getName() + " (" + hero.getEquippedArmor(Hero.SLOT_HELMET).getSummary().trim() + ")" : "None"));
            System.out.println("  Chestplate: " + (hero.getEquippedArmor(Hero.SLOT_CHEST) != null ? hero.getEquippedArmor(Hero.SLOT_CHEST).getName() + " (" + hero.getEquippedArmor(Hero.SLOT_CHEST).getSummary().trim() + ")" : "None"));
            System.out.println("  Leggings  : " + (hero.getEquippedArmor(Hero.SLOT_LEGGINGS) != null ? hero.getEquippedArmor(Hero.SLOT_LEGGINGS).getName() + " (" + hero.getEquippedArmor(Hero.SLOT_LEGGINGS).getSummary().trim() + ")" : "None"));
            System.out.println("  Accessory : " + (hero.getEquippedAccessory() != null ? hero.getEquippedAccessory().getName() + " (" + hero.getEquippedAccessory().getSummary().trim() + ")" : "None"));
            if (hero.hasFullSetBonus()) {
                ArmorSet activeSet = hero.getActiveArmorSet();
                String setDisplay = (activeSet != null) ? activeSet.getDisplayLabel() : "Set Bonus: " + hero.getActiveSetName() + " Complete";
                System.out.println("  >>> " + setDisplay + " <<<");
            }
            System.out.println("--------------------------------------------------");
            System.out.println("1. Equip / Change Weapon");
            System.out.println("2. Equip / Change Armor Piece");
            System.out.println("3. Equip / Change Accessory");
            System.out.println("4. Unequip an Item");
            System.out.println("5. Auto-Equip Best Gear for this Hero");
            System.out.println("0. Back to Roster");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    equipItemFlow(input, playerDataManager, player, hero, Weapon.class, "Weapon");
                    break;
                case "2":
                    equipItemFlow(input, playerDataManager, player, hero, Armor.class, "Armor");
                    break;
                case "3":
                    equipItemFlow(input, playerDataManager, player, hero, Accessory.class, "Accessory");
                    break;
                case "4":
                    unequipFlow(input, playerDataManager, player, hero);
                    break;
                case "5":
                    int eq = autoEquipHero(player, hero);
                    player.syncCounters();
                    LinkedList<Achievement> newUnlocks = playerDataManager.getAchievementManager().checkAndUnlockAchievements(player);
                    playerDataManager.saveAllPlayersToCsv();
                    System.out.println("\n[SUCCESS] Auto-equipped " + eq + " best items on " + hero.getName() + "!");
                    announceUnlockedAchievements(newUnlocks);
                    input.readLine("Press Enter to continue...");
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option.");
                    input.readLine("Press Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static <T extends Item> void equipItemFlow(InputHandling input, PlayerDataManager playerDataManager,
                                                       PlayerAcc player, Hero hero,
                                                       Class<T> itemClass, String slotName) {
        LinkedList<Item> matchingItems = new LinkedList<>();
        for (Item item : player.getInventory()) {
            if (itemClass.isInstance(item)) {
                matchingItems.add(item);
            }
        }

        if (matchingItems.isEmpty()) {
            System.out.println("\n[!] No " + slotName + " items in your inventory.");
            System.out.println("    Open crates in 'Open Crates' to find gear!");
            input.readLine("\nPress Enter to continue...");
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("       SELECT " + slotName.toUpperCase() + " FOR " + hero.getName().toUpperCase() + " (" + hero.getCharacterClass().getDisplayName() + ")");
        System.out.println("==================================================");
        int idx = 1;
        for (Item item : matchingItems) {
            boolean canEquip = hero.canEquip(item);
            String status = canEquip ? "[EQUIPPABLE]" : "[LOCKED: Requires " + item.getEquippableClassesDisplay() + "]";
            System.out.printf("  [%d] %-30s %s\n", idx++, item.toString(), status);
            System.out.println("      " + item.getSummary().trim());
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Enter item number to equip (or 0 to cancel):");

        String choice = input.readLine("Select: ");
        if (choice.equals("0")) return;

        try {
            int selectedIdx = Integer.parseInt(choice) - 1;
            if (selectedIdx >= 0 && selectedIdx < matchingItems.size()) {
                Item chosenItem = matchingItems.get(selectedIdx);

                if (!hero.canEquip(chosenItem)) {
                    System.out.println("\n[ERROR - STRICT CLASS RESTRICTION]");
                    System.out.println("  " + chosenItem.getName() + " cannot be equipped by " + hero.getCharacterClass().getDisplayName() + "!");
                    System.out.println("  Allowed classes: " + chosenItem.getEquippableClassesDisplay());
                    input.readLine("\nPress Enter to continue...");
                    return;
                }

                // Remove newly equipped item from inventory
                player.removeItem(chosenItem);

                // Equip and return old item if swapped
                Item oldItem = hero.equip(chosenItem);
                if (oldItem != null) {
                    player.addItem(oldItem);
                    System.out.println("\n[SWAP] Returned previous item to inventory: " + oldItem.getName());
                }

                // Record statistics and check achievements
                player.getStats().recordEquipmentEquipped();
                player.syncCounters();
                LinkedList<Achievement> newUnlocks = playerDataManager.getAchievementManager().checkAndUnlockAchievements(player);

                // Auto-save equipped gear & inventory state
                playerDataManager.saveAllPlayersToCsv();

                System.out.println("\n[SUCCESS] Successfully equipped " + chosenItem.getName() + " on " + hero.getName() + "!");
                announceUnlockedAchievements(newUnlocks);
                input.readLine("Press Enter to continue...");
            } else {
                System.out.println("Invalid selection.");
                input.readLine("Press Enter to continue...");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            input.readLine("Press Enter to continue...");
        }
    }

    private static void unequipFlow(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player, Hero hero) {
        System.out.println("\n==================================================");
        System.out.println("                   UNEQUIP GEAR");
        System.out.println("==================================================");
        System.out.println("1. Weapon    : " + (hero.getEquippedWeapon() != null ? hero.getEquippedWeapon().getName() : "(Empty)"));
        System.out.println("2. Helmet    : " + (hero.getEquippedArmor(Hero.SLOT_HELMET) != null ? hero.getEquippedArmor(Hero.SLOT_HELMET).getName() : "(Empty)"));
        System.out.println("3. Chestplate: " + (hero.getEquippedArmor(Hero.SLOT_CHEST) != null ? hero.getEquippedArmor(Hero.SLOT_CHEST).getName() : "(Empty)"));
        System.out.println("4. Leggings  : " + (hero.getEquippedArmor(Hero.SLOT_LEGGINGS) != null ? hero.getEquippedArmor(Hero.SLOT_LEGGINGS).getName() : "(Empty)"));
        System.out.println("5. Accessory : " + (hero.getEquippedAccessory() != null ? hero.getEquippedAccessory().getName() : "(Empty)"));
        System.out.println("0. Cancel");
        System.out.println("--------------------------------------------------");

        String choice = input.readLine("Select slot to unequip: ");
        String slot = null;
        if (choice.equals("1")) slot = "weapon";
        else if (choice.equals("2")) slot = "helmet";
        else if (choice.equals("3")) slot = "chest";
        else if (choice.equals("4")) slot = "leggings";
        else if (choice.equals("5")) slot = "accessory";
        else return;

        Item unequipped = hero.unequip(slot);
        if (unequipped != null) {
            player.addItem(unequipped);
            player.syncCounters();

            // Auto-save unequipped gear & inventory state
            playerDataManager.saveAllPlayersToCsv();

            System.out.println("\n[SUCCESS] Unequipped " + unequipped.getName() + " and returned it to your inventory.");
        } else {
            System.out.println("\n[!] That slot is already empty.");
        }
        input.readLine("Press Enter to continue...");
    }

    // =========================================================================
    // Inventory Menu (2.3)
    // =========================================================================

    public static void inventoryMenu(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        // View modes: 1=Bag Only (Unequipped), 2=All Items (Bag+Equipped), 3=Equipped Only
        int viewMode = 1;
        String typeFilter = "ALL"; // ALL, WEAPON, ARMOR, ACCESSORY
        InventorySort currentSort = InventorySort.TYPE;

        String choice;
        do {
            // Build the base item list based on view mode
            LinkedList<ItemDisplayEntry> itemsToDisplay = new LinkedList<>();

            if (viewMode == 1 || viewMode == 2) {
                // Add unequipped items from bag
                for (Item item : player.getInventory()) {
                    itemsToDisplay.add(new ItemDisplayEntry(item, null));
                }
            }

            if (viewMode == 2 || viewMode == 3) {
                // Add equipped items from heroes
                for (Hero h : player.getRoster()) {
                    if (h.getEquippedWeapon() != null) {
                        itemsToDisplay.add(new ItemDisplayEntry(h.getEquippedWeapon(), h));
                    }
                    for (Armor a : h.getEquippedArmors()) {
                        if (a != null) {
                            itemsToDisplay.add(new ItemDisplayEntry(a, h));
                        }
                    }
                    if (h.getEquippedAccessory() != null) {
                        itemsToDisplay.add(new ItemDisplayEntry(h.getEquippedAccessory(), h));
                    }
                }
            }

            // Apply type filtering
            LinkedList<ItemDisplayEntry> filtered = new LinkedList<>();
            for (ItemDisplayEntry entry : itemsToDisplay) {
                if (matchesTypeFilter(entry.item, typeFilter)) {
                    filtered.add(entry);
                }
            }

            // Apply sorting
            sortItemEntries(filtered, currentSort);

            String modeLabel = (viewMode == 1) ? "Bag Only (Unequipped)" : (viewMode == 2) ? "All Items (Bag + Equipped)" : "Equipped Only";

            System.out.println("\n==================================================");
            System.out.println("                 PLAYER INVENTORY");
            System.out.println("==================================================");
            System.out.println("  Display Mode : [" + modeLabel + "]");
            System.out.println("  Type Filter  : [" + typeFilter.toUpperCase() + "]");
            System.out.println("  Sort Strategy: [" + currentSort.getDisplayName() + "]");
            System.out.println("  Showing " + filtered.size() + " items (Total Owned: " + player.getItemsOwned() + " | In Bag: " + player.getInventory().size() + " | Equipped: " + player.getEquippedItemsCount() + ")");
            System.out.println("--------------------------------------------------");

            if (filtered.isEmpty()) {
                System.out.println("  No items match the current view and filter.");
            } else {
                int idx = 1;
                for (ItemDisplayEntry entry : filtered) {
                    Item item = entry.item;
                    String statusBadge = entry.isEquipped()
                            ? "[EQUIPPED on " + entry.equippedHero.getName() + "]"
                            : "[BAG]";
                    System.out.printf("  [%d] %-34s %s\n", idx++, item.toString(), statusBadge);
                    System.out.println("      " + item.getSummary().trim());
                }
            }

            System.out.println("--------------------------------------------------");
            System.out.println("Commands: [1-" + filtered.size() + "] Inspect Item Details & Equip/Unequip");
            System.out.println("          [V] Switch View Mode  |  [F] Filter by Item Type  |  [S] Sort Items  |  [0] Return");
            System.out.println("==================================================");

            choice = input.readLine("Select: ");
            if (choice.equals("0")) break;

            if (choice.equalsIgnoreCase("V")) {
                viewMode = selectInventoryViewMode(input, viewMode);
            } else if (choice.equalsIgnoreCase("F")) {
                typeFilter = selectInventoryTypeFilter(input, typeFilter);
            } else if (choice.equalsIgnoreCase("S")) {
                currentSort = selectInventorySortStrategy(input, currentSort);
            } else {
                try {
                    int itemIdx = Integer.parseInt(choice) - 1;
                    if (itemIdx >= 0 && itemIdx < filtered.size()) {
                        ItemDisplayEntry selected = filtered.get(itemIdx);
                        itemDetailsView(input, playerDataManager, player, selected.item, selected.equippedHero);
                    } else {
                        System.out.println("Invalid item number.");
                        input.readLine("Press Enter to continue...");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                    input.readLine("Press Enter to continue...");
                }
            }
        } while (!choice.equals("0"));
    }

    private static class ItemDisplayEntry {
        final Item item;
        final Hero equippedHero; // null if in bag

        ItemDisplayEntry(Item item, Hero equippedHero) {
            this.item = item;
            this.equippedHero = equippedHero;
        }

        boolean isEquipped() {
            return equippedHero != null;
        }
    }

    private static boolean matchesTypeFilter(Item item, String filter) {
        if (filter == null || filter.equalsIgnoreCase("ALL")) return true;
        String f = filter.toLowerCase().trim();
        if (f.equals("weapon") && item instanceof Weapon) return true;
        if (f.equals("armor") && item instanceof Armor) return true;
        if (f.equals("accessory") && item instanceof Accessory) return true;
        return item.getType().toLowerCase().contains(f);
    }

    private static void sortItemEntries(LinkedList<ItemDisplayEntry> list, InventorySort sort) {
        if (sort == null || sort == InventorySort.RECENT || list == null || list.size() <= 1) return;
        java.util.Comparator<Item> itemComp = InventorySort.getComparator(sort);
        LinkedList<ItemDisplayEntry> sorted = TreeSort.sort(list, (a, b) -> itemComp.compare(a.item, b.item));
        list.clear();
        list.addAll(sorted);
    }

    private static int selectInventoryViewMode(InputHandling input, int current) {
        System.out.println("\n--- [SWITCH INVENTORY VIEW MODE] ---");
        System.out.println("1. Bag Only (Unequipped items only) [Default]");
        System.out.println("2. All Items (Unequipped Bag + Hero Equipped Gear)");
        System.out.println("3. Equipped Only (Items currently equipped on heroes)");
        System.out.println("0. Keep Current (" + current + ")");
        String opt = input.readLine("Select mode: ");
        if (opt.equals("1")) return 1;
        if (opt.equals("2")) return 2;
        if (opt.equals("3")) return 3;
        return current;
    }

    private static String selectInventoryTypeFilter(InputHandling input, String current) {
        System.out.println("\n--- [FILTER BY ITEM TYPE] ---");
        System.out.println("1. All Items");
        System.out.println("2. Weapons Only");
        System.out.println("3. Armor Only (Helmets, Chestplates, Leggings)");
        System.out.println("4. Accessories Only");
        System.out.println("0. Keep Current (" + current + ")");
        String opt = input.readLine("Select filter: ");
        if (opt.equals("1")) return "ALL";
        if (opt.equals("2")) return "WEAPON";
        if (opt.equals("3")) return "ARMOR";
        if (opt.equals("4")) return "ACCESSORY";
        return current;
    }

    private static InventorySort selectInventorySortStrategy(InputHandling input, InventorySort current) {
        System.out.println("\n--- [INVENTORY SORTING STRATEGY] ---");
        InventorySort[] values = InventorySort.values();
        for (int i = 0; i < values.length; i++) {
            String marker = (values[i] == current) ? " [ACTIVE]" : "";
            System.out.printf("%d. %s%s\n", (i + 1), values[i].getDisplayName(), marker);
        }
        System.out.println("0. Keep Current");
        String opt = input.readLine("Select sort: ");
        try {
            int idx = Integer.parseInt(opt) - 1;
            if (idx >= 0 && idx < values.length) return values[idx];
        } catch (NumberFormatException ignored) {}
        return current;
    }

    // =========================================================================
    // Item Details View (2.3)
    // =========================================================================

    private static void itemDetailsView(InputHandling input, PlayerDataManager playerDataManager,
                                       PlayerAcc player, Item item, Hero equippedOnHero) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("                   ITEM DETAILS");
            System.out.println("==================================================");
            System.out.println("  Name             : " + item.getName());
            System.out.println("  ID               : " + item.getId());
            System.out.println("  Rarity           : " + item.getRarity());
            System.out.println("  Item Type        : " + item.getType());
            System.out.println("  Equippable By    : " + item.getEquippableClassesDisplay());
            System.out.println("  Status           : " + (equippedOnHero != null ? "[EQUIPPED on " + equippedOnHero.getName() + "]" : "[IN BAG - Unequipped]"));
            System.out.println("--------------------------------------------------");

            if (item instanceof Weapon) {
                Weapon w = (Weapon) item;
                System.out.println("  [WEAPON STATISTICS]");
                System.out.printf("  Attack Bonus     : +%d ATK\n", w.getAttack());
                System.out.printf("  Critical Chance  : +%d%% CRIT\n", w.getCritChance());
            } else if (item instanceof Armor) {
                Armor a = (Armor) item;
                System.out.println("  [ARMOR STATISTICS]");
                System.out.printf("  Defense Bonus    : +%d DEF\n", a.getDefense());
                System.out.printf("  Health Bonus     : +%d Flat HP\n", a.getHealthBonus());
                System.out.printf("  Health % Bonus   : +%d%% Max HP\n", a.getHealthPercentBonus());
                System.out.println("  Armor Set Name   : " + a.getSetName());
                if (a.hasSetName()) {
                    System.out.println("  Set Requirement  : 3 Matching Pieces (" + a.getSetName() + ")");
                }
            } else if (item instanceof Accessory) {
                Accessory acc = (Accessory) item;
                System.out.println("  [ACCESSORY STATISTICS]");
                System.out.printf("  Stat Boost       : +%d to %s\n", acc.getStatBonus(), acc.getBonusType().toUpperCase());
            }

            System.out.println("--------------------------------------------------");
            System.out.println("  Description      : \"" + item.getDescription() + "\"");
            System.out.println("--------------------------------------------------");

            if (equippedOnHero == null) {
                System.out.println("1. Equip this Item to a Hero");
            } else {
                System.out.println("1. Unequip this Item from " + equippedOnHero.getName());
            }
            System.out.println("0. Return to Inventory");
            System.out.println("==================================================");

            choice = input.readLine("Select action: ");

            if (choice.equals("1")) {
                if (equippedOnHero == null) {
                    // Equip to hero flow
                    LinkedList<Hero> eligibleHeroes = new LinkedList<>();
                    for (Hero h : player.getRoster()) {
                        if (h.canEquip(item)) {
                            eligibleHeroes.add(h);
                        }
                    }

                    if (eligibleHeroes.isEmpty()) {
                        System.out.println("\n[!] No heroes in your roster can equip this item.");
                        System.out.println("    Allowed classes: " + item.getEquippableClassesDisplay());
                        input.readLine("Press Enter to continue...");
                    } else {
                        System.out.println("\n--- [SELECT HERO TO EQUIP " + item.getName() + "] ---");
                        int hIdx = 1;
                        for (Hero h : eligibleHeroes) {
                            System.out.printf("  [%d] %s (%s, %s)\n", hIdx++, h.getName(),
                                    h.getCharacterClass().getDisplayName(), h.getStarsDisplay());
                        }
                        System.out.println("  [0] Cancel");
                        String hChoice = input.readLine("Select hero: ");
                        if (!hChoice.equals("0")) {
                            try {
                                int selectedHeroIdx = Integer.parseInt(hChoice) - 1;
                                if (selectedHeroIdx >= 0 && selectedHeroIdx < eligibleHeroes.size()) {
                                    Hero targetHero = eligibleHeroes.get(selectedHeroIdx);

                                    player.removeItem(item);
                                    Item oldItem = targetHero.equip(item);
                                    if (oldItem != null) {
                                        player.addItem(oldItem);
                                        System.out.println("[SWAP] Replaced " + oldItem.getName() + " and returned it to your bag.");
                                    }

                                    player.getStats().recordEquipmentEquipped();
                                    player.syncCounters();
                                    LinkedList<Achievement> newUnlocks = playerDataManager.getAchievementManager().checkAndUnlockAchievements(player);
                                    playerDataManager.saveAllPlayersToCsv();

                                    System.out.println("\n[SUCCESS] Successfully equipped " + item.getName() + " onto " + targetHero.getName() + "!");
                                    announceUnlockedAchievements(newUnlocks);
                                    input.readLine("Press Enter to continue...");
                                    return; // Return to inventory
                                } else {
                                    System.out.println("Invalid selection.");
                                    input.readLine("Press Enter to continue...");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid number.");
                                input.readLine("Press Enter to continue...");
                            }
                        }
                    }
                } else {
                    // Unequip flow
                    String slot = null;
                    if (item instanceof Weapon) slot = "weapon";
                    else if (item instanceof Armor) {
                        int s = Hero.armorSlotFor((Armor) item);
                        if (s == Hero.SLOT_HELMET) slot = "helmet";
                        else if (s == Hero.SLOT_CHEST) slot = "chest";
                        else if (s == Hero.SLOT_LEGGINGS) slot = "leggings";
                    } else if (item instanceof Accessory) slot = "accessory";

                    if (slot != null) {
                        Item unequipped = equippedOnHero.unequip(slot);
                        if (unequipped != null) {
                            player.addItem(unequipped);
                            player.syncCounters();
                            playerDataManager.saveAllPlayersToCsv();
                            System.out.println("\n[SUCCESS] Unequipped " + unequipped.getName() + " from " + equippedOnHero.getName() + " and returned to bag.");
                            input.readLine("Press Enter to continue...");
                            return;
                        }
                    }
                }
            }
        } while (!choice.equals("0"));
    }

    // =========================================================================
    // Shop & Trading Menu (2.4)
    // =========================================================================

    public static void shopAndTradingMenu(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("                 SHOP & TRADING");
            System.out.println("==================================================");
            System.out.println("  Convert your hard-earned coins into shards for");
            System.out.println("  summoning legendary heroes at the Gacha Portal!");
            System.out.println("--------------------------------------------------");
            System.out.println("  Coins: " + String.format("%,d", player.getCoins()) + "          Shards: " + String.format("%,d", player.getShards()));
            System.out.println("--------------------------------------------------");
            System.out.println("1. Trade 2,000 Coins  -> 1 Shard");
            System.out.println("2. Trade 10,000 Coins -> 5 Shards");
            System.out.println("3. Trade 20,000 Coins -> 10 Shards");
            System.out.println("4. Custom Shard Purchase (2,000 Coins per Shard)");
            System.out.println("0. Back to Main Menu");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    convertCoinsToShards(input, playerDataManager, player, 2000, 1);
                    break;
                case "2":
                    convertCoinsToShards(input, playerDataManager, player, 10000, 5);
                    break;
                case "3":
                    convertCoinsToShards(input, playerDataManager, player, 20000, 10);
                    break;
                case "4":
                    convertCoinsToShardsCustom(input, playerDataManager, player);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static void convertCoinsToShardsCustom(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        System.out.println("\n--- [CUSTOM SHARD PURCHASE] ---");
        System.out.println("  Conversion Rate: 2,000 Coins = 1 Shard");
        System.out.println("  Your Balance   : " + String.format("%,d", player.getCoins()) + " Coins");
        int maxShardsAffordable = player.getCoins() / 2000;
        System.out.println("  Maximum shards affordable: " + String.format("%,d", maxShardsAffordable));
        System.out.println("--------------------------------------------------");
        String opt = input.readLine("Enter number of shards to purchase (or 0 to cancel): ");
        if (opt.equals("0") || opt.trim().isEmpty()) return;

        try {
            int desiredShards = Integer.parseInt(opt);
            if (desiredShards <= 0) {
                System.out.println("Amount must be greater than 0.");
                input.readLine("Press Enter to continue...");
                return;
            }
            int coinCost = desiredShards * 2000;
            convertCoinsToShards(input, playerDataManager, player, coinCost, desiredShards);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            input.readLine("Press Enter to continue...");
        }
    }

    private static void convertCoinsToShards(InputHandling input, PlayerDataManager playerDataManager,
                                             PlayerAcc player, int coinCost, int shardsGained) {
        if (player.getCoins() < coinCost) {
            System.out.println("\n[!] Not enough coins! You need " + String.format("%,d", coinCost) +
                    " coins, but only have " + String.format("%,d", player.getCoins()) + ".");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.print("\n  Converting " + String.format("%,d", coinCost) + " coins to " + shardsGained + " shard" + (shardsGained > 1 ? "s" : ""));
        for (int i = 0; i < 5; i++) {
            System.out.print(".");
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        System.out.println();

        player.setCoins(player.getCoins() - coinCost);
        player.setShards(player.getShards() + shardsGained);

        // Record statistics
        player.getStats().recordCoinsSpent(coinCost);
        player.getStats().recordShardsEarned(shardsGained);

        // Check achievements and save
        LinkedList<Achievement> newUnlocks = playerDataManager.getAchievementManager().checkAndUnlockAchievements(player);
        playerDataManager.saveAllPlayersToCsv();

        System.out.println("\n==================================================");
        System.out.println("              CONVERSION SUCCESSFUL");
        System.out.println("==================================================");
        System.out.println("  Coins spent   : -" + String.format("%,d", coinCost));
        System.out.println("  Shards gained : +" + shardsGained);
        System.out.println("--------------------------------------------------");
        System.out.println("  New Balance   -> Coins: " + String.format("%,d", player.getCoins()) +
                "  |  Shards: " + String.format("%,d", player.getShards()));
        System.out.println("==================================================");

        announceUnlockedAchievements(newUnlocks);

        input.readLine("\nPress Enter to continue...");
    }

    // =========================================================================
    // Open Crates (2.5 Statistics tracking hook)
    // =========================================================================

    public static void openingCrates(InputHandling input, PlayerDataManager playerDataManager,
                                      CrateManager crateManager, PlayerAcc player) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("                  OPEN CRATES");
            System.out.println("==================================================");
            System.out.println("  Test your luck! Opening crates is free and");
            System.out.println("  unlimited. Earn coins, shards, and gear!");
            System.out.println("--------------------------------------------------");
            System.out.println("  Coins: " + String.format("%,d", player.getCoins()) + "    Shards: " + String.format("%,d", player.getShards()));
            System.out.println();
            System.out.println("  Crate Tier Odds:");
            System.out.println("  [Wooden Crate]   - Chance: 75%  [Shard Bonus: 0.5%]");
            System.out.println("  [Silver Crate]   - Chance: 18%  [Shard Bonus: 2%]");
            System.out.println("  [Gold Crate]     - Chance: 6%   [Shard Bonus: 5%]");
            System.out.println("  [Diamond Crate]  - Chance: 1%   [Shard Bonus: 12%]");
            System.out.println();
            System.out.println("--------------------------------------------------");
            System.out.println("1. Open a Crate");
            System.out.println("0. Back");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    performCrateOpen(input, playerDataManager, crateManager, player);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static void performCrateOpen(InputHandling input, PlayerDataManager playerDataManager,
                                         CrateManager crateManager, PlayerAcc player) {
        System.out.print("\n  Rolling crate tier");
        for (int i = 0; i < 6; i++) {
            System.out.print(".");
            try { Thread.sleep(220); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        System.out.println();

        String tier = crateManager.rollCrateTier();

        System.out.println();
        System.out.println("  =============================================");
        System.out.println("  [ " + tier.toUpperCase() + " OPENED ]");
        System.out.println("  =============================================");

        CrateManager.CrateResult result = crateManager.openCrate(tier, player);

        // Record crate statistics
        player.getStats().recordCrateOpened(tier, result.getCoinsEarned(), result.getShardsEarned(), result.getItems().size());
        player.syncCounters();

        // Check achievements
        LinkedList<Achievement> newUnlocks = playerDataManager.getAchievementManager().checkAndUnlockAchievements(player);

        // Auto-save earned coins, shards, and inventory items
        playerDataManager.saveAllPlayersToCsv();

        System.out.println();
        System.out.println("  [REWARDS]");
        System.out.println("  Coins earned   : +" + result.getCoinsEarned());
        if (result.hasShards()) {
            System.out.println("  Shards earned  : +" + result.getShardsEarned());
        }
        if (!result.getItems().isEmpty()) {
            System.out.println("  Items obtained :");
            for (Item item : result.getItems()) {
                System.out.println("   - " + item.toString());
                System.out.println("     " + item.getSummary().trim());
            }
        }
        System.out.println();
        System.out.println("  New Balance -> Coins: " + String.format("%,d", player.getCoins()) + "  |  Shards: " + String.format("%,d", player.getShards()));
        System.out.println("  Items in Bag: " + player.getInventory().size() + "  |  Total Items Owned: " + player.getItemsOwned());
        System.out.println();

        announceUnlockedAchievements(newUnlocks);

        System.out.println("  Returning to Open Crates...");
    }

    // =========================================================================
    // Achievements & Statistics Menu (2.5 & Player Option 5)
    // =========================================================================

    public static void achievementsAndStatisticsMenu(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        // Automatically check for retroactive achievements upon viewing
        LinkedList<Achievement> newUnlocks = playerDataManager.getAchievementManager().checkAndUnlockAchievements(player);
        if (!newUnlocks.isEmpty()) {
            announceUnlockedAchievements(newUnlocks);
        }

        String choice;
        do {
            AchievementManager am = playerDataManager.getAchievementManager();
            int unlockedCount = am.getUnlockedCount(player);
            int totalCount = am.getMasterTemplates().size();
            int unclaimed = am.getUnclaimedCount(player);

            System.out.println("\n==================================================");
            System.out.println("            ACHIEVEMENTS & STATISTICS");
            System.out.println("==================================================");
            System.out.println("  Player: " + player.getUsername());
            System.out.println("  Achievements Completed: " + unlockedCount + " / " + totalCount + " (" + (totalCount > 0 ? (unlockedCount * 100 / totalCount) : 0) + "%)");
            if (unclaimed > 0) {
                System.out.println("  ★ Pending Rewards Available to Claim: " + unclaimed + " ★");
            }
            System.out.println("--------------------------------------------------");
            System.out.println("1. Player Statistics Dashboard");
            System.out.println("2. Achievements List & Progress");
            System.out.println("3. Claim Unlocked Achievement Rewards (" + unclaimed + " available)");
            System.out.println("0. Back to Main Menu");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    displayStatisticsDashboard(input, player);
                    break;
                case "2":
                    displayAchievementsList(input, playerDataManager, player);
                    break;
                case "3":
                    claimAchievementRewardsFlow(input, playerDataManager, player);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static void displayStatisticsDashboard(InputHandling input, PlayerAcc player) {
        accounts.PlayerStats st = player.getStats();

        System.out.println("\n==========================================================================");
        System.out.println("                        PLAYER STATISTICS DASHBOARD");
        System.out.println("==========================================================================");
        System.out.println("  Player Account       : " + player.getUsername() + " (" + (player.isBanned() ? "[BANNED]" : "[ACTIVE]") + ")");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("  [CRATES & REWARDS]");
        System.out.printf("  Total Crates Opened  : %,d\n", st.getCratesOpened());
        System.out.printf("    • Wooden Crates    : %,d\n", st.getWoodenCrates());
        System.out.printf("    • Silver Crates    : %,d\n", st.getSilverCrates());
        System.out.printf("    • Gold Crates      : %,d\n", st.getGoldCrates());
        System.out.printf("    • Diamond Crates   : %,d\n", st.getDiamondCrates());
        System.out.println();
        System.out.println("  [HEROES & SUMMONS]");
        System.out.printf("  Total Summons Done   : %,d\n", st.getSummonsPerformed());
        System.out.printf("  Heroes in Roster     : %d\n", player.getRoster().size());
        System.out.printf("  Highest Star Hero    : %d★\n", Math.max(st.getHighestHeroRarity(), getRosterHighestStar(player)));
        System.out.println();
        System.out.println("  [INVENTORY & EQUIPMENT]");
        System.out.printf("  Total Items Obtained : %,d\n", st.getItemsObtained());
        System.out.printf("  Total Items Owned    : %,d (Bag: %d | Equipped: %d)\n",
                player.getItemsOwned(), player.getInventory().size(), player.getEquippedItemsCount());
        System.out.printf("  Times Gear Equipped  : %,d\n", st.getEquipmentEquipped());
        System.out.println();
        System.out.println("  [ECONOMY & CURRENCY]");
        System.out.printf("  Current Coins        : %,d Coins\n", player.getCoins());
        System.out.printf("  Total Coins Earned   : %,d Coins\n", st.getCoinsEarned());
        System.out.printf("  Total Coins Spent    : %,d Coins\n", st.getCoinsSpent());
        System.out.printf("  Current Shards       : %,d Shards\n", player.getShards());
        System.out.printf("  Total Shards Earned  : %,d Shards\n", st.getShardsEarned());
        System.out.printf("  Total Shards Spent   : %,d Shards\n", st.getShardsSpent());
        System.out.println("==========================================================================");
        input.readLine("\nPress Enter to return...");
    }

    private static int getRosterHighestStar(PlayerAcc player) {
        int highest = 0;
        for (Hero h : player.getRoster()) {
            if (h.getStars() > highest) highest = h.getStars();
        }
        return highest;
    }

    private static void displayAchievementsList(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        AchievementManager am = playerDataManager.getAchievementManager();
        LinkedList<Achievement> achievements = am.getAchievementsForPlayer(player);

        String choice;
        do {
            System.out.println("\n==========================================================================");
            System.out.println("                      ACHIEVEMENTS LIST & PROGRESS");
            System.out.println("==========================================================================");
            int idx = 1;
            for (Achievement ach : achievements) {
                int progress = ach.getProgress(player);
                int target = ach.getTargetCount();
                String progStr = "[" + progress + " / " + target + "]";

                String status;
                if (ach.isRewardClaimed()) {
                    status = "[CLAIMED ✓]";
                } else if (ach.isUnlocked()) {
                    status = "[UNLOCKED ★ CLAIMABLE]";
                } else {
                    status = "[IN PROGRESS]";
                }

                System.out.printf("  [%2d] %-25s %-16s %-22s\n",
                        idx++, ach.getTitle(), status, progStr);
                System.out.println("       Category: " + ach.getCategory().getDisplayName() + " | Reward: " + ach.getRewardsDisplay());
                System.out.println("       Condition: " + ach.getDescription());
                if (ach.isUnlocked() && !ach.getUnlockedAt().isEmpty()) {
                    System.out.println("       Unlocked at: " + ach.getUnlockedAt());
                }
                System.out.println("  ------------------------------------------------------------------------");
            }

            int unclaimed = am.getUnclaimedCount(player);
            System.out.println("  Total: " + am.getUnlockedCount(player) + " / " + achievements.size() + " Unlocked"
                    + "  |  " + unclaimed + " Reward(s) Pending Claim");
            System.out.println("==========================================================================");
            System.out.println("Commands: [1-" + achievements.size() + "] Claim Specific Reward  |  [C] Claim All Rewards  |  [0] Return");
            System.out.println("==========================================================================");

            choice = input.readLine("Select: ");
            if (choice.equals("0")) break;

            if (choice.equalsIgnoreCase("C")) {
                int claimed = am.claimAllRewards(player);
                playerDataManager.saveAllPlayersToCsv();
                if (claimed > 0) {
                    System.out.println("\n[SUCCESS] Successfully claimed rewards for " + claimed + " achievement(s)!");
                } else {
                    System.out.println("\n[!] No unclaimed achievement rewards available.");
                }
                input.readLine("Press Enter to continue...");
            } else {
                try {
                    int aIdx = Integer.parseInt(choice) - 1;
                    if (aIdx >= 0 && aIdx < achievements.size()) {
                        Achievement selected = achievements.get(aIdx);
                        if (!selected.isUnlocked()) {
                            System.out.println("\n[!] That achievement is still in progress (" + selected.getProgress(player) + " / " + selected.getTargetCount() + ").");
                        } else if (selected.isRewardClaimed()) {
                            System.out.println("\n[!] You have already claimed the rewards for this achievement.");
                        } else {
                            boolean ok = am.claimReward(player, selected.getId());
                            if (ok) {
                                playerDataManager.saveAllPlayersToCsv();
                                System.out.println("\n[SUCCESS] Claimed reward: " + selected.getRewardsDisplay() + "!");
                            }
                        }
                    } else {
                        System.out.println("Invalid selection.");
                    }
                    input.readLine("Press Enter to continue...");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                    input.readLine("Press Enter to continue...");
                }
            }
        } while (!choice.equals("0"));
    }

    private static void claimAchievementRewardsFlow(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        AchievementManager am = playerDataManager.getAchievementManager();
        int unclaimed = am.getUnclaimedCount(player);
        if (unclaimed == 0) {
            System.out.println("\n[!] No unclaimed achievement rewards available right now.");
            System.out.println("    Complete more achievements by opening crates, summoning heroes, and collecting gear!");
            input.readLine("Press Enter to continue...");
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("           CLAIM ACHIEVEMENT REWARDS");
        System.out.println("==================================================");
        System.out.println("  You have " + unclaimed + " unclaimed achievement reward(s) ready!");
        System.out.println("--------------------------------------------------");
        int claimed = am.claimAllRewards(player);
        playerDataManager.saveAllPlayersToCsv();

        System.out.println("[SUCCESS] Claimed all " + claimed + " available achievement rewards!");
        System.out.println("  Updated Balance -> Coins: " + String.format("%,d", player.getCoins())
                + "  |  Shards: " + String.format("%,d", player.getShards()));
        System.out.println("==================================================");
        input.readLine("\nPress Enter to continue...");
    }

}
