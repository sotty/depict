package it.unibo.deis.lia.depict;


import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.chance.Chance;
import org.drools.conf.EventProcessingOption;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;

import static junit.framework.Assert.fail;

public class KinectModelFactory {


    public static enum MODEL_TYPES {
        BASIC, DROOLS
    }

    public static KinectModel initModel( MODEL_TYPES modelType ) {
        switch ( modelType ) {

            case DROOLS :   return buildDroolsKinectModelFactory();
            case BASIC  :
            default     :   return new MapKinectModel();
        }
    }

    private KinectModelFactory() { }


    private static KinectModel buildDroolsKinectModelFactory() {
        DroolsKinectModel model = new DroolsKinectModel();
            model.setkSession( prepareKSession() );
        return model;
    }

    private static StatefulKnowledgeSession prepareKSession() {
        Chance.initialize();

        KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(Chance.getChanceKBuilderConfiguration());
        kBuilder.add( new ClassPathResource( "it/unibo/deis/lia/depict/activity_kbase.xml" ), ResourceType.CHANGE_SET );
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


}
