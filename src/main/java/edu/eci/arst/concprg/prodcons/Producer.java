package edu.eci.arst.concprg.prodcons;

import java.util.Queue;
import java.util.Random;

/**
 *
 * @author hcadavid
 */
public class Producer extends Thread {

    private final Queue<Integer> queue;

    private int dataSeed = 0;
    private Random rand = null;
    private final long stockLimit;

    public Producer(Queue<Integer> queue, long stockLimit) {
        this.queue = queue;
        rand = new Random(System.currentTimeMillis());
        this.stockLimit = stockLimit;
    }

    @Override
    public void run() {

        try {
            while (true) {
                addToQueue();
                synchronized (queue) {
                    queue.add(dataSeed);
                    queue.notifyAll();
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void addToQueue(){
        dataSeed = dataSeed + rand.nextInt(100);
        System.out.println("Producer added " + dataSeed);
    }
}

