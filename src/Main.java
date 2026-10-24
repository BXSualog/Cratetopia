import accounts.PlayerAcc;
import characters.Hero;
import items.Accessory;
import items.Armor;
import items.Item;
import items.Weapon;
import managers.CharacterDataManager;
import managers.CharacterGachaManager;
import managers.CrateManager;
import managers.ItemDataManager;
import managers.PlayerDataManager;

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        InputHandling input = new InputHandling();
        PlayerDataManager playerDataManager = new PlayerDataManager();
        ItemDataManager itemDataManager = new ItemDataManager();
        CharacterDataManager charDataManager = new CharacterDataManager();
        CharacterGachaManager gachaManager = new CharacterGachaManager(charDataManager);
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
                    loginUser(input, playerDataManager, crateManager, gachaManager);
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
                    charactersAndEquipmentMenu(input, gachaManager, player);
                    break;
                case "3":
                    inventoryMenu(input, player);
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

            if (!choice.equals("7") && !choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
                input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("7"));
    }

    // =========================================================================
    // Characters & Equipment Menu
    // =========================================================================

    public static void charactersAndEquipmentMenu(InputHandling input, CharacterGachaManager gachaManager, PlayerAcc player) {
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
                    viewRosterMenu(input, player);
                    break;
                case "2":
                    characterGachaMenu(input, gachaManager, player);
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

    private static void characterGachaMenu(InputHandling input, CharacterGachaManager gachaManager, PlayerAcc player) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("            PORTAL OF LEGENDS (GACHA)");
            System.out.println("==================================================");
            System.out.println("  Summon legendary heroes across 6 classes!");
            System.out.println("  Classes : Warrior, Mage, Assassin, Tank, Ranger, Support");
            System.out.println("  Rarities: 1★ to 6★ (Scaling stats & power)");
            System.out.println("--------------------------------------------------");
            System.out.println("  Current Shards: " + player.getShards());
            System.out.println();
            System.out.println("  Rates: 1★ (45%) | 2★ (30%) | 3★ (15%) | 4★ (7%) | 5★ (2.5%) | 6★ (0.5%)");
            System.out.println("--------------------------------------------------");
            System.out.println("1. Single Summon (5 Shards)");
            System.out.println("2. 10x Multi-Summon (45 Shards) [Guaranteed 3★+]");
            System.out.println("0. Back");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    performGacha(input, gachaManager, player, false);
                    break;
                case "2":
                    performGacha(input, gachaManager, player, true);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    input.readLine("\nPress Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static void performGacha(InputHandling input, CharacterGachaManager gachaManager, PlayerAcc player, boolean isMulti) {
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

        System.out.println("\n==================================================");
        System.out.println("              SUMMON RESULTS (" + (isMulti ? "10x" : "Single") + ")");
        System.out.println("==================================================");

        for (Hero h : result.getSummonedHeroes()) {
            System.out.println("  " + h.toString());
            System.out.printf("    HP: %-4d | ATK: %-3d | DEF: %-3d | CRIT: %d%%\n",
                    h.getBaseHp(), h.getBaseAttack(), h.getBaseDefense(), h.getBaseCritChance());
            System.out.println("    \"" + h.getDescription() + "\"");
            System.out.println();
        }

        // Use PriorityQueue to announce highest tier summoned
        if (!result.getHighestRankPulls().isEmpty()) {
            Hero top = result.getHighestRankPulls().peek();
            System.out.println("--------------------------------------------------");
            System.out.println("  ★ Top Summon: " + top.getName() + " (" + top.getStarsDisplay() + " " + top.getCharacterClass().getDisplayName() + ")");
        }

        System.out.println("--------------------------------------------------");
        System.out.println("  Remaining Shards: " + player.getShards());
        System.out.println("==================================================");
        input.readLine("\nPress Enter to continue...");
    }

    // =========================================================================
    // View Roster & Equipment
    // =========================================================================

    private static void viewRosterMenu(InputHandling input, PlayerAcc player) {
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
                    heroDetailAndEquipMenu(input, player, roster.get(heroIdx));
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

    private static void heroDetailAndEquipMenu(InputHandling input, PlayerAcc player, Hero hero) {
        String choice;
        do {
            System.out.println("\n==================================================");
            System.out.println("           HERO DETAILS & EQUIPMENT");
            System.out.println("==================================================");
            System.out.println("  Name  : " + hero.getName());
            System.out.println("  Class : " + hero.getCharacterClass().getDisplayName() + " [" + hero.getStarsDisplay() + " " + hero.getRarityTitle() + "]");
            System.out.println("  Bio   : " + hero.getDescription());
            System.out.println("--------------------------------------------------");
            System.out.printf("  Base Stats  : HP: %-4d | ATK: %-3d | DEF: %-3d | CRIT: %d%%\n",
                    hero.getBaseHp(), hero.getBaseAttack(), hero.getBaseDefense(), hero.getBaseCritChance());
            System.out.printf("  Total Stats : HP: %-4d | ATK: %-3d | DEF: %-3d | CRIT: %d%%\n",
                    hero.getTotalHp(), hero.getTotalAttack(), hero.getTotalDefense(), hero.getTotalCritChance());
            System.out.println("--------------------------------------------------");
            System.out.println("  [Equipped Equipment]");
            System.out.println("  Weapon    : " + (hero.getEquippedWeapon() != null ? hero.getEquippedWeapon().getName() + " (" + hero.getEquippedWeapon().getSummary().trim() + ")" : "None"));
            System.out.println("  Armor     : " + (hero.getEquippedArmor() != null ? hero.getEquippedArmor().getName() + " (" + hero.getEquippedArmor().getSummary().trim() + ")" : "None"));
            System.out.println("  Accessory : " + (hero.getEquippedAccessory() != null ? hero.getEquippedAccessory().getName() + " (" + hero.getEquippedAccessory().getSummary().trim() + ")" : "None"));
            System.out.println("--------------------------------------------------");
            System.out.println("1. Equip / Change Weapon");
            System.out.println("2. Equip / Change Armor");
            System.out.println("3. Equip / Change Accessory");
            System.out.println("4. Unequip an Item");
            System.out.println("0. Back to Roster");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");

            switch (choice) {
                case "1":
                    equipItemFlow(input, player, hero, Weapon.class, "Weapon");
                    break;
                case "2":
                    equipItemFlow(input, player, hero, Armor.class, "Armor");
                    break;
                case "3":
                    equipItemFlow(input, player, hero, Accessory.class, "Accessory");
                    break;
                case "4":
                    unequipFlow(input, player, hero);
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option.");
                    input.readLine("Press Enter to continue...");
            }
        } while (!choice.equals("0"));
    }

    private static <T extends Item> void equipItemFlow(InputHandling input, PlayerAcc player, Hero hero,
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

    private static void unequipFlow(InputHandling input, PlayerAcc player, Hero hero) {
        System.out.println("\n==================================================");
        System.out.println("                   UNEQUIP GEAR");
        System.out.println("==================================================");
        System.out.println("1. Weapon    : " + (hero.getEquippedWeapon() != null ? hero.getEquippedWeapon().getName() : "(Empty)"));
        System.out.println("2. Armor     : " + (hero.getEquippedArmor() != null ? hero.getEquippedArmor().getName() : "(Empty)"));
        System.out.println("3. Accessory : " + (hero.getEquippedAccessory() != null ? hero.getEquippedAccessory().getName() : "(Empty)"));
        System.out.println("0. Cancel");
        System.out.println("--------------------------------------------------");

        String choice = input.readLine("Select slot to unequip: ");
        String slot = null;
        if (choice.equals("1")) slot = "weapon";
        else if (choice.equals("2")) slot = "armor";
        else if (choice.equals("3")) slot = "accessory";
        else return;

        Item unequipped = hero.unequip(slot);
        if (unequipped != null) {
            player.addItem(unequipped);
            System.out.println("\n[SUCCESS] Unequipped " + unequipped.getName() + " and returned it to your inventory.");
        } else {
            System.out.println("\n[!] Slot was already empty.");
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

    private static void performCrateOpen(InputHandling input, CrateManager crateManager, PlayerAcc player) {
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
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
