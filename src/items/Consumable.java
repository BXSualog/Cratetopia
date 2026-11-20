package items;

/**
 * Consumable -- a usable item that any character class can carry.
 *
 * OOP Concept -- INHERITANCE
 * --------------------------
 * Consumable extends Item, so it gets all the shared fields (id, name,
 * rarity, type, description) for free.  It only adds what is unique to
 * consumables: a category and a drop probability.
 *
 * Rules
 * -----
 *  - Equippable by ALL classes (no class restriction).
 *  - A player may carry at most MAX_STACK = 2 of the same consumable.
 *  - Effects are display-only for now; gameplay use is a future feature.
 */
public class Consumable extends Item implements Usable {

    // Max number of this consumable a player can carry at one time
    public static final int MAX_STACK = 2;

    // Category groups consumables by their purpose (e.g. "Attack", "Defense")
    private final String category;

    // Drop probability (integer %) used when picking loot from a loot table
    private final int probability;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a Consumable item.
     *
     * @param id          Unique machine-readable ID (e.g. "C001").
     * @param name        Display name (e.g. "Strength Potion").
     * @param rarity      Rarity tier -- typically "Common" for consumables.
     * @param category    Group name: Attack / Defense / Speed / Magic / Recovery / Progression.
     * @param description Effect text shown in the item details view.
     * @param probability Drop probability (%) for loot table selection.
     */
    public Consumable(String id, String name, String rarity,
                      String category, String description, int probability) {
        // "All" means every character class can use this item.
        // "Consumable" is the item type displayed in inventory.
        super(id, name, rarity, "Consumable", "All", description);
        this.category    = (category != null) ? category.trim() : "General";
        this.probability = probability;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Returns the category group (e.g. "Attack", "Recovery"). */
    public String getCategory() { return category; }

    /** Returns the drop probability (%) for loot table use. */
    public int getProbability() { return probability; }

    /** Returns the maximum stack size any player can hold of this consumable. */
    public int getMaxStack() { return MAX_STACK; }

    // -------------------------------------------------------------------------
    // Overrides from Item
    // -------------------------------------------------------------------------

    /**
     * Short stat summary shown next to the item name in inventory lists.
     * Example: "[Consumable | Attack] Strength Potion -- Increases Attack by 10%"
     */
    @Override
    public String getSummary() {
        return "[Consumable | " + category + "] " + getName()
                + " -- " + getDescription();
    }

    /**
     * Full string representation used when printing in item lists.
     * Shows the name, category, and stack limit reminder.
     */
    @Override
    public String toString() {
        return "[Consumable] " + getName()
                + " (" + category + ") [Max carry: " + MAX_STACK + "]";
    }
}
