package fr.irisa.diverse.unittesting;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Created by marodrig on 07/05/2015.
 */
public class ExecutionListener extends RunListener
{
    private int totalCount = 0;

    private int failuresCount = 0;

    /**
     * Called before any tests have been run.
     * */
    public void testRunStarted(Description description)	throws java.lang.Exception
    {
        //System.out.println("Number of testcases to execute : " + description.testCount());
    }

    /**
     *  Called when all tests have finished
     * */
    public void testRunFinished(Result result) throws java.lang.Exception
    {
        //System.out.println("Number of testcases executed : " + result.getRunCount());
    }

    /**
     *  Called when an atomic test is about to be started.
     * */
    public void testStarted(Description description) throws java.lang.Exception
    {
        //System.out.println("Starting execution of test case : "+ description.getMethodName());
    }

    /**
     *  Called when an atomic test has finished, whether the test succeeds or fails.
     * */
    public void testFinished(Description description) throws java.lang.Exception
    {
        if (!description.toString().contains("initializationError")) {
            totalCount++;
        }

        //System.out.println("Finished execution of test case : "+ description.getMethodName());
    }

    /**
     *  Called when an atomic test fails.
     * */
    public void testFailure(Failure failure) throws java.lang.Exception
    {
        if (!failure.getDescription().toString().contains("initializationError")) {
            failuresCount++;
            System.out.println("Test failed: " + failure.getTestHeader() + " : "  + failure.getMessage());
            if ( failure.getTrace() != null ) {
                System.out.println(failure.getTrace());
            }
        }
    }

    /**
     *  Called when a test will not be run, generally because a test method is annotated with Ignore.
     * */
    public void testIgnored(Description description) throws java.lang.Exception
    {
        System.out.println("Execution of test case ignored : "+ description.getMethodName());
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getFailuresCount() {
        return failuresCount;
    }
}
