package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WebSocketUtil 테스트")
class WebSocketUtilTest {

    @Test
    @DisplayName("세션에서 인증된 Principal을 성공적으로 추출한다")
    void should_extract_principal_from_session() {
        // given
        CustomPrincipal expectedPrincipal = CustomPrincipal.builder().userId(1L).role("USER").build();
        WebSocketSession session = WebSocketSessionStub.withPrincipal(expectedPrincipal);

        // when
        CustomPrincipal actualPrincipal = WebSocketUtil.extractPrincipal(session);

        // then
        assertThat(actualPrincipal).isEqualTo(expectedPrincipal);
    }

    @Test
    @DisplayName("세션에서 userId를 성공적으로 추출한다")
    void should_extract_user_id_from_session() {
        // given
        CustomPrincipal principal = CustomPrincipal.builder().userId(1L).role("USER").build();
        WebSocketSession session = WebSocketSessionStub.withPrincipal(principal);

        // when
        Long userId = WebSocketUtil.extractUserId(session);

        // then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("세션에 Principal이 없으면 IllegalStateException을 던진다")
    void should_throw_exception_if_principal_is_missing() {
        // given
        WebSocketSession session = WebSocketSessionStub.open();

        // when & then
        assertThatThrownBy(() -> WebSocketUtil.extractPrincipal(session))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("인증 정보가 없습니다");
    }

    @Test
    @DisplayName("Principal이 인증되지 않은 상태이면 IllegalStateException을 던진다")
    void should_throw_exception_if_principal_is_not_authenticated() {
        // given
        CustomPrincipal unauthenticatedPrincipal = CustomPrincipal.builder().provider(OAuthProvider.GOOGLE).providerId("123").role("GUEST").build();
        WebSocketSession session = WebSocketSessionStub.withPrincipal(unauthenticatedPrincipal);

        // when & then
        assertThatThrownBy(() -> WebSocketUtil.extractPrincipal(session))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("인증되지 않은 사용자");
    }
}