package eu.darkbot.api.future;

import eu.darkbot.api.objects.Item;

public interface ItemFutureResult<R> extends FutureResult<R> {

    Item getItem();
}
