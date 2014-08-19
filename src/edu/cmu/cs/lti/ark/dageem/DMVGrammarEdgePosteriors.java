/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.Set;

import cern.colt.list.IntArrayList;

/**
 * @author scohen
 * 
 */
public class DMVGrammarEdgePosteriors extends DMVGrammar {

	public DMVGrammarEdgePosteriors(Alphabet a) {
		super(a);
	}

	public void timesAxiomDeltaStart(EValue v2, int head, int q) {
		int hash;
		// double p1, p2, r2;

		EValue v = new EValue();
		v.setOne();

		switch (q) {
		case STATE_InitialLeftOnly:
			v.setp(Chart.semiring.Times(safeDouble(leftStopHasChild, head),
					safeDouble(rightStopNoChild, head)));

			break;
		case STATE_InitialRightOnly:
			v.setp(Chart.semiring.Times(safeDouble(leftStopNoChild, head),
					safeDouble(rightStopHasChild, head)));

			break;
		case STATE_InitialRightAndLeft:
			v.setp(Chart.semiring.Times(safeDouble(leftStopHasChild, head),
					safeDouble(rightStopHasChild, head)));

			break;
		case STATE_InitialNone:
			v.setp(Chart.semiring.Times(safeDouble(leftStopNoChild, head),
					safeDouble(rightStopNoChild, head)));

			break;

		default:
			break;
		}

		v2.Times(v2.p(), v);
	}

	public void timesAxiomDeltaRight(EValue v, int head, int q, int r, int child) {

		v.Times(getAxiomDeltaRight(head, q, r, child));

	}

	public void timesAxiomDeltaLeft(EValue v, int head, int q, int r, int child) {

		v.Times(getAxiomDeltaLeft(head, q, r, child));

	}

	public void timesAxiomAttachmentRight(EValue v, int i, int j, int head,
			int child, int r, int s_) {
		int hash = hasher(HASH_AttachRight, i, j);

		double p2 = getAxiomAttachmentRight(i, j, head, child, r, s_);
		double r2 = p2;

		v.Times(hash, p2, r2);
	}

	public void timesAxiomAttachmentLeft(EValue v, int i, int j, int head,
			int child, int r, int s_) {
		int hash = hasher(HASH_AttachLeft, i, j);

		double p2 = getAxiomAttachmentLeft(i, j, head, child, r, s_);
		double r2 = p2;

		v.Times(hash, p2, r2);
	}

	public void timesAxiomRoot(EValue v, int i, int head) {
		int hash = hasher(HASH_Root, i, 0);

		double p2 = getAxiomRoot(i, head);
		double r2 = p2;

		v.Times(hash, p2, r2);
	}

	public static double exp(double val) {
	    final long tmp = (long) (1512775 * val + 1072632447);
	    return Double.longBitsToDouble(tmp << 32);
	}
	
	public CompleteGraphGrammar createCompleteGraphGrammar(int n, EValue v2,
			boolean pool) {
		CompleteGraphGrammar g = new CompleteGraphGrammar(n, pool);

		if (v2 == null) {
			return null;
		}

		IntArrayList lst = v2.getKeyList();
		
		for (int i=0; i<lst.size(); i++) {
			int h = lst.get(i);
			int[] arr = hasherMap.get(h);
			double v = Math.exp(Chart.semiring.LogDivide(v2.r(h), v2.p()));
			
			switch (arr[0]) {
			case HASH_AttachRight:
				g.setEdge(arr[1] - 1, arr[2] - 1, v);
				break;
			case HASH_AttachLeft:
				g.setEdge(arr[1] - 1, arr[2] - 1, v);
				break;
			case HASH_Root:
				g.setRoot(arr[1] - 1, v);
				break;
			}
		}

		return g;
	}
}
