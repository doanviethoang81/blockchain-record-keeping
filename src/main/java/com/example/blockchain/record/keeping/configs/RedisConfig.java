package com.example.blockchain.record.keeping.configs;

import com.example.blockchain.record.keeping.utils.EnvUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    private final String host = EnvUtil.get("REDIS_HOST");
    private final String password = EnvUtil.get("REDIS_PASSWORD");
    private int port = 6379;
    private boolean sslEnabled = true;

    public RedisConfig() {
        if (host == null || host.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalStateException("REDIS_HOST or REDIS_PASSWORD is not set. Host: " + host + ", Password: " + password);
        }
        System.out.println("Redis Config: host=" + host + ", port=" + port + ", sslEnabled=" + sslEnabled);
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);
        redisConfig.setPassword(password);

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfig = LettuceClientConfiguration.builder();

        if (sslEnabled) {
            clientConfig.useSsl();
        }

        return new LettuceConnectionFactory(redisConfig, clientConfig.build());
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}

