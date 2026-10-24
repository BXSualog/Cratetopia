package items;

/**
 * Represents a weapon item obtained from crates.
 * Loaded from data/weapons.csv
 */
public class Weapon extends Item {
    private final int attack;
    private final int critChance;

    public Weapon(String id, String name, String rarity, String type,
                  int attack, int critChance, String description) {
        super(id, name, rarity, type, description);
        this.attack = attack;
        this.critChance = critChance;
    }

    public int getAttack()     { return attack; }
    public int getCritChance() { return critChance; }

    @Override
    public String getSummary() {
        return String.format("  ATK: %-4d | CRIT: %d%%  | %s", attack, critChance, getDescription());
    }
}
