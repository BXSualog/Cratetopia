package characters;

import items.Accessory;
import items.Armor;
import items.Item;
import items.Weapon;

/**
 * Represents a playable Character / Hero in Crateopia.
 * Supports 1 to 6 star ratings and class-restricted item equipping.
 */
public class Hero implements Comparable<Hero> {
    private final String id;
    private final String name;
    private final CharacterClass characterClass;
    private final int stars; // 1 to 6
    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final int baseCritChance;
    private final String description;

    // Equipment Slots
    private Weapon equippedWeapon;
    private Armor equippedArmor;
    private Accessory equippedAccessory;

    public Hero(String id, String name, CharacterClass characterClass, int stars,
                int baseHp, int baseAttack, int baseDefense, int baseCritChance, String description) {
        this.id = id;
        this.name = name;
        this.characterClass = characterClass;
        this.stars = Math.max(1, Math.min(6, stars));
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseCritChance = baseCritChance;
        this.description = description;
    }

    public String getId()                   { return id; }
    public String getName()                 { return name; }
    public CharacterClass getCharacterClass() { return characterClass; }
    public int getStars()                   { return stars; }
    public int getBaseHp()                  { return baseHp; }
    public int getBaseAttack()              { return baseAttack; }
    public int getBaseDefense()             { return baseDefense; }
    public int getBaseCritChance()          { return baseCritChance; }
    public String getDescription()          { return description; }

    public Weapon getEquippedWeapon()       { return equippedWeapon; }
    public Armor getEquippedArmor()         { return equippedArmor; }
    public Accessory getEquippedAccessory() { return equippedAccessory; }

    /**
     * Checks if the character can equip the given item.
     * Item must be equippable by this character's class.
     */
    public boolean canEquip(Item item) {
        if (item == null) return false;
        return item.canEquip(this.characterClass);
    }

    /**
     * Equips an item into its respective slot.
     * Strict class validation: Returns null if item is not equippable by this class.
     * If valid, equips the item and returns any previously equipped item from that slot (or null).
     */
    public Item equip(Item item) {
        if (!canEquip(item)) {
            return null;
        }

        if (item instanceof Weapon) {
            Item old = this.equippedWeapon;
            this.equippedWeapon = (Weapon) item;
            return old;
        } else if (item instanceof Armor) {
            Item old = this.equippedArmor;
            this.equippedArmor = (Armor) item;
            return old;
        } else if (item instanceof Accessory) {
            Item old = this.equippedAccessory;
            this.equippedAccessory = (Accessory) item;
            return old;
        }
        return null;
    }

    /**
     * Unequips an item from the specified slot ("weapon", "armor", "accessory").
     * Returns the removed item, or null if empty.
     */
    public Item unequip(String slot) {
        if (slot == null) return null;
        switch (slot.toLowerCase().trim()) {
            case "weapon":
                Item w = this.equippedWeapon;
                this.equippedWeapon = null;
                return w;
            case "armor":
                Item a = this.equippedArmor;
                this.equippedArmor = null;
                return a;
            case "accessory":
                Item acc = this.equippedAccessory;
                this.equippedAccessory = null;
                return acc;
            default:
                return null;
        }
    }

    // -------------------------------------------------------------------------
    // Stat Calculations
    // -------------------------------------------------------------------------

    public int getTotalHp() {
        int bonus = 0;
        if (equippedArmor != null) {
            bonus += equippedArmor.getHealthBonus();
        }
        if (equippedAccessory != null && "health".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            bonus += equippedAccessory.getStatBonus();
        }
        return baseHp + bonus;
    }

    public int getTotalAttack() {
        int bonus = 0;
        if (equippedWeapon != null) {
            bonus += equippedWeapon.getAttack();
        }
        if (equippedAccessory != null && "attack".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            bonus += equippedAccessory.getStatBonus();
        }
        return baseAttack + bonus;
    }

    public int getTotalDefense() {
        int bonus = 0;
        if (equippedArmor != null) {
            bonus += equippedArmor.getDefense();
        }
        if (equippedAccessory != null && "defense".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            bonus += equippedAccessory.getStatBonus();
        }
        return baseDefense + bonus;
    }

    public int getTotalCritChance() {
        int bonus = 0;
        if (equippedWeapon != null) {
            bonus += equippedWeapon.getCritChance();
        }
        if (equippedAccessory != null && "crit_chance".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            bonus += equippedAccessory.getStatBonus();
        }
        return baseCritChance + bonus;
    }

    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) sb.append("★");
        for (int i = stars; i < 6; i++) sb.append("☆");
        return sb.toString();
    }

    public String getRarityTitle() {
        switch (stars) {
            case 1: return "Common";
            case 2: return "Uncommon";
            case 3: return "Rare";
            case 4: return "Epic";
            case 5: return "Legendary";
            case 6: return "Mythic";
            default: return "Novice";
        }
    }

    @Override
    public int compareTo(Hero other) {
        if (other == null) return 1;
        // Higher stars first, then by name
        int cmp = Integer.compare(other.stars, this.stars);
        if (cmp != 0) return cmp;
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return "[" + getStarsDisplay() + " " + getRarityTitle() + "] " + name + " (" + characterClass.getDisplayName() + ")";
    }
}
