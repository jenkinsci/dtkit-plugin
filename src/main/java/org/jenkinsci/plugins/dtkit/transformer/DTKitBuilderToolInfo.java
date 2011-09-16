package org.jenkinsci.plugins.dtkit.transformer;

import com.thalesgroup.dtkit.metrics.hudson.api.type.MetricsType;

import java.io.File;
import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilderToolInfo implements Serializable {

    private File cusXSLFile;

    private final MetricsType metricsType;

    private final File outputDir;

    private final String expandedPattern;

    private final long buildTime;

    public DTKitBuilderToolInfo(MetricsType metricsType, File outputDir, String expandedPattern, long buildTime) {
        this.metricsType = metricsType;
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
}
