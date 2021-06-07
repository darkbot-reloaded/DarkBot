package eu.darkbot.api.future;

import eu.darkbot.api.items.ItemUseResult;
import eu.darkbot.api.items.Item;

import java.util.Optional;
import java.util.function.Consumer;

public interface ItemFutureResult extends FutureResult<ItemUseResult> {

    ItemFutureResult EMPTY = new CanceledItemFutureResult();

    Item getItem();

    class CanceledItemFutureResult implements ItemFutureResult {

        @Override
        public Optional<ItemUseResult> getResult() {
            return Optional.of(ItemUseResult.FAILED);
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
        public FutureResult<ItemUseResult> onDone(Consumer<FutureResult<ItemUseResult>> consumer) {
            return this;
        }

        @Override
        public Item getItem() {
            return null;
        }
    }
}
