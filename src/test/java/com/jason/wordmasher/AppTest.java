package com.jason.wordmasher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    private String[] args;
    private static final int CREATE_DUMMY_ARRAY_MAX = 500;
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

    /**
     * Asserts App.validateArgs returns false for an illegal number of string array elements.
     */
    public void testValidateArgs_invalidNumberOfArgs() {
        args = createDummyArray(0);
        assertEquals(false, App.validateArgs(args));

        args = createDummyArray(1);
        assertEquals(false, App.validateArgs(args));

        args = createDummyArray(2);
        assertEquals(false, App.validateArgs(args));

        args = createDummyArray(3);
        assertEquals(false, App.validateArgs(args));

        args = createDummyArray(5);
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
     * Asserts App.oneInNChance returns true 100% (or 1 in 1) of the time.
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
     * Asserts App.oneInNChance returns true 25% (or 1 in 4) of the time, plus or minus one percent.
     */
    public void testOneInNChance_oneInFour() {
        SummaryStatistics stats = getOneInNStats(100, 4);
        assert(!(stats.getMean() > (25 + 1)) && !(stats.getMean() < (25 - 1)));
    }

    /**
     * Asserts App.oneInNChance returns true 5% (or 1 in 20) of the time, plus or minus one percent.
     */
    public void testOneInNChance_oneInTwenty() {
        SummaryStatistics stats = getOneInNStats(100, 20);
        assert(!(stats.getMean() > (5 + 1)) && !(stats.getMean() < (5 - 1)));
    }

    /**
     * Asserts App.getNRandomStringsFromList returns a list of the correct length.
     */
    public void testGetNRandomStringsFromList_returnsCorrectListSize() {
        mockList = createDummyStringList(100);
        if(mockList == null) {
            fail("testGetNRandomStringsFromList was unable to generate a mockList.");
        }
        testRun = App.getNRandomAndDistinctStringsFromList(mockList, 10);
        assertEquals(testRun.size(), 10);
        testRun = App.getNRandomAndDistinctStringsFromList(mockList, 6);
        assertEquals(testRun.size(), 6);
    }

    /**
     * Asserts that App.getNRandomAndDistinctStringsFromList returns a list of randomized elements.
     */
    public void testGetNRandomStringsFromList_isRandom() {
        mockList = createDummyStringList(CREATE_DUMMY_ARRAY_MAX);
        List<String> mockListCopy = new ArrayList<>(mockList);
        testRun = App.getNRandomAndDistinctStringsFromList(mockList, CREATE_DUMMY_ARRAY_MAX);
        if(testRun == null) {
            fail("testRun cannot be null here.");
        }
        if(mockList.size() != testRun.size()) {
            fail("mockList cannot have a different size than testRun here.");
        }
        /*
        There is a very, VERY slim chance that the below statement could return true. In fact, the chances
        are 1 in the factorial of CREATE_DUMMY_ARRAY_MAX, which has lately been 500. (This would be a number with
        1,135 digits.) Email me if this ever happens so we can marvel at this statistical near-impossibility together.
        */
        assertFalse(mockListCopy.equals(testRun));
    }

    /**
     * Asserts that App.getNRandomAndDistinctStringsFromList returns a list of distinct elements.
     */
    public void testGetNRandomStringsFromList_isDistinct() {
        // Make mockList and testRun class member variables.
        mockList = createDummyStringList(25);
        assert(App.listContainsDistinctItems(App.getNRandomAndDistinctStringsFromList(mockList, 10)));
    }

    /**
     * Asserts App.listContainsDistinctItems correctly identifies a list of distinct items (of any type).
     */
    public void testListContainsDistinctItems() {
        assert(App.listContainsDistinctItems(createDummyStringList(10)));
    }

    //**************************//
    //***** HELPER METHODS *****//
    //**************************//

    /**
     * Utilizes createDummyArray to create a dummy string list of distinct numbered elements,
     * e.g. ["0", "1", "2", ...].
     *
     * @param n The list length
     * @return  The dummy string list
     */
    private List<String> createDummyStringList(int n) {
        // Prevent createDummyArray from throwing an illegal argument exception.
        if(n < 0 || n > CREATE_DUMMY_ARRAY_MAX) {
            return null;
        }
        List<String> list = new ArrayList<>();
        String[] array = createDummyArray(n);
        if(!Collections.addAll(list, array)) {
            return null;
        }
        return list;
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
    private String[] createDummyArray(int n) {
        if(n < 0 || n > CREATE_DUMMY_ARRAY_MAX) {
            throw new IllegalStateException("createDummyArgsArray called with illegal integer argument " + n + ".");
        }
        String[] args = new String[n];
        for(int i = 0; i < n; i++) {
            args[i] = Integer.toString(i);
        }
        return args;
    }

    /**
     * Helper method to generate a SummaryStatistics object for testOneInNChance_oneIn* unit tests.
     *
     * Creates (runs)^2 iterations of data for analysis.
     *
     * For example, a call to oneInNChance(4) has a 1/4 = 25% chance of returning true. So run that method
     * runs times (where 'runs' is the first method parameter), and each time record the true / false value
     * to a boolean list.
     *
     * Then step through every item of that boolean list and count each time a true value occurs. For a run of
     * 100, there should be roughly 25 true elements in that list, plus or minus some standard deviation.
     *
     * Do this 100 times for each 100 iterations for a total of 10k data points. Add all of these values to the
     * SummaryStatistics object for analysis upon return.
     *
     * @param runs   Number of runs
     * @param oneInN Chance, i.e. 1 in 4 = 25%.
     * @return       SummaryStatistics object
     */
    private SummaryStatistics getOneInNStats(int runs, int oneInN) {
        List<Boolean> bools = new ArrayList<>();
        SummaryStatistics stats = new SummaryStatistics();
        int count = 0;
        for(int i = 0; i < runs; i++) {
            bools.clear();
            for(int j = 0; j < runs; j++) {
                bools.add(App.oneInNChance(oneInN));
            }
            for(Boolean b : bools) {
                if(b) {
                    count++;
                }
            }
            stats.addValue((double)count);
            count = 0;
        }
        return stats;
    }
}

























