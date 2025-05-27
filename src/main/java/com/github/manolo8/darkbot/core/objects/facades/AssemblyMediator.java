package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import eu.darkbot.api.managers.AssemblyAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

@Getter
public class AssemblyMediator extends Updatable implements AssemblyAPI {
    private int selectedRecipeIndex;
    private final Recipe selectedRecipe = new Recipe();

    private boolean listUpdated = false;
    private final FlashList<Recipe> recipes = FlashList.ofVector(Recipe::new);
    private final List<Filter> filters = new ArrayList<>();
    private final FlashList<RowFilter> rowSettings = FlashList.ofVector(RowFilter::new);

    private boolean isFilterDropDownOpen;

    @Override
    public void update() {
        if (address == 0) return;
        selectedRecipeIndex = readInt(0x48);

        listUpdated = false;
        selectedRecipe.updateIfChanged(readAtom(0x70));
        selectedRecipe.update();

        if (rowSettings.updateAndReport(readAtom(0x78, 0xb0))) {
            filters.clear();
            for (int i = 0; i < rowSettings.size(); i++) {
                RowFilter currRow = rowSettings.get(i);
                filters.add(currRow.first.withPos(i, 0));
                filters.add(currRow.second.withPos(i, 1));
            }
        }

        isFilterDropDownOpen = readBoolean(0x78, 0x60, 0x1D0);
    }

    @Override
    public List<? extends AssemblyAPI.Recipe> getRecipes() {
        if (!listUpdated) {
            recipes.update(readAtom(0x60, 0x20));
            listUpdated = true;
        }
        return recipes;
    }

    @Getter
    @ToString
    private static class Recipe extends Updatable implements AssemblyAPI.Recipe {
        @Getter(AccessLevel.NONE)
        @ToString.Exclude
        private final FlashListLong rewardsArr = FlashListLong.ofVector();

        private String recipeId, visibility = "";
        private final List<String> rewards = new ArrayList<>();
        private final FlashList<ResourceRequired> resourcesRequired = FlashList.ofVector(ResourceRequired::new);
        private boolean isCraftable, isInProgress, isCollectable = false;
        private int craftTimeLeft, craftTimeRequired;
        private final FlashListLong craftTimeData = FlashListLong.ofVector();

        @Override
        public void update(long address) {
            super.update(address);

            recipeId = readString(0x58, 0x48);

            rewardsArr.update(API.readAtom(address + 0x60));
            rewards.clear();
            rewardsArr.forEach(ptr -> rewards.add(API.readString(ptr, 0x48)));

            resourcesRequired.update(API.readAtom(address + 0x50));
        }

        @Override
        public void update() {
            isCraftable = readBoolean(0x20);
            craftTimeLeft = (int) readDouble(0x40, 0x28, 0x38);

            long data = readAtom(0x58, 0x40, 0x20);
            visibility = API.readString(data, 0x90);
            craftTimeData.update(API.readAtom(data, 0x98));
            if (!craftTimeData.isEmpty())
                craftTimeRequired = API.readInt(craftTimeData.getLong(0), 0x24);
            isInProgress = craftTimeLeft > 0 && craftTimeLeft <= craftTimeRequired;
            isCollectable = !isInProgress && readDouble(0x40, 0x20, 0x28) == 1.0;
        }

    }

    @Getter
    @ToString
    private static class ResourceRequired extends Auto implements AssemblyAPI.ResourceRequired {
        private String resourceId = "";
        private double amountRequired;

        public void update() {
            if (address <= 0) return;
            resourceId = API.readString(address, 0x28, 0x48);
            amountRequired = API.readDouble(address + 0x30);
            // this also gives back same value, not sure which is correct
            // amountRequired = API.readDouble(address + 0x38);
        }
    }

    @ToString
    private static class RowFilter extends Reporting {
        private final ItemFilter first = new ItemFilter();
        private final ItemFilter second = new ItemFilter();

        @Override
        public boolean updateAndReport() {
            if (address == 0) return false;

            return first.updateAndReport(readAtom(0x20)) | second.updateAndReport(readAtom(0x28));
        }
    }

    @Getter
    @ToString
    private static class ItemFilter extends Reporting implements AssemblyAPI.Filter {
        private String filterName = "";
        private boolean isChecked;
        private int row, col;
        private double x, y;

        @Override
        public void update(long address) {
            super.update(address);
            filterName = API.readString(address, 0x20);
        }

        @Override
        public boolean updateAndReport() {
            if (address == 0) return false;
            var data = readAtom(0x28);
            isChecked = API.readBoolean(data, 0x1D0);
            x = API.readDouble(data, 0x158);
            y = API.readDouble(data, 0x160);
            // Always false, we only care about address itself changing, which reports true regardless
            return false;
        }

        public ItemFilter withPos(int row, int col) {
            this.row = row;
            this.col = col;
            return this;
        }
    }
}
