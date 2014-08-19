/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.HashSet;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.function.IntProcedure;

/**
 * @author scohen
 * 
 */
public class DMVGrammar extends SplitHeadAutomatonGrammar {

	private Alphabet alphabet;

	protected HashMap<Integer, HashMap<Integer, Double>> leftAttach;
	protected HashMap<Integer, HashMap<Integer, Double>> rightAttach;

	protected OpenIntDoubleHashMap leftContinueNoChild;
	protected OpenIntDoubleHashMap rightContinueNoChild;

	protected OpenIntDoubleHashMap leftContinueHasChild;
	protected OpenIntDoubleHashMap rightContinueHasChild;

	protected OpenIntDoubleHashMap leftStopNoChild;
	protected OpenIntDoubleHashMap rightStopNoChild;

	protected OpenIntDoubleHashMap leftStopHasChild;
	protected OpenIntDoubleHashMap rightStopHasChild;

	// hash marks for the expectation semiring
	protected static final int HASH_AttachRight = -1;
	protected static final int HASH_AttachLeft = -2;
	protected static final int HASH_Root = -3;
	private static final int HASH_LeftContinueNoChild = -4;
	private static final int HASH_RightContinueNoChild = -5;
	private static final int HASH_LeftContinueHasChild = -6;
	private static final int HASH_RightContinueHasChild = -7;
	private static final int HASH_LeftStopNoChild = -8;
	private static final int HASH_RightStopNoChild = -9;
	private static final int HASH_LeftStopHasChild = -10;
	private static final int HASH_RightStopHasChild = -11;

	protected HashMap<Integer, int[]> hasherMap;

	protected OpenIntDoubleHashMap roots;

	public static final int STATE_InitialLeftOnly = 0;
	public static final int STATE_InitialRightOnly = 1;
	public static final int STATE_InitialRightAndLeft = 2;
	public static final int STATE_L1 = 3;
	public static final int STATE_R1 = 4;
	public static final int STATE_R2 = 5;
	public static final int STATE_L2 = 6;
	public static final int STATE_InitialNone = 7;

	public MyRand rand;

	public static final int[] STATES_flip = { STATE_R1, STATE_R2,
			STATE_InitialLeftOnly, STATE_InitialNone };

	public static final int[] STATES_initial = { STATE_InitialNone,
			STATE_InitialLeftOnly, STATE_InitialRightOnly,
			STATE_InitialRightAndLeft };

	public static final int[] STATES_all = { 0, 1, 2, 3, 4, 5, 6, 7 };

	public static final int[][] STATES_q_right = { {}, { STATE_R1 },
			{ STATE_R2 }, {}, { STATE_R1 }, { STATE_R2 }, {}, {} };

	public static final int[][] STATES_q_left = { { STATE_L1 }, {}, {},
			{ STATE_L1 }, {}, { STATE_L2 }, { STATE_L2 }, {} };

	public DMVGrammar(Alphabet alpha) {
		alphabet = alpha;

		leftAttach = new HashMap<Integer, HashMap<Integer, Double>>();
		rightAttach = new HashMap<Integer, HashMap<Integer, Double>>();

		leftContinueNoChild = new OpenIntDoubleHashMap();
		rightContinueNoChild = new OpenIntDoubleHashMap();

		leftContinueHasChild = new OpenIntDoubleHashMap();
		rightContinueHasChild = new OpenIntDoubleHashMap();

		leftStopNoChild = new OpenIntDoubleHashMap();
		rightStopNoChild = new OpenIntDoubleHashMap();

		leftStopHasChild = new OpenIntDoubleHashMap();
		rightStopHasChild = new OpenIntDoubleHashMap();

		hasherMap = new HashMap<Integer, int[]>();

		roots = new OpenIntDoubleHashMap();

		rand = new MyRand();
	}

	/*
	 * private HashMap<Integer, Double> leftContinueNoChild; private
	 * HashMap<Integer, Double> rightContinueNoChild; private HashMap<Integer,
	 * Double> leftContinueHasChild; private HashMap<Integer, Double>
	 * rightContinueHasChild; private HashMap<Integer, Double> leftStopNoChild;
	 * private HashMap<Integer, Double> rightStopNoChild; private
	 * HashMap<Integer, Double> leftStopHasChild; private HashMap<Integer,
	 * Double> rightStopHasChild;
	 */

	public void cleanup() {
		hasherMap = new HashMap<Integer, int[]>();
	}

	public boolean isFlipState(int state) {
		return (state == STATE_R2 || state == STATE_InitialLeftOnly
				|| state == STATE_InitialNone || state == STATE_R1);
	}

	public Alphabet getAlphabet() {
		return alphabet;
	}

	protected void addPairHash(HashMap<Integer, HashMap<Integer, Double>> h,
			int i1, int i2, double v) {
		HashMap<Integer, Double> h_;
		if (!h.containsKey(i1)) {
			h_ = new HashMap<Integer, Double>();
			h.put(i1, h_);
		} else {
			h_ = h.get(i1);
		}

		h_.put(i2, v);
	}

	protected double getHash(HashMap<Integer, Double> h, int k) {
		Double d = h.get(k);
		if (d == null) {
			return Chart.semiring.Zero;
		}

		return d;
	}

	public void timesAxiomDeltaRight(EValue v, int head, int q, int r, int child) {
		int hash = 0;

		if (statesEqual(q, r, STATE_InitialLeftOnly, STATE_L1)) {
			hash = hasher(HASH_LeftContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_L1, STATE_L1)) {
			hash = hasher(HASH_LeftContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_InitialRightOnly, STATE_R1)) {
			hash = hasher(HASH_RightContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_R1, STATE_R1)) {
			hash = hasher(HASH_RightContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_InitialRightAndLeft, STATE_R2)) {
			hash = hasher(HASH_RightContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_R2, STATE_R2)) {
			hash = hasher(HASH_RightContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_R2, STATE_L2)) {
			hash = hasher(HASH_LeftContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_L2, STATE_L2)) {
			hash = hasher(HASH_LeftContinueHasChild, head);
		}

		double p2 = getAxiomDeltaRight(head, q, r, child);
		double r2 = p2;

		v.Times(hash, p2, r2);
	}

	public void timesAxiomDeltaLeft(EValue v, int head, int q, int r, int child) {
		int hash = 0;

		if (statesEqual(q, r, STATE_InitialLeftOnly, STATE_L1)) {
			hash = hasher(HASH_LeftContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_L1, STATE_L1)) {
			hash = hasher(HASH_LeftContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_InitialRightOnly, STATE_R1)) {
			hash = hasher(HASH_RightContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_R1, STATE_R1)) {
			hash = hasher(HASH_RightContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_InitialRightAndLeft, STATE_R2)) {
			hash = hasher(HASH_RightContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_R2, STATE_R2)) {
			hash = hasher(HASH_RightContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_R2, STATE_L2)) {
			hash = hasher(HASH_LeftContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_L2, STATE_L2)) {
			hash = hasher(HASH_LeftContinueHasChild, head);
		}

		double p2 = getAxiomDeltaLeft(head, q, r, child);
		double r2 = p2;

		v.Times(hash, p2, r2);
	}

	public int hasher(int a, int b, int c) {
		int[] arr = new int[3];
		arr[0] = a;
		arr[1] = b;
		arr[2] = c;

		int h = EisnerSattaChart.hasher(a, b, c, 0, 0);

		if (!hasherMap.containsKey(h)) {
			hasherMap.put(h, arr);
		}

		return h;
	}

	public int hasher(int a, int b) {
		int[] arr = new int[2];
		arr[0] = a;
		arr[1] = b;

		int h = EisnerSattaChart.hasher(a, b, 0, 0, 0);

		if (!hasherMap.containsKey(h)) {
			hasherMap.put(h, arr);
		}

		return h;
	}

	public void timesAxiomAttachmentRight(EValue v, int i, int j, int head,
			int child, int r, int s_) {
		int hash = hasher(HASH_AttachRight, head, child);

		double p2 = getAxiomAttachmentRight(i, j, head, child, r, s_);
		double r2 = p2;

		v.Times(hash, p2, r2);
	}

	public void timesAxiomAttachmentLeft(EValue v, int i, int j, int head,
			int child, int r, int s_) {
		int hash = hasher(HASH_AttachLeft, head, child);

		double p2 = getAxiomAttachmentLeft(i, j, head, child, r, s_);
		double r2 = p2;

		v.Times(hash, p2, r2);
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

			hash = hasher(HASH_LeftStopHasChild, head);
			v.setr(hash, v.p());

			hash = hasher(HASH_RightStopNoChild, head);
			v.setr(hash, v.p());

			break;
		case STATE_InitialRightOnly:
			v.setp(Chart.semiring.Times(safeDouble(leftStopNoChild, head),
					safeDouble(rightStopHasChild, head)));

			hash = hasher(HASH_LeftStopNoChild, head);
			v.setr(hash, v.p());

			hash = hasher(HASH_RightStopHasChild, head);
			v.setr(hash, v.p());

			break;
		case STATE_InitialRightAndLeft:
			v.setp(Chart.semiring.Times(safeDouble(leftStopHasChild, head),
					safeDouble(rightStopHasChild, head)));

			hash = hasher(HASH_LeftStopHasChild, head);
			v.setr(hash, v.p());

			hash = hasher(HASH_RightStopHasChild, head);
			v.setr(hash, v.p());

			break;
		case STATE_InitialNone:
			v.setp(Chart.semiring.Times(safeDouble(leftStopNoChild, head),
					safeDouble(rightStopNoChild, head)));

			hash = hasher(HASH_LeftStopNoChild, head);
			v.setr(hash, v.p());

			hash = hasher(HASH_RightStopNoChild, head);
			v.setr(hash, v.p());

			break;

		default:
			break;
		}

		v2.Times(v2.p(), v);
	}

	public void timesAxiomRoot(EValue v, int i, int head) {
		int hash = hasher(HASH_Root, head, 0);

		double p2 = getAxiomRoot(i, head);
		double r2 = p2;

		v.Times(hash, p2, r2);
	}

	public double getAxiomDeltaRight(int head, int q, int r, int child) {

		Double v = null;

		if (statesEqual(q, r, STATE_InitialLeftOnly, STATE_L1)) {
			v = safeDouble(leftContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_L1, STATE_L1)) {
			v = safeDouble(leftContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_InitialRightOnly, STATE_R1)) {
			v = safeDouble(rightContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_R1, STATE_R1)) {
			v = safeDouble(rightContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_InitialRightAndLeft, STATE_R2)) {
			v = safeDouble(rightContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_R2, STATE_R2)) {
			v = safeDouble(rightContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_R2, STATE_L2)) {
			v = safeDouble(leftContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_L2, STATE_L2)) {
			v = safeDouble(leftContinueHasChild, head);
		}

		if (v == null) {
			return Chart.semiring.Zero;
		}

		return v;
	}

	public double safeDouble(OpenIntDoubleHashMap h, int k) {
		if (!h.containsKey(k)) {
			return Chart.semiring.Zero;
		} else {
			return h.get(k);
		}
	}

	public double noLogSafeDouble(OpenIntDoubleHashMap h, int k) {
		if (!h.containsKey(k)) {
			return 0;
		} else {
			return h.get(k);
		}
	}

	public double safeDouble(HashMap<Integer, HashMap<Integer, Double>> h,
			int k, int i) {
		if (!h.containsKey(k)) {
			return Chart.semiring.Zero;
		}
		if (!h.get(k).containsKey(i)) {
			return Chart.semiring.Zero;
		}
		return h.get(k).get(i);
	}

	public double noLogSafeDouble(HashMap<Integer, HashMap<Integer, Double>> h,
			int k, int i) {
		if (!h.containsKey(k)) {
			return 0;
		}
		if (!h.get(k).containsKey(i)) {
			return 0;
		}
		return h.get(k).get(i);
	}

	public double getAxiomDeltaStart(int head, int q) {
		double v;

		switch (q) {
		case STATE_InitialLeftOnly:
			v = Chart.semiring.Times(safeDouble(leftStopHasChild, head),
					safeDouble(rightStopNoChild, head));
			break;
		case STATE_InitialRightOnly:
			v = Chart.semiring.Times(safeDouble(rightStopHasChild, head),
					safeDouble(leftStopNoChild, head));
			break;
		case STATE_InitialRightAndLeft:
			v = Chart.semiring.Times(safeDouble(rightStopHasChild, head),
					safeDouble(leftStopHasChild, head));
			break;
		case STATE_InitialNone:
			v = Chart.semiring.Times(safeDouble(leftStopNoChild, head),
					safeDouble(rightStopNoChild, head));
			break;

		default:
			v = Chart.semiring.Zero;
			break;
		}

		return v;
	}

	public boolean statesEqual(int q, int r, int q_, int r_) {
		return ((q == q_) && (r == r_));
	}

	public double getAxiomDeltaLeft(int head, int q, int r, int child) {

		Double v = null;

		if (statesEqual(q, r, STATE_InitialLeftOnly, STATE_L1)) {
			v = safeDouble(leftContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_L1, STATE_L1)) {
			v = safeDouble(leftContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_InitialRightOnly, STATE_R1)) {
			v = safeDouble(rightContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_R1, STATE_R1)) {
			v = safeDouble(rightContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_InitialRightAndLeft, STATE_R2)) {
			v = safeDouble(rightContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_R2, STATE_R2)) {
			v = safeDouble(rightContinueHasChild, head);
		}

		if (statesEqual(q, r, STATE_R2, STATE_L2)) {
			v = safeDouble(leftContinueNoChild, head);
		}

		if (statesEqual(q, r, STATE_L2, STATE_L2)) {
			v = safeDouble(leftContinueHasChild, head);
		}

		if (v == null) {
			return Chart.semiring.Zero;
		}

		return v;
	}

	public double getAxiomRoot(int i, int head) {
		return safeDouble(roots, head);
	}

	public double getAxiomAttachmentRight(int i, int j, int head, int child,
			int r, int s_) {

		HashMap<Integer, Double> h = rightAttach.get(head);

		if (h == null) {
			return Chart.semiring.Zero;
		}

		Double v = h.get(child);

		if (v == null) {
			return Chart.semiring.Zero;
		}

		return v;
	}

	public double getAxiomAttachmentLeft(int i, int j, int head, int child,
			int r, int s_) {

		HashMap<Integer, Double> h = leftAttach.get(head);

		if (h == null) {
			return Chart.semiring.Zero;
		}

		Double v = h.get(child);

		if (v == null) {
			return Chart.semiring.Zero;
		}

		return v;
	}

	public int[] getStatesInitial(int head) {
		return STATES_initial;
	}

	public int[] getStatesDeltaRight(int head, int q, int child) {

		return STATES_q_right[q];
	}

	public int[] getStatesDeltaLeft(int head, int q, int child) {

		return STATES_q_left[q];
	}

	public int[] getFlipStates(int head) {
		return STATES_flip;
	}

	public boolean isStateFinal(int q) {
		return ((q == STATE_InitialNone) || (q == STATE_L1) || (q == STATE_R1) || (q == STATE_L2));
	}

	public boolean isStateInitial(int q) {
		return ((q == STATE_InitialRightAndLeft)
				|| (q == STATE_InitialLeftOnly)
				|| (q == STATE_InitialRightOnly) || (q == STATE_InitialNone));
	}

	public void writeAxioms(final PrintWriter br,
			final OpenIntDoubleHashMap hashMap, final String axiomName,
			final String axiomSuffix) {
		hashMap.forEachKey(new IntProcedure() {
			public boolean apply(int key) {
				String head = (String) alphabet.reverseLookup(key);
				double v = Chart.semiring.convertFromSemiring(hashMap.get(key));
				br.println(axiomName + " " + head + axiomSuffix + " : " + v);
				return true;
			}
		});
	}

	public void writeGrammar(String filename) throws IOException {
		PrintWriter br = new PrintWriter(new FileWriter(filename));

		writeAxioms(br, leftContinueNoChild, "leftcontinue", " nochild");
		writeAxioms(br, leftContinueHasChild, "leftcontinue", " haschild");
		writeAxioms(br, rightContinueHasChild, "rightcontinue", " haschild");
		writeAxioms(br, rightContinueNoChild, "rightcontinue", " nochild");
		writeAxioms(br, leftStopNoChild, "leftstop", " nochild");
		writeAxioms(br, leftStopHasChild, "leftstop", " haschild");
		writeAxioms(br, rightStopHasChild, "rightstop", " haschild");
		writeAxioms(br, rightStopNoChild, "rightstop", " nochild");
		writeAxioms(br, roots, "root", "");

		for (int key : leftAttach.keySet()) {
			String head = (String) alphabet.reverseLookup(key);

			for (int key2 : leftAttach.get(key).keySet()) {
				String child = (String) alphabet.reverseLookup(key2);
				double v = Chart.semiring.convertFromSemiring(leftAttach.get(
						key).get(key2));

				br.println("leftattach " + child + " <- " + head + " : " + v);
			}
		}

		for (int key : rightAttach.keySet()) {
			String head = (String) alphabet.reverseLookup(key);

			for (int key2 : rightAttach.get(key).keySet()) {
				String child = (String) alphabet.reverseLookup(key2);
				double v = Chart.semiring.convertFromSemiring(rightAttach.get(
						key).get(key2));

				br.println("rightattach " + head + " -> " + child + " : " + v);
			}
		}

		br.close();
	}

	public double pickMax(double m, double c, double cc) {
		if ((c > 0) && (cc < 0)) {
			if (m > -c / cc) {
				return -c / cc;
			}
		}

		return m;
	}

	public double pickMin(double m, double c, double cc) {
		if ((c > 0) && (cc > 0)) {
			if (m < -c / cc) {
				return -c / cc;
			}
		}

		return m;
	}

	public class MyRand extends java.util.Random {
		public synchronized double nextUniform() {

			long l = ((long) (next(26)) << 27) + next(27);
			return l / (double) (1L << 53);
		}

		public synchronized double nextGamma(double alpha, double beta,
				double lambda) {
			double gamma = 0;
			if (alpha <= 0 || beta <= 0) {
				throw new IllegalArgumentException(
						"alpha and beta must be strictly positive.");
			}
			if (alpha < 1) {
				double b, p;
				boolean flag = false;
				b = 1 + alpha * Math.exp(-1);
				while (!flag) {
					p = b * nextUniform();
					if (p > 1) {
						gamma = -Math.log((b - p) / alpha);
						if (nextUniform() <= Math.pow(gamma, alpha - 1))
							flag = true;
					} else {
						gamma = Math.pow(p, 1 / alpha);
						if (nextUniform() <= Math.exp(-gamma))
							flag = true;
					}
				}
			} else if (alpha == 1) {
				gamma = -Math.log(nextUniform());
			} else {
				double y = -Math.log(nextUniform());
				while (nextUniform() > Math.pow(y * Math.exp(1 - y), alpha - 1))
					y = -Math.log(nextUniform());
				gamma = alpha * y;
			}
			return beta * gamma + lambda;
		}

		public double[] randDirichlet(int size, double alpha) {
			double[] v = new double[size];

			double s = 0.0;

			for (int i = 0; i < v.length; i++) {
				v[i] = nextGamma(alpha);

				s = s + v[i];
			}

			for (int i = 0; i < v.length; i++) {
				v[i] = v[i] / s;
			}

			return v;
		}

		public synchronized double nextGamma(double alpha) {
			return nextGamma(alpha, 1, 0);
		}
	}

	public void setToDirichletInitializer(SentenceCorpus corpus, double alpha) {

		System.err
				.println("Using harmonic as a skeleton, resetting to random (Dirichlet = "
						+ alpha + ")");
		setToHarmonicInitializer(corpus);

		for (Integer h : leftAttach.keySet()) {
			HashMap<Integer, Double> la = leftAttach.get(h);

			double[] randArray = rand.randDirichlet(la.size(), alpha);

			Set<Integer> mySet = la.keySet();

			int k = 0;
			for (Integer i : mySet) {
				la.put(i, randArray[k]);
				k++;
			}
		}

		for (Integer h : rightAttach.keySet()) {
			HashMap<Integer, Double> la = rightAttach.get(h);

			double[] randArray = rand.randDirichlet(la.size(), alpha);

			Set<Integer> mySet = la.keySet();

			int k = 0;
			for (Integer i : mySet) {
				la.put(i, randArray[k]);
				k++;
			}
		}

		IntArrayList arr1 = leftContinueNoChild.keys();

		for (int i = 0; i < arr1.size(); i++) {
			int key = arr1.get(i);

			double[] randArray = rand.randDirichlet(2, alpha);

			leftContinueNoChild.put(key, randArray[0]);
			leftStopNoChild.put(key, randArray[1]);
		}

		IntArrayList arr2 = rightContinueNoChild.keys();

		for (int i = 0; i < arr2.size(); i++) {
			int key = arr2.get(i);

			double[] randArray = rand.randDirichlet(2, alpha);

			rightContinueNoChild.put(key, randArray[0]);
			rightStopNoChild.put(key, randArray[1]);
		}

		IntArrayList arr3 = leftContinueHasChild.keys();

		for (int i = 0; i < arr3.size(); i++) {
			int key = arr3.get(i);

			double[] randArray = rand.randDirichlet(2, alpha);

			leftContinueHasChild.put(key, randArray[0]);
			leftStopHasChild.put(key, randArray[1]);
		}

		IntArrayList arr4 = rightContinueHasChild.keys();

		for (int i = 0; i < arr4.size(); i++) {
			int key = arr4.get(i);

			double[] randArray = rand.randDirichlet(2, alpha);

			rightContinueHasChild.put(key, randArray[0]);
			rightStopHasChild.put(key, randArray[1]);
		}
	}

	public void setToHarmonicInitializer(SentenceCorpus corpus) {

		System.err.println("Using harmonic initializer...");

		DMVGrammar cc = new DMVGrammar(alphabet);

		for (int l = 0; l < corpus.size(); l++) {
			SentenceDocument doc = corpus.get(l);
			int n = doc.length();

			double[] er = new double[n];
			for (int i = 0; i < er.length; i++) {
				er[i] = 0;
			}

			double[] el = new double[n];
			for (int i = 0; i < el.length; i++) {
				el[i] = 0;
			}

			for (int i = 0; i < doc.length(); i++) {
				noLogSafeMapInc(roots, doc.wordAt(i), 1.0 / doc.length());
			}

			for (int j = 0; j < doc.length(); j++) {
				double sum = 0.0;

				for (int i = 0; i < doc.length(); i++) {
					if (i != j) {
						sum += 1.0 / (Math.abs(i - j));
					}
				}

				for (int i = 0; i < j; i++) {
					double x = ((n - 1.0) / n) * (1.0 / sum) * (1.0 / (j - i));
					er[i] += x;
					noLogSafeMapInc(rightAttach, doc.wordAt(i), doc.wordAt(j),
							x);
				}

				for (int i = j + 1; i < n; i++) {
					double x = ((n - 1.0) / n) * (1.0 / sum) * (1.0 / (i - j));
					el[i] += x;
					noLogSafeMapInc(leftAttach, doc.wordAt(i), doc.wordAt(j), x);
				}
			}

			for (int i = 0; i < n; i++) {
				if (el[i] > 0) {
					noLogSafeMapInc(leftContinueNoChild, doc.wordAt(i), 0.0);
					noLogSafeMapInc(cc.leftContinueNoChild, doc.wordAt(i), 1.0);
					noLogSafeMapInc(leftContinueHasChild, doc.wordAt(i), el[i]);
					noLogSafeMapInc(cc.leftContinueHasChild, doc.wordAt(i),
							-1.0);
					noLogSafeMapInc(leftStopNoChild, doc.wordAt(i), 1.0);
					noLogSafeMapInc(cc.leftStopNoChild, doc.wordAt(i), -1.0);
					noLogSafeMapInc(leftStopHasChild, doc.wordAt(i), 0.0);
					noLogSafeMapInc(cc.leftStopHasChild, doc.wordAt(i), 1.0);
				} else {
					noLogSafeMapInc(leftStopNoChild, doc.wordAt(i), 1.0);
				}

				if (er[i] > 0) {
					noLogSafeMapInc(rightContinueNoChild, doc.wordAt(i), 0.0);
					noLogSafeMapInc(cc.rightContinueNoChild, doc.wordAt(i), 1.0);
					noLogSafeMapInc(rightContinueHasChild, doc.wordAt(i), er[i]);
					noLogSafeMapInc(cc.rightContinueHasChild, doc.wordAt(i),
							-1.0);
					noLogSafeMapInc(rightStopNoChild, doc.wordAt(i), 1.0);
					noLogSafeMapInc(cc.rightStopNoChild, doc.wordAt(i), -1.0);
					noLogSafeMapInc(rightStopHasChild, doc.wordAt(i), 0.0);
					noLogSafeMapInc(cc.rightStopHasChild, doc.wordAt(i), 1.0);
				} else {
					noLogSafeMapInc(rightStopNoChild, doc.wordAt(i), 1.0);
				}

			}
		}

		IntArrayList arr = roots.keys();

		for (int i = 0; i < arr.size(); i++) {
			int a = arr.get(i);

			noLogSafeMapInc(roots, a, 0.1);

			for (int j = 0; j < arr.size(); j++) {
				int b = arr.get(j);

				noLogSafeMapInc(leftAttach, a, b, 0.1);
				noLogSafeMapInc(rightAttach, a, b, 0.1);
			}

			noLogSafeMapInc(leftContinueNoChild, a, 0.1);
			noLogSafeMapInc(leftStopNoChild, a, 0.1);
			noLogSafeMapInc(leftContinueHasChild, a, 0.1);
			noLogSafeMapInc(leftStopHasChild, a, 0.1);

			noLogSafeMapInc(rightContinueNoChild, a, 0.1);
			noLogSafeMapInc(rightStopNoChild, a, 0.1);
			noLogSafeMapInc(rightContinueHasChild, a, 0.1);
			noLogSafeMapInc(rightStopHasChild, a, 0.1);
		}

		double min_e = 0.0;
		double max_e = 1.0;

		for (int i = 0; i < arr.size(); i++) {
			int a = arr.get(i);

			max_e = pickMax(max_e, noLogSafeDouble(roots, a),
					noLogSafeDouble(cc.roots, a));
			max_e = pickMax(max_e, noLogSafeDouble(leftContinueNoChild, a),
					noLogSafeDouble(cc.leftContinueNoChild, a));
			max_e = pickMax(max_e, noLogSafeDouble(leftStopNoChild, a),
					noLogSafeDouble(cc.leftStopNoChild, a));
			max_e = pickMax(max_e, noLogSafeDouble(leftContinueHasChild, a),
					noLogSafeDouble(cc.leftContinueHasChild, a));
			max_e = pickMax(max_e, noLogSafeDouble(leftStopHasChild, a),
					noLogSafeDouble(cc.leftStopHasChild, a));

			min_e = pickMin(min_e, noLogSafeDouble(roots, a),
					noLogSafeDouble(cc.roots, a));
			min_e = pickMin(min_e, noLogSafeDouble(leftContinueNoChild, a),
					noLogSafeDouble(cc.leftContinueNoChild, a));
			min_e = pickMin(min_e, noLogSafeDouble(leftStopNoChild, a),
					noLogSafeDouble(cc.leftStopNoChild, a));
			min_e = pickMin(min_e, noLogSafeDouble(leftContinueHasChild, a),
					noLogSafeDouble(cc.leftContinueHasChild, a));
			min_e = pickMin(min_e, noLogSafeDouble(leftStopHasChild, a),
					noLogSafeDouble(cc.leftStopHasChild, a));

			max_e = pickMax(max_e, noLogSafeDouble(rightContinueNoChild, a),
					noLogSafeDouble(cc.rightContinueNoChild, a));
			max_e = pickMax(max_e, noLogSafeDouble(rightStopNoChild, a),
					noLogSafeDouble(cc.rightStopNoChild, a));
			max_e = pickMax(max_e, noLogSafeDouble(rightContinueHasChild, a),
					noLogSafeDouble(cc.rightContinueHasChild, a));
			max_e = pickMax(max_e, noLogSafeDouble(rightStopHasChild, a),
					noLogSafeDouble(cc.rightStopHasChild, a));

			min_e = pickMin(min_e, noLogSafeDouble(rightContinueNoChild, a),
					noLogSafeDouble(cc.rightContinueNoChild, a));
			min_e = pickMin(min_e, noLogSafeDouble(rightStopNoChild, a),
					noLogSafeDouble(cc.rightStopNoChild, a));
			min_e = pickMin(min_e, noLogSafeDouble(rightContinueHasChild, a),
					noLogSafeDouble(cc.rightContinueHasChild, a));
			min_e = pickMin(min_e, noLogSafeDouble(rightStopHasChild, a),
					noLogSafeDouble(cc.rightStopHasChild, a));

			for (int j = 0; j < arr.size(); j++) {
				int b = arr.get(j);

				max_e = pickMax(max_e, noLogSafeDouble(leftAttach, a, b),
						noLogSafeDouble(cc.leftAttach, a, b));
				max_e = pickMax(max_e, noLogSafeDouble(rightAttach, a, b),
						noLogSafeDouble(cc.rightAttach, a, b));

				min_e = pickMin(min_e, noLogSafeDouble(leftAttach, a, b),
						noLogSafeDouble(cc.leftAttach, a, b));
				min_e = pickMin(min_e, noLogSafeDouble(rightAttach, a, b),
						noLogSafeDouble(cc.rightAttach, a, b));
			}
		}

		double pr_first_kid = 0.9 * max_e + 0.1 * min_e;

		for (int i = 0; i < arr.size(); i++) {
			int a = arr.get(i);

			noLogSafeMapInc(roots, a, noLogSafeDouble(cc.roots, a)
					* pr_first_kid);

			for (int j = 0; j < arr.size(); j++) {
				int b = arr.get(j);

				noLogSafeMapInc(leftAttach, a, b,
						noLogSafeDouble(cc.leftAttach, a, b) * pr_first_kid);
				noLogSafeMapInc(rightAttach, a, b,
						noLogSafeDouble(cc.rightAttach, a, b) * pr_first_kid);
			}

			noLogSafeMapInc(leftContinueNoChild, a,
					noLogSafeDouble(cc.leftContinueNoChild, a) * pr_first_kid);
			noLogSafeMapInc(leftStopNoChild, a,
					noLogSafeDouble(cc.leftStopNoChild, a) * pr_first_kid);
			noLogSafeMapInc(leftContinueHasChild, a,
					noLogSafeDouble(cc.leftContinueHasChild, a) * pr_first_kid);
			noLogSafeMapInc(leftStopHasChild, a,
					noLogSafeDouble(cc.leftStopHasChild, a) * pr_first_kid);

			noLogSafeMapInc(rightContinueNoChild, a,
					noLogSafeDouble(cc.rightContinueNoChild, a) * pr_first_kid);
			noLogSafeMapInc(rightStopNoChild, a,
					noLogSafeDouble(cc.rightStopNoChild, a) * pr_first_kid);
			noLogSafeMapInc(rightContinueHasChild, a,
					noLogSafeDouble(cc.rightContinueHasChild, a) * pr_first_kid);
			noLogSafeMapInc(rightStopHasChild, a,
					noLogSafeDouble(cc.rightStopHasChild, a) * pr_first_kid);
		}

		for (int i = 0; i < arr.size(); i++) {
			int a = arr.get(i);

			safeMapSet(roots, a, Math.log(noLogSafeDouble(roots, a)));

			for (int j = 0; j < arr.size(); j++) {
				int b = arr.get(j);

				safeMapSet(leftAttach, a, b,
						Math.log(noLogSafeDouble(leftAttach, a, b)));
				safeMapSet(rightAttach, a, b,
						Math.log(noLogSafeDouble(rightAttach, a, b)));
			}

			safeMapSet(leftContinueNoChild, a,
					Math.log(noLogSafeDouble(leftContinueNoChild, a)));
			safeMapSet(leftStopNoChild, a,
					Math.log(noLogSafeDouble(leftStopNoChild, a)));
			safeMapSet(leftContinueHasChild, a,
					Math.log(noLogSafeDouble(leftContinueHasChild, a)));
			safeMapSet(leftStopHasChild, a,
					Math.log(noLogSafeDouble(leftStopHasChild, a)));

			safeMapSet(rightContinueNoChild, a,
					Math.log(noLogSafeDouble(rightContinueNoChild, a)));
			safeMapSet(rightStopNoChild, a,
					Math.log(noLogSafeDouble(rightStopNoChild, a)));
			safeMapSet(rightContinueHasChild, a,
					Math.log(noLogSafeDouble(rightContinueHasChild, a)));
			safeMapSet(rightStopHasChild, a,
					Math.log(noLogSafeDouble(rightStopHasChild, a)));

		}

		normalizeGrammar();
	}

	public void readGrammar(String filename, SentenceCorpus corpus)
			throws IOException {
		// read grammar here from file

		if (filename.equals("@harmonic@")) {
			setToHarmonicInitializer(corpus);
			// writeGrammar("/tmp/harmonic");
			return;
		}

		if (filename.startsWith("@dirichlet=")) {
			if (!filename.endsWith("@")) {
				System.err
						.println("Could not parse Dirichlet initializer alpha value. Setting to 1.");

				setToDirichletInitializer(corpus, 1.0);
			} else {

				StringTokenizer tokenizer = new StringTokenizer(filename, "=");

				if (tokenizer.countTokens() != 2) {
					System.err
							.println("Could not parse Dirichlet initializer alpha value. Setting to 1.");

					setToDirichletInitializer(corpus, 1.0);
				} else {
					tokenizer.nextToken();

					String s = tokenizer.nextToken();

					String alphaString = s.substring(0, s.length() - 1);

					setToDirichletInitializer(corpus,
							Double.parseDouble(alphaString));
				}
			}

			return;
		}

		System.err.println("Reading " + filename);
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String os;

		while ((os = br.readLine()) != null) {
			double v = 0;

			if (!os.startsWith("//") && !os.equals("")) {
				StringTokenizer st = new StringTokenizer(os, ":");

				if (st.countTokens() != 2) {
					ioReadError(os,
							"wrong number of tokens in line. Is this file in the right format?");
				} else {
					String axiom = st.nextToken();

					try {
						v = Chart.semiring.convertToSemiring(Double
								.parseDouble(st.nextToken()));
					}

					catch (NumberFormatException e) {
						ioReadError(os, "could not parse " + st.nextToken()
								+ " as number. Skipping.");
						continue;
					}

					st = new StringTokenizer(axiom, " ");

					if (st.countTokens() == 2) {
						String cmd = st.nextToken();
						String lex = st.nextToken();

						if (cmd.equals("root")) {
							roots.put(alphabet.lookupIndex(lex), v);
						}
					} else if (st.countTokens() == 4) {
						String cmd = st.nextToken();
						String lex1 = st.nextToken();
						String arrow = st.nextToken();
						String lex2 = st.nextToken();

						if (cmd.equals("rightattach")) {
							if (arrow.equals("->")) {
								// push head=lex1, child=lex2, v to grammar
								addPairHash(rightAttach,
										alphabet.lookupIndex(lex1),
										alphabet.lookupIndex(lex2), v);
							} else {
								ioReadError(os,
										"wrong direction of arrow (should be ->)");
							}
						}

						if (cmd.equals("leftattach")) {
							if (arrow.equals("<-")) {
								// push head=lex2, child=lex1, v to grammar
								addPairHash(leftAttach,
										alphabet.lookupIndex(lex2),
										alphabet.lookupIndex(lex1), v);
							} else {
								ioReadError(os,
										"wrong direction of arrow (should be <-)");
							}
						}

					} else if (st.countTokens() == 3) {
						String cmd = st.nextToken();
						String lex = st.nextToken();
						String flag = st.nextToken();

						boolean cmdParsed = false;

						if (cmd.equals("rightcontinue")) {
							if (flag.equals("nochild")) {
								cmdParsed = true;

								// push rightcontinue, nochild, lex, v to
								// grammar
								rightContinueNoChild.put(
										alphabet.lookupIndex(lex), v);

							} else if (flag.equals("haschild")) {
								cmdParsed = true;

								// push rightcontinue, haschild, lex, v to
								// grammar
								rightContinueHasChild.put(
										alphabet.lookupIndex(lex), v);

							}
						}

						if (cmd.equals("leftcontinue")) {
							if (flag.equals("nochild")) {
								cmdParsed = true;
								// push leftcontinue, nochild, lex, v to grammar
								leftContinueNoChild.put(
										alphabet.lookupIndex(lex), v);

							} else if (flag.equals("haschild")) {
								cmdParsed = true;
								// push leftcontinue, haschild, lex, v to
								// grammar
								leftContinueHasChild.put(
										alphabet.lookupIndex(lex), v);
							}
						}

						if (cmd.equals("rightstop")) {
							if (flag.equals("nochild")) {
								cmdParsed = true;
								// push rightstop, nochild, lex, v to grammar
								rightStopNoChild.put(alphabet.lookupIndex(lex),
										v);

							} else if (flag.equals("haschild")) {
								cmdParsed = true;
								// push rightstop, haschild, lex, v to grammar
								rightStopHasChild.put(
										alphabet.lookupIndex(lex), v);

							}
						}

						if (cmd.equals("leftstop")) {
							if (flag.equals("nochild")) {
								cmdParsed = true;
								// push leftstop, nochild, lex, v to grammar
								leftStopNoChild.put(alphabet.lookupIndex(lex),
										v);

							} else if (flag.equals("haschild")) {
								cmdParsed = true;
								// push leftstop, haschild, lex, v to grammar
								leftStopHasChild.put(alphabet.lookupIndex(lex),
										v);
							}
						}

						if (!cmdParsed) {
							ioReadError(os, "unknown parameter");
						}
					} else {
						ioReadError(os, "wrong number of tokens");
					}
				}
			}
		}
	}

	private void ioReadError(String os, String err) {
		System.err.println("Cannot parse " + os + ": " + err
				+ ". Ignoring line.");
	}

	public void setAxiomDeltaRight(int head, int q, int r, int child, double v) {

		if (isStateInitial(q) || (q == STATE_R2)) {
			switch (r) {
			case STATE_L1:
				leftContinueNoChild.put(head, v);
				break;
			case STATE_R1:
				rightContinueNoChild.put(head, v);
				break;
			case STATE_R2:
				rightContinueNoChild.put(head, v);
				break;
			case STATE_L2:
				leftContinueNoChild.put(head, v);
				break;
			}
		} else {
			switch (r) {
			case STATE_L1:
				leftContinueHasChild.put(head, v);
				break;
			case STATE_R1:
				rightContinueHasChild.put(head, v);
				break;
			case STATE_R2:
				rightContinueHasChild.put(head, v);
				break;
			case STATE_L2:
				leftContinueHasChild.put(head, v);
				break;
			}

		}
	}

	public void setAxiomDeltaLeft(int head, int q, int r, int child, double v) {

		if (isStateInitial(q)) {
			switch (r) {
			case STATE_L1:
				leftContinueNoChild.put(head, v);
				break;
			case STATE_R1:
				rightContinueNoChild.put(head, v);
				break;
			case STATE_R2:
				rightContinueNoChild.put(head, v);
				break;
			case STATE_L2:
				leftContinueNoChild.put(head, v);
				break;
			}
		} else {
			switch (r) {
			case STATE_L1:
				leftContinueHasChild.put(head, v);
				break;
			case STATE_R1:
				rightContinueHasChild.put(head, v);
				break;
			case STATE_R2:
				rightContinueHasChild.put(head, v);
				break;
			case STATE_L2:
				leftContinueHasChild.put(head, v);
				break;
			}

		}

	}

	public void setAxiomAttachmentRight(int head, int child, int r, int s_,
			double v) {

		HashMap<Integer, Double> h = rightAttach.get(head);

		if (h == null) {
			h = new HashMap<Integer, Double>();
			rightAttach.put(head, h);
		}

		h.put(child, v);
	}

	public void setAxiomAttachmentLeft(int head, int child, int r, int s_,
			double v) {

		HashMap<Integer, Double> h = leftAttach.get(head);

		if (h == null) {
			h = new HashMap<Integer, Double>();
			leftAttach.put(head, h);
		}

		h.put(child, v);

	}

	public void normalizeHash(HashMap<Integer, HashMap<Integer, Double>> hash) {
		Set<Integer> set = hash.keySet();

		for (int i : set) {
			HashMap<Integer, Double> h = hash.get(i);

			double v = Chart.semiring.Zero;

			for (int j : h.keySet()) {
				v = Chart.semiring.LogSum(v, h.get(j));
			}

			for (int j : h.keySet()) {
				h.put(j, Chart.semiring.LogDivide(h.get(j), v));
			}
		}
	}

	public void normalizeTwoHashes(OpenIntDoubleHashMap hash1,
			OpenIntDoubleHashMap hash2) {

		HashSet<Integer> set = new HashSet<Integer>();

		IntArrayList setList = new IntArrayList();
		hash1.keys(setList);

		for (int i = 0; i < setList.size(); i++) {
			set.add(setList.get(i));
		}

		setList.clear();
		hash2.keys(setList);

		for (int i = 0; i < setList.size(); i++) {
			set.add(setList.get(i));
		}

		for (int i : set) {
			double v1 = safeDouble(hash1, i);
			double v2 = safeDouble(hash2, i);
			double v = Chart.semiring.LogSum(v1, v2);

			hash1.put(i, Chart.semiring.LogDivide(v1, v));
			hash2.put(i, Chart.semiring.LogDivide(v2, v));
		}
	}

	public void normalizeGrammar() {
		normalizeHash(leftAttach);
		normalizeHash(rightAttach);
		normalizeTwoHashes(leftContinueNoChild, leftStopNoChild);
		normalizeTwoHashes(rightContinueNoChild, rightStopNoChild);
		normalizeTwoHashes(leftContinueHasChild, leftStopHasChild);
		normalizeTwoHashes(rightContinueHasChild, rightStopHasChild);

		final double[] v = new double[1];
		// Chart.Semiring.Zero;

		v[0] = Chart.semiring.Zero;

		roots.forEachKey(new IntProcedure() {
			public boolean apply(int i) {
				v[0] = Chart.semiring.LogSum(v[0], roots.get(i));
				return true;
			}
		});

		/*
		 * for (int i : roots.keySet()) { v = Chart.Semiring.LogSum(v,
		 * roots.get(i)); }
		 */

		roots.forEachKey(new IntProcedure() {
			public boolean apply(int i) {
				roots.put(i, Chart.semiring.LogDivide(roots.get(i), v[0]));
				return true;
			}
		});

		/*
		 * for (int i : roots.keySet()) { roots.put(i,
		 * Chart.Semiring.LogDivide(roots.get(i), v)); }
		 */
	}

	public void setAxiomRoot(int head, double v) {

		roots.put(head, v);

	}

	public void setAxiomDeltaStart(int head, int q, double v) {

		// TODO, need to do this, there is an issue with factorizing v
		assert false;
	}

	public void safeMapInc(HashMap<Integer, HashMap<Integer, Double>> hash,
			int a, int b, double v) {
		if (!hash.containsKey(a)) {
			hash.put(a, new HashMap<Integer, Double>());
		}

		if (!hash.get(a).containsKey(b)) {
			hash.get(a).put(b, v);
		} else {
			hash.get(a).put(b, Chart.semiring.LogSum(hash.get(a).get(b), v));
		}
	}

	public void safeMapInc(OpenIntDoubleHashMap hash, int a, double v) {

		if (!hash.containsKey(a)) {
			hash.put(a, v);
		} else {
			hash.put(a, Chart.semiring.LogSum(hash.get(a), v));
		}
	}

	public void noLogSafeMapInc(
			HashMap<Integer, HashMap<Integer, Double>> hash, int a, int b,
			double v) {
		if (!hash.containsKey(a)) {
			hash.put(a, new HashMap<Integer, Double>());
		}

		if (!hash.get(a).containsKey(b)) {
			hash.get(a).put(b, v);
		} else {
			hash.get(a).put(b, hash.get(a).get(b) + v);
		}
	}

	public void noLogSafeMapInc(OpenIntDoubleHashMap hash, int a, double v) {

		if (!hash.containsKey(a)) {
			hash.put(a, v);
		} else {
			hash.put(a, hash.get(a) + v);
		}
	}

	public void safeMapSet(HashMap<Integer, HashMap<Integer, Double>> hash,
			int a, int b, double v) {
		if (!hash.containsKey(a)) {
			hash.put(a, new HashMap<Integer, Double>());
		}

		hash.get(a).put(b, v);
	}

	public void safeMapSet(OpenIntDoubleHashMap hash, int a, double v) {

		hash.put(a, v);
	}

	public void setCounts(EValue v2) {
		if (v2 == null) {
			return;
		}

		IntArrayList lst = v2.getKeyList();

		// Set<Integer> set = v2.rSet();

		for (int i = 0; i < lst.size(); i++) {
			int h = lst.get(i);
			int[] arr = hasherMap.get(h);
			double v = Chart.semiring.LogDivide(v2.r(h), v2.p());

			switch (arr[0]) {
			case HASH_AttachRight:
				// System.err.println("attachright "
				// + alphabet.reverseLookup(arr[1]) + " "
				// + alphabet.reverseLookup(arr[2]) + " " + v);
				safeMapSet(rightAttach, arr[1], arr[2], v);
				break;
			case HASH_AttachLeft:
				// System.err.println("attachleft "
				// + alphabet.reverseLookup(arr[1]) + " "
				// + alphabet.reverseLookup(arr[2]) + " " + v);
				safeMapSet(leftAttach, arr[1], arr[2], v);
				break;
			case HASH_Root:
				// System.err.println("root " + alphabet.reverseLookup(arr[1])
				// + " " + v);
				safeMapSet(roots, arr[1], v);
				break;
			case HASH_LeftContinueNoChild:
				// System.err.println("leftcontinue_nochild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapSet(leftContinueNoChild, arr[1], v);
				break;
			case HASH_RightContinueNoChild:
				// System.err.println("rightcontinue_nochild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapSet(rightContinueNoChild, arr[1], v);
				break;
			case HASH_LeftContinueHasChild:
				// System.err.println("leftcontinue_haschild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapSet(leftContinueHasChild, arr[1], v);
				break;
			case HASH_RightContinueHasChild:
				// System.err.println("rightcontinue_haschild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapSet(rightContinueHasChild, arr[1], v);
				break;
			case HASH_LeftStopNoChild:
				// System.err.println("leftstop_nochild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapSet(leftStopNoChild, arr[1], v);
				break;
			case HASH_RightStopNoChild:
				// System.err.println("rightstop_nochild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapSet(rightStopNoChild, arr[1], v);
				break;
			case HASH_LeftStopHasChild:
				// System.err.println("leftstop_haschild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapSet(leftStopHasChild, arr[1], v);
				break;
			case HASH_RightStopHasChild:
				// System.err.println("rightstop_haschild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapSet(rightStopHasChild, arr[1], v);
				break;
			}
		}
	}

	public void increaseCounts(EValue v2, DMVGrammar g) {
		if (v2 == null) {
			return;
		}

		IntArrayList lst = v2.getKeyList();

		// Set<Integer> set = v2.rSet();

		for (int i = 0; i < lst.size(); i++) {
			int h = lst.get(i);
			int[] arr = hasherMap.get(h);
			double v = Chart.semiring.LogDivide(v2.r(h), v2.p());

			switch (arr[0]) {
			case HASH_AttachRight:
				// System.err.println("attachright "
				// + alphabet.reverseLookup(arr[1]) + " "
				// + alphabet.reverseLookup(arr[2]) + " " + v);
				safeMapInc(g.rightAttach, arr[1], arr[2], v);
				break;
			case HASH_AttachLeft:
				// System.err.println("attachleft "
				// + alphabet.reverseLookup(arr[1]) + " "
				// + alphabet.reverseLookup(arr[2]) + " " + v);
				safeMapInc(g.leftAttach, arr[1], arr[2], v);
				break;
			case HASH_Root:
				// System.err.println("root " + alphabet.reverseLookup(arr[1])
				// + " " + v);
				safeMapInc(g.roots, arr[1], v);
				break;
			case HASH_LeftContinueNoChild:
				// System.err.println("leftcontinue_nochild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapInc(g.leftContinueNoChild, arr[1], v);
				break;
			case HASH_RightContinueNoChild:
				// System.err.println("rightcontinue_nochild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapInc(g.rightContinueNoChild, arr[1], v);
				break;
			case HASH_LeftContinueHasChild:
				// System.err.println("leftcontinue_haschild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapInc(g.leftContinueHasChild, arr[1], v);
				break;
			case HASH_RightContinueHasChild:
				// System.err.println("rightcontinue_haschild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapInc(g.rightContinueHasChild, arr[1], v);
				break;
			case HASH_LeftStopNoChild:
				// System.err.println("leftstop_nochild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapInc(g.leftStopNoChild, arr[1], v);
				break;
			case HASH_RightStopNoChild:
				// System.err.println("rightstop_nochild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapInc(g.rightStopNoChild, arr[1], v);
				break;
			case HASH_LeftStopHasChild:
				// System.err.println("leftstop_haschild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapInc(g.leftStopHasChild, arr[1], v);
				break;
			case HASH_RightStopHasChild:
				// System.err.println("rightstop_haschild "
				// + alphabet.reverseLookup(arr[1]) + " " + v);
				safeMapInc(g.rightStopHasChild, arr[1], v);
				break;
			}
		}
	}

	public double getLeftAttach(int h, int d) {
		if (leftAttach.get(h) == null) {
			return Chart.semiring.Zero;
		} else {
			if (leftAttach.get(h).get(d) == null) {
				return Chart.semiring.Zero;
			} else {
				return leftAttach.get(h).get(d);
			}
		}
	}

	public double getRightAttach(int h, int d) {
		if (rightAttach.get(h) == null) {
			return Chart.semiring.Zero;
		} else {
			if (rightAttach.get(h).get(d) == null) {
				return Chart.semiring.Zero;
			} else {
				return rightAttach.get(h).get(d);
			}
		}
	}

	public double getLeftContinue(int h, boolean stop, boolean hasChild) {
		if (stop) {
			if (hasChild) {
				return leftStopHasChild.get(h);
			} else {
				return leftStopNoChild.get(h);
			}
		} else {
			if (hasChild) {
				return leftContinueHasChild.get(h);
			} else {
				return leftContinueNoChild.get(h);
			}
		}
	}

	public double getRightContinue(int h, boolean stop, boolean hasChild) {
		if (stop) {
			if (hasChild) {
				return rightStopHasChild.get(h);
			} else {
				return rightStopNoChild.get(h);
			}
		} else {
			if (hasChild) {
				return rightContinueHasChild.get(h);
			} else {
				return rightContinueNoChild.get(h);
			}
		}
	}

	public double getRoot(int h) {
		if (roots.containsKey(h)) {
			return roots.get(h);
		} else {
			return Chart.semiring.Zero;
		}
	}

	public String PCFGNT(String nt, int h) {
		return nt + "^" + alphabet.reverseLookup(h);
	}

	public void writePCFGBinaryRules(PrintStream printer, String N, String L,
			String R, int h, int c) {

		printer.println(PCFGNT("N", h)
				+ " "
				+ PCFGNT(N, c)
				+ " "
				+ PCFGNT(L, h)
				+ " "
				+ Math.exp(leftAttach.get(h).get(c)
						+ leftContinueNoChild.get(h) + rightStopNoChild.get(h)));

		printer.println(PCFGNT("N", h)
				+ " "
				+ PCFGNT(R, h)
				+ " "
				+ PCFGNT(N, c)
				+ " "
				+ Math.exp(rightAttach.get(h).get(c)
						+ rightContinueNoChild.get(h)));

		printer.println(PCFGNT("R", h)
				+ " "
				+ PCFGNT(N, c)
				+ " "
				+ PCFGNT(L, h)
				+ " "
				+ Math.exp(leftAttach.get(h).get(c) + rightStopHasChild.get(h)
						+ leftContinueNoChild.get(h)));

		printer.println(PCFGNT("R", h)
				+ " "
				+ PCFGNT(R, h)
				+ " "
				+ PCFGNT(N, c)
				+ " "
				+ Math.exp(rightAttach.get(h).get(c)
						+ rightContinueHasChild.get(h)));

		printer.println(PCFGNT("L", h)
				+ " "
				+ PCFGNT(N, c)
				+ " "
				+ PCFGNT(L, h)
				+ " "
				+ Math.exp(leftAttach.get(h).get(c)
						+ leftContinueHasChild.get(h)));

	}

	public void writePCFG(PrintStream printer) {
		for (int h = 0; h < alphabet.size(); h++) {
			printer.println(PCFGNT("N", h) + " " + Math.exp(roots.get(h)));

			printer.println(PCFGNT("PN", h)
					+ " "
					+ alphabet.reverseLookup(h)
					+ " "
					+ Math.exp(leftStopNoChild.get(h) + rightStopNoChild.get(h)));

			printer.println(PCFGNT("PL", h) + " " + alphabet.reverseLookup(h)
					+ " " + Math.exp(leftStopHasChild.get(h)));

			printer.println(PCFGNT("PR", h)
					+ " "
					+ alphabet.reverseLookup(h)
					+ " "
					+ Math.exp(leftStopNoChild.get(h)
							+ rightStopHasChild.get(h)));

			for (int c = 0; c < alphabet.size(); c++) {
				writePCFGBinaryRules(printer, "N", "L", "R", h, c);
				writePCFGBinaryRules(printer, "N", "PL", "PR", h, c);
				writePCFGBinaryRules(printer, "N", "L", "PR", h, c);
				writePCFGBinaryRules(printer, "N", "PL", "R", h, c);
				writePCFGBinaryRules(printer, "PN", "L", "R", h, c);
				writePCFGBinaryRules(printer, "PN", "PL", "PR", h, c);
				writePCFGBinaryRules(printer, "PN", "L", "PR", h, c);
				writePCFGBinaryRules(printer, "PN", "PL", "R", h, c);
			}
		}
	}
}
