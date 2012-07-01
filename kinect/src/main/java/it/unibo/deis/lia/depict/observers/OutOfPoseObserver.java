package it.unibo.deis.lia.depict.observers;

import it.unibo.deis.lia.depict.KinectModel;
import org.OpenNI.*;


public class OutOfPoseObserver implements IObserver<PoseDetectionEventArgs> {

    private KinectModel model;
    private SkeletonCapability skeletonCap;
    private PoseDetectionCapability poseDetectionCap;

    public OutOfPoseObserver( KinectModel model, SkeletonCapability skeletonCap, PoseDetectionCapability poseDetectionCap ) {
        this.model = model;
        this.skeletonCap = skeletonCap;
        this.poseDetectionCap = poseDetectionCap;
    }

    public void update( IObservable<PoseDetectionEventArgs> poseDetectionEventArgsIObservable, PoseDetectionEventArgs poseDetectionEventArgs ) {
        int userId = poseDetectionEventArgs.getUser();
        String pose = poseDetectionEventArgs.getPose();
        
        model.outOfPose( userId, pose );
    }
}
