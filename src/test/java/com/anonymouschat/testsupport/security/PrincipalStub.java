package com.anonymouschat.testsupport.security;

import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import java.security.Principal;

/**
 * 테스트용 Principal 객체를 생성하는 유틸리티 클래스.
 */
public class PrincipalStub {
    public static Principal authenticated(Long userId) {
        return new CustomPrincipal(userId, null, null, "USER");
    }

    public static Principal unauthenticated() {
        return new CustomPrincipal(null, OAuthProvider.GOOGLE, "providerId", null);
    }
}
