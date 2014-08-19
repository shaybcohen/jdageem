/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.util.StringTokenizer;

/**
 * @author scohen
 * 
 */
public class EisnerSattaAlgorithm extends AgendaAlgorithm {

	private SplitHeadAutomatonGrammar grammar;

	private boolean useBacktrack;

	private EValue poppedSecondary;

	private int[] words;

	public EisnerSattaAlgorithm(SplitHeadAutomatonGrammar g,
			boolean useSecondary) {
		super(useSecondary);
		grammar = g;
		useBacktrack = (Chart.semiring.Idempotent());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cmu.cs.lti.ark.dageem.AgendaAlgorithm#processedPoppedTerm(edu.cmu
	 * .cs.lti.ark.dageem.Term, int, double)
	 */
	@Override
	public void processedPoppedTerm(Term poppedTerm, int poppedHash,
			double poppedValue, EValue poppedSecondary, double priority) {

		this.poppedSecondary = poppedSecondary;

		/*if (Chart.semiring.Idempotent()) {
			System.err.println("popping " + poppedTerm + " with value "
					+ poppedValue);
		}*/
		
		if (poppedTerm instanceof RTriangleTerm) {
			createRTrapFromRTriangle_AttachRight(poppedTerm, poppedValue);
			createLTrapFromRTriangle_AttachLeft(poppedTerm, poppedValue);
			createRTriangleFromRTriangle_CompleteRight(poppedTerm, poppedValue);
			createGoalFromRTriangle_Goal(poppedTerm, poppedValue);
		}

		if (poppedTerm instanceof LTriangleTerm) {
			createRTrapFromLTriangle_AttachRight(poppedTerm, poppedValue);
			createLTrapFromLTriangle_AttachLeft(poppedTerm, poppedValue);
			createLTriangleFromLTriangle_CompleteLeft(poppedTerm, poppedValue);
			createGoalFromLTriangle_Goal(poppedTerm, poppedValue);
		}

		if (poppedTerm instanceof RTrapTerm) {
			createRTriangleFromRTrap_CompleteRight(poppedTerm, poppedValue);
		}

		if (poppedTerm instanceof LTrapTerm) {
			createLTriangleFromLTrap_CompleteLeft(poppedTerm, poppedValue);
		}
	}

	public int wordAt(int w) {
		assert w <= words.length;

		return words[w - 1];
	}

	public void assertSentence(String s, Alphabet a) {
		StringTokenizer st = new StringTokenizer(s);

		words = new int[st.countTokens()];

		chart().setLength(words.length);

		int tokens = st.countTokens();

		for (int i = 0; i < tokens; i++) {
			words[i] = a.lookupIndex(st.nextToken());

			assertWord(i + 1);
		}
	}

	public void assertSentence(SentenceDocument d) {
		words = new int[d.length()];

		chart().setLength(words.length);

		for (int i = 0; i < d.length(); i++) {
			words[i] = d.wordAt(i);
			assertWord(i + 1);
		}
	}

	public void assertWord(int h) {
		for (int q : grammar.getStatesInitial(wordAt(h))) {
			RTriangleTerm rtriangleTerm = new RTriangleTerm(q, h, h);
			rtriangleTerm.setValue(Chart.semiring.Zero);
			chart().addTerm(rtriangleTerm);

			EValue secondaryValue = null;
			if (useSecondary) {
				// corresponds to expectation semiring 1 = < p=1, r=0 >
				secondaryValue = new EValue();
				secondaryValue.setOne();
				grammar.timesAxiomDeltaStart(secondaryValue, wordAt(h), q);
				//rtriangleTerm.setSecondaryValue(secondaryValue);
			}

			// null should be a chain
			agenda.markAgendaItem(rtriangleTerm,
					grammar.getAxiomDeltaStart(wordAt(h), q), secondaryValue,
					null, true);
		}

		for (int s : grammar.getFlipStates(wordAt(h))) {

			LTriangleTerm ltriangleTerm = new LTriangleTerm(s, h, h, s);
			ltriangleTerm.setValue(Chart.semiring.Zero);
			chart().addTerm(ltriangleTerm);

			EValue secondaryValue = null;
			if (useSecondary) {
				// corresponds to expectation semiring 1 = < p=1, r=0 >
				secondaryValue = new EValue();
				secondaryValue.setOne();
				//ltriangleTerm.setSecondaryValue(secondaryValue);
			}

			// null should be a chain
			agenda.markAgendaItem(ltriangleTerm, Chart.semiring.One,
					secondaryValue, null, true);
		}
	}

	public int[] backtrack() {
		int[] dep = new int[words.length];

		for (int i = 0; i < dep.length; i++) {
			dep[i] = -1;
		}


		return backtrackHelper(dep, chart().getTerm(GoalTerm.hashcode(0)));
	}

	public int[] backtrackHelper(int[] dep, Term t) {
		BacktrackChain bt = chart().getBacktrack(t.hashcode());
		
		if (bt == null) {
			return dep;
		}

		if (t instanceof RTrapTerm) {
			RTrapTerm rtrap = (RTrapTerm) t;

			dep[rtrap.h_() - 1] = rtrap.h() - 1;
		}

		if (t instanceof LTrapTerm) {
			LTrapTerm ltrap = (LTrapTerm) t;

			dep[ltrap.h_() - 1] = ltrap.h() - 1;
		}

		return backtrackHelper(
				backtrackHelper(dep, chart().getTerm(bt.get(1))), chart()
						.getTerm(bt.get(0)));
	}

	public EisnerSattaChart chart() {
		return (EisnerSattaChart) chart;
	}

	public boolean isGoal(Term t) {
		if (t instanceof GoalTerm) {
			/*System.err.println("value = " + t.value());
			int[] dep = backtrack();
			for (int i=0; i<dep.length; i++)
			{
				System.err.print((dep[i]+1)+" ");
			}
			System.err.println();*/

			return true;
		}
		return false;
	}

	EValue safeSecondaryCopy() {
		if (poppedSecondary == null) {
			return new EValue();
		} else {
			return poppedSecondary.getCopy();
			//return poppedSecondary;
		}
	}

	public void createRTrapFromRTriangle_AttachRight(Term term,
			double triangleValue) {

		RTriangleTerm rtriangleTerm = (RTriangleTerm) term;

		int q = rtriangleTerm.q();
		int h = rtriangleTerm.h();
		int i = rtriangleTerm.j() + 1;

		ChartIterator ltriangles = chart().getLTrianglesFrom_i_FinalState(i);

		while (ltriangles.hasNext()) {
			LTriangleTerm ltriangleTerm = (LTriangleTerm) ltriangles.getTerm();

			if (ltriangleTerm.value() == Chart.semiring.Zero) { continue; } 
			
			int h_ = ltriangleTerm.h();
			int s_ = ltriangleTerm.s();

			int[] r_states = grammar.getStatesDeltaRight(wordAt(h), q,
					wordAt(h_));

			for (int j = 0; j < r_states.length; j++) {
				RTrapTerm rtrapTerm = RTrapTerm.addToChart(chart(),
						r_states[j], h, h_, s_);

				double value = Chart.semiring.Times(Chart.semiring.Times(
						triangleValue, ltriangleTerm.value()), Chart.semiring
						.Times(grammar.getAxiomDeltaRight(wordAt(h), q,
								r_states[j], wordAt(h_)), grammar
								.getAxiomAttachmentRight(h, h_, wordAt(h),
										wordAt(h_), r_states[j], s_)));

				EValue secondaryValue = null;

				if (useSecondary) {
					secondaryValue = safeSecondaryCopy();
					secondaryValue.Times(triangleValue, ltriangleTerm);

					grammar.timesAxiomDeltaRight(secondaryValue, wordAt(h), q,
							r_states[j], wordAt(h_));
					grammar.timesAxiomAttachmentRight(secondaryValue, h, h_,
							wordAt(h), wordAt(h_), r_states[j], s_);
				}

				BacktrackChain chain = null;
				if (useBacktrack) {
					chain = new BacktrackChain(2);
					chain.set(0, rtriangleTerm.hashcode());
					chain.set(1, ltriangleTerm.hashcode());
				}

				agenda.markAgendaItem(rtrapTerm, value, secondaryValue, chain);
			}
		}
	}

	public void createRTrapFromLTriangle_AttachRight(Term term,
			double triangleValue) {

		LTriangleTerm ltriangleTerm = (LTriangleTerm) term;

		int F = ltriangleTerm.q();

		if (!grammar.isStateFinal(F)) {
			return;
		}

		int i = ltriangleTerm.i();
		int h_ = ltriangleTerm.h();
		int s_ = ltriangleTerm.s();

		ChartIterator rtriangles = chart().getRTrianglesFrom_j(i - 1);

		while (rtriangles.hasNext()) {
			RTriangleTerm rtriangleTerm = (RTriangleTerm) rtriangles.getTerm();

			if (rtriangleTerm.value() == Chart.semiring.Zero) { continue; } 
			
			int h = rtriangleTerm.h();
			int q = rtriangleTerm.q();

			int[] r_states = grammar.getStatesDeltaRight(wordAt(h), q,
					wordAt(h_));

			for (int j = 0; j < r_states.length; j++) {
				RTrapTerm rtrapTerm = RTrapTerm.addToChart(chart(),
						r_states[j], h, h_, s_);

				double value = Chart.semiring.Times(Chart.semiring.Times(
						triangleValue, rtriangleTerm.value()), Chart.semiring
						.Times(grammar.getAxiomDeltaRight(wordAt(h), q,
								r_states[j], wordAt(h_)), grammar
								.getAxiomAttachmentRight(h, h_, wordAt(h),
										wordAt(h_), r_states[j], s_)));

				EValue secondaryValue = null;

				if (useSecondary) {
					secondaryValue = safeSecondaryCopy();
					secondaryValue.Times(triangleValue, rtriangleTerm);
					grammar.timesAxiomDeltaRight(secondaryValue, wordAt(h), q,
							r_states[j], wordAt(h_));
					grammar.timesAxiomAttachmentRight(secondaryValue, h, h_,
							wordAt(h), wordAt(h_), r_states[j], s_);
				}

				BacktrackChain chain = null;
				if (useBacktrack) {
					chain = new BacktrackChain(2);
					chain.set(0, rtriangleTerm.hashcode());
					chain.set(1, ltriangleTerm.hashcode());
				}

				agenda.markAgendaItem(rtrapTerm, value, secondaryValue, chain);
			}
		}
	}

	public void createLTrapFromRTriangle_AttachLeft(Term term,
			double triangleValue) {

		RTriangleTerm rtriangleTerm = (RTriangleTerm) term;

		int s_ = rtriangleTerm.q();

		if (!grammar.isFlipState(s_)) {
			return;
		}

		int h_ = rtriangleTerm.h();
		int i = rtriangleTerm.j();

		ChartIterator ltriangles = chart().getLTrianglesFrom_i(i + 1);

		while (ltriangles.hasNext()) {
			LTriangleTerm ltriangleTerm = (LTriangleTerm) ltriangles.getTerm();

			if (ltriangleTerm.value() == Chart.semiring.Zero) { continue; } 

			int h = ltriangleTerm.h();
			int s = ltriangleTerm.s();
			int q = ltriangleTerm.q();

			int[] r_states = grammar.getStatesDeltaLeft(wordAt(h), q,
					wordAt(h_));

			for (int j = 0; j < r_states.length; j++) {
				LTrapTerm ltrapTerm = LTrapTerm.addToChart(chart(),
						r_states[j], h_, h, s_, s);

				double value = Chart.semiring.Times(Chart.semiring.Times(
						triangleValue, ltriangleTerm.value()), Chart.semiring
						.Times(grammar.getAxiomDeltaLeft(wordAt(h), q,
								r_states[j], wordAt(h_)), grammar
								.getAxiomAttachmentLeft(h, h_, wordAt(h),
										wordAt(h_), r_states[j], s))); // should
																		// it be
																		// s or
																		// s_?

				EValue secondaryValue = null;

				if (useSecondary) {
					secondaryValue = safeSecondaryCopy();
					secondaryValue.Times(triangleValue, ltriangleTerm);
					grammar.timesAxiomDeltaLeft(secondaryValue, wordAt(h), q,
							r_states[j], wordAt(h_));
					grammar.timesAxiomAttachmentLeft(secondaryValue, h, h_,
							wordAt(h), wordAt(h_), r_states[j], s);
				}

				BacktrackChain chain = null;
				if (useBacktrack) {
					chain = new BacktrackChain(2);
					chain.set(0, rtriangleTerm.hashcode());
					chain.set(1, ltriangleTerm.hashcode());
				}

				agenda.markAgendaItem(ltrapTerm, value, secondaryValue, chain);
			}
		}
	}

	public void createLTrapFromLTriangle_AttachLeft(Term term,
			double triangleValue) {

		LTriangleTerm ltriangleTerm = (LTriangleTerm) term;

		int q = ltriangleTerm.q();
		int s = ltriangleTerm.s();
		int h = ltriangleTerm.h();
		int i = ltriangleTerm.i() - 1;

		ChartIterator rtriangles = chart().getRTrianglesFrom_j(i);

		while (rtriangles.hasNext()) {
			RTriangleTerm rtriangleTerm = (RTriangleTerm) rtriangles.getTerm();

			if (rtriangleTerm.value() == Chart.semiring.Zero) { continue; } 

			int s_ = rtriangleTerm.q();

			if (grammar.isFlipState(s_)) {

				int h_ = rtriangleTerm.h();

				int[] r_states = grammar.getStatesDeltaLeft(wordAt(h), q,
						wordAt(h_));

				for (int j = 0; j < r_states.length; j++) {
					LTrapTerm ltrapTerm = LTrapTerm.addToChart(chart(),
							r_states[j], h_, h, s_, s);

					double value = Chart.semiring.Times(Chart.semiring.Times(
							triangleValue, rtriangleTerm.value()),
							Chart.semiring.Times(grammar.getAxiomDeltaLeft(
									wordAt(h), q, r_states[j], wordAt(h_)),
									grammar.getAxiomAttachmentLeft(h, h_,
											wordAt(h), wordAt(h_), r_states[j],
											s))); // should
													// it
													// be
													// s
													// or
													// s_?

					EValue secondaryValue = null;

					if (useSecondary) {
						secondaryValue = safeSecondaryCopy();
						secondaryValue.Times(triangleValue, rtriangleTerm);
						grammar.timesAxiomDeltaLeft(secondaryValue, wordAt(h),
								q, r_states[j], wordAt(h_));
						grammar.timesAxiomAttachmentLeft(secondaryValue, h, h_,
								wordAt(h), wordAt(h_), r_states[j], s);
					}

					// BacktrackChain chain = new BacktrackChain(2);
					// chain.set(0, rtriangleTerm.hashcode());
					// chain.set(1, ltriangleTerm.hashcode());

					BacktrackChain chain = null;
					if (useBacktrack) {
						chain = new BacktrackChain(2);
						chain.set(0, rtriangleTerm.hashcode());
						chain.set(1, ltriangleTerm.hashcode());
					}

					agenda.markAgendaItem(ltrapTerm, value, secondaryValue,
							chain);
				}
			}
		}

	}

	public void createRTriangleFromRTrap_CompleteRight(Term term,
			double trapValue) {

		RTrapTerm rtrapTerm = (RTrapTerm) term;

		int q = rtrapTerm.q();
		int s_ = rtrapTerm.s_();
		int h = rtrapTerm.h();
		int h_ = rtrapTerm.h_();

		ChartIterator rtriangles = chart().getRTrianglesFrom_h_q(h_, s_);

		while (rtriangles.hasNext()) {

			RTriangleTerm rtriangleTerm = (RTriangleTerm) rtriangles.getTerm();

			if (rtriangleTerm.value() == Chart.semiring.Zero) { continue; } 

			int i = rtriangleTerm.j();

			RTriangleTerm rtriangleTerm_consequent = RTriangleTerm.addToChart(
					chart(), q, h, i);

			double value = Chart.semiring.Times(trapValue,
					rtriangleTerm.value());

			EValue secondaryValue = null;

			if (useSecondary) {
				secondaryValue = safeSecondaryCopy();
				secondaryValue.Times(trapValue, rtriangleTerm);
			}

			BacktrackChain chain = null;
			if (useBacktrack) {
				chain = new BacktrackChain(2);
				chain.set(0, rtrapTerm.hashcode());
				chain.set(1, rtriangleTerm.hashcode());
			}

			agenda.markAgendaItem(rtriangleTerm_consequent, value,
					secondaryValue, chain);

		}
	}

	public void createRTriangleFromRTriangle_CompleteRight(Term term,
			double triangleValue) {

		RTriangleTerm rtriangleTerm = (RTriangleTerm) term;

		int s_ = rtriangleTerm.q();
		int i = rtriangleTerm.j();
		int h_ = rtriangleTerm.h();

		ChartIterator rtraps = chart().getRTrapsFrom_h__s_(h_, s_);

		while (rtraps.hasNext()) {

			RTrapTerm rtrapTerm = (RTrapTerm) rtraps.getTerm();

			if (rtrapTerm.value() == Chart.semiring.Zero) { continue; } 

			int q = rtrapTerm.q();
			int h = rtrapTerm.h();

			RTriangleTerm rtriangleTerm_consequent = RTriangleTerm.addToChart(
					chart(), q, h, i);

			double value = Chart.semiring.Times(triangleValue,
					rtrapTerm.value());

			EValue secondaryValue = null;

			if (useSecondary) {
				secondaryValue = safeSecondaryCopy();
				secondaryValue.Times(triangleValue, rtrapTerm);
			}

			BacktrackChain chain = null;
			if (useBacktrack) {
				chain = new BacktrackChain(2);
				chain.set(0, rtrapTerm.hashcode());
				chain.set(1, rtriangleTerm.hashcode());
			}

			agenda.markAgendaItem(rtriangleTerm_consequent, value,
					secondaryValue, chain);

		}

	}

	public void createLTriangleFromLTriangle_CompleteLeft(Term term,
			double triangleValue) {

		LTriangleTerm ltriangleTerm = (LTriangleTerm) term;

		int F = ltriangleTerm.q();

		if (!grammar.isStateFinal(F)) {
			return;
		}

		int s_ = ltriangleTerm.s();
		int h_ = ltriangleTerm.h();
		int i = ltriangleTerm.i();

		ChartIterator ltraps = chart().getLTrapsFrom_h__s_(h_, s_);

		while (ltraps.hasNext()) {

			LTrapTerm ltrapTerm = (LTrapTerm) ltraps.getTerm();

			if (ltrapTerm.value() == Chart.semiring.Zero) { continue; } 
			
			int q = ltrapTerm.q();
			int s = ltrapTerm.s();
			int h = ltrapTerm.h();

			LTriangleTerm ltriangleTerm_consequent = LTriangleTerm.addToChart(
					chart(), q, i, h, s);

			double value = Chart.semiring.Times(triangleValue,
					ltrapTerm.value());

			EValue secondaryValue = null;

			if (useSecondary) {
				secondaryValue = safeSecondaryCopy();
				secondaryValue.Times(triangleValue, ltrapTerm);
			}

			BacktrackChain chain = null;
			if (useBacktrack) {
				chain = new BacktrackChain(2);
				chain.set(0, ltrapTerm.hashcode());
				chain.set(1, ltriangleTerm.hashcode());
			}

			agenda.markAgendaItem(ltriangleTerm_consequent, value,
					secondaryValue, chain);

		}

	}

	public void createLTriangleFromLTrap_CompleteLeft(Term term,
			double trapValue) {

		LTrapTerm ltrapTerm = (LTrapTerm) term;

		int q = ltrapTerm.q();
		int s = ltrapTerm.s();
		int h = ltrapTerm.h();
		int h_ = ltrapTerm.h_();
		int s_ = ltrapTerm.s_();

		ChartIterator ltriangles = chart().getLTrianglesFrom_h_s_FinalState(h_,
				s_);

		while (ltriangles.hasNext()) {

			LTriangleTerm ltriangleTerm = (LTriangleTerm) ltriangles.getTerm();

			if (ltriangleTerm.value() == Chart.semiring.Zero) { continue; } 

			int i = ltriangleTerm.i();

			LTriangleTerm ltriangleTerm_consequent = LTriangleTerm.addToChart(
					chart(), q, i, h, s);

			double value = Chart.semiring.Times(trapValue,
					ltriangleTerm.value());

			EValue secondaryValue = null;

			if (useSecondary) {
				secondaryValue = safeSecondaryCopy();
				secondaryValue.Times(trapValue, ltriangleTerm);
			}

			// BacktrackChain chain = new BacktrackChain(2);
			// chain.set(0, rtriangleTerm.hashcode());
			// chain.set(1, ltriangleTerm.hashcode());
			BacktrackChain chain = null;
			if (useBacktrack) {
				chain = new BacktrackChain(2);
				chain.set(0, ltrapTerm.hashcode());
				chain.set(1, ltriangleTerm.hashcode());
			}

			agenda.markAgendaItem(ltriangleTerm_consequent, value,
					secondaryValue, chain);

		}
	}

	public void createGoalFromLTriangle_Goal(Term term, double triangleValue) {

		LTriangleTerm ltriangleTerm = (LTriangleTerm) term;

		int i = ltriangleTerm.i();

		if (i > 1) {
			return;
		}

		int F = ltriangleTerm.q();

		if (!grammar.isStateFinal(F)) {
			return;
		}

		int h = ltriangleTerm.h();
		int s = ltriangleTerm.s();

		ChartIterator rtriangles = chart().getRTrianglesFrom_h_q_n(h, s);

		while (rtriangles.hasNext()) {

			RTriangleTerm rtriangleTerm = (RTriangleTerm) rtriangles.getTerm();

			if (rtriangleTerm.value() == Chart.semiring.Zero) { continue; } 

			GoalTerm goalTerm = GoalTerm.addToChart(chart());

			double value = Chart.semiring.Times(Chart.semiring.Times(
					grammar.getAxiomRoot(h, wordAt(h)), triangleValue),
					rtriangleTerm.value());

			EValue secondaryValue = null;

			if (useSecondary) {
				secondaryValue = safeSecondaryCopy();
				secondaryValue.Times(triangleValue, rtriangleTerm);
				grammar.timesAxiomRoot(secondaryValue, h, wordAt(h));
			}

			BacktrackChain chain = null;
			if (useBacktrack) {
				chain = new BacktrackChain(2);
				chain.set(0, ltriangleTerm.hashcode());
				chain.set(1, rtriangleTerm.hashcode());
			}

			agenda.markAgendaItem(goalTerm, value, secondaryValue, chain);

		}
	}

	public int getSentenceLength() {
		return words.length;
	}

	public void createGoalFromRTriangle_Goal(Term term, double triangleValue) {

		RTriangleTerm rtriangleTerm = (RTriangleTerm) term;

		int n = rtriangleTerm.j();

		if (n < getSentenceLength()) {
			return;
		}

		int h = rtriangleTerm.h();
		int s = rtriangleTerm.q();

		ChartIterator ltriangles = chart().getLTrianglesFrom_h_s_1_FinalState(
				h, s);

		while (ltriangles.hasNext()) {

			LTriangleTerm ltriangleTerm = (LTriangleTerm) ltriangles.getTerm();

			if (ltriangleTerm.value() == Chart.semiring.Zero) { continue; } 

			GoalTerm goalTerm = GoalTerm.addToChart(chart());

			double value = Chart.semiring.Times(Chart.semiring.Times(
					grammar.getAxiomRoot(h, wordAt(h)), triangleValue),
					ltriangleTerm.value());

			EValue secondaryValue = null;

			if (useSecondary) {
				secondaryValue = safeSecondaryCopy();
				secondaryValue.Times(triangleValue, ltriangleTerm);
				grammar.timesAxiomRoot(secondaryValue, h, wordAt(h));
			}

			BacktrackChain chain = null;
			if (useBacktrack) {
				chain = new BacktrackChain(2);
				chain.set(0, ltriangleTerm.hashcode());
				chain.set(1, rtriangleTerm.hashcode());
			}

			// BacktrackChain chain = new BacktrackChain(2);
			// chain.set(0, rtriangleTerm.hashcode());
			// chain.set(1, ltriangleTerm.hashcode());

			agenda.markAgendaItem(goalTerm, value, secondaryValue, chain);

		}
	}
}
