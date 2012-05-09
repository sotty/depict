package http.it.unibo.deis.lia.depict;

import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionStatus;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;

import java.util.HashMap;
import java.util.Map;


public class DroolsKinectModel extends MapKinectModel implements KinectModel {

    private boolean cacheData;

    private Map<Integer, FactHandle> userHandles = new HashMap<Integer, FactHandle>();

    private StatefulKnowledgeSession kSession;
    private WorkingMemoryEntryPoint userEP;

    private WorkingMemoryEntryPoint headEP;
    private WorkingMemoryEntryPoint neckEP;
    private WorkingMemoryEntryPoint torsoEP;
    private WorkingMemoryEntryPoint leftShoulderEP;
    private WorkingMemoryEntryPoint leftElbowEP;
    private WorkingMemoryEntryPoint leftHandEP;
    private WorkingMemoryEntryPoint rightShoulderEP;
    private WorkingMemoryEntryPoint rightElbowEP;
    private WorkingMemoryEntryPoint rightHandEP;
    private WorkingMemoryEntryPoint leftHipEP;
    private WorkingMemoryEntryPoint leftKneeEP;
    private WorkingMemoryEntryPoint leftFootEP;
    private WorkingMemoryEntryPoint rightHipEP;
    private WorkingMemoryEntryPoint rightKneeEP;
    private WorkingMemoryEntryPoint rightFootEP;




    @Override
    public boolean isValid() {
        return userEP != null
                &&  headEP != null
                &&  neckEP != null
                &&  torsoEP != null
                &&  leftShoulderEP != null
                &&  leftElbowEP != null
                &&  leftHandEP != null
                &&  rightShoulderEP != null
                &&  rightElbowEP != null
                &&  rightHandEP != null
                &&  leftHipEP != null
                &&  leftKneeEP != null
                &&  leftFootEP != null
                &&  rightHipEP != null
                &&  rightKneeEP != null
                &&  rightFootEP != null
                ;
    }

    public StatefulKnowledgeSession getkSession() {
        return kSession;
    }

    public void setkSession(StatefulKnowledgeSession kSession) {
        this.kSession = kSession;

        kSession.setGlobal( "factory", new ObjectFactory() );

        userEP = kSession.getWorkingMemoryEntryPoint( "userEP" );

        headEP = kSession.getWorkingMemoryEntryPoint( "userEP" );
        neckEP = kSession.getWorkingMemoryEntryPoint( "neckEP" );
        torsoEP = kSession.getWorkingMemoryEntryPoint( "torsoEP" );
        leftShoulderEP = kSession.getWorkingMemoryEntryPoint( "leftShoulderEP" );
        leftElbowEP = kSession.getWorkingMemoryEntryPoint( "leftElbowEP" );
        leftHandEP = kSession.getWorkingMemoryEntryPoint( "leftHandEP" );
        rightShoulderEP = kSession.getWorkingMemoryEntryPoint( "rightShoulderEP" );
        rightElbowEP = kSession.getWorkingMemoryEntryPoint( "rightElbowEP" );
        rightHandEP = kSession.getWorkingMemoryEntryPoint( "rightHandEP" );
        leftHipEP = kSession.getWorkingMemoryEntryPoint( "leftHipEP" );
        leftKneeEP = kSession.getWorkingMemoryEntryPoint( "leftKneeEP" );
        leftFootEP = kSession.getWorkingMemoryEntryPoint( "leftFootEP" );
        rightHipEP = kSession.getWorkingMemoryEntryPoint( "rightHipEP" );
        rightKneeEP = kSession.getWorkingMemoryEntryPoint( "rightKneeEP" );
        rightFootEP = kSession.getWorkingMemoryEntryPoint( "rightFootEP" );

    }


    public boolean isCacheData() {
        return cacheData;
    }

    public void setCacheData(boolean cacheData) {
        this.cacheData = cacheData;
    }

    public void newUser( int userId ) {
        if ( cacheData ) {
            super.newUser( userId );
        }

        if ( ! userHandles.containsKey( userId ) && isValid() ) {
            FactHandle uHandle = userEP.insert( userId );
            userHandles.put( userId, uHandle );
            kSession.fireAllRules();
        }
    }

    public void removeUser( int userId ) {
        if ( cacheData ) {
            super.removeUser( userId );
        }
        if ( userHandles.containsKey( userId ) && isValid() ) {
            userEP.retract( userHandles.get( userId ) );
            kSession.fireAllRules();
        }
    }

    public void calibrationComplete( int userId ) {
        if ( cacheData ) {
            super.calibrationComplete( userId );
        }

    }

    public Map<SkeletonJoint, SkeletonJointPosition> getSkeleton( int userId ) {
//        System.err.println( "Ask 4 skeleton " + userId );
        if ( cacheData ) {
            return super.getSkeleton( userId );
        }
        return null;
    }

    @Override
    public void updateCoM(int userId, Point3D com) {
        super.updateCoM(userId, com);

        Coords coords = new Coords( userId,
                com.getX(),
                com.getY(),
                com.getZ(),
                1.0f );
        userEP.insert( coords );

        kSession.fireAllRules();
    }

    public void updateJoint( int userId, SkeletonJoint joint, SkeletonJointPosition skeletonJointPosition, boolean batch ) {
        if ( cacheData ) {
            super.updateJoint( userId, joint, skeletonJointPosition, batch );
        }

        if ( ! isValid() ) {
            System.err.println( "Can't update joint, model not valid");
            return;
        }

        Coords coords = new Coords( userId,
                skeletonJointPosition.getPosition().getX(),
                skeletonJointPosition.getPosition().getY(),
                skeletonJointPosition.getPosition().getZ(),
                skeletonJointPosition.getConfidence() );


        switch ( joint.ordinal() ) {
            case 0 : headEP.insert( coords );
                break;
            case 1 : neckEP.insert( coords );
                break;
            case 2 : torsoEP.insert( coords );
                break;
            case 5 : leftShoulderEP.insert( coords );
                break;
            case 6 : leftElbowEP.insert( coords );
                break;
            case 8 : leftHandEP.insert( coords );
                break;
            case 11 : rightShoulderEP.insert( coords );
                break;
            case 12 : rightElbowEP.insert( coords );
                break;
            case 14 : rightHandEP.insert( coords );
                break;
            case 16 : leftHipEP.insert( coords );
                break;
            case 17 : leftKneeEP.insert( coords );
                break;
            case 19 : leftFootEP.insert( coords );
                break;
            case 20 : rightHipEP.insert( coords );
                break;
            case 21 : rightKneeEP.insert( coords );
                break;
            case 23 : rightFootEP.insert( coords );
                break;
            default : throw new IllegalStateException( "Unrecognized Joint data" + skeletonJointPosition );
        }

        if ( ! batch ) kSession.fireAllRules();
    }

    public void completeBatchRefresh() {
        kSession.fireAllRules();
    }

    public void inPose( int userId, String pose ) {
        if ( cacheData ) {
            super.inPose( userId, pose );
        }
    }

    public void outOfPose( int userId, String pose ) {
        if ( cacheData ) {
            super.outOfPose( userId, pose );
        }
    }

    public void detectingPose( int userId, String pose, PoseDetectionStatus status ) {
        if ( cacheData ) {
            super.detectingPose( userId, pose, status );
        }

    }

    public static class Coords {
        public int   id;
        public float x;
        public float y;
        public float z;
        public float a;

        public Coords(int id, float x, float y, float z, float a) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.a = a;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Coords{" +
                    "id=" + id +
                    ", x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    ", a=" + a +
                    '}';
        }
    }
}
