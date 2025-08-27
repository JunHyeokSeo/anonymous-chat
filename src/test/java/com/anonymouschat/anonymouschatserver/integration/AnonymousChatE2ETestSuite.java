package com.anonymouschat.anonymouschatserver.integration;

import com.anonymouschat.anonymouschatserver.integration.controller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AuthControllerIntegrationTest.class,
		AuthCallbackControllerIntegrationTest.class,
		UserControllerIntegrationTest.class,
		ChatRoomControllerIntegrationTest.class,
		MessageControllerIntegrationTest.class,
		BlockControllerIntegrationTest.class,
		PageControllerIntegrationTest.class
})
@DisplayName("Anonymous Chat Server - E2E Integration Test Suite")
public class AnonymousChatE2ETestSuite {
}