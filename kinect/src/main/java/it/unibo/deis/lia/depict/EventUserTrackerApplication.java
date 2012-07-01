package it.unibo.deis.lia.depict;

public class EventUserTrackerApplication {

    private EventUserTracker tracker;

    private boolean render = true;
//    private boolean render = false;

    public EventUserTrackerApplication() {
        this.tracker = new EventUserTracker( KinectModelFactory.MODEL_TYPES.DROOLS, render );
    }

    void run() {
        tracker.run();
    }

    public static void main(String s[]) {
        new EventUserTrackerApplication().run();
    }

}
