package com.pchudzik.sonarbreaker.mvn.report;

import lombok.RequiredArgsConstructor;

import java.util.Properties;

@RequiredArgsConstructor
public class SonarReport {
    private final Properties properties;

    public String getTaskId() {
        return properties.getProperty("ceTaskId");
    }

    public String getSonarApiUrl() {
        return properties.getProperty("serverUrl");
    }
}
