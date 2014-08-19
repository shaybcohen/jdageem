package edu.cmu.cs.lti.ark.dageem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class PoppedValueContainer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2265069046291099979L;
	
	Term myTerm;
	BacktrackChain myBacktrack;
	double myValue;
	Object myObject;
	double myPriority;

	public PoppedValueContainer()
	{
		
	}
	
	public PoppedValueContainer(PoppedValueContainer c)
	{
		myTerm = c.myTerm;
		myBacktrack = c.myBacktrack;
		myValue = c.myValue;
		myObject = c.myObject;
		myPriority = c.myPriority;
	}
	
	
	private void writeObject(ObjectOutputStream out) throws IOException {

		out.writeDouble(myPriority);
		out.writeObject(myTerm);
		
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		myPriority = in.readDouble();
		myTerm = (Term)in.readObject();

		myValue = Chart.semiring.Zero;
		myObject = null;
		myBacktrack = null;
	}

}
