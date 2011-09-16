package org.jenkinsci.plugins.dtkit.types.test;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.metrics.model.InputMetricException;
import com.thalesgroup.dtkit.metrics.model.InputMetricFactory;
import hudson.Extension;
import org.jenkinsci.plugins.dtkit.types.CustomType;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class CustomTestType extends TestType implements CustomType {

    private static TestCustomInputMetricDescriptor DESCRIPTOR = new TestCustomInputMetricDescriptor();

    private String customXSL;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public CustomTestType(String pattern, String customXSL, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
        this.customXSL = customXSL;
    }

    public TestTypeDescriptor<? extends TestType> getDescriptor() {
        return DESCRIPTOR;
    }

    @SuppressWarnings("unused")
    @Override
    public String getCustomXSL() {
        return customXSL;
    }

    @Extension
    public static class TestCustomInputMetricDescriptor extends TestTypeDescriptor<CustomTestType> {

        public TestCustomInputMetricDescriptor() {
            super(CustomTestType.class, null);
        }

        @Override
        public String getId() {
            return this.getClass().getName();
        }

        @Override
        public InputMetric getInputMetric() {
            try {
                return InputMetricFactory.getInstance(CustomTestInputMetric.class);
            } catch (InputMetricException e) {
                throw new RuntimeException("Can't create the inputMetric object for the class " + CustomTestInputMetric.class);
            }
        }

        public boolean isCustomType() {
            return true;
        }
    }
}