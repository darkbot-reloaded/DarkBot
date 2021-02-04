package eu.darkbot.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArrayUtils {

    @SafeVarargs
    public static <T> List<T> asImmutableList(T... a) {
        return Collections.unmodifiableList(Arrays.asList(a));
    }

    /**
     * Returns true whenever collection is not null and not empty.
     */
    public static boolean isNotBlank(Collection<?> coll) {
        return coll != null && !coll.isEmpty();
    }
}
