package com.jason.wordmasher;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A Java / Maven coding exercise that mashes random words together in interesting ways.
 *
 * See README for usage rules, how to run, and output examples.
 *
 * Sample program args: "english_words.txt" "special_characters.txt" "output.txt" 10
 *
 * Created September 2017 by Jason Herrboldt (intothefuture@gmail.com, github.com/jasonherrboldt).
 */
public class App {

    // Program arguments
    private static File wordsFile;
    private static File specialCharactersFile;
    private static File outputFile;
    private static int numberOfFrankenwordsToCreate = 0;

    // Logging
    private static final String DATE_STR = getTodaysDate();
    private static final String LOG_DIR = "logs/";
    private static final String LOG_FILENAME = LOG_DIR + DATE_STR + ".txt";
    private static final File LOG_FILE = new File(LOG_FILENAME);

    // Misc variables
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
    private static final String WORDS_FILE_ARG = "-wordsfile";
    private static final String SPECIAL_CHARS_FILE_ARG = "-specialcharsfile";
    private static final String NUM_TO_PRINT_ARG = "-numtoprint";
    private static final String SPACES_ARG = "-spaces";

    /**
     * Main program method.
     *
     * @param args Program arguments.
     */
    public static void main(String[] args) {
        startLog();
        try {
            parseArgs(args);
            englishWords = readFileIntoListOfStrings(wordsFile);
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


    //*******************//
    //***** LOGGING *****//
    //*******************//


    /**
     * Open a new logging session. Create a new /log directory as needed.
     */
    private static void startLog() { // can be functionally tested
        File dir = new File("logs");
        if(!dir.exists()) {
            if(!dir.mkdir()) {
                print("WARN: unable to create directory 'logs'.");
            }
        }
        logEntry("New log started.");
    }

    /**
     * Add a new log entry. Create a new document as needed, or append to an existing one.
     *
     * @param log the log entry
     */
    private static void logEntry(String log) throws IllegalStateException { // can be functionally tested
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
    private static String getTodaysDate() { // can be functionally tested
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


    //********************************//
    //***** PARSING PROGRAM ARGS *****//
    //********************************//

    // -words “english_words.txt” -specialchars “special_characters_A_shortest.txt” -spaces -numtoprint 1000

    /**
     * Validates and parses program arguments.
     *
     * (This method prints un-escaped user input to the logs, which is normally not allowed. But this is a toy program.)
     *
     * @param args Program arguments
     */
    static void parseArgs(String[] args) throws IllegalArgumentException { // todo: not tested

        // prepare data structures
        List<Integer> acceptableArgCount = new ArrayList<>(Arrays.asList(4, 5, 6, 7));
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        ListIterator<String> iterator = argsList.listIterator();
        while (iterator.hasNext()) {
            iterator.set(iterator.next().toLowerCase());
        }

        // Validate arguments

        // Make sure a legal number of arguments were received.
        if(!acceptableArgCount.contains(argsList.size())) {
            logEntry("Error: Program must have 4, 5, 6, or 7 arguments. Number of arguments received: "
                    + args.length + ".");
            logEntry("Program terminated");
            throw new IllegalArgumentException("App.parseArgs encountered one or more illegal program arguments.");
        }

        if(!argsAreInGoodOrder(argsList)) {
            logEntry("The args do not appear to be in good order.");
            logEntry("Program terminated");
            throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
        }




//        for(int i = 0; i < argsList.size(); i++) {
//            String arg = argsList.get(i);
//            if(arg.charAt(0) == '-') {
//                if(i == argsList.size() - 1 && !arg.equals(SPACES_ARG)) {
//                    logEntry("Error: The last program arg begins with '-' and it is not " + SPACES_ARG);
//                    logEntry("Program terminated");
//                    throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
//                }
//            }
//        }

        // Check for illegal arguments.
        if(illegalArgsReceived(argsList)) {
            logEntry("Error: One or more illegal arguments were received.");
            logEntry("Program terminated");
            throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
        }

        // Make sure minimum required args were received.
        if((!argsList.contains(WORDS_FILE_ARG) && argsList.contains(NUM_TO_PRINT_ARG))) {
            logEntry("Error: The minimum args '-wordsfile' and '-numtoprint' were not both found.");
            logEntry("Program terminated");
            throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
        }

        // Populate class member variables.
        for(int i = 0; i < argsList.size(); i++) {
            if(argsList.get(i).equals(WORDS_FILE_ARG)) {
                wordsFile = makeNewFile(argsList.get(i + 1));
                if(wordsFile == null) {
                    logEntry("Error: App.makeNewFile returned null when attempting to populate wordsFile.");
                    logEntry("Program terminated");
                    throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
                }
            }
            if(argsList.get(i).equals(SPECIAL_CHARS_FILE_ARG)) {
                specialCharactersFile = makeNewFile(argsList.get(i + 1));
                if(specialCharactersFile == null) {
                    logEntry("Error: App.makeNewFile returned null when attempting to populate specialCharactersFile.");
                    logEntry("Program terminated");
                    throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
                }
            }
            if(argsList.get(i).equals(NUM_TO_PRINT_ARG)) {
                numberOfFrankenwordsToCreate = getNumberOfFrankenwordsToCreate(argsList.get(i + 1));
                if(numberOfFrankenwordsToCreate == -1) {
                    logEntry("Error: App.getNumberOfFrankenwordsToCreate returned -1.");
                    logEntry("Program terminated");
                    throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
                }
            }
        }



        outputFile = new File("output.txt");




//        for (int i = 0; i < 2; i++) {
//            if (!fileExists(args[i])) {
//                logEntry("Error: Unable to verify that file " + args[i] + " exists.");
//                logEntry("Program terminated");
//                throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
//            }
//            File thisFile = new File(args[i]);
//            if(thisFile.length() == 0) {
//                logEntry("Error: File " + args[i] + " appears to be empty.");
//                logEntry("Program terminated");
//                throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
//            }
//        }
//        try {
//            numberOfFrankenwordsToCreate = Integer.parseInt(args[3]);
//        } catch (NumberFormatException e) {
//            logEntry("Error: Unable to parse number of frankenwords to print. Argument received: " + args[3] + ".");
//            logEntry("Program terminated");
//            throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
//        }
//        if (numberOfFrankenwordsToCreate < 1 || numberOfFrankenwordsToCreate > MAX_FRANKENWORDS) {
//            logEntry("Error: Number of frankenwords to print must be > 0 and < " + MAX_FRANKENWORDS + ".");
//            logEntry("Argument received " + numberOfFrankenwordsToCreate + ".");
//            logEntry("Program terminated");
//            throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
//        }
//
//        // Parse arguments
//        wordsFile = new File(args[0]);
//        specialCharactersFile = new File(args[1]);
//        outputFile = new File(args[2]);



        logEntry("Program arguments validated and parsed.");
    }

    /**
     * Determines whether or not the args appear to be in good order. Specifically,
     *
     * @param argsList the list of args to analyze
     * @return         true if the args appear to be in good order, false otherwise.
     */
    public static boolean argsAreInGoodOrder(List<String> argsList) {
        for(int i = 0; i < argsList.size() - 1; i++) {
            String thisArg = argsList.get(i);
            // Will not cause index out of bounds exceptions because for loop stops 1 short of end.
            if(thisArg.charAt(0) == '-' && (argsList.get(i + 1).charAt(0) == '-')) {
                if(!thisArg.equals(SPACES_ARG)) {
                    logEntry("A dash arg is followed by another dash arg, and the first dash arg is not " +
                            SPACES_ARG + ": " + thisArg);
                    return false;
                }
            }
        }
        String lastArg = argsList.get(argsList.size() - 1);
        if(lastArg.charAt(0) == '-' && !lastArg.equals(SPACES_ARG)) {
            logEntry("The last arg is a dash arg, and it is not " + SPACES_ARG + ": " + lastArg);
            throw new IllegalArgumentException(PARSE_ARGS_ERROR_MESSAGE);
        }
        return true;
    }

    /**
     * @param argsList the args to analyze
     * @return         true if the args are legal, false otherwise
     */
    public static boolean illegalArgsReceived(List<String> argsList) {
        List<String> acceptableArgs = new ArrayList<>(Arrays.asList(WORDS_FILE_ARG, SPECIAL_CHARS_FILE_ARG,
                NUM_TO_PRINT_ARG, SPACES_ARG));
        for(String s : argsList) {
            if(s.charAt(0) == '-') {
                if(!acceptableArgs.contains(s)) {
                    logEntry("Error: App.illegalArgsReceived determined that arg " + s + " is illegal.");
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Create and validate a new File object.
     *
     * @param fileName Name of file to analyze
     * @return         The file, if it exists and is not empty. Null otherwise.
     */
    public static File makeNewFile(String fileName) { // todo: not tested
        if(StringUtils.isNotBlank(fileName)) {
            logEntry("Error: App.makeNewFile received a blank arg.");
            return null;
        }
        File file = new File(fileName);
        if (!file.exists()) {
            logEntry("Error: App.makeNewFile was unable to determine that the file " + fileName + " exists.");
            return null;
        }
        if(file.length() == 0) {
            logEntry("Error: App.makeNewFile was unable to determine that the file " + fileName + " contains any data.");
            return null;
        }
        return file;
    }

    /**
     * Parse the number of frankenwords to create.
     *
     * @param frankenwordArg the program arg to parse
     * @return               the successfully converted int, -1 otherwise.
     */
    public static int getNumberOfFrankenwordsToCreate(String frankenwordArg) { // todo: not tested
        int returnInt;
        try {
            returnInt = Integer.parseInt(frankenwordArg);
        } catch (NumberFormatException e) {
            logEntry("Error: App.getNumberOfFrankenwordsToCreate was unable to convert the arg " + frankenwordArg +
                    " to an integer.");
            return -1;
        }
        if (returnInt < 1 || returnInt > MAX_FRANKENWORDS) {
            logEntry("Error: App.getNumberOfFrankenwordsToCreate determined that this number of words to print is " +
                    "out of bounds: " + returnInt);
            return -1;
        }
        return returnInt;
    }

    /**
     * Verifies specified file exists.
     *
     * @param fileName Name of file
     * @return         True if file can be read, false otherwise.
     */
//    private static boolean fileExists(String fileName) { // can be functionally tested
//        File file = new File(fileName);
//        return file.exists();
//    }

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


    //*******************************//
    //***** MAKING FRANKENWORDS *****//
    //*******************************//


    /**
     * Make a list of frankenwords using getWordsToMash.
     *
     * @return a list of frankenwords
     */
    private static List<String> makeFrankenwords() { // can be functionally tested
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
     * Prints a list of frankenwords to the output file. (Will overwrite existing file of the same name.)
     *
     * @param frankenwords The list of frankenwords to print.
     */
    private static void printFrankenwords(List<String> frankenwords) { // can be functionally tested
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
     * Make a frankenword from a list of words to mash.
     *
     * @param wordsToMash The words to mash
     * @return            The frankenword
     */
    private static String makeFrankenword(List<String> wordsToMash) { // can be functionally tested
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
        // todo: if specialcharsfile exists...
        if(oneInNChance(4)) {
            frankenword = addSpecialCharacters(frankenword, specialCharacters);
        }
        // todo: if -space arg received...
        if(oneInNChance(4)) {
            if(frankenword.length() > 6) {
                if(oneInNChance(2)) {
                    frankenword = breakInTwo(frankenword);
                } else {
                    frankenword = breakInThree(frankenword);
                }
            } else {
                frankenword = breakInTwo(frankenword);
            }
        }
        return frankenword;
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
                                       List<String> usedEnglishWords_) throws IllegalStateException { // tested
        if(numberOfWordsToMash < MIN_WORDS_TO_MASH || numberOfWordsToMash > MAX_WORDS_TO_MASH) {
            errorMessage = "Error: App.getWordsToMash received an illegal int: "
                    + numberOfWordsToMash + ".";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        if(englishWords == null || englishWords.isEmpty()) {
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
            mashedWord.append(makeSubword(s, n));
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
            if(oneInNChance(11)) {
                returnArray[i] = Character.toUpperCase(frankenwordCharArray[i]);
            } else {
                returnArray[i] = Character.toLowerCase(frankenwordCharArray[i]);
            }
        }
        return String.valueOf(returnArray);
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


    //**************************//
    //***** HELPER METHODS *****//
    //**************************//


    /**
     * Inserts a space into a random index of a frankenword, effectively breaking it into two different words.
     *
     * The longest possible mashed word is 9 * 3 = 27.
     *
     * @param frankenword the word to break
     * @return            the broken word
     */
    static String breakInTwo(String frankenword) { // tested
        if(frankenword == null || frankenword.length() < 3 || frankenword.length() > 27) {
            errorMessage = "Error: App.breakInTwo received an illegal argument.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        int randInt = getRandomIntInInclusiveRange(1, frankenword.length() - 1);
        String substring_A = frankenword.substring(0, randInt);
        String substring_B = frankenword.substring(randInt, frankenword.length());

        return substring_A + " " + substring_B;
    }

    /**
     * Utilizes App.breakInTwo two insert two spaces into a random index of a frankenword, effectively breaking it
     * into three different words. The smallest possible word sent to breakInTwo has a length of 3, and the longest
     * has a length of 18.
     *
     * @param frankenword the word to break -- must be between 8 and 27 characters long, inclusive.
     * @return            the broken word
     */
    static String breakInThree(String frankenword) { // tested
        if(frankenword == null || frankenword.length() < 7 || frankenword.length() > 27) {
            errorMessage = "Error: App.breakInThree received an illegal argument.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // Split the word somewhere near the middle (tested to be safe).
        int split = getRandomIntInInclusiveRange(3, frankenword.length() - 4);

        // Send each half of the frankenword to breakInTwo and return the concatenated result.
        String substring_A = breakInTwo(frankenword.substring(0, split));
        String substring_B = breakInTwo(frankenword.substring(split, frankenword.length()));
        return substring_A + substring_B;
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
     * Shortcut to System.out.println
     *
     * @param s String to print
     */
    static void print(String s) {
        System.out.println(s);
    }

    /**
     * Generates a pseudorandom number in a specified INCLUSIVE range.
     *
     * @param min The minimum value of the range (inclusive)
     * @param max The maximum value of the range (inclusive)
     * @return    The chosen pseudorandom number
     */
    static int getRandomIntInInclusiveRange(int min, int max) { // tested
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}