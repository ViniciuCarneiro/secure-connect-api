package com.secure.connect.secure_connect.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
