package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
@PropertySource("classpath:application.properties")
public class RedisService {

    @Autowired
    private AWSService awsService;

    private Jedis jedis;

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private Integer port;

    @Value("${redis.channel}")
    private String channel;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeJedis() {
        System.out.println(host + ":" + port);

        try {
            jedis = new Jedis(host, port);
            jedis.subscribe(new JedisPubSubImpl(new Jedis(host, port), awsService), channel);
        } catch (Exception ex) {
            System.out.println("Jedis initialization gone wrong. Details: " + ex.getMessage());
            throw ex;
        }

    }
}
