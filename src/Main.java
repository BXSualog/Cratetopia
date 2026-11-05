import accounts.AdminAcc;
import accounts.PlayerAcc;
import characters.CharacterClass;
import characters.Hero;
import items.Accessory;
import items.Armor;
import items.ArmorSet;
import items.Item;
import items.Weapon;
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
        AdminDataManager adminDataManager = new AdminDataManager(playerDataManager, itemDataManager, charDataManager);
        CharacterGachaManager gachaManager = new CharacterGachaManager(charDataManager);
        CrateManager crateManager = new CrateManager(itemDataManager);

        // Auto-save on JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            playerDataManager.saveAllPlayersToCsv();
            adminDataManager.saveAdminsToCsv();
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
            System.out.println("1. System Overview & Economy Analytics");
            System.out.println("2. Player Account Management");
            System.out.println("3. Game Content Catalogs (Heroes & Items)");
            System.out.println("4. Admin Credential Management");
            System.out.println("5. Save All Game Data");
            System.out.println("0. Logout");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    adminSystemOverview(input, adminDataManager);
                    break;

                case "2":
                    adminPlayerManagement(input, adminDataManager);
                    break;

                case "3":
                    adminCatalogsMenu(input, adminDataManager);
                    break;

                case "4":
                    adminAccountsMenu(input, adminDataManager, admin);
                    break;

                case "5":
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
                    System.out.println("[SUCCESS] All system, player, and administrator records saved!");
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

    /**
     * Submenu 1: System Overview & Economy Analytics
     */
    private static void adminSystemOverview(InputHandling input, AdminDataManager adminDataManager) {
        int totalPlayers = adminDataManager.getTotalPlayersCount();
        long totalCoins = adminDataManager.getTotalEconomyCoins();
        long totalShards = adminDataManager.getTotalEconomyShards();
        long avgCoins = (totalPlayers > 0) ? (totalCoins / totalPlayers) : 0;
        long avgShards = (totalPlayers > 0) ? (totalShards / totalPlayers) : 0;

        System.out.println("\n==================================================");
        System.out.println("       SYSTEM OVERVIEW & ECONOMY ANALYTICS");
        System.out.println("==================================================");
        System.out.printf("  Total Registered Players  : %,d (Active: %,d | Banned: %,d)\n",
                totalPlayers, adminDataManager.getActivePlayersCount(), adminDataManager.getBannedPlayersCount());
        System.out.printf("  Total Coins in Economy    : %,d\n", totalCoins);
        System.out.printf("  Total Shards in Economy   : %,d\n", totalShards);
        System.out.printf("  Average Coins per Player  : %,d\n", avgCoins);
        System.out.printf("  Average Shards per Player : %,d\n", avgShards);
        System.out.println("--------------------------------------------------");
        System.out.printf("  Catalog Hero Templates    : %,d\n", adminDataManager.getTotalHeroesCatalogCount());
        System.out.printf("  Catalog Item Templates    : %,d\n", adminDataManager.getTotalItemsCatalogCount());
        if (adminDataManager.getItemDataManager() != null) {
            System.out.printf("    - Weapons     : %,d\n", adminDataManager.getItemDataManager().getWeapons().size());
            System.out.printf("    - Armors      : %,d\n", adminDataManager.getItemDataManager().getArmors().size());
            System.out.printf("    - Accessories : %,d\n", adminDataManager.getItemDataManager().getAccessories().size());
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

    /**
     * Submenu 3: Game Content Catalogs
     */
    private static void adminCatalogsMenu(InputHandling input, AdminDataManager adminDataManager) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("              GAME CONTENT CATALOGS");
            System.out.println("==================================================");
            System.out.println("1. View Hero Templates Catalog");
            System.out.println("2. View Equipment & Item Catalog");
            System.out.println("0. Back to Admin Dashboard");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    adminViewHeroesCatalog(input, adminDataManager);
                    break;
                case "2":
                    adminViewItemsCatalog(input, adminDataManager);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static void adminViewHeroesCatalog(InputHandling input, AdminDataManager adminDataManager) {
        CharacterDataManager cdm = adminDataManager.getCharDataManager();
        if (cdm == null) {
            System.out.println("\n[!] Character Data Manager is not available.");
            input.readLine("Press Enter to return...");
            return;
        }

        System.out.println("\nSelect Hero Class to filter (or 'all' for all heroes):");
        System.out.println("Classes: Warrior, Mage, Assassin, Tank, Ranger, Support, Brawler");
        String filter = input.readLine("Class / 'all' (or '0' to cancel): ");
        if (filter.equals("0")) return;

        LinkedList<Hero> heroes;
        if (filter.equalsIgnoreCase("all") || filter.isEmpty()) {
            heroes = cdm.getAllHeroes();
        } else {
            CharacterClass cc = null;
            for (CharacterClass c : CharacterClass.values()) {
                if (c.name().equalsIgnoreCase(filter.trim()) || c.getDisplayName().equalsIgnoreCase(filter.trim())) {
                    cc = c;
                    break;
                }
            }
            if (cc == null) {
                System.out.println("\n[!] Unknown character class.");
                input.readLine("Press Enter to return...");
                return;
            }
            heroes = cdm.getHeroesByClass(cc);
        }

        System.out.println("\n==================================================");
        System.out.println("                  HERO CATALOG (" + heroes.size() + " Found)");
        System.out.println("==================================================");
        int idx = 1;
        for (Hero h : heroes) {
            System.out.printf("  [%d] %-4s %-20s %-10s %s (%s)\n",
                    idx++, h.getId(), h.getName(), h.getCharacterClass().getDisplayName(),
                    h.getStarsDisplay(), h.getElementalPowerDisplay());
            System.out.printf("      Stats -> HP:%d | ATK:%d | DEF:%d | CRIT:%d%%\n",
                    h.getBaseHp(), h.getBaseAttack(), h.getBaseDefense(), h.getBaseCritChance());
        }
        System.out.println("==================================================");
        input.readLine("\nPress Enter to return...");
    }

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
        System.out.println("0. Cancel");
        String cat = input.readLine("Select: ");

        LinkedList<? extends Item> list;
        String catName;
        if (cat.equals("1")) {
            list = idm.getWeapons();
            catName = "WEAPONS";
        } else if (cat.equals("2")) {
            list = idm.getArmors();
            catName = "ARMOR";
        } else if (cat.equals("3")) {
            list = idm.getAccessories();
            catName = "ACCESSORIES";
        } else {
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("             ITEM CATALOG: " + catName + " (" + list.size() + ")");
        System.out.println("==================================================");
        int idx = 1;
        for (Item it : list) {
            System.out.printf("  [%d] %-6s %-25s [%s]\n", idx++, it.getId(), it.getName(), it.getRarity());
            System.out.println("      Classes : " + it.getEquippableClassesDisplay());
            System.out.println("      Summary : " + it.getSummary().trim());
        }
        System.out.println("==================================================");
        input.readLine("\nPress Enter to return...");
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
                    inventoryMenu(input, player);
                    break;
                case "4":
                    shopAndTradingMenu(input, playerDataManager, player);
                    break;
                case "5":
                    // Achievements & Statistics
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

            if (!choice.equals("7") && !choice.equals("1") && !choice.equals("2") && !choice.equals("3") && !choice.equals("4")) {
                input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("7"));
    }

    // =========================================================================
    // Characters & Equipment Menu
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
            System.out.println("  Shards Available : " + player.getShards());
            System.out.println("--------------------------------------------------");
            System.out.println("1. View Character Roster & Equip Gear");
            System.out.println("2. Character Gacha Summon (Portal of Legends)");
            System.out.println("0. Back to Main Menu");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    viewRosterMenu(input, playerDataManager, player);
                    break;
                case "2":
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
            System.out.println("  Rarities: 1★ to 6★ (Scaling stats & power)");
            System.out.println("--------------------------------------------------");
            System.out.println("  Current Shards: " + player.getShards());
            System.out.println();
            System.out.println("  Rates: 1★ (45%) | 2★ (30%) | 3★ (15%) | 4★ (7%) | 5★ (2.5%) | 6★ (0.5%)");
            System.out.println("--------------------------------------------------");
            System.out.println("1. Single Summon (1 Shard)");
            System.out.println("2. 10x Multi-Summon (10 Shards) [Guaranteed 3★+]");
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

        // Use PriorityQueue to announce highest tier summoned
        if (!result.getHighestRankPulls().isEmpty()) {
            Hero top = result.getHighestRankPulls().peek();
            System.out.println("--------------------------------------------------");
            System.out.println("  ★ Top Summon: " + top.getName() + " - \"" + top.getTitle() + "\" (" + top.getStarsDisplay() + " " + top.getCharacterClass().getDisplayName() + " | " + top.getElementalPowerDisplay() + ")");
        }

        System.out.println("--------------------------------------------------");
        System.out.println("  Remaining Shards: " + player.getShards());
        System.out.println("==================================================");
        input.readLine("\nPress Enter to continue...");
    }

    // =========================================================================
    // View Roster & Equipment
    // =========================================================================

    private static void viewRosterMenu(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
        LinkedList<Hero> roster = player.getRoster();
        if (roster.isEmpty()) {
            System.out.println("\n[!] Your hero roster is empty!");
            System.out.println("    Use Shards in the 'Portal of Legends' to summon your first hero.");
            input.readLine("\nPress Enter to return...");
            return;
        }

        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("                   HERO ROSTER");
            System.out.println("==================================================");
            int idx = 1;
            for (Hero h : roster) {
                System.out.printf("  [%d] %-30s | HP:%-4d ATK:%-3d DEF:%-3d\n",
                        idx++, h.toString(), h.getTotalHp(), h.getTotalAttack(), h.getTotalDefense());
            }
            System.out.println("--------------------------------------------------");
            System.out.println("Select a hero number to inspect and equip gear (or 0 to return):");

            choice = input.readLine("Select: ");
            if (choice.equals("0")) break;

            try {
                int heroIdx = Integer.parseInt(choice) - 1;
                if (heroIdx >= 0 && heroIdx < roster.size()) {
                    heroDetailAndEquipMenu(input, playerDataManager, player, roster.get(heroIdx));
                } else {
                    System.out.println("Invalid hero selection.");
                    input.readLine("Press Enter to continue...");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
                input.readLine("Press Enter to continue...");
            }
        } while (!choice.equals("0"));
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

                // Auto-save equipped gear & inventory state
                playerDataManager.saveAllPlayersToCsv();

                System.out.println("\n[SUCCESS] Successfully equipped " + chosenItem.getName() + " on " + hero.getName() + "!");
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

            // Auto-save unequipped gear & inventory state
            playerDataManager.saveAllPlayersToCsv();

            System.out.println("\n[SUCCESS] Unequipped " + unequipped.getName() + " and returned it to your inventory.");
        } else {
            System.out.println("\n[!] That slot is already empty.");
        }
        input.readLine("Press Enter to continue...");
    }

    // =========================================================================
    // Inventory Menu
    // =========================================================================

    public static void inventoryMenu(InputHandling input, PlayerAcc player) {
        LinkedList<Item> inv = player.getInventory();
        System.out.println("\n==================================================");
        System.out.println("                 PLAYER INVENTORY");
        System.out.println("==================================================");
        System.out.println("  Total Items Owned: " + inv.size());
        System.out.println("--------------------------------------------------");

        if (inv.isEmpty()) {
            System.out.println("  Your inventory is empty!");
            System.out.println("  Open crates in Option 1 to get weapons, armor, and accessories.");
        } else {
            int idx = 1;
            for (Item item : inv) {
                System.out.printf("  [%d] %s\n", idx++, item.toString());
                System.out.println("      " + item.getSummary().trim());
            }
        }
        System.out.println("==================================================");
        input.readLine("\nPress Enter to continue...");
    }

    // =========================================================================
    // Shop & Trading Menu
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
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
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

        // Auto-save player balance changes
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
        input.readLine("\nPress Enter to continue...");
    }

    // =========================================================================
    // Crates
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
            System.out.println("  Coins: " + player.getCoins() + "    Shards: " + player.getShards());
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
        System.out.println("  New Balance -> Coins: " + player.getCoins() + "  |  Shards: " + player.getShards());
        System.out.println("  Items in Inventory: " + player.getInventory().size());
        System.out.println();

        System.out.println("  Returning to Open Crates...");
    }
}
