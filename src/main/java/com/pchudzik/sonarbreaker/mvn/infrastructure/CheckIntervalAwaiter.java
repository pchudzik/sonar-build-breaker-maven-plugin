package com.pchudzik.sonarbreaker.mvn.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class CheckIntervalAwaiter {
    private final long sleepTime;

    @SneakyThrows
    public void sleep() {
        Thread.sleep(sleepTime);
    }
}
