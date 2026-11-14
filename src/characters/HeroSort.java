package characters;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * HeroSort – specifies sorting strategies and filtering for the player's hero roster.
 *
 * OOP Concepts: ENUM, COMPARATOR, STRATEGY PATTERN
 * DSA Concepts: TreeSort (Binary Search Tree Sort), LinkedList traversal and linear filtering.
 */
public enum HeroSort {
    DEFAULT("Default (Roster Order)"),
    RARITY_DESC("Star Level (6★ -> 1★)"),
    RARITY_ASC("Star Level (1★ -> 6★)"),
    HP_DESC("Total HP (Highest First)"),
    ATK_DESC("Total Attack (Highest First)"),
    DEF_DESC("Total Defense (Highest First)"),
    CRIT_DESC("Total Crit % (Highest First)"),
    NAME_ASC("Hero Name (A -> Z)"),
    CLASS_ORDER("Class Grouping");

    private final String displayName;

    HeroSort(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a new sorted LinkedList of heroes according to the chosen strategy
     * using the TreeSort (Binary Search Tree Sort) ADT algorithm.
     */
    public static LinkedList<Hero> sort(LinkedList<Hero> heroes, HeroSort strategy) {
        LinkedList<Hero> sorted = new LinkedList<>(heroes);
        if (strategy == null || strategy == DEFAULT) {
            return sorted;
        }

        Comparator<Hero> comp;
        switch (strategy) {
            case RARITY_DESC:
                comp = (a, b) -> {
                    int c = Integer.compare(b.getStars(), a.getStars());
                    return (c != 0) ? c : a.getName().compareToIgnoreCase(b.getName());
                };
                break;

            case RARITY_ASC:
                comp = (a, b) -> {
                    int c = Integer.compare(a.getStars(), b.getStars());
                    return (c != 0) ? c : a.getName().compareToIgnoreCase(b.getName());
                };
                break;

            case HP_DESC:
                comp = (a, b) -> {
                    int c = Integer.compare(b.getTotalHp(), a.getTotalHp());
                    return (c != 0) ? c : a.getName().compareToIgnoreCase(b.getName());
                };
                break;

            case ATK_DESC:
                comp = (a, b) -> {
                    int c = Integer.compare(b.getTotalAttack(), a.getTotalAttack());
                    return (c != 0) ? c : a.getName().compareToIgnoreCase(b.getName());
                };
                break;

            case DEF_DESC:
                comp = (a, b) -> {
                    int c = Integer.compare(b.getTotalDefense(), a.getTotalDefense());
                    return (c != 0) ? c : a.getName().compareToIgnoreCase(b.getName());
                };
                break;

            case CRIT_DESC:
                comp = (a, b) -> {
                    int c = Integer.compare(b.getTotalCritChance(), a.getTotalCritChance());
                    return (c != 0) ? c : a.getName().compareToIgnoreCase(b.getName());
                };
                break;

            case NAME_ASC:
                comp = (a, b) -> a.getName().compareToIgnoreCase(b.getName());
                break;

            case CLASS_ORDER:
                comp = (a, b) -> {
                    int c = a.getCharacterClass().name().compareTo(b.getCharacterClass().name());
                    return (c != 0) ? c : Integer.compare(b.getStars(), a.getStars());
                };
                break;

            default:
                return sorted;
        }

        // DSA: Uses TreeSort (Binary Search Tree In-Order Traversal)
        return TreeSort.sort(sorted, comp);
    }

    /**
     * Filters heroes by class, star rating, and element using LinkedList linear scan.
     * Passing null or "ALL" skips that filter dimension.
     */
    public static LinkedList<Hero> filter(LinkedList<Hero> heroes, CharacterClass classFilter,
                                          Integer starFilter, String elementFilter) {
        LinkedList<Hero> result = new LinkedList<>();
        if (heroes == null) return result;

        for (Hero h : heroes) {
            // Class filter
            if (classFilter != null && h.getCharacterClass() != classFilter) {
                continue;
            }
            // Star filter
            if (starFilter != null && starFilter > 0 && h.getStars() != starFilter) {
                continue;
            }
            // Element filter
            if (elementFilter != null && !elementFilter.equalsIgnoreCase("ALL")
                    && !elementFilter.trim().isEmpty()) {
                if (!h.getElementalPower().equalsIgnoreCase(elementFilter.trim())) {
                    continue;
                }
            }
            result.add(h);
        }
        return result;
    }
}
