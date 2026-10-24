package items;

import characters.CharacterClass;
import java.util.TreeSet;

/**
 * Base class representing any loot item obtainable from crates.
 * Uses Tree-based collection (TreeSet) for equippable classes adhering to DSA constraints.
 */
public abstract class Item {
    private final String id;
    private final String name;
    private final String rarity;   // Common, Uncommon, Rare, Epic, Legendary
    private final String type;
    private final String equippableClassesRaw;
    private final TreeSet<CharacterClass> equippableClasses = new TreeSet<>();
    private final boolean canBeEquippedByAll;
    private final String description;

    public Item(String id, String name, String rarity, String type, String equippableClassesRaw, String description) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.type = type;
        this.equippableClassesRaw = equippableClassesRaw != null ? equippableClassesRaw.trim() : "All";
        this.description = description;

        boolean all = false;
        if (this.equippableClassesRaw.equalsIgnoreCase("All")) {
            all = true;
            for (CharacterClass cc : CharacterClass.values()) {
                this.equippableClasses.add(cc);
            }
        } else {
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

    public Item(String id, String name, String rarity, String type, String description) {
        this(id, name, rarity, type, "All", description);
    }

    public String getId()                   { return id; }
    public String getName()                 { return name; }
    public String getRarity()               { return rarity; }
    public String getType()                 { return type; }
    public String getEquippableClassesRaw() { return equippableClassesRaw; }
    public TreeSet<CharacterClass> getEquippableClasses() { return equippableClasses; }
    public String getDescription()          { return description; }

    /**
     * Checks if this item can be equipped by the given character class.
     */
    public boolean canEquip(CharacterClass characterClass) {
        if (characterClass == null) return false;
        if (canBeEquippedByAll) return true;
        return equippableClasses.contains(characterClass);
    }

    /**
     * Formatted string showing which classes can use this item.
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

    /**
     * Returns a short formatted summary string for display.
     */
    public abstract String getSummary();

    @Override
    public String toString() {
        return "[" + rarity + "] " + name + " (" + type + ") [" + getEquippableClassesDisplay() + "]";
    }
}
