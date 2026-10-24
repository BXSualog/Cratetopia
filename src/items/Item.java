package items;

/**
 * Base class representing any loot item obtainable from crates.
 */
public abstract class Item {
    private final String id;
    private final String name;
    private final String rarity;   // Common, Uncommon, Rare, Epic, Legendary
    private final String type;
    private final String description;

    public Item(String id, String name, String rarity, String type, String description) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.type = type;
        this.description = description;
    }

    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getRarity()      { return rarity; }
    public String getType()        { return type; }
    public String getDescription() { return description; }

    /**
     * Returns a short formatted summary string for display.
     */
    public abstract String getSummary();

    @Override
    public String toString() {
        return "[" + rarity + "] " + name + " (" + type + ")";
    }
}
