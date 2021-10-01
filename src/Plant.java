import java.util.LinkedList;
import java.util.List;

/**
 * Plant is used to test multithreading with Orange and Worker. Each plant is its own thread and will create
 * several Worker threads that will work on Oranges. The goal is to avoid race conditions and
 * "stale" data while multiple threads are working at the same time on the same data.
 */
public class Plant implements Runnable {
    //final class vars
    public static final long PROCESSING_TIME = 5 * 1000;
    private static final int NUM_PLANTS = 2;
    private static final int NUM_WORKERS = 4;
    public final int ORANGES_PER_BOTTLE = 3;

    //Class vars
    public final Thread thread;
    private int orangesProvided;
    private int orangesProcessed;
    private volatile boolean timeToWork;
    private Worker[] workers;
    //Linked list for keeping oranges stored.
    private final List<Orange> orangePile = new LinkedList<Orange>();

    //Main thread
    public static void main(String[] args) {
        // Startup the plants
        Plant[] plants = new Plant[NUM_PLANTS];
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i);
            plants[i].startPlant();
        }

        // Give the plants time to do work
        delay(PROCESSING_TIME, "Plant malfunction");

        // Stop the plant, and wait for it to shutdown
        for (Plant p : plants) {
            p.stopPlant();
        }
        for (Plant p : plants) {
            try {
                p.thread.join();
            } catch (InterruptedException e) {
                System.err.println(p.thread.getName() + " stop malfunction");
            }
        }

        // Summarize the results
        int totalProvided = 0;
        int totalProcessed = 0;
        int totalBottles = 0;
        int totalWasted = 0;
        for (Plant p : plants) {
            totalProvided += p.getProvidedOranges();
            totalProcessed += p.getProcessedOranges();
            totalBottles += p.getBottles();
            totalWasted += p.getWaste();
        }
        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println("Created " + totalBottles +
                ", wasted " + totalWasted + " oranges");
    }

    //Delay to let plants work
    private static void delay(long time, String errMsg) {
        long sleepTime = Math.max(1, time);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println(errMsg);
        }
    }

    /**
     * @param threadNum
     */
    Plant(int threadNum) {
        orangesProvided = 0;
        orangesProcessed = 0;
        thread = new Thread(this, "Plant[" + threadNum + "]");
        workers = new Worker[NUM_WORKERS];
        for(int i = 0; i < NUM_WORKERS; i++) {
            workers[i] = new Worker(i, this);
        }
    }

    /**
     * Method to be called by workers who want an orange. It will either give them the
     * orange on top of the orangePile or make a new orange to give them. Synch to avoid multithreading issues
     * @return Orange
     */
    public synchronized Orange getOrange() {
        int size = getPileSize();
        if(size > 0) {
            return orangePile.remove(size-1);
        } else {
            incProvidedOranges();
            return new Orange();
        }
    }

    //Starts the plant thread then the worker threads.
    public void startPlant() {
        timeToWork = true;
        thread.start();
        for(Worker w:workers) {
            w.startWorking();
        }
    }

    //calls the workers stop and then sets the plants working flag to false
    public synchronized void stopPlant() {
        for(Worker w : workers) {
            w.stopWorking();
        }
        timeToWork = false;
    }

    public void run() {
        System.out.print(Thread.currentThread().getName() + " Processing oranges");
        /*
        Plant constantly produces oranges while running. If the plant can't keep up with the workers,
        the workers can make their own oranges in getOrange().
         */
        while (timeToWork) {
            addOrange(new Orange());
            incProvidedOranges();
        }
        //Joining workers so that the plant doesn't stop before the workers stop
        try {
            for(Worker w : workers) {
                w.thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    //Synchronized get and add methods to insure data consistency.
    public synchronized void addOrange(Orange o) {
        orangePile.add(o);
    }

    public synchronized void incProcessedOranges() {
        orangesProcessed++;
    }

    public synchronized void incProvidedOranges() {
        orangesProvided++;
    }

    private synchronized int getPileSize() {
        return orangePile.size();
    }

    public int getProvidedOranges() {
        return orangesProvided;
    }

    public int getProcessedOranges() {
        return orangesProcessed;
    }

    public int getBottles() {
        return orangesProcessed / ORANGES_PER_BOTTLE;
    }

    public int getWaste() {
        return ((orangesProvided - orangesProcessed)+(orangesProcessed%ORANGES_PER_BOTTLE));
    }
}