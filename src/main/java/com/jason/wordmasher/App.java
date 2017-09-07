package com.jason.wordmasher;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A simple Java / Maven coding exercise that mashes random words together in interesting ways.
 *
 * Created by Jason Herrboldt (intothefuture@gmail.com), September 2017.
 */
public class App {

    private static File englishWordsFile;
    private static File specialCharactersFile;
    private static File outputFile;
    private static final String DATE_STR = getTodaysDate();
    private static final String LOG_DIR = "logs/";
    private static final String LOG_FILENAME = LOG_DIR + DATE_STR + ".txt";
    private static final File LOG_FILE = new File(LOG_FILENAME);
    private static int numberOfFrankenwordsToPrint = 0;
    private static List<String> chosenEnglishWords;
    private static List<String> allEnglishWords;
    private static List<String> randomWords;
    private static List<String> frankenwords;
    private static List<String> specialCharacters;
    private static String parsingArgsResults;

    public static void main(String[] args) {
        startLog();
        if(!parseArgs(args)) {
            logEntry("Encountered an error parsing program arguments: " + parsingArgsResults);
            print("Encountered an error parsing program arguments: " + parsingArgsResults);
        } else {
            // initializeMemberVariables();
            allEnglishWords = readFileIntoMemory(englishWordsFile);
            // debug
            printNStringsFromList(allEnglishWords, "allEnglishWords", 10);
            specialCharacters = readFileIntoMemory(specialCharactersFile);
            // debug
            printNStringsFromList(specialCharacters, "specialCharacters", 15);
            randomWords = pickRandomWords();

            // 50% chance of picking 2 or 3 words from word list.
            int numberOfWordsToPick = oneInNChance(2) ? 2 : 3;

            chosenEnglishWords = getNRandomStringsFromList(allEnglishWords, numberOfWordsToPick);
            if(chosenEnglishWords == null) {
                throw new IllegalStateException("chosenEnglishWords may not be null as it is sent from main to " +
                        "printNStringsFromList.");
            } else {
                printNStringsFromList(chosenEnglishWords, "chosenEnglishWords", chosenEnglishWords.size());
            }

            // continue...
        }
    }

    /**
     * Chose n words at random from the list of all English words.
     *
     * @param n The number of words to chose
     * @return  The chosen English words
     */
    public static List<String> getNRandomStringsFromList(List<String> list, int n) {
        if(list == null || list.isEmpty() || n < 1) {
            return null;
        }
        int firstRandomInt = 0;
        int secondRandomInt = 0;
        // Ensure the two random ints are distinct.
        while(firstRandomInt == secondRandomInt) {
            firstRandomInt = getRandomIntInRange(0, allEnglishWords.size());
            secondRandomInt = getRandomIntInRange(0, allEnglishWords.size());
        }
        List<String> returnList = new ArrayList<>();
        returnList.add(list.get(firstRandomInt));
        returnList.add(list.get(secondRandomInt));
        return returnList;
    }

    /**
     * Generates a pseudorandom number in a specified range.
     *
     * @param min The minimum value of the range
     * @param max The maximum value of the range
     * @return    The chosen pseudorandom number
     */
    public static int getRandomIntInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Processes program arguments.
     *
     * @param args Program arguments
     * @return     True if program arguments could be processed, false otherwise.
     */
    private static boolean parseArgs(String[] args) {
        if(!validateArgs(args)) {
            return false;
        } else {
            englishWordsFile = new File(args[0]);
            specialCharactersFile = new File(args[1]);
            outputFile = new File(args[2]);
            logEntry("All program arguments have been parsed.");
            return true;
        }
    }

    /**
     * Validates program arguments. Writes error messages to parsingArgsResults, which main displays to user.
     *
     * @param args Program arguments
     * @return     True if program arguments are valid, false otherwise.
     */
    public static boolean validateArgs(String[] args) {
        parsingArgsResults = "";
        if(args.length != 4) {
            parsingArgsResults = "Invalid number of program arguments received. Should be 4. " +
                    "Arguments received: " + args.length + ".";
            return false;
        }
        for (int i = 0; i < 2; i++) {
            if (!fileExists(args[i])) {
                parsingArgsResults = "Unable to validate file '" + args[i] + "' can be read.";
                return false;
            }
            File thisFile = new File(args[i]);
            if(thisFile.length() == 0) {
                parsingArgsResults = "File '" + args[i] + "' does not contain any data.";
                return false;
            }
        }
        try {
            numberOfFrankenwordsToPrint = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            parsingArgsResults = "Unable to parse number of frankenwords to print. " +
                    "Argument received: " + args[3] + ".";
            return false;
        }
        if (numberOfFrankenwordsToPrint < 1 || numberOfFrankenwordsToPrint > 1000) {
            parsingArgsResults = "Number of frankenwords to print must be > 0 and < 1001. " +
                    "Argument received: " + numberOfFrankenwordsToPrint + ".";
            return false;
        }
        logEntry("All program arguments validated.");
        return true;
    }

    /**
     * Reads contents of a file into a list of strings.
     *
     * @param  file File to read
     * @return List of strings or null if exception thrown
     */
    private static List<String> readFileIntoMemory(File file) {
        List<String> returnList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                returnList.add(line);
            }
        } catch (IOException e) {
            return null;
        }
        logEntry("The file " + file.getName() + " has been read into memory.");
        return returnList;
    }

    /**
     * @return A list of two or three words chosen at random from the list of English words.
     */
    public static List<String> pickRandomWords() {
        List<String> returnList = new ArrayList<>();


        return returnList;
    }

    /**
     * Generates a one in N boolean. For example, if N is four, a one in four chance would have a 25% chance of
     * returning true.
     *
     * @param n The chance range.
     * @return  One in N chance of being true
     */
    public static boolean oneInNChance(int n) {
        if(n < 1) {
            logEntry("oneInNChance received a negative integer: " + n);
            throw new IllegalStateException("oneInNChance received a negative integer: " + n);
        } else if (n > 100) {
            logEntry("oneInNChance received an integer greater than 100: " + n);
            throw new IllegalStateException("oneInNChance received an integer greater than 100: " + n);
        } else {
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
    public static void print(String s) {
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
