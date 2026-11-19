package com.example.mongo.service.impl;

import com.example.mongo.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TestServiceImpl implements TestService {
    @Override
    public void testCompletableFuture() {
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MINUTES.sleep(1L);
                log.info("Test");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
