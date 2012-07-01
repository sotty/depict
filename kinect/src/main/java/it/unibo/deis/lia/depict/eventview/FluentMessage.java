package it.unibo.deis.lia.depict.eventview;


public  class FluentMessage {

    private String label;

    private long start;

    private Interval value;

    public static FluentMessage newMessage( String label, long start, Interval value ) {
        return new FluentMessage( label, start, value );
    }

    private FluentMessage( String label, long start, Interval value ) {
        this.label = label;
        this.start = start;
        this.value = value;
    }


    public String getLabel() {
        return label;
    }


    public long getStart() {
        return start;
    }


    public Interval getValue() {
        return value;
    }

}