package com.pchudzik.sonarbreaker.mvn;

import com.pchudzik.sonarbreaker.mvn.infrastructure.CheckIntervalAwaiter;
import com.pchudzik.sonarbreaker.mvn.infrastructure.TimeProvider;
import com.pchudzik.sonarbreaker.mvn.report.SonarReport;
import com.pchudzik.sonarbreaker.mvn.report.SonarReportLoader;
import com.pchudzik.sonarbreaker.mvn.sonar.api.AnalysisCompletionAwait;
import com.pchudzik.sonarbreaker.mvn.sonar.api.SonarAnalysisChecker;
import com.pchudzik.sonarbreaker.mvn.sonar.api.SonarApi;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "break", defaultPhase = LifecyclePhase.VERIFY, aggregator = true)
public class BuildBreakMojo extends AbstractMojo {

    /**
     * Where maven report is stored.
     */
    @Parameter(
            alias = "sonarbreaker.sonarReportLocation",
            defaultValue = "target/sonar/report-task.txt")
    private String sonarReportLocation;

    @Parameter(
            alias = "sonarbreaker.timeout",
            defaultValue = "180")
    private int sonarAnalysisTimeout;

    @Parameter(
            alias = "sonarbreaker.checkInterval",
            defaultValue = "1000")
    private int checkInterval;

    public void execute() throws MojoExecutionException {
        final SonarReport sonarReport = SonarReportLoader.loadReport(sonarReportLocation);
        final SonarApi sonarApi = new SonarApi(sonarReport.getSonarApiUrl(), getLog());

        final SonarAnalysisChecker checker = new SonarAnalysisChecker(
                CheckerContext.builder()
                        .taskId(sonarReport.getTaskId())
                        .analysisTimeout(sonarAnalysisTimeout)
                        .build(), sonarApi,
                AnalysisCompletionAwait.builder()
                        .timeProvider(new TimeProvider())
                        .checkIntervalAwaiter(new CheckIntervalAwaiter(checkInterval))
                        .sonarApi(sonarApi)
                        .build());

        checker.doCheck();
    }

}
