package fr.irisa.diverse;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * Properties for each project under analysis.
 *
 * Created by marodrig on 11/05/2015.
 */
public class ProjectProperties extends Properties{

    /**
     * Default properties to obtain values of non-defined properties from
     */
    private final ProjectProperties defaultProperties;

    /**
     * Path to the compiled classes of the project
     */
    private String target = "";

    /**
     * Path to the compiled classes of the test of the project
     */
    private String testTarget = "";

    /**
     * Path to the coverage information of the project.
     *
     * A coverage information is a group of Jacoco .exec files each .exec file containing the coverage of
     * a single test case
     */
    private String coverageInfo = "";
    private String projectRoot;

    /**
     * Name of a method to analyze.
     *
     * If this property is empty the whole project is analyzed
     */
    private String methodName;

    public String getOutput() {
        return output;
    }

    /**
     * Output path where the transformed classes are going to be stored
     */
    private String output = "";

    public ProjectProperties() {
        this.defaultProperties = new ProjectProperties();
    }

        public ProjectProperties(ProjectProperties defaultProperties) {
        this.defaultProperties = defaultProperties;
    }

    /**
     * Read specific properties and assing them to fields for ease of manipulation
     */
    private void configure() {
        target = getProperty("target", defaultProperties.getTarget());
        testTarget = getProperty("test.target", defaultProperties.getTestTarget());
        coverageInfo = getProperty("coverage.info", defaultProperties.getCoverageInfo());
        output = getProperty("output", defaultProperties.getOutput());
        projectRoot = getProperty("project");
        methodName = getProperty("method");
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        super.load(reader);
        configure();
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        super.load(inStream);
        configure();
    }

    public String getTarget() {
        return getProjectRoot() + "/" + target;
    }

    public String getTestTarget() {
        return getProjectRoot() + "/" + testTarget;
    }

    public String getCoverageInfo() {
        return getProjectRoot() + "/" + coverageInfo;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public String getMethodName() {
        return methodName;
    }
}
