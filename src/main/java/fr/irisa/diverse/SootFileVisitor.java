package fr.irisa.diverse;

import com.sun.deploy.panel.ExceptionListDialog;
import fr.irisa.diverse.printing.GraphVisPrettyPrint;
import fr.irisa.diverse.transformations.SwitchableTransformation;
import fr.irisa.diverse.unittesting.SuiteRunner;
import soot.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by marodrig on 06/05/2015.
 */
public class SootFileVisitor extends SimpleFileVisitor<Path> {

    private final Path targetFolder;

    private final String projectOutput;

    private final SuiteRunner runner;

    private final String coverageInfo;

    private String methodClass;

    private String finalMethod;

    public String getMethodClass() {
        return methodClass;
    }

    public void setMethodClass(String methodClass) {
        this.methodClass = methodClass;
    }


    public String getFinalMethod() {
        return finalMethod;
    }

    public void setFinalMethod(String finalMethod) {
        this.finalMethod = finalMethod;
    }


    public SootFileVisitor(String method, String projectTarget,
                           String testTarget, String coverageInfo, String projectOutput) {
        if (method == null || method.isEmpty()) {
            this.methodClass = "";
            this.finalMethod = "";
        } else {
            this.methodClass = method.substring(0, method.lastIndexOf("."));
            this.finalMethod = method.substring(method.lastIndexOf(".") + 1, method.length());
        }
        this.targetFolder = Paths.get(projectTarget);
        this.projectOutput = projectOutput;
        this.coverageInfo = coverageInfo;
        this.runner = new SuiteRunner(Paths.get(projectTarget), Paths.get(testTarget), Paths.get(coverageInfo));
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        //Don't visit files which are not classes
        if (!file.toString().endsWith(".class")) return FileVisitResult.CONTINUE;

        //Find the class name
        String className = targetFolder.relativize(file).toString().replace(File.separator, ".");
        className = className.substring(0, className.lastIndexOf(".class"));

        //Visit only the requested class
        if (!(methodClass == null || methodClass.isEmpty() || methodClass.equals(className)))
            return FileVisitResult.CONTINUE;

        try {
            //If the class has no coverage, is pointless to analyze it
            if (!runner.hasCoverage(className)) {
                System.out.println(className + "-> NOT COVERED");
                return FileVisitResult.CONTINUE;
            }

            //Transform class by class
            SwitchableTransformation switchable = new SwitchableTransformation();
            HashMap<String, String[]> graphs = new HashMap<>();
            PackManager.v().getPack("jtp").add(
                    new Transform("jtp.myTransform", new BodyTransformer() {
                        protected void internalTransform(Body body, String phase, Map options) {
                            if (finalMethod.isEmpty() ||
                                    (body.getMethod().getName().equals(finalMethod) &&
                                            body.getMethod().getDeclaringClass().getName().equals(methodClass))) {

                                String[] gs = new String[2];
                                gs[0] = new GraphVisPrettyPrint(body).printControlFlow();
                                //System.out.println(gs[0]);
                                switchable.execute(body);
                                if (switchable.getNumberOfTransformation() > 0) {
                                    //Only register if there was changes
                                    gs[1] = new GraphVisPrettyPrint(body).printControlFlow();
                                    graphs.put(body.getMethod().getName(), gs);
                                }
                            }
                            //counter.execute(body);
                        }
                    }));

            //Params oh yes, is so beautiful (https://ssebuild.cased.de/nightly/soot/doc/soot_options.htm)
            String[] sootParams = new String[]{
                    //Input options https://ssebuild.cased.de/nightly/soot/doc/soot_options.htm#section_2
                    "-cp", ".;" + targetFolder, "-pp", className,
                    //Output options (https://ssebuild.cased.de/nightly/soot/doc/soot_options.htm#section_3)
                    "-d", projectOutput, "-allow-phantom-refs"};//, "-f", "J"};
            //Verbose output
            soot.Main.main(sootParams);

            if (graphs.size() > 0) {
                //If the graph.size > means that we could analyze at least one method

                Path coveragePath = Paths.get(coverageInfo);
                if (coveragePath.toFile().exists()) runner.runForClass(className); //Run the tests of this class only
                else runner.run(); //No coverage info, run all tests

                //reset the counter
                G.reset();

                //Stop at the first sign of trouble
                if (!runner.errorsFound()) return FileVisitResult.CONTINUE;
                else {
                    for (String[] s : graphs.values()) {
                        System.out.println(s[0]);
                        System.out.println(s[1]);
                    }
                    return FileVisitResult.TERMINATE;
                }
            } else {
                G.reset();
                return FileVisitResult.CONTINUE;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return FileVisitResult.TERMINATE;
        } finally {
            //Erase the transformed file
            String s = targetFolder.toString() + File.separator + targetFolder.relativize(file).toString();
            Files.copy(Paths.get(s), file);
        }
    }


}
