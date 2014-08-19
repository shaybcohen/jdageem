/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 *
 */
public abstract class AgendaInterface {

	public abstract boolean pop(PoppedValueContainer container);

	public abstract void setChart(Chart c);
	
	public abstract void markAgendaItem(Term term, double value, Object obj,
			BacktrackChain bt);
	
	public abstract void markAgendaItem(Term term, double value, Object obj,
			BacktrackChain bt, boolean b);
	
	public abstract void setExhaustive(boolean b);
	
}
