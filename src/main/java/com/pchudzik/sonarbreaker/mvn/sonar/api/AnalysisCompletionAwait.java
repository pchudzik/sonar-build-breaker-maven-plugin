package com.pchudzik.sonarbreaker.mvn.sonar.api;

import com.pchudzik.sonarbreaker.mvn.infrastructure.CheckIntervalAwaiter;
import com.pchudzik.sonarbreaker.mvn.infrastructure.TimeProvider;
import com.pchudzik.sonarbreaker.mvn.sonar.api.SonarApi.AnalysisStatus;
import lombok.Builder;
import org.apache.maven.plugin.MojoExecutionException;

@Builder
public class AnalysisCompletionAwait {
    private final SonarApi sonarApi;
    private final TimeProvider timeProvider;
    private final CheckIntervalAwaiter checkIntervalAwaiter;

    public AnalysisStatus waitForAnalysisCompletion(String taskId, final int analysisTimeout) throws MojoExecutionException {
        final long startQuery = timeProvider.currentTimeMillis();

        AnalysisStatus analysisStatus;
        boolean isTimeout;

        do {
            analysisStatus = sonarApi.getAnalysisStatus(taskId);
            isTimeout = timeProvider.currentTimeMillis() - startQuery > analysisTimeout;

            checkIntervalAwaiter.sleep();
        } while (!(analysisStatus.isFinished() || isTimeout));

        if (isTimeout) {
            throw new MojoExecutionException("Failed when obtaining analysis results after " + analysisTimeout);
        }

        return analysisStatus;
    }
}
