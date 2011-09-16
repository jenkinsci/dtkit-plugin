package org.jenkinsci.plugins.dtkit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.CoverageTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.MeasureTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.ViolationsTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.*;
import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderConversionService;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderLog;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderValidationService;
import org.jenkinsci.plugins.dtkit.service.DTKitReportProcessingService;
import org.jenkinsci.plugins.dtkit.transformer.DTKitBuilderToolInfo;
import org.jenkinsci.plugins.dtkit.transformer.DTKitBuilderTransformer;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilder extends Builder {

    private transient final String generatedFolder = "generatedTUSARFiles";
    private transient final String generatedTests = generatedFolder + "/TESTS";
    private transient final String generatedCoverage = generatedFolder + "/COVERAGE";
    private transient final String generatedMeasures = generatedFolder + "/MEASURES";
    private transient final String generatedViolations = generatedFolder + "/VIOLATIONS";

    private TestType[] tests;
    private CoverageType[] coverages;
    private ViolationsType[] violations;
    private MeasureType[] measures;

    public DTKitBuilder(TestType[] tests,
                        CoverageType[] coverages,
                        ViolationsType[] violations,
                        MeasureType[] measures) {
        this.tests = tests;
        this.coverages = coverages;
        this.violations = violations;
        this.measures = measures;
    }

    @SuppressWarnings("unused")
    public TestType[] getTests() {
        return tests;
    }

    @SuppressWarnings("unused")
    public CoverageType[] getCoverages() {
        return coverages;
    }

    @SuppressWarnings("unused")
    public ViolationsType[] getViolations() {
        return violations;
    }

    @SuppressWarnings("unused")
    public MeasureType[] getMeasures() {
        return measures;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }


    private boolean processInputMetricType(final AbstractBuild<?, ?> build, final BuildListener listener, MetricsType metricsType, FilePath outputFileParent) throws IOException, InterruptedException {

        final DTKitBuilderLog DTKitBuilderLog = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(listener);
            }
        }).getInstance(DTKitBuilderLog.class);

        //Retrieves the pattern
        String newExpandedPattern = metricsType.getPattern();
        newExpandedPattern = newExpandedPattern.replaceAll("[\t\r\n]+", " ");
        newExpandedPattern = Util.replaceMacro(newExpandedPattern, build.getEnvironment(listener));

        //Build a new build info
        final DTKitBuilderToolInfo DTKitBuilderToolInfo = new DTKitBuilderToolInfo(metricsType, new File(outputFileParent.toURI()), newExpandedPattern, build.getTimeInMillis());

        // Archiving tool reports into JUnit files
        DTKitBuilderTransformer DTKitBuilderTransformer = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(listener);
                bind(DTKitBuilderToolInfo.class).toInstance(DTKitBuilderToolInfo);
                bind(DTKitBuilderValidationService.class).in(Singleton.class);
                bind(DTKitBuilderConversionService.class).in(Singleton.class);
                bind(DTKitBuilderLog.class).in(Singleton.class);
                bind(DTKitReportProcessingService.class).in(Singleton.class);
            }
        }).getInstance(DTKitBuilderTransformer.class);

        boolean resultTransformation = build.getWorkspace().act(DTKitBuilderTransformer);
        if (!resultTransformation) {
            build.setResult(Result.FAILURE);
            DTKitBuilderLog.info("Stopping recording.");
            return false;
        }

        return true;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {

        try {
            final StringBuffer sb = new StringBuffer();

            final DTKitBuilderLog dtkitbuilderlog = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(BuildListener.class).toInstance(listener);
                }
            }).getInstance(DTKitBuilderLog.class);
            dtkitbuilderlog.info("Starting converting.");


            DTKitReportProcessingService processingService = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(BuildListener.class).toInstance(listener);
                }
            }).getInstance(DTKitReportProcessingService.class);


            boolean isInvoked = false;

            // Apply conversion for all tests tools
            if (tests.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), generatedTests);
                outputFileParent.mkdirs();
                for (TestType testsType : tests) {
                    dtkitbuilderlog.info("Processing " + testsType.getDescriptor().getDisplayName());
                    if (!processingService.isEmptyPattern(testsType.getPattern())) {
                        boolean result = processInputMetricType(build, listener, testsType, outputFileParent);
                        if (result) {
                            isInvoked = true;
                        }
                    }
                }
                sb.append(";").append(generatedTests);
            }
            if (coverages.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), generatedCoverage);
                outputFileParent.mkdirs();
                for (CoverageType coverageType : coverages) {
                    dtkitbuilderlog.info("Processing " + coverageType.getDescriptor().getDisplayName());
                    if (!processingService.isEmptyPattern(coverageType.getPattern())) {
                        boolean result = processInputMetricType(build, listener, coverageType, outputFileParent);
                        if (result) {
                            isInvoked = true;
                        }
                    }
                }
                sb.append(";").append(generatedCoverage);
            }
            if (violations.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), generatedViolations);
                outputFileParent.mkdirs();
                for (ViolationsType violationsType : violations) {
                    dtkitbuilderlog.info("Processing " + violationsType.getDescriptor().getDisplayName());
                    if (!processingService.isEmptyPattern(violationsType.getPattern())) {
                        boolean result = processInputMetricType(build, listener, violationsType, outputFileParent);
                        if (result) {
                            isInvoked = true;
                        }
                    }
                }
                sb.append(";").append(generatedViolations);
            }
            if (measures.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), generatedMeasures);
                outputFileParent.mkdirs();
                for (MeasureType measureType : measures) {
                    dtkitbuilderlog.info("Processing " + measureType.getDescriptor().getDisplayName());
                    if (!processingService.isEmptyPattern(measureType.getPattern())) {
                        boolean result = processInputMetricType(build, listener, measureType, outputFileParent);
                        if (result) {
                            isInvoked = true;
                        }
                    }
                }
                sb.append(";").append(generatedMeasures);
            }


            // Remove the first character
            sb.delete(0, 1);

            List<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
            parameterValues.add(new StringParameterValue("sonar.language", "tusar"));
            parameterValues.add(new StringParameterValue("sonar.tusar.reportsPaths", sb.toString()));
            build.addAction(new ParametersAction(parameterValues));

            return true;
        } catch (Throwable e) {
            build.setResult(Result.FAILURE);
            return false;
        }
    }


    @Extension(ordinal = 1)
    @SuppressWarnings("unused")
    public static final class TusarNotifierDescriptor extends BuildStepDescriptor<Builder> {


        public TusarNotifierDescriptor() {
            super(DTKitBuilder.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getHelpFile() {
            return "/plugin/dtkit/help.html";
        }


        @Override
        public String getDisplayName() {
            return "DTKit Steps";
        }

        public DescriptorExtensionList<TestType, TestTypeDescriptor<?>> getListTestDescriptors() {
            return TestTypeDescriptor.all();
        }

        public DescriptorExtensionList<ViolationsType, ViolationsTypeDescriptor<?>> getListViolationDescriptors() {
            return ViolationsTypeDescriptor.all();
        }

        public DescriptorExtensionList<MeasureType, MeasureTypeDescriptor<?>> getListMeasureDescriptors() {
            return MeasureTypeDescriptor.all();
        }

        public DescriptorExtensionList<CoverageType, CoverageTypeDescriptor<?>> getListCoverageDescriptors() {
            return CoverageTypeDescriptor.all();
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData)
                throws FormException {

            List<TestType> tests = Descriptor.newInstancesFromHeteroList(req, formData, "tests", getListTestDescriptors());
            List<CoverageType> coverages = Descriptor.newInstancesFromHeteroList(req, formData, "coverages", getListCoverageDescriptors());
            List<ViolationsType> violations = Descriptor.newInstancesFromHeteroList(req, formData, "violations", getListViolationDescriptors());
            List<MeasureType> measures = Descriptor.newInstancesFromHeteroList(req, formData, "measures", getListMeasureDescriptors());

            return new DTKitBuilder(tests.toArray(new TestType[tests.size()]),
                    coverages.toArray(new CoverageType[coverages.size()]),
                    violations.toArray(new ViolationsType[violations.size()]),
                    measures.toArray(new MeasureType[measures.size()])
            );
        }
    }

}
