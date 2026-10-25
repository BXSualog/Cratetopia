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
 * Loads all character pools from separate class CSV files under data/.
 * Employs TreeMap and LinkedList strictly adhering to DSA constraints.
 */
public class CharacterDataManager {
    private static final String WARRIORS_CSV  = "data/warriors.csv";
    private static final String MAGES_CSV     = "data/mages.csv";
    private static final String ASSASSINS_CSV = "data/assassins.csv";
    private static final String TANKS_CSV     = "data/tanks.csv";
    private static final String RANGERS_CSV   = "data/rangers.csv";
    private static final String SUPPORTS_CSV  = "data/supports.csv";
    private static final String BRAWLERS_CSV  = "data/brawlers.csv";

    private final TreeMap<CharacterClass, LinkedList<Hero>> heroesByClass = new TreeMap<>();
    private final LinkedList<Hero> allHeroes = new LinkedList<>();

    public CharacterDataManager() {
        for (CharacterClass cc : CharacterClass.values()) {
            heroesByClass.put(cc, new LinkedList<>());
        }
        loadAllClasses();
    }

    private void loadAllClasses() {
        loadClassCsv(WARRIORS_CSV,  CharacterClass.WARRIOR);
        loadClassCsv(MAGES_CSV,     CharacterClass.MAGE);
        loadClassCsv(ASSASSINS_CSV, CharacterClass.ASSASSIN);
        loadClassCsv(TANKS_CSV,     CharacterClass.TANK);
        loadClassCsv(RANGERS_CSV,   CharacterClass.RANGER);
        loadClassCsv(SUPPORTS_CSV,  CharacterClass.SUPPORT);
        loadClassCsv(BRAWLERS_CSV,  CharacterClass.BRAWLER);
    }

    private void loadClassCsv(String filePath, CharacterClass characterClass) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("[CharacterDataManager] Missing character file: " + filePath);
            return;
        }

        // Header: id,name,title,elemental_power,stars,hp,attack,defense,crit_chance,description
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header

                String[] p = line.split(",", 10);
                if (p.length < 8) continue;

                Hero hero;
                if (p.length >= 10) {
                    String id = p[0].trim();
                    String name = p[1].trim();
                    String title = p[2].trim();
                    String elementalPower = p[3].trim();
                    int stars = parseIntSafe(p[4].trim(), 1);
                    int hp = parseIntSafe(p[5].trim(), 100);
                    int attack = parseIntSafe(p[6].trim(), 10);
                    int defense = parseIntSafe(p[7].trim(), 10);
                    int critChance = parseIntSafe(p[8].trim(), 5);
                    String desc = p[9].trim();

                    hero = new Hero(id, name, title, elementalPower, characterClass, stars, hp, attack, defense, critChance, desc);
                } else {
                    // Legacy 8-column fallback
                    String id = p[0].trim();
                    String name = p[1].trim();
                    int stars = parseIntSafe(p[2].trim(), 1);
                    int hp = parseIntSafe(p[3].trim(), 100);
                    int attack = parseIntSafe(p[4].trim(), 10);
                    int defense = parseIntSafe(p[5].trim(), 10);
                    int critChance = parseIntSafe(p[6].trim(), 5);
                    String desc = p[7].trim();

                    hero = new Hero(id, name, "", "None", characterClass, stars, hp, attack, defense, critChance, desc);
                }

                heroesByClass.get(characterClass).add(hero);
                allHeroes.add(hero);
            }
        } catch (IOException e) {
            System.err.println("[CharacterDataManager] Failed to load " + filePath + ": " + e.getMessage());
        }
    }

    public LinkedList<Hero> getAllHeroes() {
        return allHeroes;
    }

    public LinkedList<Hero> getHeroesByClass(CharacterClass characterClass) {
        LinkedList<Hero> list = heroesByClass.get(characterClass);
        return list != null ? list : new LinkedList<>();
    }

    public LinkedList<Hero> getHeroesByStars(int stars) {
        LinkedList<Hero> result = new LinkedList<>();
        for (Hero h : allHeroes) {
            if (h.getStars() == stars) {
                result.add(h);
            }
        }
        return result;
    }

    public Hero getHeroTemplateById(String id) {
        if (id == null) return null;
        for (Hero h : allHeroes) {
            if (h.getId().equalsIgnoreCase(id)) return h;
        }
        return null;
    }

    /**
     * Creates a brand new hero instance from a template ID so equipment slots are independent.
     */
    public Hero createNewHeroInstance(String id) {
        Hero template = getHeroTemplateById(id);
        if (template == null) return null;
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

    private int parseIntSafe(String s, int defaultVal) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return defaultVal; }
    }
}
