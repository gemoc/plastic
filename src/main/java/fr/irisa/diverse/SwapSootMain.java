package fr.irisa.diverse;

import fr.inria.diversify.buildSystem.maven.MavenDependencyResolver;
import fr.irisa.diverse.transformations.SwitchableCounter;
import fr.irisa.diverse.transformations.SwitchableTransformation;
import fr.irisa.diverse.unittesting.SuiteRunner;
import org.apache.log4j.PropertyConfigurator;
import soot.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by marodrig on 16/04/2015.
 */
public class SwapSootMain {

    public static void main(String[] args) throws Exception {

        PropertyConfigurator.configure("src/log4j.properties");

        //Global configuration.
        //The global configuration resides in allprojects.properties file. There you may find 'default' properties
        //that can be overrided later for all projects
        ProjectProperties defaultProperties = new ProjectProperties();
        defaultProperties.load(Main.class.getClassLoader().getResourceAsStream("projects/allprojects.properties"));

        //Collect the path of all project property info files
        Collection<String> projects = new ArrayList<String>();
        for (String pName : defaultProperties.getProperty("projects").split(",")) {
            projects.add("projects/" + pName.trim() + ".properties");
        }

        //Process all projects
        for (String project : projects) {

            //Load local configuration for the project being processed
            ProjectProperties p = new ProjectProperties(defaultProperties);
            p.load(SwapSootMain.class.getClassLoader().getResourceAsStream(project));
            //String projectOutput = p.getProperty("output"); //+ File.separator + target;
            //String projectTarget = p.getProperty("project");
            //testTarget  = p.getProperty("test.target", testTarget);
            //coverageInfo = defaultProperties.getProperty("coverage.info", coverageInfo);
            //String method = p.getProperty("method", "");

            //Add to the class path all the dependencies of the POM.xml for the project
            //Needed to run test and to reduce the amount of phantom classes
            MavenDependencyResolver resolver = new MavenDependencyResolver();
            resolver.DependencyResolver(p.getProjectRoot() + "/pom.xml");
            String projectOutput = p.getOutput() + File.separator + p.getTarget();

            URLClassLoader child = new URLClassLoader(new URL[] {
                    Paths.get(projectOutput).toUri().toURL(),
                    Paths.get(p.getTestTarget()).toUri().toURL(),
            }, Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(child);

            //Run the test suite first so we can know what test cases fails without manipulation and exclude them
            //SuiteRunner runner = new SuiteRunner()

            runSootEachFile(p.getMethodName(), p.getTarget(), p.getTestTarget(), p.getCoverageInfo(), projectOutput);

            /*
            SwitchableCounter counter = new SwitchableCounter();
            runSootDirectorty(counter, projectTarget, projectOutput, method);
            runTest(projectOutput, p.getProperty("output", output) + File.separator + testTarget);
            writeCountResults(counter, output, project, p);
            */
        }
    }

    /**
     * Runs soot file by file
     */
    private static void runSootEachFile(String method, String projectTarget, String testTarget,
                                        String coveragePath, String projectOutput) throws IOException {
        SootFileVisitor visitor = new SootFileVisitor(method, projectTarget, testTarget, coveragePath, projectOutput);
        Files.walkFileTree(Paths.get(projectTarget), visitor);
    }

/*
    private static void writeResume(String projectOutput, String fileName,
                                    HashMap<String, SwitchableCounter.Counter> counters) throws IOException {
        File fbody = new File(projectOutput + File.separator + "resume" + File.separator + fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fbody));

        String SEP = ";";
        for (Map.Entry<String, SwitchableCounter.Counter> e : counters.entrySet()) {
            writer.write(e.getKey() + SEP +
                    (e.getValue().getTotal() - e.getValue().getSwitchable()) + SEP +
                    e.getValue().getSwitchable() + SEP +
                    ((float) e.getValue().getSwitchable() / (float) e.getValue().getTotal()) * 100.0f);
            writer.write("\n");
        }
        writer.close();
    }
*/


    /**
     *
     * Runs the whole soot directory
     */
    /*
    private static void runSootDirectorty(SwitchableCounter counter,
                                          String projectTarget, String projectOutput, String method) {

        SwitchableTransformation switchable = new SwitchableTransformation();

        int k = method.lastIndexOf('.');
        final String finalMethod = method.isEmpty() ? "" : method.substring(k + 1, method.length());
        final String methodClass = method.isEmpty() ? "" : method.substring(0, k);
        PackManager.v().getPack("jtp").add(
                new Transform("jtp.myTransform", new BodyTransformer() {
                    protected void internalTransform(Body body, String phase, Map options) {
                        if (finalMethod.isEmpty() ||
                                (body.getMethod().getName().equals(finalMethod) &&
                                        body.getMethod().getDeclaringClass().getName().equals(methodClass))) {
                            //new GraphVisPrettyPrint2(body).printControlFlow();
                            switchable.execute(body);
                            //new GraphVisPrettyPrint2(body).printControlFlow();
                        }
                        counter.execute(body);
                    }
                }));

        //Params oh yes, is so beautiful (https://ssebuild.cased.de/nightly/soot/doc/soot_options.htm)
        String[] sootParams = new String[]{
                //Input options https://ssebuild.cased.de/nightly/soot/doc/soot_options.htm#section_2
                "-cp", ".", "-pp", "-process-dir", projectTarget,
                //Output options (https://ssebuild.cased.de/nightly/soot/doc/soot_options.htm#section_3)
                "-d", projectOutput, "-allow-phantom-refs"};//, "-f", "J"};

        //Verbose output
        soot.Main.main(sootParams);

        //reset the counter
        G.reset();
    }

    private static void runTest(String projectOutput, String testProject) throws IOException {
        new SuiteRunner(Paths.get(projectOutput), Paths.get(testProject), null).run();
    }
  */
}
