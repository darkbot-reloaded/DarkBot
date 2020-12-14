package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.utils.Drive;

public class OreTradeGui extends Gui {

    private static final int SELLING_X_OFFSET = 80;
    private Drive drive;

    public OreTradeGui(Main main) {
        this.drive = main.hero.drive;
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

}
