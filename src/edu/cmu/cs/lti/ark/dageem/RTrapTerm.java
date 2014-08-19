/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 *
 */

public class RTrapTerm extends Term {
	int q, h, h_, s_;
	int hashcode = -1;
	
	public RTrapTerm(int q, int h, int h_, int s_)
	{
		assert h < h_;

		this.q = q; this.h = h; this.h_ = h_; this.s_ = s_;		
	}
	
	public int q()
	{
		return q;
	}
	
	public int h()
	{
		return h;
	}
	
	public int h_()
	{
		return h_;
	}
	
	public int s_()
	{
		return s_;
	}
	
	public int hashcode()
	{
		if (hashcode == -1) {
			hashcode = EisnerSattaChart.hasher(EisnerSattaChart.TYPE_RTrap, q, h, h_, s_);
		}
		
		return hashcode;
	}

	public static int hashcode(int q, int h, int h_, int s_) {
		return EisnerSattaChart.hasher(EisnerSattaChart.TYPE_RTrap, q, h, h_, s_);
	}

	public String toString()
	{
		return "rtrapTerm(q="+q()+",h="+h()+",h_="+h_()+",s_="+s_()+")";
	}

	
	public static RTrapTerm addToChart(Chart c, int q, int h, int h_, int s_)
	{
		RTrapTerm rtrapTerm;
		int hash = hashcode(q, h, h_, s_);
		
		if (c.hasTerm(hash))
		{
			rtrapTerm = (RTrapTerm) c.getTerm(hash);
		} else {
			rtrapTerm = new RTrapTerm(q, h, h_, s_);
			c.addTerm(rtrapTerm);
			rtrapTerm.setValue(Chart.semiring.Zero);
		}
		
		return rtrapTerm;
	}

}
