package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WebSocketRateLimitGuard}에 대한 단위 테스트 클래스입니다.
 */
@DisplayName("WebSocketRateLimitGuard 테스트")
class WebSocketRateLimitGuardTest {

    private WebSocketRateLimitGuard rateLimitGuard;

    @BeforeEach
    void setUp() {
        rateLimitGuard = new WebSocketRateLimitGuard();
    }

    /**
     * 설정된 용량(capacity) 내에서는 메시지 전송 요청이 모두 허용되는지 검증합니다.
     */
    @Test
    @DisplayName("지정된 용량 내에서는 요청을 허용한다")
    void should_allow_requests_within_capacity() {
        // given
        long userId = 1L;
        MessageType type = MessageType.CHAT;

        // when & then
        for (int i = 0; i < 20; i++) {
            assertThat(rateLimitGuard.allow(userId, type)).as("요청 #" + (i + 1)).isTrue();
        }
    }

    /**
     * 설정된 용량을 초과하는 메시지 전송 요청이 거부되는지 검증합니다.
     */
    @Test
    @DisplayName("용량을 초과하면 요청을 거부한다")
    void should_deny_requests_exceeding_capacity() {
        // given
        long userId = 1L;
        MessageType type = MessageType.CHAT;

        // when
        // CHAT 타입의 용량(20)만큼 요청을 허용합니다.
        for (int i = 0; i < 20; i++) {
            rateLimitGuard.allow(userId, type);
        }

        // then
        // 21번째 요청은 거부되어야 합니다.
        assertThat(rateLimitGuard.allow(userId, type)).as("용량 초과 요청").isFalse();
    }

    /**
     * 토큰 버킷의 토큰이 시간이 지남에 따라 올바르게 리필되어,
     * 이전에 거부되었던 요청이 다시 허용되는지 검증합니다.
     */
    @Test
    @DisplayName("시간이 지나면 토큰이 리필되어 다시 요청을 허용한다")
    void should_allow_request_again_after_token_refill() throws InterruptedException {
        // given
        long userId = 1L;
        MessageType type = MessageType.ENTER;

        // when
        // ENTER/LEAVE 타입의 용량(5)만큼 요청을 허용하여 토큰을 모두 소진합니다.
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimitGuard.allow(userId, type)).as("초기 버스트").isTrue();
        }
        assertThat(rateLimitGuard.allow(userId, type)).as("용량 초과").isFalse();

        // then
        TimeUnit.SECONDS.sleep(1);
        assertThat(rateLimitGuard.allow(userId, type)).as("1초 후 리필된 토큰으로 요청 1").isTrue();
        assertThat(rateLimitGuard.allow(userId, type)).as("1초 후 리필된 토큰으로 요청 2").isTrue();
        assertThat(rateLimitGuard.allow(userId, type)).as("1초 후 리필된 토큰 소진").isFalse();
    }

    /**
     * `clear` 메소드 호출 시 특정 사용자의 모든 토큰 버킷 정보가 올바르게 삭제되는지 검증합니다.
     * 삭제 후에는 해당 사용자에 대해 새로운 버킷이 생성되어 다시 용량만큼 요청이 허용되어야 합니다.
     */
    @Test
    @DisplayName("clear 시 사용자의 버킷 정보를 삭제한다")
    void should_clear_bucket_for_user() {
        // given
        long userId = 1L;
        MessageType type = MessageType.CHAT;
        rateLimitGuard.allow(userId, type);

        // when
        rateLimitGuard.clear(userId);

        // then
        // clear 후에는 새 버킷이 생성되므로 다시 capacity 만큼 요청이 허용되어야 합니다.
        assertThat(rateLimitGuard.allow(userId, type)).as("클리어 후 첫 요청").isTrue();
    }
}
