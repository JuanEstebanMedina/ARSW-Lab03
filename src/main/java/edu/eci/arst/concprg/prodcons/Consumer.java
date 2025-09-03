package edu.eci.arst.concprg.prodcons;

import java.util.Queue;

/**
 *
 * @author hcadavid
 */
public class Consumer extends Thread {

    private final Queue<Integer> queue;

    public Consumer(Queue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        queue.wait();
                    }
                    pollFromQueue();
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
