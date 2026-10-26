package items;

/**
 * Armor – a concrete subclass of Item representing a piece of body armor.
 *
 * OOP Concepts – INHERITANCE & POLYMORPHISM
 * ------------------------------------------
 * Armor EXTENDS Item, inheriting all shared equipment logic.
 * It adds armor-specific stats: defense, flat HP bonus, and % HP bonus.
 * It also stores a setName for armor-set bonus detection.
 *
 * HP Calculation Formula (applied inside Hero.getTotalHp()):
 *   Total HP = (Base HP + flatHealthBonus) * (1 + healthPercentBonus / 100.0)
 *   The percentage is applied AFTER the flat bonus is added.
 *
 * Armor is loaded from data/armor.csv.
 * CSV header:
 *   id, name, rarity, type, set_name, defense,
 *   health_bonus, health_percentage_bonus, equippable_classes, description
 *
 * Armor slot mapping (resolved in Hero.armorSlotFor()):
 *   type = "Helmet"     → SLOT_HELMET   (index 0)
 *   type = "Chest"      → SLOT_CHEST    (index 1)
 *   type = "Leggings"   → SLOT_LEGGINGS (index 2)
 */
public class Armor extends Item {

    // "None" means this piece does not belong to any named set.
    // Otherwise, it's a name like "Dragon" that must match other armor pieces
    // in the hero's loadout to trigger the full-set bonus.
    private final String setName;

    // Flat defense points this piece adds (reduces incoming damage)
    private final int defense;

    // Flat HP points added to the hero's base HP
    private final int healthBonus;

    // Percentage HP added after the flat bonus is applied (e.g. 5 → +5%)
    private final int healthPercentBonus;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Full constructor – all fields explicitly provided.
     *
     * @param id                 Unique item ID (e.g. "arm_dragon_helmet").
     * @param name               Display name (e.g. "Dragon Helmet").
     * @param rarity             Rarity tier ("Common", "Rare", "Epic", "Legendary").
     * @param type               Armor slot type ("Helmet", "Chest", or "Leggings").
     * @param setName            Named set this piece belongs to, or "None".
     * @param defense            Flat defense bonus.
     * @param healthBonus        Flat HP bonus.
     * @param healthPercentBonus Percentage HP bonus (e.g. 5 means +5%).
     * @param equippableClasses  Semicolon-separated class names, or "All".
     * @param description        Flavour / stat text.
     */
    public Armor(String id, String name, String rarity, String type,
                 String setName, int defense, int healthBonus, int healthPercentBonus,
                 String equippableClasses, String description) {
        // Pass shared fields up to the Item constructor (super = parent class)
        super(id, name, rarity, type, equippableClasses, description);

        // Store "None" if no set name was provided (blank or null)
        this.setName            = (setName == null || setName.isBlank()) ? "None" : setName.trim();
        this.defense            = defense;
        this.healthBonus        = healthBonus;
        this.healthPercentBonus = healthPercentBonus;
    }

    /**
     * Convenience constructor – defaults equippable classes to "All".
     */
    public Armor(String id, String name, String rarity, String type,
                 String setName, int defense, int healthBonus, int healthPercentBonus,
                 String description) {
        this(id, name, rarity, type, setName, defense, healthBonus, healthPercentBonus,
                "All", description);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the set name this armor piece belongs to (e.g. "Dragon").
     * Returns "None" if it is not part of any set.
     */
    public String getSetName()          { return setName; }

    /** Returns the flat defense bonus this armor provides. */
    public int getDefense()             { return defense; }

    /** Returns the flat HP bonus this armor adds (before % scaling). */
    public int getHealthBonus()         { return healthBonus; }

    /** Returns the percentage HP bonus (e.g. 5 means the formula gets +5%). */
    public int getHealthPercentBonus()  { return healthPercentBonus; }

    /**
     * Returns true when this piece belongs to a named armor set.
     * Used by Hero.hasFullSetBonus() to detect set completion.
     */
    public boolean hasSetName() {
        return !setName.equalsIgnoreCase("None");
    }

    // -------------------------------------------------------------------------
    // Polymorphism – our unique implementation of the abstract getSummary()
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted stat summary shown in the equip / inventory UI.
     * If this piece belongs to a set, the set name is prepended.
     *
     * Example (no set):
     *   "  DEF: 30   | HP+: 120  | HP%+: 5%  | Classes: Tank | Sturdy iron plate"
     *
     * Example (with set):
     *   "  [Set: Dragon] DEF: 50   | HP+: 200  | HP%+: 15% | Classes: Tank | …"
     */
    @Override
    public String getSummary() {
        // Show "[Set: Dragon] " prefix only when this piece belongs to a set
        String setTag = hasSetName() ? "[Set: " + setName + "] " : "";
        return String.format("  %sDEF: %-4d | HP+: %-4d | HP%%+: %-3d%% | Classes: %-16s | %s",
                setTag, defense, healthBonus, healthPercentBonus,
                getEquippableClassesDisplay(), getDescription());
    }
}
