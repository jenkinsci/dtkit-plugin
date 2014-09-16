package org.jenkinsci.plugins.dtkit.types.test;

import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricException;
import org.jenkinsci.lib.dtkit.model.InputMetricFactory;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class TusarTestType extends TestType {

    private static TusarTestTypeDescriptor DESCRIPTOR = new TusarTestTypeDescriptor();

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public TusarTestType(String pattern, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
    }

    public TestTypeDescriptor<? extends TestType> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static class TusarTestTypeDescriptor extends TestTypeDescriptor<TusarTestType> {

        public TusarTestTypeDescriptor() {
            super(TusarTestType.class, null);
        }

        @Override
        public String getId() {
            return this.getClass().getName();
        }

        @Override
        public InputMetric getInputMetric() {
            try {
                return InputMetricFactory.getInstance(TusarTestInputMetric.class);
            } catch (InputMetricException e) {
                throw new RuntimeException("Can't create the inputMetric object for the class " + TusarTestInputMetric.class);
            }
        }
    }
}