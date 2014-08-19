package edu.cmu.cs.lti.ark.dageem;

import org.apache.commons.math.util.FastMath;

public class Semiring {

	public static double Zero = java.lang.Double.NEGATIVE_INFINITY;
	public static double One = 0;
	public static int Semiring_Max = 1;
	public static int Semiring_LogReal = 2;
	public static int semiring = Semiring_Max;

	public static int getSemiring() {
		return semiring;
	}

	public static void setSemiring(int s) {
		semiring = s;
	}

	public static void setSemiringLogReal() {
		semiring = Semiring_LogReal;
	}

	public static double convertToSemiring(double v) {
		return v;
	}

	public static double convertFromSemiring(double v) {
		return v;
	}

	public static void setSemiringMax() {
		semiring = Semiring_Max;
	}

	public static boolean Idempotent() {
		return (semiring == Semiring_Max);
	}

	public static double Times(double v1, double v2) {
		return (v1 + v2);
	}

	public static double Plus(double v1, double v2) {
		if (semiring == Semiring_Max) {
			if (v1 > v2) {
				return v1;
			} else {
				return v2;
			}
		}

		return LogSum(v1, v2);
	}

	public static double LogMinus(double lx, double ly) {

		if (ly == Zero || lx - ly > 700.0)
			return lx;
		if (lx == Zero || ly - lx > 700.0) {
			System.err.println("Warning: LogMinus gives a negative answer: lx="
					+ lx + " ly=" + ly);
			return Zero;
		}

		if (lx > ly) {
			return FastMath.log1p(-FastMath.exp(ly - lx)) + lx;
		} else {
			System.err.println("Warning: LogMinus gives a negative answer: lx="
					+ lx + " ly=" + ly);
			return Zero;
		}
	}

	public static double LogDivide(double v1, double v2) {
		return (v1 - v2);
	}

	public static double ETimes(double p1, double p2, double r1, double r2) {
		double v = LogSum(Times(p1, r2), Times(p2, r1));

		return v;
	}

	public static double LogSum(double lx, double ly) {
		if (lx == Zero)
			return ly;
		if (ly == Zero)
			return lx;
		double d = lx - ly;
		if (d >= 0) {
			if (d > 745)
				return lx;
			else
				return lx + FastMath.log1p(FastMath.exp(-d));
		} else {
			if (d < -745)
				return ly;
			else
				return ly + FastMath.log1p(FastMath.exp(d));
		}
	}

	public static double exp(double val) {
		final long tmp = (long) (1512775 * val + 1072632447);
		return Double.longBitsToDouble(tmp << 32);
	}

}
