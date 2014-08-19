/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 * 
 */
public class LTrapTerm extends Term {
	int q, h_, h, s, s_;
	int hashcode = -1;

	public LTrapTerm(int q, int h_, int h, int s_, int s) {
		assert h_ < h;

		this.q = q;
		this.h_ = h_;
		this.h = h;
		this.s_ = s_;
		this.s = s;
	}

	public int q() {
		return q;
	}

	public int h_() {
		return h_;
	}

	public int h() {
		return h;
	}

	public int s() {
		return s;
	}

	public int s_() {
		return s_;
	}

	public String toString() {
		return "ltrapTerm(h_=" + h_() + ",h=" + h() + ",q=" + q() + ",s=" + s()
				+ ",s_=" + s_() + ")";
	}

	public static LTrapTerm addToChart(Chart c, int q, int h_, int h, int s_,
			int s) {

		int ltrapTermHash = LTrapTerm.hashcode(q, h_, h, s_, s);
		LTrapTerm ltrapTerm;

		if (c.hasTerm(ltrapTermHash)) {
			ltrapTerm = (LTrapTerm) c.getTerm(ltrapTermHash);
		} else {
			ltrapTerm = new LTrapTerm(q, h_, h, s_, s);
			c.addTerm(ltrapTerm);
			ltrapTerm.setValue(Chart.semiring.Zero);
		}

		return ltrapTerm;
	}

	public int hashcode() {
		if (hashcode == -1) {
			hashcode = EisnerSattaChart.hasher(EisnerSattaChart.TYPE_LTrap, q,
					h_, h, s, s_);
		}

		return hashcode;
	}

	public static int hashcode(int q, int h_, int h, int s_, int s) {
		return EisnerSattaChart.hasher(EisnerSattaChart.TYPE_LTrap, q, h_, h,
				s_);
	}
}