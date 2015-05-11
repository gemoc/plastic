package fr.irisa.diverse;

import fr.irisa.diverse.transformations.SwitchableTransformation;
import fr.irisa.diverse.unittesting.SuiteRunner;
import soot.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

/**
 * Created by marodrig on 06/05/2015.
 */
public class SootFileVisitor extends SimpleFileVisitor<Path> {

    private final Path targetFolder;

    private final String projectOutput;

    private final SuiteRunner runner;

    private final String method;

    public SootFileVisitor(String method, String projectTarget,
                           String testTarget, String coverageInfo, String projectOutput) {
        this.method = method;
        this.targetFolder = Paths.get(projectTarget);
        this.projectOutput = projectOutput;
        this.runner = new SuiteRunner(Paths.get(projectTarget), Paths.get(testTarget), Paths.get(coverageInfo));
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        try {
            if ( !file.toString().endsWith(".class") ) return FileVisitResult.CONTINUE;

            SwitchableTransformation switchable = new SwitchableTransformation();
            String className = targetFolder.relativize(file).toString().replace(File.separator, ".");
            className = className.substring(0, className.lastIndexOf(".class"));
            PackManager.v().getPack("jtp").add(
                    new Transform("jtp.myTransform", new BodyTransformer() {
                        protected void internalTransform(Body body, String phase, Map options) {
                            /*
                            if (finalMethod.isEmpty() ||
                                    (body.getMethod().getName().equals(finalMethod) &&
                                            body.getMethod().getDeclaringClass().getName().equals(methodClass))) {*/
                                //new GraphVisPrettyPrint2(body).printControlFlow();
                                switchable.execute(body);
                                //new GraphVisPrettyPrint2(body).printControlFlow();
                            //}
                            //counter.execute(body);
                        }
                    }));

            //Params oh yes, is so beautiful (https://ssebuild.cased.de/nightly/soot/doc/soot_options.htm)
            String[] sootParams = new String[] {
                    //Input options https://ssebuild.cased.de/nightly/soot/doc/soot_options.htm#section_2
                    "-cp", ".;" + targetFolder, "-pp",  className,
                    //Output options (https://ssebuild.cased.de/nightly/soot/doc/soot_options.htm#section_3)
                    "-d", projectOutput, "-allow-phantom-refs"};//, "-f", "J"};
            //Verbose output
            soot.Main.main(sootParams);

            //Run the tests of this class only
            runner.runForClass(className);

            //reset the counter
            G.reset();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return FileVisitResult.TERMINATE;
        }
        return FileVisitResult.CONTINUE;
    }

}
