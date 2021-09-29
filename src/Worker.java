public class Worker implements Runnable{
    //Class variables
    private volatile boolean working; //Flag for running thread
    private volatile boolean workingOnOrange; //Flag if busy
    public final Thread thread; //The thread that runs

    //Constructor for worker, uses same naming convention as Plant
    Worker(int threadNum, String plant) {
        thread = new Thread(this, "Worker ["+threadNum+"]" + " " + plant);
        working = false;
        workingOnOrange = false;
    }

    //Signal to the worker to start working
    public void startWorking() {
        working = true;
        thread.start();
    }

    public void run() {
        while(checkWorking()) {
        }
    }

    public synchronized boolean checkWorking() {
        if(working) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean checkWorkingOnOrange() {
        if(workingOnOrange) {
            return true;
        } else {
            return false;
        }
    }

    public Orange processOrange(Orange o) {
        if (o.getState() != Orange.State.Processed) {
            workingOnOrange = true;
            System.out.println(thread.getName()+" "+o.getState()+" orange");
            o.runProcess();
        } else {
            System.out.println("Error: already processed orange handed to worker");
        }
        workingOnOrange = false;
        return o;
    }

    public synchronized void stopWorking() {
        working = false;
    }

}
