package items;

import characters.CharacterClass;
import java.util.LinkedList;

/**
 * Item – the abstract base class for every piece of equipment in the game.
 *
 * OOP Concepts – ABSTRACTION & INHERITANCE
 * -----------------------------------------
 * This class is "abstract", which means:
 *   1. You can never create a plain Item — only concrete subclasses like
 *      Weapon, Armor, or Accessory.
 *   2. It forces every subclass to implement getSummary(), because the
 *      display format differs per item type.
 *
 * All shared data (id, name, rarity, type, equippable classes) lives here so
 * Weapon/Armor/Accessory don't have to repeat it.  That is the "Don't Repeat
 * Yourself" principle in action.
 *
 * DSA Concept – LinkedList
 * ------------------------------------
 * equippableClasses is a LinkedList<CharacterClass>.
 * Stores allowed character classes in order with simple linear traversal.
 */
public abstract class Item {

    // --- Core identity fields (private & final = immutable after construction) ---
    private final String id;     // Unique machine-readable ID (e.g. "wpn_iron_sword")
    private final String name;   // Display name (e.g. "Iron Sword")
    private final String rarity; // "Common", "Rare", "Epic", or "Legendary"
    private final String type;   // Sub-type (e.g. "Sword", "Helmet", "Ring")

    // The original comma/semicolon-separated string from the CSV ("Warrior;Mage")
    private final String equippableClassesRaw;

    // DSA: LinkedList — stores allowed character classes for this item
    private final LinkedList<CharacterClass> equippableClasses = new LinkedList<>();

    // True when the item can be equipped by ANY character class.
    private final boolean canBeEquippedByAll;

    private final String description; // Flavour text shown on the item card

    // Quantity tracked for inventory display and stacking (defaults to 1)
    private int quantity = 1;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Full constructor – parses equippable classes from a raw string.
     *
     * The equippableClassesRaw string can be:
     *   "All"                 → every class can use this item
     *   "Warrior;Mage"        → only Warriors and Mages can use it
     *
     * @param id                    Unique item ID.
     * @param name                  Display name.
     * @param rarity                Rarity tier (Common / Rare / Epic / Legendary).
     * @param type                  Item sub-type (Sword, Helmet, Ring, …).
     * @param equippableClassesRaw  Semicolon-separated class names, or "All".
     * @param description           Flavour / stat description text.
     */
    public Item(String id, String name, String rarity, String type,
                String equippableClassesRaw, String description) {
        this.id   = id;
        this.name = name;
        this.rarity = rarity;
        this.type = type;
        this.equippableClassesRaw = (equippableClassesRaw != null)
                ? equippableClassesRaw.trim() : "All";
        this.description = description;

        // --- Parse equippable classes into the LinkedList ---
        boolean all = false;
        if (this.equippableClassesRaw.equalsIgnoreCase("All")) {
            // "All" → add every CharacterClass constant
            all = true;
            for (CharacterClass cc : CharacterClass.values()) {
                this.equippableClasses.add(cc);
            }
        } else {
            // Split on ";" and add each named class individually
            String[] tokens = this.equippableClassesRaw.split(";");
            for (String token : tokens) {
                CharacterClass cc = CharacterClass.fromString(token.trim());
                if (cc != null) {
                    this.equippableClasses.add(cc);
                }
            }
        }
        this.canBeEquippedByAll = all;
    }

    /**
     * Convenience constructor — defaults equippableClasses to "All".
     * Used when every class can equip the item (no restriction).
     */
    public Item(String id, String name, String rarity, String type, String description) {
        this(id, name, rarity, type, "All", description);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getId()                                       { return id; }
    public String getName()                                     { return name; }
    public String getRarity()                                   { return rarity; }
    public String getType()                                     { return type; }
    public String getEquippableClassesRaw()                     { return equippableClassesRaw; }
    public LinkedList<CharacterClass> getEquippableClasses()    { return equippableClasses; }
    public String getDescription()                              { return description; }
    public int getQuantity()                                    { return quantity; }
    public void setQuantity(int quantity)                       { this.quantity = Math.max(1, quantity); }
    public void addQuantity(int amount)                         { this.quantity += amount; }

    // -------------------------------------------------------------------------
    // Business logic
    // -------------------------------------------------------------------------

    /**
     * Checks whether this item can be equipped by the given character class.
     *
     * @param characterClass  The hero's class to check.
     * @return                true if the hero's class is allowed to equip this item.
     */
    public boolean canEquip(CharacterClass characterClass) {
        if (characterClass == null) return false;
        if (canBeEquippedByAll) return true;          // no restriction → always yes
        return equippableClasses.contains(characterClass);
    }

    /**
     * Builds a human-readable string listing all classes that can equip this item.
     * Example: "Warrior, Mage" or "All Classes".
     *
     * @return  A comma-separated list or "All Classes".
     */
    public String getEquippableClassesDisplay() {
        if (canBeEquippedByAll) return "All Classes";
        StringBuilder sb = new StringBuilder();
        for (CharacterClass cc : equippableClasses) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(cc.getDisplayName());
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Category – broad group this item belongs to (WEAPON / ARMOR / ACCESSORY / CONSUMABLE)
    // Each subclass overrides this to return its own category name.
    // This lets us filter items without using instanceof.
    // -------------------------------------------------------------------------

    /**
     * Returns the broad category of this item as a simple string.
     * Subclasses override this to identify themselves:
     *   Weapon     -> "WEAPON"
     *   Armor      -> "ARMOR"
     *   Accessory  -> "ACCESSORY"
     *   Consumable -> "CONSUMABLE"
     *
     * @return  Category string in UPPERCASE.
     */
    public String getItemCategory() {
        return "ITEM"; // default — subclasses override this
    }

    // -------------------------------------------------------------------------
    // Abstract method – every subclass must provide its own summary format
    // -------------------------------------------------------------------------

    /**
     * Returns a short, formatted stat summary for display in the UI.
     *
     * OOP Concept – ABSTRACTION / POLYMORPHISM:
     *   Declaring this abstract forces Weapon, Armor, and Accessory to each
     *   write their own version.  When code calls item.getSummary(), Java
     *   automatically runs the right version for whichever subclass item is.
     */
    public abstract String getSummary();

    // -------------------------------------------------------------------------
    // toString – used when printing the item in inventory/equip lists
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "[" + rarity + "] " + name + " (" + type + ")"
                + " [" + getEquippableClassesDisplay() + "]";
    }
}
