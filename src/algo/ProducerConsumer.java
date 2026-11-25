package algo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class Producer implements Runnable{

    private final BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 5; i++) {
                queue.put(i);
                System.out.println("produced:" + i);
                Thread.sleep(1000L);
            }
            queue.put(-1);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

}

class Consumer implements Runnable{

    private final BlockingQueue<Integer> queue;
    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        int i;
        try {
            while (true) {
                i = queue.take();
                if (i == -1) {
                    break;
                }
                System.out.println("Consumed:" + i);
                Thread.sleep(2000L);
            }
        }  catch (InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }
}

public class ProducerConsumer {
    public static void main(String[] args){
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3);
        Thread producer = new Thread(new Producer(queue));
        Thread consumer = new Thread(new Consumer(queue));
        producer.start();
        consumer.start();
    }
}