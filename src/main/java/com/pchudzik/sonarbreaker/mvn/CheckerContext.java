package com.pchudzik.sonarbreaker.mvn;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckerContext {
    private final String taskId;
    private final int analysisTimeout;
}
