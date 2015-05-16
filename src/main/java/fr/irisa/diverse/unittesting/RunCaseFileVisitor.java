package fr.irisa.diverse.unittesting;

import org.junit.runner.JUnitCore;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by marodrig on 07/05/2015.
 */
public class RunCaseFileVisitor extends SimpleFileVisitor<Path> {

    /**
     * Path where the tests are
     */
    private final Path testFolder;

    public Path getTestFolder() {
        return testFolder;
    }

    /**
     * Listener to output the result of the tests
     */
    private ExecutionListener listener = new ExecutionListener();

    public ExecutionListener getListener() {
        return listener;
    }

    public RunCaseFileVisitor(Path testFolder) {
        this.testFolder = testFolder;
    }

    public void runCase(String testCase) throws ClassNotFoundException {
        Class cls = Thread.currentThread().getContextClassLoader().loadClass(testCase);
        runCase(cls);
    }

    public void runCase(Class testCase) {
        JUnitCore runner = new JUnitCore();
        runner.addListener(listener);
        runner.run(testCase);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try {
            String className = testFolder.relativize(file).toString().replace(File.separator, ".");
            className = className.substring(0, className.lastIndexOf(".class"));
            runCase(className);
        } catch (ClassNotFoundException e) {
            return FileVisitResult.TERMINATE;
        }
        return FileVisitResult.CONTINUE;
    }
}
