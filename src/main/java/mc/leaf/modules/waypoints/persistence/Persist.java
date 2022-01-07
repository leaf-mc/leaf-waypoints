package mc.leaf.modules.waypoints.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows field to be persistent as NBT data.
 *
 * @author alexpado
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Persist {

    /**
     * The key under which the value will be stored.
     *
     * @return The key.
     *
     * @see org.bukkit.NamespacedKey
     */
    String key();

}
