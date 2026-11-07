package characters;

/**
 * Element – an enum listing every elemental power a hero can have.
 *
 * OOP Concept – ENUM with multiple fields
 * ----------------------------------------
 * Like CharacterClass, this is an enum, but each constant stores two pieces
 * of data: a displayName and a description.  Enums can have any number of
 * fields — they are essentially a special kind of class.
 *
 * Using an enum instead of raw Strings prevents typos like "Fier" or "fire "
 * that would silently break comparisons at runtime.
 */
public enum Element {

    // Format: CONSTANT("Display Name", "Short description")
    FIRE     ("Fire",      "Offensive elemental power"),
    WATER    ("Water",     "Fluid elemental power"),
    EARTH    ("Earth",     "Durable elemental power"),
    WIND     ("Wind",      "Swift elemental power"),
    LIGHTNING("Lightning", "Electrical elemental power"),
    ICE      ("Ice",       "Frost elemental power"),
    LIGHT    ("Light",     "Radiant elemental power"),
    DARK     ("Dark",      "Shadow elemental power"),
    NONE     ("None",      "Neutral elemental power");

    // Each constant carries these two String fields.
    private final String displayName;
    private final String description;

    /**
     * Enum constructor – stores the display name and description for each element.
     *
     * @param displayName  The name shown on screen (e.g. "Fire").
     * @param description  A short description of what this element represents.
     */
    Element(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /** Returns the pretty display name (e.g. "Fire"). */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the short description (e.g. "Offensive elemental power"). */
    public String getDescription() {
        return description;
    }

    /**
     * Converts a raw String into a matching Element constant.
     * Returns Element.NONE if no match is found (safe default).
     *
     * DSA note – Linear Search:
     *   Iterates through all 9 constants one by one until a match is found.
     *
     * @param text  Input string, e.g. "fire", "DARK", "Light".
     * @return      The matching Element, or NONE if nothing matches.
     */
    public static Element fromString(String text) {
        if (text == null) return NONE;
        String clean = text.trim(); // strip extra whitespace
        for (Element e : values()) {
            if (e.name().equalsIgnoreCase(clean) || e.displayName.equalsIgnoreCase(clean)) {
                return e;
            }
        }
        return NONE; // safe fallback instead of null
    }

    /**
     * Overrides the default toString() so printing an Element shows "Fire"
     * instead of "FIRE".
     */
    @Override
    public String toString() {
        return displayName;
    }
}
