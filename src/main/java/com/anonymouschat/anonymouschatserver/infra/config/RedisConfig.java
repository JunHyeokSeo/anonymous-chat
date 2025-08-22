package com.anonymouschat.anonymouschatserver.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@EnableConfigurationProperties(OAuthTokenProperties.class)
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// ObjectMapper 설정
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		// Key Serializer: String
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		// Value Serializer: JSON
		GenericJackson2JsonRedisSerializer jsonSerializer =
				new GenericJackson2JsonRedisSerializer(objectMapper);
		template.setValueSerializer(jsonSerializer);
		template.setHashValueSerializer(jsonSerializer);

		template.afterPropertiesSet();
		return template;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
				                                 .entryTtl(Duration.ofMinutes(5))
				                                 .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				                                 .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
				                                 .disableCachingNullValues();

		return RedisCacheManager.builder(connectionFactory)
				       .cacheDefaults(config)
				       .build();
	}
}