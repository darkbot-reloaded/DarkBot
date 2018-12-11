package com.github.manolo8.darkbot.core.utils.pet;

public class PetKamikaze extends PetModule {

    @Override
    protected boolean isEnabled0() {
        for (int i = 0; i < helper.size; i++) {

            int c = helper.pixels[i];

            if (c == 16757504
                    && helper.add(i, 1, -8) == 9266180
                    && helper.add(i, 0, 8) == 16757504
                    && helper.add(i, 7, 1) == 16757504
                    && helper.add(i, -7, 3) == 657930
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

            if (c == 16757504
                    && helper.add(i, 5, 5) == 16757504
                    && helper.add(i, -6, -6) == 16757504
                    && helper.add(i, -1, -9) == 10515470
                    && helper.add(i, -4, 5) == 2236962
            ) {
                helper.click(i);
                return true;
            }

        }

        return false;
    }
}
