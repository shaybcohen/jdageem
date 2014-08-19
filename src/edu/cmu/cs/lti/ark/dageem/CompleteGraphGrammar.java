package edu.cmu.cs.lti.ark.dageem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class CompleteGraphGrammar extends SplitHeadAutomatonGrammar {

	private double[][] edges;
	private double[] roots;
	private int n;

	private HashMap<Integer, int[]> hasherMap;

	public static final int STATE_InitialLeftOnly = 0;
	public static final int STATE_InitialRightOnly = 1;
	public static final int STATE_InitialRightAndLeft = 2;
	public static final int STATE_L1 = 3;
	public static final int STATE_R1 = 4;
	public static final int STATE_R2 = 5;
	public static final int STATE_L2 = 6;
	public static final int STATE_InitialNone = 7;

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

	private static double[][] edgesPool;
	private static double[] rootsPool;
	private static int currentPool = -1;

	// hash marks for the expectation semiring
	private static final int HASH_AttachRight = -1;
	private static final int HASH_AttachLeft = -2;
	private static final int HASH_Root = -3;

	public CompleteGraphGrammar(int n) {
		roots = new double[n];
		edges = new double[n][n];

		this.n = n;

		hasherMap = new HashMap<Integer, int[]>();
	}

	public CompleteGraphGrammar(int n, boolean pool) {
		this.n = n;

		if (pool == false) {
			roots = new double[n];
			edges = new double[n][n];
		}

		if (pool == true) {
			if (n > currentPool) {
				rootsPool = new double[n];
				edgesPool = new double[n][n];
				edges = edgesPool;
				roots = rootsPool;
				currentPool = n;
			} else {
				edges = edgesPool;
				roots = rootsPool;
			}
		}

		hasherMap = new HashMap<Integer, int[]>();
	}

	public void setEdge(int i, int j, double v) {
		// System.err.println((i+1)+"->"+(j+1)+" = "+v);

		edges[i][j] = v - n;
	}

	public void setRoot(int i, double v) {
		// System.err.println("root: "+(i+1)+"="+v);

		roots[i] = v - n;
	}

	public int length() {
		return n;
	}

	public double getEdge(int i, int j) {
		return edges[i][j];
	}

	public double getRoot(int i) {
		return roots[i];
	}

	public int hasher(int a, int b, int c) {

		int h = EisnerSattaChart.hasher(a, b, c, 0, 0);
		int[] arr = new int[3];
		arr[0] = a;
		arr[1] = b;
		arr[2] = c;

		if (!hasherMap.containsKey(h)) {
			hasherMap.put(h, arr);
		}

		return h;
	}

	public int hasher(int a, int b) {

		int h = EisnerSattaChart.hasher(a, b, 0, 0, 0);
		int[] arr = new int[2];
		arr[0] = a;
		arr[1] = b;

		if (!hasherMap.containsKey(h)) {
			hasherMap.put(h, arr);
		}

		return h;
	}

	public double getAxiomDeltaRight(int head, int q, int r, int child) {
		return Chart.semiring.One;
	}

	public double getAxiomDeltaLeft(int head, int q, int r, int child) {
		return Chart.semiring.One;
	}

	public double getAxiomAttachmentRight(int i, int j, int head, int child,
			int r, int s_) {
		return edges[i - 1][j - 1];
	}

	public double getAxiomAttachmentLeft(int i, int j, int head, int child,
			int r, int s_) {
		return edges[i - 1][j - 1];
	}

	public double getAxiomDeltaStart(int head, int q) {
		return Chart.semiring.One;
	}

	public double getAxiomRoot(int i, int head) {
		return roots[i - 1];
	}

	public void timesAxiomDeltaRight(EValue v, int head, int q, int r, int child) {
	}

	public void timesAxiomDeltaLeft(EValue v, int head, int q, int r, int child) {
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

	public void timesAxiomDeltaStart(EValue v, int head, int q) {
	}

	public void timesAxiomRoot(EValue v, int i, int head) {
		int hash = hasher(HASH_Root, i, 0);

		double p2 = getAxiomRoot(i, head);
		double r2 = p2;

		v.Times(hash, p2, r2);
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

	public int[] getStatesInitial(int head) {
		return STATES_initial;
	}

	public boolean isFlipState(int state) {
		return (state == STATE_R2 || state == STATE_InitialLeftOnly
				|| state == STATE_InitialNone || state == STATE_R1);
	}

	public void setAxiomDeltaRight(int head, int q, int r, int child, double v) {

	}

	public void setAxiomDeltaLeft(int head, int q, int r, int child, double v) {
	}

	public void setAxiomAttachmentRight(int head, int child, int r, int s_,
			double v) {
	}

	public void setAxiomAttachmentLeft(int head, int child, int r, int s_,
			double v) {
	}

	public void setAxiomRoot(int head, double v) {
	}

	public void setAxiomDeltaStart(int head, int q, double v) {
	}

	public static CompleteGraphGrammar readNextCompleteGrammar(BufferedReader r) {

		int n_max = 0;
		
		CompleteGraphGrammar g_ = new CompleteGraphGrammar(200);
		
		System.err.println("Reading the next symmetric attachment scores.");
		try {
			String s = "begin";

			while (!s.equals("")) {
				s = r.readLine();

				if (!s.equals("")) {
					StringTokenizer tokenizer = new StringTokenizer(s, "\t");
					
					String word1 = tokenizer.nextToken();
					String word2 = tokenizer.nextToken();
					String POS1 = tokenizer.nextToken();
					String POS2 = tokenizer.nextToken();
					String index1 = tokenizer.nextToken();
					String index2 = tokenizer.nextToken();
					String Z = tokenizer.nextToken();
					String distance = tokenizer.nextToken();
					
					double distance_ = Double.parseDouble(distance);
					int index1_ = Integer.parseInt(index1);
					int index2_ = Integer.parseInt(index2);
					
					if (index1_ > n_max) { n_max = index1_; }
					if (index2_ > n_max) { n_max = index2_; }

					g_.setEdge(index1_, index2_, -distance_);
					g_.setEdge(index2_, index1_, -distance_);
				}
			}

			n_max++;
			
			CompleteGraphGrammar g = new CompleteGraphGrammar(n_max);
			
			for (int i=0; i < n_max; i++)
			{
				for (int j=0; j < n_max; j++)
				{
					g.setEdge(i, j, g_.getEdge(i, j));
				}
			}
			
			return g;
		}

		catch (IOException e) {
			return null;
		}
	}
}
