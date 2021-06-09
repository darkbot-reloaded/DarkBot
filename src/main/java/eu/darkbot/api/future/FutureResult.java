package eu.darkbot.api.future;

import java.util.Optional;
import java.util.function.Consumer;

public interface FutureResult<R> {

    /**
     * @return {@link Optional#empty()} if {@link FutureResult#isDone()} is false
     */
    Optional<R> getResult();

    /**
     * @return current {@link Status} of this {@link FutureResult}
     */
    Status getStatus();

    /**
     * Will try to cancel this {@link FutureResult}
     * If this method returns true that means execution of this Future is canceled/interrupted
     *
     * May return false if current status is already {@link Status#COMMITTED}
     * and mayInterrupt parameter is set to false.
     *
     * @param mayInterrupt should interrupt on {@link Status#COMMITTED} status.
     * @return true if is canceled or interrupted
     */
    boolean cancel(boolean mayInterrupt);

    default boolean isDone() {
        return isCompleted() || isCanceled() || isInterrupted();
    }

    default boolean isCanceled() {
        return getStatus() == Status.CANCELED;
    }

    default boolean isPending() {
        return getStatus() == Status.PENDING;
    }

    default boolean isCommitted() {
        return getStatus() == Status.COMMITTED;
    }

    default boolean isCompleted() {
        return getStatus() == Status.COMPLETED;
    }

    default boolean isInterrupted() {
        return getStatus() == Status.INTERRUPTED;
    }

    FutureResult<R> onDone(Consumer<FutureResult<R>> consumer);

    enum Status {
        PENDING,
        COMMITTED,
        COMPLETED,
        CANCELED,
        INTERRUPTED
    }
}
