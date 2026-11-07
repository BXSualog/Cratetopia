package items;

/**
 * ArmorSet – a plain data container (value object) that describes one named
 * armor set and the bonus stats it grants when the full 3-piece set is worn.
 *
 * OOP Concept – ENCAPSULATION
 * ----------------------------
 * All fields are private and final.  They are set once in the constructor and
 * can never change afterward.  Callers use the public getters to read them.
 * This makes ArmorSet objects "immutable" — safe to share without fear of
 * accidental modification.
 *
 * A full set bonus activates when a Hero equips a Helmet, Chestplate, AND
 * Leggings that all share the same set name (e.g. "Dragon").
 */
public class ArmorSet {

    // The key that must match the set_name column in armor.csv (case-insensitive)
    private final String setName;

    // Short title shown on the hero inspect screen (e.g. "Dragon's Fortitude")
    private final String bonusTitle;

    // Longer description of what the bonus does (e.g. "+20 DEF & +15% HP")
    private final String bonusDescription;

    // Flat defense points added when the full set is equipped
    private final int bonusDefense;

    // Percentage HP added when the full set is equipped (e.g. 15 → +15%)
    private final int bonusHpPercent;

    /**
     * Constructs one ArmorSet definition (usually loaded from armor_sets.csv).
     *
     * @param setName          Internal key matching armor pieces (e.g. "Dragon").
     * @param bonusTitle       Display title (e.g. "Dragon's Fortitude").
     * @param bonusDescription Readable stat description (e.g. "+20 DEF & +15% HP").
     * @param bonusDefense     Flat DEF points added by this set bonus.
     * @param bonusHpPercent   Percentage HP added by this set bonus.
     */
    public ArmorSet(String setName, String bonusTitle, String bonusDescription,
                    int bonusDefense, int bonusHpPercent) {
        this.setName          = setName;
        this.bonusTitle       = bonusTitle;
        this.bonusDescription = bonusDescription;
        this.bonusDefense     = bonusDefense;
        this.bonusHpPercent   = bonusHpPercent;
    }

    // -------------------------------------------------------------------------
    // Getters – read-only access to the private fields
    // -------------------------------------------------------------------------

    /** The internal set key (e.g. "Dragon"). */
    public String getSetName()          { return setName; }

    /** The short display title (e.g. "Dragon's Fortitude"). */
    public String getBonusTitle()       { return bonusTitle; }

    /** The readable description shown on the equip screen. */
    public String getBonusDescription() { return bonusDescription; }

    /** Flat defense bonus when the full 3-piece set is worn. */
    public int getBonusDefense()        { return bonusDefense; }

    /** Percentage HP bonus when the full 3-piece set is worn. */
    public int getBonusHpPercent()      { return bonusHpPercent; }

    /**
     * Builds a single display label combining the title and description.
     * Example output: "Set Bonus: Dragon's Fortitude (+20 DEF & +15% HP)"
     *
     * @return  Formatted string ready to print on the hero inspect screen.
     */
    public String getDisplayLabel() {
        return "Set Bonus: " + bonusTitle + " (" + bonusDescription + ")";
    }
}
