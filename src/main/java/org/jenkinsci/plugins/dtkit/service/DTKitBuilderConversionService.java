package org.jenkinsci.plugins.dtkit.service;

import com.google.inject.Inject;
import com.thalesgroup.dtkit.metrics.hudson.api.type.MetricsType;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.util.converter.ConversionException;
import org.jenkinsci.plugins.dtkit.exception.DTKitBuilderException;
import org.jenkinsci.plugins.dtkit.transformer.DTKitBuilderToolInfo;
import org.jenkinsci.plugins.dtkit.types.CustomInputMetric;
import org.jenkinsci.plugins.dtkit.types.CustomType;

import java.io.File;
import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilderConversionService implements Serializable {

    private DTKitBuilderLog log;

    @Inject
    @SuppressWarnings("unused")
    void load(DTKitBuilderLog log) {
        this.log = log;
    }

    /**
     * Prepares the conversion by adding specific behavior for the CustomType
     *
     * @param DTKitBuilderToolInfo the info wrapper object
     * @param workspace            the current workspace
     * @throws org.jenkinsci.plugins.dtkit.exception.DTKitBuilderException
     *          an XUnitException is thrown if there is a preparation error.
     */
    private void prepareConversion(DTKitBuilderToolInfo DTKitBuilderToolInfo, File workspace) throws DTKitBuilderException {
        MetricsType metricsType = DTKitBuilderToolInfo.getMetricsType();
        if (metricsType instanceof CustomType) {
            String xsl = ((CustomType) metricsType).getCustomXSL();
            File xslFile = new File(workspace, xsl);
            if (!xslFile.exists()) {
                throw new DTKitBuilderException("The input xsl '" + xsl + "' relative to the workspace '" + workspace + "'doesn't exist.");
            }
            DTKitBuilderToolInfo.setCusXSLFile(xslFile);
        }
    }


    /**
     * Converts the inputFile into a JUnit output file
     *
     * @param dtKitBuilderToolInfo the tool info wrapper object
     * @param inputFile            the input file to be converted
     * @param workspace            the workspace
     * @param outputDirectory      the output parent directory that contains the TUSAR output file
     * @return the converted file
     * @throws org.jenkinsci.plugins.dtkit.exception.DTKitBuilderException
     *          an XUnitException is thrown if there is a conversion error.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File convert(DTKitBuilderToolInfo dtKitBuilderToolInfo, File inputFile, File workspace, File outputDirectory) throws DTKitBuilderException {

        //Prepare the conversion when there is a custom type
        prepareConversion(dtKitBuilderToolInfo, workspace);

        MetricsType metricsType = dtKitBuilderToolInfo.getMetricsType();

        InputMetric inputMetric = dtKitBuilderToolInfo.getInputMetric();

        final String TUSAR_FILE_POSTFIX = ".xml";
        final String TUSAR_FILE_PREFIX = "TUSAR-";
        File junitTargetFile = new File(outputDirectory, TUSAR_FILE_PREFIX + inputFile.hashCode() + TUSAR_FILE_POSTFIX);
        log.info("Converting '" + inputFile + "' .");

        try {
            //Set the XSL for custom type
            if (metricsType instanceof CustomType) {
                ((CustomInputMetric) inputMetric).setCustomXSLFile(dtKitBuilderToolInfo.getCusXSLFile());
            }
            inputMetric.convert(inputFile, junitTargetFile);
        } catch (ConversionException ce) {
            throw new DTKitBuilderException("Conversion error", ce);
        }

        return junitTargetFile;
    }
}
