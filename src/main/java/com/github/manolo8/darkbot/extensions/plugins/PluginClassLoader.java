package com.github.manolo8.darkbot.extensions.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public class PluginClassLoader extends URLClassLoader {

    private static final List<String> PROTECTED = Arrays.asList(
            "java.lang.reflect",
            "java.lang.Thread",
            "com.github.manolo8.darkbot.utils.ReflectionUtils");

    PluginClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (PROTECTED.stream().anyMatch(name::startsWith))
            throw new ClassNotFoundException(name + " is a protected class");

        return super.loadClass(name, resolve);
    }

}
