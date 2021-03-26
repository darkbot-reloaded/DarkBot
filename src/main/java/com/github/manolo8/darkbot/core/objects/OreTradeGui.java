package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.utils.Drive;
import eu.darkbot.api.entities.Station;
import eu.darkbot.api.managers.OreAPI;
import org.jetbrains.annotations.NotNull;

public class OreTradeGui extends Gui implements OreAPI {

    private static final int SELLING_X_OFFSET = 80;

    private final Drive drive;
    private final RefinementGui refinement;

    public OreTradeGui(Main main, RefinementGui refinementGui) {
        this.drive = main.hero.drive;
        this.refinement = refinementGui;
    }

    public void sellOre(Ore ore) {
        click(45 + ore.getXOffset(), 175);
    }

    public boolean showTrade(boolean show, BasePoint base) {
        if (trySetShowing(show)) {
            if (show) {
                base.clickable.setRadius(800);
                drive.clickCenter(true, base.locationInfo.now);
                base.clickable.setRadius(0);
            } else click(8, 8);
            return false;
        }
        return isAnimationDone();
    }

    public enum Ore {
        PROMETIUM, ENDURIUM, TERBIUM, PROMETID, DURANIUM, PROMERIUM, SEPROM, PALLADIUM, OSMIUM;

        int getXOffset() {
            return ordinal() * SELLING_X_OFFSET;
        }
    }

    @Override
    public int getAmount(@NotNull OreAPI.Ore ore) {
        return refinement.getAmount(ore);
    }

    @Override
    public void sellOre(@NotNull OreAPI.Ore ore) {
        if (!ore.isSellable()) return;
        click(45 + ore.ordinal() * SELLING_X_OFFSET, 175);
    }

    @Override
    public boolean canSellOres() {
        return isVisible() && isAnimationDone();
    }

    @Override
    public boolean showTrade(boolean show, @NotNull Station.Refinery tradePoint) {
        return showTrade(show, (BasePoint) tradePoint);
    }
}
