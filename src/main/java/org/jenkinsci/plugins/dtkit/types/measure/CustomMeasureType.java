package org.jenkinsci.plugins.dtkit.types.measure;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.MeasureTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.MeasureType;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.metrics.model.InputMetricException;
import com.thalesgroup.dtkit.metrics.model.InputMetricFactory;
import hudson.Extension;
import org.jenkinsci.plugins.dtkit.types.CustomType;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class CustomMeasureType extends MeasureType implements CustomType {

    private static CustomMeasureInputMetricDescriptor DESCRIPTOR = new CustomMeasureInputMetricDescriptor();

    private String customXSL;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public CustomMeasureType(String pattern, String customXSL, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
        this.customXSL = customXSL;
    }

    public MeasureTypeDescriptor<? extends MeasureType> getDescriptor() {
        return DESCRIPTOR;
    }

    @SuppressWarnings("unused")
    @Override
    public String getCustomXSL() {
        return customXSL;
    }

    @Extension
    public static class CustomMeasureInputMetricDescriptor extends MeasureTypeDescriptor<CustomMeasureType> {

        public CustomMeasureInputMetricDescriptor() {
            super(CustomMeasureType.class, null);
        }

        @Override
        public String getId() {
            return this.getClass().getName();
        }

        @Override
        public InputMetric getInputMetric() {
            try {
                return InputMetricFactory.getInstance(CustomMeasureInputMetric.class);
            } catch (InputMetricException e) {
                throw new RuntimeException("Can't create the inputMetric object for the class " + CustomMeasureInputMetric.class);
            }
        }

        public boolean isCustomType() {
            return true;
        }
    }
}