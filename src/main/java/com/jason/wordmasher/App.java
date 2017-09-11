package com.jason.wordmasher;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    private static final int MAX_WHILE = 1000;
    private static final int MAX_WORDS_TO_MASH = 10;
    private static final int MIN_CANDIDATE_WORD_LENGTH = 2;
    private static final String PARSE_ARGS_ERROR_MESSAGE = "App.parseArgs encountered one or more " +
            "illegal program arguments.";
    private static List<String> englishWords;
    private static List<String> specialCharacters;
    private static List<String> usedEnglishWords;
    private static String errorMessage;

    public static void main(String[] args) {
        startLog();
        try {
            parseArgs(args);
            englishWords = readFileIntoMemory(englishWordsFile);
            specialCharacters = readFileIntoMemory(specialCharactersFile);

            // debug
            printNStringsFromList(englishWords, "englishWords", 10);
            // debug
            printNStringsFromList(specialCharacters, "specialCharacters", 15);

            // continue...

            logEntry("Program finished.");
        } catch (Exception e) {
            String time = new SimpleDateFormat("kk:mm:ss").format(new Date());
            print("\nSomething went wrong around " + time + ". See " + LOG_FILENAME
                    + " for more information.");
        }
    }

    /**
     * Validates and parses program arguments.
     *
     * (This method prints un-escaped user input to the logs. Not addressed because this is a toy program.)
     *
     * @param args Program arguments
     */
    static void parseArgs(String[] args) throws IllegalArgumentException {

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
    private static boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /**
     * Reads contents of a file into a list of strings.
     *
     * @param  file File to read
     * @return List of strings or null if exception thrown
     */
    static List<String> readFileIntoMemory(File file) throws IllegalStateException {
        if(!fileExists(file.getName())) {
            errorMessage = "App.readFileIntoMemory received a non-existent file: " + file.getName();
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        if(file.length() == 0) {
            errorMessage = "App.readFileIntoMemory received an empty file: " + file.getName();
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        List<String> returnList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                returnList.add(line);
            }
        } catch (IOException e) {
            errorMessage = "App.readFileIntoMemory threw an IO exception: " + e.getMessage();
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        logEntry("The file " + file.getName() + " has been read into memory.");
        return returnList;
    }

    /**
     * Get a list of words to mash from englishWords.
     *
     * @param numberOfWordsToMash The number of words to mash
     * @return                    A list of words to mash
     */
    List<String> getWordsToMash(int numberOfWordsToMash) throws IllegalStateException {
        if(numberOfWordsToMash < 1 || numberOfWordsToMash > MAX_WORDS_TO_MASH) {
            errorMessage = "App.getWordsToMash received an illegal int: "
                    + numberOfWordsToMash + ".";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        } else {
            List<String> wordsToMash = new ArrayList<>();
            int i = 0;
            while(wordsToMash.size() < numberOfWordsToMash) {
                int randInt = getRandomIntInRange(0, englishWords.size() - 1);
                String candidateWord = englishWords.get(randInt);
                if(!usedEnglishWords.contains(candidateWord) && !wordsToMash.contains(candidateWord)) {
                    if(candidateWord.length() > MIN_CANDIDATE_WORD_LENGTH && candidateWord.length()
                            < MAX_CANDIDATE_WORD_LENGTH) {
                        wordsToMash.add(candidateWord);
                        usedEnglishWords.add(candidateWord);
                    }
                }
                i++;
                if(i > MAX_WHILE) {
                    errorMessage = "A while loop in getWordsToMash exceeded " + MAX_WHILE + " iterations.";
                    logEntry(errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
            }
            return wordsToMash;
        }
    }

    /**
     * Open a new logging session. Create a new /log directory as needed.
     */
    private static void startLog() {
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
    private static void logEntry(String log) {
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

    /**
     * @return today's date in the format YYYY-MM-DD
     */
    private static String getTodaysDate() {
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
    static void print(String s) {
        System.out.println(s);
    }

    /**
     * Mostly for debugging.
     *
     * @param list     The list to print
     * @param listName The name of the list
     * @param n        How many items of the list to print
     */
    private static void printNStringsFromList(List<String> list, String listName, int n) {
        if(n > list.size()) {
            errorMessage = "Unable to print list. List has " + list.size() + " elements, and n = " + n + ".";
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
    static File createFileWithStringList(List<String> list, String fileName) throws IllegalStateException {
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
                    errorMessage = "App.writeStringListToFile was unable to create a new file.";
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
            errorMessage = "App.writeStringListToFile threw an IO exception.";
            logEntry(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    /**
     * Generates a pseudorandom number in a specified INCLUSIVE range.
     *
     * @param min The minimum value of the range (inclusive)
     * @param max The maximum value of the range (inclusive)
     * @return    The chosen pseudorandom number
     */
    static int getRandomIntInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}







































