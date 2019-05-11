package org.jenkinsci.plugins.dtkit.types.violation;

import org.jenkinsci.lib.dtkit.descriptor.ViolationsTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.ViolationsType;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricException;
import org.jenkinsci.lib.dtkit.model.InputMetricFactory;
import hudson.Extension;
import org.jenkinsci.plugins.dtkit.types.CustomType;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class CustomViolationType extends ViolationsType implements CustomType {

    private static CustomViolationsTypeDescriptor DESCRIPTOR = new CustomViolationsTypeDescriptor();

    private String customXSL;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public CustomViolationType(String pattern, String customXSL, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
        this.customXSL = customXSL;
    }

    public ViolationsTypeDescriptor<? extends ViolationsType> getDescriptor() {
        return DESCRIPTOR;
    }

    @SuppressWarnings("unused")
    @Override
    public String getCustomXSL() {
        return customXSL;
    }

    @Extension
    public static class CustomViolationsTypeDescriptor extends ViolationsTypeDescriptor<CustomViolationType> {

        public CustomViolationsTypeDescriptor() {
            super(CustomViolationType.class, null);
        }

        @Override
        public String getId() {
            return this.getClass().getName();
        }

        @Override
        public InputMetric getInputMetric() {
            try {
                return InputMetricFactory.getInstance(CustomViolationInputMetric.class);
            } catch (InputMetricException e) {
                throw new RuntimeException("Can't create the inputMetric object for the class " + CustomViolationInputMetric.class);
            }
        }

        public boolean isCustomType() {
            return true;
        }
    }
}