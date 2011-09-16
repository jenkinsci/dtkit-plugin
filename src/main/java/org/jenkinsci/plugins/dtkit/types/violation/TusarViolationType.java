package org.jenkinsci.plugins.dtkit.types.violation;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.ViolationsTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.ViolationsType;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.metrics.model.InputMetricException;
import com.thalesgroup.dtkit.metrics.model.InputMetricFactory;
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