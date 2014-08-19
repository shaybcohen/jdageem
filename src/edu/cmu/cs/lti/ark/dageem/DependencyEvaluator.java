/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 *
 */
public interface DependencyEvaluator {

	double[] eval(int[][] gold, int[][] parsed);
	
}
