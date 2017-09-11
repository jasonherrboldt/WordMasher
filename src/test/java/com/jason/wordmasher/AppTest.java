package com.jason.wordmasher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    /**
     * Asserts App.parseArgs throws an exception for an illegal number of string array elements.
     */
    public void testParseArgs_invalidNumberOfArgs() {

        // Test for 0, 1, 2, and 3 dummy args.
        for(int i = 0; i < 4; i++) {
            args = createDummyArray(i);
            try {
                App.parseArgs(args);
                fail("parseArgs should have thrown an illegal argument exception.");
            } catch (IllegalArgumentException e) {
                // Do nothing; test asserts exception is properly thrown.
            }
        }

        // No test is needed for the correct number of args (4); happy path is validated in another test.

        args = createDummyArray(5);
        try {
            App.parseArgs(args);
            fail("parseArgs should have thrown an illegal argument exception.");
        } catch (IllegalArgumentException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.parseArts throws an exception for program arguments with length of > 50.
     */
    public void testParseArgs_argsTooLong() {
        args = createArgsArray("a.txt", "b.txt", "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii.txt", "4");
        try {
            App.parseArgs(args);
            fail("parseArgs should have thrown an illegal argument exception.");
        } catch (IllegalArgumentException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.parseArgs throws an exception if arguments array contains nonexistent files.
     */
    public void testParseArgs_nonExistentFiles() {
        args = createArgsArray("a.txt", "b.txt", "c.txt", "4");
        try {
            App.parseArgs(args);
            fail("parseArgs should have thrown an illegal argument exception.");
        } catch (IllegalArgumentException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.parseArgs throws an exception if arguments array contains empty files.
     */
    public void testParseArgs_emptyArgFiles() {
        String emptyFileName = "empty.txt";
        List<String> emptyList = new ArrayList<>();
        File tempFile = App.createFileWithStringList(emptyList, emptyFileName);
        if(!tempFile.canRead()) {
            fail("Unable to read temporary test file " + tempFile.getName());
        }
        String[] args = createArgsArray(emptyFileName, "charList.txt", "output.txt", "4");
        try {
            App.parseArgs(args);
            fail("parseArgs should have thrown an illegal argument exception.");
        } catch (IllegalArgumentException e) {
            // Do nothing; test asserts exception is properly thrown.
            if(!tempFile.delete()) {
                fail("testParseArgs_emptyArgFiles was unable to delete " + tempFile.getName());
            }
        }
    }

    /**
     * Asserts App.parseArgs throws an exception if arguments array contains an un-parsable integer string.
     */
    public void testParseArgs_unparsableNumberOfFrankenwords() {
        args = createArgsArray("english_words.txt", "special_characters.txt", "output.txt", "x");
        try {
            App.parseArgs(args);
            fail("parseArgs should have thrown an illegal argument exception.");
        } catch (IllegalArgumentException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.validateArgs throws an exception if 4th program arguments is < 1.
     */
    public void testParseArgs_negativeNumberOfFrankenwords() {
        args = createArgsArray("english_words.txt", "special_characters.txt", "output.txt", "-1");
        try {
            App.parseArgs(args);
            fail("parseArgs should have thrown an illegal argument exception.");
        } catch (IllegalArgumentException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.validateArgs throws an exception if 4th program arguments is > 1000.
     */
    public void testParseArgs_tooManyFrankenwords() {
        args = createArgsArray("english_words.txt", "special_characters.txt", "output.txt", "1001");
        try {
            App.parseArgs(args);
            fail("parseArgs should have thrown an illegal argument exception.");
        } catch (IllegalArgumentException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.validateArgs does not throw an exception when given valid program arguments.
     */
    public void testParseArgs_allParsableArguments() {
        args = createArgsArray("english_words.txt", "special_characters.txt", "output.txt", "500");
        try {
            App.parseArgs(args);
        } catch (IllegalArgumentException e) {
            fail("parseArgs should not have thrown an illegal argument exception.");
        }
    }

    /**
     * Asserts App.readFileIntoMemory throws an exception if it receives a non-existent file as an argument.
     */
    public void testReadFileIntoMemory_nonExistentFile() {
        try {
            File fakeFile = new File("fake.txt");
            App.readFileIntoMemory(fakeFile);
            fail("App.readFileIntoMemory should have thrown an illegal argument exception.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.readFileIntoMemory throws an exception if it receives an empty file as an argument.
     */
    public void testReadFileIntoMemory_emptyFile() {
        try {
            File tempFile = File.createTempFile("empty", ".txt");
            tempFile.deleteOnExit();
            try {
                App.readFileIntoMemory(tempFile);
                fail("App.readFileIntoMemory should have thrown an illegal argument exception.");
            } catch (IllegalStateException e) {
                // Do nothing; test asserts exception is properly thrown.
            }
        } catch (IOException e) {
            fail("Unable to create empty text file. " + e.getMessage());
        }
    }

    /**
     * Asserts App.readFileIntoMemory returns a list that reflects the file data.
     */
    public void testReadFileIntoMemory_correctData() {
        List<String> mockList = createDummyStringList(20);
        if (mockList == null || mockList.isEmpty()) {
            fail("testReadFileIntoMemory_correctData was unable to populate mockList.");
        } else {
            File mockFile = App.createFileWithStringList(mockList, "mock.txt");
            List<String> methodCall = App.readFileIntoMemory(mockFile);
            assertEquals(methodCall, mockList);
            if (!mockFile.delete()) {
                App.print("testReadFileIntoMemory_correctData was unable to delete " + mockFile.getName());
            }
        }
    }

    /**
     * Asserts App.getRandomIntInRange generates all random integers in a small inclusive range.
     */
    public void testGetRandomIntInRange_correctRange() {
        Set<Integer> generatedInts = new HashSet<>();
        int minInclusive = 1;
        int maxInclusive = 5;
        for(int i = 0; i < 50; i++) {
            generatedInts.add(App.getRandomIntInRange(minInclusive, maxInclusive));
        }
        assert(generatedInts.size() == 5);
    }



    //**************************//
    //***** HELPER METHODS *****//
    //**************************//



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
}

























