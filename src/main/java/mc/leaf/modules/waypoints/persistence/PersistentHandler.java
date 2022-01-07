package mc.leaf.modules.waypoints.persistence;

import mc.leaf.core.interfaces.ILeafModule;
import mc.leaf.modules.waypoints.persistence.wrappers.LocationKeyWrapper;
import org.bukkit.*;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class PersistentHandler<K extends ILeafModule, T extends Persistable<K>> implements PersistentDataType<PersistentDataContainer, T> {

    private final K              module;
    private final Class<T>       clazz;
    private       Function<K, T> supplier;

    public PersistentHandler(K module, Class<T> clazz) {

        this.module = module;
        this.clazz  = clazz;
    }

    public PersistentHandler(K module, Class<T> clazz, Function<K, T> supplier) {

        this.module   = module;
        this.clazz    = clazz;
        this.supplier = supplier;
    }

    public static Location toLocation(String worldName, Double x, Double y, Double z) {

        Objects.requireNonNull(worldName, "Unable to unbox location: The provided world name was null.");
        Objects.requireNonNull(x, "Unable to unbox location: The provided x coordinate was null.");
        Objects.requireNonNull(y, "Unable to unbox location: The provided y coordinate was null.");
        Objects.requireNonNull(z, "Unable to unbox location: The provided y coordinate was null.");

        World world = Bukkit.getWorld(worldName);

        Objects.requireNonNull(world, "Unable to unbox location: The provided world name didn't match any known world.");
        return new Location(world, x, y, z);
    }

    public ILeafModule getModule() {

        return this.module;
    }

    private NamespacedKey getKey(String key) {

        return new NamespacedKey(this.module.getPlugin(), key);
    }

    /**
     * Returns the primitive data type of this tag.
     *
     * @return the class
     */
    @NotNull
    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {

        return PersistentDataContainer.class;
    }

    /**
     * Returns the complex object type the primitive value resembles.
     *
     * @return the class type
     */
    @NotNull
    @Override
    public Class<T> getComplexType() {

        return this.clazz;
    }

    /**
     * Returns the primitive data that resembles the complex object passed to this method.
     *
     * @param complex
     *         the complex object instance
     * @param context
     *         the context this operation is running in
     *
     * @return the primitive value
     */
    @NotNull
    @Override
    public PersistentDataContainer toPrimitive(@NotNull T complex, @NotNull PersistentDataAdapterContext context) {

        PersistentDataContainer container = context.newPersistentDataContainer();

        try {
            // Scan for persistent data
            List<Field> fields = Arrays.stream(this.getComplexType().getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Persist.class)).toList();

            for (Field field : fields) {
                Persist persist = field.getAnnotation(Persist.class);
                field.setAccessible(true);
                Object data = field.get(complex);

                if (data == null) {
                    container.remove(this.getKey(persist.key()));
                } else if (data instanceof String value) {
                    container.set(this.getKey(persist.key()), STRING, value);
                } else if (data instanceof Integer value) {
                    container.set(this.getKey(persist.key()), INTEGER, value);
                } else if (data instanceof Short value) {
                    container.set(this.getKey(persist.key()), SHORT, value);
                } else if (data instanceof Long value) {
                    container.set(this.getKey(persist.key()), LONG, value);
                } else if (data instanceof Location value) {
                    double x     = value.getX();
                    double y     = value.getY();
                    double z     = value.getZ();
                    String world = value.getWorld().getName();

                    LocationKeyWrapper wrapper = new LocationKeyWrapper(this::getKey, persist.key());

                    container.set(wrapper.getX(), DOUBLE, x);
                    container.set(wrapper.getY(), DOUBLE, y);
                    container.set(wrapper.getZ(), DOUBLE, z);
                    container.set(wrapper.getW(), STRING, world);
                } else if (data instanceof UUID) {
                    String uuid = data.toString();
                    container.set(this.getKey(persist.key()), STRING, uuid);
                } else if (data instanceof Color color) {
                    container.set(this.getKey(persist.key()), INTEGER, color.asRGB());
                } else {
                    this.getModule().getPlugin().getLogger()
                            .log(Level.WARNING, "Unsupported data type: " + data.getClass().getCanonicalName());
                }
                field.setAccessible(false);
            }
        } catch (Exception e) {
            this.getModule().getPlugin().getLogger().log(Level.WARNING, "Unable to persist entity: " + e.getMessage());
        }

        return container;
    }

    /**
     * Creates a complex object based of the passed primitive value
     *
     * @param primitive
     *         the primitive value
     * @param context
     *         the context this operation is running in
     *
     * @return the complex object instance
     */
    @NotNull
    @Override
    public T fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {

        T instance = this.supplier.apply(this.module);
        try {
            List<Field> fields = Arrays.stream(this.getComplexType().getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Persist.class)).toList();

            for (Field field : fields) {
                Persist persist = field.getAnnotation(Persist.class);
                field.setAccessible(true);

                if (field.getType() == String.class) {
                    field.set(instance, primitive.get(this.getKey(persist.key()), STRING));
                } else if (field.getType() == Integer.class) {
                    field.set(instance, primitive.get(this.getKey(persist.key()), INTEGER));
                } else if (field.getType() == Short.class) {
                    field.set(instance, primitive.get(this.getKey(persist.key()), SHORT));
                } else if (field.getType() == Long.class) {
                    field.set(instance, primitive.get(this.getKey(persist.key()), LONG));
                } else if (field.getType() == Location.class) {
                    LocationKeyWrapper wrapper = new LocationKeyWrapper(this::getKey, persist.key());

                    String worldName = primitive.get(wrapper.getW(), STRING);
                    Double x         = primitive.get(wrapper.getX(), DOUBLE);
                    Double y         = primitive.get(wrapper.getY(), DOUBLE);
                    Double z         = primitive.get(wrapper.getZ(), DOUBLE);

                    try {
                        Location location = toLocation(worldName, x, y, z);
                        field.set(instance, location);
                    } catch (NullPointerException exception) {
                        this.getModule().getPlugin().getLogger().log(Level.WARNING, exception.getMessage());
                    }

                } else if (field.getType() == UUID.class) {
                    String uuid = primitive.get(this.getKey(persist.key()), STRING);
                    field.set(instance, uuid == null ? null : UUID.fromString(uuid));
                } else if (field.getType() == Color.class) {
                    Integer rgb = primitive.get(this.getKey(persist.key()), INTEGER);
                    if (rgb == null) {
                        this.getModule().getPlugin().getLogger()
                                .log(Level.WARNING, "Unable to unbox color: The provided rgb was null.");
                    } else {
                        field.set(instance, Color.fromRGB(rgb));
                    }
                } else {
                    this.getModule().getPlugin().getLogger()
                            .log(Level.WARNING, "Unsupported data type: " + field.getType().getCanonicalName());
                }
                field.setAccessible(false);
            }
        } catch (Exception e) {
            this.getModule().getPlugin().getLogger().log(Level.WARNING, "Unable to persist entity: " + e.getMessage());
        }

        return instance;
    }

}
