package mc.leaf.modules.waypoints.listeners;

import mc.leaf.core.events.LeafListener;
import mc.leaf.modules.waypoints.LeafWaypoints;
import mc.leaf.modules.waypoints.LeafWaypointsModule;
import mc.leaf.modules.waypoints.items.GhostTeleporterTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WaypointListener extends LeafListener {

    private final LeafWaypointsModule module;
    private final List<Action>        allowedActions = Arrays.asList(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    public WaypointListener(LeafWaypointsModule module) {

        this.module = module;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        // Checking if this event should be handled by this listener.
        if (!this.allowedActions.contains(event.getAction())) {
            return;
        }

        if (event.getItem() == null || event.getItem().getItemMeta() == null) {
            return;
        }

        if (event.getClickedBlock() != null && !event.getPlayer().isSneaking()) {
            Block block = event.getClickedBlock();
            if (block.getType().isInteractable()) {
                return;
            }
        }

        ItemMeta                     meta        = event.getItem().getItemMeta();
        Optional<GhostTeleporterTag> optionalTag = GhostTeleporterTag.from(module, meta);

        if (optionalTag.isEmpty()) {
            return;
        }

        GhostTeleporterTag tag = optionalTag.get();

        if (event.getPlayer().isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) { // Rewrite location mode

            if (tag.getOwner() != null && !event.getPlayer().getUniqueId().equals(tag.getOwner())) {
                event.getPlayer()
                        .sendMessage(LeafWaypoints.PREFIX + " Only the owner can change the destination of the teleportation star.");
                return;
            }

            Block block = event.getClickedBlock();

            if (block == null) {
                event.getPlayer().sendMessage(LeafWaypoints.PREFIX + " An error occurred.");
                return;
            }

            if (!block.getType().name().contains("_BED")) {
                event.getPlayer().sendMessage(LeafWaypoints.PREFIX + " Only a bed can be used as destination.");
                return;
            }

            tag.setLocation(block.getLocation().toBlockLocation().add(0.5, 1, 0.5));
            tag.setOwner(event.getPlayer().getUniqueId());
            tag.persist(meta);
            event.getItem().setItemMeta(meta);
            event.getPlayer()
                    .sendMessage(LeafWaypoints.PREFIX + " The new destination has been saved in the teleportation star.");
            return;
        }

        if (tag.getLocation() == null) {
            event.getPlayer().sendMessage(LeafWaypoints.PREFIX + " No destination defined.");
            return;
        }

        long durationLeft = (tag.getLastTeleport() + 8000) - System.currentTimeMillis();

        if (durationLeft > 0) {
            event.getPlayer()
                    .sendMessage(String.format("%s Please wait before teleporting again... (%s seconds remaining)", LeafWaypoints.PREFIX, Math.round(durationLeft / 1000f)));
            return;
        }

        tag.setLastTeleport(System.currentTimeMillis());
        tag.persist(meta);
        event.getItem().setItemMeta(meta);

        this.playTeleportAnimation(event.getPlayer().getLocation());
        event.getPlayer().teleport(tag.getLocation());
        event.getPlayer().swingMainHand();
        this.playTeleportAnimation(event.getPlayer().getLocation());
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        ItemStack stack = player.getInventory().getItemInMainHand();

        if (stack.getType() == Material.AIR || stack.getItemMeta() == null) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();

        Optional<GhostTeleporterTag> optionalTag = GhostTeleporterTag.from(module, meta);

        if (optionalTag.isEmpty()) {
            return;
        }

        event.setCancelled(true);
        GhostTeleporterTag tag = optionalTag.get();

        long durationLeft = (tag.getLastTeleport() + 8000) - System.currentTimeMillis();

        if (durationLeft > 0) {
            player.sendMessage(String.format("%s Please wait before teleporting again... (%s seconds remaining)", LeafWaypoints.PREFIX, Math.round(durationLeft / 1000f)));
            return;
        }

        tag.setLastTeleport(System.currentTimeMillis());
        tag.persist(meta);
        stack.setItemMeta(meta);

        this.playTeleportAnimation(event.getEntity().getLocation());
        event.getEntity().teleport(tag.getLocation());
        player.swingMainHand();
        this.playTeleportAnimation(event.getEntity().getLocation());
    }

    private void playTeleportAnimation(Location location) {

        location.getWorld().spawnParticle(Particle.FLAME, location, 300, 0, 1, 0);
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, 2, 1);
    }

}
