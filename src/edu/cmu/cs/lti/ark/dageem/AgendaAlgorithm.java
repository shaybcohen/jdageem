package edu.cmu.cs.lti.ark.dageem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

//import edu.columbia.cs.nlp.shiftreduce.CGSBacktrackChain;
//import edu.columbia.cs.nlp.shiftreduce.CGSProbabilisticGrammar;

public abstract class AgendaAlgorithm {

	protected AgendaInterface agenda;
	protected Chart chart;
	protected boolean useSecondary;
	protected ArrayList<PoppedValueContainer> transientItems;
	protected int agendaSize = 0;
	protected boolean isRecording;

	public AgendaAlgorithm(boolean useSecondary) {
		agenda = new Agenda(useSecondary);
		
		isRecording = false;

		this.useSecondary = useSecondary;
		transientItems = new ArrayList<PoppedValueContainer>();
	}

	public int agendaSize() {
		return agendaSize;
	}

	public void setChart(Chart c) {
		agenda.setChart(c);
		chart = c;
	}

	public Chart getChart() {
		return chart;
	}

	public void clear(boolean useSecondary) {
		agenda = new Agenda(useSecondary);
		agenda.setChart(chart);
	}

	public abstract void processedPoppedTerm(Term poppedTerm, int poppedHash,
			double poppedValue, EValue poppedSecondary, double priority);

	public void assertTerm(Term t) {
		agenda.markAgendaItem(t, t.value(), t.secondaryValue(), null, true);
	}

	public void startRecording() {
		System.err.println("Recording agenda...");

		agenda = new RecordedAgenda(agenda);
		((RecordedAgenda) agenda).setRecordingMode(true);
		agenda.setChart(chart);

		isRecording = true;
	}

	public void stopRecording(String filename) {

		if (!isRecording) {
			System.err.println("No need to stop recording.");
			return;
		}

		System.err.println("Stopping recording and saving agenda to "
				+ filename);

		try {
			FileOutputStream fos = null;
			ObjectOutputStream out = null;

			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(agenda);
			out.close();
		}

		catch (IOException e) {
			System.err.println("Could not write " + filename + " : " + e);
			return;
		}
	}

	public void loadRecording(String filename, boolean recordIfNotFound) {
		FileInputStream fis = null;
		ObjectInputStream in = null;

		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);

			agenda = (RecordedAgenda) in.readObject();

			agenda.setChart(chart);

			in.close();

			isRecording = false;

			return;
		}

		catch (IOException e) {
			System.err.println("Could not load agenda from " + filename + " : "
					+ e);
		}

		catch (ClassNotFoundException e) {
			System.err.println("Error: " + e);
		}

		System.err.println("Using regular agenda.");

		agenda = new Agenda(useSecondary);

		if (recordIfNotFound) {
			startRecording();
		}
	}

	public void addTransientItemToAgenda(Term t, double value,
			Object secondaryValue, BacktrackChain bt, boolean b) {

		PoppedValueContainer transientItem = new PoppedValueContainer();

		transientItem.myBacktrack = bt;
		transientItem.myObject = secondaryValue;
		transientItem.myPriority = 0;
		transientItem.myTerm = t;
		transientItem.myValue = value;

		transientItems.add(transientItem);
	}

	public abstract boolean isGoal(Term t);

	public boolean isSeedTerm(Term term) {
		return false;
	}

	public boolean updatePoppedItemValue(Term poppedTerm, double oldValue,
			double poppedValue, EValue poppedSecondaryValue) {
		boolean changed = false;

		boolean isSeed = isSeedTerm(poppedTerm);

		double v;

		if (isSeed) {
			v = poppedValue;
		} else {
			v = Chart.semiring.Plus(oldValue, poppedValue);
		}

		poppedTerm.setValue(v);

		if (useSecondary) {
			if (poppedTerm.secondaryValue() == null) {
				poppedTerm.setSecondaryValue(poppedSecondaryValue);
				changed = true;
			} else {
				if (isSeed) {
					poppedTerm
							.setSecondaryValue(poppedSecondaryValue.getCopy());
					changed = true;
				} else {
					changed = ((EValue) (poppedTerm.secondaryValue()))
							.Plus(poppedSecondaryValue);
				}
			}
		}

		if (oldValue != v) {
			changed = true;
		}

		return changed;
	}

	public boolean agendaIterator() {
		double poppedValue;
		Term poppedTerm;
		EValue poppedSecondaryValue;
		BacktrackChain poppedBacktrack = null;

		boolean popped;
		double poppedPriority;

		if (transientItems.size() > 0) {
			PoppedValueContainer transientItem = transientItems.get(0);

			transientItems.remove(0);

			popped = true;
			poppedTerm = transientItem.myTerm;
			poppedBacktrack = transientItem.myBacktrack;
			poppedValue = transientItem.myValue;
			poppedSecondaryValue = (EValue) transientItem.myObject;
			poppedPriority = 0;

		} else {
			agendaSize++;
			PoppedValueContainer container = new PoppedValueContainer();

			popped = agenda.pop(container);
			poppedTerm = container.myTerm;
			poppedBacktrack = container.myBacktrack;
			poppedValue = container.myValue;
			poppedSecondaryValue = (EValue) container.myObject;
			poppedPriority = container.myPriority;
		}

		if (!popped) {
			return false;
		}

		int poppedHash = poppedTerm.hashcode();

		double oldValue = chart.currVal(poppedHash);

		// in the paper:
		// delta -> poppedValue
		// old -> oldValue

		boolean changed = updatePoppedItemValue(poppedTerm, oldValue,
				poppedValue, poppedSecondaryValue);

		if (!chart.hasTerm(poppedHash)) {
			chart.addTerm(poppedTerm);
		}

		if ((!changed) && (!chart.hasMultipleBacktracks())) {
			return true;
		} else {
			changed = true;
		}
		// changed = true;

		// notice this trick is for the max semiring
		if (changed) {
			// set val
			// poppedTerm.setValue(poppedValue);

			if (useSecondary) {
				// poppedTerm.secondaryValue().Plus(poppedSecondaryValue);
			}

			if (((oldValue <= poppedTerm.value()) && (Chart.semiring
					.Idempotent())) || (chart.hasMultipleBacktracks())) {
				chart.addBacktrack(poppedHash, poppedBacktrack, poppedValue);
			}

			processedPoppedTerm(poppedTerm, poppedHash, poppedValue,
					(EValue) poppedSecondaryValue, poppedPriority);
		}

		if (chart.isPriorityModeNormal()) {
			if ((!useSecondary) && (!chart.hasMultipleBacktracks())
					&& (Chart.semiring.Idempotent()) && (isGoal(poppedTerm))) {
				return false;
			}
		}

		return true;
	}
}