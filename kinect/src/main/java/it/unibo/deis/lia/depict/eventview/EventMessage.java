package it.unibo.deis.lia.depict.eventview;


public class EventMessage {

    private String label;

    private long start;

    public static EventMessage newMessage( String label, long start ) {
        if (label == null || label.isEmpty())
            throw new IllegalArgumentException(
                    "Illegal 'label' argument in EventMessage.newMessage(String, long): "
                            + label);
        return new EventMessage( label, start );
    }

    private EventMessage( String label, long start ) {
        if (label == null || label.isEmpty())
            throw new IllegalArgumentException(
                    "Illegal 'label' argument in EventMessage(String, long): "
                            + label);
        this.label = label;
        this.start = start;
    }


    public String getLabel() {
        return label;
    }


    public long getStart() {
        return start;
    }

}