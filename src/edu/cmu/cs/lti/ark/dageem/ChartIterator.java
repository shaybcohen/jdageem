/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.HashSet;
import java.util.Iterator;

/**
 * @author scohen
 * 
 */
public class ChartIterator {

	private HashSet<Integer> set;
	private Iterator<Integer> it;
	private Chart chart;
	
	public ChartIterator(HashSet<Integer> s, Chart c)
	{
		chart = c;
		set = s;
		if (s == null)
		{
			it = null;
			return;
		} else {
			it = set.iterator();
		}
	}
	
	public boolean hasNext() {
		if (set == null)
		{
			return false;
		} else {
			return it.hasNext();
		}
	}

	public Term getTerm() {
		return chart.getTerm(it.next());
	}

}
