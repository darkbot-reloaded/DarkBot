package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.facades.AssemblyMediator;
import eu.darkbot.api.managers.AssemblyAPI;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.util.Timer;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AssemblyManager extends Gui implements AssemblyAPI {
    private final AssemblyMediator assemblyMediator;
    private final BotAPI bot;
    private final Timer guiUsed = Timer.getRandom(19_000, 1000);

    @Override
    public void update() {
        super.update();
        // Last gui usage >20s ago, close gui
        if (bot.isRunning() && guiUsed.isInactive()) {
            this.show(false);
        }
    }

    @Override
    public boolean show(boolean value) {
        if (value) guiUsed.activate();
        return super.show(value);
    }

    @Override
    public int getSelectedRecipeIndex() {
        return assemblyMediator.getSelectedRecipeIndex();
    }

    @Override
    public Recipe getSelectedRecipe() {
        return assemblyMediator.getSelectedRecipe();
    }

    @Override
    public boolean isFilterDropDownOpen() {
        return assemblyMediator.isFilterDropDownOpen();
    }

    @Override
    public List<? extends Recipe> getRecipes() {
        return assemblyMediator.getRecipes();
    }

    @Override
    public List<? extends Filter> getFilters() {
        return assemblyMediator.getFilters();
    }

    public boolean clickRecipeIndex(int index) {
        if (show(true)) {
            click(67 + (33 * index), 110);
            return true;
        }
        return false;
    }

    public boolean clickCraftCollect() {
        if (show(true)) {
            click(420, 410);
            return true;
        }
        return false;
    }
}
