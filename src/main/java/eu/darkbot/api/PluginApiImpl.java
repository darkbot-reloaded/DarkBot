package eu.darkbot.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public class PluginApiImpl implements PluginAPI {

    private final Set<Singleton> singletons = new HashSet<>();
    private final Set<Class<?>> implClasses = new HashSet<>();

    public PluginApiImpl(Singleton singleton,
                         Class<? extends API>... implementations) {
        this.singletons.add(this);
        this.singletons.add(singleton);
        Collections.addAll(this.implClasses, implementations);
    }

    private <T extends Singleton> T getOrCreateSingleton(Class<T> clazz) throws UnsupportedOperationException {
        for (Singleton implementation : singletons) {
            if (clazz.isInstance(implementation))
                return clazz.cast(implementation);
        }
        T impl = createNewInstance(clazz);
        singletons.add(impl);
        return impl;
    }

    private <T> T createNewInstance(Class<T> clazz) throws UnsupportedOperationException {
        if (clazz.isInterface())
            return (T) createNewInstance(implClasses.stream()
                    .filter(clazz::isAssignableFrom)
                    .findFirst()
                    .orElseThrow(() ->
                            new UnsupportedOperationException("No implementation found for " + clazz.getName())));

        Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                .filter(c ->
                        Arrays.stream(c.getParameterTypes()).allMatch(API.class::isAssignableFrom))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("No API-only constructor in " + clazz.getName()));

        try {
            return (T) constructor.newInstance(Arrays.stream(constructor.getParameterTypes())
                    .map(p -> getOrCreate((Class<API>) p))
                    .toArray(Object[]::new));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Exception calling constructor for API: " + clazz.getName(), e);
        }
    }

    private <T> T getOrCreate(Class<T> clazz) {
        if (Singleton.class.isAssignableFrom(clazz))
            return (T) getOrCreateSingleton((Class<Singleton>) clazz);
        return createNewInstance(clazz);
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
        if (!api.isInterface())
            throw new UnsupportedOperationException("Can't get API from implementation " +
                    api.getName() + ", use the API interface");

        return getOrCreate(api);
    }

    @Override
    public @NotNull <T> T requireInstance(@NotNull Class<T> clazz) throws UnsupportedOperationException {
        if (clazz.isInterface())
            throw new UnsupportedOperationException("Can't create instance from interface " +
                    clazz.getName() + ", use requireAPI instead");

        return getOrCreate(clazz);
    }

}
