package com.github.manolo8.darkbot.behaviours.memory;

import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.List;
import java.util.Set;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.behaviours.memory.AvmMemory.*;

/**
 * Resolves AS3 field offsets by field name at runtime.
 * <p>
 * Offsets are read from the class traits with {@link ObjectInspector} once per class and cached by vtable,
 * so a client update that adds, removes or reorders fields doesn't break the cleaners.
 * A field name declared both in a class and in one of its base classes is ambiguous and never resolved.
 * A field that a cleaner needs but can't be resolved, e.g. renamed by a client update, is reported once per class
 * and client by {@link #require}, and the step that needs it does nothing.
 * <p>
 * The traits parser trusts what it reads, so two checks keep a wrong offset from ever being used:
 * <ul>
 *     <li>before: only a class of the running client is parsed, recognized by its traits pointing to the client's
 *     VM core. An address that is not an object (e.g. freed memory) is never parsed.</li>
 *     <li>after: the fields found must end where the class says its instances end. Otherwise the parser misread
 *     the class and none of its fields is used.</li>
 * </ul>
 * {@link #attach} must be called when the client (re)loads: vtable addresses may then be reused.
 */
class SlotResolver {

    /** Slot types stored as atoms or values: writing 0 to them would not mean null. */
    private static final Set<String> NON_REFERENCE_TYPES =
            Set.of("", "*", "Object", "int", "uint", "Number", "Boolean");
    /** The object is null or not an object of the running client: nothing is known about it, nothing is reported. */
    private static final ClassSlots NONE = new ClassSlots(false);

    private final Long2ObjectMap<ClassSlots> slotsByVtable = new Long2ObjectOpenHashMap<>();
    private long core;

    /** Starts resolving for the client whose VM core is given, forgetting the classes of a previous client. */
    void attach(long core) {
        this.core = core;
        slotsByVtable.clear();
    }

    /** VM core of the client being resolved, 0 before {@link #attach}. Identifies the client. */
    long getCore() {
        return core;
    }

    /** Offset of the field in the object's class, or -1 if it can't be resolved. */
    long offset(long object, String field) {
        return slots(object).offset(field);
    }

    /**
     * Offset of a field typed as a class, where 0 means null, or -1 if there is no such field.
     * Only these fields may be cleared: untyped fields hold atoms, where 0 is not a valid value.
     */
    long referenceOffset(long object, String field) {
        ClassSlots slots = slots(object);
        return slots.references.contains(field) ? slots.offset(field) : -1;
    }

    /** Value of an object field, or 0 if it is null or the field can't be resolved. */
    long readPointer(long object, String field) {
        long offset = offset(object, field);
        return offset < 0 ? 0 : API.readAtom(object + offset);
    }

    /** {@link #readPointer}, for a field the cleaner needs: see {@link #require}. */
    long readRequired(long object, String field) {
        return require(object, field) ? readPointer(object, field) : 0;
    }

    /**
     * Whether the object's class has all the fields, for fields the cleaner reads. A missing one means the client
     * changed: it is reported once. False without a report if the object is null or not valid.
     */
    boolean require(long object, String... fields) {
        return check(object, false, fields);
    }

    /** {@link #require}, for fields the cleaner clears: they must also be typed as a class. */
    boolean requireReference(long object, String... fields) {
        return check(object, true, fields);
    }

    private boolean check(long object, boolean reference, String... fields) {
        ClassSlots slots = slots(object);
        if (slots == NONE) return false;
        if (!slots.readable) {
            report(slots, object, "", "fields can't be read reliably: steps using them are skipped");
            return false;
        }
        boolean found = true;
        for (String field : fields) {
            boolean exists = slots.offset(field) >= 0;
            if (exists && (!reference || slots.references.contains(field))) continue;
            found = false;
            report(slots, object, field, (exists ? "is not typed as a class" : "not found") +
                    ", the client changed: that step is skipped");
        }
        return found;
    }

    /** Prints a problem of a class field once, so the class name is only read for a new problem. */
    private void report(ClassSlots slots, long object, String field, String problem) {
        if (!slots.reported.add(field)) return;
        String name = ByteUtils.readObjectName(object) + (field.isEmpty() ? "" : "." + field);
        System.out.println("Memory cleaner: " + name + " " + problem);
    }

    private ClassSlots slots(long object) {
        if (object == 0) return NONE;
        long vtable = API.readLong(object + OBJECT_VTABLE);
        if (vtable == 0) return NONE;
        ClassSlots slots = slotsByVtable.get(vtable);
        if (slots != null) return slots;

        long traits = API.readLong(vtable + VTABLE_TRAITS);
        if (core == 0 || traits == 0 || API.readLong(traits + TRAITS_CORE) != core
                || !ByteUtils.isScriptableObjectValid(object)) return NONE;
        // A class of the running client: its traits never change, so the result, readable or not, is final
        slotsByVtable.put(vtable, slots = resolve(object, traits));
        return slots;
    }

    private static ClassSlots resolve(long object, long traits) {
        List<ObjectInspector.Slot> list = ObjectInspector.getObjectSlots(object);
        int hashTable = API.readInt(traits + TRAITS_HASHTABLE_OFFSET);
        long fieldsEnd = hashTable != 0 ? hashTable : API.readInt(traits + TRAITS_TOTAL_SIZE);

        long end = 0;
        for (ObjectInspector.Slot slot : list) end = Math.max(end, slot.offset + slot.size);
        // Fields that don't end where the instance does (up to alignment) mean a misread class
        if (list.isEmpty() || end > fieldsEnd || fieldsEnd - end >= Long.BYTES) return new ClassSlots(false);

        ClassSlots slots = new ClassSlots(true);
        Set<String> ambiguous = new ObjectOpenHashSet<>();
        for (ObjectInspector.Slot slot : list) {
            if (slots.offsets.containsKey(slot.name)) {
                ambiguous.add(slot.name);
                continue;
            }
            slots.offsets.put(slot.name, slot.offset);
            if (slot.size == Long.BYTES && slot.type != null && !NON_REFERENCE_TYPES.contains(slot.type))
                slots.references.add(slot.name);
        }
        slots.offsets.keySet().removeAll(ambiguous);
        slots.references.removeAll(ambiguous);
        return slots;
    }

    private static final class ClassSlots {
        private final boolean readable;
        private final Object2LongMap<String> offsets = new Object2LongOpenHashMap<>();
        private final Set<String> references = new ObjectOpenHashSet<>();
        /** Fields whose problem was already reported. */
        private final Set<String> reported = new ObjectOpenHashSet<>();

        private ClassSlots(boolean readable) {
            this.readable = readable;
            offsets.defaultReturnValue(-1);
        }

        private long offset(String field) {
            return offsets.getLong(field);
        }
    }
}
