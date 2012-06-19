package http.it.unibo.deis.lia.depict.eventview;

import http.it.unibo.deis.lia.depict.eventview.EventMessage;
import http.it.unibo.deis.lia.depict.eventview.FluentMessage;

public interface EventViewListener {

    public void add( EventMessage message );

    public void add( FluentMessage message );
}
