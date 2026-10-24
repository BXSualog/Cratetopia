package characters;

import items.Accessory;
import items.Armor;
import items.ArmorSet;
import items.Item;
import items.Weapon;

/**
 * Represents a playable Character / Hero.
 *
 * Equipment layout
 * ----------------
 *   Weapon      : 1 slot  (Weapon)
 *   Armor slots : 3 slots (Helmet, Chestplate, Leggings)
 *   Accessory   : 1 slot  (Accessory)
 *
 * HP formula (Formula 1)
 * ----------------------
 *   Step 1 - Sum flat bonuses from all 3 armors + accessory (if HP type)
 *   Step 2 - Sum % bonuses from all 3 armors
 *   Step 3 - If a full armor set is active, add the set's % bonus
 *   Total HP = (Base HP + flatBonus) * (1 + totalPercentBonus / 100.0)
 *
 * Set bonus
 * ---------
 *   A full set bonus activates when Helmet, Chestplate, AND Leggings all
 *   share the same non-"None" set name.
 */
public class Hero implements Comparable<Hero> {

    // Armor slot indices
    public static final int SLOT_HELMET     = 0;
    public static final int SLOT_CHEST      = 1;
    public static final int SLOT_LEGGINGS   = 2;


    private final String id;
    private final String name;
    private final String title;
    private final String elementalPower;
    private final CharacterClass characterClass;
    private final int stars; // 1 to 6
    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final int baseCritChance;
    private final String description;

    // Equipment slots
    private Weapon    equippedWeapon;
    private Armor[]   equippedArmors = new Armor[3]; // [Helmet, Chest, Leggings]
    private Accessory equippedAccessory;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Hero(String id, String name, String title, String elementalPower, CharacterClass characterClass, int stars,
                int baseHp, int baseAttack, int baseDefense, int baseCritChance, String description) {
        this.id             = id;
        this.name           = name;
        this.title          = (title != null && !title.trim().isEmpty()) ? title.trim() : "";
        this.elementalPower = (elementalPower != null && !elementalPower.trim().isEmpty()) ? elementalPower.trim() : "None";
        this.characterClass = characterClass;
        this.stars          = Math.max(1, Math.min(6, stars));
        this.baseHp         = baseHp;
        this.baseAttack     = baseAttack;
        this.baseDefense    = baseDefense;
        this.baseCritChance = baseCritChance;
        this.description    = description;
    }

    public Hero(String id, String name, CharacterClass characterClass, int stars,
                int baseHp, int baseAttack, int baseDefense, int baseCritChance, String description) {
        this(id, name, "", "None", characterClass, stars, baseHp, baseAttack, baseDefense, baseCritChance, description);
    }

    // -------------------------------------------------------------------------
    // Basic Getters
    // -------------------------------------------------------------------------

    public String getId()                     { return id; }
    public String getName()                   { return name; }
    public String getTitle()                  { return title; }
    public String getElementalPower()         { return elementalPower; }
    public Element getElement()               { return Element.fromString(elementalPower); }
    public String getElementalPowerDisplay()  { return elementalPower; }
    public CharacterClass getCharacterClass() { return characterClass; }
    public int getStars()                     { return stars; }
    public int getBaseHp()                    { return baseHp; }
    public int getBaseAttack()                { return baseAttack; }
    public int getBaseDefense()               { return baseDefense; }
    public int getBaseCritChance()            { return baseCritChance; }
    public String getDescription()            { return description; }

    // Equipment getters
    public Weapon    getEquippedWeapon()      { return equippedWeapon; }
    public Accessory getEquippedAccessory()   { return equippedAccessory; }

    /** Returns the armor in the given slot index (SLOT_HELMET / SLOT_CHEST / SLOT_LEGGINGS). */
    public Armor getEquippedArmor(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 3) return null;
        return equippedArmors[slotIndex];
    }

    /** Returns all 3 armor slots as an array (may contain nulls). */
    public Armor[] getEquippedArmors() {
        return equippedArmors;
    }

    // -------------------------------------------------------------------------
    // Equipment compatibility
    // -------------------------------------------------------------------------

    /**
     * Returns whether this hero can equip the given item based on class restrictions.
     */
    public boolean canEquip(Item item) {
        if (item == null) return false;
        return item.canEquip(this.characterClass);
    }

    public static final ArmorSetCatalog DEFAULT_CATALOG = new ArmorSetCatalog();

    /**
     * Resolves the correct armor slot index for a given Armor piece by its type.
     * Returns -1 if the type does not map to a valid armor slot.
     */
    public static int armorSlotFor(Armor armor) {
        if (armor == null) return -1;
        switch (armor.getType().toLowerCase().trim()) {
            case "helmet":
            case "helm":
            case "cap":
            case "crown":
                return SLOT_HELMET;
            case "chest":
            case "chestplate":
            case "robe":
            case "vest":
            case "tunic":
            case "armor":
                return SLOT_CHEST;
            case "leggings":
            case "boots":
            case "greaves":
            case "sandals":
                return SLOT_LEGGINGS;
            default:
                return -1;
        }
    }

    /**
     * Human-readable slot name for a given slot index.
     */
    public static String armorSlotName(int slotIndex) {
        switch (slotIndex) {
            case SLOT_HELMET:   return "Helmet";
            case SLOT_CHEST:    return "Chestplate";
            case SLOT_LEGGINGS: return "Leggings";
            default:            return "Unknown Armor Slot";
        }
    }

    // -------------------------------------------------------------------------
    // Equip / Unequip
    // -------------------------------------------------------------------------

    /**
     * Equips an item into its correct slot.
     *
     * For Armor: slot is determined by the armor's type (Helmet / Chest / Leggings).
     *
     * Returns the previously equipped item from that slot (may be null).
     * Returns null WITHOUT equipping if the item is not compatible with this hero's class,
     * or if the armor's type does not map to a valid slot.
     */
    public Item equip(Item item) {
        if (!canEquip(item)) return null;

        if (item instanceof Weapon) {
            Item old = equippedWeapon;
            equippedWeapon = (Weapon) item;
            return old;

        } else if (item instanceof Armor) {
            Armor armor     = (Armor) item;
            int   slotIndex = armorSlotFor(armor);
            if (slotIndex < 0) return null; // unknown armor type
            Item old = equippedArmors[slotIndex];
            equippedArmors[slotIndex] = armor;
            return old;

        } else if (item instanceof Accessory) {
            Item old = equippedAccessory;
            equippedAccessory = (Accessory) item;
            return old;
        }

        return null;
    }

    /**
     * Unequips the item in the given slot name.
     * Supported slot names: "weapon", "helmet", "chest", "leggings", "accessory"
     *
     * Returns the removed item, or null if the slot was empty.
     */
    public Item unequip(String slot) {
        if (slot == null) return null;
        switch (slot.toLowerCase().trim()) {
            case "weapon":
                Item w = equippedWeapon;
                equippedWeapon = null;
                return w;
            case "helmet":
            case "helm":
                Item h = equippedArmors[SLOT_HELMET];
                equippedArmors[SLOT_HELMET] = null;
                return h;
            case "chest":
            case "chestplate":
            case "robe":
            case "vest":
            case "tunic":
            case "armor":
                Item c = equippedArmors[SLOT_CHEST];
                equippedArmors[SLOT_CHEST] = null;
                return c;
            case "leggings":
            case "boots":
            case "greaves":
            case "sandals":
                Item l = equippedArmors[SLOT_LEGGINGS];
                equippedArmors[SLOT_LEGGINGS] = null;
                return l;
            case "accessory":
                Item acc = equippedAccessory;
                equippedAccessory = null;
                return acc;
            default:
                return null;
        }
    }

    // -------------------------------------------------------------------------
    // Armor Set Detection
    // -------------------------------------------------------------------------

    /**
     * Returns true if all 3 armor slots are filled and all share the same
     * non-"None" set name — meaning the full set bonus is active.
     */
    public boolean hasFullSetBonus() {
        Armor helmet   = equippedArmors[SLOT_HELMET];
        Armor chest    = equippedArmors[SLOT_CHEST];
        Armor leggings = equippedArmors[SLOT_LEGGINGS];

        if (helmet == null || chest == null || leggings == null) return false;
        if (!helmet.hasSetName()) return false;

        return helmet.getSetName().equalsIgnoreCase(chest.getSetName())
                && helmet.getSetName().equalsIgnoreCase(leggings.getSetName());
    }

    /**
     * Returns the active set name, or null if no full set is equipped.
     */
    public String getActiveSetName() {
        return hasFullSetBonus() ? equippedArmors[SLOT_HELMET].getSetName() : null;
    }

    /**
     * Resolves the ArmorSet definition from DEFAULT_CATALOG.
     */
    public ArmorSet getActiveArmorSet() {
        return getActiveArmorSet(DEFAULT_CATALOG);
    }

    /**
     * Resolves the ArmorSet definition from the catalog for the active set.
     * Returns null if no full set is equipped or the set is not in the catalog.
     *
     * @param catalog  the game's ArmorSetCatalog instance
     */
    public ArmorSet getActiveArmorSet(ArmorSetCatalog catalog) {
        String setName = getActiveSetName();
        if (setName == null || catalog == null) return null;
        return catalog.getSet(setName);
    }

    // -------------------------------------------------------------------------
    // Stat Calculations
    // -------------------------------------------------------------------------

    /**
     * Total HP using Formula 1:
     *   Total HP = (Base HP + flatHpBonus) * (1 + totalHpPercent / 100.0)
     *
     * Flat bonus sources : all equipped armor pieces + accessory (if HP type)
     * Percent bonus sources: all equipped armor pieces + active set bonus
     */
    public int getTotalHp() {
        return getTotalHp(DEFAULT_CATALOG);
    }

    /**
     * Total HP with set bonus resolved from a catalog.
     */
    public int getTotalHp(ArmorSetCatalog catalog) {
        int flatBonus    = 0;
        int percentBonus = 0;

        // Accumulate from 3 armor slots
        for (Armor armor : equippedArmors) {
            if (armor != null) {
                flatBonus    += armor.getHealthBonus();
                percentBonus += armor.getHealthPercentBonus();
            }
        }

        // Accessory HP bonus (flat)
        if (equippedAccessory != null
                && "health".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            flatBonus += equippedAccessory.getStatBonus();
        }

        // Full-set HP percent bonus
        if (catalog != null) {
            ArmorSet activeSet = getActiveArmorSet(catalog);
            if (activeSet != null) {
                percentBonus += activeSet.getBonusHpPercent();
            }
        }

        // Formula 1: (Base + flat) * (1 + percent/100)
        return (int) ((baseHp + flatBonus) * (1.0 + percentBonus / 100.0));
    }

    public int getTotalAttack() {
        int bonus = 0;
        if (equippedWeapon != null) {
            bonus += equippedWeapon.getAttack();
        }
        if (equippedAccessory != null
                && "attack".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            bonus += equippedAccessory.getStatBonus();
        }
        return baseAttack + bonus;
    }

    public int getTotalDefense() {
        return getTotalDefense(DEFAULT_CATALOG);
    }

    /**
     * Total defense with optional set bonus resolved from a catalog.
     */
    public int getTotalDefense(ArmorSetCatalog catalog) {
        int bonus = 0;
        for (Armor armor : equippedArmors) {
            if (armor != null) {
                bonus += armor.getDefense();
            }
        }
        if (equippedAccessory != null
                && "defense".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            bonus += equippedAccessory.getStatBonus();
        }
        // Full-set defense bonus
        if (catalog != null) {
            ArmorSet activeSet = getActiveArmorSet(catalog);
            if (activeSet != null) {
                bonus += activeSet.getBonusDefense();
            }
        }
        return baseDefense + bonus;
    }

    public int getTotalCritChance() {
        int bonus = 0;
        if (equippedWeapon != null) {
            bonus += equippedWeapon.getCritChance();
        }
        if (equippedAccessory != null
                && "crit_chance".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            bonus += equippedAccessory.getStatBonus();
        }
        return baseCritChance + bonus;
    }

    // -------------------------------------------------------------------------
    // Display Helpers
    // -------------------------------------------------------------------------

    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) sb.append("★");
        return sb.toString();
    }

    @Override
    public int compareTo(Hero other) {
        if (other == null) return 1;
        int cmp = Integer.compare(other.stars, this.stars);
        if (cmp != 0) return cmp;
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        String titleStr = (title != null && !title.isEmpty()) ? " - \"" + title + "\"" : "";
        String elemStr = (!"None".equalsIgnoreCase(elementalPower) && !elementalPower.isEmpty()) ? " | " + elementalPower : "";
        return "[" + getStarsDisplay() + "] "
                + name + titleStr + " (" + characterClass.getDisplayName() + elemStr + ")";
    }
}
