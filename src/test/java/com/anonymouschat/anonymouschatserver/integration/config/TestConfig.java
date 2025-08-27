package com.anonymouschat.anonymouschatserver.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "com.anonymouschat.anonymouschatserver.integration.common")
public class TestConfig {
}