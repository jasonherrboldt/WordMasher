# WordMasher
A Java coding exercise that mashes parts of different words together in a random fashion to create interesting "frankenwords". 

## How To Run
Clone the project into a local directory and import the project into your favorite IDE. (I used IntelliJ IDEA - Community 2017.2.) Make sure you have Maven installed and a local Maven repository. See pom.xml for application and unit test dependencies. 

## Program Usage

Args may be submitted in any order. Do not surround arg strings with double or single quotes. Recommend no spaces in input file names and input files be in project root directory. Of course this goes without saying, but make sure the referenced files exist and contain acceptable data. 

The user will always be alerted if errors occur, and referred to the log file as well as this README. (See LOGGING and EXCEPTIONS sections below.) Program exits gracefully in all events. 

* (Required) -wordsfile [WORDS_FILE]: A user-provided list of words to mash. Words must have length {len | 2 > len < 10} to be picked up by the program. The program selects 2 or 3 distinct words for every frankenword, which creates a relationship between the number of words in the words file and the number of requested frankenwords. Please be mindful of this relationship to ensure program does not exit without generating any output. Words must be separated in the file by a carriage return, i.e. hitting Return between each typed word. A handy file of 58,000 English words may be downloaded [here](http://www.mieliestronk.com/wordlist.html). A copy of this file is included in the project root (english_words.txt).
* (Required) -numtoprint [INT]: The number of frankenwords to print to the output file. INT must be an integer-parsable string in the range {n | 0 > n < 1001}.
* -specialcharsfile [SPECIAL_CHARS_FILE]: A user-provided list of special characters to be randomly sprinkled into the frankenwords. Character strings must have a length of exactly one, and must be separated in the file by a carriage return. There is no restriction on what special characters may be injected at runtime, but some may cause trouble when opening the output file with certain programs. Use with caution. Special characters may repeat; if you want more numbers to appear than other special chars, enter the chars 0 - 9 multiple times and other special chars only once. Four sample special character files are included in the project root, or you can create your own. (See SPECIAL CHARACTERS section below for more information.) 
* -addspaces: Program will randomly inject one or two spaces into generated frankenwords. (See SPACES section below for more information.) 

## Sample Output
    WITH SPECIAL CHARACTERS AND ADDED SPACES
    ipw
    malamEsn
    phIs3sfi;
    std
    VocAliSegssed
    ing12t
    sg#
    iro o
    rs m5Nlyca1iN
    altErsm pE
    
    WITH JUST ADDED SPACES
    utensilo
    isvenEre
    isr aelilon
    urllyinsurEs
    ch anceoypinC
    agicalblanc
    Tyrant twhod unit
    Jomuviewer
    tE deEpithe
    E strewing
    
    WITH JUST SPECIAL CHARACTERS
    9ub)er
    Earnedunitgl
    sigs
    opsemp
    aLues
    civilvestsinTEst
    Uratesequate
    Epurit5)
    chersgladwep
    Eetake
    
    WITH NO SPECIAL CHARACTERS OR ADDED SPACES
    mErtaStaxi
    pluckeRconveyits
    Ccruingbomberd
    ghtfEElarm
    Utwardrage
    tgoingst
    echine
    Stz
    pdefusp
    oilEdb

## Test-Driven Development

All unit tests are located in the AppTest class. 

I'm a big cheerleader of test-driven development. Every method in this project was broken down into smaller helper methods, and each helper method was rigorously unit tested. 

My approach is to create a rough draft of a method to explore and invent its functionality. Then I break the method into smaller helper methods, each with their own injectable arguments and non-void returns. 

Finally I create unit tests for the helper methods, which often results in changing the method signatures to accept additional parameters for injection. This helps me figure out how to mock any necessary objects, which for the purposes of this exercise consist largely of data structures from Java's util package. 

Of course these tests often fail right out of the gate, which leads me to refine the methods until the tests pass. There are a number of methods that employ the use of pseudorandom number generators, e.g. how to decide which words to select, or how special characters and spaces should be added. For these kinds of methods, I wrapped the unit tests in a loop and made sure all possible method outcomes are captured, usually in a Set object. 

My favorite thing about test-driven development is that once I'm done rigorously testing all my helper methods, I can simply click them together and watch the whole thing run smoothly the first time. (Well, close to the first time.) 

Functional testing is limited; there are only four acceptable workflows ("happy paths") through the program. (See the SAMPLE OUTPUT section above.) For this reason I added the file regression_tests.txt, which contains program arguments for all four happy paths. If something goes wrong the program will alert the user and exit gracefully. 

## High Level Design
The program starts by opening a file containing a number of words and reading them into memory. It will then select either 2 or 3 words at random from the list, with the following requirements: the selected words must have a length {int len | 2 > len < MAX}, where MAX will be around 10. No two n words may be the same. This holds for all frankenwords created in a given session; no two identical words will be used from the list of all words read into memory while the program is running.

Once it has n distinct and random words of a specified length, it mashes them up to create a frankenword. 

It starts by creating subwords for each of the n chosen words, deciding at random each time which subword pattern to employ: 

* Subword pattern A: Create a subword with inclusive range [0, i] where 0 > i < word.length.
* Subword pattern B: Create a subword with inclusive range [j, [word.length - 1)] where j >= 0. 
* Subword pattern C: Create a subword with inclusive range [i, j], where 0 >= i < (j - 1) and (i + 1) > j < word.length. 

Consider the test word MYSTIFY. 

| M | Y | S | T | I | F | Y |

Subword pattern A would have a minimum subword of M and a maximum subword of MYSTIFY.

Subword pattern B would have a maximum subword of MYSTIFY and a minimum subword of Y.

Subword pattern C would allow for a range of possible values for i and j, but always leaving a minimum of one character between the two. (It is for this reason that only words of length three and greater are allowed.) For the word MYSTIFY, which consists of seven characters, i could span the inclusive range [0, 4] and j could span the inclusive range [2, 6], with the understanding that the values of both i and j are dependent on each other. 

One possible implementation of this pattern would be to first select i, then go on to select j given the constraints of the value of i. For example, let j take its maximum possible value for the example word, which in this case is 6. Now i can be any integer in the inclusive range [0, 4]. Let's say it's 2. Now we plug that into the formula for j. We have (i + 1) > j < word.length --> (2 + 1) > j < 7 --> 3 > j < 7. This means j can now be any integer in the inclusive range [4, 6]. Suppose the randomly chosen integer is 5. We have i = 2, j = 5. The subword is then STIF. Words of length n can create subwords that span lengths of [1, (n - 1)], inclusive. 

Once the program has two or three subwords it concatenates them in random order to produce a frankenword. If a frankenword has a length of < 3, n random letters are added until the minimum word length is 3. (Really the only possible edge case here is a frankenword of length 2, given the constraints above.)

## CAPITALIZATION

There is a 50% chance that the first letter of the frankenword will be capitalized and the remaining characters made lower-case. For the other 50%, the algorithm will give each letter of the frankenword a 14% chance to be capitalized, otherwise it will be made lower-case. 

## SPECIAL CHARACTERS

A weighted random boolean function decides if special characters should be inserted into the frankenword. There will be a 1 in X chance (where X is around 6) that this boolean function returns true. 

The special character inserter picks an integer r as such: 
* If the frankenword length is < 6, r can only be 1. Then a special character s is chosen at random from the list of special characters, and used to replace a random index of the frankenword.
* If the frankenword length is > 5, r can have an even chance of being 1 or 2. Then one or two special characters are chosen at random from the list of special characters -- the same special character may be chosen twice -- and used to replace two random (but distinct) indices of the frankenword. 

## SPACES

A weighted random boolean function decides if special characters should be inserted into the frankenword. There will be a 1 in X chance (where X is around 4) that this boolean function returns true. 

If the frankenword length is > 6, there is an even chance that it will be broken into either two or three words. Otherwise it will be broken into two words.

## OUTPUT

Finally the list of frankenwords is printed to an output file with name "output.txt" in the project root directory. The program will overwrite a file of the same name if it already exists.

## LOGGING

Several methods print useful information to the log file. This aids the reproduction of critical issues. I elected to roll my own logging infrastructure instead of using Log4J for the simple reason that there is not enough demand for the full utility of Log4J's features.

Logs are created in the /log directory and named with today's date, e.g. 2017-09-24.txt. Sessions that begin before midnight and carry over will be logged to the same file. New sessions started after midnight will be logged to a new file. Each entry line has the following format: HH:MM:SS:SSS [log message]. Log uses military time.

## EXCEPTIONS

With the exception of logging and parsing program arguments, the program's entire main method is couched within a try / catch block. This block will catch all possible errors, warn the user if something goes wrong, print helpful error information to the log, and exit gracefully. If the program runs successfully, a message will appear to the user that the output file has been populated. 
