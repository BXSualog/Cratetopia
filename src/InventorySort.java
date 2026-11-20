import items.Accessory;
import items.Armor;
import items.Consumable;
import items.Item;
import items.Weapon;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * InventorySort – non-tree-based multi-level sorting and filtering for player inventory.
 *
 * OOP Concept: Strategy Pattern via Enum with custom Comparator implementations.
 * DSA Concept: Divide-and-Conquer Merge Sort on LinkedList with O(n log n) time complexity,
 *              strictly non-tree-based to satisfy CC 204 project rubrics.
 *              Implements explicit multi-level comparison (Primary criterion + Secondary tie-breaker).
 */
public enum InventorySort {
    RECENT("Recent (Default)", null, "Acquisition Order", "None"),
    TYPE("Item Type (Weapon -> Armor -> Accessory -> Consumable)",
            (a, b) -> {
                int cmp = Integer.compare(typeRank(a), typeRank(b)); // Level 1: Item Type rank
                if (cmp != 0) return cmp;
                int rCmp = Integer.compare(rarityRank(b.getRarity()), rarityRank(a.getRarity())); // Level 2: Rarity descending
                return (rCmp != 0) ? rCmp : a.getName().compareToIgnoreCase(b.getName());
            },
            "Item Type (Weapon -> Armor -> Accessory)", "Rarity Tier (Descending)"),
    RARITY("Rarity (Legendary -> Common)",
            (a, b) -> {
                int cmp = Integer.compare(rarityRank(b.getRarity()), rarityRank(a.getRarity())); // Level 1: Rarity descending
                return (cmp != 0) ? cmp : a.getName().compareToIgnoreCase(b.getName()); // Level 2: Name A-Z
            },
            "Rarity Tier (Descending)", "Item Name (A → Z)"),
    NAME("Name (A -> Z)",
            (a, b) -> {
                int cmp = a.getName().compareToIgnoreCase(b.getName()); // Level 1: Name A-Z
                return (cmp != 0) ? cmp : Integer.compare(rarityRank(b.getRarity()), rarityRank(a.getRarity())); // Level 2: Rarity descending
            },
            "Item Name (A → Z)", "Rarity Tier (Descending)"),
    QUANTITY("Quantity (Highest First)",
            (a, b) -> {
                int cmp = Integer.compare(b.getQuantity(), a.getQuantity()); // Level 1: Quantity descending
                return (cmp != 0) ? cmp : a.getName().compareToIgnoreCase(b.getName()); // Level 2: Name A-Z
            },
            "Quantity Owned (Descending)", "Item Name (A → Z)"),
    STAT("Primary Stat Bonus (Highest First)",
            (a, b) -> {
                int cmp = Integer.compare(primaryStatValue(b), primaryStatValue(a)); // Level 1: Stat value descending
                return (cmp != 0) ? cmp : Integer.compare(rarityRank(b.getRarity()), rarityRank(a.getRarity())); // Level 2: Rarity descending
            },
            "Primary Stat Bonus (Descending)", "Rarity Tier (Descending)");

    private final String displayName;
    private final Comparator<Item> comparator;
    private final String primaryCriterion;
    private final String secondaryCriterion;

    InventorySort(String displayName, Comparator<Item> comparator, String primaryCriterion, String secondaryCriterion) {
        this.displayName = displayName;
        this.comparator = comparator;
        this.primaryCriterion = primaryCriterion;
        this.secondaryCriterion = secondaryCriterion;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrimaryCriterion() {
        return primaryCriterion;
    }

    public String getSecondaryCriterion() {
        return secondaryCriterion;
    }

    public Comparator<Item> getComparator() {
        return comparator;
    }

    public static int rarityRank(String rarity) {
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

    public static int typeRank(Item item) {
        if (item instanceof Weapon) return 1;
        if (item instanceof Armor) return 2;
        if (item instanceof Accessory) return 3;
        if (item instanceof Consumable) return 4;
        return 5;
    }

    public static int primaryStatValue(Item item) {
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
     * Sorts items using Merge Sort (Non-tree-based divide-and-conquer algorithm).
     * Time Complexity: O(n log n), Space Complexity: O(n).
     */
    public static LinkedList<Item> sort(LinkedList<Item> items, InventorySort strategy) {
        if (items == null || items.size() <= 1 || strategy == null || strategy.comparator == null) {
            return new LinkedList<>(items != null ? items : new LinkedList<>());
        }
        return mergeSort(new LinkedList<>(items), strategy.comparator);
    }

    /**
     * Recursive Merge Sort on LinkedList.
     */
    public static <T> LinkedList<T> mergeSort(LinkedList<T> list, Comparator<T> cmp) {
        if (list.size() <= 1) {
            return list;
        }

        int mid = list.size() / 2;
        LinkedList<T> left = new LinkedList<>();
        LinkedList<T> right = new LinkedList<>();

        int i = 0;
        for (T item : list) {
            if (i < mid) {
                left.add(item);
            } else {
                right.add(item);
            }
            i++;
        }

        left = mergeSort(left, cmp);
        right = mergeSort(right, cmp);

        return merge(left, right, cmp);
    }

    private static <T> LinkedList<T> merge(LinkedList<T> left, LinkedList<T> right, Comparator<T> cmp) {
        LinkedList<T> merged = new LinkedList<>();
        while (!left.isEmpty() && !right.isEmpty()) {
            if (cmp.compare(left.peekFirst(), right.peekFirst()) <= 0) {
                merged.add(left.pollFirst());
            } else {
                merged.add(right.pollFirst());
            }
        }
        while (!left.isEmpty()) {
            merged.add(left.pollFirst());
        }
        while (!right.isEmpty()) {
            merged.add(right.pollFirst());
        }
        return merged;
    }

    /**
     * Filters items by item category ("ALL", "WEAPON", "ARMOR", "ACCESSORY", "CONSUMABLE")
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
            } else if (cat.equals("consumable") && item instanceof Consumable) {
                filtered.add(item);
            } else if (item.getType().toLowerCase().contains(cat)) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}
