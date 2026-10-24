package items;

/**
 * Represents an armor item obtained from crates.
 * Loaded from data/armor.csv
 *
 * CSV header:
 *   id,name,rarity,type,set_name,defense,health_bonus,health_percentage_bonus,equippable_classes,description
 *
 * Armor slots:
 *   type = "Helmet"     -> equips to ARMOR_HELMET  slot
 *   type = "Chest"      -> equips to ARMOR_CHEST   slot
 *   type = "Leggings"   -> equips to ARMOR_LEGS    slot
 *
 * Health calculation (Formula 1):
 *   Total HP = (Base HP + flat health_bonus) * (1 + health_percentage_bonus / 100.0)
 *   The percentage is applied after the flat bonus.
 */
public class Armor extends Item {

    // "None" means the piece does not belong to a named set
    private final String setName;
    private final int defense;
    private final int healthBonus;         // flat HP points added
    private final int healthPercentBonus;  // percentage of HP added (e.g. 5 -> +5%)

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Full constructor (all fields explicitly provided). */
    public Armor(String id, String name, String rarity, String type,
                 String setName, int defense, int healthBonus, int healthPercentBonus,
                 String equippableClasses, String description) {
        super(id, name, rarity, type, equippableClasses, description);
        this.setName            = (setName == null || setName.isBlank()) ? "None" : setName.trim();
        this.defense            = defense;
        this.healthBonus        = healthBonus;
        this.healthPercentBonus = healthPercentBonus;
    }

    /** Convenience constructor — defaults equippableClasses to "All". */
    public Armor(String id, String name, String rarity, String type,
                 String setName, int defense, int healthBonus, int healthPercentBonus,
                 String description) {
        this(id, name, rarity, type, setName, defense, healthBonus, healthPercentBonus, "All", description);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getSetName()           { return setName; }
    public int getDefense()              { return defense; }
    public int getHealthBonus()          { return healthBonus; }
    public int getHealthPercentBonus()   { return healthPercentBonus; }

    /** True when this piece belongs to a named set (not "None"). */
    public boolean hasSetName() {
        return !setName.equalsIgnoreCase("None");
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    @Override
    public String getSummary() {
        String setTag = hasSetName() ? "[Set: " + setName + "] " : "";
        return String.format("  %sDEF: %-4d | HP+: %-4d | HP%%+: %-3d%% | Classes: %-16s | %s",
                setTag, defense, healthBonus, healthPercentBonus,
                getEquippableClassesDisplay(), getDescription());
    }
}
