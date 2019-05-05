package com.pchudzik.sonarbreaker.mvn.report

import spock.lang.Specification

class SonarReportTest extends Specification {
    private final sonarUrl = "http://localhost:9000"
    private final taskId = "AWp56xNccZwjKgldaYXl"

    def "resolves sonar properties from sonar report summary"() {
        given:
        final properties = createProperties(sonarUrl, taskId)

        when:
        final report = new SonarReport(properties)

        then:
        taskId == report.taskId
        sonarUrl == report.sonarApiUrl
    }

    static Properties createProperties(String sonarUrl, String taskId) {
        final props = new Properties()
        props.load(new StringReader("""
            projectKey=org.sonarqube:parent
            serverUrl=${sonarUrl}
            serverVersion=7.7.0.23042
            dashboardUrl=${sonarUrl}/dashboard?id=org.sonarqube%3Aparent
            ceTaskId=${taskId}
            ceTaskUrl=${sonarUrl}/api/ce/task?id=${taskId}
            """))
        return props
    }
}
