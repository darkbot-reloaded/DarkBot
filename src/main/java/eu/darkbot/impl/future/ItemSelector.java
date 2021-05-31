package eu.darkbot.impl.future;

import com.github.manolo8.darkbot.core.manager.FacadeManager;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import eu.darkbot.api.future.ItemFutureResult;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.impl.utils.AbstractFutureResult;

public class ItemSelector
        extends AbstractFutureResult<HeroItemsAPI.UsageResult>
        implements ItemFutureResult<HeroItemsAPI.UsageResult>, Runnable {

    private final Item item;
    private final SettingsProxy settings;
    private final SlotBarsProxy slotBars;

    public ItemSelector(Item item, FacadeManager facadeManager) {
        this.item = item;
        this.settings = facadeManager.settings;
        this.slotBars = facadeManager.slotBars;
    }

    @Override
    public void run() {
        if (isPending()) setStatus(Status.COMMITTED);

        if (!item.hasShortcut() || !item.isActivatable()) set(HeroItemsAPI.UsageResult.NOT_AVAILABLE);
        else if (!item.isReady()) set(HeroItemsAPI.UsageResult.ON_COOLDOWN);
        else {
            SlotBarsProxy.Type slotBarType = item.getSlotBarType();
            int slotNumber = item.getFirstSlotNumber();

            if (slotBarType == null || slotNumber == -1) set(HeroItemsAPI.UsageResult.UNSUCCESSFUL);
            else {
                boolean toggleProAction = (slotBarType == SlotBarsProxy.Type.PRO_ACTION_BAR) != slotBars.isProActionBarVisible();

                if ((!toggleProAction || settings.pressKeybind(SettingsProxy.KeyBind.TOGGLE_PRO_ACTION))
                        && settings.pressKeybind(SettingsProxy.KeyBind.of(slotBarType, slotNumber)))
                    set(HeroItemsAPI.UsageResult.SELECTED);
                else set(HeroItemsAPI.UsageResult.UNSUCCESSFUL);
            }
        }

        fireListeners();
    }

    @Override
    public Item getItem() {
        return item;
    }
}
