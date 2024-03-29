package com.github.manolo8.darkbot.core.utils.pathfinder;

import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.utils.PathFinder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class AreaImpl implements Area {

    public boolean changed;

    public abstract Locatable toSide(Locatable point);

    public abstract Collection<? extends Locatable> getPoints(@NotNull PathFinder pf);

}
