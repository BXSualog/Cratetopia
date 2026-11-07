package items;

import characters.CharacterClass;
import java.util.TreeSet;

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
 * DSA Concept – TreeSet (Sorted Set)
 * ------------------------------------
 * equippableClasses is a TreeSet<CharacterClass>.
 *
 * A TreeSet stores elements in sorted order and prevents duplicates, backed
 * by a self-balancing Binary Search Tree (BST) internally.
 *   • Inserting a class: O(log n)
 *   • Checking if a class is present (contains): O(log n)
 *
 * This is better than a plain array here because:
 *   – We never add duplicate classes.
 *   – We don't need index-based access; we only need "does this set contain X?"
 */
public abstract class Item {

    // --- Core identity fields (private & final = immutable after construction) ---
    private final String id;     // Unique machine-readable ID (e.g. "wpn_iron_sword")
    private final String name;   // Display name (e.g. "Iron Sword")
    private final String rarity; // "Common", "Rare", "Epic", or "Legendary"
    private final String type;   // Sub-type (e.g. "Sword", "Helmet", "Ring")

    // The original comma/semicolon-separated string from the CSV ("Warrior;Mage")
    private final String equippableClassesRaw;

    // DSA: TreeSet keeps class entries sorted and duplicate-free, backed by a BST.
    private final TreeSet<CharacterClass> equippableClasses = new TreeSet<>();

    // True when the item can be equipped by ANY character class.
    private final boolean canBeEquippedByAll;

    private final String description; // Flavour text shown on the item card

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

        // --- Parse equippable classes into the TreeSet ---
        boolean all = false;
        if (this.equippableClassesRaw.equalsIgnoreCase("All")) {
            // "All" → add every CharacterClass constant to the set
            all = true;
            for (CharacterClass cc : CharacterClass.values()) {
                this.equippableClasses.add(cc); // TreeSet.add() = O(log n) BST insert
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
    public TreeSet<CharacterClass> getEquippableClasses()       { return equippableClasses; }
    public String getDescription()                              { return description; }

    // -------------------------------------------------------------------------
    // Business logic
    // -------------------------------------------------------------------------

    /**
     * Checks whether this item can be equipped by the given character class.
     *
     * DSA note – TreeSet.contains():
     *   Because the TreeSet is backed by a BST, this lookup runs in O(log n)
     *   time — faster than scanning an unsorted list.
     *
     * @param characterClass  The hero's class to check.
     * @return                true if the hero's class is allowed to equip this item.
     */
    public boolean canEquip(CharacterClass characterClass) {
        if (characterClass == null) return false;
        if (canBeEquippedByAll) return true;          // no restriction → always yes
        return equippableClasses.contains(characterClass); // O(log n) BST lookup
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
        // TreeSet iterates in sorted order, so output is always alphabetical
        for (CharacterClass cc : equippableClasses) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(cc.getDisplayName());
        }
        return sb.toString();
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
