package characters;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * HeroSort – sorting and filtering strategies for the hero roster.
 * DSA: TreeMap (Red-Black BST) for O(n log n) sorted key grouping.
 */
public enum HeroSort {
    DEFAULT("Default (Roster Order)"),
    RARITY_DESC("Star Level (6★ → 1★)"),
    RARITY_ASC("Star Level (1★ → 6★)"),
    HP_DESC("Total HP (Highest First)"),
    ATK_DESC("Total Attack (Highest First)"),
    DEF_DESC("Total Defense (Highest First)"),
    CRIT_DESC("Total Crit % (Highest First)"),
    NAME_ASC("Hero Name (A → Z)"),
    CLASS_ORDER("Class Grouping");

    private final String displayName;
    HeroSort(String d) { this.displayName = d; }
    public String getDisplayName() { return displayName; }

    /** Sorts heroes by inserting into a TreeMap and reading back in key order. */
    public static LinkedList<Hero> sort(LinkedList<Hero> heroes, HeroSort strategy) {
        if (strategy == null || strategy == DEFAULT || heroes == null) return new LinkedList<>(heroes != null ? heroes : new LinkedList<>());

        // DSA: TreeMap with custom comparator — O(log n) per insert, in-order traversal gives sorted output
        TreeMap<String, LinkedList<Hero>> map = new TreeMap<>();
        for (Hero h : heroes) {
            String key = buildKey(h, strategy);
            LinkedList<Hero> bucket = map.get(key);
            if (bucket == null) { bucket = new LinkedList<>(); map.put(key, bucket); }
            bucket.add(h);
        }
        LinkedList<Hero> result = new LinkedList<>();
        for (LinkedList<Hero> bucket : map.values()) result.addAll(bucket);
        return result;
    }

    private static String buildKey(Hero h, HeroSort strategy) {
        // Pad numbers so lexicographic order = numeric order; prefix for inversion
        switch (strategy) {
            case RARITY_DESC:  return pad(99 - h.getStars()) + h.getName().toLowerCase();
            case RARITY_ASC:   return pad(h.getStars())       + h.getName().toLowerCase();
            case HP_DESC:      return pad(999999 - h.getTotalHp())  + h.getName().toLowerCase();
            case ATK_DESC:     return pad(999999 - h.getTotalAttack())  + h.getName().toLowerCase();
            case DEF_DESC:     return pad(999999 - h.getTotalDefense())  + h.getName().toLowerCase();
            case CRIT_DESC:    return pad(999999 - h.getTotalCritChance())  + h.getName().toLowerCase();
            case NAME_ASC:     return h.getName().toLowerCase();
            case CLASS_ORDER:  return h.getCharacterClass().name() + pad(99 - h.getStars()) + h.getName().toLowerCase();
            default:           return h.getName().toLowerCase();
        }
    }

    private static String pad(int n) { return String.format("%07d", Math.max(0, n)); }

    /** Filters heroes by class, star rating, and element. Null/"ALL" skips that filter. */
    public static LinkedList<Hero> filter(LinkedList<Hero> heroes, CharacterClass classFilter,
                                          Integer starFilter, String elementFilter) {
        LinkedList<Hero> result = new LinkedList<>();
        if (heroes == null) return result;
        for (Hero h : heroes) {
            if (classFilter != null && h.getCharacterClass() != classFilter) continue;
            if (starFilter != null && starFilter > 0 && h.getStars() != starFilter) continue;
            if (elementFilter != null && !elementFilter.equalsIgnoreCase("ALL") && !elementFilter.trim().isEmpty()
                    && !h.getElementalPower().equalsIgnoreCase(elementFilter.trim())) continue;
            result.add(h);
        }
        return result;
    }
}
