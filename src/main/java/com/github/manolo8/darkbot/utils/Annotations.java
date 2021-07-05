package com.github.manolo8.darkbot.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.function.Function;

public class Annotations {

    public static <T, A1 extends Annotation, A2 extends Annotation> T
    getAnnotation(AnnotatedElement el, Class<A1> cl1, Function<A1, T> t1, Class<A2> cl2, Function<A2, T> t2) {
        return getAnnotation(el, cl1, t1).orElseGet(() -> getAnnotation(el, cl2, t2).orElse(null));
    }

    public static <T, A extends Annotation> Optional<T> getAnnotation(AnnotatedElement el, Class<A> cl1, Function<A, T> t) {
        return Optional.ofNullable(el.getAnnotation(cl1)).map(t);
    }

}
