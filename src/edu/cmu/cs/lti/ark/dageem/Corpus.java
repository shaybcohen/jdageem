/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 *
 */
public abstract class Corpus {

	public abstract int size();
	
	public abstract Document get(int k);
	
}
