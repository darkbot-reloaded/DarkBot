package eu.darkbot.api.future;

import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.api.objects.Item;

import java.util.Optional;
import java.util.function.Consumer;

public interface ItemFutureResult extends FutureResult<HeroItemsAPI.UsageResult> {

    ItemFutureResult EMPTY = new CanceledItemFutureResult();

    Item getItem();

    class CanceledItemFutureResult implements ItemFutureResult {

        @Override
        public Optional<HeroItemsAPI.UsageResult> getResult() {
            return Optional.of(HeroItemsAPI.UsageResult.UNSUCCESSFUL);
        }

        @Override
        public Status getStatus() {
            return Status.CANCELED;
        }

        @Override
        public boolean cancel(boolean mayInterrupt) {
            return true;
        }

        @Override
        public FutureResult<HeroItemsAPI.UsageResult> onDone(Consumer<FutureResult<HeroItemsAPI.UsageResult>> consumer) {
            return this;
        }

        @Override
        public Item getItem() {
            return null;
        }
    }
}
