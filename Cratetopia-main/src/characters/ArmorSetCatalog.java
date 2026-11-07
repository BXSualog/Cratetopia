package characters;

import items.ArmorSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

/**
 * ArmorSetCatalog – a registry (lookup table) of all named armor sets.
 *
 * OOP Concept – ENCAPSULATION & SINGLE RESPONSIBILITY
 * -----------------------------------------------------
 * This class has one job: load the armor-set definitions from a CSV file and
 * let other classes look them up by name.  Hero uses it to resolve the set
 * bonus when a full 3-piece set is equipped.
 *
 * DSA Concept – TreeMap (Sorted Map / BST-backed dictionary)
 * -----------------------------------------------------------
 * catalog is a TreeMap<String, ArmorSet>.
 *
 * A TreeMap stores key-value pairs in sorted order by key (alphabetical for
 * Strings) and is backed by a Red-Black Tree (a type of self-balancing BST).
 *
 *   Lookup by key (getSet):  O(log n)  ← faster than linear search through a list
 *   Insertion (registerSet): O(log n)
 *
 * The constructor is given String.CASE_INSENSITIVE_ORDER so "dragon" and
 * "Dragon" map to the same entry.
 *
 * Loaded from: data/armor_sets.csv
 * CSV header:  set_name, bonus_title, bonus_defense, bonus_hp_percent, bonus_description
 */
public class ArmorSetCatalog {

    private static final String ARMOR_SETS_CSV = "data/armor_sets.csv";

    // DSA: TreeMap — key = set name (case-insensitive), value = ArmorSet definition
    private final TreeMap<String, ArmorSet> catalog =
            new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Constructor – immediately loads all set definitions from the CSV file.
     * If the file is missing, the catalog remains empty (no set bonuses).
     */
    public ArmorSetCatalog() {
        loadFromCsv();
    }

    // -------------------------------------------------------------------------
    // CSV Loader
    // -------------------------------------------------------------------------

    /**
     * Reads data/armor_sets.csv line by line and stores each row as an
     * ArmorSet in the TreeMap catalog.
     *
     * Why BufferedReader?
     *   It wraps a FileReader and reads text in chunks (buffered), which is
     *   much faster than reading one character at a time.
     */
    private void loadFromCsv() {
        File file = new File(ARMOR_SETS_CSV);
        if (!file.exists()) {
            // Not a crash — just warn and continue with an empty catalog.
            System.err.println("[ArmorSetCatalog] " + ARMOR_SETS_CSV
                    + " not found — no set bonuses loaded.");
            return;
        }

        // try-with-resources: the BufferedReader is closed automatically when done
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true; // used to skip the header row

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // skip blank lines

                if (first) {
                    first = false; // skip the CSV header (set_name,bonus_title,…)
                    continue;
                }

                // Split into at most 5 columns so bonus_description can contain commas
                String[] p = line.split(",", 5);
                if (p.length < 4) continue; // malformed row — skip it

                String setName          = p[0].trim();
                String bonusTitle       = p[1].trim();
                int    bonusDefense     = parseIntSafe(p[2].trim()); // convert "20" → 20
                int    bonusHpPercent   = parseIntSafe(p[3].trim()); // convert "15" → 15
                // If no description column exists, fall back to the bonus title
                String bonusDescription = (p.length >= 5) ? p[4].trim() : bonusTitle;

                // Store in the TreeMap: O(log n) insertion
                catalog.put(setName,
                        new ArmorSet(setName, bonusTitle, bonusDescription,
                                bonusDefense, bonusHpPercent));
            }

        } catch (IOException e) {
            System.err.println("[ArmorSetCatalog] Failed to load " + ARMOR_SETS_CSV
                    + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Public API – used by Hero to resolve set bonuses
    // -------------------------------------------------------------------------

    /**
     * Looks up an ArmorSet by its set name.
     *
     * DSA note – TreeMap.get() runs in O(log n) using the BST.
     * The CASE_INSENSITIVE_ORDER comparator means "dragon" == "Dragon" == "DRAGON".
     *
     * @param setName  The set name to search for (e.g. "Dragon").
     * @return         The ArmorSet definition, or null if not registered.
     */
    public ArmorSet getSet(String setName) {
        if (setName == null) return null;
        return catalog.get(setName); // O(log n) BST lookup
    }

    /**
     * Returns the entire catalog TreeMap (used by admin/debug features).
     */
    public TreeMap<String, ArmorSet> getCatalog() {
        return catalog;
    }

    /**
     * Registers (or replaces) a set definition at runtime.
     * Useful for admin tools that want to add or modify set bonuses without
     * restarting the game.
     *
     * @param armorSet  The set definition to register.
     */
    public void registerSet(ArmorSet armorSet) {
        if (armorSet != null) {
            catalog.put(armorSet.getSetName(), armorSet); // O(log n) insert
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Safely parses a String into an int.
     * Returns 0 if the string is not a valid number (avoids crashing on bad CSV data).
     *
     * @param s  The string to parse (e.g. "20", "15").
     * @return   The parsed integer, or 0 on failure.
     */
    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0; // safe default when the CSV cell is empty or non-numeric
        }
    }
}
