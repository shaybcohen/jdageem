/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.list.IntArrayList;

/**
 * @author scohen
 * 
 */
public class MultinomialDMVGrammar extends DMVGrammar {

	private int[] N;
	private int[] heads;
	private int[] signature;
	private ArrayList<Integer[]> mapping;

	private static final int SIGNATURE_LeftAttach = 1;
	private static final int SIGNATURE_RightAttach = 2;
	private static final int SIGNATURE_LeftNoChild = 3;
	private static final int SIGNATURE_LeftHasChild = 4;
	private static final int SIGNATURE_RightHasChild = 5;
	private static final int SIGNATURE_RightNoChild = 6;
	private static final int SIGNATURE_Root = 7;

	public MultinomialDMVGrammar(Alphabet alpha) {
		super(alpha);
	}

	public int[] sizes() {
		return N;
	}

	public HashSet<Integer> getKeySet(OpenIntDoubleHashMap h) {
		HashSet<Integer> s = new HashSet<Integer>();
		IntArrayList lst = new IntArrayList();

		h.keys(lst);

		for (int i = 0; i < lst.size(); i++) {
			s.add(lst.get(i));
		}

		return s;
	}

	public void initMultinomials() {
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		ArrayList<Integer> h = new ArrayList<Integer>();
		ArrayList<Integer> s = new ArrayList<Integer>();
		mapping = new ArrayList<Integer[]>();

		Integer[] set = (Integer[]) (leftAttach.keySet()
				.toArray(new Integer[0]));
		Arrays.sort(set);

		for (int i : set) {
			sizes.add(leftAttach.get(i).size());
			h.add(i);
			s.add(SIGNATURE_LeftAttach);
			mapping.add((Integer[]) leftAttach.get(i).keySet()
					.toArray(new Integer[0]));
		}

		set = (Integer[]) (rightAttach.keySet().toArray(new Integer[0]));
		Arrays.sort(set);

		for (int i : set) {
			sizes.add(rightAttach.get(i).size());
			h.add(i);
			s.add(SIGNATURE_RightAttach);
			mapping.add((Integer[]) rightAttach.get(i).keySet()
					.toArray(new Integer[0]));
		}

		HashSet<Integer> st = new HashSet<Integer>();
		st.addAll(getKeySet(leftContinueNoChild));
		st.addAll(getKeySet(leftStopNoChild));
		set = (Integer[]) st.toArray(new Integer[0]);
		Arrays.sort(set);

		for (int i : set) {
			sizes.add(2);
			h.add(i);
			s.add(SIGNATURE_LeftNoChild);
			mapping.add(null);
		}

		st.clear();
		st.addAll(getKeySet(leftContinueHasChild));
		st.addAll(getKeySet(leftStopHasChild));
		set = (Integer[]) st.toArray(new Integer[0]);
		Arrays.sort(set);

		for (int i : set) {
			sizes.add(2);
			h.add(i);
			s.add(SIGNATURE_LeftHasChild);
			mapping.add(null);
		}

		st.clear();
		st.addAll(getKeySet(rightContinueHasChild));
		st.addAll(getKeySet(rightStopHasChild));
		set = (Integer[]) st.toArray(new Integer[0]);
		Arrays.sort(set);

		for (int i : set) {
			sizes.add(2);
			h.add(i);
			s.add(SIGNATURE_RightHasChild);
			mapping.add(null);
		}

		st.clear();
		st.addAll(getKeySet(rightContinueNoChild));
		st.addAll(getKeySet(rightStopNoChild));
		set = (Integer[]) st.toArray(new Integer[0]);
		Arrays.sort(set);

		for (int i : set) {
			sizes.add(2);
			h.add(i);
			s.add(SIGNATURE_RightNoChild);
			mapping.add(null);
		}

		set = (Integer[]) getKeySet(roots).toArray(new Integer[0]);
		sizes.add(set.length);
		s.add(SIGNATURE_Root);
		h.add(-1);
		mapping.add(set);

		// copy to class members
		N = new int[sizes.size()];
		for (int i = 0; i < N.length; i++) {
			N[i] = sizes.get(i);
		}

		heads = new int[h.size()];
		for (int i = 0; i < heads.length; i++) {
			heads[i] = h.get(i);
		}

		signature = new int[s.size()];
		for (int i = 0; i < signature.length; i++) {
			signature[i] = s.get(i);
		}
		// mapping is already copied
	}

	public int getHeadAt(int i) {
		return heads[i];
	}

	public void copyToCount(Set<Integer> allowedHeads, CountInterface count) {
		// Iterate over all multinomials
		// and use count.setCountValue to copy the parameter

		int n = 0;

		for (int head : heads) {
			if ((allowedHeads == null) || (allowedHeads.contains(head))
					|| (head == -1)) {
				for (int j = 0; j < N[n]; j++) {

					switch (signature[n]) {
					case SIGNATURE_LeftAttach:
						if ((allowedHeads == null) || allowedHeads.contains(j)) {
							count.setCountValue(n, j,
									safeDouble(leftAttach, head, j));
						} else {
							count.setCountValue(n, j, Chart.semiring.Zero);
						}
						break;
					case SIGNATURE_RightAttach:
						if ((allowedHeads == null) || allowedHeads.contains(j)) {
							count.setCountValue(n, j, safeDouble(rightAttach, head, j));
						} else {
							count.setCountValue(n, j, Chart.semiring.Zero);
						}
						break;
					case SIGNATURE_LeftNoChild:
						if (j == 0) {
							count.setCountValue(n, j, safeDouble(leftContinueNoChild, head));
						} else if (j == 1) {
							count.setCountValue(n, j, safeDouble(leftStopNoChild, head));
						} else {
							System.err
									.println("Warning: |Multinomial| > 2 for stopping probabilities.");
						}
						break;
					case SIGNATURE_LeftHasChild:
						if (j == 0) {
							count.setCountValue(n, j,
									safeDouble(leftContinueHasChild, head));
						} else if (j == 1) {
							count.setCountValue(n, j,
									safeDouble(leftStopHasChild, head));
						} else {
							System.err
									.println("Warning: |Multinomial| > 2 for stopping probabilities.");
						}
						break;
					case SIGNATURE_RightHasChild:
						if (j == 0) {
							count.setCountValue(n, j,
									safeDouble(rightContinueHasChild, head));
						} else if (j == 1) {
							count.setCountValue(n, j,
									safeDouble(rightStopHasChild, head));
						} else {
							System.err
									.println("Warning: |Multinomial| > 2 for stopping probabilities.");
						}
						break;
					case SIGNATURE_RightNoChild:
						if (j == 0) {
							count.setCountValue(n, j,
									safeDouble(rightContinueNoChild, head));
						} else if (j == 1) {
							count.setCountValue(n, j,
									safeDouble(rightStopNoChild, head));
						} else {
							System.err
									.println("Warning: |Multinomial| > 2 for stopping probabilities.");
						}
						break;
					case SIGNATURE_Root:
						count.setCountValue(n, j, safeDouble(roots, j));
						break;
					}

				}
			}

			n++;
		}
	}

	public void copyFromCount(Set<Integer> allowedHeads, CountInterface count) {
		// Iterate over all multinomials
		// and use count.getCountValue to copy the parameter

		int n = 0;

		for (int head : heads) {
			if ((allowedHeads == null) || (allowedHeads.contains(head))
					|| (head == -1)) {
				for (int j = 0; j < N[n]; j++) {
					double v = count.getCountValue(n, j);

					switch (signature[n]) {
					case SIGNATURE_LeftAttach:
						leftAttach.get(head).put(j, v);
						break;
					case SIGNATURE_RightAttach:
						rightAttach.get(head).put(j, v);
						break;
					case SIGNATURE_LeftNoChild:
						if (j == 0) {
							leftContinueNoChild.put(head, v);
						} else if (j == 1) {
							leftStopNoChild.put(head, v);
						} else {
							System.err
									.println("Warning: |Multinomial| > 2 for stopping probabilities.");
						}
						break;
					case SIGNATURE_LeftHasChild:
						if (j == 0) {
							leftContinueHasChild.put(head, v);
						} else if (j == 1) {
							leftStopHasChild.put(head, v);
						} else {
							System.err
									.println("Warning: |Multinomial| > 2 for stopping probabilities.");
						}
						break;
					case SIGNATURE_RightHasChild:
						if (j == 0) {
							rightContinueHasChild.put(head, v);
						} else if (j == 1) {
							rightStopHasChild.put(head, v);
						} else {
							System.err
									.println("Warning: |Multinomial| > 2 for stopping probabilities.");
						}
						break;
					case SIGNATURE_RightNoChild:
						if (j == 0) {
							rightContinueNoChild.put(head, v);
						} else if (j == 1) {
							rightStopNoChild.put(head, v);
						} else {
							System.err
									.println("Warning: |Multinomial| > 2 for stopping probabilities.");
						}
						break;
					case SIGNATURE_Root:
						roots.put(j, v);
						break;
					}

				}
			}

			n++;
		}
	}

}
