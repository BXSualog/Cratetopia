package managers;

import items.Accessory;
import items.Armor;
import items.Item;
import items.Weapon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

/**
 * Loads and provides access to all item pools from CSV files.
 * Adheres strictly to DSA constraints using LinkedList collections.
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

    private final LinkedList<Weapon>    weapons     = new LinkedList<>();
    private final LinkedList<Armor>     armors      = new LinkedList<>();
    private final LinkedList<Accessory> accessories = new LinkedList<>();

    public ItemDataManager() {
        loadWeapons();
        loadArmors();
        loadAccessories();
    }

    // -------------------------------------------------------------------------
    // Public accessors
    // -------------------------------------------------------------------------

    public LinkedList<Weapon> getWeapons() {
        return weapons;
    }

    public LinkedList<Armor> getArmors() {
        return armors;
    }

    public LinkedList<Accessory> getAccessories() {
        return accessories;
    }

    /** Returns all weapons of the given rarity. */
    public LinkedList<Weapon> getWeaponsByRarity(String rarity) {
        LinkedList<Weapon> result = new LinkedList<>();
        for (Weapon w : weapons) {
            if (w.getRarity().equalsIgnoreCase(rarity)) result.add(w);
        }
        return result;
    }

    /** Returns all armors of the given rarity. */
    public LinkedList<Armor> getArmorsByRarity(String rarity) {
        LinkedList<Armor> result = new LinkedList<>();
        for (Armor a : armors) {
            if (a.getRarity().equalsIgnoreCase(rarity)) result.add(a);
        }
        return result;
    }

    /** Returns all accessories of the given rarity. */
    public LinkedList<Accessory> getAccessoriesByRarity(String rarity) {
        LinkedList<Accessory> result = new LinkedList<>();
        for (Accessory ac : accessories) {
            if (ac.getRarity().equalsIgnoreCase(rarity)) result.add(ac);
        }
        return result;
    }

    /** Returns all items (any type) of the given rarity pooled together. */
    public LinkedList<Item> getAllEquipmentByRarity(String rarity) {
        LinkedList<Item> result = new LinkedList<>();
        result.addAll(getWeaponsByRarity(rarity));
        result.addAll(getArmorsByRarity(rarity));
        result.addAll(getAccessoriesByRarity(rarity));
        return result;
    }

    public Item getItemById(String id) {
        if (id == null) return null;
        for (Weapon w : weapons) {
            if (w.getId().equalsIgnoreCase(id)) return w;
        }
        for (Armor a : armors) {
            if (a.getId().equalsIgnoreCase(id)) return a;
        }
        for (Accessory ac : accessories) {
            if (ac.getId().equalsIgnoreCase(id)) return ac;
        }
        return null;
    }

    /**
     * Creates a fresh instance of an item by template ID for player inventories or equipment slots.
     */
    public Item createNewItemInstance(String id) {
        Item template = getItemById(id);
        if (template == null) return null;
        if (template instanceof Weapon) {
            Weapon w = (Weapon) template;
            return new Weapon(w.getId(), w.getName(), w.getRarity(), w.getType(),
                    w.getAttack(), w.getCritChance(), w.getEquippableClassesDisplay(), w.getDescription());
        } else if (template instanceof Armor) {
            Armor a = (Armor) template;
            return new Armor(a.getId(), a.getName(), a.getRarity(), a.getType(),
                    a.getSetName(), a.getDefense(), a.getHealthBonus(), a.getHealthPercentBonus(),
                    a.getEquippableClassesDisplay(), a.getDescription());
        } else if (template instanceof Accessory) {
            Accessory ac = (Accessory) template;
            return new Accessory(ac.getId(), ac.getName(), ac.getRarity(), ac.getType(),
                    ac.getStatBonus(), ac.getBonusType(), ac.getEquippableClassesDisplay(), ac.getDescription());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // CSV loaders
    // -------------------------------------------------------------------------

    private void loadWeapons() {
        // Header: id,name,rarity,type,attack,crit_chance,equippable_classes,description
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(WEAPONS_CSV), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header
                String[] p = line.split(",", 8);
                if (p.length < 7) continue;

                if (p.length == 7) {
                    weapons.add(new Weapon(
                            p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                            parseIntSafe(p[4].trim()), parseIntSafe(p[5].trim()),
                            "All", p[6].trim()
                    ));
                } else {
                    weapons.add(new Weapon(
                            p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                            parseIntSafe(p[4].trim()), parseIntSafe(p[5].trim()),
                            p[6].trim(), p[7].trim()
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("[ItemDataManager] Failed to load weapons: " + e.getMessage());
        }
    }

    private void loadArmors() {
        // Header: id,name,rarity,type,set_name,defense,health_bonus,health_percentage_bonus,equippable_classes,description
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(ARMOR_CSV), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header
                // Split into at most 10 parts
                String[] p = line.split(",", 10);
                if (p.length < 9) continue;

                // p[0]=id, p[1]=name, p[2]=rarity, p[3]=type, p[4]=set_name,
                // p[5]=defense, p[6]=health_bonus, p[7]=health_percentage_bonus,
                // p[8]=equippable_classes, p[9]=description
                if (p.length == 9) {
                    // No description column — use empty string
                    armors.add(new Armor(
                            p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                            p[4].trim(),
                            parseIntSafe(p[5].trim()), parseIntSafe(p[6].trim()),
                            parseIntSafe(p[7].trim()),
                            p[8].trim(), ""
                    ));
                } else {
                    armors.add(new Armor(
                            p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                            p[4].trim(),
                            parseIntSafe(p[5].trim()), parseIntSafe(p[6].trim()),
                            parseIntSafe(p[7].trim()),
                            p[8].trim(), p[9].trim()
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("[ItemDataManager] Failed to load armors: " + e.getMessage());
        }
    }

    private void loadAccessories() {
        // Header: id,name,rarity,type,stat_bonus,bonus_type,equippable_classes,description
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(ACCESSORIES_CSV), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header
                String[] p = line.split(",", 8);
                if (p.length < 7) continue;

                if (p.length == 7) {
                    accessories.add(new Accessory(
                            p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                            parseIntSafe(p[4].trim()), p[5].trim(),
                            "All", p[6].trim()
                    ));
                } else {
                    accessories.add(new Accessory(
                            p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                            parseIntSafe(p[4].trim()), p[5].trim(),
                            p[6].trim(), p[7].trim()
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("[ItemDataManager] Failed to load accessories: " + e.getMessage());
        }
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }

    // -------------------------------------------------------------------------
    // Admin Catalog Management (Add / Edit / Delete / Save)
    // -------------------------------------------------------------------------

    public boolean saveWeapons() {
        File file = new File(WEAPONS_CSV);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, false))) {
            writer.write("id,name,rarity,type,attack,crit_chance,equippable_classes,description");
            writer.newLine();
            for (Weapon w : weapons) {
                writer.write(w.getId() + "," +
                        escapeCsv(w.getName()) + "," +
                        escapeCsv(w.getRarity()) + "," +
                        escapeCsv(w.getType()) + "," +
                        w.getAttack() + "," +
                        w.getCritChance() + "," +
                        escapeCsv(w.getEquippableClassesRaw()) + "," +
                        escapeCsv(w.getDescription()));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("[ItemDataManager] Failed to save weapons: " + e.getMessage());
            return false;
        }
    }

    public boolean saveArmors() {
        File file = new File(ARMOR_CSV);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, false))) {
            writer.write("id,name,rarity,type,set_name,defense,health_bonus,health_percentage_bonus,equippable_classes,description");
            writer.newLine();
            for (Armor a : armors) {
                writer.write(a.getId() + "," +
                        escapeCsv(a.getName()) + "," +
                        escapeCsv(a.getRarity()) + "," +
                        escapeCsv(a.getType()) + "," +
                        escapeCsv(a.getSetName()) + "," +
                        a.getDefense() + "," +
                        a.getHealthBonus() + "," +
                        a.getHealthPercentBonus() + "," +
                        escapeCsv(a.getEquippableClassesRaw()) + "," +
                        escapeCsv(a.getDescription()));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("[ItemDataManager] Failed to save armors: " + e.getMessage());
            return false;
        }
    }

    public boolean saveAccessories() {
        File file = new File(ACCESSORIES_CSV);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, false))) {
            writer.write("id,name,rarity,type,stat_bonus,bonus_type,equippable_classes,description");
            writer.newLine();
            for (Accessory ac : accessories) {
                writer.write(ac.getId() + "," +
                        escapeCsv(ac.getName()) + "," +
                        escapeCsv(ac.getRarity()) + "," +
                        escapeCsv(ac.getType()) + "," +
                        ac.getStatBonus() + "," +
                        escapeCsv(ac.getBonusType()) + "," +
                        escapeCsv(ac.getEquippableClassesRaw()) + "," +
                        escapeCsv(ac.getDescription()));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("[ItemDataManager] Failed to save accessories: " + e.getMessage());
            return false;
        }
    }

    public void saveAllItems() {
        saveWeapons();
        saveArmors();
        saveAccessories();
    }

    public boolean addWeapon(Weapon weapon) {
        if (weapon == null || weapon.getId() == null || getItemById(weapon.getId()) != null) {
            return false;
        }
        weapons.add(weapon);
        return saveWeapons();
    }

    public boolean updateWeapon(String id, Weapon updated) {
        if (id == null || updated == null) return false;
        for (int i = 0; i < weapons.size(); i++) {
            if (weapons.get(i).getId().equalsIgnoreCase(id)) {
                weapons.set(i, updated);
                return saveWeapons();
            }
        }
        return false;
    }

    public boolean addArmor(Armor armor) {
        if (armor == null || armor.getId() == null || getItemById(armor.getId()) != null) {
            return false;
        }
        armors.add(armor);
        return saveArmors();
    }

    public boolean updateArmor(String id, Armor updated) {
        if (id == null || updated == null) return false;
        for (int i = 0; i < armors.size(); i++) {
            if (armors.get(i).getId().equalsIgnoreCase(id)) {
                armors.set(i, updated);
                return saveArmors();
            }
        }
        return false;
    }

    public boolean addAccessory(Accessory accessory) {
        if (accessory == null || accessory.getId() == null || getItemById(accessory.getId()) != null) {
            return false;
        }
        accessories.add(accessory);
        return saveAccessories();
    }

    public boolean updateAccessory(String id, Accessory updated) {
        if (id == null || updated == null) return false;
        for (int i = 0; i < accessories.size(); i++) {
            if (accessories.get(i).getId().equalsIgnoreCase(id)) {
                accessories.set(i, updated);
                return saveAccessories();
            }
        }
        return false;
    }

    public boolean deleteItem(String id) {
        if (id == null) return false;

        for (int i = 0; i < weapons.size(); i++) {
            if (weapons.get(i).getId().equalsIgnoreCase(id)) {
                weapons.remove(i);
                return saveWeapons();
            }
        }
        for (int i = 0; i < armors.size(); i++) {
            if (armors.get(i).getId().equalsIgnoreCase(id)) {
                armors.remove(i);
                return saveArmors();
            }
        }
        for (int i = 0; i < accessories.size(); i++) {
            if (accessories.get(i).getId().equalsIgnoreCase(id)) {
                accessories.remove(i);
                return saveAccessories();
            }
        }
        return false;
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
