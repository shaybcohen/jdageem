/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.Arrays;

/**
 * @author scohen
 *
 */
public class BacktrackChain {

	protected int[] ptrs;
	
	public BacktrackChain(int n)
	{
		ptrs = new int[n];
	}
	
	public void set(int i, int v)
	{
		ptrs[i] = v;
	}
	
	public int get(int i)
	{
		return ptrs[i];
	}
	
	public int size()
	{
		return ptrs.length;
	}
	
	public int hashCode()
	{
		return Arrays.hashCode(ptrs);
	}
	
	public boolean equals(Object o)
	{
		BacktrackChain b = (BacktrackChain)o;
		
		return Arrays.equals(ptrs, b.ptrs);
	}
	
	public void print(Chart c)
	{
		for (int i=0; i<ptrs.length; i++)
		{
			System.err.print(" >> ");
			System.err.println(c.getTerm(get(i)) + " : "+c.getTerm(get(i)).value());			
		}
		System.err.println(this);
	}
}
