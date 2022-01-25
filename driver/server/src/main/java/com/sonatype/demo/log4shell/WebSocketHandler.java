package com.sonatype.demo.log4shell;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.HashSet;

@WebSocket
public class WebSocketHandler {

    private Gson gson = new Gson();


    public static WebSocketHandler handler;

    public WebSocketHandler() {
        handler = this;
    }

    private HashSet<Session> sessions = new HashSet<>();

    @OnWebSocketConnect
    public void onConnect(Session s) throws Exception {
        sessions.add(s);
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace();
    }

    @OnWebSocketClose
    public void onClose(Session s, int statusCode, String reason) {

        sessions.remove(s);
    }

    @OnWebSocketMessage
    public void onMessage(Session s, String message) {


    }


    private void publish(Object o) {


        String msg = "";
        if (o == null) return;
        if (o instanceof String) {
            msg = o.toString();
        } else {
            msg = gson.toJson(o);
        }

        final String payload = msg;

        sessions.stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendStringByFuture(payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendUpdate(String target) {


        String msg = ("{'cmd':'update','target':'" + target + "' }").replace("'", "\"");
        publish(msg);

    }
}
