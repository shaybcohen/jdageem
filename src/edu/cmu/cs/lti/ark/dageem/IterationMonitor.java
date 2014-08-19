/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

/**
 * @author scohen
 * 
 */
public class IterationMonitor {

	private int n;
	private int i;
	private int step, step2;
	private long currentTime;

	public IterationMonitor(int n) {
		reset(n);
	}

	public void reset() {
		i = 0;
	}

	public void reset(int n) {
		this.n = n;
		step = (n / 1000) + 1;
		step2 = (n / 10) + 1;

		reset();
	}

	public void update() {
		if (i == 0) {
			getMillis();
		}
		i++;

		if ((i % step) == 0) {
			System.err.print(".");
		}

		if ((i % step2) == 0) {
			if (((double)i / n) >= 0.1) {
				System.err.print("[" + ((i*100 / n)) + "%, "+(getMillis()/1000)+"s]");
			}
		}
	}
	
	public long getMillis()
	{
		long d = System.currentTimeMillis() - currentTime;
		currentTime = System.currentTimeMillis();
		
		return d;
	}
}
