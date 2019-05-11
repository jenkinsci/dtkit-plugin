package org.jenkinsci.plugins.dtkit.types.coverage;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.jenkinsci.lib.dtkit.descriptor.CoverageTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.CoverageType;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.util.validator.ValidationService;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class TusarCoverageType extends CoverageType {

    private static TusaCoverageTypeDescriptor DESCRIPTOR = new TusaCoverageTypeDescriptor();

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public TusarCoverageType(String pattern, boolean faildedIfNotNew, boolean deleteOutputFiles) {
        super(pattern, faildedIfNotNew, deleteOutputFiles);
    }

    public CoverageTypeDescriptor<? extends CoverageType> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static class TusaCoverageTypeDescriptor extends CoverageTypeDescriptor<TusarCoverageType> {

        public TusaCoverageTypeDescriptor() {
            super(TusarCoverageType.class, null);
        }

        @Override
        public String getId() {
            return this.getClass().getName();
        }

        @Override
        public InputMetric getInputMetric() {
            //return InputMetricFactory.getInstance(TusarCoverageInputMetric.class);
            InputMetric inputMetric = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(ValidationService.class);
                }
            }).getInstance(TusarCoverageInputMetric.class);
            return inputMetric;
        }
    }
}