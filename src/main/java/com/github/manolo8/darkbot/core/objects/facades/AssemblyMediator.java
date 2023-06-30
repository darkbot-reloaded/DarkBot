package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.AssemblyAPI;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class AssemblyMediator extends Updatable implements AssemblyAPI {
    public int selectedRecipeIndex;
    public Recipe selectedRecipe = new Recipe();
    public CheckBox filterDropdown = new CheckBox();
    public List<Recipe> recipes = new ArrayList<>();
    private final ObjArray recipesPtr = ObjArray.ofVector(true);
    public List<RowSetting> rowSettings = new ArrayList<>();
    private final ObjArray rowSettingsArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        if (address == 0) return;
        //get index of current selected index
        selectedRecipeIndex = Main.API.readInt(address + 0x48);

        //get list of shown recipe
        long recipeCollectionAddress = Main.API.readMemoryLong(address + 0x60) & ByteUtils.ATOM_MASK;
        recipesPtr.update(Main.API.readMemoryLong(recipeCollectionAddress + 0x20) & ByteUtils.ATOM_MASK);
        synchronized (Main.UPDATE_LOCKER) {
            recipesPtr.sync(recipes, Recipe::new);
        }

        //get selected recipe info
        long selectedRecipeAddress = Main.API.readMemoryLong(address + 0x70) & ByteUtils.ATOM_MASK;
        selectedRecipe.update(selectedRecipeAddress);

        //get list of selected filters
        long itemFilterViewController = Main.API.readMemoryLong(address + 0x78) & ByteUtils.ATOM_MASK;
        rowSettingsArr.update(Main.API.readMemoryLong(itemFilterViewController + 0xb0) & ByteUtils.ATOM_MASK);
        synchronized (Main.UPDATE_LOCKER) {
            rowSettingsArr.sync(rowSettings, RowSetting::new);
        }

        //get filter drop down is open
        filterDropdown.update(Main.API.readMemoryLong(itemFilterViewController + 0x60) & ByteUtils.ATOM_MASK);

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
    public AssemblyAPI.CheckBox getFilterDropdown() {
        return filterDropdown;
    }

    @Override
    public List<? extends AssemblyAPI.Recipe> getRecipes() {
        return recipes;
    }

    @Override
    public List<? extends AssemblyAPI.RowSetting> getRowSettings() {
        return rowSettings;
    }

    public static class Recipe extends Auto implements AssemblyAPI.Recipe {
        public ItemVo itemVo = new ItemVo();
        public List<ItemVo> rewards = new ArrayList<>();
        public final ObjArray rewardsArr = ObjArray.ofVector(true);

        public List<ResourceRequired> resourcesRequired = new ArrayList<>();
        private final ObjArray resourcesRequiredArr = ObjArray.ofVector(true);

        public boolean isCraftable = false;

        @Override
        public void update() {
            if (address == 0) return;
            isCraftable = API.readBoolean(address + 0x20);

            long itemVoAddress = Main.API.readMemoryLong(address + 0x58) & ByteUtils.ATOM_MASK;
            itemVo.update(itemVoAddress);

            long rewardsArrAddress = Main.API.readMemoryLong(address + 0x60) & ByteUtils.ATOM_MASK;
            rewardsArr.update(rewardsArrAddress);
            rewardsArr.sync(rewards, ItemVo::new);

            long resourcesRequiredArrAddress = Main.API.readMemoryLong(address + 0x50) & ByteUtils.ATOM_MASK;
            resourcesRequiredArr.update(resourcesRequiredArrAddress);
            resourcesRequiredArr.sync(resourcesRequired, ResourceRequired::new);
        }

        @Override
        public String toString() {
            return Recipe.class.getSimpleName() + " - " + itemVo.lootId + ":" + isCraftable;
        }

        @Override
        public AssemblyAPI.ItemVo getItemVo() {
            return itemVo;
        }

        @Override
        public List<? extends AssemblyAPI.ItemVo> getRewards() {
            return rewards;
        }

        @Override
        public List<? extends AssemblyAPI.ResourceRequired> getResourcesRequired() {
            return resourcesRequired;
        }

        @Override
        public boolean getIsCraftable() {
            return isCraftable;
        }
    }

    public static class ItemVo extends Auto implements AssemblyAPI.ItemVo {
        public String lootId = "";

        public void update() {
            if (address <= 0) return;
            this.lootId = API.readMemoryString(address, 0x48);
        }

        @Override
        public String getLootId() {
            return lootId;
        }

        @Override
        public String toString() {
            return ItemVo.class.getSimpleName() + " - " + lootId;
        }
    }

    public static class ResourceRequired extends Auto implements AssemblyAPI.ResourceRequired {
        public ItemVo itemVo = new ItemVo();
        public double amountRequired, amountRequiredBackup = -1.0;

        public void update() {
            if (address <= 0) return;
            long itemvo = Main.API.readMemoryLong(address + 0x28) & ByteUtils.ATOM_MASK;
            itemVo.update(itemvo);
            amountRequired = API.readDouble(address + 0x30);
            //this also gives back same value, not sure which is correct
            amountRequiredBackup = API.readDouble(address + 0x38);
        }

        @Override
        public String toString() {
            return ResourceRequired.class.getSimpleName() + " - " + itemVo.lootId + ":" + amountRequired;
        }

        @Override
        public AssemblyAPI.ItemVo getItemVo() {
            return itemVo;
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

    public static class RowSetting extends Auto implements AssemblyAPI.RowSetting {
        public RowEntryVO first = new RowEntryVO();
        public RowEntryVO second = new RowEntryVO();

        public void update() {
            if (address <= 0) return;
            first.update(Main.API.readMemoryLong(address + 0x20) & ByteUtils.ATOM_MASK);
            second.update(Main.API.readMemoryLong(address + 0x28) & ByteUtils.ATOM_MASK);
        }

        @Override
        public String toString() {
            return RowSetting.class.getSimpleName() + " - " + first.filter + ":" + first.checkBox.getIsChecked() + " - " + second.filter + ":" + second.checkBox.getIsChecked();
        }

        @Override
        public AssemblyAPI.RowEntryVO getFirst() {
            return first;
        }

        @Override
        public AssemblyAPI.RowEntryVO getSecond() {
            return second;
        }
    }

    public static class RowEntryVO extends Auto implements AssemblyAPI.RowEntryVO {
        public String filter = "";
        public CheckBox checkBox = new CheckBox();

        public void update() {
            if (address <= 0) return;
            filter = API.readString(address, 0x20);
            checkBox.update(Main.API.readMemoryLong(address + 0x28) & ByteUtils.ATOM_MASK);
        }

        @Override
        public String toString() {
            return RowEntryVO.class.getSimpleName() + " - " + filter + " - " + checkBox.getIsChecked();
        }

        @Override
        public String getFilter() {
            return filter;
        }

        @Override
        public AssemblyAPI.CheckBox getCheckBox() {
            return checkBox;
        }
    }

    public static class CheckBox extends Auto implements AssemblyAPI.CheckBox {
        public boolean isChecked = false;

        public void update() {
            if (address <= 0) return;
            isChecked = API.readBoolean(address + 0x1D0);
        }

        @Override
        public String toString() {
            return CheckBox.class.getSimpleName() + " - " + isChecked;
        }

        @Override
        public boolean getIsChecked() {
            return isChecked;
        }
    }
}
