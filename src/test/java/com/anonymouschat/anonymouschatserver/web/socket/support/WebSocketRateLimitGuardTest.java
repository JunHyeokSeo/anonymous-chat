package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebSocketRateLimitGuard 테스트")
class WebSocketRateLimitGuardTest {

    private WebSocketRateLimitGuard rateLimitGuard;

    @BeforeEach
    void setUp() {
        rateLimitGuard = new WebSocketRateLimitGuard();
    }

    @Test
    @DisplayName("지정된 용량 내에서는 요청을 허용한다")
    void should_allow_requests_within_capacity() {
        // given
        long userId = 1L;
        MessageType type = MessageType.CHAT;

        // when & then
        // CHAT 타입의 기본 capacity는 20이므로 20번까지는 즉시 허용되어야 한다.
        for (int i = 0; i < 20; i++) {
            assertThat(rateLimitGuard.allow(userId, type)).isTrue();
        }
    }

    @Test
    @DisplayName("용량을 초과하면 요청을 거부한다")
    void should_deny_requests_exceeding_capacity() {
        // given
        long userId = 1L;
        MessageType type = MessageType.CHAT;

        // when
        for (int i = 0; i < 20; i++) {
            rateLimitGuard.allow(userId, type);
        }

        // then
        // 21번째 요청은 거부되어야 한다.
        assertThat(rateLimitGuard.allow(userId, type)).isFalse();
    }

    @Test
    @DisplayName("시간이 지나면 토큰이 리필되어 다시 요청을 허용한다")
    void should_allow_request_again_after_token_refill() throws InterruptedException {
        // given
        long userId = 1L;
        MessageType type = MessageType.ENTER;

        // when
        // ENTER/LEAVE 타입의 capacity는 5
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimitGuard.allow(userId, type)).as("Initial burst").isTrue();
        }
        assertThat(rateLimitGuard.allow(userId, type)).as("Exceeding capacity").isFalse();

        // then
        // ENTER/LEAVE 타입의 리필 속도는 초당 2개. 1초를 기다리면 최소 2개의 토큰이 채워져야 한다.
        TimeUnit.SECONDS.sleep(1);
        assertThat(rateLimitGuard.allow(userId, type)).as("After 1s refill").isTrue();
        assertThat(rateLimitGuard.allow(userId, type)).as("After 1s refill").isTrue();
        assertThat(rateLimitGuard.allow(userId, type)).as("After 1s refill, tokens depleted").isFalse();
    }

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
        // clear 후에는 새 버킷이 생성되므로 다시 capacity만큼 요청이 허용되어야 한다.
        assertThat(rateLimitGuard.allow(userId, type)).isTrue();
    }
}
