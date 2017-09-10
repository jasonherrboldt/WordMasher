package com.jason.wordmasher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    private String[] args;
    private static final int CREATE_DUMMY_ARRAY_MAX = 2000;
    private List<String> testRun;
    private List<String> mockList;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testHello(){
        assert(true);
    }
}

























