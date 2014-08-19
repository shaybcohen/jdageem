package edu.cmu.cs.lti.ark.dageem;

public abstract class LogisticNormalInsideOutsideInterface extends InsideOutsideInterface {

	public abstract LogisticNormalModel createModel();
	
	public abstract void getInsideOutside(Document doc, LogisticNormalVariationalInference varParams);
	
	public abstract double getInitLogPhi(LogisticNormalModel model, int i, int j);
	
	public abstract LogisticNormalVariationalInference createVariationalInferenceObject(LogisticNormalModel model, Document doc);

	public abstract boolean multinomialParticipates(Document doc, int i);
}
