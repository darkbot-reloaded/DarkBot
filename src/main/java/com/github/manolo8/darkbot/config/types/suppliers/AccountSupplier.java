package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import eu.darkbot.api.config.annotations.Dropdown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AccountSupplier implements Dropdown.Options<Integer> {
    private final BackpageManager backpageManager;
    private final HeroManager heroManager;
    public AccountSupplier(BackpageManager backpageManager, HeroManager heroManager) {
        this.backpageManager = backpageManager;
        this.heroManager = heroManager;
    }
    @Override
    public Collection<Integer> options() {
        List<Integer> ids = new ArrayList<>();
        ids.add(0);
        ids.addAll(backpageManager.getAccountIds());
        return ids;
    }

    @Override
    public @NotNull String getText(@Nullable Integer option) {
        if(Integer.valueOf(0).equals(option)) return "(none)";
        PlayerInfo info = heroManager.main.config.PLAYER_INFOS.get(option);
        if(info != null) return info.getUsername();
        return Objects.toString(option);
    }

    @Override
    public @Nullable String getTooltip(@Nullable Integer option) {
        if(Integer.valueOf(0).equals(option)) return null;
        return "User ID: " + option;
    }
}
