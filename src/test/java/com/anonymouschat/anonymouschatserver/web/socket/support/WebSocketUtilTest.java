package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.UnauthorizedException;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.testsupport.security.PrincipalStub;
import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link WebSocketUtil}에 대한 단위 테스트 클래스입니다.
 * WebSocket 세션에서 Principal 및 사용자 ID를 추출하는 유틸리티 메소드의 기능을 검증합니다.
 */
@DisplayName("WebSocketUtil 테스트")
class WebSocketUtilTest {

    /**
     * WebSocket 세션에 올바르게 설정된 Principal 로부터
     * {@link WebSocketUtil#extractPrincipal(WebSocketSession)} 메소드가
     * {@link CustomPrincipal} 객체를 성공적으로 추출하는지 검증합니다.
     */
    @Test
    @DisplayName("세션에서 인증된 Principal을 성공적으로 추출한다")
    void should_extract_principal_from_session() {
        // given
        CustomPrincipal expectedPrincipal = (CustomPrincipal) PrincipalStub.authenticated(1L);
        WebSocketSession session = WebSocketSessionStub.withPrincipal(expectedPrincipal);

        // when
        CustomPrincipal actualPrincipal = WebSocketUtil.extractPrincipal(session);

        // then
        assertThat(actualPrincipal).isEqualTo(expectedPrincipal);
    }

    /**
     * WebSocket 세션에 올바르게 설정된 Principal 로부터
     * {@link WebSocketUtil#extractUserId(WebSocketSession)} 메소드가
     * 사용자 ID를 성공적으로 추출하는지 검증합니다.
     */
    @Test
    @DisplayName("세션에서 userId를 성공적으로 추출한다")
    void should_extract_user_id_from_session() {
        // given
        WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(1L));

        // when
        Long userId = WebSocketUtil.extractUserId(session);

        // then
        assertThat(userId).isEqualTo(1L);
    }

    /**
     * WebSocket 세션에 Principal(인증 정보)이 없는 경우,
     * {@link WebSocketUtil#extractPrincipal(WebSocketSession)} 메소드가
     * {@link IllegalStateException}을 올바르게 던지는지 검증합니다.
     */
    @Test
    @DisplayName("세션에 Principal이 없으면 IllegalStateException을 던진다")
    void should_throw_exception_if_principal_is_missing() {
        // given
	    // principal 포함되지 않은 session 생성
        WebSocketSession session = WebSocketSessionStub.open();

        // when & then
        // Principal 추출 시 IllegalStateException이 발생하고, 메시지에 "인증 정보가 없습니다"가 포함되어 있는지 검증.
        assertThatThrownBy(() -> WebSocketUtil.extractPrincipal(session))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    /**
     * WebSocket 세션의 Principal이 인증되지 않은 상태(`isAuthenticatedUser() == false`)일 때,
     * {@link WebSocketUtil#extractPrincipal(WebSocketSession)} 메소드가
     * {@link IllegalStateException}을 올바르게 던지는지 검증합니다.
     */
    @Test
    @DisplayName("Principal이 인증되지 않은 상태이면 IllegalStateException을 던진다")
    void should_throw_exception_if_principal_is_not_authenticated() {
        // given
        WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.unauthenticated());

        // when & then
        // Principal 추출 시 IllegalStateException이 발생하고, 메시지에 "인증되지 않은 사용자"가 포함되어 있는지 검증합니다.
        assertThatThrownBy(() -> WebSocketUtil.extractPrincipal(session))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }
}
