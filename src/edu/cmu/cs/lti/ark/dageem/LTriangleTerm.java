/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 * 
 */
public class LTriangleTerm extends Term {
	int q, i, h, s;
	int hashcode = -1;

	public LTriangleTerm(int q, int i, int h, int s) {
		assert i <= h;

		this.q = q;
		this.h = h;
		this.i = i;
		this.s = s;
	}

	public int q() {
		return q;
	}

	public int i() {
		return i;
	}

	public int h() {
		return h;
	}

	public int s() {
		return s;
	}

	public int hashcode() {
		if (hashcode == -1) {
			hashcode = EisnerSattaChart.hasher(EisnerSattaChart.TYPE_LTriangle, q, i,
				h, s);
		}
		
		return hashcode;
	}

	public String toString() {
		return "ltriangleTerm(i=" + i() + ",h=" + h() + ",q=" + q() + ",s="
				+ s() + ")";
	}

	public static int hashcode(int q, int i, int h, int s) {
		return EisnerSattaChart.hasher(EisnerSattaChart.TYPE_LTriangle, q, i,
				h, s);
	}

	public static LTriangleTerm addToChart(Chart c, int q, int i, int h, int s) {

		int ltriangleTermHash = LTriangleTerm.hashcode(q, i, h, s);

		LTriangleTerm ltriangleTerm_consequent;

		if (c.hasTerm(ltriangleTermHash)) {
			ltriangleTerm_consequent = (LTriangleTerm) c
					.getTerm(ltriangleTermHash);
		} else {
			ltriangleTerm_consequent = new LTriangleTerm(q, i, h, s);
			c.addTerm(ltriangleTerm_consequent);
			ltriangleTerm_consequent.setValue(Chart.semiring.Zero);
		}

		return ltriangleTerm_consequent;

	}
}
