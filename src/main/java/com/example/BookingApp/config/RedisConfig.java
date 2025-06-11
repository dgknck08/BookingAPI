package com.example.BookingApp.config;

import org.springframework.cache.CacheManager;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {
    
	
	
	
	//Daha çok doğrudan Redis ile etkileşim için. 
	//kodun içinde Redis’e key-value şeklinde veri yazmak, okumak, set, hash, list gibi yapılarını kullanmak için.e
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
    
    
    //Spring cache abstraction.  
    //@Cacheable, @CachePut, @CacheEvict annotation kullanilirsa, 
    //bunların arkasında bu CacheManager devreye girip Redisi cache deposu olarak kullanıyor.
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // Different TTL for different cache types
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("events", defaultConfig.entryTtl(Duration.ofHours(1))); // Events cache for 1 hour
        cacheConfigurations.put("bookings", defaultConfig.entryTtl(Duration.ofMinutes(5))); // Bookings cache for 5 minutes
        cacheConfigurations.put("seats", defaultConfig.entryTtl(Duration.ofMinutes(2))); // Seats cache for 2 minutes
        cacheConfigurations.put("venues", defaultConfig.entryTtl(Duration.ofHours(24))); // Venues cache for 24 hours
        cacheConfigurations.put("statistics", defaultConfig.entryTtl(Duration.ofMinutes(15))); // Stats cache for 15 minutes
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
