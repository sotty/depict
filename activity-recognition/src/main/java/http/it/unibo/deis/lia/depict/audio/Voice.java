package http.it.unibo.deis.lia.depict.audio;

import com.sun.speech.freetts.VoiceManager;

public class Voice {

    static {
        System.setProperty( "freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory"
        );
    }

//    private static void initVoice() {
//        VoiceManager voiceManager = VoiceManager.getInstance();
//        voice = voiceManager.getVoice( "kevin16" );
//        voice.allocate();
//    }


    public static void speak( final String msg ) {
        Thread t = new Thread( new Runnable() {
            public void run() {
                VoiceManager voiceManager = VoiceManager.getInstance();
                com.sun.speech.freetts.Voice voice = voiceManager.getVoice( "kevin16" );
                voice.allocate();
                voice.speak( msg );
                voice.deallocate();
            }
        } );

        t.run();
    }
//
//    public static void speak( final String msg ) {
//                VoiceManager voiceManager = VoiceManager.getInstance();
//                com.sun.speech.freetts.Voice voice = voiceManager.getVoice( "kevin16" );
//                voice.allocate();
//                voice.speak( msg );
//                voice.deallocate();
//    }

}
