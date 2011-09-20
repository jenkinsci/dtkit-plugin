package org.jenkinsci.plugins.dtkit.transformer;

import com.google.inject.Inject;
import hudson.FilePath;
import hudson.util.IOException2;
import org.jenkinsci.plugins.dtkit.exception.DTKitBuilderException;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderConversionService;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderLog;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderValidationService;
import org.jenkinsci.plugins.dtkit.service.DTKitReportProcessingService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilderTransformer implements FilePath.FileCallable<Boolean>, Serializable {

    private DTKitReportProcessingService DTKitReportProcessingService;

    private DTKitBuilderConversionService DTKitBuilderConversionService;

    private DTKitBuilderValidationService DTKitBuilderValidationService;

    private DTKitBuilderToolInfo DTKitBuilderToolInfo;

    private DTKitBuilderLog DTKitBuilderLog;

    @Inject
    @SuppressWarnings("unused")
    void loadService(
            DTKitReportProcessingService DTKitReportProcessingService,
            DTKitBuilderConversionService DTKitBuilderConversionService,
            DTKitBuilderValidationService DTKitBuilderValidationService,
            DTKitBuilderToolInfo DTKitBuilderToolInfo,
            DTKitBuilderLog DTKitBuilderLog) {
        this.DTKitReportProcessingService = DTKitReportProcessingService;
        this.DTKitBuilderConversionService = DTKitBuilderConversionService;
        this.DTKitBuilderValidationService = DTKitBuilderValidationService;
        this.DTKitBuilderToolInfo = DTKitBuilderToolInfo;
        this.DTKitBuilderLog = DTKitBuilderLog;
    }

    public Boolean invoke(File ws, hudson.remoting.VirtualChannel channel) throws IOException, InterruptedException {

        try {

            List<String> resultFiles = DTKitReportProcessingService.findReports(DTKitBuilderToolInfo, ws, DTKitBuilderToolInfo.getExpandedPattern());
            if (resultFiles.size() == 0) {
                return false;
            }

            if (!DTKitReportProcessingService.checkIfFindsFilesNewFiles(DTKitBuilderToolInfo, resultFiles, ws)) {
                return false;
            }

            for (String curFileName : resultFiles) {

                File curFile = DTKitReportProcessingService.getCurrentReport(ws, curFileName);
                if (!DTKitBuilderValidationService.checkFileIsNotEmpty(curFile)) {
                    String msg = "The file '" + curFile.getPath() + "' is empty. This file has been ignored.";
                    DTKitBuilderLog.warning(msg);
                    return false;
                }

                if (!DTKitBuilderValidationService.validateInputFile(DTKitBuilderToolInfo, curFile)) {
                    DTKitBuilderLog.warning("The file '" + curFile + "' has been ignored.");
                    return false;
                }

                File targetFile = DTKitBuilderConversionService.convert(DTKitBuilderToolInfo, curFile, ws, DTKitBuilderToolInfo.getOutputDir());

                boolean result = DTKitBuilderValidationService.validateOutputFile(DTKitBuilderToolInfo, curFile, targetFile);
                if (!result) {
                    return false;
                }
            }

        } catch (DTKitBuilderException xe) {
            throw new IOException2("There are some problems during the conversion into standard output: " + xe.getMessage(), xe);
        }

        return true;
    }

}
