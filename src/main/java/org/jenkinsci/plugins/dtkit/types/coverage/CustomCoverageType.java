package org.jenkinsci.plugins.dtkit.types.coverage;

import org.jenkinsci.lib.dtkit.descriptor.CoverageTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.CoverageType;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricException;
import org.jenkinsci.lib.dtkit.model.InputMetricFactory;
import hudson.Extension;
import org.jenkinsci.plugins.dtkit.types.CustomType;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class CustomCoverageType extends CoverageType implements CustomType {

    private static CustomCoverageInputMetricDescriptor DESCRIPTOR = new CustomCoverageInputMetricDescriptor();

    private String customXSL;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public CustomCoverageType(String pattern, String customXSL, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
        this.customXSL = customXSL;
    }

    public CoverageTypeDescriptor<? extends CoverageType> getDescriptor() {
        return DESCRIPTOR;
    }

    @SuppressWarnings("unused")
    @Override
    public String getCustomXSL() {
        return customXSL;
    }

    @Extension
    public static class CustomCoverageInputMetricDescriptor extends CoverageTypeDescriptor<CustomCoverageType> {

        public CustomCoverageInputMetricDescriptor() {
            super(CustomCoverageType.class, null);
        }

        @Override
        public String getId() {
            return this.getClass().getName();
        }

        @Override
        public InputMetric getInputMetric() {
            try {
                return InputMetricFactory.getInstance(CustomCoverageInputMetric.class);
            } catch (InputMetricException e) {
                throw new RuntimeException("Can't create the inputMetric object for the class " + CustomCoverageInputMetric.class);
            }
        }

        public boolean isCustomType() {
            return true;
        }
    }
}