package http.it.unibo.deis.lia.depict.observers;

import http.it.unibo.deis.lia.depict.KinectModel;
import org.OpenNI.*;

public class CalibrationCompleteObserver implements IObserver<CalibrationProgressEventArgs> {

    private KinectModel model;
    private SkeletonCapability skeletonCap;
    private PoseDetectionCapability poseDetectionCap;

    public CalibrationCompleteObserver( KinectModel model, SkeletonCapability skeletonCap, PoseDetectionCapability poseDetectionCap) {
        this.model = model;
        this.skeletonCap = skeletonCap;
        this.poseDetectionCap = poseDetectionCap;
    }

    public void update(IObservable<CalibrationProgressEventArgs> observable,
                       CalibrationProgressEventArgs args)
    {
        System.out.println("Calibration complete: " + args.getStatus());
        try
        {
            if (args.getStatus() == CalibrationProgressStatus.OK)
            {
                System.out.println("Starting tracking "  +args.getUser());
                skeletonCap.startTracking(args.getUser());
                model.calibrationComplete( args.getUser() );
            }
            else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT)
            {
                if (skeletonCap.needPoseForCalibration())
                {
                    poseDetectionCap.startPoseDetection(skeletonCap.getSkeletonCalibrationPose(), args.getUser());
                }
                else
                {
                    skeletonCap.requestSkeletonCalibration(args.getUser(), true);
                }
            }
        } catch (StatusException e)
        {
            e.printStackTrace();
        }
    }
}