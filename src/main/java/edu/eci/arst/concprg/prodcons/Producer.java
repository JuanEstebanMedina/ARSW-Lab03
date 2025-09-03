package edu.eci.arst.concprg.prodcons;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author hcadavid
 */
public class Producer extends Thread {

    private final int productionDelayMs;
    private final BlockingQueue<Integer> queue;

    private int dataSeed = 0;
    private Random rand = null;
    // private final long stockLimit;

    public Producer(BlockingQueue<Integer> queue, long stockLimit, int productionDelayMs) {
        this.queue = queue;
        rand = new Random(System.currentTimeMillis());
        this.productionDelayMs = productionDelayMs;
        // this.stockLimit = stockLimit;
    }

    @Override
    public void run() {
        try {
            while (true) {
                addToQueue();

                if (productionDelayMs > 0) {
                    Thread.sleep(productionDelayMs);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void addToQueue() throws InterruptedException {
        if (queue.remainingCapacity() > 0) {
            dataSeed = dataSeed + rand.nextInt(100);
            System.out.println("Producer added " + dataSeed);
            queue.put(dataSeed);
        }
    }
}
