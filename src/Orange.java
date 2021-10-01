/**
 * Orange is a class used for testing multithreading with Plant and Worker. Each instance of Orange
 * has an enumeration that shows its "state" (i.e. fetched, peeled) and a thread can do
 * "work" on the Orange which delays the thread for an abstract amount of time to
 * simulate the work and then gets the next "state".
 */
public class Orange {
    //Enumeration for state of the orange
    public enum State {
        Fetched(15),
        Peeled(38),
        Squeezed(29),
        Bottled(17),
        Processed(1);

        //Length-1 for use in indexes
        private static final int finalIndex = State.values().length - 1;

        //Arbitrary time so oranges take a while to process
        final int timeToComplete;

        //Constructor for State
        State(int timeToComplete) {
            this.timeToComplete = timeToComplete;
        }

        //Method for State that gets the next state of the orange
        State getNext() {
            int currIndex = this.ordinal();
            if (currIndex >= finalIndex) {
                throw new IllegalStateException("Already at final state");
            }
            return State.values()[currIndex + 1];
        }
    }

    //Class variable for the State
    private State state;

    //Constructor for Orange
    public Orange() {
        state = State.Fetched;
        doWork();
    }

    //Get the state of the orange
    public State getState() {
        return state;
    }

    //Run the next step of the orange's state
    public void runProcess() {
        // Don't attempt to process an already completed orange
        if (state == State.Processed) {
            throw new IllegalStateException("This orange has already been processed");
        }
        doWork();
        state = state.getNext();
    }

    private void doWork() {
        // Sleep for the amount of time necessary to do the work
        try {
            Thread.sleep(state.timeToComplete);
        } catch (InterruptedException e) {
            System.err.println("Incomplete orange processing, juice may be bad");
        }
    }
}