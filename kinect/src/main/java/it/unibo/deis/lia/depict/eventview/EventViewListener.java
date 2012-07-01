package it.unibo.deis.lia.depict.eventview;

public interface EventViewListener {

    public void add( EventMessage message );

    public void add( FluentMessage message );
}
