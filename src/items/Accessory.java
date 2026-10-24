package items;

/**
 * Represents an accessory item obtained from crates.
 * Loaded from data/accessories.csv
 */
public class Accessory extends Item {
    private final int statBonus;
    private final String bonusType;   // e.g. attack, health, defense, crit_chance

    public Accessory(String id, String name, String rarity, String type,
                     int statBonus, String bonusType, String equippableClasses, String description) {
        super(id, name, rarity, type, equippableClasses, description);
        this.statBonus = statBonus;
        this.bonusType = bonusType;
    }

    public Accessory(String id, String name, String rarity, String type,
                     int statBonus, String bonusType, String description) {
        this(id, name, rarity, type, statBonus, bonusType, "All", description);
    }

    public int    getStatBonus()  { return statBonus; }
    public String getBonusType()  { return bonusType; }

    @Override
    public String getSummary() {
        return String.format("  +%-4d %-11s | Classes: %-16s | %s",
                statBonus, bonusType.toUpperCase(), getEquippableClassesDisplay(), getDescription());
    }
}
