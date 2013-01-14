package org.jenkinsci.plugins.dtkit.service;

import com.google.inject.Inject;
import hudson.model.BuildListener;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilderLog implements Serializable {

    private static BuildListener buildListener;

    @Inject
    @SuppressWarnings("unused")
    void set(BuildListener buildListener) {
        DTKitBuilderLog.buildListener = buildListener;
    }

    /**
     * Log an info output to the given logger
     *
     * @param message The message to be outputted
     */
    public void info(String message) {
        buildListener.getLogger().println("[DTKit] [INFO] - " + message);
    }


    /**
     * Log an error output to the given logger
     *
     * @param message The message to be outputted
     */
    public static void error(String message) {
        buildListener.getLogger().println("[DTKit] [ERROR] - " + message);
    }

    /**
     * Log a warning output to the given logger
     *
     * @param message The message to be outputted
     */
    public void warning(String message) {
        buildListener.getLogger().println("[DTKit] [WARNING] - " + message);
    }

}
