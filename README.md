JDAGEEM - A Java package for grammar induction
Version 1.01

By Shay Cohen <scohen@inf.ed.ac.uk>

http://www.ark.cs.cmu.edu/DAGEEM

JDageem is an extensible Java package that includes several implementations of parsing and training algorithms for dependency grammar induction. More specifically, JDageem includes:

1. An implementation of the split head automaton parsing algorithm of Eisner and Satta (1999), also specialized to handle the dependency model with valence of Klein and Manning (2004). Both Viterbi and the mininum Bayes risk decoding (Goodman, 1996) versions are implemented.

2. An implementation of the expectation-maximization algorithm for the DMV.

3. An implementation of the variational expectation-maximization algorithm of Cohen and Smith (2010), which makes use of the logistic normal priors (for the DMV).

4. An implementation of a variant of the harmonic DMV initializer that appears in Klein and Manning (2004).

Consult with the manual to extend JDageem to use the logistic normal prior with other models. 

For information about using this package, please refer to the document doc/manual.pdf.

See also the HISTORY file for list of updates.
