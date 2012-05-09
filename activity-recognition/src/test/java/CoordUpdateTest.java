import http.it.unibo.deis.lia.depict.DroolsKinectModel;
import http.it.unibo.deis.lia.depict.KinectModelFactory;
import http.it.unibo.deis.lia.depict.audio.Voice;
import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.chance.Chance;
import org.drools.common.DefaultFactHandle;
import org.drools.common.EventFactHandle;
import org.drools.conf.EventProcessingOption;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.runtime.rule.FactHandle;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;


public class CoordUpdateTest {

    protected StatefulKnowledgeSession init( String drl ) {
        return init( new String[] { drl } );
    }

    protected StatefulKnowledgeSession init( String[] drls ) {
        Chance.initialize();

        KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder( Chance.getChanceKBuilderConfiguration() );
        for ( String s : drls ) {
            kBuilder.add( new ClassPathResource( s ), ResourceType.DRL );
        }
        if ( kBuilder.hasErrors() ) {
            fail( kBuilder.getErrors().toString() );
        }

        KnowledgeBaseConfiguration kbConf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        kbConf.setOption( EventProcessingOption.STREAM );
        KnowledgeBase kBase = KnowledgeBaseFactory.newKnowledgeBase( kbConf );
        kBase.addKnowledgePackages( kBuilder.getKnowledgePackages() );

        KnowledgeSessionConfiguration ksConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksConf.setOption( ClockTypeOption.get( "realtime" ) );
        StatefulKnowledgeSession kSession = kBase.newStatefulKnowledgeSession( ksConf, null );
        kSession.fireAllRules();

        return kSession;
    }


    @Test
    public void testSafetyCheckFailure() throws InterruptedException {
        StatefulKnowledgeSession kSession = launch( 2 );
        System.err.println( reportWMObjects( kSession ) );

        assertEquals( 2, kSession.getObjects().size() );
    }

    @Test
    public void testSafetyCheckSuccess() throws InterruptedException {
        StatefulKnowledgeSession kSession = launch( 0 );
        System.err.println( reportWMObjects( kSession ) );

        assertEquals( 2, kSession.getObjects().size() );
    }

    @Test
    public void testSafetyCheckPartial() throws InterruptedException {
        StatefulKnowledgeSession kSession = launch( 1 );
        System.err.println( reportWMObjects( kSession ) );

        assertEquals( 2, kSession.getObjects().size() );
    }

    protected StatefulKnowledgeSession launch( int caseId ) throws InterruptedException {


        int uid = 415;

        int MAX_ITER = 100;
        long SLEEP = 30;
        long MAXD = 2 * MAX_ITER * SLEEP;
        long MIDD = 3 * MAX_ITER * SLEEP / 2;
        long HALFD = MAX_ITER * SLEEP ;

        float DELTA = 1.5f * 4.0f / MAX_ITER;
        float LEFT_BASE_Y = 2.0f;
        float RIGHT_BASE_Y;
        switch ( caseId ) {
            case 0 : RIGHT_BASE_Y = 2.5f; break;
            case 1 : RIGHT_BASE_Y = -0.25f; break;
            case 2 :
            default: RIGHT_BASE_Y = -20f; break;
        }




        Voice.speak( "Init" );

        String[] drls = new String[] {
                "it/unibo/deis/lia/depict/fuzzy_EC.drl",
                "it/unibo/deis/lia/depict/type_declares.drl",
                "it/unibo/deis/lia/depict/stream_adapters.drl",
                "it/unibo/deis/lia/depict/usecase.drl"
        };

        StatefulKnowledgeSession kSession = init( drls );

        DroolsKinectModel dkm = (DroolsKinectModel) KinectModelFactory.initModel( KinectModelFactory.MODEL_TYPES.DROOLS );
        assertNotNull( dkm );
        dkm.setkSession( kSession );
        assertTrue( dkm.isValid() );

        dkm.newUser( uid );
//        assertEquals( 20, kSession.getObjects().size() );

        kSession.fireAllRules();



        switch ( caseId ) {
            case 0 :
                kSession.insert( "" + MAXD ); break;
            case 1 :
                kSession.insert( "" + MIDD ); break;
            case 2 :
            default:
                kSession.insert( "" + HALFD ); break;
        }



        System.out.println( "Starting..." );
        long now = new Date().getTime();
        for ( int j = 0; j < MAX_ITER; j++ ) {
            float rx = (float) ( 0.1f * Math.random() );
            float ry = DELTA * j;
            float rz = (float) ( 0.1f * Math.random() );

            dkm.updateCoM( uid, new Point3D( 0, 0, 0 ) );

            dkm.startBatchRefresh();
            dkm.updateJoint( uid, SkeletonJoint.LEFT_HAND, new SkeletonJointPosition( new Point3D( 5.0f + rx, LEFT_BASE_Y + ry, 3.0f + rz ), 0.95f ), true );
            dkm.updateJoint( uid, SkeletonJoint.RIGHT_HAND, new SkeletonJointPosition( new Point3D( 3.0f + rx, RIGHT_BASE_Y + ry, 8.0f + rz ), 0.95f ), true );
            dkm.completeBatchRefresh();

            Thread.sleep( SLEEP );
        }
        kSession.fireAllRules();
        long dur = new Date().getTime() - now;




        System.err.println( "Duration " + dur );


        dkm.removeUser( uid );

        Thread.sleep( 2500 );

        return kSession;
    }



    public String reportWMObjects(StatefulKnowledgeSession session) {
        PriorityQueue<String> queue = new PriorityQueue<String>();
        for (FactHandle fh : session.getFactHandles()) {
            Object o;
            if (fh instanceof EventFactHandle) {
                EventFactHandle efh = (EventFactHandle) fh;
                queue.add("\t " + efh.getStartTimestamp() + "\t" + efh.getObject().toString() + "\n");
            } else {
                o = ((DefaultFactHandle) fh).getObject();
                queue.add("\t " + o.toString() + "\n");
            }

        }
        String ans = " ---------------- WM " + session.getObjects().size() + " --------------\n";
        while (! queue.isEmpty()) {
            Object o = queue.poll();
            ans += o;
        }
        ans += " ---------------- END WM " + session.getObjects().size() + " -----------\n";
        return ans;
    }
}
