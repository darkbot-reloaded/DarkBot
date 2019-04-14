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
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ReflectionUtils {

    public static <T> T createInstance(Class<T> clazz) {
        return createInstance(clazz, null, null);
    }

    public static <T, P> T createInstance(Class<T> clazz, Class<P> paramTyp, P param) {
        try {
            if (paramTyp != null) {
                try {
                    return clazz.getConstructor(paramTyp).newInstance(param);
                } catch (NoSuchMethodException ignore) {
                }
            }
            return clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            throw new RuntimeException("No default constructor found for " + clazz.getName());
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
                throw new IOException("Failed to create folder structure");

            Files.copy(original.toPath(), source.toPath(), REPLACE_EXISTING);

            List<String> optionList = Arrays.asList("-classpath", System.getProperty("java.class.path") + ";dist/InlineCompiler.jar");

            if (compiler.getTask(null, fileManager, diagnostics, optionList, null,
                    fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(source))).call())
                return new URLClassLoader(new URL[]{new File("tmp").toURI().toURL()})
                    .loadClass("com.github.manolo8.darkbot.modules." + moduleName);

            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())
                System.err.format("Error (%d,%d) java: %s%n", diagnostic.getLineNumber(), diagnostic.getColumnNumber(), diagnostic.getMessage(null));
            throw new UnsupportedOperationException("The provided file did not compile successfully, look at the console for more details.");
        } finally {
            try {
                deleteFolder(new File("tmp"));
            } catch (IOException ignore) {}
        }
    }

    private static void deleteFolder(File f) throws IOException {
        if (!f.exists()) return;
        if (f.isDirectory()) for (File c : f.listFiles()) deleteFolder(c);
        if (!f.delete()) throw new FileNotFoundException("Failed to delete: " + f);
    }

}
