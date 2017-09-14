package com.jason.wordmasher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.*;
import java.util.*;

/**
 * Unit test suite for WordMasher App.
 */
public class AppTest extends TestCase {

    private static String[] args;
    private static final int CREATE_DUMMY_ARRAY_MAX = 2000;
    private static final int MAX_ENGLISH_WORDS_MOCK = 100;
    private static List<String> mockList;
    private static List<String> englishWordsMock;
    private static List<String> usedEnglishWordsMock;
    private static List<String> wordsToMash = populateWordsToMash();
    private static char[] specialCharactersMock = populateSpecialCharactersMock();

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
        File tempFile = createFileWithStringList(emptyList, emptyFileName);
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
     * Asserts App.readFileIntoListOfStrings returns a list that reflects the file data.
     */
    public void testReadFileIntoListOfStrings_correctData() {
        mockList = createDummyStringList(20);
        if (mockList == null || mockList.isEmpty()) {
            fail("testReadFileIntoListOfStrings_correctData was unable to populate mockList.");
        } else {
            File mockFile = createFileWithStringList(mockList, "mock.txt");
            List<String> methodCall = App.readFileIntoListOfStrings(mockFile);
            assertEquals(methodCall, mockList);
            if (!mockFile.delete()) {
                App.print("testReadFileIntoListOfStrings_correctData was unable to delete " + mockFile.getName());
            }
        }
    }

    /**
     * Asserts App.readFileIntoCharArray returns a char array that reflects the file data.
     */
    public void testReadFileIntoCharArray() {
        mockList = createDummyStringList(10); // Will be strings of length 1.
        if (mockList == null || mockList.isEmpty()) {
            fail("testReadFileIntoCharArray was unable to populate mockList.");
        } else {
            File mockFile = createFileWithStringList(mockList, "mock.txt");
            char[] mockListCharArray = convertStringListToCharArray(mockList);
            char[] methodCall = App.readFileIntoCharArray(mockFile);
            if(mockListCharArray.length != methodCall.length) {
                if (!mockFile.delete()) {
                    App.print("testReadFileIntoListOfStrings_correctData was unable to delete " + mockFile.getName());
                }
                fail("mockListCharArray.length != methodCall.length. Cannot proceed. mockListCharArray.length = "
                        + mockListCharArray.length + ", methodCall.length = " + methodCall.length);
            }
            boolean mismatchFound = false;
            for(int i = 0; i < mockListCharArray.length; i++) {
                if(mockListCharArray[i] != methodCall[i]) {
                    mismatchFound = true;
                }
            }
            if (!mockFile.delete()) {
                App.print("testReadFileIntoListOfStrings_correctData was unable to delete " + mockFile.getName());
            }
            assertFalse(mismatchFound);
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
        if(usedEnglishWordsMock != null) {
            usedEnglishWordsMock.clear();
        }
        List<String> thisWordsToMash = App.getWordsToMash(App.MAX_WORDS_TO_MASH, englishWordsMock, usedEnglishWordsMock);
        List<String> englishWordMockSub = new ArrayList<>(englishWordsMock.subList(0, 5));
        List<String> thisWordsToMashSub = new ArrayList<>(thisWordsToMash.subList(0, 5));
        assertFalse(englishWordMockSub.equals(thisWordsToMashSub));
    }

    /**
     * Asserts App.makeSubword properly implements case 1. (See README.)
     */
    public void testMakeSubword_case_1() {
        List<String> subWords = new ArrayList<>();
        String word = "UNIVERSE";
        for(int i = 0; i < 100; i++) {
            subWords.add(App.makeSubword(word, 1));
        }
        // Copy the list elements to a set to count all generated subwords.
        Set<String> subWordsSet = new HashSet<>(subWords);
        /*
            Number of possible subwords is same as word length:
            1: U
            2: UN
            3: UNI
            4: UNIV
            5: UNIVE
            6: UNIVER
            7: UNIVERS
            8: UNIVERSE
        */
        assertEquals(subWordsSet.size(), word.length());
    }

    /**
     * Asserts App.makeSubword properly implements case 2. (See README.)
     */
    public void testMakeSubword_case_2() {
        List<String> subWords = new ArrayList<>();
        String word = "UNIVERSE";
        for(int i = 0; i < 100; i++) {
            subWords.add(App.makeSubword(word, 2));
        }
        Set<String> subWordsSet = new HashSet<>(subWords);
        /*
            Same principle applies as in case 1, but this time in reverse.
            1: UNIVERSE
            2:  NIVERSE
            3:   IVERSE
            4:    VERSE
            5:     ERSE
            6:      RSE
            7:       SE
            8:        E
         */
        assertEquals(subWordsSet.size(), word.length());
    }

    /**
     * Asserts App.makeSubword properly implements case 3. (See README.)
     */
    public void testMakeSubword_case_3() {
        List<String> subWords = new ArrayList<>();
        // String word = "Universe";
        String word = "CODE";
        for(int i = 0; i < 200; i++) { // Pulled 200 out of the air. Seemed right.
            subWords.add(App.makeSubword(word, 3));
        }
        Set<String> subWordsSet = new HashSet<>(subWords);
        /*
            This time the subwords can be created by moving both the left AND right indices.
            There are 35 distinct subwords in "UNIVERSE", so for this case I picked something a little easier.
            There are only 10 possible distinct subwords in "CODE":
            1: CODE
            2:  ODE
            3:   DE
            4:    E
            5: COD
            6: CO
            7: C
            8:  OD
            9:  O
            10:  D
         */
        assertEquals(subWordsSet.size(), 10);
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
        assert(!(stats.getMean() > (25 + 2)) && !(stats.getMean() < (25 - 2)));
    }

    /**
     * Asserts App.oneInNChance returns true 5% (or 1 in 20) of the time, plus or minus one percent.
     */
    public void testOneInNChance_oneInTwenty() {
        SummaryStatistics stats = getOneInNStats(100, 20);
        assert(!(stats.getMean() > (5 + 1)) && !(stats.getMean() < (5 - 1)));
    }

    /**
     * Asserts App.mashWords generates unique strings for some input list of words.
     *
     * For example, using "one", "two", and "three", possible frankenwords could be "onewoee", "oneoe",
     * "onetwoee", "nore", "etwth", or "onewore".
     *
     * This test asserts that the generated frankenwords have at least 3 characters and are not equal to any
     * of the words in the input list, e.g. "one", "two", and "three".
     */
    public void testMashWords_returnsUniqueFrankenwords() {

        mockList = new ArrayList<>();
        mockList.add("one");
        mockList.add("two");
        mockList.add("three");
        List<String> mashWordList = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            mashWordList.add(App.mashWords(mockList));
        }
        for(String s : mashWordList) {
            assertTrue(s.length() > 2);
            assertFalse(mockList.contains(s));
        }
    }

    /**
     * Asserts App.convertStringListToCharArray happy path returns a correct char[].
     */
    public void testConvertStringListToCharArray() {
        mockList = createDummyStringList(10); // "1", "2", "3", ...
        try {
            char[] testRun = convertStringListToCharArray(mockList);
            if(testRun.length != mockList.size()) {
                fail("testRun.length != mockList.size(). Cannot proceed.");
            }
            boolean mismatchFound = false;
            for(int i = 0; i < testRun.length; i++) {
                if(!Character.toString(testRun[i]).equals(mockList.get(i))) {
                    mismatchFound = true;
                }
            }
            assert(!mismatchFound);
        } catch (IllegalStateException e) {
            fail("App.convertStringListToCharArray threw an exception when it shouldn't have.");
        }
    }

    /**
     * Asserts App.substringInclusive returns a substring of inclusive indices.
     */
    public void testSubstringInclusive() {
        String inclusiveSubword = App.substringInclusive("abracadabra", 3, 8);
        assertEquals(inclusiveSubword, "acadab");
    }

    /**
     * Asserts App.addSpecialCharacters returns a modified word of the same length over several runs.
     */
    public void testAddSpecialCharacters_returnsWordOfSameLength() {
        String frankenword = "scrappy";
        for(int i = 0; i < 100; i++) {
            String methodCall = App.addSpecialCharacters(frankenword, specialCharactersMock);
            assert(!frankenword.equals(methodCall));
            assertEquals(frankenword.length(), methodCall.length());
        }
    }

    /**
     * Asserts App.addSpecialCharacters successfully adds 1 or 2 special characters to a word over several runs.
     */
    public void testAddSpecialCharacters_adds1or2chars() {
        for(int i = 0; i < 100; i++) {
            String methodCall = App.addSpecialCharacters("scrappy", specialCharactersMock);
            char[] methodCallChars = methodCall.toCharArray();
            int specialCharCount = 0;
            for (char c : methodCallChars) {
                if (!isAthruZ(Character.toString(c))) {
                    specialCharCount++;
                }
            }
            assert (specialCharCount == 1 || specialCharCount == 2);
        }
    }

    /**
     * Asserts App.addSpecialCharacters successfully adds special characters to a word in all possible char positions
     * over several runs.
     */
    public void testAddSpecialCharacters_convertsAllPossibleCharsToSpecial() {
        Set<Integer> specialCharIndices = new HashSet<>();
        String frankenword = "level";
        for(int i = 0; i < 100; i++) {
            String methodCall = App.addSpecialCharacters(frankenword, specialCharactersMock);
            char[] methodCallChars = methodCall.toCharArray();
            for (int j = 0; j < methodCallChars.length; j++) {
                char thisChar = methodCallChars[j];
                if (!isAthruZ(Character.toString(thisChar))) {
                    specialCharIndices.add(j);
                }
            }
        }
        assertEquals(specialCharIndices.size(), frankenword.length());
    }

    /**
     * Asserts AppTest.isAThruZ is working properly.
     */
    public void testIsAthruZ() {
        List<String> strings = new ArrayList<>();
        strings.add("aBc");
        strings.add("bcD");
        strings.add("cde");
        strings.add("DEf");

        for(String s : strings) {
            assert(isAthruZ(s));
        }

        assertFalse(isAthruZ("^6r"));
    }

    /**
     * Asserts App.addStandardCapitalization turns "coSMos" into both "Cosmos" and "cosmos".
     */
    public void testAddStandardCapitalization() {
        String allLower = "coSMos";
        Set<String> words = new HashSet<>();
        for(int i = 0; i < 10; i++) {
            words.add(App.addStandardCapitalization(allLower));
        }
        assertEquals(words.size(), 2);
        assert(words.contains("Cosmos"));
        assert(words.contains("cosmos"));
    }

    /**
     * Asserts App.addWeirdCapitalization returns a string of the correct length.
     */
    public void testAddWeirdCapitalization_correctLength() {
        String test = "universal";
        String methodCall = App.addWeirdCapitalization(test);
        assertEquals(test.length(), methodCall.length());
    }

    /**
     * Asserts App.addWeirdCapitalization returns at least one unique string over a few short iterations.
     */
    public void testAddWeirdCapitalization_returnsUniqueString() {
        boolean mismatchFound = false;
        String frankenword = "spaceship";
        String methodCall = "";
        for(int i = 0; i < 5; i++) {
            methodCall = App.addWeirdCapitalization(frankenword);
            if(!methodCall.equals(frankenword)) {
                mismatchFound = true;
            }
        }
        assert(mismatchFound);
    }

    /**
     * Asserts App.getRandomCharacter returns all possible random letters of the alphabet over several iterations.
     */
    public void testGetRandomCharacter() {
        Set<String> letters = new HashSet<>();
        for(int i = 0; i < 500; i++) {
            letters.add(App.getRandomCharacter());
        }
        assertEquals(letters.size(), 26);
    }

    /**
     * Asserts App.breakInTwo returns a word of the same length + 1 over several iterations.
     */
    public void testBreakInTwo_plusOneLength() {
        for(int i = 0; i < 100; i++) {
            String testWord = "laser";
            String methodCall = App.breakInTwo(testWord);
            assertEquals(testWord.length() + 1, methodCall.length());
        }
    }

    /**
     * Asserts App.breakInTwo returns a word with exactly one space (" ") over several iterations.
     */
    public void testBreakInTwo_spaceCharFound() {
        for(int i = 0; i < 100; i++) {
            String testWord = "laser";
            String methodCall = App.breakInTwo(testWord);
            char[] ch = methodCall.toCharArray();
            int spacesFound = 0;
            for(int j = 0; j < ch.length; j++) {
                if(ch[j] == ' ') {
                    spacesFound++;
                }
            }
            assertEquals(spacesFound, 1);
        }
    }


    //**************************//
    //***** HELPER METHODS *****//
    //**************************//


    /**
     * Writes a list of strings to a newly-created file. Writes over files if they already exist.
     *
     * @param list     List of strings to print
     * @param fileName Name of file to create
     * @return         Newly-created file
     */
    private File createFileWithStringList(List<String> list, String fileName)
            throws IllegalStateException {
        try {
            FileWriter fw;
            BufferedWriter bw;
            PrintWriter out;
            File file = new File(fileName);
            if(!file.exists()) {
                if(file.createNewFile()) {
                    fw = new FileWriter(file);
                    bw = new BufferedWriter(fw);
                    out = new PrintWriter(bw);
                    for(String s : list) {
                        out.println(s);
                    }
                    out.close();
                } else {
                    throw new IllegalStateException("Error: App.writeStringListToFile was unable to create a new file.");
                }
            } else {
                fw = new FileWriter(file, true);
                bw = new BufferedWriter(fw);
                out = new PrintWriter(bw);
                for(String s : list) {
                    out.println(s);
                }
                out.close();
            }
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Error: App.writeStringListToFile threw an IO exception.");
        }
    }

    /**
     * Convert a list of strings to a char array. Strings in list must have a length of exactly 1.
     *
     * @param list List of single-character strings
     * @return     Converted char array
     */
    private char[] convertStringListToCharArray(List<String> list) throws IllegalStateException { // tested
        if(list == null || list.isEmpty()) {
            throw new IllegalStateException("Error: App.convertStringListToCharArray received a null or empty " +
                    "list argument.");
        }
        char[] charArray = new char[list.size()];
        for(int i = 0; i < list.size(); i++) {
            String thisStr = list.get(i);
            if(thisStr.length() != 1) {
                throw new IllegalStateException("Error: App.convertStringListToCharArray received a list with a " +
                        "string of length != 1: " + thisStr);
            }
            /*
            The above if statement forbids list.get(i) from returning anything other than a string of length 1.
            So no index out of bounds exceptions are possible below.
             */
            charArray[i] = list.get(i).charAt(0);
        }
        return charArray;
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


    /**
     * @return a populated char array of special characters
     */
    private static char[] populateSpecialCharactersMock() {
        char[] returnArr = new char[10];
        returnArr[0] = ';';
        returnArr[1] = '!';
        returnArr[2] = '#';
        returnArr[3] = '$';
        returnArr[4] = '%';
        returnArr[5] = '^';
        returnArr[6] = '&';
        returnArr[7] = '*';
        returnArr[8] = '(';
        returnArr[9] = ')';
        return returnArr;
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

    /**
     * @return a populated wordsToMash variable
     */
    private static List<String> populateWordsToMash() {
        englishWordsMock = populateEnglishWordsMock();
        usedEnglishWordsMock = new ArrayList<>();
        return App.getWordsToMash(App.MAX_WORDS_TO_MASH, englishWordsMock, usedEnglishWordsMock);
    }

    /**
     * Determines if a string is a-z or A-Z.
     *
     * @param s String to analyze
     * @return True if string contains only a-z, false otherwise.
     */
    private boolean isAthruZ(String s) {
        return s.matches("^[a-zA-Z]+$");
    }
}

























