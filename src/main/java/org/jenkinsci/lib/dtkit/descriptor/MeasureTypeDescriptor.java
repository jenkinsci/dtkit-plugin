/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2010, Thales Corporate Services SAS, Gregory Boissinot
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
package org.jenkinsci.lib.dtkit.descriptor;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricException;
import org.jenkinsci.lib.dtkit.model.InputMetricFactory;
import org.jenkinsci.lib.dtkit.type.MeasureType;


public abstract class MeasureTypeDescriptor<T extends MeasureType> extends Descriptor<MeasureType> {

    private transient Class<? extends InputMetric> inputMetricClass;

    protected MeasureTypeDescriptor(Class<T> clazz, final Class<? extends InputMetric> inputMetricClass) {
        super(clazz);
        this.inputMetricClass = inputMetricClass;
    }

    public static DescriptorExtensionList<MeasureType, MeasureTypeDescriptor<?>> all() {
        return Hudson.get().<MeasureType, MeasureTypeDescriptor<?>>getDescriptorList(MeasureType.class);
    }

    @Override
    public String getDisplayName() {
        return getInputMetric().getLabel();
    }

    public InputMetric getInputMetric() {
        try {
            return InputMetricFactory.getInstance(inputMetricClass);
        } catch (InputMetricException e) {
            return null;
        }
    }
}
