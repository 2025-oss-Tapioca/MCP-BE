package com.tapioca.MCPBE.util.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BeWsClient {

    private final ObjectMapper om = new ObjectMapper();
    private final Map<String, Session> pool = new ConcurrentHashMap<>();

    public void connectAndRegister(String internalKey, String callbackUrl,
                                   String sourceType, String teamCode) {
        pool.computeIfAbsent(internalKey, k -> {
            try {
                WebSocketContainer c = ContainerProvider.getWebSocketContainer();
                Session s = c.connectToServer(new Endpoint() {
                    @Override public void onOpen(Session session, EndpointConfig config) { /* no-op */ }
                }, ClientEndpointConfig.Builder.create().build(), URI.create(callbackUrl));

                sendJson(s, Map.of(
                        "type", "register",
                        "sourceType", sourceType,
                        "code", teamCode
                ));
                return s;
            } catch (Exception e) {
                throw new RuntimeException("BE WS connect/register failed: " + e.getMessage(), e);
            }
        });
    }

    public void sendLog(String internalKey, Map<String,Object> data) {
        Session s = pool.get(internalKey);
        if (s == null || !s.isOpen()) return;
        sendJson(s, Map.of("type","log","data",data));
    }

    private void sendJson(Session s, Object payload) {
        try {
            s.getAsyncRemote().sendText(om.writeValueAsString(payload));
        } catch (Exception e) {
            try { s.close(); } catch (Exception ignore) {}
            throw new RuntimeException("WS send failed: " + e.getMessage(), e);
        }
    }
}
