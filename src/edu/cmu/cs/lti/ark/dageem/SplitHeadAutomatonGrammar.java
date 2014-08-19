package edu.cmu.cs.lti.ark.dageem;

public abstract class SplitHeadAutomatonGrammar {

	public abstract double getAxiomDeltaRight(int head, int q, int r, int child);

	public abstract double getAxiomDeltaLeft(int head, int q, int r, int child);

	public abstract double getAxiomAttachmentRight(int i, int j, int head, int child, int r, int s_);

	public abstract double getAxiomAttachmentLeft(int i, int j, int head, int child, int r, int s_);

	public abstract double getAxiomDeltaStart(int head, int q);
	
	public abstract double getAxiomRoot(int i, int head);

	public abstract void timesAxiomDeltaRight(EValue v, int head, int q, int r, int child);

	public abstract void timesAxiomDeltaLeft(EValue v, int head, int q, int r, int child);

	public abstract void timesAxiomAttachmentRight(EValue v, int i, int j, int head, int child, int r, int s_);

	public abstract void timesAxiomAttachmentLeft(EValue v, int i, int j, int head, int child, int r, int s_);

	public abstract void timesAxiomDeltaStart(EValue v, int head, int q);
	
	public abstract void timesAxiomRoot(EValue v, int i, int head);
	
	public abstract int[] getStatesDeltaRight(int head, int q, int child);

	public abstract int[] getStatesDeltaLeft(int head, int q, int child);
	
	public abstract boolean isStateFinal(int q);
	
	public abstract int[] getFlipStates(int head);
	
	public abstract int[] getStatesInitial(int head);
	
	public abstract boolean isFlipState(int state);
	
	public abstract void setAxiomDeltaRight(int head, int q, int r, int child, double v);

	public abstract void setAxiomDeltaLeft(int head, int q, int r, int child, double v);

	public abstract void setAxiomAttachmentRight(int head, int child, int r, int s_, double v);

	public abstract void setAxiomAttachmentLeft(int head, int child, int r, int s_, double v);

	public abstract void setAxiomRoot(int head, double v);
	
	public abstract void setAxiomDeltaStart(int head, int q, double v);
}
