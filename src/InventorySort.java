import items.Accessory;
import items.Armor;
import items.Item;
import items.Weapon;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * InventorySort – specifies the sorting strategy for player inventory and equipment.
 *
 * OOP Concept – ENUM with methods & Strategy pattern
 * DSA Concept – TreeMap (Red-Black BST) for O(n log n) sorted key grouping,
 *               LinkedList linear search filtering.
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
            case "mythic":    return 6;
            case "legendary": return 5;
            case "epic":      return 4;
            case "rare":      return 3;
            case "uncommon":  return 2;
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

    private static String pad(int n) {
        return String.format("%07d", Math.max(0, n));
    }

    /**
     * Builds a sort key so TreeMap sorts items in the correct order.
     * Number padding ensures lexicographical order matches numeric order.
     */
    public static String buildSortKey(Item item, InventorySort strategy) {
        if (item == null || strategy == null) return "";
        switch (strategy) {
            case TYPE:
                // Type ascending (Weapon -> Armor -> Accessory), then Rarity descending, then Name A-Z
                return pad(typeRank(item)) + "_" + pad(99 - rarityRank(item.getRarity())) + "_" + item.getName().toLowerCase();
            case RARITY:
                // Rarity descending (Legendary -> Common), then Name A-Z
                return pad(99 - rarityRank(item.getRarity())) + "_" + item.getName().toLowerCase();
            case NAME:
                // Name ascending A-Z
                return item.getName().toLowerCase();
            case QUANTITY:
                // Quantity descending, then Name A-Z
                return pad(999999 - item.getQuantity()) + "_" + item.getName().toLowerCase();
            case STAT:
                // Primary stat bonus descending, then Name A-Z
                return pad(999999 - primaryStatValue(item)) + "_" + item.getName().toLowerCase();
            default:
                return item.getName().toLowerCase();
        }
    }

    /**
     * Sorts a list of items using the TreeMap (Red-Black BST) ADT.
     * Items are grouped into TreeMap buckets by sort key, then collected back.
     * Average Time Complexity: O(n log n).
     */
    public static LinkedList<Item> sort(LinkedList<Item> items, InventorySort strategy) {
        LinkedList<Item> sorted = new LinkedList<>(items != null ? items : new LinkedList<>());
        if (strategy == null || strategy == RECENT || sorted.size() <= 1) {
            return sorted;
        }

        // DSA: TreeMap (Red-Black BST) – keys are sorted automatically
        TreeMap<String, LinkedList<Item>> map = new TreeMap<>();
        for (Item item : sorted) {
            String key = buildSortKey(item, strategy);
            LinkedList<Item> bucket = map.get(key);
            if (bucket == null) {
                bucket = new LinkedList<>();
                map.put(key, bucket);
            }
            bucket.add(item);
        }

        LinkedList<Item> result = new LinkedList<>();
        for (LinkedList<Item> bucket : map.values()) {
            result.addAll(bucket);
        }
        return result;
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
