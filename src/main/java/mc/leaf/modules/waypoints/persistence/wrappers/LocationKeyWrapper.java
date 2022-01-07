package mc.leaf.modules.waypoints.persistence.wrappers;

import org.bukkit.NamespacedKey;

import java.util.function.Function;

public record LocationKeyWrapper(Function<String, NamespacedKey> keySpace, String key) {

    public NamespacedKey getW() {
        return keySpace.apply(String.format("%s.w", this.key));
    }

    public NamespacedKey getX() {
        return keySpace.apply(String.format("%s.x", this.key));
    }

    public NamespacedKey getY() {
        return keySpace.apply(String.format("%s.y", this.key));
    }

    public NamespacedKey getZ() {
        return keySpace.apply(String.format("%s.z", this.key));
    }

}
