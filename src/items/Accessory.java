package items;

/**
 * Accessory – a concrete subclass of Item representing a ring, amulet, etc.
 *
 * OOP Concepts – INHERITANCE & POLYMORPHISM
 * ------------------------------------------
 * Accessory EXTENDS Item, so it inherits all shared item logic.
 * It adds two accessory-specific fields:
 *   statBonus  – the numeric amount of the bonus (e.g. 30)
 *   bonusType  – what stat it boosts (e.g. "attack", "health", "defense",
 *                "crit_chance")
 *
 * Hero.getTotalAttack() / getTotalHp() / etc. check bonusType to know which
 * stat to apply the bonus to.
 *
 * Accessories are loaded from data/accessories.csv.
 * CSV header: id, name, rarity, type, stat_bonus, bonus_type, equippable_classes, description
 */
public class Accessory extends Item {

    // The numeric amount of the stat boost (e.g. 30 attack or 50 health)
    private final int statBonus;

    // Which stat this accessory boosts: "attack", "health", "defense", "crit_chance"
    private final String bonusType;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Full constructor – used when the CSV specifies class restrictions.
     *
     * @param id                Unique item ID (e.g. "acc_attack_ring").
     * @param name              Display name (e.g. "Ring of Power").
     * @param rarity            Rarity tier ("Common", "Rare", "Epic", "Legendary").
     * @param type              Accessory type label (e.g. "Ring", "Amulet", "Belt").
     * @param statBonus         Numeric bonus amount.
     * @param bonusType         Stat the bonus applies to ("attack", "health", etc.).
     * @param equippableClasses Semicolon-separated class names, or "All".
     * @param description       Flavour / stat description text.
     */
    public Accessory(String id, String name, String rarity, String type,
                     int statBonus, String bonusType,
                     String equippableClasses, String description) {
        // Pass shared fields up to the Item constructor
        super(id, name, rarity, type, equippableClasses, description);
        this.statBonus  = statBonus;
        this.bonusType  = bonusType;
    }

    /**
     * Convenience constructor – defaults equippable classes to "All".
     */
    public Accessory(String id, String name, String rarity, String type,
                     int statBonus, String bonusType, String description) {
        this(id, name, rarity, type, statBonus, bonusType, "All", description);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the numeric bonus amount (e.g. 30).
     * Hero stat methods compare bonusType and add this value to the right stat.
     */
    public int    getStatBonus()  { return statBonus; }

    /**
     * Returns the stat name this accessory boosts.
     * Possible values: "attack", "health", "defense", "crit_chance".
     */
    public String getBonusType()  { return bonusType; }

    // -------------------------------------------------------------------------
    // Polymorphism – our unique implementation of the abstract getSummary()
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted summary line for the inventory / equip screen.
     * Example: "  +30   ATTACK      | Classes: Warrior, Assassin | Forged in dragonfire"
     *
     * The bonusType is printed in UPPERCASE so it stands out visually.
     */
    @Override
    public String getSummary() {
        return String.format("  +%-4d %-11s | Classes: %-16s | %s",
                statBonus, bonusType.toUpperCase(), getEquippableClassesDisplay(), getDescription());
    }
}
