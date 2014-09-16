package org.jenkinsci.plugins.dtkit.types.violation;

import org.jenkinsci.lib.dtkit.descriptor.ViolationsTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.ViolationsType;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricException;
import org.jenkinsci.lib.dtkit.model.InputMetricFactory;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class TusarViolationType extends ViolationsType {

    private static TusarViolationsTypeDescriptor DESCRIPTOR = new TusarViolationsTypeDescriptor();

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public TusarViolationType(String pattern, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
    }

    public ViolationsTypeDescriptor<? extends ViolationsType> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static class TusarViolationsTypeDescriptor extends ViolationsTypeDescriptor<TusarViolationType> {

        public TusarViolationsTypeDescriptor() {
            super(TusarViolationType.class, null);
        }

        @Override
        public String getId() {
            return this.getClass().getName();
        }

        @Override
        public InputMetric getInputMetric() {
            try {
                return InputMetricFactory.getInstance(TusarViolationInputMetric.class);
            } catch (InputMetricException e) {
                throw new RuntimeException("Can't create the inputMetric object for the class " + CustomViolationInputMetric.class);
            }
        }

    }
}