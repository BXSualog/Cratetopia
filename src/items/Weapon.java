package items;

/**
 * Weapon – a concrete subclass of Item representing a weapon piece of equipment.
 *
 * OOP Concepts – INHERITANCE & POLYMORPHISM
 * ------------------------------------------
 * Weapon EXTENDS Item, so it automatically has id, name, rarity, type, the
 * equippable-class restriction logic, etc. — all inherited for free.
 *
 * It adds two weapon-specific stats:
 *   attack    – flat attack points added to the hero's base attack
 *   critChance – extra critical hit percentage
 *
 * The @Override on getSummary() is POLYMORPHISM in action: when any code calls
 * item.getSummary() on a Weapon object, Java runs THIS version automatically.
 *
 * Weapons are loaded from data/weapons.csv.
 * CSV header: id, name, rarity, type, attack, crit_chance, equippable_classes, description
 */
public class Weapon extends Item {

    // Weapon-specific stats (private & final = immutable after construction)
    private final int attack;      // bonus attack points added to the hero
    private final int critChance;  // bonus crit chance percentage

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Full constructor – used when the CSV row specifies which classes can
     * equip this weapon.
     *
     * @param id                Unique item ID (e.g. "wpn_iron_sword").
     * @param name              Display name (e.g. "Iron Sword").
     * @param rarity            Rarity tier ("Common", "Rare", "Epic", "Legendary").
     * @param type              Weapon type (e.g. "Sword", "Staff", "Bow").
     * @param attack            Flat attack bonus.
     * @param critChance        Flat crit-chance bonus (percentage).
     * @param equippableClasses Semicolon-separated class names, or "All".
     * @param description       Flavour / stat description text.
     */
    public Weapon(String id, String name, String rarity, String type,
                  int attack, int critChance, String equippableClasses, String description) {
        // Call the Item constructor to store all the shared fields.
        super(id, name, rarity, type, equippableClasses, description);
        this.attack     = attack;
        this.critChance = critChance;
    }

    /**
     * Convenience constructor – defaults equippable classes to "All"
     * (any hero class can equip this weapon).
     */
    public Weapon(String id, String name, String rarity, String type,
                  int attack, int critChance, String description) {
        this(id, name, rarity, type, attack, critChance, "All", description);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Returns the flat attack bonus this weapon provides. */
    public int getAttack()     { return attack; }

    /** Returns the bonus crit-chance percentage this weapon provides. */
    public int getCritChance() { return critChance; }

    // -------------------------------------------------------------------------
    // Polymorphism – our unique implementation of the abstract getSummary()
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted stat summary line used in the equip UI.
     * Example: "  ATK: 45   | CRIT: 8% | Classes: Warrior, Assassin | Deals fire damage"
     *
     * OOP note – @Override means we are replacing the abstract version in Item
     * with a Weapon-specific format.
     */
    @Override
    public String getSummary() {
        return String.format("  ATK: %-4d | CRIT: %d%% | Classes: %-16s | %s",
                attack, critChance, getEquippableClassesDisplay(), getDescription());
    }
}
