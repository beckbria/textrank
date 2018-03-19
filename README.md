# TextRank

This is a set of utilities for ranking the fitness of source strings based on ngram statistics.  The string "Sample" could be broken up into the following 3-character substrings:

* SAM
* AMP
* MPL
* PLE

These substrings can be compared to how frequently they appear in the English language, and a score can be assigned.  This library includes tools to generate such frequency lists from provided strings, as well as a scorer to rank strings based on those frequencies.  It also includes examples of using the scorer to attack substitution ciphers.