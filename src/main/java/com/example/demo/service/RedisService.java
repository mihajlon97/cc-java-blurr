package com.example.demo.service;

import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

@Service
public class RedisService {

    public void connectAndPublish(HostAndPort hostAndPort, String channel, String msg) {
        Jedis jedis = null;

        try {
            jedis = new Jedis(hostAndPort);
            jedis.publish(channel, msg);
        } catch (Exception ex) {
            throw new RuntimeException("Jedis exception - " + ex.getMessage());
        } finally {
            if (jedis != null) jedis.close();
        }
    }
}
