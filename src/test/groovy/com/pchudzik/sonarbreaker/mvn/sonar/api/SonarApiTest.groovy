package com.pchudzik.sonarbreaker.mvn.sonar.api

import com.github.tomakehurst.wiremock.WireMockServer
import org.apache.maven.plugin.logging.Log
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

class SonarApiTest extends Specification {
    private static final anyExecutionTime = 2000L
    private static final anyAnalysisId = "AWp56xeh7ZUs_TSvDVqh"
    private WireMockServer wireMock
    private SonarApi sonarApi
    private String sonarUrl

    def setup() {
        final serverOptions = options().port(18273)

        wireMock = new WireMockServer(serverOptions)
        wireMock.start()

        sonarUrl = "http://${serverOptions.bindAddress()}:${serverOptions.portNumber()}"
        sonarApi = new SonarApi(sonarUrl, Mock(Log))
    }

    def cleanup() {
        wireMock.stop()
    }

    @Unroll
    def "detects #status as finished analysis status"() {
        given:
        final taskId = "AWp56xNccZwjKgldaYXl"
        wireMock.stubFor(get("/api/ce/task?id=${taskId}")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(analysisStatusResponse(status))))

        when:
        final result = sonarApi.getAnalysisStatus(taskId)

        then:
        result.finished == isFinished

        where:
        status         || isFinished
        "SUCCESS"      || true
        "CANCELED"     || true
        "FAILED"       || true
        "PENDING"      || false
        "EXTRA_STATUS" || false
    }

    @Unroll
    def "detects #status as failed status"() {
        given:
        final taskId = "AWp56xNccZwjKgldaYXl"
        wireMock.stubFor(get("/api/ce/task?id=${taskId}")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(analysisStatusResponse(status))))

        when:
        final result = sonarApi.getAnalysisStatus(taskId)

        then:
        result.failed == isFailed

        where:
        status         || isFailed
        "CANCELED"     || true
        "FAILED"       || true
        "SUCCESS"      || false
        "PENDING"      || false
        "EXTRA_STATUS" || false
    }

    def "deserializes analysis status fields"() {
        given:
        final taskId = "AWp56xNccZwjKgldaYXl"
        final analysisId = "alamakota123"
        final executionTime = 1827L
        wireMock.stubFor(get("/api/ce/task?id=${taskId}")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(analysisStatusResponse("SUCCESS", analysisId, executionTime))))

        when:
        final result = sonarApi.getAnalysisStatus(taskId)

        then:
        result.finished
        !result.failed

        and:
        result.executionTime == executionTime
        result.analysisId == analysisId
    }

    @Unroll
    def "detects #status as analysis result"() {
        given:
        final analysisId = "alamakota123"
        wireMock.stubFor(get("/api/qualitygates/project_status?analysisId=${analysisId}")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(analysisResultResponse(analysisResult))))

        when:
        final result = sonarApi.getQualityGateStatus(analysisId)

        then:
        result.isFailure() == isFailure

        where:
        analysisResult || isFailure
        "OK"           || false
        "ERROR"        || true
        "EXTRA_STATUS" || true
    }

    private static String analysisResultResponse(String status) {
        """
        {
            "projectStatus": {
                "status": "${status}",
                "conditions": [],
                "periods": [],
                "ignoredConditions": false
            }
        }
        """
    }

    private static String analysisStatusResponse(String status) {
        analysisStatusResponse(status, anyAnalysisId, anyExecutionTime)
    }

    private static String analysisStatusResponse(String status, String analysisId, long executionTime) {
        """
        {
            "task": {
                "id": "AWp56xNccZwjKgldaYXl",
                "type": "REPORT",
                "componentId": "AWp56xLacZwjKgldaYXf",
                "componentKey": "org.sonarqube:parent",
                "componentName": "Example of SonarQube Scanner for Maven + Code Coverage by UT and IT",
                "componentQualifier": "TRK",
                "analysisId": "${analysisId}",
                "status": "${status}",
                "submittedAt": "2019-05-02T19:00:10+0000",
                "startedAt": "2019-05-02T19:00:11+0000",
                "executedAt": "2019-05-02T19:00:13+0000",
                "executionTimeMs": ${executionTime},
                "logs": false,
                "hasScannerContext": true,
                "organization": "default-organization",
                "warningCount": 2,
                "warnings": []
            }
        }
        """
    }
}