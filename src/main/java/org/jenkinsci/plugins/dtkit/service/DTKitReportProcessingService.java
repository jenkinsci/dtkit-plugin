package org.jenkinsci.plugins.dtkit.service;

import com.google.inject.Inject;
import com.thalesgroup.dtkit.metrics.hudson.api.type.MetricsType;
import hudson.Util;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.dtkit.transformer.DTKitBuilderToolInfo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Gregory Boissinot
 */
public class DTKitReportProcessingService implements Serializable {

    private DTKitBuilderLog log;

    @Inject
    @SuppressWarnings("unused")
    void load(DTKitBuilderLog log) {
        this.log = log;
    }

    /**
     * Tests if the pattern is empty.
     *
     * @param pattern the given pattern of the current test tool
     * @return true if empty or blank, false otherwise
     */
    public boolean isEmptyPattern(String pattern) {
        return pattern == null || pattern.trim().length() == 0;
    }

    /**
     * Gets all reports from the given parent path and the pattern.
     */
    public List<String> findReports(DTKitBuilderToolInfo dTKitBuilderToolInfo, File parentPath, String pattern) {

        String toolName = dTKitBuilderToolInfo.getToolName();
        FileSet fs = Util.createFileSet(parentPath, pattern);
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] xunitFiles = ds.getIncludedFiles();

        if (xunitFiles.length == 0) {
            String msg = "[" + toolName + "] - No test report file(s) were found with the pattern '"
                    + pattern + "' relative to '" + parentPath + "' for the testing framework '" + toolName + "'."
                    + "  Did you enter a pattern relative to the correct directory?"
                    + "  Did you generate the result report(s) for '" + toolName + "'?";
            log.error(msg);
        } else {
            String msg = "[" + toolName + "] - " + xunitFiles.length + " test report file(s) were found with the pattern '"
                    + pattern + "' relative to '" + parentPath + "' for the testing framework '" + toolName + "'.";
            log.info(msg);
        }
        return Arrays.asList(xunitFiles);
    }


    /**
     * Checks if all the finds files are new file
     *
     * @param DTKitBuilderToolInfo the wrapped object
     * @param files                the file list
     * @param workspace            the root location of the file list
     * @return true if all files are new, false otherwise
     */
    public boolean checkIfFindsFilesNewFiles(DTKitBuilderToolInfo DTKitBuilderToolInfo, List<String> files, File workspace) {

        MetricsType metricsType = DTKitBuilderToolInfo.getMetricsType();

        if (metricsType.isFailIfNotNew()) {
            ArrayList<File> oldResults = new ArrayList<File>();
            for (String value : files) {
                File reportFile = new File(workspace, value);
                // if the file was not updated this build, that is a problem
                if (DTKitBuilderToolInfo.getBuildTime() - 3000 > reportFile.lastModified()) {
                    oldResults.add(reportFile);
                }
            }

            if (!oldResults.isEmpty()) {
                long localTime = System.currentTimeMillis();
                if (localTime < DTKitBuilderToolInfo.getBuildTime() - 1000) {
                    // build time is in the the future. clock on this slave must be running behind
                    String msg = "Clock on this slave is out of sync with the master, and therefore \n" +
                            "I can't figure out what test results are new and what are old.\n" +
                            "Please keep the slave clock in sync with the master.";
                    log.error(msg);
                    return false;
                }

                String msg = "Reports were found but not all of them are new. Did all the tests run?\n";
                for (File f : oldResults) {
                    msg += String.format("  * %s is %s old\n", f, Util.getTimeSpanString(DTKitBuilderToolInfo.getBuildTime() - f.lastModified()));
                }
                log.error(msg);
                return false;
            }
        }

        return true;
    }

    /**
     * Gets a file from a root file and a name
     *
     * @param root the root path
     * @param name the filename
     * @return the current file
     */
    public File getCurrentReport(File root, String name) {
        return new File(root, name);
    }
}
