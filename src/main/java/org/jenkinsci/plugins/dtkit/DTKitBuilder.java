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
import java.util.ListIterator;

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

    private final String rootFolder;
    private transient final String generatedFolder;
    private transient final String generatedTests;
    private transient final String generatedCoverage;
    private transient final String generatedMeasures;
    private transient final String generatedViolations;

    
    private TestType[] tests;
    private CoverageType[] coverages;
    private ViolationsType[] violations;
    private MeasureType[] measures;
    
    private static final String DEFAULT_ROOT_PATH = "generatedDTKITFiles";
    private static final String DEFAULT_TEST_PATH = "/TESTS";
    private static final String DEFAULT_COVERAGE_PATH = "/COVERAGE";
    private static final String DEFAULT_MEASURES_PATH = "/MEASURES";
    private static final String DEFAULT_VIOLATIONS_PATH = "/VIOLATIONS";

    /**
     * Represents checkbox value in plugin configuration page
     */
    private boolean selectedForSuppress;

    public DTKitBuilder(TestType[] tests,
                        CoverageType[] coverages,
                        ViolationsType[] violations,
                        MeasureType[] measures,
                        String rootFolderPath,
                        boolean selectedForSuppress
                        ){
        this.tests = tests;
        this.coverages = coverages;
        this.violations = violations;
        this.measures = measures;

        this.setSelectedForSuppress(selectedForSuppress);

        if (rootFolderPath == null || rootFolderPath.trim().isEmpty()){
        	this.rootFolder = DEFAULT_ROOT_PATH;
            this.generatedFolder = DEFAULT_ROOT_PATH;
        } else {
        	this.rootFolder = rootFolderPath;
            if (new File(rootFolderPath).isAbsolute()){
                this.generatedFolder = DEFAULT_ROOT_PATH;
            } else {
                this.generatedFolder = rootFolderPath;
            }
        }
        
        this.generatedTests = generatedFolder + DEFAULT_TEST_PATH;
        this.generatedCoverage = generatedFolder + DEFAULT_COVERAGE_PATH;
        this.generatedMeasures = generatedFolder + DEFAULT_MEASURES_PATH;
        this.generatedViolations = generatedFolder + DEFAULT_VIOLATIONS_PATH;
        
    }
    
    public String getRootFolder() {
		return rootFolder;
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

    /**
     * Used with DTKit plugin configuration.
     * When selectedForSuppress, this option allows to suppress old build files prior to execute a new build
     */
    public boolean isSelectedForSuppress() {
        return selectedForSuppress;
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

    public void setSelectedForSuppress(boolean selectedForSuppress) {
        this.selectedForSuppress = selectedForSuppress;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {

        try {
        	//Because of old jobs configuration, we have to check that those values are not null
        	String rootFolder = this.rootFolder == null?(DEFAULT_ROOT_PATH):this.rootFolder;
            String generatedTests = this.generatedTests == null?(DEFAULT_ROOT_PATH+DEFAULT_TEST_PATH):this.generatedTests;
            String generatedCoverage = this.generatedCoverage == null?(DEFAULT_ROOT_PATH+DEFAULT_COVERAGE_PATH):this.generatedCoverage;
            String generatedMeasures = this.generatedMeasures == null?(DEFAULT_ROOT_PATH+DEFAULT_MEASURES_PATH):this.generatedMeasures;
            String generatedViolations = this.generatedViolations == null?(DEFAULT_ROOT_PATH+DEFAULT_VIOLATIONS_PATH):this.generatedViolations;

            final StringBuffer sb = new StringBuffer();

            final DTKitBuilderLog log = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(BuildListener.class).toInstance(listener);
                }
            }).getInstance(DTKitBuilderLog.class);

            // cause of eventual presence of last build generated files
            // check for cleaning old build generated files
            if (this.isSelectedForSuppress()) {
                log.info("Checking for old build generated files...");
                // and suppress them
                if (this.cleanOldBuildFiles(new FilePath(build.getWorkspace(), rootFolder))) {
                    log.info("Old build generated files suppressed...");
                }
            }

            log.info("Starting converting.");

            DTKitReportProcessingService processingService = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(BuildListener.class).toInstance(listener);
                }
            }).getInstance(DTKitReportProcessingService.class);


            boolean isInvoked = false;
            
            if (new File(rootFolder).isAbsolute()){
            	log.error("Tusar root folder musn't be absolute, result are generated in default folder: " +
                        "[workspace]/generatedDTKITFiles.");
            }

            // Apply conversion for all tests tools
            if (tests.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), generatedTests);
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
                sb.append(";").append(generatedTests);
            }
            if (coverages.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), generatedCoverage);
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
                sb.append(";").append(generatedCoverage);
            }
            if (violations.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), generatedViolations);
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
                sb.append(";").append(generatedViolations);
            }
            if (measures.length != 0) {
                FilePath outputFileParent = new FilePath(build.getWorkspace(), generatedMeasures);
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
                sb.append(";").append(generatedMeasures);
            }


            // Remove the first character
            sb.delete(0, 1);
            
            constructTUSARReportPathForSonar(build, sb.toString());
            
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
    /**
     * Since it is possible to add several DTKit build steps, it is also possible to have several root folder.
     * This method will correctly update the sonar.tusar.reportsPaths property when a new dtkit build step is met.
     * @param build
     */
    private void constructTUSARReportPathForSonar(final AbstractBuild<?, ?> build, String reportPaths){
    	boolean isTusarPropertyAlreadyAdded = false;
        
        for( Action a : build.getActions()){
            if(a instanceof ParametersAction){
                StringParameterValue parameter = (StringParameterValue) ((ParametersAction) a).getParameter("sonar.tusar.reportsPaths");
                //If there is more than one dtkit build step, we have to update the sonar.tusar.reportsPaths property
                if(null != parameter){
                    build.getActions().remove(a);
                    List<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
                    parameterValues.add(new StringParameterValue("sonar.language", "tusar"));
                    parameterValues.add(new StringParameterValue("sonar.tusar.reportsPaths", parameter.value + ";" + reportPaths));
                    build.addAction(new ParametersAction(parameterValues));
                    isTusarPropertyAlreadyAdded = true;
                }
            }
        }

        //The first dtkit met build step is treated here
        if(false == isTusarPropertyAlreadyAdded) {
            List<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
            parameterValues.add(new StringParameterValue("sonar.language", "tusar"));
            parameterValues.add(new StringParameterValue("sonar.tusar.reportsPaths", reportPaths));
            build.addAction(new ParametersAction(parameterValues));
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


    /**
     * This method performs a recursive walk into output directory tree to delete all old build files if present
     * @param rootFile    the root folder that contains all output sub-folders and files
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean cleanOldBuildFiles(FilePath rootFile) throws IOException, InterruptedException {
        boolean resultValue = false;
        List<FilePath> dirList = rootFile.list();
        ListIterator dirListIterator = dirList.listIterator();
        FilePath tempDir;

        // check for root folder presence to avoid error if first time build...
        if (rootFile.exists()) {
            // check for output folders
            if (!dirList.isEmpty()) {
                while (dirListIterator.hasNext()) {
                    tempDir =  (FilePath) dirListIterator.next();
                    tempDir.deleteRecursive();
                    if (!resultValue && !tempDir.exists()) {
                        resultValue = true;
                    }
                }
            }
        }
        //
        return resultValue;
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
                    formData.getString("rootFolder"),
                    formData.getBoolean("selectedForSuppress")
            );
        }
    }

}
