package com.pchudzik.sonarbreaker.mvn.sonar.api

import com.pchudzik.sonarbreaker.mvn.CheckerContext
import org.apache.maven.plugin.MojoExecutionException
import spock.lang.Specification
import spock.lang.Subject


class SonarAnalysisCheckerTest extends Specification {
    private checkerContext = CheckerContext.builder()
            .taskId("taskId")
            .analysisTimeout(100)
            .build()
    private sonarApi = Mock(SonarApi)
    private awaiter = Mock(AnalysisCompletionAwait)

    @Subject
    private checker = new SonarAnalysisChecker(checkerContext, sonarApi, awaiter)

    def "fails when analysis status is failed"() {
        given:
        awaiter.waitForAnalysisCompletion(checkerContext.taskId, checkerContext.analysisTimeout) >> Mock(SonarApi.AnalysisStatus) {
            isFailed() >> true
        }

        when:
        checker.doCheck()

        then:
        thrown(MojoExecutionException)
    }

    def "fails when quality gate is not met"() {
        given:
        final analysisId = "alamakota1"
        awaiter.waitForAnalysisCompletion(checkerContext.taskId, checkerContext.analysisTimeout) >> Mock(SonarApi.AnalysisStatus) {
            isFailed() >> false
            getAnalysisId() >> analysisId
        }
        sonarApi.getQualityGateStatus(analysisId) >> Mock(SonarApi.AnalysisResult) {
            isFailure() >> true
        }

        when:
        checker.doCheck()

        then:
        thrown(MojoExecutionException)
    }

    def "passes when analysis finished and quality gate met"() {
        given:
        final analysisId = "alamakota1"
        awaiter.waitForAnalysisCompletion(checkerContext.taskId, checkerContext.analysisTimeout) >> Mock(SonarApi.AnalysisStatus) {
            isFailed() >> false
            getAnalysisId() >> analysisId
        }
        sonarApi.getQualityGateStatus(analysisId) >> Mock(SonarApi.AnalysisResult) {
            isFailure() >> false
        }

        when:
        checker.doCheck()

        then:
        noExceptionThrown()
    }
}
