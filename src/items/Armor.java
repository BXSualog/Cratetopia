package items;

/**
 * Represents an armor item obtained from crates.
 * Loaded from data/armor.csv
 */
public class Armor extends Item {
    private final int defense;
    private final int healthBonus;

    public Armor(String id, String name, String rarity, String type,
                 int defense, int healthBonus, String description) {
        super(id, name, rarity, type, description);
        this.defense = defense;
        this.healthBonus = healthBonus;
    }

    public int getDefense()     { return defense; }
    public int getHealthBonus() { return healthBonus; }

    @Override
    public String getSummary() {
        return String.format("  DEF: %-4d | HP+: %-4d | %s", defense, healthBonus, getDescription());
    }
}
