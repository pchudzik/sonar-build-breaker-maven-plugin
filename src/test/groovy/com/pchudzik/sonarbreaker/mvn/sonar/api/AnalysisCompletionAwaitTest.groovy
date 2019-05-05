package com.pchudzik.sonarbreaker.mvn.sonar.api

import com.pchudzik.sonarbreaker.mvn.infrastructure.CheckIntervalAwaiter
import com.pchudzik.sonarbreaker.mvn.infrastructure.TimeProvider
import org.apache.maven.plugin.MojoExecutionException
import spock.lang.Specification

import java.util.stream.IntStream

class AnalysisCompletionAwaitTest extends Specification {

    private static final boolean finished = true
    private static final boolean notFinished = false

    private final String taskId = "alamakota123"
    private final analysisTimeout = 20

    private sonarApi = Mock(SonarApi)
    private timeProvider = Mock(TimeProvider)
    private checkIntervalAwaiter = Mock(CheckIntervalAwaiter)
    private awaiter = AnalysisCompletionAwait.builder()
            .sonarApi(sonarApi)
            .timeProvider(timeProvider)
            .checkIntervalAwaiter(checkIntervalAwaiter)
            .build()

    def "awaits until analysis is finished"() {
        given:
        def finishedStatus = analysisStatus(finished)
        sonarApi.getAnalysisStatus(taskId) >>> [
                analysisStatus(notFinished),
                finishedStatus
        ]

        when:
        def lastStatus = awaiter.waitForAnalysisCompletion(taskId, analysisTimeout)

        then:
        noExceptionThrown()

        and:
        lastStatus == finishedStatus
    }

    def "awaits until timeout is not reached"() {
        sonarApi.getAnalysisStatus(taskId) >> analysisStatus(notFinished)
        timeProvider.currentTimeMillis() >>> IntStream.range(0, 12).toArray()


        when:
        awaiter.waitForAnalysisCompletion(taskId, 10)

        then:
        thrown(MojoExecutionException)
    }

    private SonarApi.AnalysisStatus analysisStatus(boolean finished) {
        Mock(SonarApi.AnalysisStatus) {
            isFinished() >> finished
        }
    }
}
