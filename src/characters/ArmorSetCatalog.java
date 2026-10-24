package characters;

import items.ArmorSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

/**
 * Registry of all defined armor sets in the game.
 *
 * Loaded from data/armor_sets.csv at startup.
 *
 * CSV header:
 *   set_name,bonus_title,bonus_defense,bonus_hp_percent,bonus_description
 *
 * set_name         : Must match the set_name column in armor.csv (case-insensitive).
 * bonus_title      : Short display name for the set bonus (e.g. "Dragon's Fortitude").
 * bonus_defense    : Flat defense points added when the full 3-piece set is equipped.
 * bonus_hp_percent : Percentage HP added when the full 3-piece set is equipped.
 * bonus_description: Short readable description shown on the hero inspect screen
 *                    (e.g. "+20 DEF & +15% HP").
 *
 * Uses TreeMap<String, ArmorSet> for ordered, key-based lookup — consistent
 * with the DSA TreeMap pattern used throughout the project.
 */
public class ArmorSetCatalog {

    private static final String ARMOR_SETS_CSV = "data/armor_sets.csv";

    private final TreeMap<String, ArmorSet> catalog = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /** Loads the armor set catalog from data/armor_sets.csv. */
    public ArmorSetCatalog() {
        loadFromCsv();
    }

    // -------------------------------------------------------------------------
    // CSV Loader
    // -------------------------------------------------------------------------

    private void loadFromCsv() {
        // Header: set_name,bonus_title,bonus_defense,bonus_hp_percent,bonus_description
        File file = new File(ARMOR_SETS_CSV);
        if (!file.exists()) {
            System.err.println("[ArmorSetCatalog] " + ARMOR_SETS_CSV + " not found — no set bonuses loaded.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header

                // Split into at most 5 parts so bonus_description can contain commas
                String[] p = line.split(",", 5);
                if (p.length < 4) continue;

                String setName          = p[0].trim();
                String bonusTitle       = p[1].trim();
                int    bonusDefense     = parseIntSafe(p[2].trim());
                int    bonusHpPercent   = parseIntSafe(p[3].trim());
                String bonusDescription = (p.length >= 5) ? p[4].trim() : bonusTitle;

                catalog.put(setName, new ArmorSet(setName, bonusTitle, bonusDescription,
                        bonusDefense, bonusHpPercent));
            }
        } catch (IOException e) {
            System.err.println("[ArmorSetCatalog] Failed to load " + ARMOR_SETS_CSV + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Registry access
    // -------------------------------------------------------------------------

    /**
     * Returns the ArmorSet for the given set name, or null if not found.
     */
    public ArmorSet getSet(String setName) {
        if (setName == null) return null;
        return catalog.get(setName);
    }

    /**
     * Returns the full catalog (for admin display / management).
     */
    public TreeMap<String, ArmorSet> getCatalog() {
        return catalog;
    }

    /**
     * Registers or replaces a set definition at runtime.
     * Used by the admin to configure custom set bonuses.
     */
    public void registerSet(ArmorSet armorSet) {
        if (armorSet != null) {
            catalog.put(armorSet.getSetName(), armorSet);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }
}
