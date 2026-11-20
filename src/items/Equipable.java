package items;

import characters.CharacterClass;
import java.util.LinkedList;

/**
 * Equipable - interface representing an item that can be equipped onto a character.
 * Demonstrates OOP Abstraction: defines the contract for equippable gear.
 */
public interface Equipable {
    /**
     * Checks if a character class is eligible to equip this item.
     * @param characterClass The character class enum.
     * @return true if permitted, false otherwise.
     */
    boolean canEquip(CharacterClass characterClass);

    /**
     * Returns the equippable classes as a LinkedList.
     */
    LinkedList<CharacterClass> getEquippableClasses();

    /**
     * Returns the display representation of equippable classes.
     */
    String getEquippableClassesDisplay();
}
