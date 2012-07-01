package it.unibo.deis.lia.depict;


import org.OpenNI.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

public abstract class AbstractUserTracker {

    protected KinectModel model;
    protected TrackingGUI frame;

    protected boolean shouldRun = true;


    protected void initModel( KinectModelFactory.MODEL_TYPES modelType, boolean caching ) {
        this.model = KinectModelFactory.initModel( modelType );
        model.setCacheData( caching );

        if ( caching && modelType == KinectModelFactory.MODEL_TYPES.DROOLS ) {
            ( (DroolsKinectModel) model).getkSession().setGlobal("view", frame.getEventPanel() );
        }
    }


    protected void initGUI( boolean render ) throws GeneralException {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsDevice gd = gs[ gs.length - 1 ];


        frame = new TrackingGUI( "Kinect Detect", gd.getDefaultConfiguration() );


        frame.addWindowStateListener( new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                shouldRun = false;
            }
        });

        frame.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent arg0) {}
            public void keyReleased(KeyEvent arg0) {}
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    shouldRun = false;
                }
            }
        });

        if ( render ) {
            TrackPanel panel = new TrackPanel( this );
            JRECPanel eventPanel = new JRECPanel( frame.getLayout(), true );
            frame.initialize( panel, eventPanel );

        } else {
            frame.initialize( null , null );
        }

    }

    public void run() {
        while( shouldRun ) {
            resample();
        }
        dispose();

    }


    public void resample() {
        try {
            waitForNextSample();

            refreshUsers();
            refreshJoints();

            if ( frame != null ) {
                frame.repaint();
            }

        } catch (StatusException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    protected void refreshUsers() throws StatusException {
        int[] users = getUsers();
        for ( int i = 0; i < users.length; ++i ) {
            refreshUserCoM(users[i]);
        }

    }

    protected void refreshUserCoM( int user ) throws StatusException {
        model.updateCoM(user, getUserCoM(user));
    }

    protected void refreshJoints() throws StatusException {
        int[] users = getUsers();
        for ( int i = 0; i < users.length; ++i ) {
            refreshJoints( users[ i ] );
        }

    }


    public void refreshJoints( int user ) throws StatusException {
        model.startBatchRefresh();

        refreshJoint( user, SkeletonJoint.HEAD, true );
        refreshJoint( user, SkeletonJoint.NECK, true );

        refreshJoint( user, SkeletonJoint.LEFT_SHOULDER, true );
        refreshJoint( user, SkeletonJoint.LEFT_ELBOW, true );
        refreshJoint( user, SkeletonJoint.LEFT_HAND, true );

        refreshJoint( user, SkeletonJoint.RIGHT_SHOULDER, true );
        refreshJoint( user, SkeletonJoint.RIGHT_ELBOW, true );
        refreshJoint( user, SkeletonJoint.RIGHT_HAND, true );

        refreshJoint( user, SkeletonJoint.TORSO, true );

        refreshJoint( user, SkeletonJoint.LEFT_HIP, true );
        refreshJoint( user, SkeletonJoint.LEFT_KNEE, true );
        refreshJoint( user, SkeletonJoint.LEFT_FOOT, true );

        refreshJoint( user, SkeletonJoint.RIGHT_HIP, true );
        refreshJoint( user, SkeletonJoint.RIGHT_KNEE, true );
        refreshJoint( user, SkeletonJoint.RIGHT_FOOT, true );

        model.completeBatchRefresh();
    }


    public abstract void refreshJoint( int user, SkeletonJoint joint, boolean batch ) throws StatusException;

    public abstract Point3D getUserCoM( int uid ) throws StatusException;

    public abstract int[] getUsers() throws StatusException;

    public abstract void dispose();

    public abstract void waitForNextSample() throws StatusException;

    public abstract DepthMetaData getDepthGenMetaData();

    public abstract Point3D convertRealWorldToProjective( Point3D worldPt ) throws StatusException;

    public abstract boolean isSkeletonTracking( int user );

    public abstract boolean isSkeletonCalibrating( int user );

    public abstract String getSkeletonCalibrationPose() throws StatusException;

    public abstract Map<SkeletonJoint,SkeletonJointPosition> getSkeleton( int user );

    public abstract SceneMetaData getUserPixels( int user );
}
