package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtAuthenticationFactory;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenResolver;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
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

    @Test
    @DisplayName("유효한 토큰으로 핸드셰이크 시도 시, Principal을 attributes에 저장하고 true를 반환한다")
    void should_store_principal_and_return_true_for_valid_token() throws Exception {
        // given
        String validToken = "valid-token";
        CustomPrincipal principal = CustomPrincipal.builder().userId(1L).role("USER").build();

        when(tokenResolver.resolve(request)).thenReturn(validToken);
        doNothing().when(jwtValidator).validate(validToken);
        when(authFactory.createPrincipal(validToken)).thenReturn(principal);

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isTrue();
        assertThat(attributes.get("principal")).isEqualTo(principal);
        verify(response, never()).setStatusCode(any());
    }

    @Test
    @DisplayName("토큰 해석(resolve) 실패 시, 401 상태 코드를 설정하고 false를 반환한다")
    void should_set_unauthorized_and_return_false_for_invalid_token() throws Exception {
        // given
        when(tokenResolver.resolve(request)).thenThrow(new IllegalArgumentException("No token"));

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isFalse();
        assertThat(attributes).doesNotContainKey("principal");
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Principal이 인증된 사용자가 아니면, 401 상태 코드를 설정하고 false를 반환한다")
    void should_return_false_if_principal_is_not_an_authenticated_user() throws Exception {
        // given
        String validToken = "valid-token";
        CustomPrincipal unauthenticatedPrincipal = CustomPrincipal.builder().provider(OAuthProvider.GOOGLE).providerId("123").role("GUEST").build();

        when(tokenResolver.resolve(request)).thenReturn(validToken);
        when(authFactory.createPrincipal(validToken)).thenReturn(unauthenticatedPrincipal);

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isFalse();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }
}
