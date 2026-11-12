package managers;

import characters.CharacterClass;
import characters.Hero;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * CharacterDataManager – loads all hero templates from CSV files and provides
 * lookup / filtering / editing methods for the rest of the game.
 *
 * OOP Concept – SINGLE RESPONSIBILITY & ENCAPSULATION
 * ---------------------------------------------------
 * Central data source and catalog manager for heroes across all 7 classes.
 *
 * DSA Concepts used here
 * -----------------------
 * TreeMap<CharacterClass, LinkedList<Hero>> heroesByClass:
 *   Maps each class enum to a list of its heroes with O(log n) class lookup.
 * LinkedList<Hero> allHeroes:
 *   Flat list of every hero across all classes for linear search and iteration.
 */
public class CharacterDataManager {

    // File paths for each class-specific CSV
    private static final String WARRIORS_CSV  = "data/warriors.csv";
    private static final String MAGES_CSV     = "data/mages.csv";
    private static final String ASSASSINS_CSV = "data/assassins.csv";
    private static final String TANKS_CSV     = "data/tanks.csv";
    private static final String RANGERS_CSV   = "data/rangers.csv";
    private static final String SUPPORTS_CSV  = "data/supports.csv";
    private static final String BRAWLERS_CSV  = "data/brawlers.csv";

    // DSA: TreeMap stores heroes organized by their CharacterClass.
    private final TreeMap<CharacterClass, LinkedList<Hero>> heroesByClass = new TreeMap<>();

    // DSA: Flat LinkedList of all heroes regardless of class.
    private final LinkedList<Hero> allHeroes = new LinkedList<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public CharacterDataManager() {
        for (CharacterClass cc : CharacterClass.values()) {
            heroesByClass.put(cc, new LinkedList<>());
        }
        loadAllClasses();
    }

    // -------------------------------------------------------------------------
    // CSV Loading
    // -------------------------------------------------------------------------

    /**
     * Loads all seven character class CSV files in one go.
     */
    public void loadAllClasses() {
        for (CharacterClass cc : CharacterClass.values()) {
            heroesByClass.get(cc).clear();
        }
        allHeroes.clear();

        loadClassCsv(WARRIORS_CSV,  CharacterClass.WARRIOR);
        loadClassCsv(MAGES_CSV,     CharacterClass.MAGE);
        loadClassCsv(ASSASSINS_CSV, CharacterClass.ASSASSIN);
        loadClassCsv(TANKS_CSV,     CharacterClass.TANK);
        loadClassCsv(RANGERS_CSV,   CharacterClass.RANGER);
        loadClassCsv(SUPPORTS_CSV,  CharacterClass.SUPPORT);
        loadClassCsv(BRAWLERS_CSV,  CharacterClass.BRAWLER);
    }

    /**
     * Reads one CSV file and creates Hero objects from every data row.
     *
     * The CSV can have two formats:
     *   10-column (full):   id, name, title, elemental_power, stars, hp, attack, defense, crit, desc
     *   8-column (legacy):  id, name, stars, hp, attack, defense, crit, desc  (no title/element)
     *
     * Parsed heroes are added to:
     *   – heroesByClass.get(characterClass)  (the class-specific LinkedList)
     *   – allHeroes                           (the flat "all heroes" LinkedList)
     *
     * @param filePath       Path to the CSV file (relative to the project root).
     * @param characterClass The class that every hero in this file belongs to.
     */
    private void loadClassCsv(String filePath, CharacterClass characterClass) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("[CharacterDataManager] Missing character file: " + filePath);
            return; // skip missing files gracefully
        }

        // try-with-resources: reader closes itself even if an exception occurs
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true; // flag to skip the header row

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // skip blank lines
                if (first) { first = false; continue; } // skip CSV header row

                // Split into at most 10 parts (description may contain commas)
                String[] p = line.split(",", 10);
                if (p.length < 8) continue; // malformed row — skip it

                Hero hero;

                if (p.length >= 10) {
                    // Full 10-column format: title and elemental_power are present
                    String id            = p[0].trim();
                    String name          = p[1].trim();
                    String title         = p[2].trim();
                    String elementalPower = p[3].trim();
                    int    stars         = parseIntSafe(p[4].trim(), 3);
                    int    hp            = parseIntSafe(p[5].trim(), 100);
                    int    attack        = parseIntSafe(p[6].trim(), 10);
                    int    defense       = parseIntSafe(p[7].trim(), 10);
                    int    critChance    = parseIntSafe(p[8].trim(), 5);
                    String desc          = cleanDescription(p[9]);

                    hero = new Hero(id, name, title, elementalPower,
                            characterClass, stars, hp, attack, defense, critChance, desc);

                } else {
                    // Legacy 8-column format: no title or elemental_power columns
                    String id         = p[0].trim();
                    String name       = p[1].trim();
                    int    stars      = parseIntSafe(p[2].trim(), 3);
                    int    hp         = parseIntSafe(p[3].trim(), 100);
                    int    attack     = parseIntSafe(p[4].trim(), 10);
                    int    defense    = parseIntSafe(p[5].trim(), 10);
                    int    critChance = parseIntSafe(p[6].trim(), 5);
                    String desc       = cleanDescription(p[7]);

                    // Use empty title and "None" element as defaults
                    hero = new Hero(id, name, "", "None",
                            characterClass, stars, hp, attack, defense, critChance, desc);
                }

                // Add to the class-specific list (O(1) LinkedList add-to-tail)
                heroesByClass.get(characterClass).add(hero);
                // Also add to the flat all-heroes list
                allHeroes.add(hero);
            }

        } catch (IOException e) {
            System.err.println("[CharacterDataManager] Failed to load " + filePath
                    + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Public Query Methods
    // -------------------------------------------------------------------------

    /**
     * Returns the full list of every hero loaded across all classes.
     *
     * @return  The allHeroes LinkedList (direct reference — do not modify).
     */
    public LinkedList<Hero> getAllHeroes() {
        return allHeroes;
    }

    /**
     * Returns all heroes belonging to the given class.
     *
     * DSA note – TreeMap.get(): O(log n) lookup by CharacterClass key.
     *
     * @param characterClass  The class to filter by.
     * @return                LinkedList of heroes, or an empty list if none found.
     */
    public LinkedList<Hero> getHeroesByClass(CharacterClass characterClass) {
        LinkedList<Hero> list = heroesByClass.get(characterClass);
        return list != null ? list : new LinkedList<>();
    }

    /**
     * Returns all heroes that have exactly the given star rating.
     *
     * DSA note – Linear Search O(n):
     *   We iterate the entire allHeroes LinkedList and keep only matching ones.
     *   There is no pre-built index for star rating, so linear search is used.
     *
     * @param stars  The desired star level (1–6).
     * @return       A new LinkedList containing only heroes with that star level.
     */
    public LinkedList<Hero> getHeroesByStars(int stars) {
        LinkedList<Hero> result = new LinkedList<>();
        for (Hero h : allHeroes) {
            if (h.getStars() == stars) {
                result.add(h); // keep heroes that match the star tier
            }
        }
        return result;
    }

    /**
     * Finds a hero template by its unique ID string.
     *
     * DSA note – Linear Search O(n):
     *   Walks allHeroes from head to tail until a match is found.
     *   Returns null if no hero has the given ID.
     *
     * @param id  The hero ID to search for (case-insensitive).
     * @return    The matching Hero template, or null.
     */
    public Hero getHeroTemplateById(String id) {
        if (id == null) return null;
        for (Hero h : allHeroes) {
            if (h.getId().equalsIgnoreCase(id)) {
                return h; // found it — return immediately (early exit)
            }
        }
        return null; // not found
    }

    /**
     * Creates a brand-new Hero instance from a template ID.
     *
     * Why copy instead of using the template directly?
     *   The template heroes in allHeroes have no equipment.  If we gave the
     *   player the template object itself, equipping gear would modify the
     *   shared template — every future hero of that type would appear to be
     *   already equipped.  Copying gives each summoned hero its own fresh
     *   equipment slots (all null / empty).
     *
     * @param id  The hero ID to create a new instance of.
     * @return    A fresh Hero with the same stats but empty equipment slots,
     *            or null if the ID is not found.
     */
    public Hero createNewHeroInstance(String id) {
        Hero template = getHeroTemplateById(id);
        if (template == null) return null;

        // Create a completely new Hero object — equipment slots are blank by default
        return new Hero(
                template.getId(),
                template.getName(),
                template.getTitle(),
                template.getElementalPower(),
                template.getCharacterClass(),
                template.getStars(),
                template.getBaseHp(),
                template.getBaseAttack(),
                template.getBaseDefense(),
                template.getBaseCritChance(),
                template.getDescription()
        );
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Safely parses a String into an int, returning a default value if the
     * string is not a valid number (prevents NumberFormatException crashes on
     * bad CSV data).
     *
     * @param s           The string to parse.
     * @param defaultVal  Value to return if parsing fails.
     * @return            Parsed int or defaultVal.
     */
    private int parseIntSafe(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * Cleans double quotes and outer quotes from CSV description fields.
     *
     * @param raw The raw description string from CSV.
     * @return Cleaned description without extraneous double quotes.
     */
    private String cleanDescription(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        while (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s.replace("\"\"", "\"").replace("\"", "'").trim();
    }

    // -------------------------------------------------------------------------
    // Admin Catalog Management (Add / Edit / Remove / Save)
    // -------------------------------------------------------------------------

    /**
     * Returns the CSV file path corresponding to the given CharacterClass.
     */
    public String getCsvPathForClass(CharacterClass characterClass) {
        if (characterClass == null) return null;
        switch (characterClass) {
            case WARRIOR:  return WARRIORS_CSV;
            case MAGE:     return MAGES_CSV;
            case ASSASSIN: return ASSASSINS_CSV;
            case TANK:     return TANKS_CSV;
            case RANGER:   return RANGERS_CSV;
            case SUPPORT:  return SUPPORTS_CSV;
            case BRAWLER:  return BRAWLERS_CSV;
            default:       return null;
        }
    }

    /**
     * Saves all heroes of a specific class back to their respective CSV file.
     *
     * @param characterClass The class to persist.
     * @return true if written successfully; false otherwise.
     */
    public boolean saveClassCsv(CharacterClass characterClass) {
        String path = getCsvPathForClass(characterClass);
        if (path == null) return false;

        LinkedList<Hero> list = heroesByClass.get(characterClass);
        if (list == null) list = new LinkedList<>();

        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, false))) {
            writer.write("id,name,title,elemental_power,stars,hp,attack,defense,crit_chance,description");
            writer.newLine();

            for (Hero h : list) {
                writer.write(h.getId() + "," +
                        escapeCsv(h.getName()) + "," +
                        escapeCsv(h.getTitle()) + "," +
                        escapeCsv(h.getElementalPower()) + "," +
                        h.getStars() + "," +
                        h.getBaseHp() + "," +
                        h.getBaseAttack() + "," +
                        h.getBaseDefense() + "," +
                        h.getBaseCritChance() + "," +
                        escapeCsv(h.getDescription()));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("[CharacterDataManager] Failed to save " + path + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves all hero classes to their respective CSV files.
     */
    public void saveAllHeroesToCsv() {
        for (CharacterClass cc : CharacterClass.values()) {
            saveClassCsv(cc);
        }
    }

    /**
     * Adds a new hero template to the catalog and saves to CSV.
     *
     * @param hero The new hero template.
     * @return true if added; false if hero ID is duplicate or invalid.
     */
    public boolean addHero(Hero hero) {
        if (hero == null || hero.getId() == null || hero.getId().trim().isEmpty()) {
            return false;
        }
        if (getHeroTemplateById(hero.getId()) != null) {
            return false; // duplicate ID
        }

        CharacterClass cc = hero.getCharacterClass();
        heroesByClass.get(cc).add(hero);
        allHeroes.add(hero);
        saveClassCsv(cc);
        return true;
    }

    /**
     * Updates an existing hero template in the catalog and rewrites the CSV.
     *
     * @param oldHeroId  The ID of the hero to update.
     * @param updatedHero The updated Hero object.
     * @return true if updated successfully; false if old hero was not found.
     */
    public boolean updateHero(String oldHeroId, Hero updatedHero) {
        if (oldHeroId == null || updatedHero == null) return false;

        Hero oldHero = getHeroTemplateById(oldHeroId);
        if (oldHero == null) return false;

        CharacterClass oldClass = oldHero.getCharacterClass();
        CharacterClass newClass = updatedHero.getCharacterClass();

        // Update in allHeroes
        int allIdx = allHeroes.indexOf(oldHero);
        if (allIdx >= 0) {
            allHeroes.set(allIdx, updatedHero);
        } else {
            allHeroes.add(updatedHero);
        }

        // Update in class list
        LinkedList<Hero> oldList = heroesByClass.get(oldClass);
        if (oldList != null) {
            oldList.remove(oldHero);
        }
        heroesByClass.get(newClass).add(updatedHero);

        saveClassCsv(oldClass);
        if (oldClass != newClass) {
            saveClassCsv(newClass);
        }
        return true;
    }

    /**
     * Removes a hero template from the catalog and updates the CSV.
     *
     * @param id The hero ID to remove.
     * @return true if found and removed; false otherwise.
     */
    public boolean removeHero(String id) {
        if (id == null) return false;
        Hero hero = getHeroTemplateById(id);
        if (hero == null) return false;

        CharacterClass cc = hero.getCharacterClass();
        allHeroes.remove(hero);
        LinkedList<Hero> classList = heroesByClass.get(cc);
        if (classList != null) {
            classList.remove(hero);
        }
        saveClassCsv(cc);
        return true;
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
