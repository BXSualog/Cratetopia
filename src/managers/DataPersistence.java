package managers;

import java.util.LinkedList;

/**
 * DataPersistence - generic interface defining CRUD and persistence operations
 * for data manager components.
 * Demonstrates OOP Abstraction.
 *
 * @param <T> The entity type managed by this persistence service.
 */
public interface DataPersistence<T> {
    /**
     * Loads entities from underlying storage.
     */
    void load();

    /**
     * Saves entities to underlying storage.
     */
    void save();

    /**
     * Retrieves all managed entities.
     */
    LinkedList<T> getAll();
}
