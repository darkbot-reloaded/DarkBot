package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class BoosterProxy extends Updatable {
    public List<Booster> boosters = new ArrayList<>();

    private ObjArray boostersArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.FIX;

        boostersArr.update(API.readMemoryLong(data + 0x48));
        boostersArr.sync(boosters, Booster::new, null);
    }

    public static class Booster extends UpdatableAuto {
        public double amount, cd;
        public String category, name;

        private ObjArray subBoostersArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            this.category = API.readMemoryString(address, 0x20);
            this.name     = API.readMemoryString(address, 0x40); //0x48 description;
            this.amount   = API.readMemoryDouble(address + 0x50);
            this.subBoostersArr.update(API.readMemoryLong(address + 0x30));

            double current, min = 0;
            for (int i = 0; i < subBoostersArr.getSize(); i++) {
                current = API.readMemoryDouble(subBoostersArr.get(i), 0x30, 0x38);
                if (min == 0 || (current != 0 && current < min)) min = current;
            }
            this.cd = min;
            //System.out.println(Time.secondsToString((int) cd));
        }
    }
}
