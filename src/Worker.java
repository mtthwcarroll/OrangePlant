/**
 * Worker is a class to test multithreading with Plant and Orange. Workers are created by
 * a plant thread and get their Oranges from the plant's data structure (orangePile). The workers
 * "work" on the Oranges and then put them back on the orangePile or throw them out if the Orange is
 * in the state "Processed" and tells the plant an Orange has been processed.
 */
public class Worker implements Runnable{
    //Class variables
    private volatile boolean working;
    public final Thread thread;
    public final Plant plant;

    //Constructor for worker, uses same naming convention as Plant + the plant the worker is from
    Worker(int threadNum, Plant p) {
        thread = new Thread(this, "Worker ["+threadNum+"]" + " " + p.thread.getName());
        this.plant = p;
        working = false;
    }

    //Signal to the worker to start working and start their thread
    public void startWorking() {
        working = true;
        thread.start();
    }

    /**
     * While the worker is working they will get an orange from the plant and run
     * the next process for the orange. After, if the orange is fully processed they will discard it and
     * run incProcessedOranges otherwise they will add it back to the plant's orangePile.
     */
    public void run() {
        while (working) {
            Orange o = plant.getOrange();
            System.out.println(thread.getName() + " " + o.getState() + " orange");
            o.runProcess();
            if(o.getState() == Orange.State.Processed) {
                plant.incProcessedOranges();
            } else {
                plant.addOrange(o);
            }
        }
        System.out.println(thread.getName() + " Done");
    }

    public void stopWorking() {
        working = false;
    }

}