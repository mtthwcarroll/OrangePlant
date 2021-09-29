import java.util.LinkedList;

public class Plant implements Runnable {
    // How long do we want to run the juice processing
    public static final long PROCESSING_TIME = 5 * 1000;

    private static final int NUM_PLANTS = 2;
    private static final int NUM_WORKERS = 4;

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

    private static void delay(long time, String errMsg) {
        long sleepTime = Math.max(1, time);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println(errMsg);
        }
    }

    public final int ORANGES_PER_BOTTLE = 3;
    private final Thread thread;
    private int orangesProvided;
    private int orangesProcessed;
    private volatile boolean timeToWork;
    private Worker[] workers;

    //Linked lists for keeping oranges stored.
    private volatile LinkedList<Orange> fetchedOranges = new LinkedList<Orange>();
    private volatile LinkedList<Orange> peeledOranges = new LinkedList<Orange>();
    private volatile LinkedList<Orange> squeezedOranges = new LinkedList<Orange>();

    Plant(int threadNum) {
        orangesProvided = 0;
        orangesProcessed = 0;
        thread = new Thread(this, "Plant[" + threadNum + "]");
        workers = new Worker[NUM_WORKERS];

        for(int i = 0; i < NUM_WORKERS; i++) {
            workers[i] = new Worker(i, thread.getName());
        }
    }

    private synchronized Orange giveOrange(LinkedList<Orange> list, Worker w) {
        if(!w.checkWorkingOnOrange() && list.size() > 0) {
            Orange o = w.processOrange(list.removeLast());
            return o;
        } else {
            return null;
        }
    }

    private synchronized void addOrange(LinkedList<Orange> list, Orange o) {
        list.add(o);
    }

    public void startPlant() {
        timeToWork = true;
        for(Worker w:workers) {
            w.startWorking();
        }
        thread.start();
    }

    public synchronized void stopPlant() {
        for(Worker w : workers) {
            w.stopWorking();
        }
        timeToWork = false;
    }

/*    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }*/

    public void run() {
        System.out.print(Thread.currentThread().getName() + " Processing oranges");
        while (timeToWork) {
            if(!workers[0].checkWorkingOnOrange()) {
                Orange o = workers[0].processOrange(new Orange());
                addOrange(fetchedOranges, o);
                ++orangesProvided;
            }
            Orange o1 = giveOrange(fetchedOranges, workers[1]);
            if(o1 != null) {
                addOrange(peeledOranges, o1);
            }
            Orange o2 = giveOrange(peeledOranges, workers[2]);
            if(o2 != null) {
                addOrange(squeezedOranges, o2);
            }
            Orange o3 = giveOrange(squeezedOranges, workers[3]);
            if(o3 != null) {
                ++orangesProcessed;
            }
        }
        System.out.println("");
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    private synchronized int getListLength(LinkedList<Orange> list) {
        return list.size();
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
        return orangesProcessed % ORANGES_PER_BOTTLE;
    }
}