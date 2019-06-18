package si.slotex.nlp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import si.slotex.nlp.entity.Document;

/**
 * Configuration for jedis connection to Redis. We use redis for queues where we
 * save documents until they are processed.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
@Configuration
public class RedisConfig
{
    private Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Bean
    RedisConnectionFactory jedisConnectionFactory()
    {
        logger.info("Connecting to REDIS instance at: " +redisHost + ":" + redisPort);
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.getPoolConfig().setMaxTotal(20);
        jedisConnectionFactory.getPoolConfig().setMaxIdle(5);
        jedisConnectionFactory.getPoolConfig().setMinIdle(2);

        return jedisConnectionFactory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    RedisTemplate<String, Document> redisTemplate()
    {
        final RedisTemplate<String, Document> redisTemplate = new RedisTemplate<>();

        RedisSerializer keys = new StringRedisSerializer();
        RedisSerializer<Document> values = new Jackson2JsonRedisSerializer<>(Document.class);

        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setKeySerializer(keys);
        redisTemplate.setValueSerializer(values);
        redisTemplate.setHashKeySerializer(keys);
        redisTemplate.setHashValueSerializer(values);
        return redisTemplate;
    }

}
