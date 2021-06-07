package eu.darkbot.impl.future;

import eu.darkbot.api.future.FutureResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractFutureResult<R> implements FutureResult<R> {

    private R result;
    private Status status = Status.PENDING;
    private List<Consumer<FutureResult<R>>> completeListeners;

    @Override
    public Optional<R> getResult() {
        return Optional.ofNullable(result);
    }

    protected void set(R result) {
        this.result = result;
        setStatus(Status.COMPLETED);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    protected void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean cancel(boolean mayInterrupt) {
        if (isPending()) setStatus(Status.CANCELED);
        if (isCommitted() && mayInterrupt) setStatus(Status.INTERRUPTED);

        return isCanceled() || isInterrupted();
    }

    @Override
    public FutureResult<R> onDone(Consumer<FutureResult<R>> consumer) {
        if (this.completeListeners == null)
            this.completeListeners = new ArrayList<>();

        this.completeListeners.add(consumer);
        return this;
    }

    protected void fireListeners() {
        for (Consumer<FutureResult<R>> futureResult : this.completeListeners)
            futureResult.accept(this);
    }
}
