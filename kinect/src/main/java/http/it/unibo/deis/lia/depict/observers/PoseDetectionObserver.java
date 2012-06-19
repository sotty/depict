package http.it.unibo.deis.lia.depict.observers;

import http.it.unibo.deis.lia.depict.KinectModel;
import org.OpenNI.*;


public class PoseDetectionObserver implements IObserver<PoseDetectionInProgressEventArgs>  {

    private KinectModel model;
    private SkeletonCapability skeletonCap;
    private PoseDetectionCapability poseDetectionCap;

    public PoseDetectionObserver( KinectModel model, SkeletonCapability skeletonCap, PoseDetectionCapability poseDetectionCap ) {
        this.model = model;
        this.skeletonCap = skeletonCap;
        this.poseDetectionCap = poseDetectionCap;
    }


    public void update(IObservable<PoseDetectionInProgressEventArgs> poseDetectionInProgressEventArgsIObservable, PoseDetectionInProgressEventArgs args) {
        try {
            if ( args.getPose().equals( skeletonCap.getSkeletonCalibrationPose() ) ) {
                poseDetectionCap.stopPoseDetection(args.getUser());
                skeletonCap.requestSkeletonCalibration( args.getUser(), true );
            } else {
                model.detectingPose( args.getUser(), args.getPose(), args.getStatus() );
            }
        } catch (StatusException e) {
            e.printStackTrace();
        }
    }
}
