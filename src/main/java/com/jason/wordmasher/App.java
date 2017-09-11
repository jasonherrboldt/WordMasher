package com.jason.wordmasher;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple Java / Maven coding exercise that mashes random words together in interesting ways.
 *
 * Github profile: github.com/jasonherrboldt
 *
 * Created by Jason Herrboldt (intothefuture@gmail.com), September 2017.
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

    // Misc global variables
    private static final int MAX_WORDS_TO_MASH = 10;
    private static final int MAX_WHILE = 1000;
    private static final int MAX_FRANKENWORDS = 1000;
    private static final int ARG_LENGTH_MAX = 50;
    private static List<String> englishWords;
    private static List<String> specialCharacters;
    private static List<String> usedEnglishWords;

    public static void main(String[] args) {
        startLog();
        try {
            parseArgs(args);
        } catch (Exception e) {
            print("\nSomething went wrong. See log for more information.");
        }
    }

    /*
    void parseArgs(String[] args)
        If args.length != 4
            Log the error
            Throw an illegal arguments exception
        for (int i = 0; i < 2; i++) {
            if (!fileExists(args[i])) {
                Log the error
                Throw a new illegal arguments exception
            File thisFile = new File(args[i]);
            if(thisFile.length() == 0) {
                Log the error
                Throw a new illegal arguments exception
            try {
                numberOfFrankenwordsToPrint = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                Log the error
                Throw a new illegal arguments exception
            if (numberOfFrankenwordsToPrint < 1
                || numberOfFrankenwordsToPrint > MAX_FRANKENWORDS) {
                Log the error
                Throw a new illegal arguments exception
        englishWordsFile = new File(args[0]);
        specialCharactersFile = new File(args[1]);
        outputFile = new File(args[2]);
     */

    static void parseArgs(String[] args){
        if(args.length != 4) {
            logEntry("Error: Program must have 4 arguments. Number of arguments received: "+ args.length + ".");
            logEntry("Program terminated");
            throw new IllegalArgumentException("App.parseArgs encountered one or more illegal program arguments.");
        }
        for (int i = 0; i < 4; i++) {
            if (args[i].length() > ARG_LENGTH_MAX) {
                logEntry("Error: One or more program argument exceeds the maximum length of " + ARG_LENGTH_MAX + ".");
                logEntry("Program terminated");
                throw new IllegalArgumentException("App.parseArgs encountered one or more illegal program arguments.");
            }
        }
        for (int i = 0; i < 2; i++) {
            if (!fileExists(args[i])) {
                logEntry("Error: Unable to verify that file " + args[i] + " exists.");
                logEntry("Program terminated");
                throw new IllegalArgumentException("App.parseArgs encountered one or more illegal program arguments.");
            }
            File thisFile = new File(args[i]);
            if(thisFile.length() == 0) {
                logEntry("Error: File " + args[i] + " appears to be empty.");
                logEntry("Program terminated");
                throw new IllegalArgumentException("App.parseArgs encountered one or more illegal program arguments.");
            }
        }
        try {
            numberOfFrankenwordsToCreate = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            logEntry("Error: Unable to parse number of frankenwords to print. Argument received: " + args[3] + ".");
            logEntry("Program terminated");
            throw new IllegalArgumentException("App.parseArgs encountered one or more illegal program arguments.");
        }
    }

    /**
     * Verifies specified file exists.
     *
     * @param fileName Name of file
     * @return         True if file can be read, false otherwise.
     */
    private static boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.canRead();
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
            print("WARN: Main.logEntry encountered an IO exception: " + e.getMessage());
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

}







































