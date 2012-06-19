package http.it.unibo.deis.lia.depict.observers;


import http.it.unibo.deis.lia.depict.KinectModel;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.UserEventArgs;

public class LostUserObserver implements IObserver<UserEventArgs> {

    private KinectModel model;

    public LostUserObserver( KinectModel model ) {
        this.model = model;
    }

    public void update(IObservable<UserEventArgs> observable,
                       UserEventArgs args) {
        model.removeUser( args.getId() );
    }
}