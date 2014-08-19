package edu.cmu.cs.lti.ark.dageem;

public class RTriangleTerm extends Term {

	int q, h, j;
	int hashcode = -1;

	public RTriangleTerm(int q, int h, int j) {
		assert h <= j;

		this.q = q;
		this.h = h;
		this.j = j;
	}

	public int q() {
		return q;
	}

	public int h() {
		return h;
	}

	public int j() {
		return j;
	}

	public int hashcode() {
		if (hashcode == -1) {
			hashcode = EisnerSattaChart.hasher(EisnerSattaChart.TYPE_RTriangle, q, h,
					j, 0);
		}
		
		return hashcode;
	}

	public String toString() {
		return "rtriangleTerm(h=" + h() + ",j=" + j() + ",q=" + q() + ")";
	}

	public static int hashcode(int q, int h, int j) {
		return EisnerSattaChart.hasher(EisnerSattaChart.TYPE_RTriangle, q, h,
				j);
	}

	public static RTriangleTerm addToChart(Chart c, int q, int h, int j) {
		int rtriangleTermHash = RTriangleTerm.hashcode(q, h, j);

		RTriangleTerm rtriangleTerm_consequent;

		if (c.hasTerm(rtriangleTermHash)) {
			rtriangleTerm_consequent = (RTriangleTerm) c.getTerm(
					rtriangleTermHash);
		} else {
			rtriangleTerm_consequent = new RTriangleTerm(q, h, j);
			c.addTerm(rtriangleTerm_consequent);
			rtriangleTerm_consequent.setValue(Chart.semiring.Zero);
		}

		return rtriangleTerm_consequent;
	}
}
