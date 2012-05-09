package org.OpenNI.Samples.UserTracker.observers;

import http.it.unibo.deis.lia.depict.KinectModel;
import org.OpenNI.*;

public class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs> {

    private KinectModel model;
    private SkeletonCapability skeletonCap;
    private PoseDetectionCapability poseDetectionCap;

    public PoseDetectedObserver( KinectModel model, SkeletonCapability skeletonCap, PoseDetectionCapability poseDetectionCap ) {
        this.model = model;
        this.skeletonCap = skeletonCap;
        this.poseDetectionCap = poseDetectionCap;
    }

    public void update( IObservable<PoseDetectionEventArgs> observable,
                       PoseDetectionEventArgs args ) {
        try {
            if ( args.getPose().equals( skeletonCap.getSkeletonCalibrationPose() ) ) {
                poseDetectionCap.stopPoseDetection(args.getUser());
                skeletonCap.requestSkeletonCalibration( args.getUser(), true );
            } else {
                model.inPose( args.getUser(), args.getPose() );
            }
        } catch (StatusException e) {
            e.printStackTrace();
        }
    }
}