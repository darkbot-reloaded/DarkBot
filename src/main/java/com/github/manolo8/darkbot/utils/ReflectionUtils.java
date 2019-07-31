package com.github.manolo8.darkbot.utils;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ReflectionUtils {
    private ReflectionUtils() {};

    /** A map from primitive types to their corresponding wrapper types. */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;
    static {
        Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<>(16);
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(char.class, Character.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(void.class, Void.class);

        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(primitiveToWrapper);
    }

    private static Map<Class<?>, Object> SINGLETON_INSTANCES = new HashMap<>();

    public static <T> T createSingleton(Class<T> clazz) {
        //noinspection unchecked
        return (T) SINGLETON_INSTANCES.computeIfAbsent(clazz, ReflectionUtils::createInstance);
    }

    public static <T> T createInstance(Class<T> clazz) {
        return createInstance(clazz, null, null);
    }

    public static <T, P> T createInstance(Class<T> clazz, Class<P> paramTyp, P param) {
        try {
            if (paramTyp != null) {
                try {
                    return clazz.getConstructor(paramTyp).newInstance(param);
                } catch (NoSuchMethodException ignore) {}
            }
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No default constructor found for " + clazz.getName());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error creating instance of " + clazz.getName(), e);
        }
    }

    public static Class<?> compileModule(File original) throws Exception {
        String moduleName = original.getName().replace(".java", "");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null)
            throw new UnsupportedOperationException("No java compiler found, invalid classpath");

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            File source = new File("tmp/com/github/manolo8/darkbot/modules/" + original.getName());
            if (!source.getParentFile().exists() && !source.getParentFile().mkdirs())
                throw new IOException("Failed to create folder structure. No permission?");

            Files.copy(original.toPath(), source.toPath(), REPLACE_EXISTING);

            List<String> optionList = Arrays.asList("-classpath", System.getProperty("java.class.path") + ";dist/InlineCompiler.jar");

            if (compiler.getTask(null, fileManager, diagnostics, optionList, null, fileManager.getJavaFileObjects(source)).call()) {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("tmp").toURI().toURL()});

                Class<?> module = classLoader.loadClass("com.github.manolo8.darkbot.modules." + moduleName);

                File moduleFolder = new File("tmp/com/github/manolo8/darkbot/modules/");
                File[] subclasses = moduleFolder.listFiles(f -> f.getName().matches(moduleName + "([$][a-zA-Z0-9]+)+[.]class$"));
                if (subclasses == null) return module;

                for (File file : subclasses) {
                    classLoader.loadClass("com.github.manolo8.darkbot.modules." + file.getName().replace(".class", ""));
                }

                return module;

            }

            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())
                System.err.format("Error (%d,%d) java: %s%n", diagnostic.getLineNumber(), diagnostic.getColumnNumber(), diagnostic.getMessage(null));
            throw new UnsupportedOperationException("There was an error in the module. Look at the console for more details and ask the module creator.");
        } finally {
            try {
                deleteFolder(new File("tmp"));
            } catch (IOException ignore) {}
        }
    }

    private static void deleteFolder(File f) throws IOException {
        if (!f.exists()) return;
        if (f.isDirectory()) for (File c : f.listFiles()) deleteFolder(c);
        if (!f.delete()) throw new FileNotFoundException("Failed to delete folders. No permission?: " + f);
    }

    public static Object get(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void set(Field field, Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> Class<T> wrapped(Class<T> type) {
        if (!type.isPrimitive()) return type;
        //noinspection unchecked
        return (Class<T>) PRIMITIVE_TO_WRAPPER.get(type);
    }

}
