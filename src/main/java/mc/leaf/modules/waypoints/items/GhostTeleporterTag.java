package mc.leaf.modules.waypoints.items;

import mc.leaf.core.utils.MinecraftColors;
import mc.leaf.modules.waypoints.LeafWaypointsModule;
import mc.leaf.modules.waypoints.persistence.Persist;
import mc.leaf.modules.waypoints.persistence.Persistable;
import mc.leaf.modules.waypoints.persistence.PersistentHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GhostTeleporterTag implements Persistable<LeafWaypointsModule> {

    private final LeafWaypointsModule module;

    @Persist(key = "location")
    private Location location;

    @Persist(key = "owner")
    private UUID owner;

    @Persist(key = "last.teleportation")
    private Long lastTeleport;

    public GhostTeleporterTag(LeafWaypointsModule module) {

        this.module = module;
    }

    /**
     * Try to read this {@link Persistable} entity from the provided {@link PersistentDataHolder}.
     *
     * @param module
     *         The {@link Module} associated with this {@link Persistable} entity.
     * @param holder
     *         The {@link PersistentDataHolder} that may contain this {@link Persistable} entity.
     *
     * @return An {@link Optional} {@link GhostTeleporterTag}, if found.
     */
    public static Optional<GhostTeleporterTag> from(LeafWaypointsModule module, PersistentDataHolder holder) {

        EntityPersistence       handler   = new EntityPersistence(module);
        PersistentDataContainer container = holder.getPersistentDataContainer();
        return Optional.ofNullable(container.get(GhostTeleporterTag.getNamespacedKey(module), handler));
    }

    /**
     * Retrieve the namespace under which a {@link Persistable} can be found.
     *
     * @param module
     *         The {@link Module} associated with this {@link Persistable} entity.
     *
     * @return A {@link NamespacedKey}.
     */
    public static NamespacedKey getNamespacedKey(LeafWaypointsModule module) {

        return new NamespacedKey(module.getPlugin(), "teleporter.ghost");
    }

    public Location getLocation() {

        return location;
    }

    public void setLocation(Location location) {

        this.location = location;
    }

    public UUID getOwner() {

        return owner;
    }

    public void setOwner(UUID owner) {

        this.owner = owner;
    }

    public Long getLastTeleport() {

        return lastTeleport;
    }

    public void setLastTeleport(Long lastTeleport) {

        this.lastTeleport = lastTeleport;
    }

    /**
     * Write this {@link Persistable} entity into the provided {@link PersistentDataHolder}.
     *
     * @param holder
     *         The {@link PersistentDataHolder} that will contain this {@link Persistable} entity.
     */
    @Override
    public void persist(@NotNull PersistentDataHolder holder) {

        EntityPersistence       handler   = new EntityPersistence(this.getModule());
        PersistentDataContainer container = holder.getPersistentDataContainer();
        container.set(GhostTeleporterTag.getNamespacedKey(this.getModule()), handler, this);

        if (holder instanceof ItemMeta meta) {

            if (!meta.hasDisplayName()) {
                TextComponent text = Component.text("Teleportation Star", Style.style(MinecraftColors.GREEN, TextDecoration.BOLD));
                meta.displayName(text);
            }

            List<Component> lore = new ArrayList<>();

            lore.add(Component.text("Some people seems to", Style.style(TextDecoration.BOLD)));
            lore.add(Component.text("have seen ghosts...", Style.style(TextDecoration.BOLD)));

            if (this.getOwner() != null) {
                lore.add(Component.empty());
                OfflinePlayer player = Bukkit.getOfflinePlayer(this.getOwner());
                lore.add(Component.text(String.format("%s's teleportation star", player.getName()), Style.style(MinecraftColors.GREEN, TextDecoration.BOLD)));
            }


            if (this.getLocation() != null) {
                lore.add(Component.empty());

                String location = String.format("§7X: %s Y: %s Z: %s", this.getLocation().getBlockX(), this
                        .getLocation().getBlockY(), this.getLocation().getBlockZ());
                String world = "§7W: " + this.getLocation().getWorld().getName();

                lore.add(Component.text(location, Style.style(MinecraftColors.GRAY)));
                lore.add(Component.text(world, Style.style(MinecraftColors.GRAY)));
            }

            meta.lore(lore);
        }
    }

    /**
     * Remove this {@link Persistable} entity from the provided {@link PersistentDataHolder}.
     *
     * @param holder
     *         The {@link PersistentDataHolder} from which this {@link Persistable} entity will be removed.
     */
    @Override
    public void desist(@NotNull PersistentDataHolder holder) {

        PersistentDataContainer container = holder.getPersistentDataContainer();
        container.remove(GhostTeleporterTag.getNamespacedKey(this.getModule()));
    }

    /**
     * Retrieve the {@link Module} associated with this {@link Persistable}.
     *
     * @return A {@link Module}.
     */
    @NotNull
    @Override
    public LeafWaypointsModule getModule() {

        return this.module;
    }

    private static class EntityPersistence extends PersistentHandler<LeafWaypointsModule, GhostTeleporterTag> {

        public EntityPersistence(LeafWaypointsModule module) {

            super(module, GhostTeleporterTag.class, GhostTeleporterTag::new);
        }

    }

}
