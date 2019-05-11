package org.jenkinsci.plugins.dtkit.transformer;

import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import jenkins.SlaveToMasterFileCallable;
import org.jenkinsci.plugins.dtkit.exception.DTKitBuilderException;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderConversionService;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderLog;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderValidationService;
import org.jenkinsci.plugins.dtkit.service.DTKitReportProcessingService;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilderTransformer extends SlaveToMasterFileCallable<Boolean> {
    private static final long serialVersionUID = 1347249196623983658L;

    private DTKitReportProcessingService dtkitReportProcessingService;

    private DTKitBuilderConversionService dtkitBuilderConversionService;

    private DTKitBuilderValidationService dtkitBuilderValidationService;

    private DTKitBuilderToolInfo dtkitBuilderToolInfo;

    private DTKitBuilderLog dtkitBuilderLog;

    @Inject
    void loadService(
            DTKitReportProcessingService dtkitReportProcessingService,
            DTKitBuilderConversionService dtkitBuilderConversionService,
            DTKitBuilderValidationService dtkitBuilderValidationService,
            DTKitBuilderToolInfo dtkitBuilderToolInfo,
            DTKitBuilderLog dtkitBuilderLog) {
        this.dtkitReportProcessingService = dtkitReportProcessingService;
        this.dtkitBuilderConversionService = dtkitBuilderConversionService;
        this.dtkitBuilderValidationService = dtkitBuilderValidationService;
        this.dtkitBuilderToolInfo = dtkitBuilderToolInfo;
        this.dtkitBuilderLog = dtkitBuilderLog;
    }

    public Boolean invoke(File ws, hudson.remoting.VirtualChannel channel) throws IOException, InterruptedException {

        try {

            List<String> resultFiles = dtkitReportProcessingService.findReports(dtkitBuilderToolInfo, ws, dtkitBuilderToolInfo.getExpandedPattern());
            if (resultFiles.size() == 0) {
                return false;
            }

            if (!dtkitReportProcessingService.checkIfFindsFilesNewFiles(dtkitBuilderToolInfo, resultFiles, ws)) {
                return false;
            }

            for (String curFileName : resultFiles) {

                File curFile = dtkitReportProcessingService.getCurrentReport(ws, curFileName);
                if (!dtkitBuilderValidationService.checkFileIsNotEmpty(curFile)) {
                    String msg = "The file '" + curFile.getPath() + "' is empty. This file has been ignored.";
                    dtkitBuilderLog.warning(msg);
                    return false;
                }
                dtkitBuilderLog.info("Converting '" + curFile + "' .");
                if (!dtkitBuilderValidationService.validateInputFile(dtkitBuilderToolInfo, curFile)) {
                    dtkitBuilderLog.warning("The file '" + curFile + "' has been ignored.");
                    return false;
                }

                File targetFile = dtkitBuilderConversionService.convert(dtkitBuilderToolInfo, curFile, ws, dtkitBuilderToolInfo.getOutputDir());

                boolean result = dtkitBuilderValidationService.validateOutputFile(dtkitBuilderToolInfo, curFile, targetFile);
                if (!result) {
                    return false;
                }
            }

        } catch (DTKitBuilderException xe) {
            throw new IOException("There are some problems during the conversion into standard output: " + xe.getMessage(), xe);
        }

        return true;
    }

}
