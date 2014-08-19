/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author scohen
 * 
 */
public class RecordedAgenda extends AgendaInterface implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1526266522577727584L;

	private AgendaInterface activeAgenda;
	private boolean isRecording;
	// private Chart chart;

	private ArrayList<PoppedValueContainer> recordedAgenda;
	private HashMap<Integer, PoppedValueContainer> recordedAgendaHash;

	private Chart chart;
	
	public RecordedAgenda(AgendaInterface activeAgenda) {
		this.activeAgenda = activeAgenda;

		recordedAgenda = new ArrayList<PoppedValueContainer>();
		recordedAgendaHash = new HashMap<Integer, PoppedValueContainer>();
		
		isRecording = false;
	}
	
	public RecordedAgenda()
	{
		activeAgenda = null;
		isRecording = false;
	}

	public void setRecordingMode(boolean b) {
		isRecording = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cmu.cs.lti.ark.dageem.AgendaInterface#pop(edu.cmu.cs.lti.ark.dageem
	 * .AgendaInterface.PoppedValueContainer)
	 */
	@Override
	public boolean pop(PoppedValueContainer container) {

		if (isRecording) {
			// relying on the agenda popping each item exactly once
			boolean b = activeAgenda.pop(container);

			if (b) {
				PoppedValueContainer c = new PoppedValueContainer(container);

				recordedAgenda.add(c);

				recordedAgendaHash.put(container.myTerm.hashcode(), c);
			}

			return b;
		} else {
			if (recordedAgenda.size() == 0) {
				return false;
			} else {
				PoppedValueContainer c = recordedAgenda.get(0);
				recordedAgenda.remove(0);

				container.myTerm = chart.getTerm(c.myTerm.hashcode());
				container.myBacktrack = c.myBacktrack;
				container.myValue = c.myValue;
				container.myObject = c.myObject;
				container.myPriority = c.myPriority; // priority is fixed and
														// does not depend on
														// the value (such as
														// bucketing mode)

				return true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cmu.cs.lti.ark.dageem.AgendaInterface#setChart(edu.cmu.cs.lti.ark
	 * .dageem.Chart)
	 */
	@Override
	public void setChart(Chart c) {
		if (isRecording) {
			activeAgenda.setChart(c);
		}
		
		chart = c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cmu.cs.lti.ark.dageem.AgendaInterface#markAgendaItem(edu.cmu.cs.lti
	 * .ark.dageem.Term, double, java.lang.Object,
	 * edu.cmu.cs.lti.ark.dageem.BacktrackChain)
	 */
	@Override
	public void markAgendaItem(Term term, double value, Object obj,
			BacktrackChain bt) {
		markAgendaItem(term, value, obj, bt, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cmu.cs.lti.ark.dageem.AgendaInterface#markAgendaItem(edu.cmu.cs.lti
	 * .ark.dageem.Term, double, java.lang.Object,
	 * edu.cmu.cs.lti.ark.dageem.BacktrackChain, boolean)
	 */
	@Override
	public void markAgendaItem(Term term, double value, Object obj,
			BacktrackChain bt, boolean b) {

		if (isRecording) {
			activeAgenda.markAgendaItem(term, value, obj, bt, b);
		} else {
			PoppedValueContainer container = recordedAgendaHash.get(term
					.hashcode());
			container.myValue = Chart.semiring.Plus(container.myValue, value);
			container.myObject = obj;
			container.myBacktrack = bt;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.cmu.cs.lti.ark.dageem.AgendaInterface#setExhaustive(boolean)
	 */
	@Override
	public void setExhaustive(boolean b) {
		// TODO Auto-generated method stub
		if (isRecording) {
			activeAgenda.setExhaustive(b);
		}
	}

	
	private void writeObject(ObjectOutputStream out) throws IOException {

		out.writeObject(recordedAgenda);
		out.writeObject(recordedAgendaHash);
		
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		recordedAgenda = (ArrayList<PoppedValueContainer>)in.readObject();
		recordedAgendaHash = (HashMap<Integer, PoppedValueContainer>)in.readObject();

	}
}
