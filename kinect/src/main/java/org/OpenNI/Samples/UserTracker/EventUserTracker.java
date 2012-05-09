/****************************************************************************
 *                                                                           *
 *  OpenNI 1.x Alpha                                                         *
 *  Copyright (C) 2011 PrimeSense Ltd.                                       *
 *                                                                           *
 *  This file is part of OpenNI.                                             *
 *                                                                           *
 *  OpenNI is free software: you can redistribute it and/or modify           *
 *  it under the terms of the GNU Lesser General Public License as published *
 *  by the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  OpenNI is distributed in the hope that it will be useful,                *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
 *  GNU Lesser General Public License for more details.                      *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public License *
 *  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
 *                                                                           *
 ****************************************************************************/
package org.OpenNI.Samples.UserTracker;

import http.it.unibo.deis.lia.depict.KinectModel;
import http.it.unibo.deis.lia.depict.KinectModelFactory;
import org.OpenNI.*;
import org.OpenNI.Samples.UserTracker.observers.*;

import java.util.Date;

public class EventUserTracker {



    private Context context;

    private UserGenerator userGen;
    private DepthGenerator depthGen;
    private SkeletonCapability skeletonCap;
    private PoseDetectionCapability poseDetectionCap;

    private KinectModel model;

    private TrackingGUI frame;


    //TODO: pick this up dynamically
    private final String MODULES_XML = "/home/davide/Projects/Git/DEPICT/kinect/src/main/resources/modules.xml";




    public EventUserTracker( KinectModelFactory.MODEL_TYPES modelType, TrackingGUI frame, boolean render ) {

        this.model = KinectModelFactory.initModel(modelType);
            model.setCacheData( render );

        this.frame = frame;

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

            System.err.println(java.util.Arrays.asList(poseDetectionCap.getAllAvailablePoses()));

            if ( render ) {
                TrackPanel panel = new TrackPanel( userGen, depthGen, skeletonCap, model );
                frame.initialize( panel );
            } else {
                frame.initialize( null );
            }


        } catch (GeneralException e) {
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


    public void resample() {
        try {
            context.waitAnyUpdateAll();
            
            refreshUsers();
            refreshJoints();

            if ( frame != null ) {
                frame.repaint();
            }

        } catch (StatusException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void refreshUsers() throws StatusException {
        int[] users = userGen.getUsers();
        for ( int i = 0; i < users.length; ++i ) {
            refreshUserCoM(users[i]);
        }
        
    }

    private void refreshUserCoM( int user ) throws StatusException {
        model.updateCoM( user, userGen.getUserCoM( user ) );
    }

    private void refreshJoints() throws StatusException {
        int[] users = userGen.getUsers();
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


    public void refreshJoint( int user, SkeletonJoint joint, boolean batch ) throws StatusException {
        SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user, joint);
        if ( pos.getPosition().getZ() != 0 ) {
            model.updateJoint( user, joint, new SkeletonJointPosition( depthGen.convertRealWorldToProjective( pos.getPosition() ), pos.getConfidence() ), batch );
        } else {
            model.updateJoint( user, joint, new SkeletonJointPosition( new Point3D(), 0 ), batch );
        }
    }





}
