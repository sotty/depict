package http.it.unibo.deis.lia.depict;

import org.OpenNI.*;
import http.it.unibo.deis.lia.depict.observers.*;

public class EventUserTracker extends AbstractUserTracker {



    protected Context context;

    protected UserGenerator userGen;
    protected DepthGenerator depthGen;
    protected SkeletonCapability skeletonCap;
    protected PoseDetectionCapability poseDetectionCap;



    //TODO: pick this up dynamically
    private final String MODULES_XML = "/home/davide/Projects/Git/DEPICT/depict/kinect/src/main/resources/modules.xml";




    public EventUserTracker( KinectModelFactory.MODEL_TYPES modelType, boolean render ) {


        try {

            context = Context.createFromXmlFile( MODULES_XML, new OutArg<ScriptNode>() );

            userGen = UserGenerator.create(context);
            depthGen = DepthGenerator.create(context);
            skeletonCap = userGen.getSkeletonCapability();
            poseDetectionCap = userGen.getPoseDetectionCapability();

            userGen.getNewUserEvent().addObserver( new NewUserObserver( model, skeletonCap, poseDetectionCap ) );
            userGen.getLostUserEvent().addObserver( new LostUserObserver( model ) );
            skeletonCap.getCalibrationCompleteEvent().addObserver( new CalibrationCompleteObserver( model, skeletonCap, poseDetectionCap ) );
            poseDetectionCap.getPoseDetectedEvent().addObserver( new PoseDetectedObserver( model, skeletonCap, poseDetectionCap ) );
            poseDetectionCap.getOutOfPoseEvent().addObserver( new OutOfPoseObserver( model, skeletonCap, poseDetectionCap ) );
//            poseDetectionCap.getPoseDetectionInProgressEvent().addObserver( new PoseDetectionObserver( model, skeletonCap, poseDetectionCap ) );

            skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);

            context.startGeneratingAll();

            initModel( modelType, render );
            initGUI(render);

        } catch ( GeneralException e ) {
            e.printStackTrace();
            System.exit(1);
        }
    }




    public void dispose() {

        if ( frame != null ) {
            frame.dispose();
        }

        userGen.dispose();
        depthGen.dispose();
        skeletonCap.dispose();
        poseDetectionCap.dispose();

        try {
            context.stopGeneratingAll();
        } catch (StatusException e) {
            e.printStackTrace();
        }
        context.dispose();

    }



    public void waitForNextSample() throws StatusException {
        context.waitAndUpdateAll();
    }

    
    public DepthMetaData getDepthGenMetaData() {
        return depthGen.getMetaData();
    }

    
    public Point3D convertRealWorldToProjective( Point3D worldPt ) throws StatusException {
        return depthGen.convertRealWorldToProjective( worldPt );
    }

    
    public boolean isSkeletonTracking( int user ) {
        return skeletonCap.isSkeletonTracking( user );
    }

    
    public boolean isSkeletonCalibrating( int user ) {
        return skeletonCap.isSkeletonCalibrating( user );
    }

    
    public String getSkeletonCalibrationPose() throws StatusException {
        return skeletonCap.getSkeletonCalibrationPose();
    }

    public java.util.Map<SkeletonJoint, SkeletonJointPosition> getSkeleton( int user ) {
        return model.getSkeleton( user );
    }

    public SceneMetaData getUserPixels( int user ) {
        return userGen.getUserPixels( user );
    }


    public void refreshJoint( int user, SkeletonJoint joint, boolean batch ) throws StatusException {
        SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user, joint);
        if ( pos.getPosition().getZ() != 0 ) {
            model.updateJoint( user, joint, new SkeletonJointPosition( depthGen.convertRealWorldToProjective( pos.getPosition() ), pos.getConfidence() ), batch );
        } else {
            model.updateJoint( user, joint, new SkeletonJointPosition( new Point3D(), 0 ), batch );
        }
    }

    public Point3D getUserCoM( int uid ) throws StatusException {
        return userGen.getUserCoM( uid );
    }

    public int[] getUsers() throws StatusException {
        return userGen.getUsers();
    }



}
