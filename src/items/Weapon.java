package items;

/**
 * Represents a weapon item obtained from crates.
 * Loaded from data/weapons.csv
 */
public class Weapon extends Item {
    private final int attack;
    private final int critChance;

    public Weapon(String id, String name, String rarity, String type,
                  int attack, int critChance, String equippableClasses, String description) {
        super(id, name, rarity, type, equippableClasses, description);
        this.attack = attack;
        this.critChance = critChance;
    }

    public Weapon(String id, String name, String rarity, String type,
                  int attack, int critChance, String description) {
        this(id, name, rarity, type, attack, critChance, "All", description);
    }

    public int getAttack()     { return attack; }
    public int getCritChance() { return critChance; }

    @Override
    public String getSummary() {
        return String.format("  ATK: %-4d | CRIT: %d%% | Classes: %-16s | %s",
                attack, critChance, getEquippableClassesDisplay(), getDescription());
    }
}
