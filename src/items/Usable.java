package items;

/**
 * Usable - interface representing an item that can be used or consumed.
 * Demonstrates OOP Abstraction: defines contract for usable items.
 */
public interface Usable {
    /**
     * Returns the functional category of the item.
     */
    String getCategory();

    /**
     * Returns the probability of dropping or using.
     */
    int getProbability();
}
