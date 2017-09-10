package com.jason.wordmasher;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple Java / Maven coding exercise that mashes random words together in interesting ways.
 *
 * Created by Jason Herrboldt (intothefuture@gmail.com), September 2017.
 */
public class App {

    // Program arguments
    // Let englishWordsFile be the file of all English words
    private static File englishWordsFile;

    // Let specialCharactersFile be the file of special characters
    private static File specialCharactersFile;

    // Let outputFile be the output file
    private static File outputFile;

    // Let numberOfFrankenwordsToCreate be the number of words to create
    private static int numberOfFrankenwordsToCreate = 0;

    // Misc global variables
    // Let MAX_WORDS_TO_MASH be some integer around 10
    private static final int MAX_WORDS_TO_MASH = 10;

    // Let MAX_WHILE be some integer around 1000
    private static final int MAX_WHILE = 1000;

    // Let MAX_FRANKENWORDS be some integer around 1000
    private static final int MAX_FRANKENWORDS = 1000;

    // Let englishWords be the list of English words read into memory
    private static List<String> englishWords;

    // Let specialCharacters be the list of special characters read into memory
    private static List<String> specialCharacters;

    // Let usedWords be an empty list of strings
    private static List<String> usedEnglishWords;

    // Logging
    private static final String DATE_STR = getTodaysDate();
    private static final String LOG_DIR = "logs/";
    private static final String LOG_FILENAME = LOG_DIR + DATE_STR + ".txt";
    private static final File LOG_FILE = new File(LOG_FILENAME);


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

}







































