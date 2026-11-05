/**
 * InventorySort – specifies the sorting strategy for player inventory and hero roster.
 *
 * OOP Concept – ENUM (Enumeration):
 *   Defines a strongly typed, fixed set of sorting constants with custom properties
 *   (displayName) and encapsulation.
 *
 * Sorting Strategies:
 *   - RECENT   : Most recently acquired / summoned first (LIFO order).
 *   - RARITY   : Highest rarity tier / star rating descending.
 *   - NAME     : Alphabetical ordering A to Z.
 *   - QUANTITY : Stack count / item quantity descending.
 */
public enum InventorySort {
    RECENT("Recent (Default)"),
    RARITY("Rarity"),
    NAME("Name"),
    QUANTITY("Quantity");

    private final String displayName;

    InventorySort(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
