/*******************************************************************************
 * Copyright (c) 2010 Thales Corporate Services SAS                             *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.tusarnotifier.types.coverage;

import org.jenkinsci.lib.dtkit.descriptor.CoverageTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.CoverageType;
import com.thalesgroup.hudson.plugins.tusarnotifier.types.CustomType;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class CustomCoverageType extends CoverageType implements CustomType {

    private String customXSL;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public CustomCoverageType(String pattern, String customXSL, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
        this.customXSL = customXSL;
    }

    public CoverageTypeDescriptor<? extends CoverageType> getDescriptor() {
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public String getCustomXSL() {
        return customXSL;
    }

    public Object readResolve() {
        return new org.jenkinsci.plugins.dtkit.types.coverage.CustomCoverageType(this.getPattern(), this.getCustomXSL(), this.isFailIfNotNew(), this.isDeleteOutputFiles());
    }
}