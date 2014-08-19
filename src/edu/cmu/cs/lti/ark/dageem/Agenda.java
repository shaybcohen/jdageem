/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.ArrayList;

import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.map.OpenIntIntHashMap;
//import edu.columbia.cs.nlp.shiftreduce.CGSBacktrackChain;

/**
 * @author scohen
 * 
 */

public class Agenda extends AgendaInterface {

	private static final int ARRAYAGENDA_INITIAL_CAPACITY = 1000;

	private OpenIntDoubleHashMap valuesHash;
	private OpenIntObjectHashMap objsHash;
	private OpenIntIntHashMap nthHash;
	private OpenIntDoubleHashMap prioritiesHash;

	Term[] terms;
	BacktrackChain[] backtrack;
	double[] priorities;
	double[] values;
	Object[] objects;
	int[] nth;

	ArrayList<Integer> deletedIndices;

	int lastHeapInd, lastInd;

	int[] heapArray;
	int[] heapNth;
	int arraysLength, heapArraysLength;

	Chart chart;

	private boolean exhaustive;

	boolean useObjects;

	Agenda(boolean useSecondary) {
		lastHeapInd = -1;
		lastInd = 0;
		useObjects = useSecondary;
		exhaustive = false;

		arraysLength = ARRAYAGENDA_INITIAL_CAPACITY;
		heapArraysLength = ARRAYAGENDA_INITIAL_CAPACITY;

		terms = new Term[ARRAYAGENDA_INITIAL_CAPACITY];
		backtrack = new BacktrackChain[ARRAYAGENDA_INITIAL_CAPACITY];
		priorities = new double[ARRAYAGENDA_INITIAL_CAPACITY];
		values = new double[ARRAYAGENDA_INITIAL_CAPACITY];
		nth = new int[ARRAYAGENDA_INITIAL_CAPACITY];

		if (useObjects) {
			objects = new Object[ARRAYAGENDA_INITIAL_CAPACITY];
		}

		heapArray = new int[ARRAYAGENDA_INITIAL_CAPACITY];
		heapNth = new int[ARRAYAGENDA_INITIAL_CAPACITY];

		nthHash = new OpenIntIntHashMap();
		prioritiesHash = new OpenIntDoubleHashMap();
		valuesHash = new OpenIntDoubleHashMap();
		objsHash = new OpenIntObjectHashMap();

		deletedIndices = new ArrayList<Integer>();
	}

	public void setChart(Chart c) {
		chart = c;
	}

	public void setExhaustive(boolean b) {
		exhaustive = b;
	}

	boolean bubbleUp(int holeIndex) {
		int value = heapArray[holeIndex];
		int value_nth = heapNth[holeIndex];

		int parent = (holeIndex - 1) / 2;

		boolean bubblingHappened = false;

		while (holeIndex > 0
				&& priorities[heapArray[parent]] < priorities[value]) {
			bubblingHappened = true;
			heapArray[holeIndex] = heapArray[parent];
			heapNth[holeIndex] = heapNth[parent];
			holeIndex = parent;
			parent = (holeIndex - 1) / 2;
		}

		heapArray[holeIndex] = value;
		heapNth[holeIndex] = value_nth;

		return bubblingHappened;
	}

	void bubbleDown_downfirst(int holeIndex, int length) {
		int value = heapArray[holeIndex];
		int nth = heapNth[holeIndex];

		int secondChild = 2 * holeIndex + 2;
		while (secondChild < length) {
			if (priorities[heapArray[secondChild]] < priorities[heapArray[secondChild - 1]])
				secondChild--;
			heapArray[holeIndex] = heapArray[secondChild];
			heapNth[holeIndex] = heapNth[secondChild];

			holeIndex = secondChild;
			secondChild = 2 * (secondChild + 1);
		}

		if (secondChild == length) {
			heapArray[holeIndex] = heapArray[secondChild - 1];
			heapNth[holeIndex] = heapNth[secondChild - 1];
			holeIndex = secondChild - 1;
		}

		heapArray[holeIndex] = value;
		heapNth[holeIndex] = nth;
		bubbleUp(holeIndex);
	}

	public void markAgendaItem(Term term, double value, Object obj,
			BacktrackChain bt) {
		markAgendaItem(term, value, obj, bt, false);
	}

	// addEqual should be false by default
	public void markAgendaItem(Term term, double value, Object obj,
			BacktrackChain bt, boolean addEqual) {

		double newValue;
		int hashTerm = term.hashcode();
		if (value == Chart.semiring.Zero) {
			return;
		}

		// System.err.println("marking "+term+" with value "+Math.exp(value));

		/*
		 * if ((chart.currVal(hashTerm) >= value) && (!addEqual)) { return; }
		 */

		double currentValue;
		if (valuesHash.containsKey(hashTerm)) {
			currentValue = valuesHash.get(hashTerm);
		} else {
			currentValue = Chart.semiring.Zero;
		}

		newValue = Chart.semiring.Plus(value, currentValue);

		boolean changed = (newValue != currentValue);
		if (useObjects) {
			if (objsHash.containsKey(hashTerm)) {

				changed = ((EValue) obj).Plus((EValue) objsHash.get(hashTerm));

			}
		}

		if (!exhaustive) {
			if (!changed) {
				return;
			}
		}

		if (useObjects) {
			objsHash.put(hashTerm, obj);
		}

		/*
		 * if (Chart.Semiring.Idempotent()) { System.err.println("updating " +
		 * term + " " + currentValue + "->" + newValue); }
		 */
		valuesHash.put(hashTerm, newValue);

		// System.err.println("marking: "+term+" with value "+value);

		int useInd = 0;

		if (deletedIndices.size() == 0) {
			useInd = lastInd;
			lastInd++;
		} else {
			useInd = deletedIndices.get(deletedIndices.size() - 1);
			deletedIndices.remove(deletedIndices.size() - 1);
		}

		if (lastInd == arraysLength) {
			// enlarge all data arrays
			Term[] terms2 = new Term[arraysLength * 2];
			BacktrackChain[] backtrack_2 = new BacktrackChain[arraysLength * 2];
			double[] values2 = new double[arraysLength * 2];
			double[] priorities2 = new double[arraysLength * 2];
			int[] nth2 = new int[arraysLength * 2];
			Object[] objects2 = null;

			if (useObjects) {
				objects2 = new Object[arraysLength * 2];
				System.arraycopy(objects, 0, objects2, 0, arraysLength);
			}

			System.arraycopy(terms, 0, terms2, 0, terms.length);
			System.arraycopy(backtrack, 0, backtrack_2, 0, arraysLength);

			System.arraycopy(values, 0, values2, 0, arraysLength);
			System.arraycopy(priorities, 0, priorities2, 0, arraysLength);
			System.arraycopy(nth, 0, nth2, 0, arraysLength);

			terms = terms2;
			backtrack = backtrack_2;
			values = values2;
			priorities = priorities2;
			nth = nth2;

			if (useObjects) {
				objects = objects2;
			}

			arraysLength = arraysLength * 2;
		}

		terms[useInd] = term;
		if (bt == null) {
			// TODO need to check if this makes sense, or should create new
			// Backtrack.
			backtrack[useInd] = null;
		} else {
			backtrack[useInd] = bt;
		}
		if (useObjects) {
			objects[useInd] = obj;
		}
		values[useInd] = newValue;

		// Use priority instead of value?

		priorities[useInd] = chart.getPriority(term, newValue, obj); // value;

		int x;

		if (nthHash.containsKey(hashTerm)) {
			x = nthHash.get(hashTerm) + 1;
		} else {
			x = 1;
		}

		// double oldPriority = Chart.Semiring.Zero;

		/*
		 * if (prioritiesHash.containsKey(hashTerm)) { oldPriority =
		 * prioritiesHash.get(hashTerm); }
		 */

		nth[useInd] = x;

		// if (priorities[useInd] > oldPriority) {
		nthHash.put(hashTerm, x);
		prioritiesHash.put(hashTerm, priorities[useInd]);
		// }

		if (lastHeapInd == heapArraysLength - 1) {
			// enlarge heap array
			int[] heapArray2 = new int[heapArraysLength * 2];
			int[] heapNth2 = new int[heapArraysLength * 2];

			System.arraycopy(heapArray, 0, heapArray2, 0, heapArraysLength);
			System.arraycopy(heapNth, 0, heapNth2, 0, heapArraysLength);

			heapArray = heapArray2;
			heapNth = heapNth2;

			heapArraysLength = heapArraysLength * 2;
		}

		lastHeapInd++;

		heapArray[lastHeapInd] = useInd;
		heapNth[lastHeapInd] = x;

		bubbleUp(lastHeapInd);
	}

	void checkValidity() {
		double maxValue = priorities[heapArray[0]];
		for (int i = 1; i <= lastHeapInd; i++) {
			if (maxValue < priorities[heapArray[i]]) {
				System.err.println("invalid value " + priorities[heapArray[i]]
						+ ">" + maxValue + " in " + i);
			}
		}
	}


	public boolean pop(PoppedValueContainer container) {

		Term myTerm = null;
		BacktrackChain myBacktrack = null;
		double myValue = Chart.semiring.Zero;
		Object myObject;
		double myPriority = -1;
		
		while (true) {
			if (lastHeapInd == -1) {
				container.myTerm = myTerm;
				container.myBacktrack = myBacktrack;
				container.myValue = myValue;
				container.myPriority = myPriority;
				return false;
			}

			// swap between front and back
			int idx = heapArray[0];
			int nth = heapNth[0];

			heapArray[0] = heapArray[lastHeapInd];
			heapNth[0] = heapNth[lastHeapInd];

			heapArray[lastHeapInd] = idx;
			heapNth[lastHeapInd] = nth;

			bubbleDown_downfirst(0, lastHeapInd);

			idx = heapArray[lastHeapInd];
			nth = heapNth[lastHeapInd];
			Term term = terms[idx];

			int termHash = term.hashcode();

			lastHeapInd--;

			deletedIndices.add(idx);

			if (nth == nthHash.get(termHash)) {
				myBacktrack = backtrack[idx];
				
				myTerm = term;
				myValue = values[idx];

				if (useObjects) {
					myObject = objects[idx];
				} else {
					myObject = null;
				}

				// nthHash.remove(term);
				valuesHash.removeKey(termHash);
				objsHash.removeKey(termHash);
				prioritiesHash.removeKey(termHash);

				container.myTerm = myTerm;
				container.myBacktrack = myBacktrack;
				container.myValue = myValue;
				container.myObject = myObject;
				container.myPriority = priorities[idx];

				return true;
			}
		}
	}

	void clear() {

		lastHeapInd = -1;
		lastInd = 0;

		arraysLength = ARRAYAGENDA_INITIAL_CAPACITY;
		heapArraysLength = ARRAYAGENDA_INITIAL_CAPACITY;

		terms = new Term[ARRAYAGENDA_INITIAL_CAPACITY];
		backtrack = new BacktrackChain[ARRAYAGENDA_INITIAL_CAPACITY];
		priorities = new double[ARRAYAGENDA_INITIAL_CAPACITY];
		values = new double[ARRAYAGENDA_INITIAL_CAPACITY];
		nth = new int[ARRAYAGENDA_INITIAL_CAPACITY];

		if (useObjects) {
			objects = new Object[ARRAYAGENDA_INITIAL_CAPACITY];
		}

		heapArray = new int[ARRAYAGENDA_INITIAL_CAPACITY];
		heapNth = new int[ARRAYAGENDA_INITIAL_CAPACITY];

		nthHash = new OpenIntIntHashMap();
		prioritiesHash = new OpenIntDoubleHashMap();
		valuesHash = new OpenIntDoubleHashMap();
		objsHash = new OpenIntObjectHashMap();

		deletedIndices = new ArrayList<Integer>();
	}

	void printHeap() {
		for (int i = 0; i <= lastHeapInd; i++) {
			System.err.println(terms[heapArray[i]]);
		}
	}

	int size() {
		return lastHeapInd + 1;
	}

}
