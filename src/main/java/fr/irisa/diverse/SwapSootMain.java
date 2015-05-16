package fr.irisa.diverse;

import fr.inria.diversify.buildSystem.maven.MavenDependencyResolver;
import fr.irisa.diverse.transformations.SwitchableCounter;
import fr.irisa.diverse.transformations.SwitchableTransformation;
import fr.irisa.diverse.unittesting.SuiteRunner;
import org.apache.log4j.PropertyConfigurator;
import soot.*;
import soot.options.Options;

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

    public static void configure(String classpath, String output) {
        Options.v().set_verbose(true);
        Options.v().set_process_dir(Arrays.asList(classpath));
        Options.v().set_keep_line_number(true);
        Options.v().set_src_prec(Options.src_prec_class);

        String cp = System.getProperty("java.class.path");//.replace("\\", "/");
        Options.v().set_soot_classpath(cp);
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_dir(output);
/*
        PhaseOptions.v().setPhaseOption("bb", "off");
        PhaseOptions.v().setPhaseOption("tag.ln", "on");
        PhaseOptions.v().setPhaseOption("jj.a", "on");
        PhaseOptions.v().setPhaseOption("jj.ule", "on");
*/
        Options.v().set_whole_program(false);
    }

    /*
    public static void runFromAPI(Properties p) {
        configure(p.getProperty("cp"), p.getProperty("output"));
        Scene.v().loadNecessaryClasses();
        SootClass c = Scene.v().loadClassAndSupport("fr.irisa.diverse.testclases.TestClass1");

        c.setApplicationClass();
        PackManager.v().runPacks();

        // Retrieve the method and its body
        SootMethod m = c.getMethodByName("foo");

        Body b = m.retrieveActiveBody();
        // Instruments bytecode
        new GuaranteedDefs(new ExceptionalUnitGraph(b));
    }*/

    public static void main(String[] args) throws Exception {

        PropertyConfigurator.configure("src/log4j.properties");

        //I hate property files....
        //Global configuration
        Properties props = new Properties();
        props.load(Main.class.getClassLoader().getResourceAsStream("projects/allprojects.properties"));
        String target = props.getProperty("target");
        String testTarget = props.getProperty("test.target");
        String coverageInfo = props.getProperty("coverage.info", "");

        //Collect all project property info files
        Collection<String> projects = new ArrayList<String>();
        for (String pName : props.getProperty("projects").split(",")) {
            projects.add("projects/" + pName.trim() + ".properties");
        }

        //Process all projects
        for (String project : projects) {
            //Load local configuration for the project
            Properties p = new Properties();
            p.load(SwapSootMain.class.getClassLoader().getResourceAsStream(project));
            String projectOutput = p.getProperty("output"); //+ File.separator + target;
            String projectTarget = p.getProperty("project");
            testTarget  = p.getProperty("test.target", testTarget);
            coverageInfo = props.getProperty("coverage.info", coverageInfo);
            String method = p.getProperty("method", "");

            //Needed to run test and to reduce the amount of phantom classes
            MavenDependencyResolver resolver = new MavenDependencyResolver();
            resolver.DependencyResolver(projectTarget + "/pom.xml");

            //Resolve Paths
            testTarget = projectOutput + File.separator + testTarget;
            if ( !coverageInfo.isEmpty() && coverageInfo != null )
                coverageInfo = projectTarget + File.separator + coverageInfo;
            if (!projectTarget.endsWith(File.pathSeparator)) projectTarget += File.separator;
            projectTarget += props.getProperty("target", target);

            projectOutput = p.getProperty("output") + File.separator + target;

            URLClassLoader child = new URLClassLoader(new URL[] {
                    Paths.get(projectOutput).toUri().toURL(),
                    Paths.get(testTarget).toUri().toURL(),
            }, Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(child);

            //Run the test suite first so we can know what test cases fails without manipulation and exclude them
            //SuiteRunner runner = new SuiteRunner()

            runSootEachFile(method, projectTarget, testTarget, coverageInfo, projectOutput);

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

    private static void cleanUp(String projectOutput) {
        //Clean up the output dir
        /*
        File outputFolder = new File(projectOutput);
        for (File f : outputFolder.listFiles()) {
            FileUtils.deleteDirectory(f);
        }*/
    }

    /**
     * Runs the whole soot directory
     */
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

    private static void writeCountResults(SwitchableCounter counter, String projectOutput, String project,
                                          Properties p) throws IOException {
        String projectName = project.substring(project.indexOf("/") + 1, project.lastIndexOf(".properties"));
        writeResume(projectOutput, projectName + "-method.txt", counter.getMethodCounters());
        writeResume(projectOutput, projectName + "-class.txt", counter.getClassCounters());
        HashMap<String, SwitchableCounter.Counter> projectCounter = new HashMap<>();
        projectCounter.put(projectName, counter.getProjectCounter());
        writeResume(projectOutput, projectName + "-project.txt", projectCounter);

        System.out.println(p.getProperty("description"));
        System.out.println("All Jimple: " + counter.getProjectCounter().getTotal());
        System.out.println("All Jimple Switchable: " + counter.getProjectCounter().getSwitchable());
    }

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
}
