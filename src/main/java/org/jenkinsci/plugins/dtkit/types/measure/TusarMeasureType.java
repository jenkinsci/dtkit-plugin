package org.jenkinsci.plugins.dtkit.types.measure;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.MeasureTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.MeasureType;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.metrics.model.InputMetricException;
import com.thalesgroup.dtkit.metrics.model.InputMetricFactory;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class TusarMeasureType extends MeasureType {

    private static TusarMeasureTypeDescriptor DESCRIPTOR = new TusarMeasureTypeDescriptor();

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public TusarMeasureType(String pattern, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
    }

    public MeasureTypeDescriptor<? extends MeasureType> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static class TusarMeasureTypeDescriptor extends MeasureTypeDescriptor<TusarMeasureType> {

        public TusarMeasureTypeDescriptor() {
            super(TusarMeasureType.class, null);
        }

        @Override
        public String getId() {
            return this.getClass().getName();
        }

        @Override
        public InputMetric getInputMetric() {
            try {
                return InputMetricFactory.getInstance(TusarMeasureInputMetric.class);
            } catch (InputMetricException e) {
                throw new RuntimeException("Can't create the inputMetric object for the class " + TusarMeasureInputMetric.class);
            }
        }

    }
}