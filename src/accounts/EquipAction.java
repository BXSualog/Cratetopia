package accounts;

import characters.Hero;
import items.Item;

/**
 * EquipAction – encapsulates a single equipment change on a hero for undo operations.
 *
 * OOP Concept: ENCAPSULATION & COMMAND PATTERN
 * DSA Concept: Stack element for LIFO undo history.
 */
public class EquipAction {
    private final Hero hero;
    private final String slot;          // "weapon", "helmet", "chest", "leggings", "accessory"
    private final Item previousItem;    // Item that was previously equipped (null if slot was empty)
    private final Item newItem;         // Item that was newly equipped (null if action was an unequip)

    public EquipAction(Hero hero, String slot, Item previousItem, Item newItem) {
        this.hero = hero;
        this.slot = slot;
        this.previousItem = previousItem;
        this.newItem = newItem;
    }

    public Hero getHero() { return hero; }
    public String getSlot() { return slot; }
    public Item getPreviousItem() { return previousItem; }
    public Item getNewItem() { return newItem; }

    @Override
    public String toString() {
        String heroName = (hero != null) ? hero.getName() : "Unknown Hero";
        if (newItem != null && previousItem != null) {
            return "Swapped " + previousItem.getName() + " with " + newItem.getName() + " on " + heroName;
        } else if (newItem != null) {
            return "Equipped " + newItem.getName() + " on " + heroName;
        } else if (previousItem != null) {
            return "Unequipped " + previousItem.getName() + " from " + heroName;
        }
        return "Equipment change on " + heroName;
    }
}
