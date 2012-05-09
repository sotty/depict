import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.chance.Chance;
import org.drools.io.impl.ByteArrayResource;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.time.SessionPseudoClock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class WindowTest {



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

        Map res = new HashMap();
        StatefulKnowledgeSession kSession = kBase.newStatefulKnowledgeSession();
        kSession.setGlobal( "map", res );
        kSession.fireAllRules();


        return kSession;
    }

    @Test
    public void testSampling() {

        StatefulKnowledgeSession ks = init( "window.drl" );

        for ( int j = 0; j < 33; j++ ) {
            System.out.println( "Time is now >> " + j );
            ks.insert( 1.0*j );


            ks.fireAllRules();
        }

        fail( "TODO Insert assertion checks" );

    }



}

