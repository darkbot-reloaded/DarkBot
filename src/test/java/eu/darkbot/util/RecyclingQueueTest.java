package eu.darkbot.util;

import com.github.manolo8.darkbot.utils.data.RecyclingQueue;
import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

public class RecyclingQueueTest {

    @Test
    public void testRecyclingQueue() {
        RecyclingQueue<Value> queue = new RecyclingQueue<>(Value::new);

        Assert.assertThrows(NoSuchElementException.class, queue::get);
        Assert.assertThrows(NoSuchElementException.class, queue::remove);

        queue.add().val = 1;
        queue.add().val = 2;
        Assert.assertEquals(2, queue.size());

        queue.remove();
        queue.remove();
        Assert.assertEquals(0, queue.size());
        Assert.assertThrows(NoSuchElementException.class, queue::get);
        Assert.assertThrows(NoSuchElementException.class, queue::remove);

        // Ensure it recycles the values
        Assert.assertEquals(1, queue.add().val);
        Assert.assertEquals(2, queue.add().val);

        // Assert a new node will be fresh
        Assert.assertEquals(0, queue.add().val);

        Assert.assertEquals(3, queue.size());

        queue.remove();
        Assert.assertEquals(2, queue.size());

        // Ensure it recycles the value
        Assert.assertEquals(1, queue.add().val);
    }


    private static class Value {
        int val;
    }

}
