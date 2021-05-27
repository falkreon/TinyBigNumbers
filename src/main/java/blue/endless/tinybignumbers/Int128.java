/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2021, Falkreon (Isaac Ellingson) falkreon@gmail.com
 */

package blue.endless.tinybignumbers;

import java.math.BigInteger;

/**
 * Stores a 128-bit number as a twos-complement signed
 */
public record Int128(long high, long low) {
	public static final Int128 ZERO = Int128.valueOf(0);
	public static final Int128 ONE  = Int128.valueOf(1);
	public static final Int128 NEGATIVE_ONE = Int128.valueOf(-1);
	public static final Int128 TEN = Int128.valueOf(10);
	
	private static final long INT_MASK = 0xFFFFFFFF;
	private static final long INT_SHIFT = 32;
	//We would normally say 0xFFFFFFFF_FFFFFFFF, but this is considered "out of range" to java compilers because long is a signed type.
	private static final long NEGATIVE_SIGN_EXTEND = -1L;
	
	/**
	 * Returns an Int128 whose value is {@code (this + other)}.
	 *
	 * @param  other value to be added to this Int128.
	 * @return {@code this + other}
	 */
	public Int128 add(Int128 other) {
		if (other.isZero()) return this;
		if (isZero()) return other;
		
		final long a = (low & INT_MASK) + (other.low & INT_MASK);
		final long ac = (a >>> INT_SHIFT) & INT_MASK;
		
		final long b = (low >>> INT_SHIFT)&INT_MASK + (other.low >>> INT_SHIFT)&INT_MASK + ac;
		final long bc = (b >>> INT_SHIFT) & INT_MASK;
		
		final long c = (high & INT_MASK) + (other.high & INT_MASK) + bc;
		final long cc = (c >>> INT_SHIFT) & INT_MASK;
		
		final long d = (high >>> INT_SHIFT)&INT_MASK + (other.high >>> INT_SHIFT)&INT_MASK + cc;
		
		final long sumHigh = ((d & INT_MASK) << INT_SHIFT) | (c & INT_MASK);
		final long sumLow  = ((b & INT_MASK) << INT_SHIFT) | (a & INT_MASK);
		
		return new Int128(sumHigh, sumLow);
	}
	
	/**
	 * Returns an Int128 whose value is {@code (this - other)}.
	 *
	 * @param  other value to be subtracted from this Int128.
	 * @return {@code this - other}
	 */
	public Int128 subtract(Int128 other) {
		return add(other.negate());
	}
	
	/**
	 * Returns an Int128 whose value is {@code (this * other)}.
	 *
	 * @param  other value to be multiplied by this Int128.
	 * @return {@code this * other}
	 */
	public Int128 multiply(Int128 other) {
		Int128 magnitude = multiplyPositive(this.abs(), other.abs());
		int sigA = this.signum();
		int sigB = other.signum();
		if (sigA==sigB || sigA==0 || sigB==0) return magnitude;
		return magnitude.negate();
	}
	
	private static Int128 multiplyPositive(Int128 x, Int128 y) {
		if (x.isZero() || y.isZero()) return ZERO;
		if (x.equals(ONE)) return y;
		if (y.equals(ONE)) return x;
		
		final long a = (x.low & INT_MASK) * (y.low & INT_MASK);
		final long ac = (a >>> INT_SHIFT) & INT_MASK;
		
		final long b = (x.low >>> INT_SHIFT)&INT_MASK * (y.low >>> INT_SHIFT)&INT_MASK + ac;
		final long bc = (b >>> INT_SHIFT) & INT_MASK;
		
		final long c = (x.high & INT_MASK) * (y.high & INT_MASK) + bc;
		final long cc = (c >>> INT_SHIFT) & INT_MASK;
		
		final long d = (x.high >>> INT_SHIFT)&INT_MASK * (y.high >>> INT_SHIFT)&INT_MASK + cc;
		
		final long productHigh = ((d & INT_MASK) << INT_SHIFT) | (c & INT_MASK);
		final long productLow  = ((b & INT_MASK) << INT_SHIFT) | (a & INT_MASK);
		
		return new Int128(productHigh, productLow);
	}
	
	/**
	 * Returns an Int128 whose value is {@code (this / other)}.
	 *
	 * @param  other value by which this Int128 is to be divided.
	 * @return {@code this / other}
	 */
	public Int128 divide(Int128 other) {
		if (other.isZero()) throw new ArithmeticException("Divide by zero");
		if (other.equals(ONE)) return this;
		
		/* Now that I've written this I somehow can't bring myself to implement division by hand */
		return valueOf(toBigInteger().divide(other.toBigInteger()));
	}
	
	/**
	 * Returns an Int128 whose value is the absolute value of this Int128
	 * @return {@code abs(this)}
	 */
	public Int128 abs() {
		return (isNegative()) ? negate() : this;
	}
	
	/**
	 * Returns an Int128 which is the two's-complement negative of this Int128.
	 * @return {@code -this}
	 */
	public Int128 negate() {
		Int128 onesComplement = new Int128(~high, ~low);
		return onesComplement.add(ONE);
	}
	
	/**
	 * Returns the signum function of this Int128.
	 * @return -1, 0 or 1 as the value of this Int128 is negative, zero or positive, respectively
	 */
	public int signum() {
		if (low==0 && high==0) return 0;
		if (high<0) return -1;
		return 1;
	}
	
	/**
	 * Returns true if this Int128's value is zero.
	 * @return true if {@code this.equals(ZERO)}, otherwise false.
	 */
	public boolean isZero() {
		return low==0 && high==0;
	}
	
	/**
	 * Returns true if this Int128's value is less than zero.
	 * @return {@code (this<ZERO)}
	 */
	public boolean isNegative() {
		return (high<0);
	}
	
	public Int128 shiftLeft(int spaces) {
		if (spaces==0) return this;
		//if (spaces<0) return shiftRight(-spaces);
		
		if (spaces==64) return new Int128(low, 0);
		
		if (spaces>64) {
			return new Int128(low<<(spaces-64), 0);
		}
		
		final long mask = (long) (Math.pow(2, spaces)-1); //May not be necessary but we're not taking chances
		final long a = high << spaces | ((low >>> (64-spaces))&mask);
		final long b = low << spaces;
		return new Int128(a, b);
	}
	
	/*//TODO: Implement
	public Int128 shiftRight(int spaces) {
		
	}*/
	
	/**
	 * Converts this Int128 to a {@code long}. This conversion is analogous to a
	 * <i>narrowing primitive conversion</i> from {@code long} to
	 * {@code int} as defined in <cite>The Java Language Specification</cite>:
	 * if this Int128 is too big to fit in a {@code long}, only the low-order 64
	 * bits are returned. Note that this conversion can lose information about the
	 * overall magnitude of the Int128 value as well as return a result with the
	 * opposite sign.
	 *
	 * @return this Int128 converted to a {@code long}.
	 * @see #longValueExact()
	 */
	public long longValue() {
		return low;
	}
	
	/**
	 * Converts this Int128 to a {@code long}, checking for lost information.
	 * If the value of this Int128 is out of the range of the {@code long} type,
	 * then an {@code ArithmeticException} is thrown.
	 *
	 * @return this Int128 converted to a {@code long}.
	 * @throws ArithmeticException if the value of {@code this} will not exactly
	 *         fit in a {@code long}.
	 * @see #longValue()
	 */
	public long longValueExact() throws ArithmeticException {
		if (high==0 && low>=0) {
			//If high is empty and we wouldn't swap signs, go for it
			return low;
		} else if (high==-1 && low<0) {
			//Likewise, if high is 0xFFFFFFFF_FFFFFFFF (a sign-extension, basically) and low won't flip signs to positive, go ahead.
			return low;
		} else {
			//We either have important information in high, or are using the sign bit in low, so the number won't fit.
			throw new ArithmeticException("Int128 out of long range");
		}
	}
	
	/**
	 * Converts this Int128 to a BigInteger.
	 * @return a BigInteger whose value is exactly the value of this Int128
	 */
	public BigInteger toBigInteger() {
		/*
		 * There are other ways to shift the bits around but it turns out this is really the most reliable way to get it done.
		 */
		
		byte[] val = new byte[16];
		val[ 0] = (byte) (high >>> 56);
		val[ 1] = (byte) (high >>> 48);
		val[ 2] = (byte) (high >>> 40);
		val[ 3] = (byte) (high >>> 32);
		val[ 4] = (byte) (high >>> 24);
		val[ 5] = (byte) (high >>> 16);
		val[ 6] = (byte) (high >>>  8);
		val[ 7] = (byte) (high);
		
		val[ 8] = (byte) (low >>> 56);
		val[ 9] = (byte) (low >>> 48);
		val[10] = (byte) (low >>> 40);
		val[11] = (byte) (low >>> 32);
		val[12] = (byte) (low >>> 24);
		val[13] = (byte) (low >>> 16);
		val[14] = (byte) (low >>>  8);
		val[15] = (byte) (low);
		
		return new BigInteger(val);
	}
	
	/**
	 * Creates an Int128 whose value is the value of the specified BigInteger.
	 * This conversion is analogous to a <i>narrowing primitive conversion</i>
	 * from {@code long} to {@code int} as defined in
	 * <cite>The Java Language Specification</cite>: if the BigInteger is too
	 * big to fit in an Int128, only the low-order 128 bits are returned. Note
	 * that this conversion can lose information about the overall magnitude of
	 * the Int128 value, but the sign <em>is preserved</em>.
	 *
	 * @param b The BigInteger whose value should be returned
	 * @return An Int128 containing the value of the provided BigInteger
	 */
	public static Int128 valueOf(BigInteger b) {
		BigInteger positive = b.abs();
		
		long high = positive.shiftRight(64).and(BigInteger.valueOf(-1)).longValue();
		long low = positive.and(BigInteger.valueOf(-1)).longValue();
		
		if (b.signum()==-1) {
			return new Int128(high, low).negate();
		} else {
			return new Int128(high, low);
		}
	}
	
	/**
	 * Creates an Int128 whose value is equal to that of the specified {@code long}.
	 * @param l value of the Int128 to return
	 * @return an Int128 with the specified value
	 */
	public static Int128 valueOf(long l) {
		if (l==0) return ZERO;
		if (l==1) return ONE;
		if (l==-1) return NEGATIVE_ONE;
		if (l==10) return TEN;
		
		if (l<0) {
			//sign-extend
			return new Int128(NEGATIVE_SIGN_EXTEND, l);
		} else {
			return new Int128(0, l);
		}
	}
	
	public String toString() {
		return toBigInteger().toString(); //Yeah, it's a copout. We'll do better later.
	}
}
