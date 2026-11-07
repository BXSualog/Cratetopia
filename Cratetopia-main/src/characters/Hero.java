package characters;

import items.Accessory;
import items.Armor;
import items.ArmorSet;
import items.Item;
import items.Weapon;

/**
 * Hero – represents a single summoned playable character.
 *
 * OOP Concepts demonstrated here
 * --------------------------------
 * ENCAPSULATION:
 *   All fields are private.  Stats can only be read through getters, never
 *   set directly from outside.  This keeps Hero in a consistent state.
 *
 * COMPARABLE (Interface implementation):
 *   Hero implements Comparable<Hero> so PriorityQueue in GachaResult can
 *   automatically sort heroes from highest to lowest star rating.
 *
 * POLYMORPHISM (instanceof checks in equip()):
 *   equip() accepts any Item, then uses instanceof to figure out which slot
 *   to put it in (Weapon → weapon slot, Armor → one of 3 armor slots, etc.).
 *
 * DSA Concept – Array for equipment slots
 * -----------------------------------------
 * equippedArmors is a fixed-size array of length 3:
 *   Index 0 = Helmet    (SLOT_HELMET)
 *   Index 1 = Chestplate (SLOT_CHEST)
 *   Index 2 = Leggings  (SLOT_LEGGINGS)
 *
 * An array is perfect here because the number of armor slots never changes
 * (always exactly 3).  Access by index is O(1) — instant.
 *
 * Equipment layout
 * ----------------
 *   Weapon     : 1 slot  (Weapon)
 *   Armor slots: 3 slots (index array — see above)
 *   Accessory  : 1 slot  (Accessory)
 *
 * HP calculation formula (applied in getTotalHp()):
 *   Total HP = (Base HP + flatBonus) * (1 + totalPercentBonus / 100.0)
 *   Flat bonus  = sum of armor healthBonus + accessory bonus (if health type)
 *   Percent bonus = sum of armor healthPercentBonus + active set bonus
 */
public class Hero implements Comparable<Hero> {

    // --- Armor slot index constants (used so we avoid magic numbers like 0, 1, 2) ---
    public static final int SLOT_HELMET   = 0; // equippedArmors[0]
    public static final int SLOT_CHEST    = 1; // equippedArmors[1]
    public static final int SLOT_LEGGINGS = 2; // equippedArmors[2]

    // --- Identity & base stats (all private & final — set once, never change) ---
    private final String id;           // unique ID matching the CSV (e.g. "hero_aragorn")
    private final String name;         // display name (e.g. "Aragorn")
    private final String title;        // optional flavour title (e.g. "King of Men")
    private final String elementalPower; // element name string (e.g. "Fire")
    private final CharacterClass characterClass; // the enum class (WARRIOR, MAGE, …)
    private final int stars;           // rarity tier 1–6 (clamped in constructor)
    private final int baseHp;          // base HP before equipment bonuses
    private final int baseAttack;      // base ATK before equipment bonuses
    private final int baseDefense;     // base DEF before equipment bonuses
    private final int baseCritChance;  // base crit % before equipment bonuses
    private final String description;  // flavour text shown on the hero card

    // --- Equipment slots (mutable — can change as player equips/unequips) ---
    private Weapon    equippedWeapon;
    private Armor[]   equippedArmors  = new Armor[3]; // DSA: fixed-length array
    private Accessory equippedAccessory;

    // Shared catalog instance used whenever a Hero resolves its set bonus.
    // "static final" means it is created once and shared by all Hero objects.
    public static final ArmorSetCatalog DEFAULT_CATALOG = new ArmorSetCatalog();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Full constructor – used when loading a hero from a CSV file with all
     * fields including title and elemental power.
     *
     * @param id             Unique hero ID.
     * @param name           Display name.
     * @param title          Optional flavour title (empty string if none).
     * @param elementalPower Element name string (e.g. "Fire"); defaults to "None".
     * @param characterClass The hero's class (from CharacterClass enum).
     * @param stars          Rarity 1–6 (clamped to valid range).
     * @param baseHp         Base HP stat.
     * @param baseAttack     Base ATK stat.
     * @param baseDefense    Base DEF stat.
     * @param baseCritChance Base crit-chance %.
     * @param description    Flavour text.
     */
    public Hero(String id, String name, String title, String elementalPower,
                CharacterClass characterClass, int stars,
                int baseHp, int baseAttack, int baseDefense,
                int baseCritChance, String description) {
        this.id             = id;
        this.name           = name;
        // Store an empty string if title is null or blank
        this.title          = (title != null && !title.trim().isEmpty()) ? title.trim() : "";
        // Default to "None" if no element provided
        this.elementalPower = (elementalPower != null && !elementalPower.trim().isEmpty())
                ? elementalPower.trim() : "None";
        this.characterClass = characterClass;
        // Math.max / Math.min clamp stars to the range [1, 6]
        this.stars          = Math.max(1, Math.min(6, stars));
        this.baseHp         = baseHp;
        this.baseAttack     = baseAttack;
        this.baseDefense    = baseDefense;
        this.baseCritChance = baseCritChance;
        this.description    = description;
    }

    /**
     * Convenience constructor – for heroes without a title or elemental power.
     * Delegates to the full constructor with empty title and "None" element.
     */
    public Hero(String id, String name, CharacterClass characterClass, int stars,
                int baseHp, int baseAttack, int baseDefense,
                int baseCritChance, String description) {
        this(id, name, "", "None", characterClass, stars,
                baseHp, baseAttack, baseDefense, baseCritChance, description);
    }

    // -------------------------------------------------------------------------
    // Basic Getters
    // -------------------------------------------------------------------------

    public String getId()                     { return id; }
    public String getName()                   { return name; }
    public String getTitle()                  { return title; }
    public String getElementalPower()         { return elementalPower; }

    /** Converts the elemental power string into the Element enum constant. */
    public Element getElement()               { return Element.fromString(elementalPower); }

    /** Same as getElementalPower() — kept for display purposes. */
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

    /**
     * Returns the armor equipped in the given slot index.
     *
     * DSA note – Array access is O(1): index directly maps to memory address.
     *
     * @param slotIndex  0=Helmet, 1=Chest, 2=Leggings (use the SLOT_ constants).
     * @return           The equipped Armor, or null if the slot is empty.
     */
    public Armor getEquippedArmor(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 3) return null; // guard: out-of-bounds
        return equippedArmors[slotIndex];
    }

    /** Returns the raw armor array (all 3 slots, may contain nulls). */
    public Armor[] getEquippedArmors() {
        return equippedArmors;
    }

    // -------------------------------------------------------------------------
    // Equipment compatibility
    // -------------------------------------------------------------------------

    /**
     * Returns whether this hero is allowed to equip the given item.
     * Delegates the class-restriction check to Item.canEquip().
     *
     * @param item  The item to check (may be null).
     * @return      true if the item is compatible with this hero's class.
     */
    public boolean canEquip(Item item) {
        if (item == null) return false;
        return item.canEquip(this.characterClass); // Item knows its own class restrictions
    }

    // -------------------------------------------------------------------------
    // Static helpers for armor slot resolution
    // -------------------------------------------------------------------------

    /**
     * Maps an Armor's "type" string to the correct slot index constant.
     * Returns -1 if the type string is unrecognised.
     *
     * Example: armorSlotFor(helmet) → SLOT_HELMET (0)
     *
     * @param armor  The armor piece to inspect.
     * @return       SLOT_HELMET, SLOT_CHEST, SLOT_LEGGINGS, or -1.
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
                return -1; // unknown type
        }
    }

    /**
     * Returns the human-readable name for a slot index.
     * Used in UI messages like "Equipped to Helmet slot".
     *
     * @param slotIndex  0, 1, or 2.
     * @return           "Helmet", "Chestplate", "Leggings", or "Unknown Armor Slot".
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
     * Equips an item into the correct slot on this hero.
     *
     * OOP Concept – POLYMORPHISM via instanceof:
     *   equip() accepts any Item reference.  We use instanceof to figure out
     *   the actual runtime type (Weapon, Armor, or Accessory) and handle each
     *   case differently.
     *
     * Rules:
     *   - If the item cannot be equipped by this hero's class, returns null
     *     without equipping anything.
     *   - If a slot already has an item, the old item is returned so the
     *     caller can put it back in the player's inventory (swap behaviour).
     *   - For Armor, the slot is determined by the armor's type string.
     *
     * @param item  The item to equip.
     * @return      The previously equipped item from that slot (may be null),
     *              or null if equipping failed.
     */
    public Item equip(Item item) {
        if (!canEquip(item)) return null; // class restriction check first

        if (item instanceof Weapon) {
            // --- Weapon slot ---
            Item old = equippedWeapon;        // save current weapon (may be null)
            equippedWeapon = (Weapon) item;   // put new weapon in
            return old;                       // return old so inventory can get it back

        } else if (item instanceof Armor) {
            // --- One of the 3 armor slots ---
            Armor armor     = (Armor) item;
            int   slotIndex = armorSlotFor(armor); // find which slot this armor type goes to
            if (slotIndex < 0) return null;         // unrecognised armor type → fail silently
            Item old = equippedArmors[slotIndex];   // save old armor (may be null)
            equippedArmors[slotIndex] = armor;      // put new armor in
            return old;

        } else if (item instanceof Accessory) {
            // --- Accessory slot ---
            Item old = equippedAccessory;
            equippedAccessory = (Accessory) item;
            return old;
        }

        return null; // item type not handled
    }

    /**
     * Removes the item from the named slot and returns it.
     *
     * Supported slot names (case-insensitive):
     *   "weapon", "helmet" / "helm", "chest" / "chestplate" / "robe" / "vest" / "tunic" / "armor",
     *   "leggings" / "boots" / "greaves" / "sandals", "accessory"
     *
     * @param slot  The slot name to clear.
     * @return      The unequipped item, or null if the slot was already empty.
     */
    public Item unequip(String slot) {
        if (slot == null) return null;
        switch (slot.toLowerCase().trim()) {
            case "weapon":
                Item w = equippedWeapon;
                equippedWeapon = null; // clear the slot
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
                return null; // unknown slot name
        }
    }

    // -------------------------------------------------------------------------
    // Armor Set Detection
    // -------------------------------------------------------------------------

    /**
     * Returns true if the hero has a FULL armor set active.
     *
     * A full set requires:
     *   – All 3 armor slots (Helmet, Chest, Leggings) are filled.
     *   – All 3 pieces share the same non-"None" set name.
     *
     * @return  true if the full set bonus is active.
     */
    public boolean hasFullSetBonus() {
        Armor helmet   = equippedArmors[SLOT_HELMET];
        Armor chest    = equippedArmors[SLOT_CHEST];
        Armor leggings = equippedArmors[SLOT_LEGGINGS];

        // All three slots must be filled
        if (helmet == null || chest == null || leggings == null) return false;
        // At least one piece must belong to a named set
        if (!helmet.hasSetName()) return false;
        // All three pieces must share the same set name
        return helmet.getSetName().equalsIgnoreCase(chest.getSetName())
                && helmet.getSetName().equalsIgnoreCase(leggings.getSetName());
    }

    /**
     * Returns the active set name if a full set is equipped, or null otherwise.
     *
     * @return  The set name (e.g. "Dragon"), or null.
     */
    public String getActiveSetName() {
        return hasFullSetBonus() ? equippedArmors[SLOT_HELMET].getSetName() : null;
    }

    /**
     * Resolves the ArmorSet definition from the shared DEFAULT_CATALOG.
     * Returns null if no full set is active or the set is not in the catalog.
     */
    public ArmorSet getActiveArmorSet() {
        return getActiveArmorSet(DEFAULT_CATALOG);
    }

    /**
     * Resolves the ArmorSet definition from the given catalog.
     * Useful in tests where you want to inject a different catalog.
     *
     * @param catalog  The ArmorSetCatalog to look up the set in.
     * @return         The matching ArmorSet, or null.
     */
    public ArmorSet getActiveArmorSet(ArmorSetCatalog catalog) {
        String setName = getActiveSetName();
        if (setName == null || catalog == null) return null;
        return catalog.getSet(setName); // TreeMap O(log n) lookup
    }

    // -------------------------------------------------------------------------
    // Stat Calculations
    // -------------------------------------------------------------------------

    /**
     * Calculates the hero's total HP including all equipment bonuses.
     *
     * Formula:
     *   Total HP = (Base HP + flatBonus) * (1 + percentBonus / 100.0)
     *
     * Flat bonus sources:
     *   – Each equipped armor piece's healthBonus
     *   – Accessory statBonus (only if bonusType == "health")
     *
     * Percent bonus sources:
     *   – Each equipped armor piece's healthPercentBonus
     *   – Active full-set bonus (from ArmorSetCatalog)
     *
     * @return  Total HP as an integer (decimal part truncated).
     */
    public int getTotalHp() {
        return getTotalHp(DEFAULT_CATALOG);
    }

    /**
     * getTotalHp overload that accepts a custom catalog (used in tests).
     */
    public int getTotalHp(ArmorSetCatalog catalog) {
        int flatBonus    = 0; // flat HP points to add before % scaling
        int percentBonus = 0; // total percentage to scale with

        // Step 1: Sum flat and percent bonuses from all 3 armor slots
        for (Armor armor : equippedArmors) {
            if (armor != null) {
                flatBonus    += armor.getHealthBonus();
                percentBonus += armor.getHealthPercentBonus();
            }
        }

        // Step 2: Add accessory flat HP bonus (only if this accessory boosts health)
        if (equippedAccessory != null
                && "health".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            flatBonus += equippedAccessory.getStatBonus();
        }

        // Step 3: Add full-set HP % bonus if a complete set is active
        if (catalog != null) {
            ArmorSet activeSet = getActiveArmorSet(catalog);
            if (activeSet != null) {
                percentBonus += activeSet.getBonusHpPercent();
            }
        }

        // Step 4: Apply the formula — (int) truncates the decimal
        return (int) ((baseHp + flatBonus) * (1.0 + percentBonus / 100.0));
    }

    /**
     * Calculates total ATK = base attack + weapon ATK bonus + accessory ATK bonus.
     *
     * @return  Total attack stat.
     */
    public int getTotalAttack() {
        int bonus = 0;
        // Add weapon ATK (if a weapon is equipped)
        if (equippedWeapon != null) {
            bonus += equippedWeapon.getAttack();
        }
        // Add accessory ATK bonus (only if it boosts "attack")
        if (equippedAccessory != null
                && "attack".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            bonus += equippedAccessory.getStatBonus();
        }
        return baseAttack + bonus;
    }

    /**
     * Calculates total DEF using the default catalog.
     */
    public int getTotalDefense() {
        return getTotalDefense(DEFAULT_CATALOG);
    }

    /**
     * Calculates total DEF = base defense + sum of all armor DEF bonuses
     * + accessory DEF bonus (if applicable) + set bonus DEF.
     */
    public int getTotalDefense(ArmorSetCatalog catalog) {
        int bonus = 0;
        // Sum DEF from all equipped armor pieces
        for (Armor armor : equippedArmors) {
            if (armor != null) {
                bonus += armor.getDefense();
            }
        }
        // Add accessory DEF bonus
        if (equippedAccessory != null
                && "defense".equalsIgnoreCase(equippedAccessory.getBonusType())) {
            bonus += equippedAccessory.getStatBonus();
        }
        // Add set bonus DEF
        if (catalog != null) {
            ArmorSet activeSet = getActiveArmorSet(catalog);
            if (activeSet != null) {
                bonus += activeSet.getBonusDefense();
            }
        }
        return baseDefense + bonus;
    }

    /**
     * Calculates total CRIT = base crit + weapon crit bonus + accessory crit bonus.
     *
     * @return  Total crit-chance percentage.
     */
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

    /**
     * Builds the star-rating display string (e.g. 3 stars → "★★★").
     *
     * @return  A string of '★' characters matching the hero's star level.
     */
    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) sb.append("★");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Comparable – required for PriorityQueue ordering in GachaResult
    // -------------------------------------------------------------------------

    /**
     * Compares two Heroes so the PriorityQueue (MaxHeap) puts the highest-star
     * hero at the front.
     *
     * DSA Concept – Comparable / Priority Queue ordering:
     *   When we call highestRankPulls.offer(hero) in GachaResult, the
     *   PriorityQueue uses this compareTo() to decide where in the heap to
     *   place the hero.  Higher stars → compared as "smaller" so they bubble
     *   to the top (min-heap behaviour reversed by comparing other.stars
     *   against this.stars instead of the other way around).
     *
     * @param other  The other Hero to compare against.
     * @return       Negative if this hero should come BEFORE other in the queue.
     */
    @Override
    public int compareTo(Hero other) {
        if (other == null) return 1; // nulls go to the end
        // Compare other.stars vs this.stars → higher stars rank first
        int cmp = Integer.compare(other.stars, this.stars);
        if (cmp != 0) return cmp;
        // Tie-break alphabetically by name
        return this.name.compareToIgnoreCase(other.name);
    }

    // -------------------------------------------------------------------------
    // toString – used when printing a hero in lists
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        // Build optional title suffix: ` - "King of Men"` or empty
        String titleStr = (title != null && !title.isEmpty()) ? " - \"" + title + "\"" : "";
        // Build optional element suffix: ` | Fire` or empty
        String elemStr  = (!"None".equalsIgnoreCase(elementalPower) && !elementalPower.isEmpty())
                ? " | " + elementalPower : "";
        // Example: [★★★] Aragorn - "King of Men" (Warrior | Fire)
        return "[" + getStarsDisplay() + "] "
                + name + titleStr
                + " (" + characterClass.getDisplayName() + elemStr + ")";
    }
}
