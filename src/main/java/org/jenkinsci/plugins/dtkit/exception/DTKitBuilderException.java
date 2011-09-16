package org.jenkinsci.plugins.dtkit.exception;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilderException extends Exception {

    public DTKitBuilderException(String message) {
        super(message);
    }

    public DTKitBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

}
