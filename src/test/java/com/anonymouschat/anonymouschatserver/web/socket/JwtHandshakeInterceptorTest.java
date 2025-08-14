package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtAuthenticationFactory;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenResolver;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
import com.anonymouschat.testsupport.security.PrincipalStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * {@link JwtHandshakeInterceptor}에 대한 단위 테스트 클래스입니다.
 * WebSocket 핸드셰이크 과정에서 JWT 토큰 검증 및 Principal 주입 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtHandshakeInterceptor 테스트")
class JwtHandshakeInterceptorTest {

    @Mock private JwtTokenResolver tokenResolver;
    @Mock private JwtValidator jwtValidator;
    @Mock private JwtAuthenticationFactory authFactory;
    @InjectMocks private JwtHandshakeInterceptor interceptor;

    @Mock private ServerHttpRequest request;
    @Mock private ServerHttpResponse response;
    @Mock private WebSocketHandler wsHandler;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
    }

    /**
     * 유효한 JWT 토큰을 사용하여 핸드셰이크를 시도할 때,
     * Principal이 WebSocket 세션 attributes에 저장되고 핸드셰이크가 성공하는지 검증합니다.
     */
    @Test
    @DisplayName("유효한 토큰으로 핸드셰이크 시도 시, Principal을 attributes에 저장하고 true를 반환한다")
    void should_store_principal_and_return_true_for_valid_token() {
        // given
        String validToken = "valid-token";
	    CustomPrincipal principal = (CustomPrincipal) PrincipalStub.authenticated(1L);

        when(tokenResolver.resolve(request)).thenReturn(validToken);
        doNothing().when(jwtValidator).validate(validToken);
        when(authFactory.createPrincipal(validToken)).thenReturn(principal);

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        // 핸드셰이크가 성공했는지 (true 반환) 확인.
        assertThat(result).isTrue();
        // Principal이 세션 attributes에 올바르게 저장되었는지 확인.
        assertThat(attributes.get("principal")).isEqualTo(principal);
        // 응답 상태 코드가 설정되지 않았는지 (성공적인 핸드셰이크이므로) 확인.
        verify(response, never()).setStatusCode(any());
    }

    /**
     * 토큰 해석(resolve) 과정에서 실패(예: 토큰 없음, 형식 오류)가 발생할 때,
     * 핸드셰이크가 거부되고 401 Unauthorized 상태 코드가 설정되는지 검증합니다.
     */
    @Test
    @DisplayName("토큰 해석(resolve) 실패 시, 401 상태 코드를 설정하고 false를 반환한다")
    void should_set_unauthorized_and_return_false_for_invalid_token() {
        // given
        when(tokenResolver.resolve(request)).thenThrow(new IllegalArgumentException("No token"));

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        // 핸드셰이크가 실패했는지 (false 반환) 확인.
        assertThat(result).isFalse();
        // Principal이 세션 attributes에 저장되지 않았는지 확인.
        assertThat(attributes).doesNotContainKey("principal");
        // 응답 상태 코드가 401 Unauthorized로 설정되었는지 확인.
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    /**
     * JWT 토큰은 유효하지만, 해당 토큰으로 생성된 Principal이 인증된 사용자(`isAuthenticatedUser() == true`)가 아닐 때,
     * 핸드셰이크가 거부되고 401 Unauthorized 상태 코드가 설정되는지 검증합니다.
     */
    @Test
    @DisplayName("Principal이 인증된 사용자가 아니면, 401 상태 코드를 설정하고 false를 반환한다")
    void should_return_false_if_principal_is_not_an_authenticated_user() {
        // given
        String validToken = "valid-token";
	    CustomPrincipal unauthenticatedPrincipal = (CustomPrincipal) PrincipalStub.unauthenticated();

        when(tokenResolver.resolve(request)).thenReturn(validToken);
        when(authFactory.createPrincipal(validToken)).thenReturn(unauthenticatedPrincipal);

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isFalse();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    /**
     * JWT 토큰 유효성 검증(`jwtValidator.validate`) 과정에서 실패(예: 토큰 만료, 위변조)가 발생할 때,
     * 핸드셰이크가 거부되고 401 Unauthorized 상태 코드가 설정되는지 검증합니다.
     */
    @Test
    @DisplayName("토큰 유효성 검증 실패 시, 401 상태 코드를 설정하고 false를 반환한다")
    void should_set_unauthorized_and_return_false_for_invalid_token_validation() {
        // given
        String invalidToken = "invalid-token";
        when(tokenResolver.resolve(request)).thenReturn(invalidToken);
        doThrow(new RuntimeException("Invalid token")).when(jwtValidator).validate(invalidToken); // 유효성 검증 실패 시뮬레이션

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isFalse();
        assertThat(attributes).doesNotContainKey("principal");
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }
}
