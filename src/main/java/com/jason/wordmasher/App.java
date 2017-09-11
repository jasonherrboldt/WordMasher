package com.jason.wordmasher;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private static final int MAX_WORDS_TO_MASH = 10;
    private static final int MAX_WHILE = 1000;
    private static final int MAX_FRANKENWORDS = 1000;
    private static final int ARG_LENGTH_MAX = 50;
    private static final String PARSE_ARGS_ERROR_MESSAGE = "App.parseArgs encountered one or more " +
            "illegal program arguments.";
    private static List<String> englishWords;
    private static List<String> specialCharacters;
    private static List<String> usedEnglishWords;

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
            throw new IllegalStateException("App.readFileIntoMemory received a non-existent file: " + file.getName());
        }
        if(file.length() == 0) {
            throw new IllegalStateException("App.readFileIntoMemory received an empty file: " + file.getName());
        }
        List<String> returnList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                returnList.add(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("App.readFileIntoMemory threw an IO exception: " + e.getMessage());
        }
        logEntry("The file " + file.getName() + " has been read into memory.");
        return returnList;
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
            print("Unable to print list. List has " + list.size() + " elements, and n = " + n + ".");
        } else {
            print("\nFirst " + n + " strings from list " + listName + ":");
            for(int i = 0; i < n; i++) {
                print(list.get(i));
            }
        }
    }
}







































