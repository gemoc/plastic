package fr.irisa.diverse.unittesting;

import org.jacoco.core.analysis.*;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

/**
 * The suite runner contains Junco coverage information and given a class, runs test so test covering the most
 * are executed first.
 */
public class SuiteRunner {

    private final Path srcFolder;

    private final Path testFolder;

    private final Path coverageInfoFolder;

    /**
     * Collection of test class files that covers a particular method
     */
    private HashMap<String, Collection<String>> classCoverage;

    private boolean initialized = false;

    /**
     * Run JUnit in all classes of a given directory
     * @param srcFolder Sources being tested
     * @param testFolder Path to the unit test classes
     * @param coverageInfoFolder Path to the Junco [https://github.com/marcelinorc/junco-provider.git]
     *                           coverage information (if any). Null otherwise.
     * @throws IOException
     */
    public SuiteRunner(Path srcFolder, Path testFolder, Path coverageInfoFolder) {
        this.srcFolder = srcFolder;
        this.testFolder = testFolder;
        this.coverageInfoFolder = coverageInfoFolder;
    }

    /**
     * Runs only the part of a unit test covering a particular clas
     */
    public void runForClass(String className) throws IOException {
        if ( classCoverage == null ) initCoverage(className, coverageInfoFolder);
        RunCaseFileVisitor runner = new RunCaseFileVisitor(testFolder);
        for ( String c : classCoverage.get(className) ) {
            runner.visitFile(Paths.get(c), null);
        }
    }

    /**
     * Gets all the classFiles covering a particular class
     * @param className Canonical name for which we want the classes covering
     * @param coveragePath Folder containing the coverage
     * @return A collection of paths to files
     */
    private void initCoverage(String className, Path coveragePath) throws IOException {

        classCoverage = new HashMap<>();

        for ( File f : coveragePath.toFile().listFiles()) {
            if ( f.getName().endsWith(".exec") ) {
                ExecFileLoader loader = new ExecFileLoader();
                loader.load(f);
                final CoverageBuilder coverageBuilder = new CoverageBuilder();
                final Analyzer analyzer = new Analyzer(loader.getExecutionDataStore(), coverageBuilder);
                analyzer.analyzeAll(coveragePath.toFile());
                IBundleCoverage bundle = coverageBuilder.getBundle(className);
                for (final IPackageCoverage p : bundle.getPackages()) {
                    for (final IClassCoverage c : p.getClasses()) {
                        Collection<String> paths;
                        if ( classCoverage.containsKey(c.getName()) ) paths = classCoverage.get(c.getName());
                        else {
                            paths = new ArrayList<>();
                            classCoverage.put(c.getName(), paths);
                        }
                        if ( !paths.contains(f.getAbsolutePath()) ) paths.contains(f.getAbsolutePath());
                    }
                }
            }
        }
    }


    /**
     * Runs the complete suite test
     * @throws IOException
     */
    public void run() throws IOException {
        //Validate they are dirs
        validate(srcFolder, testFolder, coverageInfoFolder);

        //Create a new class loader with class path containing the sources and tests
        URL[] urls = new URL[]{srcFolder.toUri().toURL(), testFolder.toUri().toURL()};
        URLClassLoader child = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(child);

        //Run the tests
        RunCaseFileVisitor r = new RunCaseFileVisitor(testFolder);
        Files.walkFileTree(testFolder, r);
        System.out.println("Total: " + r.getListener().getTotalCount());
        System.out.println("Failures: " + r.getListener().getFailuresCount());
    }

    /**
     *
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
}
