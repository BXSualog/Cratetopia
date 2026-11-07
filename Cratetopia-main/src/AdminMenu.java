import accounts.PlayerAcc;
import characters.CharacterClass;
import characters.Element;
import characters.Hero;
import items.Accessory;
import items.Armor;
import items.Item;
import items.Weapon;
import managers.AdminDataManager;
import managers.CharacterDataManager;
import managers.CharacterGachaManager;
import managers.CrateManager;
import managers.ItemDataManager;
import managers.RateUtil;

import java.util.LinkedList;

/**
 * AdminMenu – all the admin screens (the "view" part of the admin side).
 *
 * Each menu method only talks to the user (print + read). Every actual data
 * change is handed to AdminDataManager / the other managers, so the rules
 * live in one place and the menus stay simple.
 */
public class AdminMenu {

    private static final String[] RARITIES = {"Common", "Uncommon", "Rare", "Epic", "Legendary"};
    private static final String[] ARMOR_TYPES = {"Helmet", "Chest", "Leggings"};
    private static final String[] BONUS_TYPES = {"attack", "health", "defense", "crit_chance"};

    private final InputHandling input;
    private final AdminDataManager admin;
    private final CharacterDataManager charData;
    private final ItemDataManager itemData;
    private final CharacterGachaManager gachaManager;
    private final CrateManager crateManager;

    public AdminMenu(InputHandling input, AdminDataManager admin, CharacterDataManager charData,
                     ItemDataManager itemData, CharacterGachaManager gachaManager,
                     CrateManager crateManager) {
        this.input = input;
        this.admin = admin;
        this.charData = charData;
        this.itemData = itemData;
        this.gachaManager = gachaManager;
        this.crateManager = crateManager;
    }

    // =========================================================================
    // Small input helpers (each one keeps asking until the answer is valid)
    // =========================================================================

    private void title(String text) {
        System.out.println("\n==================================================");
        System.out.println("  " + text);
        System.out.println("==================================================");
    }

    private void pause() {
        input.readLine("\nPress Enter to continue...");
    }

    /** Whole number in [min, max]. Returns null if the admin just presses Enter (= cancel). */
    private Integer askInt(String prompt, int min, int max) {
        while (true) {
            String text = input.readLine(prompt);
            if (text.isEmpty()) return null;
            try {
                int value = Integer.parseInt(text.replace(",", ""));
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // fall through to the message below
            }
            System.out.println("  Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    /**
     * Text field for add/edit forms.
     * current == null  -> new value, required.
     * current != null  -> pressing Enter keeps the current value ("" is allowed = optional field).
     */
    private String askText(String label, String current, boolean allowCommas) {
        while (true) {
            String shown = (current != null && !current.isEmpty()) ? " [" + current + "]" : "";
            String text = input.readLine("  " + label + shown + ": ");
            if (text.isEmpty()) {
                if (current != null) return current;
                System.out.println("  This field is required.");
            } else if (!allowCommas && text.contains(",")) {
                System.out.println("  Commas are not allowed here (they would break the CSV file).");
            } else {
                return text;
            }
        }
    }

    /** Number field for add/edit forms (same Enter-keeps-current rule as askText). */
    private int askNumber(String label, Integer current, int min, int max) {
        while (true) {
            String shown = (current != null) ? " [" + current + "]" : "";
            String text = input.readLine("  " + label + " (" + min + "-" + max + ")" + shown + ": ");
            if (text.isEmpty() && current != null) return current;
            try {
                int value = Integer.parseInt(text);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // fall through
            }
            System.out.println("  Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    /** Pick one value from a fixed list (case-insensitive). Returns the official spelling. */
    private String askChoice(String label, String[] options, String current) {
        while (true) {
            String shown = (current != null) ? " [" + current + "]" : "";
            String text = input.readLine("  " + label + " (" + String.join("/", options) + ")" + shown + ": ");
            if (text.isEmpty() && current != null) return current;
            for (String option : options) {
                if (option.equalsIgnoreCase(text)) return option;
            }
            System.out.println("  Please type one of: " + String.join(", ", options));
        }
    }

    /** "All" or a list like "Warrior;Tank". Returns the cleaned-up text stored in the CSV. */
    private String askClasses(String current) {
        while (true) {
            String shown = (current != null) ? " [" + current + "]" : "";
            String text = input.readLine("  Equippable classes ('All' or e.g. Warrior;Tank)" + shown + ": ");
            if (text.isEmpty() && current != null) return current;
            if (text.equalsIgnoreCase("all")) return "All";

            StringBuilder clean = new StringBuilder();
            boolean valid = !text.isEmpty();
            for (String token : text.split(";")) {
                CharacterClass cc = CharacterClass.fromString(token);
                if (cc == null) { valid = false; break; }
                if (clean.length() > 0) clean.append(";");
                clean.append(cc.getDisplayName());
            }
            if (valid) return clean.toString();
            System.out.println("  Valid classes: Warrior, Mage, Assassin, Tank, Ranger, Support, Brawler (separate with ;)");
        }
    }

    /** A percent like "45" or "0.5". Enter keeps the current value. Returns permille. */
    private int askPercent(String label, int currentPermille) {
        while (true) {
            String text = input.readLine("  " + label + " [" + RateUtil.fmt(currentPermille) + "%]: ");
            if (text.isEmpty()) return currentPermille;
            int value = RateUtil.parsePercent(text);
            if (value >= 0) return value;
            System.out.println("  Please enter a percent from 0 to 100 (decimals like 0.5 are fine).");
        }
    }

    private boolean confirm(String question) {
        return input.readLine(question + " (yes/no): ").equalsIgnoreCase("yes");
    }

    // =========================================================================
    // Table printers
    // =========================================================================

    private void printPlayers(LinkedList<PlayerAcc> players) {
        if (players.isEmpty()) {
            System.out.println("  (no players found)");
            return;
        }
        System.out.printf("  %-16s %10s %8s %7s %6s  %s%n", "Username", "Coins", "Shards", "Heroes", "Items", "Status");
        System.out.println("  --------------------------------------------------------------");
        for (PlayerAcc p : players) {
            System.out.printf("  %-16s %,10d %,8d %7d %6d  %s%n", p.getUsername(), p.getCoins(), p.getShards(),
                    p.getRoster().size(), p.getInventory().size(), p.isBanned() ? "BANNED" : "Active");
        }
    }

    private void printItems(LinkedList<? extends Item> items) {
        if (items.isEmpty()) {
            System.out.println("  (none)");
            return;
        }
        for (Item item : items) {
            System.out.printf("  %-6s %s%n", item.getId(), item);
            System.out.println("         " + item.getSummary().trim());
        }
    }

    private void printHeroes(LinkedList<Hero> heroes) {
        if (heroes.isEmpty()) {
            System.out.println("  (none)");
            return;
        }
        for (Hero h : heroes) {
            System.out.printf("  %-7s %s%n", h.getId(), h);
            System.out.printf("          HP: %-5d ATK: %-4d DEF: %-4d CRIT: %d%%%n",
                    h.getBaseHp(), h.getBaseAttack(), h.getBaseDefense(), h.getBaseCritChance());
        }
    }

    // =========================================================================
    // 1. PLAYER MANAGEMENT
    // =========================================================================

    public void playerManagement() {
        String choice;
        do {
            title("PLAYER MANAGEMENT");
            System.out.println("1. View All Players & Statistics");
            System.out.println("2. Search Player by Username");
            System.out.println("3. Manage a Player (edit / ban / reset password / delete)");
            System.out.println("0. Back");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");
            switch (choice) {
                case "1":
                    title("ALL REGISTERED PLAYERS (" + admin.getTotalPlayers() + ")");
                    printPlayers(admin.getAllPlayers());
                    pause();
                    break;
                case "2":
                    searchPlayer();
                    break;
                case "3":
                    String name = input.readLine("Username to manage (Enter to cancel): ");
                    if (name.isEmpty()) break;
                    PlayerAcc target = admin.getPlayer(name);
                    if (target == null) {
                        System.out.println("No player named '" + name + "'.");
                        pause();
                    } else {
                        managePlayer(target);
                    }
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    pause();
            }
        } while (!choice.equals("0"));
    }

    private void searchPlayer() {
        String text = input.readLine("Search username (part of the name is fine): ");
        if (text.isEmpty()) return;

        LinkedList<PlayerAcc> matches = admin.searchPlayers(text);
        title("SEARCH RESULTS FOR '" + text + "' (" + matches.size() + ")");
        printPlayers(matches);
        if (matches.isEmpty()) {
            pause();
            return;
        }
        String name = input.readLine("\nEnter a username from the list to manage it (or press Enter to go back): ");
        if (name.isEmpty()) return;
        PlayerAcc target = admin.getPlayer(name);
        if (target == null) {
            System.out.println("No player named '" + name + "'.");
            pause();
        } else {
            managePlayer(target);
        }
    }

    private void managePlayer(PlayerAcc p) {
        while (true) {
            title("MANAGE PLAYER: " + p.getUsername() + (p.isBanned() ? "   [BANNED]" : ""));
            System.out.printf("  Coins: %,d   Shards: %,d   Heroes: %d   Items: %d%n",
                    p.getCoins(), p.getShards(), p.getRoster().size(), p.getInventory().size());
            System.out.println("--------------------------------------------------");
            System.out.println("1. Set Coins");
            System.out.println("2. Set Shards");
            System.out.println("3. Add Hero");
            System.out.println("4. Remove Hero");
            System.out.println("5. Add Item");
            System.out.println("6. Remove Item");
            System.out.println("7. " + (p.isBanned() ? "Unban Account" : "Ban Account"));
            System.out.println("8. Reset Password");
            System.out.println("9. Delete Account");
            System.out.println("0. Back");
            System.out.println("==================================================");

            String choice = input.readLine("Select an option: ");
            switch (choice) {
                case "1": {
                    Integer coins = askInt("New coin balance (0-" + AdminDataManager.MAX_BALANCE + ", Enter to cancel): ",
                            0, AdminDataManager.MAX_BALANCE);
                    if (coins != null) {
                        admin.setCoins(p, coins);
                        System.out.println("Coins set to " + String.format("%,d", p.getCoins()) + ".");
                    }
                    break;
                }
                case "2": {
                    Integer shards = askInt("New shard balance (0-" + AdminDataManager.MAX_BALANCE + ", Enter to cancel): ",
                            0, AdminDataManager.MAX_BALANCE);
                    if (shards != null) {
                        admin.setShards(p, shards);
                        System.out.println("Shards set to " + String.format("%,d", p.getShards()) + ".");
                    }
                    break;
                }
                case "3":
                    giveHero(p);
                    break;
                case "4":
                    takeHero(p);
                    break;
                case "5":
                    giveItem(p);
                    break;
                case "6":
                    takeItem(p);
                    break;
                case "7":
                    admin.setBanned(p, !p.isBanned());
                    System.out.println(p.getUsername() + (p.isBanned() ? " is now BANNED and can no longer log in."
                            : " has been unbanned."));
                    break;
                case "8":
                    resetPassword(p);
                    break;
                case "9":
                    String typed = input.readLine("Type the username '" + p.getUsername()
                            + "' to PERMANENTLY delete this account (Enter to cancel): ");
                    if (typed.equals(p.getUsername())) {
                        admin.deletePlayer(p.getUsername());
                        System.out.println("Account deleted.");
                        pause();
                        return;   // the player no longer exists, leave this menu
                    }
                    System.out.println("Deletion cancelled.");
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
            if (!choice.equals("0")) pause();
        }
    }

    private void giveHero(PlayerAcc p) {
        String id = input.readLine("Hero ID to add ('list' to see all heroes, Enter to cancel): ");
        if (id.equalsIgnoreCase("list")) {
            printHeroes(charData.getAllHeroes());
            id = input.readLine("\nHero ID to add (Enter to cancel): ");
        }
        if (id.isEmpty()) return;
        if (charData.getHeroTemplateById(id) == null) {
            System.out.println("No hero with ID '" + id + "'.");
        } else if (admin.addHeroToPlayer(p, id)) {
            System.out.println("Hero added to " + p.getUsername() + "'s roster.");
        } else {
            System.out.println(p.getUsername() + " already owns that hero.");
        }
    }

    private void takeHero(PlayerAcc p) {
        if (p.getRoster().isEmpty()) {
            System.out.println("This player has no heroes.");
            return;
        }
        int i = 1;
        for (Hero h : p.getRoster()) System.out.println("  [" + (i++) + "] " + h);
        Integer pick = askInt("Hero number to remove (Enter to cancel): ", 1, p.getRoster().size());
        if (pick == null) return;
        Hero hero = p.getRoster().get(pick - 1);
        admin.removeHeroFromPlayer(p, hero);
        System.out.println("Removed " + hero.getName() + ". Anything it was wearing went back to the inventory.");
    }

    private void giveItem(PlayerAcc p) {
        String id = input.readLine("Item ID to add ('list' to see the catalog, Enter to cancel): ");
        if (id.equalsIgnoreCase("list")) {
            printItems(itemData.getAllItems());
            id = input.readLine("\nItem ID to add (Enter to cancel): ");
        }
        if (id.isEmpty()) return;
        if (itemData.getItemById(id) == null) {
            System.out.println("No item with ID '" + id + "'.");
        } else if (admin.addItemToPlayer(p, id)) {
            System.out.println("Item added to " + p.getUsername() + "'s inventory.");
        } else {
            System.out.println(p.getUsername() + " already owns that item.");
        }
    }

    private void takeItem(PlayerAcc p) {
        if (p.getInventory().isEmpty()) {
            System.out.println("This player's inventory is empty.");
            return;
        }
        int i = 1;
        for (Item item : p.getInventory()) System.out.println("  [" + (i++) + "] " + item);
        Integer pick = askInt("Item number to remove (Enter to cancel): ", 1, p.getInventory().size());
        if (pick == null) return;
        Item item = p.getInventory().get(pick - 1);
        admin.removeItemFromPlayer(p, item);
        System.out.println("Removed " + item.getName() + " from the inventory.");
    }

    private void resetPassword(PlayerAcc p) {
        String password = input.readLine("New password for " + p.getUsername() + " (Enter to cancel): ");
        if (password.isEmpty()) return;
        if (password.contains(",")) {
            System.out.println("Password cannot contain commas.");
            return;
        }
        admin.resetPassword(p, password);
        System.out.println("Password reset. Tell the player their new password.");
    }

    // =========================================================================
    // 2. ITEM / CATALOG MANAGEMENT
    // =========================================================================

    public void itemManagement() {
        String choice;
        do {
            title("ITEM / CATALOG MANAGEMENT");
            System.out.println("  Weapons: " + itemData.getWeapons().size()
                    + "  |  Armor: " + itemData.getArmors().size()
                    + "  |  Accessories: " + itemData.getAccessories().size());
            System.out.println("--------------------------------------------------");
            System.out.println("1. View Catalog");
            System.out.println("2. Add New Item");
            System.out.println("3. Edit Item");
            System.out.println("4. Delete Item");
            System.out.println("0. Back");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");
            switch (choice) {
                case "1":
                    viewItems();
                    break;
                case "2":
                    addItem();
                    break;
                case "3":
                    editItem();
                    break;
                case "4":
                    deleteItem();
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    pause();
            }
        } while (!choice.equals("0"));
    }

    private void viewItems() {
        String which = input.readLine("View: 1) Weapons  2) Armor  3) Accessories  4) All : ");
        if (which.equals("1") || which.equals("4")) {
            title("WEAPONS (" + itemData.getWeapons().size() + ")");
            printItems(itemData.getWeapons());
        }
        if (which.equals("2") || which.equals("4")) {
            title("ARMOR (" + itemData.getArmors().size() + ")");
            printItems(itemData.getArmors());
        }
        if (which.equals("3") || which.equals("4")) {
            title("ACCESSORIES (" + itemData.getAccessories().size() + ")");
            printItems(itemData.getAccessories());
        }
        pause();
    }

    private void addItem() {
        String which = input.readLine("New item type: 1) Weapon  2) Armor  3) Accessory (Enter to cancel): ");
        Item created;
        switch (which) {
            case "1":
                System.out.println("\nNew weapon (ID will be " + itemData.generateNextId("W") + ")");
                created = weaponForm(itemData.generateNextId("W"), null);
                break;
            case "2":
                System.out.println("\nNew armor (ID will be " + itemData.generateNextId("A") + ")");
                created = armorForm(itemData.generateNextId("A"), null);
                break;
            case "3":
                System.out.println("\nNew accessory (ID will be " + itemData.generateNextId("AC") + ")");
                created = accessoryForm(itemData.generateNextId("AC"), null);
                break;
            default:
                return;
        }
        System.out.println(admin.addItem(created)
                ? "\nAdded " + created.getName() + " (" + created.getId() + ") to the catalog."
                : "\nCould not add the item (ID already exists).");
        pause();
    }

    private void editItem() {
        String id = input.readLine("ID of the item to edit (Enter to cancel): ");
        if (id.isEmpty()) return;
        Item old = itemData.getItemById(id);
        if (old == null) {
            System.out.println("No item with ID '" + id + "'.");
            pause();
            return;
        }
        System.out.println("\nEditing " + old.getName() + ". Press Enter on any field to keep its current value.");
        Item updated;
        if (old instanceof Weapon)         updated = weaponForm(old.getId(), (Weapon) old);
        else if (old instanceof Armor)     updated = armorForm(old.getId(), (Armor) old);
        else                               updated = accessoryForm(old.getId(), (Accessory) old);

        System.out.println(admin.updateItem(updated)
                ? "\nItem updated. Players' copies were refreshed too."
                : "\nCould not update the item.");
        pause();
    }

    private void deleteItem() {
        String id = input.readLine("ID of the item to delete (Enter to cancel): ");
        if (id.isEmpty()) return;
        Item item = itemData.getItemById(id);
        if (item == null) {
            System.out.println("No item with ID '" + id + "'.");
        } else if (confirm("Delete " + item.getName() + "? It is also removed from every player's inventory/gear")) {
            admin.deleteItem(item.getId());
            System.out.println("Item deleted.");
        } else {
            System.out.println("Cancelled.");
        }
        pause();
    }

    // old == null means "adding a new item"; otherwise the old values are the defaults.

    private Weapon weaponForm(String id, Weapon old) {
        String name   = askText("Name", old == null ? null : old.getName(), false);
        String rarity = askChoice("Rarity", RARITIES, old == null ? null : old.getRarity());
        String type   = askText("Type (e.g. Sword, Bow)", old == null ? null : old.getType(), false);
        int attack    = askNumber("Attack", old == null ? null : old.getAttack(), 0, 9999);
        int crit      = askNumber("Crit chance %", old == null ? null : old.getCritChance(), 0, 100);
        String classes = askClasses(old == null ? null : old.getEquippableClassesRaw());
        String desc   = askText("Description", old == null ? null : old.getDescription(), true);
        return new Weapon(id, name, rarity, type, attack, crit, classes, desc);
    }

    private Armor armorForm(String id, Armor old) {
        String name   = askText("Name", old == null ? null : old.getName(), false);
        String rarity = askChoice("Rarity", RARITIES, old == null ? null : old.getRarity());
        String type   = askChoice("Slot type", ARMOR_TYPES, old == null ? null : old.getType());
        String set    = askText("Set name ('None' for no set)", old == null ? null : old.getSetName(), false);
        if (!set.equalsIgnoreCase("None") && Hero.DEFAULT_CATALOG.getSet(set) == null) {
            System.out.println("  Note: no set called '" + set + "' exists in armor_sets.csv, so it gives no set bonus.");
        }
        int defense   = askNumber("Defense", old == null ? null : old.getDefense(), 0, 9999);
        int hpBonus   = askNumber("Health bonus", old == null ? null : old.getHealthBonus(), 0, 99999);
        int hpPercent = askNumber("Health % bonus", old == null ? null : old.getHealthPercentBonus(), 0, 100);
        String classes = askClasses(old == null ? null : old.getEquippableClassesRaw());
        String desc   = askText("Description", old == null ? null : old.getDescription(), true);
        return new Armor(id, name, rarity, type, set, defense, hpBonus, hpPercent, classes, desc);
    }

    private Accessory accessoryForm(String id, Accessory old) {
        String name   = askText("Name", old == null ? null : old.getName(), false);
        String rarity = askChoice("Rarity", RARITIES, old == null ? null : old.getRarity());
        String type   = askText("Type (e.g. Ring, Amulet)", old == null ? null : old.getType(), false);
        int bonus     = askNumber("Stat bonus", old == null ? null : old.getStatBonus(), 0, 9999);
        String bonusType = askChoice("Bonus type", BONUS_TYPES, old == null ? null : old.getBonusType());
        String classes = askClasses(old == null ? null : old.getEquippableClassesRaw());
        String desc   = askText("Description", old == null ? null : old.getDescription(), true);
        return new Accessory(id, name, rarity, type, bonus, bonusType, classes, desc);
    }

    // =========================================================================
    // 3. HERO MANAGEMENT
    // =========================================================================

    public void heroManagement() {
        String choice;
        do {
            title("HERO MANAGEMENT");
            System.out.println("  Heroes in catalog: " + charData.getAllHeroes().size());
            System.out.println("--------------------------------------------------");
            System.out.println("1. View All Heroes (all classes)");
            System.out.println("2. Add New Hero");
            System.out.println("3. Edit Hero");
            System.out.println("4. Delete Hero");
            System.out.println("0. Back");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");
            switch (choice) {
                case "1":
                    for (CharacterClass cc : CharacterClass.values()) {
                        LinkedList<Hero> heroes = charData.getHeroesByClass(cc);
                        System.out.println("\n== " + cc.getDisplayName().toUpperCase() + " (" + heroes.size() + ") ==");
                        printHeroes(heroes);
                    }
                    pause();
                    break;
                case "2":
                    addHero();
                    break;
                case "3":
                    editHero();
                    break;
                case "4":
                    deleteHero();
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    pause();
            }
        } while (!choice.equals("0"));
    }

    private void addHero() {
        String[] classNames = new String[CharacterClass.values().length];
        for (int i = 0; i < classNames.length; i++) classNames[i] = CharacterClass.values()[i].getDisplayName();

        CharacterClass cc = CharacterClass.fromString(askChoice("Class", classNames, null));
        String id = charData.generateNextId(cc);
        System.out.println("\nNew " + cc.getDisplayName() + " (ID will be " + id + ")");
        Hero created = heroForm(id, cc, null);
        System.out.println(admin.addHero(created)
                ? "\nAdded " + created.getName() + " (" + id + ") to " + cc.getDisplayName() + " heroes."
                : "\nCould not add the hero (ID already exists).");
        pause();
    }

    private void editHero() {
        String id = input.readLine("ID of the hero to edit (Enter to cancel): ");
        if (id.isEmpty()) return;
        Hero old = charData.getHeroTemplateById(id);
        if (old == null) {
            System.out.println("No hero with ID '" + id + "'.");
            pause();
            return;
        }
        System.out.println("\nEditing " + old.getName() + " (class stays " + old.getCharacterClass().getDisplayName()
                + "). Press Enter on any field to keep its current value.");
        Hero updated = heroForm(old.getId(), old.getCharacterClass(), old);
        System.out.println(admin.updateHero(updated)
                ? "\nHero updated. Players' copies were refreshed too."
                : "\nCould not update the hero.");
        pause();
    }

    private void deleteHero() {
        String id = input.readLine("ID of the hero to delete (Enter to cancel): ");
        if (id.isEmpty()) return;
        Hero hero = charData.getHeroTemplateById(id);
        if (hero == null) {
            System.out.println("No hero with ID '" + id + "'.");
        } else if (confirm("Delete " + hero.getName() + "? Players who own it lose it (their gear goes back to the inventory)")) {
            admin.deleteHero(hero.getId());
            System.out.println("Hero deleted.");
        } else {
            System.out.println("Cancelled.");
        }
        pause();
    }

    private Hero heroForm(String id, CharacterClass cc, Hero old) {
        String[] elements = new String[Element.values().length];
        for (int i = 0; i < elements.length; i++) elements[i] = Element.values()[i].getDisplayName();

        String name    = askText("Name", old == null ? null : old.getName(), false);
        String title   = askText("Title (optional)", old == null ? "" : old.getTitle(), false);
        String element = askChoice("Element", elements, old == null ? "None" : old.getElementalPower());
        int stars      = askNumber("Stars", old == null ? null : old.getStars(), 1, 6);
        int hp         = askNumber("Base HP", old == null ? null : old.getBaseHp(), 1, 99999);
        int attack     = askNumber("Base attack", old == null ? null : old.getBaseAttack(), 0, 9999);
        int defense    = askNumber("Base defense", old == null ? null : old.getBaseDefense(), 0, 9999);
        int crit       = askNumber("Base crit chance %", old == null ? null : old.getBaseCritChance(), 0, 100);
        String desc    = askText("Description", old == null ? null : old.getDescription(), true);
        return new Hero(id, name, title, element, cc, stars, hp, attack, defense, crit, desc);
    }

    // =========================================================================
    // 4. GAME ECONOMY MANAGEMENT
    // =========================================================================

    public void economyManagement() {
        String choice;
        do {
            title("GAME ECONOMY MANAGEMENT");
            System.out.println("1. View Crate Reward Tables");
            System.out.println("2. Edit Crate Tier Odds");
            System.out.println("3. Edit a Crate Tier's Rewards");
            System.out.println("4. Reset Crate Tables to Defaults");
            System.out.println("5. Gacha Pull Rates (view / edit)");
            System.out.println("6. Grant Coins or Shards to a Player");
            System.out.println("0. Back");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");
            switch (choice) {
                case "1":
                    printCrateTables();
                    pause();
                    break;
                case "2":
                    editTierOdds();
                    pause();
                    break;
                case "3":
                    editCrateRewards();
                    pause();
                    break;
                case "4":
                    if (confirm("Reset ALL crate odds and rewards to the original values?")) {
                        crateManager.resetToDefaults();
                        System.out.println("Crate tables reset.");
                    }
                    pause();
                    break;
                case "5":
                    gachaRates();
                    break;
                case "6":
                    grantCurrency();
                    pause();
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    pause();
            }
        } while (!choice.equals("0"));
    }

    private String pct(CrateManager.CrateConfig cfg, int field) {
        return RateUtil.fmt(cfg.get(field)) + "%";
    }

    private void printCrateTables() {
        title("CRATE REWARD TABLES");
        for (CrateManager.CrateConfig c : crateManager.getConfigs()) {
            System.out.println("  [" + c.getTier() + "]  chance to be rolled: " + pct(c, CrateManager.F_TIER_CHANCE));
            System.out.println("     Coins   : " + c.get(CrateManager.F_COINS_MIN) + " - " + c.get(CrateManager.F_COINS_MAX));
            System.out.println("     Weapon  : " + pct(c, CrateManager.F_WEAPON_CHANCE) + " (" + c.getItemRarity() + ")");
            System.out.println("     Armor   : " + pct(c, CrateManager.F_ARMOR_CHANCE) + " (" + c.getItemRarity() + ")");
            System.out.println("     Bonus   : " + pct(c, CrateManager.F_BONUS_CHANCE) + " (any " + c.getBonusRarity() + " equipment)");
            System.out.println("     Shards  : " + pct(c, CrateManager.F_SHARD_CHANCE) + " for "
                    + c.get(CrateManager.F_SHARD_MIN) + " - " + c.get(CrateManager.F_SHARD_MAX));
        }
    }

    private void editTierOdds() {
        System.out.println("\nSet the chance of each crate tier. They must add up to exactly 100%.");
        System.out.println("(Press Enter to keep a value.)");
        int[] chances = new int[CrateManager.TIERS.length];
        for (int i = 0; i < chances.length; i++) {
            chances[i] = askPercent(CrateManager.TIERS[i],
                    crateManager.getConfig(CrateManager.TIERS[i]).get(CrateManager.F_TIER_CHANCE));
        }
        if (crateManager.setTierChances(chances)) {
            System.out.println("Tier odds updated.");
        } else {
            int sum = 0;
            for (int c : chances) sum += c;
            System.out.println("These add up to " + RateUtil.fmt(sum) + "%, not 100%. Nothing was changed.");
        }
    }

    private void editCrateRewards() {
        for (int i = 0; i < CrateManager.TIERS.length; i++) {
            System.out.println("  " + (i + 1) + ". " + CrateManager.TIERS[i]);
        }
        Integer tierPick = askInt("Which crate? (Enter to cancel): ", 1, CrateManager.TIERS.length);
        if (tierPick == null) return;
        CrateManager.CrateConfig cfg = crateManager.getConfig(CrateManager.TIERS[tierPick - 1]);

        // field 0 (tier chance) is edited with "Edit Crate Tier Odds", so we start at 1
        for (int f = 1; f < CrateManager.FIELD_LABELS.length; f++) {
            String value = CrateManager.isChanceField(f) ? pct(cfg, f) : String.valueOf(cfg.get(f));
            System.out.printf("  %d. %-24s %s%n", f, CrateManager.FIELD_LABELS[f], value);
        }
        Integer field = askInt("Which value to change? (Enter to cancel): ", 1, CrateManager.FIELD_LABELS.length - 1);
        if (field == null) return;

        int newValue;
        if (CrateManager.isChanceField(field)) {
            newValue = askPercent(CrateManager.FIELD_LABELS[field], cfg.get(field));
        } else {
            newValue = askNumber(CrateManager.FIELD_LABELS[field], cfg.get(field), 0, 1_000_000);
        }
        System.out.println(crateManager.setValue(cfg.getTier(), field, newValue)
                ? "Updated."
                : "Rejected: a minimum cannot be higher than its maximum.");
    }

    private void gachaRates() {
        String choice;
        do {
            title("GACHA PULL RATES");
            for (int star = 1; star <= 6; star++) {
                System.out.printf("  %d★ : %s%%%n", star, RateUtil.fmt(gachaManager.getRate(star)));
            }
            System.out.println("--------------------------------------------------");
            System.out.println("1. Edit Rates (must add up to 100%)");
            System.out.println("2. Reset to Defaults");
            System.out.println("0. Back");
            System.out.println("==================================================");

            choice = input.readLine("Select an option: ");
            switch (choice) {
                case "1":
                    System.out.println("\n(Press Enter to keep a value.)");
                    int[] rates = new int[6];
                    for (int star = 1; star <= 6; star++) {
                        rates[star - 1] = askPercent(star + "★", gachaManager.getRate(star));
                    }
                    if (gachaManager.setRates(rates)) {
                        System.out.println("Gacha rates updated.");
                    } else {
                        int sum = 0;
                        for (int r : rates) sum += r;
                        System.out.println("These add up to " + RateUtil.fmt(sum) + "%, not 100%. Nothing was changed.");
                    }
                    pause();
                    break;
                case "2":
                    if (confirm("Reset the gacha rates to the original values?")) {
                        gachaManager.resetRates();
                        System.out.println("Rates reset.");
                    }
                    pause();
                    break;
                case "0":
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    pause();
            }
        } while (!choice.equals("0"));
    }

    private void grantCurrency() {
        String name = input.readLine("Username to grant to (Enter to cancel): ");
        if (name.isEmpty()) return;
        PlayerAcc p = admin.getPlayer(name);
        if (p == null) {
            System.out.println("No player named '" + name + "'.");
            return;
        }
        String what = input.readLine("Grant 1) Coins  2) Shards : ");
        if (!what.equals("1") && !what.equals("2")) return;

        Integer amount = askInt("Amount to add (1-" + AdminDataManager.MAX_BALANCE + ", Enter to cancel): ",
                1, AdminDataManager.MAX_BALANCE);
        if (amount == null) return;

        if (what.equals("1")) {
            admin.grantCoins(p, amount);
        } else {
            admin.grantShards(p, amount);
        }
        System.out.printf("Done. %s now has %,d coins and %,d shards.%n", p.getUsername(), p.getCoins(), p.getShards());
    }

    // =========================================================================
    // 5. STATISTICS / REPORTS
    // =========================================================================

    public void statisticsReport() {
        int total = admin.getTotalPlayers();
        title("STATISTICS & REPORTS");
        System.out.println("  Total registered players : " + total
                + "   (Active: " + (total - admin.getBannedCount()) + " | Banned: " + admin.getBannedCount() + ")");
        System.out.println("--------------------------------------------------");
        System.out.println("  PLAYER ACTIVITY OVERVIEW");
        System.out.printf("  Coins in circulation     : %,d   (avg %,d per player)%n",
                admin.getTotalCoins(), total == 0 ? 0 : admin.getTotalCoins() / total);
        System.out.printf("  Shards in circulation    : %,d   (avg %,d per player)%n",
                admin.getTotalShards(), total == 0 ? 0 : admin.getTotalShards() / total);
        System.out.println("  Heroes owned (all players): " + admin.getTotalHeroesOwned());
        System.out.println("  Items owned (all players) : " + admin.getTotalItemsOwned());
        System.out.println("--------------------------------------------------");
        System.out.println("  CATALOG SIZE");
        System.out.println("  Heroes: " + charData.getAllHeroes().size()
                + " | Weapons: " + itemData.getWeapons().size()
                + " | Armor: " + itemData.getArmors().size()
                + " | Accessories: " + itemData.getAccessories().size());
        System.out.println("--------------------------------------------------");

        System.out.println("  TOP 3 BY COINS");
        int rank = 1;
        for (PlayerAcc p : admin.getTopPlayers((a, b) -> Integer.compare(b.getCoins(), a.getCoins()), 3)) {
            System.out.printf("   %d. %-16s %,d coins%n", rank++, p.getUsername(), p.getCoins());
        }
        System.out.println("  TOP 3 BY HEROES");
        rank = 1;
        for (PlayerAcc p : admin.getTopPlayers((a, b) -> Integer.compare(b.getRoster().size(), a.getRoster().size()), 3)) {
            System.out.printf("   %d. %-16s %d heroes%n", rank++, p.getUsername(), p.getRoster().size());
        }
        System.out.println("  TOP 3 BY ITEMS");
        rank = 1;
        for (PlayerAcc p : admin.getTopPlayers((a, b) -> Integer.compare(b.getInventory().size(), a.getInventory().size()), 3)) {
            System.out.printf("   %d. %-16s %d items%n", rank++, p.getUsername(), p.getInventory().size());
        }
        System.out.println("==================================================");
        pause();
    }
}
