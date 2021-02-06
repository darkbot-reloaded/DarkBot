package com.github.manolo8.darkbot.core.utils.pathfinder;

import eu.darkbot.api.entities.utils.Area;
import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.utils.PathFinder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class AreaImpl implements Area {

    public boolean changed;

    public abstract PathPoint toSide(Locatable point);

    public abstract Collection<PathPoint> getPoints(@NotNull PathFinder pf);

}
