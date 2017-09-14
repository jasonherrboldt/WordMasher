package com.jason.wordmasher;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A simple Java / Maven coding exercise that mashes random words together in interesting ways.
 *
 * See README for usage rules. Sample program args: "english_words.txt" "special_characters.txt" "output.txt" 10
 *
 * Created September 2017 by Jason Herrboldt (intothefuture@gmail.com, github.com/jasonherrboldt).
 */
public class App {

    // Program arguments
    private static File englishWordsFile;
    private static File specialCharactersFile;
    private static File outputFile;
    private static int numberOfFrankenwordsToCreate = 0;

    // Logging
    private static final String DATE_STR = getTodaysDate();
    private static final String LOG_DIR = "logs/";
    private static final String LOG_FILENAME = LOG_DIR + DATE_STR + ".txt";
    private static final File LOG_FILE = new File(LOG_FILENAME);

    // Misc variables
    private static final int ARG_LENGTH_MAX = 50;
    private static final int MAX_CANDIDATE_WORD_LENGTH = 10;
    private static final int MAX_FRANKENWORDS = 1000;
    private static final int MAX_ONE_IN_N_CHANCE = 100;
    private static final int MAX_WHILE = 1000;
    static final int MAX_WORDS_TO_MASH = 10;
    private static final int MIN_CANDIDATE_WORD_LENGTH = 2;
    private static final int MIN_WORDS_TO_MASH = 1;
    private static final String PARSE_ARGS_ERROR_MESSAGE =
            "App.parseArgs encountered one or more illegal program arguments.";
    private static List<String> englishWords;
    private static char[] specialCharacters;
    private static List<String> usedEnglishWords = new ArrayList<>();
    private static String errorMessage;

    /**
     * Main program method.
     *
     * @param args Program arguments.
     */
    public static void main(String[] args) {
        startLog();
        try {
            parseArgs(args);
            englishWords = readFileIntoListOfStrings(englishWordsFile);
            specialCharacters = readFileIntoCharArray(specialCharactersFile);
            List<String> frankenwords = makeFrankenwords();
            printFrankenwords(frankenwords);
            print("\nThe output file has been populated!");
            logEntry("Program finished.");
        } catch (Exception e) {
            handleMainException(e);
        }
    }

    /**
     * Handle an exception thrown from main.
     *
     * @param e the exception
     */
    private static void handleMainException(Exception e) {

        // Try to print something meaningful to the log.
        if(errorMessage != null && !errorMessage.isEmpty()) {
            logEntry(errorMessage);
        }
        String exceptionMessage = e.getMessage();
        if(exceptionMessage != null && !exceptionMessage.isEmpty()) {
            logEntry(exceptionMessage);
        }
        Throwable cause = e.getCause();
        if(cause != null) {
            logEntry(cause.toString());
            logEntry(cause.getMessage());
        }
        StackTraceElement[] stacktrace = e.getStackTrace();
        if(stacktrace != null && stacktrace.length > 0) {
            logEntry("*** BEGIN STACKTRACE ***");
            for(StackTraceElement s : stacktrace) {
                logEntry(s.toString());
            }
            logEntry("*** END STACKTRACE ***");
        }

        // Break the news to the user.
        String time = new SimpleDateFormat("kk:mm:ss").format(new Date());
        print("\nSomething went wrong around " + time + ". See " + LOG_FILENAME
                + " for more information.");
    }

    /**
     * Prints a list of frankenwords to the output file. (Will overwrite existing file of the same name.)
     *
     * @param frankenwords The list of frankenwords to print.
     */
    static void printFrankenwords(List<String> frankenwords) { // can only be functionally tested
        if(frankenwords == null || frankenwords.isEmpty()) {
            errorMessage = "Error: App.printFrankenwords received a null or empty list.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        try {
            PrintWriter out = new PrintWriter(outputFile);
            for(String s : frankenwords) {
                out.println(s);
            }
            out.close();
        } catch (IOException e) {
            errorMessage = "Error: App.printFrankenwords threw an IO exception: " + e.getMessage();
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    /**
     * Validates and parses program arguments.
     *
     * (This method prints un-escaped user input to the logs. Not addressed because this is a toy program.)
     *
     * @param args Program arguments
     */
    static void parseArgs(String[] args) throws IllegalArgumentException { // tested

        // Validate arguments
        if(args.length != 4) {
            logEntry("Error: Program must have 4 arguments. Number of arguments received: "+ args.length + ".");
            logEntry("Program terminated");
            throw new IllegalArgumentException("App.parseArgs encountered one or more illegal program arguments.");
        }
        for (int i = 0; i < 4; i++) {
            if (args[i].length() > ARG_LENGTH_MAX) {
                logEntry("Error: One or more program argument exceeds the maximum length of " + ARG_LENGTH_MAX + ".");
                logEntry("Program terminated");
                throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
            }
        }
        for (int i = 0; i < 2; i++) {
            if (!fileExists(args[i])) {
                logEntry("Error: Unable to verify that file " + args[i] + " exists.");
                logEntry("Program terminated");
                throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
            }
            File thisFile = new File(args[i]);
            if(thisFile.length() == 0) {
                logEntry("Error: File " + args[i] + " appears to be empty.");
                logEntry("Program terminated");
                throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
            }
        }
        try {
            numberOfFrankenwordsToCreate = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            logEntry("Error: Unable to parse number of frankenwords to print. Argument received: " + args[3] + ".");
            logEntry("Program terminated");
            throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
        }
        if (numberOfFrankenwordsToCreate < 1 || numberOfFrankenwordsToCreate > MAX_FRANKENWORDS) {
            logEntry("Error: Number of frankenwords to print must be > 0 and < " + MAX_FRANKENWORDS + ".");
            logEntry("Argument received " + numberOfFrankenwordsToCreate + ".");
            logEntry("Program terminated");
            throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
        }

        // Parse arguments
        englishWordsFile = new File(args[0]);
        specialCharactersFile = new File(args[1]);
        outputFile = new File(args[2]);
        logEntry("Program arguments validated and parsed.");
    }

    /**
     * Verifies specified file exists.
     *
     * @param fileName Name of file
     * @return         True if file can be read, false otherwise.
     */
    private static boolean fileExists(String fileName) { // not tested
        File file = new File(fileName);
        return file.exists();
    }

    /**
     * Reads contents of a file into a list of strings.
     *
     * @param  file File to read
     * @return List of strings from file
     */
    static List<String> readFileIntoListOfStrings(File file) throws IllegalStateException { // tested
        List<String> returnList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                returnList.add(line);
            }
        } catch (IOException e) {
            errorMessage = "Error: App.readFileIntoListOfStrings threw an IO exception: " + e.getMessage();
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        logEntry("The file " + file.getName() + " has been read into a string list.");
        return returnList;
    }

    /**
     * Reads contents of a file into a char array. Blows up if any of the lines in the file have length > 1.
     *
     * @param  file File to read
     * @return Char array from file
     */
    static char[] readFileIntoCharArray(File file) throws IllegalStateException { // tested
        List<String> fileStringList = readFileIntoListOfStrings(file); // Already vetted for empty files.
        int fileLength = fileStringList.size();
        char[] returnArray = new char[fileLength];

        for(int i = 0; i < fileLength; i++) {
            String s = fileStringList.get(i);
            if(s.length() != 1 || s.equals("")) {
                errorMessage = "Error: App.readFileIntoCharArray encountered an illegal string in " +
                        "specialCharactersFile.";
                logEntry(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            returnArray[i] = s.charAt(0);
        }
        logEntry("The file " + file.getName() + " has been read into a char array.");
        return returnArray;
    }

    /**
     * Get a list of words to mash from englishWords.
     *
     * @param numberOfWordsToMash The number of words to mash
     * @param englishWords        englishWords mock
     * @param usedEnglishWords_   usedEnglishWords mock (can be null if not unit testing)
     * @return                    A list of words to mash
     */
    static List<String> getWordsToMash(int numberOfWordsToMash, List<String> englishWords,
                                       List<String> usedEnglishWords_) throws IllegalStateException {
        if(numberOfWordsToMash < MIN_WORDS_TO_MASH || numberOfWordsToMash > MAX_WORDS_TO_MASH) {
            errorMessage = "Error: App.getWordsToMash received an illegal int: "
                    + numberOfWordsToMash + ".";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        if(listIsNullOrEmpty(englishWords)) {
            errorMessage = "Error: getWordsToMash received a null or empty list of English words.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // Allow unit test to inject its own copy of usedEnglishWords. Otherwise use App's class member variable.
        List<String> localUsedEnglishWords;
        if(usedEnglishWords_ != null) {
            localUsedEnglishWords = usedEnglishWords_;
        } else {
            localUsedEnglishWords = usedEnglishWords;
        }

        List<String> wordsToMash = new ArrayList<>();
        int i = 0;
        while(wordsToMash.size() < numberOfWordsToMash) {
            int randInt = getRandomIntInInclusiveRange(0, englishWords.size() - 1);
            String candidateWord = englishWords.get(randInt);
            if(!localUsedEnglishWords.contains(candidateWord) && !wordsToMash.contains(candidateWord)) {
                if(candidateWord.length() > MIN_CANDIDATE_WORD_LENGTH && candidateWord.length()
                        < MAX_CANDIDATE_WORD_LENGTH) {
                    wordsToMash.add(candidateWord);
                    localUsedEnglishWords.add(candidateWord);
                }
            }
            i++;
            if(i > MAX_WHILE) {
                errorMessage = "Error: A while loop in getWordsToMash exceeded " + MAX_WHILE + " iterations.";
                logEntry(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }

        // Java is pass by value. Update App's class member variable with the local copy to maintain state.
        if(usedEnglishWords_ == null) {
            usedEnglishWords = localUsedEnglishWords;
        }
        return wordsToMash;
    }

    /**
     * Makes a subword according to the program requirements. (See README.)
     *
     * @param word The word to use
     * @param n    Which subword pattern to use
     * @return     The subword
     */
    static String makeSubword(String word, int n) throws IllegalStateException { // tested
        if(word == null || word.length() < MIN_CANDIDATE_WORD_LENGTH || word.length() > MAX_CANDIDATE_WORD_LENGTH) {
            errorMessage = "Error: makeSubword received an illegal 1st argument.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        if(n != 1 && n != 2 && n != 3) {
            errorMessage = "Error: makeSubword received an illegal 2nd argument. Must be 1, 2, or 3.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        int i = 0;
        int j = 0;

        switch(n) {
            case(1): {
                i = getRandomIntInInclusiveRange(0, word.length() - 1);
                return substringInclusive(word, 0, i);
            }
            case(2): {
                j = getRandomIntInInclusiveRange(0, word.length() - 1);
                return substringInclusive(word, j, word.length() - 1);
            }
            case(3): {
                int wordLength = word.length();
                i = getRandomIntInInclusiveRange(0, wordLength - 1);
                j = getRandomIntInInclusiveRange(i, wordLength - 1);
                return substringInclusive(word, i, j);
            }
        }

        errorMessage = "Error: logic fell through App.makeSubword.";
        logEntry(errorMessage);
        throw new IllegalStateException(errorMessage);
    }

    /**
     * Open a new logging session. Create a new /log directory as needed.
     */
    private static void startLog() { // not tested
        File dir = new File("logs");
        if(!dir.exists()) {
            if(!dir.mkdir()) {
                print("WARN: unable to create directory 'logs'.");
            }
        }
        logEntry("New log started.");
    }

    /**
     * Make a list of frankenwords using getWordsToMash.
     *
     * @return a list of frankenwords
     */
    private static List<String> makeFrankenwords() { // can only be functionally tested
        List<String> outputList = new ArrayList<>();
        int numberOfWordsToMash;
        List<String> wordsToMash;
        String frankenword;
        for(int i = 0; i < numberOfFrankenwordsToCreate; i++) {
            numberOfWordsToMash = oneInNChance(2) ? 2 : 3;
            wordsToMash = getWordsToMash(numberOfWordsToMash, englishWords, null);
            frankenword = makeFrankenword(wordsToMash);
            if(frankenword == null || frankenword.isEmpty()) {
                errorMessage = "Error: App.wordsToMash returned a null or empty frankenword to App.makeFrankenwords.";
                logEntry(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            outputList.add(frankenword);
        }
        return outputList;
    }

    /**
     * Make a frankenword from a list of words to mash.
     *
     * @param wordsToMash The words to mash
     * @return            The frankenword
     */
    private static String makeFrankenword(List<String> wordsToMash) { // can only be functionally tested
        if(wordsToMash == null || wordsToMash.size() < 2) {
            errorMessage = "Error: App.makeFrankenword received an illegal argument.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        String frankenword = mashWords(wordsToMash);
        if(frankenword.length() < 3) {
            frankenword += getRandomCharacter();
        }
        if(oneInNChance(2)) {
            frankenword = addStandardCapitalization(frankenword);
        } else {
            frankenword = addWeirdCapitalization(frankenword);
        }
        if(oneInNChance(5)) {
            frankenword = addSpecialCharacters(frankenword, specialCharacters);
        }
        if(oneInNChance(6)) {
            frankenword = breakInTwo(frankenword);
        }
        return frankenword;
    }

    /**
     * Inserts a space int a random index of a frankenword, effectively breaking it into two different words.
     *
     * @param frankenword the word to break
     * @return            the broken word
     */
    public static String breakInTwo(String frankenword) {
        int randInt = getRandomIntInInclusiveRange(1, frankenword.length() - 1);

        /*
             Example:
           String str= new String("quick brown fox jumps over the lazy dog");
           System.out.println("Substring starting from index 15:");
           System.out.println(str.substring(15));
           System.out.println("Substring starting from index 15 and ending at 20:");
           System.out.println(str.substring(15, 20));

            Substring starting from index 15:
             jumps over the lazy dog
            Substring starting from index 15 and ending at 20:
             jump
         */

        String substring_A = frankenword.substring(0, randInt);
        String substring_B = frankenword.substring(randInt, frankenword.length());

        return substring_A + " " + substring_B;
    }

    /**
     * Generate a random string character.
     *
     * @return a random string character.
     */
    static String getRandomCharacter() { // tested
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        int N = alphabet.length();
        Random r = new Random();
        char randChar = alphabet.charAt(r.nextInt(N));
        return Character.toString(randChar);
    }

    /**
     * Add a new log entry. Create a new document as needed, or append to an existing one.
     *
     * @param log the log entry
     */
    private static void logEntry(String log) throws IllegalStateException { // not tested
        if(log != null && !log.isEmpty()) {
            try {
                FileWriter fw;
                BufferedWriter bw;
                PrintWriter out;
                String time;
                if (!LOG_FILE.exists()){
                    if(LOG_FILE.createNewFile()) {
                        fw = new FileWriter(LOG_FILE);
                        bw = new BufferedWriter(fw);
                        out = new PrintWriter(bw);
                        time = new SimpleDateFormat("kk:mm:ss:SSS").format(new Date());
                        out.println(time + " " + log);
                        out.close();
                    } else {
                        print("WARN: Main.logEntry unable to create new log file.");
                    }
                } else {
                    fw = new FileWriter(LOG_FILENAME, true);
                    bw = new BufferedWriter(fw);
                    out = new PrintWriter(bw);
                    time = new SimpleDateFormat("kk:mm:ss:SSS").format(new Date());
                    out.println(time + " " + log);
                    out.close();
                }
            } catch (IOException e) {
                throw new IllegalStateException("App.logEntry threw an IO exception: " + e.getMessage());
            }
        }
    }

    /**
     * @return today's date in the format YYYY-MM-DD
     */
    private static String getTodaysDate() { // not tested
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // months are zero-indexed
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String monthStr = "";
        if(month < 10) {
            monthStr = "0" + Integer.toString(month);
        } else {
            monthStr = Integer.toString(month);
        }

        String dayStr = "";
        if(day < 10) {
            dayStr = "0" + Integer.toString(day);
        } else {
            dayStr = Integer.toString(day);
        }
        return Integer.toString(year) + "-" + monthStr + "-" + dayStr;
    }

    /**
     * Shortcut to System.out.println
     *
     * @param s String to print
     */
    static void print(String s) { // not tested
        System.out.println(s);
    }

    /**
     * Mostly for debugging.
     *
     * @param list     The list to print
     * @param listName The name of the list
     * @param n        How many items of the list to print
     */
    private static void printNStringsFromList(List<String> list, String listName, int n)
            throws IllegalStateException { // not tested
        if(n > list.size()) {
            errorMessage = "Error: Unable to print list. List has " + list.size() + " elements, and n = " + n + ".";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        } else {
            print("\nFirst " + n + " strings from list " + listName + ":");
            for(int i = 0; i < n; i++) {
                print(list.get(i));
            }
        }
    }

    /**
     * Writes a list of strings to a newly-created file. Writes over files if they already exist.
     *
     * @param list     List of strings to print
     * @param fileName Name of file to create
     * @return         Newly-created file
     */
    static File createFileWithStringList(List<String> list, String fileName)
            throws IllegalStateException { // not tested
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
                    errorMessage = "Error: App.writeStringListToFile was unable to create a new file.";
                    logEntry(errorMessage);
                    throw new IllegalStateException(errorMessage);
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
            logEntry("createFileWithStringList succesfully printed to " + fileName + ".");
            return file;
        } catch (IOException e) {
            errorMessage = "Error: App.writeStringListToFile threw an IO exception.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    /**
     * Mash together a list of words.
     *
     * @param wordsToMash The list of words to mash
     * @return            The mashed word
     */
    static String mashWords(List<String> wordsToMash) throws IllegalStateException { // tested
        if(wordsToMash == null || (wordsToMash.size() != 2 && wordsToMash.size() != 3)) {
            errorMessage = "Error: App.mashWords received an illegal argument.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        StringBuilder mashedWord = new StringBuilder("");
        List<Integer> oneTwoThree = new ArrayList<>();
        for(int i = 1; i < 4; i++) {
            oneTwoThree.add(i);
        }
        Collections.shuffle(wordsToMash);
        for(String s : wordsToMash) {
            // Let int n be 1, 2, or 3 at random
            Collections.shuffle(oneTwoThree);
            int n = oneTwoThree.get(0);
            mashedWord.append(makeSubword(s, n)); // *** THIS MIGHT BE THE CULPRIT THAT'S RETURNING EMPTY STRINGS ***
        }
        return mashedWord.toString();
    }

    /**
     * Adds random and indistinct special characters to random and distinct indices of a frankenword.
     *
     * @param frankenWord       The frankenword to process
     * @param specialCharacters The special characters to use
     * @return                  The augmented frankenword
     */
    static String addSpecialCharacters(String frankenWord, char[] specialCharacters)
            throws IllegalStateException { // tested
        if(frankenWord == null || frankenWord.length() < 3 || specialCharacters == null
                || specialCharacters.length == 0) {
            errorMessage = "Error: App.addSpecialCharacters received an illegal argument.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // Decide how many special characters to insert.
        int charsToUse = 0;
        if(frankenWord.length() < 6) {
            charsToUse = 1;
        } else {
            if(oneInNChance(2)) {
                charsToUse = 1;
            } else {
                charsToUse = 2;
            }
        }

        // Select non-distinct special characters at random.
        char[] randChars = new char[charsToUse];
        for(int i = 0; i < charsToUse; i++) {
            randChars[i] = specialCharacters[getRandomIntInInclusiveRange(0, specialCharacters.length - 1)];
        }

        // Insert special characters into random and distinct indices of the frankenword.
        List<Integer> usedIndices = new ArrayList<>();
        int i = 0;
        int whileCount = 0;
        StringBuilder frankenBuilder = new StringBuilder(frankenWord);
        while(i < charsToUse) {
            if(whileCount > MAX_WHILE) {
                errorMessage = "Error: App.addSpecialCharacters while loop exceeded " + MAX_WHILE + " iterations.";
                logEntry(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            int j = getRandomIntInInclusiveRange(0, frankenWord.length() - 1);
            if(!usedIndices.contains(j)) {
                if(i > randChars.length || j > frankenWord.length()) {
                    errorMessage = "Error: App.addSpecialCharacters obtained illegal values for either i or j.";
                    logEntry(errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
                frankenBuilder.setCharAt(j, randChars[i]);
                usedIndices.add(j);
                i++;
            }
            whileCount++;
        }
        return frankenBuilder.toString();
    }

    /**
     * Adds standard capitalization to frankenword, e.g. "eclipse" --> "Eclipse".
     *
     * @param frankenword The word to process
     * @return            The capitalized word
     */
    static String addStandardCapitalization(String frankenword) { // tested
        if(frankenword == null || frankenword.length() < 3) {
            errorMessage = "Error: App.addStandardCapitalization received an illegal string.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        if(oneInNChance(2)) {
            String firstLetter = frankenword.substring(0, 1).toUpperCase();
            String restOfLetters = frankenword.substring(1).toLowerCase();
            return firstLetter + restOfLetters;
        } else {
            return frankenword.toLowerCase();
        }
    }

    /**
     * Adds random capitalization to frankenword.
     *
     * @param frankenword The word to analyze
     * @return            The processed word
     */
    static String addWeirdCapitalization(String frankenword) { // tested
        if(frankenword == null || frankenword.length() < 3) {
            errorMessage = "Error: App.addWeirdCapitalization received an illegal string.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        frankenword = frankenword.toLowerCase();
        char[] frankenwordCharArray = frankenword.toCharArray();
        char[] returnArray = new char[frankenwordCharArray.length];
        for(int i = 0; i < frankenwordCharArray.length; i++) {
            if(oneInNChance(4)) {
                returnArray[i] = Character.toUpperCase(frankenwordCharArray[i]);
            } else {
                returnArray[i] = Character.toLowerCase(frankenwordCharArray[i]);
            }
        }
        return String.valueOf(returnArray);
    }

    /**
     * Convert a list of strings to a char array. Strings in list must have a length of exactly 1.
     *
     * @param list List of single-character strings
     * @return     Converted char array
     */
    static char[] convertStringListToCharArray(List<String> list) throws IllegalStateException { // tested
        if(list == null || list.isEmpty()) {
            errorMessage = "Error: App.convertStringListToCharArray received a null or empty list argument.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        char[] charArray = new char[list.size()];
        for(int i = 0; i < list.size(); i++) {
            String thisStr = list.get(i);
            if(thisStr.length() != 1) {
                errorMessage = "Error: App.convertStringListToCharArray received a list with a string of " +
                        "length != 1: " + thisStr;
                logEntry(errorMessage);
                throw new IllegalStateException(errorMessage);
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
     * Generates a pseudorandom number in a specified INCLUSIVE range.
     *
     * @param min The minimum value of the range (inclusive)
     * @param max The maximum value of the range (inclusive)
     * @return    The chosen pseudorandom number
     */
    static int getRandomIntInInclusiveRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Checks to see if a list of objects is null or empty.
     *
     * @param list The list to analyze
     * @return     True if the list is null or empty, false otherwise.
     */
    private static boolean listIsNullOrEmpty(List<String> list) { // not tested
        return list == null || list.isEmpty();
    }

    /**
     * Helper method to override String.substring, which has indices that are inclusive / exclusive.
     * This method has indices that are inclusive / inclusive.
     *
     * @param word       Word to process
     * @param beginIndex Beginning index (inclusive)
     * @param endIndex   Ending index (inclusive)
     * @return           Substring
     */
    static String substringInclusive(String word, int beginIndex, int endIndex)
            throws IllegalStateException { // tested
        if(beginIndex < 0 || endIndex < 0 || endIndex < beginIndex || endIndex > word.length() - 1) {
            errorMessage = "Error: App.substringInclusive encountered one or more illegal arguments";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        char[] chars = word.toCharArray();
        List<Character> outputCharList = new ArrayList<>();
        for(int i = beginIndex; i <= endIndex; i++) {
            outputCharList.add(chars[i]);
        }
        StringBuilder returnString = new StringBuilder("");
        for(Character c : outputCharList) {
            returnString.append(c.toString());
        }
        return returnString.toString();
    }

    /**
     * Generates a one in N boolean. For example, if N is four, a one in four chance would have a 25% chance of
     * returning true.
     *
     * @param n The chance range.
     * @return  One in N chance of being true
     */
    static boolean oneInNChance(int n) throws IllegalStateException { // tested
        if(n < 1 || n > MAX_ONE_IN_N_CHANCE) {
            errorMessage = "Error: oneInNChance received an illegal argument.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        List<Boolean> bools = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            if(i == 0) {
                bools.add(true);
            } else {
                bools.add(false);
            }
        }
        Collections.shuffle(bools);
        return bools.get(0);
    }
}







































