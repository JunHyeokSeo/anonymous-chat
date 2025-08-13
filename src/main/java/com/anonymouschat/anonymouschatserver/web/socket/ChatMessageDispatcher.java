package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.handler.MessageHandler;
import com.anonymouschat.anonymouschatserver.web.socket.support.WsLogTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 수신된 WebSocket 메시지(`ChatInboundMessage`)를 해당 메시지 타입에 맞는
 * {@link MessageHandler}로 라우팅하여 처리하는 디스패처 클래스입니다.
 * 메시지의 유효성을 검사하고, 지원하지 않는 메시지 타입에 대한 예외 처리를 담당합니다.
 */
@Slf4j
@Component
public class ChatMessageDispatcher {
	private final Map<MessageType, MessageHandler> handlerMap;

	/**
	 * 등록된 모든 {@link MessageHandler} 구현체들을 메시지 타입별로 매핑하여 초기화합니다.
	 *
	 * @param handlers Spring 컨텍스트에 등록된 모든 MessageHandler 구현체 리스트
	 */	public ChatMessageDispatcher(List<MessageHandler> handlers) {
		this.handlerMap = handlers.stream()
				                  .collect(Collectors.toUnmodifiableMap(MessageHandler::type, h -> h));
	}

	/**
	 * 수신된 인바운드 메시지를 해당 메시지 타입에 맞는 핸들러로 디스패치합니다.
	 * 메시지의 유효성을 검사하고, 지원하지 않는 타입일 경우 {@link UnsupportedOperationException}을 발생시킵니다.
	 *
	 * @param session 메시지를 보낸 WebSocket 세션
	 * @param inbound 처리할 인바운드 메시지 객체
	 * @throws IllegalArgumentException 메시지 페이로드가 유효하지 않을 경우
	 * @throws UnsupportedOperationException 지원하지 않는 메시지 타입일 경우
	 */	public void dispatch(WebSocketSession session, ChatInboundMessage inbound) {
		validateInbound(inbound);
		MessageHandler handler = handlerMap.get(inbound.type());
		if (handler == null) {
			throw new UnsupportedOperationException("Unsupported message type: " + inbound.type());
		}
		log.debug("{}type={} roomId={} sessionId={}", WsLogTag.sys(), inbound.type(), inbound.roomId(), session.getId());
		handler.handle(session, inbound);
	}

	/**
	 * 인바운드 메시지의 필수 필드 및 내용 유효성을 검사합니다.
	 * 이 메소드에서 발생하는 예외는 {@link ChatInboundMessage} 레코드의 생성자에서 이미 처리될 수 있습니다.
	 *
	 * @param inbound 유효성을 검사할 인바운드 메시지 객체
	 * @throws IllegalArgumentException 필수 필드가 누락되었거나 내용이 유효하지 않을 경우
	 */	private void validateInbound(ChatInboundMessage inbound) {
		if (inbound == null) {
			throw new IllegalArgumentException("Inbound payload is required.");
		}
		if (inbound.type() == null) {
			throw new IllegalArgumentException("Inbound.type is required.");
		}
		if (inbound.roomId() == null) {
			throw new IllegalArgumentException("Inbound.roomId is required.");
		}
		if (inbound.type() == MessageType.CHAT && (inbound.content() == null || inbound.content().isBlank())) {
		     throw new IllegalArgumentException("Inbound.content is required for CHAT.");
		}
	}
}
