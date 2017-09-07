# WordMasher
A Java coding exercise that mashes parts of different words together in a random fashion.

The basic idea is to write a program that generates a list of frankenwords. A frankenword is parts of two or three different words squashed together, with a possible mix of special characters like underscore, numbers, hashtag, etc. 

## System Requirements
Program requires Java 1.8 or higher to run.

## Program Usage
The program requires 4 arguments at runtime. Arguments must be received in the following order:
* Name of file containing list of English words (a list of 58,000 English words may be downloaded [here](http://www.mieliestronk.com/wordlist.html)) 
* Name of file containing special characters (see SPECIAL CHARACTERS section below)
* Name of output file (see OUTPUT section below)
* Number of frankenwords to generate {int n | 1 > n < 1001}

## How To Run
(Coming soon.)

## High Level Design
The program will start by opening a file containing a large number of English words and read them into memory. It will then select an integer at random like so: {int n | 1 > n < 4}. 

Once it has n, it then goes to the list of English words and selects n of them at random, with the following requirements: the selected words must have a length len like so: {int len | 2 > len < MAX}, where MAX will be around 10. No two n words may be the same. 

Once it has n random words of a specified length, it goes to town mashing them up to create a frankenword. 

It starts by creating subwords for each of the n chosen words, deciding at random each time which subword pattern to employ: 

* Subword pattern A: Create a subword with inclusive range [0, i] where 0 > i < word.length.
* Subword pattern B: Create a subword with inclusive range [j, [word.length - 1)] where j >= 0. 
* Subword pattern C: Create a subword with inclusive range [i, j], where 0 >= i < (j - 1) and (i + 1) > j < word.length. 

Consider the test word MYSTIFY. 

| M | Y | S | T | I | F | Y |

Subword pattern A would have a minimum subword of M and a maximum subword of MYSTIFY.

Subword pattern B would have a maximum subword of MYSTIFY and a minimum subword of Y.

Subword pattern C would allow for a range of possible values for i and j, but always leaving a minimum of one character between the two. (It is for this reason that only dictionary words of length three and greater are allowed.) For the word MYSTIFY, which consists of seven characters, i could span the inclusive range [0, 4] and j could span the inclusive range [2, 6], with the understanding that the values of both i and j are dependent on each other. 

One possible implementation of this pattern would be to first select i, then go on to select j given the constraints of the value of i. For example, let j take its maximum possible value for the example word, which in this case is 6. Now i can be any integer in the inclusive range [0, 4]. Let's say it's 2. Now we plug that into the formula for j. We have (i + 1) > j < word.length --> (2 + 1) > j < 7 --> 3 > j < 7. This means j can now be any integer in the inclusive range [4, 6]. Suppose the randomly chosen integer is 5. We have i = 2, j = 5. The subword is then STIF. Words of length n can create subwords that span lengths of [1, (n - 1)], inclusive. 

Once the program has two or three subwords it concatenates them in random order to produce a frankenword. If a frankenword has a length of < 3, n random letters are added until the minimum word length is 3. (Really the only possible edge case here is a frankenword of length 2, given the constraints above.)

## CAPITALIZATION

There is a 50% chance that the first letter of the frankenword will be capitalized and the remaining characters made lower-case. For the other 50%, the algorithm will give each letter of the frankenword a 25% chance to be capitalized, otherwise it will be made lower-case. 

## SPECIAL CHARACTERS

A weighted random boolean function decides if special characters should be inserted into the frankenword. There will be a 1 in X chance (where X is around 6) that this boolean function returns true. 

The special character inserter picks an integer r as such: 
* If the frankenword length is < 6, r can only be 1. Then a special character s is chosen at random from the list of special characters, and used to replace a random index of the frankenword.
* If the frankenword length is > 5, r can have an even chance of being 1 or 2. Then one or two special characters are chosen at random from the list of special characters—the same special character may be chosen twice—and used to replace two random (but distinct) indices of the frankenword. 

A file containing the list of special characters will be specified by the user as a program argument. Special characters may consist of space, underscore, hyphen, apostrophe, period, comma, asterisk, plus, equals, backslash, forwardslash, at sign, colon, pound, percent, carrot, tilde, open angle bracket, closed angle bracket, exclamation point, ampersand, and integers in the inclusive range [0, 9]. Space is a special case: really it's just splitting one word into two; no use putting a space before or after framkenword.

## OUTPUT

Finally the list of frankenwords should be printed to an output file, the name of which is specified as a program argument. Program will overwrite a file of the same name if it already exists.