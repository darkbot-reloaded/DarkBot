package eu.darkbot.util;

import com.github.manolo8.darkbot.utils.data.RecyclingQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

public class RecyclingQueueTest {

    @Test
    public void testRecyclingQueue() {
        RecyclingQueue<Value> queue = new RecyclingQueue<>(Value::new);

        Assertions.assertThrows(NoSuchElementException.class, queue::get);
        Assertions.assertThrows(NoSuchElementException.class, queue::remove);

        queue.add().val = 1;
        queue.add().val = 2;
        Assertions.assertEquals(2, queue.size());

        queue.remove();
        queue.remove();
        Assertions.assertEquals(0, queue.size());
        Assertions.assertThrows(NoSuchElementException.class, queue::get);
        Assertions.assertThrows(NoSuchElementException.class, queue::remove);

        // Ensure it recycles the values
        Assertions.assertEquals(1, queue.add().val);
        Assertions.assertEquals(2, queue.add().val);

        // Assert a new node will be fresh
        Assertions.assertEquals(0, queue.add().val);

        Assertions.assertEquals(3, queue.size());

        queue.remove();
        Assertions.assertEquals(2, queue.size());

        // Ensure it recycles the value
        Assertions.assertEquals(1, queue.add().val);
    }


    private static class Value {
        int val;
    }

}
