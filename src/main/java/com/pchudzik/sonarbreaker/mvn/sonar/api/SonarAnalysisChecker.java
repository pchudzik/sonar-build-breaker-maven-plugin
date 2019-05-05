package com.pchudzik.sonarbreaker.mvn.sonar.api;

import com.pchudzik.sonarbreaker.mvn.CheckerContext;
import com.pchudzik.sonarbreaker.mvn.sonar.api.SonarApi.AnalysisResult;
import org.apache.maven.plugin.MojoExecutionException;

public class SonarAnalysisChecker {
    private final CheckerContext context;
    private final SonarApi sonarApi;
    private final AnalysisCompletionAwait sonarAnalysisAwaiter;

    public SonarAnalysisChecker(CheckerContext context, SonarApi sonarApi, AnalysisCompletionAwait analysisCompletionAwaiter) {
        this.context = context;
        this.sonarApi = sonarApi;
        this.sonarAnalysisAwaiter = analysisCompletionAwaiter;
    }

    public void doCheck() throws MojoExecutionException {
        final SonarApi.AnalysisStatus analysisStatus = sonarAnalysisAwaiter.waitForAnalysisCompletion(
                context.getTaskId(),
                context.getAnalysisTimeout());

        if (analysisStatus.isFailed()) {
            throw new MojoExecutionException("Sonar analysis failed");
        }

        final AnalysisResult analysisResult = sonarApi.getQualityGateStatus(analysisStatus.getAnalysisId());
        if (analysisResult.isFailure()) {
            throw new MojoExecutionException("Quality gate set for project not met.");
        }
    }


}
