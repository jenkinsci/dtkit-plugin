package org.jenkinsci.plugins.dtkit.service;

import com.google.inject.Inject;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.util.validator.ValidationError;
import com.thalesgroup.dtkit.util.validator.ValidationException;
import org.jenkinsci.plugins.dtkit.exception.DTKitBuilderException;
import org.jenkinsci.plugins.dtkit.transformer.DTKitBuilderToolInfo;

import java.io.File;
import java.io.Serializable;


/**
 * @author Gregory Boissinot
 */
public class DTKitBuilderValidationService implements Serializable {

    private DTKitBuilderLog log;

    @Inject
    @SuppressWarnings("unused")
    void load(DTKitBuilderLog log) {
        this.log = log;
    }

    /**
     * Checks if the current input file is not empty
     *
     * @param inputFile the input file
     * @return true if not empty, false otherwise
     */
    public boolean checkFileIsNotEmpty(File inputFile) {
        return inputFile.length() != 0;
    }

    /**
     * Validates an input file
     *
     * @param DTKitBuilderToolInfo the tool tool info wrapper
     * @param inputFile            the current input file
     * @return true if the validation is success, false otherwise
     * @throws org.jenkinsci.plugins.dtkit.exception.DTKitBuilderException
     *          org.jenkinsci.plugins.dtkit.exception.TusarNotifierException
     *          an XUnitException when there are validation exceptions
     */
    public boolean validateInputFile(DTKitBuilderToolInfo DTKitBuilderToolInfo, File inputFile) throws DTKitBuilderException {

        InputMetric inputMetric = DTKitBuilderToolInfo.getMetricsType().getInputMetric();

        //Validate the input file (nom empty)
        try {
            if (!inputMetric.validateInputFile(inputFile)) {
                log.warning("The file '" + inputFile + "' is an invalid file.");
                for (ValidationError validatorError : inputMetric.getInputValidationErrors()) {
                    log.warning(validatorError.toString());
                }

                return false;
            }
        } catch (ValidationException ve) {
            throw new DTKitBuilderException("Validation error on input", ve);
        }
        return true;
    }


    /**
     * Validates the converted file against a JUnit format
     *
     * @param DTKitBuilderToolInfo the tool info wrapper object
     * @param inputFile            the input metric from the conversion
     * @param junitTargetFile      the converted input file
     * @return true if the validation is success, false otherwise
     * @throws org.jenkinsci.plugins.dtkit.exception.DTKitBuilderException
     *          org.jenkinsci.plugins.dtkit.exception.TusarNotifierException
     */
    public boolean validateOutputFile(DTKitBuilderToolInfo DTKitBuilderToolInfo, File inputFile, File junitTargetFile) throws DTKitBuilderException {
        InputMetric inputMetric = DTKitBuilderToolInfo.getMetricsType().getInputMetric();

        try {
            boolean validateOutput = inputMetric.validateOutputFile(junitTargetFile);
            if (!validateOutput) {
                log.error("The converted file for the input file '" + inputFile + "' doesn't match the TUSAR format");
                for (ValidationError validatorError : inputMetric.getOutputValidationErrors()) {
                    log.error(validatorError.toString());
                }
                return false;
            }

        } catch (ValidationException ve) {
            throw new DTKitBuilderException("Validation error on output", ve);
        }

        return true;
    }
}
