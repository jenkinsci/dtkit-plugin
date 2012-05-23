package org.jenkinsci.plugins.dtkit.transformer;

import com.thalesgroup.dtkit.metrics.hudson.api.type.*;
import com.thalesgroup.dtkit.metrics.model.InputMetric;

import java.io.File;
import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilderToolInfo implements Serializable {

    private File cusXSLFile;

    private final MetricsType metricsType;

    private final InputMetric inputMetric;

    private final File outputDir;

    private final String expandedPattern;

    private final long buildTime;

    private final String toolName;

    public DTKitBuilderToolInfo(MetricsType metricsType, File outputDir, String expandedPattern, long buildTime) {
        this.metricsType = metricsType;
        this.toolName = retrieveToolName(metricsType);
        this.inputMetric = metricsType.getInputMetric();
        this.outputDir = outputDir;
        this.expandedPattern = expandedPattern;
        this.buildTime = buildTime;
    }

    public void setCusXSLFile(File cusXSLFile) {
        this.cusXSLFile = cusXSLFile;
    }

    public File getCusXSLFile() {
        return cusXSLFile;
    }

    public MetricsType getMetricsType() {
        return metricsType;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public String getExpandedPattern() {
        return expandedPattern;
    }

    public long getBuildTime() {
        return buildTime;
    }

    public String getToolName() {
        return toolName;
    }

    public InputMetric getInputMetric() {
        return inputMetric;
    }

    private String retrieveToolName(MetricsType metricsType) {
        String toolName = null;
        if (metricsType instanceof TestType) {
            toolName = ((TestType) metricsType).getDescriptor().getDisplayName();
        }
        if (metricsType instanceof CoverageType) {
            toolName = ((CoverageType) metricsType).getDescriptor().getDisplayName();
        }
        if (metricsType instanceof ViolationsType) {
            toolName = ((ViolationsType) metricsType).getDescriptor().getDisplayName();
        }
        if (metricsType instanceof MeasureType) {
            toolName = ((MeasureType) metricsType).getDescriptor().getDisplayName();
        }
        return toolName;
    }
}
