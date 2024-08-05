package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import com.github.manolo8.darkbot.gui.utils.CachedFormatter;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.managers.BoosterAPI;

import java.awt.*;
import java.util.List;
import java.util.Objects;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.Main.UPDATE_LOCKER;

public class BoosterProxy extends Updatable implements BoosterAPI {
    public FlashList<Booster> boosters = FlashList.ofVector(Booster::new);

    @Override
    public void update() {
        synchronized (UPDATE_LOCKER) {
            boosters.update(readAtom(0x30, 0x48));
        }
    }

    public static class Booster extends Updatable implements BoosterAPI.Booster {
        public double amount, cd;
        public String category, name;

        private BoosterAPI.Type cat;
        private final CachedFormatter simpleStringFmt = CachedFormatter.ofPattern("%3s %2.0f%% %s");

        private final FlashListLong subBoostersArr = FlashListLong.ofVector();
        private final FlashListLong attributesArr = FlashListLong.ofVector();
        private final FlashListLong attributesArr2 = FlashListLong.ofVector();
        private final FlashListLong categoriesArr = FlashListLong.ofVector();

        @Override
        public void update(long address) {
            super.update(address);
            String oldCat = this.category;
            this.category = readString(0x20);
            if (!Objects.equals(this.category, oldCat))
                this.cat = BoosterAPI.Type.of(category);
            this.name = readString(0x40); //0x48 description;
        }

        @Override
        public void update() {
            this.amount = readDouble(0x50);
            this.subBoostersArr.update(readLong(0x30));

            double min = Double.POSITIVE_INFINITY, curr;
            for (int i = 0; i < subBoostersArr.size(); i++) {
                long addr = subBoostersArr.getLong(i);
                if ((curr = API.readDouble(addr, 0x30, 0x38)) > 0 && curr < min) min = curr;
            }
            this.cd = min;

            if (cd != Double.POSITIVE_INFINITY && amount <= 0) {
                double amount = 0;

                for (int i = 0; i < subBoostersArr.size(); i++) {
                    long sub = subBoostersArr.getLong(i);
                    long attributesAddr = API.readAtom(sub, 0x28, 0x40, 0x20);
                    attributesArr.update(API.readLong(attributesAddr, 0x88));
                    attributesArr2.update(API.readLong(attributesAddr, 0x90));
                    categoriesArr.update(API.readLong(attributesAddr, 0x98));
                    for (int j = 0; j < attributesArr.size(); j++) {
                        long attribute = attributesArr.getLong(j);
                        if (API.readLong(attribute + 0x20) == readLong(0x20)) {
                            amount += API.readDouble(attribute + 0x28);
                            break;
                        }
                    }
                    for (int j = 0; j < attributesArr2.size(); j++) {
                        long attribute = attributesArr2.getLong(j);
                        long category = categoriesArr.getLong(j);
                        if (category == readLong(0x20)) {
                            amount += API.readInt(attribute + 0x20);
                            break;
                        }
                    }
                }

                this.amount = amount;
            }
        }

        public String toSimpleString() {
            return simpleStringFmt.format(Time.secondsToShort(cd), amount, cat.getSmall(category));
        }

        public Color getColor() {
            return cat.getColor();
        }

        @Override
        public double getAmount() {
            return amount;
        }

        @Override
        public double getRemainingTime() {
            return cd;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @Override
    public List<Booster> getBoosters() {
        return boosters;
    }
}
