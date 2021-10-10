package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import eu.darkbot.api.extensions.selectors.PrioritizedSupplier;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractPrioritizedHandler<T, P, PS extends PrioritizedSupplier<P>> extends FeatureHandler<T> {

    protected final FeatureRegistry featureRegistry;
    protected final Function<T, PS> extractor;

    protected PrioritizedSupplier<P> currentSupplier;
    protected Set<PS> suppliers;

    protected AbstractPrioritizedHandler(FeatureRegistry featureRegistry, Function<T, PS> extractor) {
        super(AbstractPrioritizedHandler.class);

        this.featureRegistry = featureRegistry;
        this.extractor = extractor;
    }

    @Override
    public void update(Stream<FeatureDefinition<T>> features) {
        this.suppliers = features.map(featureRegistry::getFeature)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(extractor)
                .collect(Collectors.toSet());

        this.suppliers.add(getDefaultSupplier());
    }

    public PrioritizedSupplier<P> getBestSupplier() {
        return currentSupplier = suppliers.stream()
                .filter(p -> p.getPriority() != null) // get() result can be null
                .max(Comparator.comparing(PrioritizedSupplier::getPriority,
                        Comparator.comparing(PrioritizedSupplier.Priority::ordinal)))
                .orElse(getDefaultSupplier()); // in case every selector have null priority use default anyway
    }

    abstract protected PS getDefaultSupplier();
}
