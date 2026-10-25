package characters;

/**
 * Represents the 6 distinct character classes in Crateopia.
 */
public enum CharacterClass {
    WARRIOR("Warrior"),
    MAGE("Mage"),
    ASSASSIN("Assassin"),
    TANK("Tank"),
    RANGER("Ranger"),
    SUPPORT("Support");

    private final String displayName;

    CharacterClass(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CharacterClass fromString(String text) {
        if (text == null) return null;
        for (CharacterClass cc : values()) {
            if (cc.name().equalsIgnoreCase(text.trim()) || cc.displayName.equalsIgnoreCase(text.trim())) {
                return cc;
            }
        }
        return null;
    }
}
