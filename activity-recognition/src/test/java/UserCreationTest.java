import http.it.unibo.deis.lia.depict.DroolsKinectModel;
import http.it.unibo.deis.lia.depict.KinectModelFactory;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.common.DefaultFactHandle;
import org.drools.common.EventFactHandle;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static junit.framework.Assert.*;


public class UserCreationTest {

    protected StatefulKnowledgeSession init( String drl ) {
        return init( new String[] { drl } );
    }

    protected StatefulKnowledgeSession init( String[] drls ) {
        KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for ( String s : drls ) {
            kBuilder.add( new ClassPathResource( s ), ResourceType.DRL );
        }
        if ( kBuilder.hasErrors() ) {
            fail( kBuilder.getErrors().toString() );
        }
        KnowledgeBase kBase = KnowledgeBaseFactory.newKnowledgeBase();
        kBase.addKnowledgePackages( kBuilder.getKnowledgePackages() );

        StatefulKnowledgeSession kSession = kBase.newStatefulKnowledgeSession();
        kSession.fireAllRules();

        return kSession;
    }


    @Test
    public void testUserCreation() {
        String[] drls = new String[] {
                "it/unibo/deis/lia/depict/type_declares.drl",
                "it/unibo/deis/lia/depict/stream_adapters.drl"
        };

        StatefulKnowledgeSession kSession = init( drls );

        DroolsKinectModel dkm = (DroolsKinectModel) KinectModelFactory.initModel( KinectModelFactory.MODEL_TYPES.DROOLS );
            assertNotNull( dkm );
        dkm.setkSession( kSession );
            assertTrue( dkm.isValid() );

        dkm.newUser( 374 );
        dkm.newUser( 818 );

        assertEquals( 32, kSession.getObjects().size() );

        dkm.removeUser( 374 );

        assertEquals( 16, kSession.getObjects().size() );

        dkm.removeUser( 444 );

        System.err.println( reportWMObjects( kSession ) );

        assertEquals( 16, kSession.getObjects().size() );

        dkm.removeUser( 818 );

        assertEquals( 0, kSession.getObjects().size() );

        System.err.println( reportWMObjects( kSession ) );
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
        ans += " ---------------- END WM -----------\n";
        return ans;
    }
}
