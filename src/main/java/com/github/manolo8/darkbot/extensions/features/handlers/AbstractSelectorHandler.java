package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.api.extensions.selectors.PrioritizedSupplier;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSelectorHandler<T, P, PS extends PrioritizedSupplier<P>> extends FeatureHandler<T> {

    private final Function<T, PS> extractor;

    private PS last;
    private List<PS> suppliers;

    private final Comparator<PS> COMPARATOR = Comparator.comparing(PS::getPriority,
                    Comparator.nullsLast(Comparator.comparingInt(PrioritizedSupplier.Priority::ordinal).reversed()))
            .thenComparing(ps -> ps == last);

    public AbstractSelectorHandler(Function<T, PS> extractor) {
        super(AbstractSelectorHandler.class);
        this.extractor = extractor;
    }

    @Override
    public void update(Stream<FeatureDefinition<T>> features) {
        this.suppliers = features.map(featureRegistry::getFeature)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(extractor)
                .sorted(COMPARATOR)
                .collect(Collectors.toList());
    }

    public PS getBestSupplier() {
        suppliers.sort(COMPARATOR);
        return suppliers.isEmpty() ? null : (last = suppliers.get(0));
    }

    public P getBest() {
        suppliers.sort(COMPARATOR);
        for (PS supplier : suppliers) {
            P res = supplier.get();
            if (res != null) {
                last = supplier;
                return res;
            }
        }
        return null;
    }

    public PS getLastUsedSupplier() {
        return last;
    }
}
