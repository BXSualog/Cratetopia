package managers;

import items.Accessory;
import items.Armor;
import items.Item;
import items.Weapon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and provides access to all item pools from CSV files.
 * Each category (weapons, armor, accessories) is stored in its own CSV under data/.
 *
 * CSV paths:
 *   data/weapons.csv
 *   data/armor.csv
 *   data/accessories.csv
 */
public class ItemDataManager {
    private static final String WEAPONS_CSV     = "data/weapons.csv";
    private static final String ARMOR_CSV       = "data/armor.csv";
    private static final String ACCESSORIES_CSV = "data/accessories.csv";

    private final List<Weapon>    weapons     = new ArrayList<>();
    private final List<Armor>     armors      = new ArrayList<>();
    private final List<Accessory> accessories = new ArrayList<>();

    public ItemDataManager() {
        loadWeapons();
        loadArmors();
        loadAccessories();
    }

    // -------------------------------------------------------------------------
    // Public accessors
    // -------------------------------------------------------------------------

    /** Returns all weapons of the given rarity. */
    public List<Weapon> getWeaponsByRarity(String rarity) {
        List<Weapon> result = new ArrayList<>();
        for (Weapon w : weapons) {
            if (w.getRarity().equalsIgnoreCase(rarity)) result.add(w);
        }
        return result;
    }

    /** Returns all armors of the given rarity. */
    public List<Armor> getArmorsByRarity(String rarity) {
        List<Armor> result = new ArrayList<>();
        for (Armor a : armors) {
            if (a.getRarity().equalsIgnoreCase(rarity)) result.add(a);
        }
        return result;
    }

    /** Returns all accessories of the given rarity. */
    public List<Accessory> getAccessoriesByRarity(String rarity) {
        List<Accessory> result = new ArrayList<>();
        for (Accessory ac : accessories) {
            if (ac.getRarity().equalsIgnoreCase(rarity)) result.add(ac);
        }
        return result;
    }

    /** Returns all items (any type) of the given rarity pooled together. */
    public List<Item> getAllEquipmentByRarity(String rarity) {
        List<Item> result = new ArrayList<>();
        result.addAll(getWeaponsByRarity(rarity));
        result.addAll(getArmorsByRarity(rarity));
        result.addAll(getAccessoriesByRarity(rarity));
        return result;
    }

    // -------------------------------------------------------------------------
    // CSV loaders
    // -------------------------------------------------------------------------

    private void loadWeapons() {
        // Header: id,name,rarity,type,attack,crit_chance,description
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(WEAPONS_CSV)))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header
                String[] p = line.split(",", 7);
                if (p.length < 7) continue;
                weapons.add(new Weapon(
                        p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                        parseIntSafe(p[4].trim()), parseIntSafe(p[5].trim()),
                        p[6].trim()
                ));
            }
        } catch (IOException e) {
            System.err.println("[ItemDataManager] Failed to load weapons: " + e.getMessage());
        }
    }

    private void loadArmors() {
        // Header: id,name,rarity,type,defense,health_bonus,description
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(ARMOR_CSV)))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header
                String[] p = line.split(",", 7);
                if (p.length < 7) continue;
                armors.add(new Armor(
                        p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                        parseIntSafe(p[4].trim()), parseIntSafe(p[5].trim()),
                        p[6].trim()
                ));
            }
        } catch (IOException e) {
            System.err.println("[ItemDataManager] Failed to load armors: " + e.getMessage());
        }
    }

    private void loadAccessories() {
        // Header: id,name,rarity,type,stat_bonus,bonus_type,description
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(ACCESSORIES_CSV)))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header
                String[] p = line.split(",", 7);
                if (p.length < 7) continue;
                accessories.add(new Accessory(
                        p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                        parseIntSafe(p[4].trim()), p[5].trim(),
                        p[6].trim()
                ));
            }
        } catch (IOException e) {
            System.err.println("[ItemDataManager] Failed to load accessories: " + e.getMessage());
        }
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }
}
