import accounts.PlayerAcc;
import items.Item;
import managers.CrateManager;
import managers.ItemDataManager;
import managers.PlayerDataManager;

public class Main {

    public static void main(String[] args) {
        InputHandling input = new InputHandling();
        PlayerDataManager playerDataManager = new PlayerDataManager();
        ItemDataManager itemDataManager = new ItemDataManager();
        CrateManager crateManager = new CrateManager(itemDataManager);

        String choice;

        do {
            System.out.println("\n============================================");
            System.out.println("             INFINITE REALMS");
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
                    loginUser(input, playerDataManager, crateManager);
                    break;

                case "3":

                    break;

                case "0":
                    System.out.println("\nThank you for playing Infinite Realms!");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }

            if (!choice.equals("0") && !choice.equals("1") && !choice.equals("2")) {
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

    public static void loginUser(InputHandling input, PlayerDataManager playerDataManager, CrateManager crateManager) {
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
                System.out.println("Login successful! Welcome, " + player.getUsername() + "!");
                playerHomescreen(input, playerDataManager, crateManager, player);
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

    public static void playerHomescreen(InputHandling input, PlayerDataManager playerDataManager, CrateManager crateManager, PlayerAcc player) {
        String choice;

        do {
            System.out.println("\n==================================================");
            System.out.println("                 CRATEOPIA");
            System.out.println("==================================================");
            System.out.println();
            System.out.println("Welcome, " + player.getUsername());
            System.out.println();
            System.out.println("Coins: " + player.getCoins() + "          Shards: " + player.getShards());
            System.out.println("Heroes Collected: " + player.getHeroesCollected() + " Items Owned: " + player.getItemsOwned());
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
                    // Characters & Equipment
                    break;
                case "3":
                    // Inventory
                    break;
                case "4":
                    // Shop & Trading
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

            if (!choice.equals("7") && !choice.equals("1")) {
                input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("7"));
    }

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
                    performCrateOpen(input, crateManager, player);
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
     * Executes one crate roll, displays an animated result screen, and prints loot.
     */
    private static void performCrateOpen(InputHandling input, CrateManager crateManager, PlayerAcc player) {
        // Suspense animation
        System.out.print("\n  Rolling crate tier");
        for (int i = 0; i < 6; i++) {
            System.out.print(".");
            try { Thread.sleep(220); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        System.out.println();

        // Roll the tier
        String tier = crateManager.rollCrateTier();

        // Banner per tier
        System.out.println();
        System.out.println("  =============================================");
        System.out.println("  [ " + tier.toUpperCase() + " OPENED ]");
        System.out.println("  =============================================");

        // Open and get result
        CrateManager.CrateResult result = crateManager.openCrate(tier, player);

        // Display rewards
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
        System.out.println();

        System.out.println("  Returning to Open Crates...");
        try {
            Thread.sleep(1400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
