package eu.darkbot.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public class PluginApiImpl implements PluginAPI {

    private final Set<Singleton> singletons = new HashSet<>();
    private final Set<Class<?>> implClasses = new HashSet<>();

    public PluginApiImpl(Collection<Singleton> singletons,
                         Collection<Class<? extends API>> implementations) {
        this.singletons.add(this);
        this.singletons.addAll(singletons);
        this.implClasses.addAll(implementations);
    }

    private <T extends Singleton> T getOrCreateSingleton(Class<T> clazz) throws UnsupportedOperationException {
        for (Singleton implementation : singletons) {
            if (clazz.isInstance(implementation))
                return clazz.cast(implementation);
        }
        T impl = createInstance(clazz);
        singletons.add(impl);
        return impl;
    }

    @Override
    public <T extends API> @Nullable T getAPI(@NotNull Class<T> api) {
        try {
            return requireAPI(api);
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <T extends API> @NotNull T requireAPI(@NotNull Class<T> api) throws UnsupportedOperationException {
        if (api.isAssignableFrom(Singleton.class)) return (T) getOrCreateSingleton((Class<Singleton>) api);
        return createInstance(api);
    }

    @Override
    public @NotNull <T> T createInstance(@NotNull Class<T> clazz) {
        if (clazz.isAssignableFrom(Singleton.class)) return (T) getOrCreateSingleton((Class<Singleton>) clazz);

        Class<?> impl = implClasses.stream()
                .filter(implCl -> implCl.isAssignableFrom(clazz))
                .findFirst()
                .orElse(clazz);

        if (impl.isInterface())
            throw new UnsupportedOperationException("No implementation found for " + clazz.getName());

        Constructor<?> constructor = Arrays.stream(impl.getConstructors())
                .filter(c ->
                        Arrays.stream(c.getParameterTypes()).allMatch(cl -> cl.isAssignableFrom(API.class)))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("No API-only constructor in " + impl.getName()));

        try {
            return (T) constructor.newInstance(Arrays.stream(constructor.getParameterTypes())
                    .map(p -> requireAPI((Class<API>) p))
                    .toArray(Object[]::new));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Exception calling constructor for API: " + clazz.getName(), e);
        }
    }

}
