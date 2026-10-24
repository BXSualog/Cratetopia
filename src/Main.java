import accounts.PlayerAcc;
import managers.PlayerDataManager;

public class Main {

    public static void main(String[] args) {
        InputHandling input = new InputHandling();
        PlayerDataManager playerDataManager = new PlayerDataManager();

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
                    loginUser(input, playerDataManager);
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

    public static void loginUser(InputHandling input, PlayerDataManager playerDataManager) {
        System.out.println("===========================================");
        System.out.println("               PLAYER LOGIN");
        System.out.println("===========================================");

        String username = input.readLine("Enter username: ");
        String password = input.readLine("Enter password: ");

        boolean success = playerDataManager.loginPlayer(username, password);
        if (success) {
            PlayerAcc player = playerDataManager.getPlayer(username);
            System.out.println("Login successful! Welcome, " + player.getUsername() + "!");
            playerHomescreen(input, playerDataManager, player);
        } else {
            System.out.println("Invalid username or password. Please try again.");
            input.readLine("\nPress Enter to return to the Login Menu...");
        }
    }

    public static void playerHomescreen(InputHandling input, PlayerDataManager playerDataManager, PlayerAcc player) {
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
                    // Open Crates
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

            if (!choice.equals("7")) {
                input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("7"));
    }
}
