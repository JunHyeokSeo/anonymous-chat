package com.anonymouschat.testsupport.socket;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 테스트 목적으로 {@link WebSocketSession}을 시뮬레이션하는 스텁(Stub) 클래스입니다.
 * 실제 WebSocket 연결 없이 세션의 동작을 모의하고, 전송된 메시지를 캡처하며,
 * Principal 및 속성 관리를 지원합니다.
 */
public class WebSocketSessionStub implements WebSocketSession {

    private final String id;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final AtomicBoolean open = new AtomicBoolean(true);
    private final AtomicReference<CloseStatus> closeStatus = new AtomicReference<>();
    private final List<TextMessage> sentMessages = new CopyOnWriteArrayList<>();

    public WebSocketSessionStub(String id, Principal principal) {
        this.id = id;
        if (principal != null) {
            this.attributes.put("principal", principal);
        }
    }

	/**
	 * 지정된 Principal을 가진 새로운 열린 WebSocketSessionStub 인스턴스를 생성합니다.
	 *
	 * @param principal 세션에 연결할 Principal 객체
	 * @return 새로 생성된 WebSocketSessionStub 인스턴스
	 */
    public static WebSocketSessionStub withPrincipal(Principal principal) {
        return new WebSocketSessionStub("session-" + System.nanoTime(), principal);
    }

	/**
	 * 새로운 열린 WebSocketSessionStub 인스턴스를 생성합니다.
	 *
	 * @return 새로 생성된 WebSocketSessionStub 인스턴스
	 */
    public static WebSocketSessionStub open() {
        return new WebSocketSessionStub("session-" + System.nanoTime(), null);
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
    public void sendMessage(@NonNull WebSocketMessage<?> message) {
        if (message instanceof TextMessage) {
            sentMessages.add((TextMessage) message);
        }
    }

    @Override public boolean isOpen() { return open.get(); }

    @Override
    public void close(){
        close(CloseStatus.NORMAL);
    }

    @Override
    public void close(@NonNull CloseStatus status) {
        if (open.compareAndSet(true, false)) {
            this.closeStatus.set(status);
        }
    }

	/**
	 * 이 세션을 통해 전송된 텍스트 메시지의 페이로드 목록을 반환합니다.
	 * 테스트 검증에 사용됩니다.
	 *
	 * @return 전송된 텍스트 메시지 페이로드 목록
	 */
    public List<String> getSentTextPayloads() {
        return sentMessages.stream().map(TextMessage::getPayload).toList();
    }

	/**
	 * 이 세션의 현재 종료 상태를 반환합니다.
	 * 테스트 검증에 사용됩니다.
	 *
	 * @return 세션의 CloseStatus
	 */
    public CloseStatus getCloseStatus() {
        return closeStatus.get();
    }
}
