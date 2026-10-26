package managers;

import characters.CharacterClass;
import characters.Hero;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * CharacterDataManager – loads all hero templates from CSV files and provides
 * lookup / filtering methods for the rest of the game.
 *
 * OOP Concept – SINGLE RESPONSIBILITY
 * -------------------------------------
 * This class has one job: be the central data source for heroes.
 * CharacterGachaManager asks it "give me a random 3-star hero";
 * PlayerDataManager asks it "recreate hero X from a save file".
 * Neither of those classes needs to know how heroes are stored internally.
 *
 * DSA Concepts used here
 * -----------------------
 * TreeMap<CharacterClass, LinkedList<Hero>>  heroesByClass
 *   Maps each class enum to a list of its heroes.
 *   TreeMap: ordered by CharacterClass enum order; O(log n) lookup per class.
 *
 * LinkedList<Hero>  allHeroes
 *   Flat list of every hero across all classes.
 *   Used when we need to search without knowing the class upfront.
 *
 * CSV files (one per class):
 *   data/warriors.csv, data/mages.csv, data/assassins.csv,
 *   data/tanks.csv, data/rangers.csv, data/supports.csv
 *
 * CSV header per file:
 *   id, name, title, elemental_power, stars, hp, attack, defense, crit_chance, description
 */
public class CharacterDataManager {

    // File paths for each class-specific CSV
    private static final String WARRIORS_CSV  = "data/warriors.csv";
    private static final String MAGES_CSV     = "data/mages.csv";
    private static final String ASSASSINS_CSV = "data/assassins.csv";
    private static final String TANKS_CSV     = "data/tanks.csv";
    private static final String RANGERS_CSV   = "data/rangers.csv";
    private static final String SUPPORTS_CSV  = "data/supports.csv";

    // DSA: TreeMap stores heroes organized by their CharacterClass.
    // Key = CharacterClass enum value, Value = LinkedList of heroes for that class.
    // TreeMap gives us O(log n) lookup when we want "all warriors", etc.
    private final TreeMap<CharacterClass, LinkedList<Hero>> heroesByClass = new TreeMap<>();

    // DSA: Flat LinkedList of all heroes regardless of class.
    // Used for star-tier filtering or ID lookup across all classes.
    private final LinkedList<Hero> allHeroes = new LinkedList<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Initialises the manager by:
     *   1. Pre-creating an empty LinkedList in heroesByClass for every class.
     *   2. Loading all six class CSV files.
     */
    public CharacterDataManager() {
        // Make sure every CharacterClass has a list to add heroes into
        for (CharacterClass cc : CharacterClass.values()) {
            heroesByClass.put(cc, new LinkedList<>());
        }
        loadAllClasses(); // read CSV files and fill the lists
    }

    // -------------------------------------------------------------------------
    // CSV Loading
    // -------------------------------------------------------------------------

    /**
     * Loads all six character class CSV files in one go.
     */
    private void loadAllClasses() {
        loadClassCsv(WARRIORS_CSV,  CharacterClass.WARRIOR);
        loadClassCsv(MAGES_CSV,     CharacterClass.MAGE);
        loadClassCsv(ASSASSINS_CSV, CharacterClass.ASSASSIN);
        loadClassCsv(TANKS_CSV,     CharacterClass.TANK);
        loadClassCsv(RANGERS_CSV,   CharacterClass.RANGER);
        loadClassCsv(SUPPORTS_CSV,  CharacterClass.SUPPORT);
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
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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
                    int    stars         = parseIntSafe(p[4].trim(), 1);
                    int    hp            = parseIntSafe(p[5].trim(), 100);
                    int    attack        = parseIntSafe(p[6].trim(), 10);
                    int    defense       = parseIntSafe(p[7].trim(), 10);
                    int    critChance    = parseIntSafe(p[8].trim(), 5);
                    String desc          = p[9].trim();

                    hero = new Hero(id, name, title, elementalPower,
                            characterClass, stars, hp, attack, defense, critChance, desc);

                } else {
                    // Legacy 8-column format: no title or elemental_power columns
                    String id         = p[0].trim();
                    String name       = p[1].trim();
                    int    stars      = parseIntSafe(p[2].trim(), 1);
                    int    hp         = parseIntSafe(p[3].trim(), 100);
                    int    attack     = parseIntSafe(p[4].trim(), 10);
                    int    defense    = parseIntSafe(p[5].trim(), 10);
                    int    critChance = parseIntSafe(p[6].trim(), 5);
                    String desc       = p[7].trim();

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
}
