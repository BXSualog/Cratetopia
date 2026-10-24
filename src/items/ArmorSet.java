package items;

/**
 * Defines a named armor set and its full-set bonus.
 *
 * A full set bonus activates when a hero has a Helmet, Chestplate,
 * and Leggings all sharing the same set name.
 *
 * The bonus is display-only (shown on the Hero inspect screen).
 * Concrete stat modifiers are resolved in Hero.getSetBonusDefense()
 * and Hero.getSetBonusHpPercent().
 */
public class ArmorSet {

    private final String setName;
    private final String bonusTitle;       // e.g. "Dragon's Fortitude"
    private final String bonusDescription; // e.g. "+20% Defense & +15% HP"
    private final int bonusDefense;        // flat defense added by the set
    private final int bonusHpPercent;      // percentage HP added by the set

    public ArmorSet(String setName, String bonusTitle, String bonusDescription,
                    int bonusDefense, int bonusHpPercent) {
        this.setName          = setName;
        this.bonusTitle       = bonusTitle;
        this.bonusDescription = bonusDescription;
        this.bonusDefense     = bonusDefense;
        this.bonusHpPercent   = bonusHpPercent;
    }

    public String getSetName()          { return setName; }
    public String getBonusTitle()       { return bonusTitle; }
    public String getBonusDescription() { return bonusDescription; }
    public int getBonusDefense()        { return bonusDefense; }
    public int getBonusHpPercent()      { return bonusHpPercent; }

    /**
     * Returns the full display label shown on the hero inspect screen.
     * Example: "Set Bonus: Dragon's Fortitude (+20% Defense & +15% HP)"
     */
    public String getDisplayLabel() {
        return "Set Bonus: " + bonusTitle + " (" + bonusDescription + ")";
    }
}
