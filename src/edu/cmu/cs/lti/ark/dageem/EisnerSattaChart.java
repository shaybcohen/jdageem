/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;

/**
 * @author scohen
 * 
 */
public class EisnerSattaChart extends Chart {

	private SplitHeadAutomatonGrammar grammar;

	// a cut that contains all left triangles that have i in the left side, and
	// a final state F. key is i. target is set of all triangles for that i.
	private HashMap<Integer, HashSet<Integer>> cutLTrianglesFrom_i;
	private HashMap<Integer, HashSet<Integer>> cutLTrianglesFrom_i_all;
	private HashMap<Integer, HashSet<Integer>> cutRTrianglesFrom_j;

	private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> cutLTrianglesFrom_h_s;
	private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> cutLTrapsFrom_h__s_;
	private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> cutRTrapsFrom_h__s_;
	private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> cutRTrianglesFrom_h_q;
	private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> cutRTrianglesFrom_h_q_n;
	private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> cutLTrianglesFrom_h_s_1;

	public static final int TYPE_LTrap = 1;
	public static final int TYPE_RTrap = 2;
	public static final int TYPE_LTriangle = 3;
	public static final int TYPE_RTriangle = 4;
	public static final int TYPE_Goal = 5;

	private int n;

	void setLength(int n_) {
		n = n_;
	}

	public EisnerSattaChart(SplitHeadAutomatonGrammar g) {
		cutLTrianglesFrom_i = new HashMap<Integer, HashSet<Integer>>();
		cutLTrianglesFrom_i_all = new HashMap<Integer, HashSet<Integer>>();
		cutRTrianglesFrom_j = new HashMap<Integer, HashSet<Integer>>();

		cutLTrianglesFrom_h_s = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
		cutLTrapsFrom_h__s_ = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
		cutRTrapsFrom_h__s_ = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
		cutRTrianglesFrom_h_q = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
		cutRTrianglesFrom_h_q_n = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
		cutLTrianglesFrom_h_s_1 = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
		grammar = g;
	}

	private static final RabinHashFunction32 rhf = RabinHashFunction32.DEFAULT_HASH_FUNCTION;
	private static byte[] hasherTmp = new byte[6*4];	

	
	public static final int hasher(int typ, int a, int b, int c) {
		hasherTmp[0] = (byte) (typ >> 24);
        hasherTmp[1] = (byte) (typ >> 16);
        hasherTmp[2] = (byte) (typ >> 8);
        hasherTmp[3] = (byte) typ;
		hasherTmp[4] = (byte) (a >> 24);
        hasherTmp[5] = (byte) (a >> 16);
        hasherTmp[6] = (byte) (a >> 8);
        hasherTmp[7] = (byte) a;
		hasherTmp[8] = (byte) (b >> 24);
        hasherTmp[9] = (byte) (b >> 16);
        hasherTmp[10] = (byte) (b >> 8);
        hasherTmp[11] = (byte) b;
		hasherTmp[12] = (byte) (c >> 24);
        hasherTmp[13] = (byte) (c >> 16);
        hasherTmp[14] = (byte) (c >> 8);
        hasherTmp[15] = (byte) c;
        
        return Arrays.hashCode(hasherTmp);
		
        //return rhf.hash(hasherTmp, 0, 16, 0);
	}
	public static final int hasher(int typ, int a, int b, int c, int d, int e) {
		hasherTmp[0] = (byte) (typ >> 24);
        hasherTmp[1] = (byte) (typ >> 16);
        hasherTmp[2] = (byte) (typ >> 8);
        hasherTmp[3] = (byte) typ;
		hasherTmp[4] = (byte) (a >> 24);
        hasherTmp[5] = (byte) (a >> 16);
        hasherTmp[6] = (byte) (a >> 8);
        hasherTmp[7] = (byte) a;
		hasherTmp[8] = (byte) (b >> 24);
        hasherTmp[9] = (byte) (b >> 16);
        hasherTmp[10] = (byte) (b >> 8);
        hasherTmp[11] = (byte) b;
		hasherTmp[12] = (byte) (c >> 24);
        hasherTmp[13] = (byte) (c >> 16);
        hasherTmp[14] = (byte) (c >> 8);
        hasherTmp[15] = (byte) c;
		hasherTmp[16] = (byte) (d >> 24);
        hasherTmp[17] = (byte) (d >> 16);
        hasherTmp[18] = (byte) (d >> 8);
        hasherTmp[19] = (byte) d;
		hasherTmp[20] = (byte) (e >> 24);
        hasherTmp[21] = (byte) (e >> 16);
        hasherTmp[22] = (byte) (e >> 8);
        hasherTmp[23] = (byte) e;
		
        return Arrays.hashCode(hasherTmp);
        
        //return rhf.hash(hasherTmp);
        /*int[] arr = new int[6];

		arr[0] = typ;
		arr[1] = a;
		arr[2] = b;
		arr[3] = c;
		arr[4] = d;
		arr[5] = e;

		return Arrays.hashCode(arr);*/
	}

	public static final int hasher(int typ, int a, int b, int c, int d) {
		return hasher(typ, a, b, c, d, 0);
	}

	ChartIterator getChartIteratorFromPairedHash(
			HashMap<Integer, HashMap<Integer, HashSet<Integer>>> map, int i,
			int j) {
		HashMap<Integer, HashSet<Integer>> map2;

		if ((map2 = map.get(i)) == null) {
			return new ChartIterator(null, this);
		}

		return new ChartIterator(map2.get(j), this);
	}

	ChartIterator getLTrianglesFrom_h_s_FinalState(int h, int s) {
		return getChartIteratorFromPairedHash(cutLTrianglesFrom_h_s, h, s);

	}

	ChartIterator getLTrianglesFrom_h_s_1_FinalState(int h, int s) {
		return getChartIteratorFromPairedHash(cutLTrianglesFrom_h_s_1, h, s);

	}

	ChartIterator getLTrapsFrom_h__s_(int h_, int s_) {
		return getChartIteratorFromPairedHash(cutLTrapsFrom_h__s_, h_, s_);
	}

	ChartIterator getRTrapsFrom_h__s_(int h_, int s_) {
		return getChartIteratorFromPairedHash(cutRTrapsFrom_h__s_, h_, s_);
	}

	ChartIterator getRTrianglesFrom_h_q(int h, int q) {
		return getChartIteratorFromPairedHash(cutRTrianglesFrom_h_q, h, q);
	}

	ChartIterator getRTrianglesFrom_h_q_n(int h, int q) {
		return getChartIteratorFromPairedHash(cutRTrianglesFrom_h_q_n, h, q);
	}

	ChartIterator getLTrianglesFrom_i_FinalState(int i) {
		return new ChartIterator(cutLTrianglesFrom_i.get(i), this);
	}

	ChartIterator getLTrianglesFrom_i(int i) {
		return new ChartIterator(cutLTrianglesFrom_i_all.get(i), this);
	}

	ChartIterator getRTrianglesFrom_j(int i) {
		return new ChartIterator(cutRTrianglesFrom_j.get(i), this);
	}

	void addValueToPairedIntCut(
			HashMap<Integer, HashMap<Integer, HashSet<Integer>>> map, int i,
			int j, int hash) {
		HashMap<Integer, HashSet<Integer>> map2;

		if ((map2 = map.get(i)) == null) {
			map2 = new HashMap<Integer, HashSet<Integer>>();
			map.put(i, map2);
		}

		HashSet<Integer> set;

		if ((set = map2.get(j)) == null) {
			set = new HashSet<Integer>();
			map2.put(j, set);
		}

		set.add(hash);
	}

	protected void addValueToSingleIntCut(
			HashMap<Integer, HashSet<Integer>> cut, int i, int hash) {
		HashSet<Integer> set;

		if (cut.get(i) == null) {
			set = new HashSet<Integer>();
			cut.put(i, set);
		} else {
			set = cut.get(i);
		}

		set.add(hash);
	}

	public void addToCuts(Term t) {

		if (t instanceof LTriangleTerm) {
			LTriangleTerm ltriangleTerm = (LTriangleTerm) t;
			int hash = ltriangleTerm.hashcode();

			if (grammar.isStateFinal(ltriangleTerm.q())) {
				addValueToSingleIntCut(cutLTrianglesFrom_i, ltriangleTerm.i(),
						hash);
				addValueToPairedIntCut(cutLTrianglesFrom_h_s,
						ltriangleTerm.h(), ltriangleTerm.s(), hash);
				if (ltriangleTerm.i() == 1) {
					addValueToPairedIntCut(cutLTrianglesFrom_h_s_1,
							ltriangleTerm.h(), ltriangleTerm.s(), hash);
				}
			}

			addValueToSingleIntCut(cutLTrianglesFrom_i_all, ltriangleTerm.i(),
					hash);
		}

		if (t instanceof LTrapTerm) {
			LTrapTerm ltrapTerm = (LTrapTerm) t;
			addValueToPairedIntCut(cutLTrapsFrom_h__s_, ltrapTerm.h_(),
					ltrapTerm.s_(), ltrapTerm.hashcode());
		}

		if (t instanceof RTrapTerm) {
			RTrapTerm rtrapTerm = (RTrapTerm) t;
			addValueToPairedIntCut(cutRTrapsFrom_h__s_, rtrapTerm.h_(),
					rtrapTerm.s_(), rtrapTerm.hashcode());
		}

		if (t instanceof RTriangleTerm) {
			RTriangleTerm rtriangleTerm = (RTriangleTerm) t;
			int hash = rtriangleTerm.hashcode();
			
			addValueToSingleIntCut(cutRTrianglesFrom_j, rtriangleTerm.j(), hash);
			addValueToPairedIntCut(cutRTrianglesFrom_h_q, rtriangleTerm.h(),
					rtriangleTerm.q(), hash);
			if (rtriangleTerm.j() == n) {
				addValueToPairedIntCut(cutRTrianglesFrom_h_q_n,
						rtriangleTerm.h(), rtriangleTerm.q(), hash);
			}
		}
	}

	public void removeFromCuts(int h, Term t) {
		if (t instanceof LTriangleTerm) {
			LTriangleTerm ltriangleTerm = (LTriangleTerm) t;

			if (grammar.isStateFinal(ltriangleTerm.q())) {
				cutLTrianglesFrom_i.get(ltriangleTerm.i()).remove(h);
				cutLTrianglesFrom_h_s.get(ltriangleTerm.h())
						.get(ltriangleTerm.s()).remove(h);
				if (ltriangleTerm.i() == 1) {
					cutLTrianglesFrom_h_s_1.get(ltriangleTerm.h())
							.get(ltriangleTerm.s()).remove(h);
				}

			}

			cutLTrianglesFrom_i_all.get(ltriangleTerm.i()).remove(h);
		}

		if (t instanceof LTrapTerm) {
			LTrapTerm ltrapTerm = (LTrapTerm) t;

			cutLTrapsFrom_h__s_.get(ltrapTerm.h_()).get(ltrapTerm.s_())
					.remove(h);
		}

		if (t instanceof RTrapTerm) {
			RTrapTerm rtrapTerm = (RTrapTerm) t;

			cutRTrapsFrom_h__s_.get(rtrapTerm.h_()).get(rtrapTerm.s_())
					.remove(h);
		}

		if (t instanceof RTriangleTerm) {
			RTriangleTerm rtriangleTerm = (RTriangleTerm) t;

			cutRTrianglesFrom_j.get(rtriangleTerm.j()).remove(h);
			cutRTrianglesFrom_h_q.get(rtriangleTerm.h()).get(rtriangleTerm.q())
					.remove(h);
			if (rtriangleTerm.j() == n) {
				cutRTrianglesFrom_h_q_n.get(rtriangleTerm.h()).get(
						rtriangleTerm.q());
			}
		}
	}
}
