package org.jenkinsci.plugins.dtkit;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.ParameterValue;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.jenkinsci.plugins.dtkit.service.DTKitBuilderConversionService;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderLog;
import org.jenkinsci.plugins.dtkit.service.DTKitBuilderValidationService;
import org.jenkinsci.plugins.dtkit.service.DTKitReportProcessingService;
import org.jenkinsci.plugins.dtkit.transformer.DTKitBuilderToolInfo;
import org.jenkinsci.plugins.dtkit.transformer.DTKitBuilderTransformer;
import org.kohsuke.stapler.StaplerRequest;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.CoverageTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.MeasureTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.ViolationsTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.CoverageType;
import com.thalesgroup.dtkit.metrics.hudson.api.type.MeasureType;
import com.thalesgroup.dtkit.metrics.hudson.api.type.MetricsType;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.dtkit.metrics.hudson.api.type.ViolationsType;

/**
 * @author Gregory Boissinot
 */
public class DTKitBuilder extends Builder {

    private final String tusarRootFolder;
    private final String generatedFolder;
    private final String generatedTests;
    private final String generatedCoverage;
    private final String generatedMeasures;
    private final String generatedViolations;

    
    private TestType[] tests;
    private CoverageType[] coverages;
    private ViolationsType[] violations;
    private MeasureType[] measures;

    public DTKitBuilder(TestType[] tests,
                        CoverageType[] coverages,
                        ViolationsType[] violations,
                        MeasureType[] measures,
                        String tusarRootFolder
                        ){
        this.tests = tests;
        this.coverages = coverages;
        this.violations = violations;
        this.measures = measures;
        
        this.tusarRootFolder = tusarRootFolder;
        
        if (this.tusarRootFolder.equals("")){
            this.generatedFolder = "generatedDTKITFiles";
        } else {
            if (new File(tusarRootFolder).isAbsolute()){
                this.generatedFolder = "generatedDTKITFiles";
            } else {
                this.generatedFolder = tusarRootFolder;
            }
        }
        this.generatedTests = generatedFolder + "/TESTS";
        this.generatedCoverage = generatedFolder + "/COVERAGE";
        this.generatedMeasures = generatedFolder + "/MEASURES";
        this.generatedViolations = generatedFolder + "/VIOLATIONS";
    }
    
    public String getTusarRootFolder(){
        if (this.tusarRootFolder == null){
            return "";
        }
        return tusarRootFolder;
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


    public void setTests(TestType[] tests) {
        this.tests = tests;
    }

    public void setCoverages(CoverageType[] coverages) {
        this.coverages = coverages;
    }

    public void setViolations(ViolationsType[] violations) {
        this.violations = violations;
    }

    public void setMeasures(MeasureType[] measures) {
        this.measures = measures;
    }
    
    private String getGeneratedTests() {
    	if (this.tusarRootFolder == null){
    		return generatedFolder + "/TESTS";
        }
		return generatedTests;
	}

	private String getGeneratedCoverage() {
		if (this.tusarRootFolder == null){
    		return generatedFolder + "/COVERAGE";
        }
		return generatedCoverage;
	}

	private String getGeneratedMeasures() {
		if (this.tusarRootFolder == null){
    		return generatedFolder + "/MEASURES";
        }
		return generatedMeasures;
	}

	private String getGeneratedViolations() {
		if (this.tusarRootFolder == null){
    		return generatedFolder + "/VIOLATIONS";
        }
		return generatedViolations;
	}

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {

        try {
        
            final StringBuffer sb = new StringBuffer();

            final DTKitBuilderLog log = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(BuildListener.class).toInstance(listener);
                }
            }).getInstance(DTKitBuilderLog.class);

            log.info("Starting converting.");

            DTKitReportProcessingService processingService = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(BuildListener.class).toInstance(listener);
                }
            }).getInstance(DTKitReportProcessingService.class);


            boolean isInvoked = false;
            
            if (new File(getTusarRootFolder()).isAbsolute()){
                DTKitBuilderLog.error("Tusar root folder musn't be absolute, result are generated in default folder: " +
                        "[workspace]/generatedDTKITFiles.");
            }

            // Apply conversion for all tests tools
            if (tests.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), getGeneratedTests());
                outputFileParent.mkdirs();
                for (TestType testsType : tests) {
                    log.info("Processing " + testsType.getDescriptor().getDisplayName());
                    if (!processingService.isEmptyPattern(testsType.getPattern())) {
                        boolean result = processInputMetricType(build, listener, testsType, outputFileParent);
                        if (result) {
                            isInvoked = true;
                        }
                    }
                }
                sb.append(";").append(getGeneratedTests());
            }
            if (coverages.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), getGeneratedCoverage());
                outputFileParent.mkdirs();
                for (CoverageType coverageType : coverages) {
                    log.info("Processing " + coverageType.getDescriptor().getDisplayName());
                    if (!processingService.isEmptyPattern(coverageType.getPattern())) {
                        boolean result = processInputMetricType(build, listener, coverageType, outputFileParent);
                        if (result) {
                            isInvoked = true;
                        }
                    }
                }
                sb.append(";").append(getGeneratedCoverage());
            }
            if (violations.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), getGeneratedViolations());
                outputFileParent.mkdirs();
                for (ViolationsType violationsType : violations) {
                    log.info("Processing " + violationsType.getDescriptor().getDisplayName());
                    if (!processingService.isEmptyPattern(violationsType.getPattern())) {
                        boolean result = processInputMetricType(build, listener, violationsType, outputFileParent);
                        if (result) {
                            isInvoked = true;
                        }
                    }
                }
                sb.append(";").append(getGeneratedViolations());
            }
            if (measures.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), getGeneratedMeasures());
                outputFileParent.mkdirs();
                for (MeasureType measureType : measures) {
                    log.info("Processing " + measureType.getDescriptor().getDisplayName());
                    if (!processingService.isEmptyPattern(measureType.getPattern())) {
                        boolean result = processInputMetricType(build, listener, measureType, outputFileParent);
                        if (result) {
                            isInvoked = true;
                        }
                    }
                }
                sb.append(";").append(getGeneratedMeasures());
            }


            // Remove the first character
            sb.delete(0, 1);

            boolean isPathPresent = false;
            
            for( Action a : build.getActions()){
                if(a instanceof ParametersAction){
                    StringParameterValue parameter = (StringParameterValue) ((ParametersAction) a).getParameter("sonar.tusar.reportsPaths");
                    if(null != parameter){
                        build.getActions().remove(a);
                        List<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
                        parameterValues.add(new StringParameterValue("sonar.language", "tusar"));
                        parameterValues.add(new StringParameterValue("sonar.tusar.reportsPaths", parameter.value + ";" + sb.toString()));
                        build.addAction(new ParametersAction(parameterValues));
                        isPathPresent = true;
                    }
                }
            }

            if(false == isPathPresent) {
                List<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
                parameterValues.add(new StringParameterValue("sonar.language", "tusar"));
                parameterValues.add(new StringParameterValue("sonar.tusar.reportsPaths", sb.toString()));
                build.addAction(new ParametersAction(parameterValues));
            }
            
            if (!isInvoked) {
                log.error("Any files are correct. Fail build.");
                build.setResult(Result.FAILURE);
                return false;
            }

            return true;
        } catch (Throwable e) {
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    private boolean processInputMetricType(final AbstractBuild<?, ?> build, final BuildListener listener, MetricsType metricsType, FilePath outputFileParent) throws IOException, InterruptedException {

        //Retrieves the pattern
        String newExpandedPattern = metricsType.getPattern();
        newExpandedPattern = newExpandedPattern.replaceAll("[\t\r\n]+", " ");
        newExpandedPattern = Util.replaceMacro(newExpandedPattern, build.getEnvironment(listener));

        //Build a new build info
        final DTKitBuilderToolInfo toolInfo = new DTKitBuilderToolInfo(metricsType, new File(outputFileParent.toURI()), newExpandedPattern, build.getTimeInMillis());

        // Archiving tool reports into JUnit files
        DTKitBuilderTransformer dtkitBuilderTransformer = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(listener);
                bind(DTKitBuilderToolInfo.class).toInstance(toolInfo);
                bind(DTKitBuilderValidationService.class).in(Singleton.class);
                bind(DTKitBuilderConversionService.class).in(Singleton.class);
                bind(DTKitBuilderLog.class).in(Singleton.class);
                bind(DTKitReportProcessingService.class).in(Singleton.class);
            }
        }).getInstance(DTKitBuilderTransformer.class);

        return build.getWorkspace().act(dtkitBuilderTransformer);
    }

    @Extension
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
            return "DTKit Conversion";
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
                    measures.toArray(new MeasureType[measures.size()]),
                            formData.getString("tusarRootFolder")
            );
        }
    }

}
