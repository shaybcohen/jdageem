/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.HashMap;
import java.util.Set;

import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.list.IntArrayList;
import cern.colt.function.IntProcedure;

/**
 * @author scohen
 * 
 */
public class EValue {

	private double p;
	private OpenIntDoubleHashMap r;
	private boolean ownCopy;

	public EValue(boolean alloc) {
		if (alloc) {
			r = new OpenIntDoubleHashMap();
		} else {
			r = null;
		}
		p = Semiring.Zero;
		ownCopy = true;
	}

	public EValue() {
		r = new OpenIntDoubleHashMap();
		p = Semiring.Zero;
	}
	
	
	public String toString()
	{
		return "" + p;
	}

	public void copyNow() {
		if (!ownCopy) {
			OpenIntDoubleHashMap r_ = (OpenIntDoubleHashMap) r.clone();
			r = r_;
			ownCopy = true;
		}
	}

	public double r(int i) {
		if (r.containsKey(i)) {
			return r.get(i);
		} else {
			return Chart.semiring.Zero;
		}
	}

	/*
	 * public Set<Integer> rSet() { return r.keySet(); }
	 */

	public IntArrayList getKeyList() {
		return r.keys();
	}

	private static boolean[] changed = new boolean[1];

	public boolean Plus(final EValue v) {

		copyNow();

		changed[0] = false;

		v.r.forEachKey(new IntProcedure() {
			public boolean apply(int i) {
				if (!r.containsKey(i)) {
					setr(i, v.r(i));
					changed[0] = true;
				} else {
					setr(i, Semiring.LogSum(r(i), v.r(i)));
					changed[0] = true;
				}
				return true;
			}
		});

		double p2 = Semiring.LogSum(p, v.p());

		if (p2 != p) {
			p = p2;
			return true;
		}

		return changed[0];
	}

	public void setOne() {
		p = Chart.semiring.One;
	}

	public void setp(double p) {
		this.p = p;
	}

	public void Times(int i, double p2, double r2) {
		EValue v = new EValue();

		v.setp(p2);
		v.setr(i, r2);

		this.Times(v);
	}

	public void Times(double p2) {
		EValue v = new EValue();
		v.setOne();
		v.setp(p2);

		this.Times(v);
	}

	/*
	 * 
	 * public void Times(int i, double p2, double r2) { double r1;
	 * 
	 * if (!r.containsKey(i)) { r1 = Semiring.Zero; } else { r1 = r(i); }
	 * 
	 * setr(i, Semiring.ETimes(p(), p2, r1, r2)); p = Semiring.Times(p(), p2); }
	 */

	public void setr(int i, double v) {
		r.put(i, v);
	}

	public double p() {
		return p;
	}

	public void Times(EValue v) {
		this.Times(p(), v);
	}

	public void Times(double p1, Term t) {
		Times(p1, (EValue) t.secondaryValue());
	}

	public EValue getCopy() {
		EValue v = new EValue(false);
		v.r = r;
		// (OpenIntDoubleHashMap) r.clone();
		v.p = p;
		v.ownCopy = false;

		return v;
	}

	public void printHash() {
		IntArrayList lst = getKeyList();

		for (int i = 0; i < lst.size(); i++) {
			System.err.println(i + " " + (r.get(lst.get(i))));
		}
	}

	public void printHash(FeatureHashToStringInterface hasher) {

		
		IntArrayList lst = getKeyList();

		double v2 = Chart.semiring.One;
		for (int i = 0; i < lst.size(); i++) {
			v2 = Chart.semiring.Times(v2, hasher.getFeatureValue(lst.get(i)));
		}

		System.err.println("p = " + p() + " (sum = " + v2 +")");

		for (int i = 0; i < lst.size(); i++) {
			System.err.println(hasher.getFeatureName(lst.get(i)) + " " + (r.get(lst.get(i))));
		}
	}

	public void Times(final double p1, final EValue v) {

		copyNow();

		// if (v == null) { return; }

		final double p2 = v.p();

		v.r.forEachKey(new IntProcedure() {
			public boolean apply(int i) {
				double r1, r2;
				if (!r.containsKey(i)) {
					r1 = Semiring.Zero;
				} else {
					r1 = r(i);
				}

				r2 = v.r(i);

				setr(i, Semiring.ETimes(p1, p2, r1, r2));

				return true;
			}
		});

		r.forEachKey(new IntProcedure() {
			public boolean apply(int i) {
				double r1, r2;

				if (!v.r.containsKey(i)) {
					r2 = v.r(i);

					r1 = r(i);
					setr(i, Semiring.ETimes(p1, p2, r1, r2));
				}

				return true;

			}
		});

		p = Semiring.Times(p1, p2);
	}

}
