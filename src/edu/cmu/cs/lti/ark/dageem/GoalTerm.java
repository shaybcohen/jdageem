/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.io.Serializable;

/**
 * @author scohen
 *
 */
public class GoalTerm extends Term implements Serializable {

	
	private static final long serialVersionUID = -1452386774306102353L;
	private static final int hashcode = EisnerSattaChart.hasher(EisnerSattaChart.TYPE_Goal, 0, 0, 0, 0, 0); 
	
	public int hashcode() {
		return hashcode;
	}

	public static int hashcode(int i) {
		return hashcode;
	}
	
	public void setValue(double v)
	{
		super.setValue(v);
	}
	
	public static GoalTerm addToChart(Chart c)
	{
		GoalTerm goalTerm;
		int hash = hashcode(0);
		
		if (c.hasTerm(hash))
		{
			goalTerm = (GoalTerm) c.getTerm(hash);
		} else {
			goalTerm = new GoalTerm();
			c.addTerm(goalTerm);
			goalTerm.setValue(Chart.semiring.Zero);
		}
		
		return goalTerm;
	}

	public String toString()
	{
		return "goal()";
	}
}
