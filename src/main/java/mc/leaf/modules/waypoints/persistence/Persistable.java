package mc.leaf.modules.waypoints.persistence;

import mc.leaf.core.interfaces.ILeafModule;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.NotNull;

/**
 * @param <T>
 */
public interface Persistable<T extends ILeafModule> {

    /**
     * Write this {@link Persistable} entity into the provided {@link PersistentDataHolder}.
     *
     * @param holder
     *         The {@link PersistentDataHolder} that will contain this {@link Persistable} entity.
     */
    void persist(@NotNull PersistentDataHolder holder);

    /**
     * Remove this {@link Persistable} entity from the provided {@link PersistentDataHolder}.
     *
     * @param holder
     *         The {@link PersistentDataHolder} from which this {@link Persistable} entity will be removed.
     */
    void desist(@NotNull PersistentDataHolder holder);

    /**
     * Retrieve the {@link Module} associated with this {@link Persistable}.
     *
     * @return A {@link Module}.
     */
    @NotNull
    T getModule();

}
