package org.jenkinsci.plugins.dtkit.types.violation;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.ViolationsTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.ViolationsType;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.metrics.model.InputMetricException;
import com.thalesgroup.dtkit.metrics.model.InputMetricFactory;
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