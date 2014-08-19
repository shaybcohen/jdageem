/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 * 
 */

public abstract class Term {

	private double val;
	
	private Object secondaryValue;

	public void setValue(double v) {
		val = v;
	}

	public double value() {
		return val;
	}
	
	public Object secondaryValue()
	{
		return secondaryValue;
	}
	
	public void setSecondaryValue(Object v)
	{
		secondaryValue = v;
	}

	public abstract int hashcode();
}