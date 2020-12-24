/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019, Thales Corporate Services SAS, Gregory Boissinot, Nikolas Falco
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.lib.dtkit.type;

import java.io.Serializable;

import org.jenkinsci.lib.dtkit.model.InputMetric;

import hudson.ExtensionPoint;

@SuppressWarnings("serial")
public abstract class MetricsType implements ExtensionPoint, Serializable {

    private final String pattern;
    private String excludesPattern;
    private boolean skipNoTestFiles;
    private transient Boolean faildedIfNotNew;
    private boolean failIfNotNew = true;
    private boolean deleteOutputFiles = true;
    private boolean stopProcessingIfError;

    protected MetricsType(String pattern,
                          boolean skipNoTestFiles,
                          boolean failIfNotNew,
                          boolean deleteOutputFiles,
                          boolean stopProcessingIfError) {
        this.pattern = pattern;
        this.skipNoTestFiles = skipNoTestFiles;
        this.failIfNotNew = failIfNotNew;
        this.deleteOutputFiles = deleteOutputFiles;
        this.stopProcessingIfError = stopProcessingIfError;
    }

    protected MetricsType(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public String getExcludesPattern() {
        return excludesPattern;
    }

    public void setExcludesPattern(String excludesPattern) {
        this.excludesPattern = excludesPattern;
    }

    public boolean isSkipNoTestFiles() {
        return skipNoTestFiles;
    }

    public void setSkipNoTestFiles(boolean skipNoTestFiles) {
        this.skipNoTestFiles = skipNoTestFiles;
    }

    public boolean isFailIfNotNew() {
        return failIfNotNew;
    }

    public void setFailIfNotNew(boolean failIfNotNew) {
        this.failIfNotNew = failIfNotNew;
    }

    @Deprecated
    public boolean isFaildedIfNotNew() {
        return (faildedIfNotNew == null ? failIfNotNew : faildedIfNotNew.booleanValue());
    }

    public boolean isDeleteOutputFiles() {
        return deleteOutputFiles;
    }

    public void setDeleteOutputFiles(boolean deleteOutputFiles) {
        this.deleteOutputFiles = deleteOutputFiles;
    }

    public boolean isStopProcessingIfError() {
        return stopProcessingIfError;
    }

    public void setStopProcessingIfError(boolean stopProcessingIfError) {
        this.stopProcessingIfError = stopProcessingIfError;
    }

    public abstract InputMetric getInputMetric();

    protected Object readResolve() {
        if (faildedIfNotNew != null) {
            failIfNotNew = faildedIfNotNew.booleanValue();
        }

        return this;
    }

}
