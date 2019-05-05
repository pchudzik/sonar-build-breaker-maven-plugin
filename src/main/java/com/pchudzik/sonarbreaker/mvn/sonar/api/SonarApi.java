package com.pchudzik.sonarbreaker.mvn.sonar.api;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.Log;

public class SonarApi {

    private final String sonarUrl;
    private final Log log;

    public SonarApi(String sonarUrl, Log log) {
        this.sonarUrl = sonarUrl;
        this.log = log;
        Unirest.setObjectMapper(new JacksonBasedObjectMapper(log));
    }

    @SneakyThrows
    public AnalysisStatus getAnalysisStatus(String taskId) {
        return Unirest
                .get(sonarApiUrl("/ce/task"))
                .queryString("id", taskId)
                .asObject(AnalysisStatus.class)
                .getBody();
    }

    @SneakyThrows
    public AnalysisResult getQualityGateStatus(String analysisId) {
        return Unirest
                .get(sonarApiUrl("/qualitygates/project_status"))
                .queryString("analysisId", analysisId)
                .asObject(AnalysisResult.class)
                .getBody();
    }

    private String sonarApiUrl(String url) {
        return sonarUrl + "/api" + url;
    }

    private enum AnalysisSonarStatus {
        SUCCESS,
        CANCELED,
        FAILED,

        @JsonEnumDefaultValue
        PENDING;

        boolean isFinished() {
            return this != PENDING;
        }

        boolean isFailed() {
            return this == CANCELED || this == FAILED;
        }
    }

    private enum AnalysisSonarResult {
        OK,

        @JsonEnumDefaultValue
        NOT_OK;

        public boolean isFailed() {
            return this != OK;
        }
    }

    @JsonRootName("task")
    static class AnalysisStatus {
        @JsonProperty("status")
        private AnalysisSonarStatus analysisStatus;

        @Getter
        @JsonProperty("executionTimeMs")
        private long executionTime;

        @Getter
        @JsonProperty
        private String analysisId;

        public boolean isFailed() {
            return analysisStatus.isFailed();
        }

        public boolean isFinished() {
            return analysisStatus.isFinished();
        }
    }

    @JsonRootName("projectStatus")
    static class AnalysisResult {
        @JsonProperty("status")
        private AnalysisSonarResult analysisResult;

        public boolean isFailure() {
            return analysisResult.isFailed();
        }
    }

    private static class JacksonBasedObjectMapper implements ObjectMapper {
        private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        private final Log log;

        public JacksonBasedObjectMapper(Log log) {
            this.log = log;
            mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        }

        @Override
        @SneakyThrows
        public <T> T readValue(String value, Class<T> valueType) {
            log.debug("Received response from sonar: " + value);
            return mapper.readValue(value, valueType);
        }

        @Override
        @SneakyThrows
        public String writeValue(Object value) {
            throw new UnsupportedOperationException("Not supported operation as object mapper is configured for reading data");
        }
    }
}
