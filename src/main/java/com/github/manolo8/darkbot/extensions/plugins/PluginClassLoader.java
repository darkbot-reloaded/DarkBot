package com.github.manolo8.darkbot.extensions.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginClassLoader extends URLClassLoader {

    private static final List<Predicate<String>> PROTECTED = Stream.of(
            "java.lang.reflect.*",
            "java.lang.Thread",
            "java.awt.TrayIcon",
            "java.awt.SystemTray",
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "*.ReflectionUtils")
            .map(PluginClassLoader::toMatcher)
            .collect(Collectors.toList());

    private static Predicate<String> toMatcher(String pattern) {
        if (pattern.endsWith("*")) {
            String start = pattern.substring(0, pattern.length() - 1);
            return s -> s.startsWith(start);
        } else if (pattern.startsWith("*")) {
            String end = pattern.substring(1);
            return s -> s.endsWith(end);
        } else {
            return s -> s.equals(pattern);
        }
    }

    PluginClassLoader(URL[] urls) {
        super(urls);
    }

    @SuppressWarnings("unused")
    public Class<?> defineClass(String name, byte[] bytes) throws ClassNotFoundException {
        if (PROTECTED.stream().anyMatch(p -> p.test(name)))
            throw new ClassNotFoundException(name + " is a protected class");
        return super.defineClass(name, bytes, 0 ,bytes.length);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (PROTECTED.stream().anyMatch(p -> p.test(name)))
            throw new ClassNotFoundException(name + " is a protected class");

        return super.loadClass(name, resolve);
    }

    @Override
    protected String findLibrary(String libname) {
        throw new SecurityException("Library loading is not allowed from plugins");
    }

}
