package fr.irisa.diverse.unittesting;

import org.jacoco.core.analysis.*;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;

/**
 * The suite runner contains Junco coverage information and given a class, runs test so test covering the most
 * are executed first.
 */
public class SuiteRunner {

    /**
     * Classes under test folder
     */
    private final Path targetFolder;

    /**
     *  Test cases folder
     */
    private final Path testFolder;

    /**
     * Coverage information folder
     */
    private final Path coverageInfoFolder;

    /**
     * Collection of test class files that covers a particular method
     */
    private HashMap<String, Collection<String>> classCoverage;

    private boolean errorsFound;

    /**
     * Run JUnit in all classes of a given directory
     *
     * @param srcFolder          Sources being tested
     * @param testFolder         Path to the unit test classes
     * @param coverageInfoFolder Path to the Junco [https://github.com/marcelinorc/junco-provider.git]
     *                           coverage information (if any). Null otherwise.
     * @throws IOException
     */
    public SuiteRunner(Path srcFolder, Path testFolder, Path coverageInfoFolder) {
        this.targetFolder = srcFolder;
        this.testFolder = testFolder;
        this.coverageInfoFolder = coverageInfoFolder;
    }

    /**
     * Runs only the part of a unit test covering a particular class
     */
    public void runForClass(String className) throws IOException {
        errorsFound = false;
        initCoverage(coverageInfoFolder);
        RunCaseFileVisitor runner = new RunCaseFileVisitor(testFolder);
        if (classCoverage.containsKey(className)) {
            for (String c : classCoverage.get(className)) {
                System.out.println("Running case: " + c);
                FileVisitResult a = runner.visitFile(Paths.get(c), null);
                if ( a.equals(FileVisitResult.TERMINATE) || runner.getListener().getFailuresCount() > 1 ) {
                    errorsFound = true;
                    System.out.println(className + "TEST FAILED. TERMINATING");
                    return;
                }
            }
            System.out.println("***********************************");
            System.out.println("***********************************");
            System.out.println("** " + className + ". OK!!. Continue****");
            System.out.println("***********************************");
            System.out.println("***********************************");
        } else System.out.println(className + ". CLASS NOT COVERED");
    }

    /**
     * Gets all the classFiles covering a particular class
     *
     * @param coveragePath Folder containing the coverage
     * @return A collection of paths to files
     */
    private void initCoverage(Path coveragePath) throws IOException {

        if (classCoverage == null) classCoverage = new HashMap<>();
        else return;

        for (File f : coveragePath.toFile().listFiles()) {
            if (f.getName().endsWith(".exec")) {

                String testClassName = f.getName().substring(0, f.getName().lastIndexOf(".exec"));

                ExecFileLoader loader = new ExecFileLoader();
                loader.load(f);
                final CoverageBuilder coverageBuilder = new CoverageBuilder();
                final Analyzer analyzer = new Analyzer(loader.getExecutionDataStore(), coverageBuilder);
                analyzer.analyzeAll(targetFolder.toFile());

                IBundleCoverage bundle = coverageBuilder.getBundle(testClassName);

                //org.easymock.tests.ArgumentToStringTest
                for (final IPackageCoverage p : bundle.getPackages()) {
                    for (final IClassCoverage c : p.getClasses()) {
                        if (c.getLineCounter().getCoveredCount() > 0) {
                            Collection<String> paths;
                            String className = c.getName().replace('/', '.');
                            if (classCoverage.containsKey(className)) paths = classCoverage.get(className);
                            else {
                                paths = new HashSet<>();
                                classCoverage.put(className, paths);
                            }
                            if (!paths.contains(f.getAbsolutePath())) {
                                String testClassPath = testFolder + File.separator + f.getName().replace('.', '/');
                                testClassPath = testClassPath.replace("/exec", ".class");
                                paths.add(testClassPath);
                            }
                        }
                    }
                }
            }
        }
        //System.out.println("Finding coverage of " + className + ". DONE");
    }


    /**
     * Runs the complete suite test
     *
     * @throws IOException
     */
    public void run() throws IOException {
        //Validate they are dirs
        validate(targetFolder, testFolder, coverageInfoFolder);

        //Create a new class loader with class path containing the sources and tests
        URL[] urls = new URL[]{targetFolder.toUri().toURL(), testFolder.toUri().toURL()};
        URLClassLoader child = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(child);

        //Run the tests
        RunCaseFileVisitor r = new RunCaseFileVisitor(testFolder);
        Files.walkFileTree(testFolder, r);
        System.out.println("Total: " + r.getListener().getTotalCount());
        System.out.println("Failures: " + r.getListener().getFailuresCount());
    }

    /**
     * @param paths
     */
    private static void validate(Path... paths) {
        for (Path path : paths) {
            Objects.requireNonNull(path);
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException(String.format("%s is not a directory", path.toString()));
            }
        }
    }

    public boolean errorsFound() {
        return errorsFound;
    }

    public boolean hasCoverage(String className) {

        errorsFound = false;
        try {
            initCoverage(coverageInfoFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  (classCoverage.containsKey(className));
    }
}
