package characters;

/**
 * CharacterClass - an enum listing every playable class in the game.
 *
 * OOP Concept - ENUM (a special kind of class)
 * ---------------------------------------------
 * An enum is used when a variable can only ever be one of a fixed set of
 * named values.  Using CharacterClass.WARRIOR is safer than using the raw
 * String "Warrior" because the compiler checks the value for us.
 *
 * Each constant (WARRIOR, MAGE, ...) also carries a human-readable
 * displayName so we can print "Warrior" on screen instead of "WARRIOR".
 */
public enum CharacterClass {

    // Each constant is followed by the display string passed to the constructor.
    WARRIOR ("Warrior"),
    MAGE    ("Mage"),
    ASSASSIN("Assassin"),
    TANK    ("Tank"),
    RANGER  ("Ranger"),
    SUPPORT ("Support"),
    BRAWLER ("Brawler");

    // Every enum constant stores its own displayName string.
    private final String displayName;

    /**
     * Enum constructor - Java calls this for each constant above.
     *
     * @param displayName  The pretty name shown on the game screen.
     */
    CharacterClass(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the pretty name (e.g. "Warrior" instead of "WARRIOR"). */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a raw String (from a CSV file or user input) into the matching
     * CharacterClass constant.  Returns null if no match is found.
     *
     * DSA note - Linear Search:
     *   We loop through all enum values() one by one until we find a match.
     *   There are only 7 classes, so this is fast even without any sorting.
     *
     * @param text  The string to match (e.g. "warrior", "Mage", "TANK").
     * @return      The matching CharacterClass, or null if none matches.
     */
    public static CharacterClass fromString(String text) {
        if (text == null) return null;
        String clean = text.trim(); // remove accidental leading/trailing spaces
        for (CharacterClass cc : values()) {
            // Case-insensitive comparison against both "WARRIOR" and "Warrior"
            if (cc.name().equalsIgnoreCase(clean) || cc.displayName.equalsIgnoreCase(clean)) {
                return cc;
            }
        }
        return null; // no match found
    }
}
