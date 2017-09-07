package com.jason.wordmasher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    private String[] args;
    
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

    /**
     * Asserts App.validateArgs returns false for an illegal number of string array elements.
     */
    public void testValidateArgs_invalidNumberOfArgs() {
        args = createDummyArgsArray(0);
        assertEquals(false, App.validateArgs(args));

        args = createDummyArgsArray(1);
        assertEquals(false, App.validateArgs(args));

        args = createDummyArgsArray(2);
        assertEquals(false, App.validateArgs(args));

        args = createDummyArgsArray(3);
        assertEquals(false, App.validateArgs(args));

        args = createDummyArgsArray(5);
        assertEquals(false, App.validateArgs(args));
    }

    /**
     * Asserts App.validateArgs returns false if arguments array contains nonexistent files.
     */
    public void testValidateArgs_nonExistentFiles() {
        args = createArgsArray("a.txt", "b.txt", "c.txt", "4");
        assertEquals(false, App.validateArgs(args));
    }

    /**
     * Asserts App.validateArgs returns false if arguments array contains empty files.
     */
    public void testValidateArgs_emptyArgFiles() {
        try {
            File tempFile = File.createTempFile("empty", ".txt");
            tempFile.deleteOnExit();
            if(!tempFile.canRead()) {
                fail("Unable to read temporary test file 'empty*.txt'.");
            }
            String[] args = createArgsArray("empty.txt", "empty.txt", "output.txt", "4");
            assertEquals(false, App.validateArgs(args));

        } catch (IOException e) {
            fail("Unable to create empty text file. " + e.getMessage());
        }
    }

    /**
     * Asserts App.validateArgs returns false if arguments array contains an un-parsable integer string.
     */
    public void testValidateArgs_unparsableNumberOfFrankenwords() {
        args = createArgsArray("english_words.txt", "special_characters.txt", "output.txt", "x");
        assertEquals(false, App.validateArgs(args));
    }

    /**
     * Asserts App.validateArgs returns false if arguments array contains an integer string with a value of < 1.
     */
    public void testValidateArgs_negativeNumberOfFrankenwords() {
        args = createArgsArray("english_words.txt", "special_characters.txt", "output.txt", "-6");
        assertEquals(false, App.validateArgs(args));
    }

    /**
     * Asserts App.validateArgs returns false if arguments array contains an integer string with a value of > 1000.
     */
    public void testValidateArgs_illegalNumberOfFrankenwords_1001() {
        args = createArgsArray("english_words.txt", "special_characters.txt", "output.txt", "1001");
        assertEquals(false, App.validateArgs(args));
    }

    /**
     * Asserts App.validateArgs returns true if arguments array contains an integer string
     * with a value of > 0 and < 1001.
     */
    public void testValidateArgs_allParsableArguments() {
        args = createArgsArray("english_words.txt", "special_characters.txt", "output.txt", "1000");
        assertEquals(true, App.validateArgs(args));
    }

    /**
     * Assert App.oneInNChance returns true 100% (or 1 in 1) of the time.
     */
    public void testOneInNChance_oneInOne() {
        List<Boolean> bools = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            bools.add(App.oneInNChance(1));
        }
        boolean falseFound = false;
        for(Boolean b : bools) {
            if(!b) {
                falseFound = true;
            }
        }
        assertEquals(falseFound, false);
    }

    /**
     * Assert App.oneInNChance returns true 25% (or 1 in 4) of the time, plus or minus one percent.
     */
    public void testOneInNChance_oneInFour() {
        List<Boolean> bools = new ArrayList<>();
        SummaryStatistics stats = new SummaryStatistics();
        int count = 0;
        for(int i = 0; i < 100; i++) {
            bools.clear();
            for(int j = 0; j < 100; j++) {
                bools.add(App.oneInNChance(4));
            }
            for(Boolean b : bools) {
                if(b) {
                    count++;
                }
            }
            stats.addValue((double)count);
            count = 0;
        }
        assert(!(stats.getMean() > (25 + 1)) && !(stats.getMean() < (25 - 1)));
    }

    /**
     * Assert App.oneInNChance returns true 5% (or 1 in 20) of the time, plus or minus one percent.
     */
    public void testOneInNChance_oneInTwenty() {
        List<Boolean> bools = new ArrayList<>();
        SummaryStatistics stats = new SummaryStatistics();
        int count = 0;
        for(int i = 0; i < 100; i++) {
            bools.clear();
            for(int j = 0; j < 100; j++) {
                bools.add(App.oneInNChance(20));
            }
            for(Boolean b : bools) {
                if(b) {
                    count++;
                }
            }
            stats.addValue((double)count);
            count = 0;
        }
        assert(!(stats.getMean() > (5 + 1)) && !(stats.getMean() < (5 - 1)));
    }

    /**
     * Helper method to create a string array of arguments for testing.
     *
     * @param arg_0 Argument 0
     * @param arg_1 Argument 1
     * @param arg_2 Argument 2
     * @param arg_3 Argument 3
     *
     * @return A string array of arguments.
     */
    private String[] createArgsArray(String arg_0, String arg_1, String arg_2, String arg_3) {
        if(arg_0 == null || arg_1 == null || arg_2 == null || arg_3 == null) {
            throw new IllegalStateException("createArgsArray called with one or more null arguments.");
        }
        String[] args = new String[4];
        args[0] = arg_0;
        args[1] = arg_1;
        args[2] = arg_2;
        args[3] = arg_3;
        return args;
    }

    /**
     * Helper method to create dummy string array for testing. If n is "3", return array is ["0", "1", "2"].
     *
     * @param n Number of dummy arguments to create
     * @return String array of dummy arguments.
     */
    private String[] createDummyArgsArray(int n) {
        if(n < 0 || n > 100) {
            throw new IllegalStateException("createDummyArgsArray called with illegal integer argument " + n + ".");
        }
        String[] args = new String[n];
        for(int i = 0; i < n; i++) {
            args[i] = Integer.toString(i);
        }
        return args;
    }
}

























