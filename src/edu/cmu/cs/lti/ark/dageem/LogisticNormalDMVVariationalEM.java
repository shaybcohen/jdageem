/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.io.IOException;

/**
 * @author scohen
 *
 */
public class LogisticNormalDMVVariationalEM extends LogisticNormalVariationalEM {

	private String initGrammarFile;
	private Alphabet alphabet;
	private SentenceCorpus corpus;
	
	public LogisticNormalDMVVariationalEM(LogisticNormalModel model, Alphabet alphabet, String initGrammarFile, SentenceCorpus corpus)
	{
		super(model);
		
		this.alphabet = alphabet;
		this.initGrammarFile = initGrammarFile;
		this.corpus = corpus;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.cmu.cs.lti.ark.dageem.LogisticNormalVariationalEM#saveModel(java.lang.String)
	 */
	@Override
	public void saveModel(String outputFile) {
		try {
			MultinomialDMVGrammar grammar = new MultinomialDMVGrammar(
					alphabet);
			grammar.readGrammar(initGrammarFile, corpus);
			grammar.initMultinomials();
			grammar.copyFromCount(null, getModel());
			grammar.writeGrammar(outputFile);
		} catch (IOException e) {
			System.err.println("Could not write grammar: " + e);
		}
		
		System.err.println("Saved model to " + outputFile);
	}

}
