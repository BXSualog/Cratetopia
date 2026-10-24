package characters;

/**
 * Elemental identities for characters in Infinite Realms.
 * Each character commands an elemental power independently of their combat class.
 */
public enum Element {
    FIRE("Fire", "Offensive elemental power"),
    WATER("Water", "Fluid elemental power"),
    EARTH("Earth", "Durable elemental power"),
    WIND("Wind", "Swift elemental power"),
    LIGHTNING("Lightning", "Electrical elemental power"),
    ICE("Ice", "Frost elemental power"),
    LIGHT("Light", "Radiant elemental power"),
    DARK("Dark", "Shadow elemental power"),
    NONE("None", "Neutral elemental power");

    private final String displayName;
    private final String description;

    Element(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static Element fromString(String text) {
        if (text == null) return NONE;
        String clean = text.trim();
        for (Element e : values()) {
            if (e.name().equalsIgnoreCase(clean) || e.displayName.equalsIgnoreCase(clean)) {
                return e;
            }
        }
        return NONE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
