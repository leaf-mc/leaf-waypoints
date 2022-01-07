package mc.leaf.modules.waypoints;

import mc.leaf.core.interfaces.ILeafCore;
import mc.leaf.core.interfaces.ILeafModule;
import mc.leaf.modules.waypoints.items.GhostTeleporterTag;
import mc.leaf.modules.waypoints.listeners.WaypointListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class LeafWaypointsModule implements ILeafModule {

    private final JavaPlugin    plugin;
    private final ILeafCore     core;
    private final NamespacedKey craftKey;
    private       boolean       enabled;

    public LeafWaypointsModule(JavaPlugin plugin, ILeafCore core) {

        this.plugin = plugin;
        this.core   = core;
        this.core.registerModule(this);
        this.craftKey = new NamespacedKey(this.plugin, "item.tp.ghost");
    }

    @Override
    public void onEnable() {

        this.createRecipe();
        this.getCore().getEventBridge().register(this, new WaypointListener(this));
        this.enabled = true;
    }

    @Override
    public void onDisable() {

        this.getPlugin().getServer().removeRecipe(this.craftKey);
        this.enabled = false;
    }

    @Override
    public ILeafCore getCore() {

        return this.core;
    }

    @Override
    public String getName() {

        return "Waypoints";
    }

    @Override
    public boolean isEnabled() {

        return this.enabled;
    }

    @Override
    public JavaPlugin getPlugin() {

        return this.plugin;
    }

    private void createRecipe() {

        ItemStack          stack = new ItemStack(Material.NETHER_STAR);
        ItemMeta           meta  = stack.getItemMeta();
        GhostTeleporterTag tag   = new GhostTeleporterTag(this);
        tag.setLastTeleport(0L);
        tag.persist(meta);
        stack.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(this.craftKey, stack);
        recipe.shape("xo", "oo");
        recipe.setIngredient('x', Material.NETHER_STAR);
        recipe.setIngredient('o', Material.ENDER_PEARL);

        this.getPlugin().getServer().addRecipe(recipe);
    }

}
