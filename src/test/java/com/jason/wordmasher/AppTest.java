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

    private static String[] args;
    private static final int CREATE_DUMMY_ARRAY_MAX = 2000;
    private static final int MAX_ENGLISH_WORDS_MOCK = 100;
    private static List<String> testRun;
    private static List<String> mockList;
    private static List<String> englishWordsMock;
    private static List<String> usedEnglishWordsMock;
    private static List<String> wordsToMash = populateWordsToMash();

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
            generatedInts.add(App.getRandomIntInInclusiveRange(minInclusive, maxInclusive));
        }
        assert(generatedInts.size() == 5);
    }

    /**
     * Asserts that App.getWordsToMash throws an exception for arg int too small.
     */
    public void testGetWordsToMash_throwsException_intTooSmall() {
        try {
            App.getWordsToMash(App.MIN_WORDS_TO_MASH - 1, englishWordsMock, usedEnglishWordsMock);
            fail("App.getWordsToMash should have thrown an exception here.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts that App.getWordsToMash throws an exception for arg int too large.
     */
    public void testGetWordsToMash_throwsException_intTooLarge() {
        try {
            App.getWordsToMash(App.MAX_WORDS_TO_MASH + 1, englishWordsMock, usedEnglishWordsMock);
            fail("App.getWordsToMash should have thrown an exception here.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts that App.getWordsToMash throws an exception when first arg is null.
     */
    public void testGetWordsToMash_throwsException_argNull_A() {
        try {
            App.getWordsToMash(App.MAX_WORDS_TO_MASH - 1, null, usedEnglishWordsMock);
            fail("App.getWordsToMash should have thrown an exception here.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts that App.getWordsToMash throws an exception when second arg is null.
     */
    public void testGetWordsToMash_throwsException_argNull_B() {
        try {
            App.getWordsToMash(App.MAX_WORDS_TO_MASH - 1, englishWordsMock, null);
            fail("App.getWordsToMash should have thrown an exception here.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts that App.getWordsToMash throws an exception when first arg is empty.
     */
    public void testGetWordsToMash_throwsException_argEmpty() {
        List<String> emptyList = new ArrayList<>();
        try {
            App.getWordsToMash(App.MAX_WORDS_TO_MASH - 1, emptyList, usedEnglishWordsMock);
            fail("App.getWordsToMash should have thrown an exception here.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.testGetWordsToMash correctly populates usedEnglishWords.
     */
    public void testGetWordsToMash_populatesUsedEnglishWords() {
        if(wordsToMash == null) {
            fail("App.getWordsToMash returned a null list.");
        } else {
            assert(wordsToMash.equals(usedEnglishWordsMock));
        }
    }

    /**
     * Asserts App.testGetWordsToMash generates a list of distinct words.
     */
    public void testGetWordsToMash_generatesDistictWords() {
        if(wordsToMash == null) {
            fail("App.getWordsToMash returned a null list.");
        } else {
            Set<String> wordsToMashSet = new HashSet<>(wordsToMash);
            assertEquals(wordsToMashSet.size(), App.MAX_WORDS_TO_MASH);
        }
    }

    /**
     * Asserts App.testGetWordsToMash generates a list of random words.
     */
    public void testGetWordsToMash_generatesRandomWords() {
        boolean matchFound = false;
        /*
        Playing with the law of large numbers here; chances of fluke failure are lowered if the number of elements
        inspected is halved. If it fails, just run it again. Should fail EXTREMELY rarely, when the first 5 elements of
        wordsToMash (size MAX_WORDS_TO_MASH / 2) are somehow the same as the first five elements of englishWordsMock
        (size MAX_ENGLISH_WORDS_MOCK).
        */
        for(int i = 0; i < (wordsToMash.size() / 2); i++) {
            if(englishWordsMock.get(i).equals(wordsToMash.get(i))) {
                matchFound = true;
            }
        }
        assertFalse(matchFound);
    }

    /**
     * Asserts App.makeSubword throws an exception when first arg is null.
     */
    public void testMakeSubword_throwsException_firstArgNull() {
        try {
            String test = App.makeSubword(null, 1);
            fail("App.makeSubword should have thrown an exception here.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.makeSubword throws an exception when first arg is too short.
     */
    public void testMakeSubword_throwsException_firstArgTooShort() {
        try {
            String firstArg = "a";
            String test = App.makeSubword(firstArg, 1);
            fail("App.makeSubword should have thrown an exception here.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.makeSubword throws an exception when first arg is too long.
     */
    public void testMakeSubword_throwsException_firstArgTooLong() {
        try {
            String firstArg = "aaaaaaaaaaa";
            String test = App.makeSubword(firstArg, 1);
            fail("App.makeSubword should have thrown an exception here.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.makeSubword throws an exception when second arg is not 1, 2, or 3.
     */
    public void testMakeSubword_throwsException_secondArgIllegalValue() {
        try {
            String firstArg = "just right";
            String test = App.makeSubword(firstArg, 0);
            fail("App.makeSubword should have thrown an exception here.");
        } catch (IllegalStateException e) {
            // Do nothing; test asserts exception is properly thrown.
        }
    }

    /**
     * Asserts App.makeSubword properly implements case 1. (See README.)
     */
    // App.makeSubword is still under construction
    public void testMakeSubword_case_1() {
        List<String> subWords = new ArrayList<>();
        String word = "Universe";
        for(int i = 0; i < 100; i++) {
            subWords.add(App.makeSubword(word, 1));
        }
        Set<String> subWordsSet = new HashSet<>(subWords);
        assertEquals(subWordsSet.size(), word.length());
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

    /**
     * @return a list of 100 distinct strings, e.g. "word_0", "word_1", "word_2", ...
     */
    private static List<String> populateEnglishWordsMock() {
        List<String> words = new ArrayList<>();
        for(int i = 0; i < MAX_ENGLISH_WORDS_MOCK; i++) {
            StringBuilder word = new StringBuilder("word_");
            words.add(word.append(Integer.toString(i)).toString());
        }
        return words;
    }

    private static List<String> populateWordsToMash() {
        englishWordsMock = populateEnglishWordsMock();
        usedEnglishWordsMock = new ArrayList<>();
        return App.getWordsToMash(App.MAX_WORDS_TO_MASH, englishWordsMock, usedEnglishWordsMock);
    }
}

























