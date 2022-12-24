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
        Assert.assertEquals(1, queue.size());

        queue.remove();
        Assert.assertEquals(0, queue.size());
        Assert.assertThrows(NoSuchElementException.class, queue::get);
        Assert.assertThrows(NoSuchElementException.class, queue::remove);

        // Ensure it recycles the value
        Assert.assertEquals(1, queue.add().val);

        for (int i = 2; i <= 10; i++)
            queue.add().val = i;
        Assert.assertEquals(10, queue.size());

        queue.remove();
        Assert.assertEquals(9, queue.size());


        // Ensure it recycles the value
        Assert.assertEquals(1, queue.add().val);
    }


    private static class Value {
        int val;
    }

}
