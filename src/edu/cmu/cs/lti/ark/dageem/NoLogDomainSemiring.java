package edu.cmu.cs.lti.ark.dageem;


public class NoLogDomainSemiring extends Semiring {

	static {
		Zero = 0;
		One = 1;
	}

	public static double convertToSemiring(double v) {
		return Math.exp(v);
	}

	public static double convertFromSemiring(double v) {
		return Math.log(v);
	}

	public static double Times(double v1, double v2) {
		return v1*v2;
	}

	public static double Plus(double v1, double v2) {
		if (semiring == Semiring_Max) {
			if (v1 > v2) {
				return v1;
			} else {
				return v2;
			}
		}

		return v1+v2;
	}

	public static double ETimes(double p1, double p2, double r1, double r2) {
		double v = p1*r2 + p2*r1;
		
		return v;
	}

	public static double exp(double val) {
		final long tmp = (long) (1512775 * val + 1072632447);
		return Double.longBitsToDouble(tmp << 32);
	}

}
