package com.anonymouschat.testsupport.security;

import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import java.security.Principal;

/**
 * 테스트 목적으로 {@link CustomPrincipal} 객체를 생성하는 스텁(Stub) 클래스입니다.
 * {@link Principal} 인터페이스를 구현하여 인증된 사용자 또는 인증되지 않은 사용자를 시뮬레이션할 수 있습니다.
 */
public class PrincipalStub {
    /**
     * 지정된 사용자 ID를 가진 인증된 {@link CustomPrincipal} 객체를 생성합니다.
     *
     * @param userId 인증된 사용자의 ID
     * @return 해당 사용자 ID를 가진 Principal 객체
     */
    public static Principal authenticated(Long userId) {
        return new CustomPrincipal(userId, null, null, "USER");
    }

    /**
     * 인증되지 않은 {@link CustomPrincipal} 객체를 생성합니다.
     *
     * @return 인증되지 않은 Principal 객체
     */
    public static Principal unauthenticated() {
        return new CustomPrincipal(null, OAuthProvider.GOOGLE, "providerId", null);
    }
}
