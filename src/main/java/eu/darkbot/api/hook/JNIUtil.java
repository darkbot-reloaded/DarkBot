package eu.darkbot.api.hook;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to make a native signature of method.
 * <a href = "https://www.codeproject.com/Tips/1129615/JNI-Signature-for-Java-Method">JNIUtil source</a>
 */
public class JNIUtil {
    private static final Map<Object, String> PRIMITIVE_SIGNATURES = new HashMap<>();

    static {
        PRIMITIVE_SIGNATURES.put(boolean.class, "Z");
        PRIMITIVE_SIGNATURES.put(byte.class, "B");
        PRIMITIVE_SIGNATURES.put(char.class, "C");
        PRIMITIVE_SIGNATURES.put(double.class, "D");
        PRIMITIVE_SIGNATURES.put(float.class, "F");
        PRIMITIVE_SIGNATURES.put(int.class, "I");
        PRIMITIVE_SIGNATURES.put(long.class, "J");
        PRIMITIVE_SIGNATURES.put(short.class, "S");
        PRIMITIVE_SIGNATURES.put(void.class, "V");
    }

    /**
     * Generate native method signature
     */
    public static String getJNIMethodSignature(Method m) {
        StringBuilder sb = new StringBuilder("(");

        for (Class<?> p : m.getParameterTypes())
            sb.append(getJNIClassSignature(p));

        sb.append(')')
                .append(getJNIClassSignature(m.getReturnType()));
        return sb.toString();
    }

    private static String getJNIClassSignature(Class<?> c) {
        if (c.isArray()) {
            Class<?> ct = c.getComponentType();
            return '[' + getJNIClassSignature(ct);
        }

        if (c.isPrimitive())
            return PRIMITIVE_SIGNATURES.get(c);

        return 'L' + c.getName().replace('.', '/') + ';';
    }
}