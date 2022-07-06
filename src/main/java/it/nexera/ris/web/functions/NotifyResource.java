package it.nexera.ris.web.functions;

import org.primefaces.push.EventBus;
import org.primefaces.push.RemoteEndpoint;
import org.primefaces.push.annotation.OnClose;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.OnOpen;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint("/notify")
public class NotifyResource {

    @OnMessage(encoders = {
            JSONEncoder.class
    })
    public String onMessage(String id) {
        return id;
    }

    @OnOpen
    public void onOpen(RemoteEndpoint r, EventBus eventBus) {

    }

    @OnClose
    public void onClose(RemoteEndpoint r, EventBus eventBus) {

    }

}
