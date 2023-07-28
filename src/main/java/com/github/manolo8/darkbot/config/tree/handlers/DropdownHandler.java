package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.gui.tree.utils.EnumDropdownOptions;
import com.github.manolo8.darkbot.gui.tree.utils.GenericDropdownModel;
import com.github.manolo8.darkbot.utils.itf.LazyValue;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.extensions.PluginInfo;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class DropdownHandler extends LazyInitHandler<Object> {

    public static DropdownHandler of(Field field, PluginInfo namespace, PluginAPI api) {
        Dropdown dropdown = field.getAnnotation(Dropdown.class);

        Class<? extends Dropdown.Options<?>> optionCl = dropdown.options();
        LazyValue<Dropdown.Options<?>> options = optionCl != Dropdown.NullOptions.class ?
                LazyValue.of(() -> api.requireInstance(optionCl)) :
                LazyValue.resolved(optionsOf(api, namespace, field.getGenericType()));

        return new DropdownHandler(field, dropdown.multi(), options);
    }

    private static <E extends Enum<E>> Dropdown.Options<?> optionsOf(PluginAPI api, PluginInfo namespace, Type type) {
        Class<E> cl = getEnumType(type);

        if (cl == null)
            throw new UnsupportedOperationException("Dropdown is not of type enum and did not specify options");

        return new EnumDropdownOptions<>(api, namespace, cl);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> Class<E> getEnumType(Type generic) {
        if (generic instanceof Class) {
            if (((Class<?>) generic).isEnum())
                return (Class<E>) generic;
        } else if (generic instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) generic;

            Type rawType = type.getRawType();
            if (!(rawType instanceof Class) || !Collection.class.isAssignableFrom((Class<?>) rawType)) return null;

            Type[] args = type.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class && ((Class<?>) args[0]).isEnum())
                return (Class<E>) args[0];
        }
        return null;
    }

    public DropdownHandler(@Nullable Field field,
                           boolean multi,
                           LazyValue<Dropdown.Options<?>> options) {
        super(field);
        metadata.put(multi ? "isMultiDropdown" : "isDropdown", true);
        lazyMetadata.put("dropdown.options", options);
        lazyMetadata.put("dropdown.model", () -> new GenericDropdownModel<>(options.get()));
    }

}
