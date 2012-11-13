package org.jenkinsci.plugins.dtkit.service;

import com.thalesgroup.dtkit.metrics.model.OutputMetric;
import com.thalesgroup.dtkit.tusar.model.TusarModel;
import com.thalesgroup.dtkit.util.validator.ValidationError;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilderFormatValidation implements Serializable {

    public List<ValidationError> isTusarFormat(File inputXMLFile) {

        if (inputXMLFile == null) {
            throw new NullPointerException("A file must be set.");
        }

        //Keep last errors
        List<ValidationError> errors = new ArrayList<ValidationError>();
        for (OutputMetric outputMetric : TusarModel.getAllTUSAROutput()) {
            errors = outputMetric.validate(inputXMLFile);
            if (errors.isEmpty()) {
                break;
            }
        }

        //Return empty error list or the error list with the last validation
        return errors;
    }
}
