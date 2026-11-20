package characters;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * HeroSort – non-tree-based multi-level sorting and filtering for hero roster.
 *
 * OOP Concept: Strategy Pattern via Enum with custom Comparator implementations.
 * DSA Concept: Divide-and-Conquer Merge Sort on LinkedList with O(n log n) time complexity,
 *              strictly non-tree-based to satisfy CC 204 project rubrics.
 *              Implements explicit multi-level comparison (Primary criterion + Secondary tie-breaker).
 */
public enum HeroSort {
    DEFAULT("Default (Roster Order)", null, "Original Order", "None"),
    RARITY_DESC("Star Level (6★ → 1★)",
            (a, b) -> {
                int cmp = Integer.compare(b.getStars(), a.getStars()); // Level 1: Stars descending
                return (cmp != 0) ? cmp : a.getName().compareToIgnoreCase(b.getName()); // Level 2: Name A-Z
            },
            "Star Rating (Descending)", "Hero Name (A → Z)"),
    RARITY_ASC("Star Level (1★ → 6★)",
            (a, b) -> {
                int cmp = Integer.compare(a.getStars(), b.getStars()); // Level 1: Stars ascending
                return (cmp != 0) ? cmp : a.getName().compareToIgnoreCase(b.getName()); // Level 2: Name A-Z
            },
            "Star Rating (Ascending)", "Hero Name (A → Z)"),
    HP_DESC("Total HP (Highest First)",
            (a, b) -> {
                int cmp = Integer.compare(b.getTotalHp(), a.getTotalHp()); // Level 1: Total HP descending
                return (cmp != 0) ? cmp : Integer.compare(b.getStars(), a.getStars()); // Level 2: Stars descending
            },
            "Total HP (Highest First)", "Star Rating (Descending)"),
    ATK_DESC("Total Attack (Highest First)",
            (a, b) -> {
                int cmp = Integer.compare(b.getTotalAttack(), a.getTotalAttack()); // Level 1: Total Attack descending
                return (cmp != 0) ? cmp : Integer.compare(b.getStars(), a.getStars()); // Level 2: Stars descending
            },
            "Total Attack (Highest First)", "Star Rating (Descending)"),
    DEF_DESC("Total Defense (Highest First)",
            (a, b) -> {
                int cmp = Integer.compare(b.getTotalDefense(), a.getTotalDefense()); // Level 1: Total Defense descending
                return (cmp != 0) ? cmp : Integer.compare(b.getStars(), a.getStars()); // Level 2: Stars descending
            },
            "Total Defense (Highest First)", "Star Rating (Descending)"),
    CRIT_DESC("Total Crit % (Highest First)",
            (a, b) -> {
                int cmp = Integer.compare(b.getTotalCritChance(), a.getTotalCritChance()); // Level 1: Crit % descending
                return (cmp != 0) ? cmp : Integer.compare(b.getTotalAttack(), a.getTotalAttack()); // Level 2: Attack descending
            },
            "Total Crit % (Highest First)", "Total Attack (Descending)"),
    NAME_ASC("Hero Name (A → Z)",
            (a, b) -> {
                int cmp = a.getName().compareToIgnoreCase(b.getName()); // Level 1: Name A-Z
                return (cmp != 0) ? cmp : Integer.compare(b.getStars(), a.getStars()); // Level 2: Stars descending
            },
            "Hero Name (A → Z)", "Star Rating (Descending)"),
    CLASS_ORDER("Class Grouping",
            (a, b) -> {
                int cmp = a.getCharacterClass().name().compareTo(b.getCharacterClass().name()); // Level 1: Class name
                return (cmp != 0) ? cmp : Integer.compare(b.getStars(), a.getStars()); // Level 2: Stars descending
            },
            "Character Class (Grouped)", "Star Rating (Descending)");

    private final String displayName;
    private final Comparator<Hero> comparator;
    private final String primaryCriterion;
    private final String secondaryCriterion;

    HeroSort(String displayName, Comparator<Hero> comparator, String primaryCriterion, String secondaryCriterion) {
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

    public Comparator<Hero> getComparator() {
        return comparator;
    }

    /**
     * Sorts heroes using Merge Sort (Non-tree-based divide-and-conquer algorithm).
     * Time Complexity: O(n log n) guaranteed, Space Complexity: O(n).
     */
    public static LinkedList<Hero> sort(LinkedList<Hero> heroes, HeroSort strategy) {
        if (heroes == null || heroes.size() <= 1 || strategy == null || strategy.comparator == null) {
            return new LinkedList<>(heroes != null ? heroes : new LinkedList<>());
        }
        return mergeSort(new LinkedList<>(heroes), strategy.comparator);
    }

    /**
     * Recursive Merge Sort implementation operating on LinkedList.
     */
    private static LinkedList<Hero> mergeSort(LinkedList<Hero> list, Comparator<Hero> cmp) {
        if (list.size() <= 1) {
            return list;
        }

        int mid = list.size() / 2;
        LinkedList<Hero> left = new LinkedList<>();
        LinkedList<Hero> right = new LinkedList<>();

        int i = 0;
        for (Hero h : list) {
            if (i < mid) {
                left.add(h);
            } else {
                right.add(h);
            }
            i++;
        }

        left = mergeSort(left, cmp);
        right = mergeSort(right, cmp);

        return merge(left, right, cmp);
    }

    /**
     * Merge step of Merge Sort algorithm.
     */
    private static LinkedList<Hero> merge(LinkedList<Hero> left, LinkedList<Hero> right, Comparator<Hero> cmp) {
        LinkedList<Hero> merged = new LinkedList<>();
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
