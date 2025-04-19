package com.urlshortener.demo.Redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisRateLimitService {

    @Autowired
    private final StringRedisTemplate redisTemplate;

    public RedisRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final int MAX_REQUESTS = 10;
    private final int WINDOW_SECONDS = 3600;

    public boolean allowRequest(String clientIp){

        String key = "rateLimit:" + clientIp;

        Long currentCount = redisTemplate.opsForValue().increment(key, 1); //Incrementing the current count associated with client's IP.

        //For every new request, sets expiry of 1 hour with unit of seconds.
        //SLIDING WINDOW IMPLEMENTATION:
        //redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);


        //For the first request, apply a FIXED WINDOW from the time from the first request, and we will reset the rate limit in an hour.
        if (currentCount == 1){
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        return currentCount <= MAX_REQUESTS; //If new request would violate rate limit, return false.

    }

}
