package it.unibo.deis.lia.depict;

import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionStatus;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

import java.util.*;

public class MapKinectModel implements KinectModel {

    protected boolean cacheData;

    private Set<Integer> knownUsers = new HashSet<Integer>( 3 );
    private Map<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints = Collections.synchronizedMap(
            new HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>>( 3 )
    );

    public boolean isCacheData() {
        return cacheData;
    }

    public void setCacheData(boolean cacheData) {
        this.cacheData = cacheData;
    }

    public boolean isValid() {
        return true;
    }

    public void newUser( int id  ) {
        knownUsers.add(id);
    }

    public void removeUser( int id ) {
        knownUsers.remove( id );
        joints.remove( id );
    }

    public void calibrationComplete( int user ) {
        joints.put( user, new HashMap<SkeletonJoint, SkeletonJointPosition>() );
    }

    public Map<SkeletonJoint, SkeletonJointPosition> getSkeleton( int id ) {
        return joints.get( id );
    }

    public void updateJoint( int user, SkeletonJoint joint, SkeletonJointPosition skeletonJointPosition, boolean batch ) {
        if ( getSkeleton( user ) != null ) {
            getSkeleton( user ).put( joint, skeletonJointPosition );
        }
    }

    public void updateCoM(int userId, Point3D com) {

    }

    public void inPose(int userId, String pose) {

    }

    public void outOfPose(int userId, String pose) {

    }

    public void detectingPose(int user, String pose, PoseDetectionStatus status) {

    }

    public void startBatchRefresh() {

    }

    public void completeBatchRefresh() {

    }
}
