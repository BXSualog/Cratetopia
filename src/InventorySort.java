import items.Accessory;
import items.Armor;
import items.Item;
import items.Weapon;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * InventorySort – specifies the sorting strategy for player inventory and equipment.
 *
 * OOP Concept – ENUM with methods & Strategy pattern
 * DSA Concept – TreeSort (Binary Search Tree Sort), LinkedList linear search filtering
 */
public enum InventorySort {
    RECENT("Recent (Default)"),
    TYPE("Item Type (Weapon -> Armor -> Accessory)"),
    RARITY("Rarity (Legendary -> Common)"),
    NAME("Name (A -> Z)"),
    QUANTITY("Quantity"),
    STAT("Primary Stat Bonus (Highest First)");

    private final String displayName;

    InventorySort(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static int rarityRank(String rarity) {
        if (rarity == null) return 0;
        switch (rarity.toLowerCase().trim()) {
            case "legendary": return 4;
            case "epic":      return 3;
            case "rare":      return 2;
            case "common":    return 1;
            default:          return 0;
        }
    }

    private static int typeRank(Item item) {
        if (item instanceof Weapon) return 1;
        if (item instanceof Armor) return 2;
        if (item instanceof Accessory) return 3;
        return 4;
    }

    private static int primaryStatValue(Item item) {
        if (item instanceof Weapon) {
            Weapon w = (Weapon) item;
            return w.getAttack() * 2 + w.getCritChance();
        } else if (item instanceof Armor) {
            Armor a = (Armor) item;
            return a.getDefense() + a.getHealthBonus() / 5 + a.getHealthPercentBonus() * 10;
        } else if (item instanceof Accessory) {
            Accessory acc = (Accessory) item;
            return acc.getStatBonus();
        }
        return 0;
    }

    /**
     * Returns a comparator for the given inventory sorting strategy.
     */
    public static Comparator<Item> getComparator(InventorySort strategy) {
        if (strategy == null) return (a, b) -> 0;
        switch (strategy) {
            case TYPE:
                return (a, b) -> {
                    int c = Integer.compare(typeRank(a), typeRank(b));
                    if (c != 0) return c;
                    int r = Integer.compare(rarityRank(b.getRarity()), rarityRank(a.getRarity()));
                    return (r != 0) ? r : a.getName().compareToIgnoreCase(b.getName());
                };

            case RARITY:
                return (a, b) -> {
                    int r = Integer.compare(rarityRank(b.getRarity()), rarityRank(a.getRarity()));
                    return (r != 0) ? r : a.getName().compareToIgnoreCase(b.getName());
                };

            case NAME:
                return (a, b) -> a.getName().compareToIgnoreCase(b.getName());

            case QUANTITY:
                return (a, b) -> {
                    int q = Integer.compare(b.getQuantity(), a.getQuantity());
                    return (q != 0) ? q : a.getName().compareToIgnoreCase(b.getName());
                };

            case STAT:
                return (a, b) -> {
                    int s = Integer.compare(primaryStatValue(b), primaryStatValue(a));
                    return (s != 0) ? s : a.getName().compareToIgnoreCase(b.getName());
                };

            default:
                return (a, b) -> 0;
        }
    }

    /**
     * Sorts a list of items using the TreeSort (Binary Search Tree Sort) ADT.
     */
    public static LinkedList<Item> sort(LinkedList<Item> items, InventorySort strategy) {
        LinkedList<Item> sorted = new LinkedList<>(items);
        if (strategy == null || strategy == RECENT) {
            return sorted;
        }

        Comparator<Item> comp = getComparator(strategy);
        // DSA: Uses TreeSort (Binary Search Tree In-Order Traversal)
        return TreeSort.sort(sorted, comp);
    }

    /**
     * Filters items by item category ("ALL", "WEAPON", "ARMOR", "ACCESSORY")
     * using LinkedList linear traversal.
     */
    public static LinkedList<Item> filterByType(LinkedList<Item> items, String typeCategory) {
        LinkedList<Item> filtered = new LinkedList<>();
        if (items == null) return filtered;
        if (typeCategory == null || typeCategory.equalsIgnoreCase("ALL")) {
            filtered.addAll(items);
            return filtered;
        }

        String cat = typeCategory.trim().toLowerCase();
        for (Item item : items) {
            if (cat.equals("weapon") && item instanceof Weapon) {
                filtered.add(item);
            } else if (cat.equals("armor") && item instanceof Armor) {
                filtered.add(item);
            } else if (cat.equals("accessory") && item instanceof Accessory) {
                filtered.add(item);
            } else if (item.getType().toLowerCase().contains(cat)) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}
