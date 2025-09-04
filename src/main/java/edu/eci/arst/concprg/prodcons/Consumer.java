package edu.eci.arst.concprg.prodcons;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author hcadavid
 */
public class Consumer extends Thread {

    private final BlockingQueue<Integer> queue;
    private final int consumptionDelayMs;

    public Consumer(BlockingQueue<Integer> queue, int consumptionDelayMs) {
        this.queue = queue;
        this.consumptionDelayMs = consumptionDelayMs;
    }

    @Override
    public void run() {
        try {
            while (true) {
                pollFromQueue();
                if (consumptionDelayMs > 0) {
                    Thread.sleep(consumptionDelayMs);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void pollFromQueue() {
        int item = queue.poll();
        System.out.println("Consumer consumes " + item);
    }
}
