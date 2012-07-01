package it.unibo.deis.lia.depict;

import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionStatus;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

import java.util.Map;

public interface KinectModel {

    public boolean isCacheData();

    public void setCacheData(boolean cacheData);


    public boolean isValid();


    public void newUser( int userId );

    public void removeUser( int userId );

    public void calibrationComplete( int userId );

    public Map<SkeletonJoint,SkeletonJointPosition> getSkeleton( int userId );


    public void inPose( int userId, String pose );

    public void outOfPose( int userId, String pose );

    public void detectingPose( int user, String pose, PoseDetectionStatus status );


    public void startBatchRefresh();

    public void completeBatchRefresh();

    public void updateJoint( int userId, SkeletonJoint joint, SkeletonJointPosition skeletonJointPosition, boolean batch );

    public void updateCoM( int userId, Point3D com );

}
