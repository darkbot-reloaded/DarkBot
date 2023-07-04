package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.managers.AssemblyAPI;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class AssemblyMediator extends Updatable implements AssemblyAPI {
    public int selectedRecipeIndex;
    public Recipe selectedRecipe = new Recipe();
    public boolean isFilterDropDownOpen;
    public List<Recipe> recipes = new ArrayList<>();
    private final ObjArray recipesPtr = ObjArray.ofVector(true);
    public List<RowFilter> rowSettings = new ArrayList<>();
    public List<Filter> filters = new ArrayList<>();
    private final ObjArray rowSettingsArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        if (address == 0) return;
        //get index of current selected index
        selectedRecipeIndex = Main.API.readInt(address + 0x48);

        //get list of shown recipe
        long recipeCollectionAddress = Main.API.readMemoryLong(address + 0x60);
        recipesPtr.update(Main.API.readMemoryLong(recipeCollectionAddress + 0x20));
        recipesPtr.sync(recipes, Recipe::new);

        //get selected recipe info
        long selectedRecipeAddress = Main.API.readMemoryLong(address + 0x70);
        selectedRecipe.update(selectedRecipeAddress);

        //get list of selected filters
        long itemFilterViewController = Main.API.readMemoryLong(address + 0x78);
        rowSettingsArr.update(Main.API.readMemoryLong(itemFilterViewController + 0xb0));
        rowSettingsArr.sync(rowSettings, RowFilter::new);
        filters.clear();
        for (int i = 0; i < rowSettings.size(); i++) {
            RowFilter currRow = rowSettings.get(i);
            if (currRow.getFirst() != null) {
                filters.add(new Filter(currRow.getFirst(), i, 0));
            }
            if (currRow.getSecond() != null) {
                filters.add(new Filter(currRow.getSecond(), i, 1));
            }
        }

        //get filter drop down is open
        long filterDropdownAddress = Main.API.readMemoryLong(itemFilterViewController + 0x60);
        isFilterDropDownOpen = API.readBoolean(filterDropdownAddress + 0x1D0);
    }

    @Override
    public int getSelectedRecipeIndex() {
        return selectedRecipeIndex;
    }

    @Override
    public AssemblyAPI.Recipe getSelectedRecipe() {
        return selectedRecipe;
    }

    @Override
    public boolean isFilterDropDownOpen() {
        return isFilterDropDownOpen;
    }

    @Override
    public List<? extends AssemblyAPI.Recipe> getRecipes() {
        return recipes;
    }

    @Override
    public List<? extends AssemblyAPI.RowFilter> getRowFilters() {
        return rowSettings;
    }

    @Override
    public List<? extends AssemblyAPI.Filter> getFilters() {
        return filters;
    }

    public static class Recipe extends Auto implements AssemblyAPI.Recipe {
        public String recipeId = "";
        public List<String> rewards = new ArrayList<>();
        public final ObjArray rewardsArr = ObjArray.ofVector(true);

        public List<ResourceRequired> resourcesRequired = new ArrayList<>();
        private final ObjArray resourcesRequiredArr = ObjArray.ofVector(true);

        public boolean isCraftable = false;

        @Override
        public void update() {
            if (address == 0) return;
            isCraftable = API.readBoolean(address + 0x20);

            long itemVoAddress = Main.API.readMemoryLong(address + 0x58);
            recipeId = API.readMemoryString(itemVoAddress, 0x48);

            long rewardsArrAddress = Main.API.readMemoryLong(address + 0x60);
            rewardsArr.update(rewardsArrAddress);
            rewards.clear();
            rewardsArr.forEach(ptr -> rewards.add(API.readMemoryString(ptr, 0x48)));

            long resourcesRequiredArrAddress = Main.API.readMemoryLong(address + 0x50);
            resourcesRequiredArr.update(resourcesRequiredArrAddress);
            resourcesRequiredArr.sync(resourcesRequired, ResourceRequired::new);
        }

        @Override
        public String toString() {
            return Recipe.class.getSimpleName() + " - " + recipeId + ":" + isCraftable;
        }

        @Override
        public String getRecipeId() {
            return recipeId;
        }

        @Override
        public List<? extends String> getRewards() {
            return rewards;
        }

        @Override
        public List<? extends AssemblyAPI.ResourceRequired> getResourcesRequired() {
            return resourcesRequired;
        }

        @Override
        public boolean isCraftable() {
            return isCraftable;
        }
    }


    public static class ResourceRequired extends Auto implements AssemblyAPI.ResourceRequired {
        public String resourceId = "";
        public double amountRequired, amountRequiredBackup = -1.0;

        public void update() {
            if (address <= 0) return;
            long itemVoAddress = Main.API.readMemoryLong(address + 0x28);
            resourceId = API.readMemoryString(itemVoAddress, 0x48);
            amountRequired = API.readDouble(address + 0x30);
            //this also gives back same value, not sure which is correct
            amountRequiredBackup = API.readDouble(address + 0x38);
        }

        @Override
        public String toString() {
            return ResourceRequired.class.getSimpleName() + " - " + resourceId + ":" + amountRequired;
        }

        @Override
        public String getResourceId() {
            return resourceId;
        }

        @Override
        public double getAmountRequired() {
            return amountRequired;
        }

        @Override
        public double getAmountRequiredBackup() {
            return amountRequiredBackup;
        }
    }

    public static class RowFilter extends Auto implements AssemblyAPI.RowFilter {
        public ItemFilter first;
        public ItemFilter second;

        public void update() {
            if (address <= 0) return;
            long firstAddress = Main.API.readMemoryLong(address + 0x20);
            if (firstAddress > 0) {
                first = new ItemFilter();
                first.update(firstAddress);
            }
            long secondAddress = Main.API.readMemoryLong(address + 0x28);
            if (secondAddress > 0) {
                second = new ItemFilter();
                second.update(secondAddress);
            }
        }

        @Override
        public String toString() {
            return RowFilter.class.getSimpleName() + " - " + (first == null ? "null" : (first.filter + ":" + first.isChecked())) + " - " + (second == null ? "null" : (second.filter + ":" + second.isChecked()));
        }

        @Override
        public AssemblyAPI.ItemFilter getFirst() {
            return first;
        }

        @Override
        public AssemblyAPI.ItemFilter getSecond() {
            return second;
        }
    }

    public static class ItemFilter extends Auto implements AssemblyAPI.ItemFilter {
        public String filter = "";
        public boolean isChecked;

        public void update() {
            if (address <= 0) return;
            filter = API.readString(address, 0x20);
            long isCheckedAddress = Main.API.readMemoryLong(address + 0x28);
            isChecked = API.readBoolean(isCheckedAddress + 0x1D0);
        }

        @Override
        public String toString() {
            return ItemFilter.class.getSimpleName() + " - " + filter + " - " + isChecked;
        }

        @Override
        public String getFilterName() {
            return filter;
        }

        @Override
        public boolean isChecked() {
            return isChecked;
        }
    }

    public static class Filter implements AssemblyAPI.Filter {
        String filter = "";
        int row, col = -1;
        boolean isChecked = false;

        public Filter(AssemblyAPI.ItemFilter itemFilter, int row, int col) {
            this.filter = itemFilter.getFilterName();
            this.row = row;
            this.col = col;
            this.isChecked = itemFilter.isChecked();
        }

        @Override
        public String getFilterName() {
            return filter;
        }

        @Override
        public int getRow() {
            return row;
        }

        @Override
        public int getCol() {
            return col;
        }

        @Override
        public boolean isChecked() {
            return isChecked;
        }
    }
}
