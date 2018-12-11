package com.github.manolo8.darkbot.core.utils.pet;

public class PetLoot extends PetModule {

    @Override
    protected boolean isEnabled0() {
        for (int i = 0; i < helper.size; i++) {

            int c = helper.pixels[i];

            if (c == 11631875
                    && helper.add(i, -1, 8) == 16757504
                    && helper.add(i, -4, 3) == 15640320
                    && helper.add(i, 8, 2) == 657930
                    && helper.add(i, 1, -6) == 16757504
            ) {
                return true;
            }

        }

        return false;
    }

    @Override
    protected boolean enable0() {
        for (int i = 0; i < helper.size; i++) {

            int c = helper.pixels[i];

            if (c == 15771906
                    && helper.add(i, 0, 8) == 16757504
                    && helper.add(i, -9, 2) == 2236962
                    && helper.add(i, -2, -7) == 16757504
                    && helper.add(i, 2, 2) == 12092426
            ) {
                helper.click(i);
                return true;
            }

        }

        return false;
    }
}
