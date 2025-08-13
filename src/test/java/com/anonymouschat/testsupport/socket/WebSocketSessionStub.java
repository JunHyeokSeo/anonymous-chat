package com.anonymouschat.testsupport.socket;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WebSocketSessionStub implements WebSocketSession {

    private final String id;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final AtomicBoolean open = new AtomicBoolean(true);
    private final AtomicReference<CloseStatus> closeStatus = new AtomicReference<>();
    private final List<TextMessage> sentMessages = new CopyOnWriteArrayList<>();
    private boolean failOnSend = false;

    public WebSocketSessionStub(String id, Principal principal) {
        this.id = id;
        if (principal != null) {
            this.attributes.put("principal", principal);
        }
    }

    public static WebSocketSessionStub withPrincipal(Principal principal) {
        return new WebSocketSessionStub("session-" + System.nanoTime(), principal);
    }

    public static WebSocketSessionStub open() {
        return new WebSocketSessionStub("session-" + System.nanoTime(), null);
    }

    public static WebSocketSessionStub willFailOnSend() {
        WebSocketSessionStub stub = new WebSocketSessionStub("failing-session-" + System.nanoTime(), null);
        stub.failOnSend = true;
        return stub;
    }

    @NonNull @Override public String getId() { return id; }
    @Override public URI getUri() { return null; }
    @NonNull @Override public HttpHeaders getHandshakeHeaders() { return new HttpHeaders(); }
    @NonNull @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public Principal getPrincipal() { return (Principal) attributes.get("principal"); }
    @Override public InetSocketAddress getLocalAddress() { return null; }
    @Override public InetSocketAddress getRemoteAddress() { return null; }
    @Override public String getAcceptedProtocol() { return null; }
    @Override public void setTextMessageSizeLimit(int messageSizeLimit) { }
    @Override public int getTextMessageSizeLimit() { return 0; }
    @Override public void setBinaryMessageSizeLimit(int messageSizeLimit) { }
    @Override public int getBinaryMessageSizeLimit() { return 0; }
    @NonNull @Override public List<WebSocketExtension> getExtensions() { return List.of(); }

    @Override
    public void sendMessage(@NonNull WebSocketMessage<?> message) throws IOException {
        if (failOnSend) {
            throw new IOException("Simulated send failure");
        }
        if (message instanceof TextMessage) {
            sentMessages.add((TextMessage) message);
        }
    }

    @Override public boolean isOpen() { return open.get(); }

    @Override
    public void close() throws IOException {
        close(CloseStatus.NORMAL);
    }

    @Override
    public void close(@NonNull CloseStatus status) {
        if (open.compareAndSet(true, false)) {
            this.closeStatus.set(status);
        }
    }

    public List<TextMessage> getSentMessages() {
        return sentMessages;
    }

    public List<String> getSentTextPayloads() {
        return sentMessages.stream().map(TextMessage::getPayload).toList();
    }

    public CloseStatus getCloseStatus() {
        return closeStatus.get();
    }
}
