package com.pchudzik.sonarbreaker.mvn.report;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SonarReportLoader {
    private final String reportLocation;

    public static SonarReport loadReport(String reportLocation) {
        return new SonarReportLoader(reportLocation).load();
    }

    SonarReport load() {
        Properties properties = new Properties();
        try (final FileReader reader = new FileReader(reportLocation)) {
            properties.load(reader);
            return new SonarReport(properties);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Can not load report location from " + reportLocation + ". Have you executed sonar:sonar goal before?",
                    ex);
        }
    }
}
