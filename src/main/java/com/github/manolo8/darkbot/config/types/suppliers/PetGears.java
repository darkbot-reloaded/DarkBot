package com.github.manolo8.darkbot.config.types.suppliers;

import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.game.enums.PetGear;
import eu.darkbot.api.managers.PetAPI;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class PetGears implements Dropdown.Options<PetGear> {

    private static final List<PetGear> DEFAULT_OPTIONS = Arrays.asList(PetGear.PASSIVE, PetGear.GUARD,
            PetGear.LOOTER, PetGear.ENEMY_LOCATOR, PetGear.KAMIKAZE);

    private final ConditionalCollection<PetGear> gears;

    public PetGears(PetAPI pet) {
        this.gears = new ConditionalCollection<>(Arrays.asList(PetGear.values()), pet::hasGear);
    }

    @Override
    public Collection<PetGear> options() {
        return gears.isEmpty() ? DEFAULT_OPTIONS : gears;
    }

    @Override
    public String getText(PetGear value) {
        return value.getName();
    }

    private static class ConditionalCollection<T> extends AbstractCollection<T> {

        private final Collection<T> base;
        private final Predicate<T> condition;

        public ConditionalCollection(Collection<T> base, Predicate<T> condition) {
            this.base = base;
            this.condition = condition;
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            Iterator<T> baseIt = base.iterator();
            return new Iterator<T>() {
                T next;
                boolean hasNext = false;

                private T getNext() {
                    if (hasNext) return next;
                    while (baseIt.hasNext()) {
                        T next = baseIt.next();
                        if (condition.test(next)) {
                            hasNext = true;
                            return next;
                        }
                    }
                    return null;
                }

                @Override
                public boolean hasNext() {
                    getNext();
                    return hasNext;
                }

                @Override
                public T next() {
                    T next = getNext();
                    if (!hasNext)
                        throw new NoSuchElementException();
                    return next;
                }
            };
        }

        @Override
        public int size() {
            return (int) base.stream().filter(condition).count();
        }
    }

}
