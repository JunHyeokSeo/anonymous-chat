package com.anonymouschat.anonymouschatserver.domain.type;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;

import java.util.Map;

public enum OAuthProvider {
	GOOGLE {
		@Override
		public String extractProviderId(Map<String, Object> attributes) {
			return (String) attributes.get("sub");
		}
	},
	NAVER {
		@Override
		public String extractProviderId(Map<String, Object> attributes) {
			Object raw = attributes.get("response");
			if (raw instanceof Map<?, ?> rawMap) {
				Object id = rawMap.get("id");
				return String.valueOf(id);
			}
			throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
		}
	},
	KAKAO {
		@Override
		public String extractProviderId(Map<String, Object> attributes) {
			return String.valueOf(attributes.get("id"));
		}
	};

	public abstract String extractProviderId(Map<String, Object> attributes);
}
