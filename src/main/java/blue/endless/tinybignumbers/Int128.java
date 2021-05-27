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
	
	public Int128 subtract(Int128 other) {
		return add(other.negate());
	}
	
	public Int128 negate() {
		Int128 onesComplement = new Int128(~high, ~low);
		return onesComplement.add(ONE);
	}
	
	public Int128 abs() {
		return (isNegative()) ? negate() : this;
	}
	
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
	
	public Int128 divide(Int128 other) {
		if (other.isZero()) throw new ArithmeticException("Divide by zero");
		if (other.equals(ONE)) return this;
		
		return valueOf(toBigInteger().divide(other.toBigInteger())); //TODO: IMPLEMENT
	}
	
	public boolean isZero() {
		return low==0 && high==0;
	}
	
	public boolean isNegative() {
		return (high<0);
	}
	
	public int signum() {
		if (low==0 && high==0) return 0;
		if (high<0) return -1;
		return 1;
	}
	
	public long longValue() {
		return low;
	}
	
	public BigInteger toBigInteger() {
		/*
		 * One way to accomplish this:
		 * * strip the top bit of high into a signum
		 * * strip the top bit of low into a separate integer
		 * * shift the low, high-bit-of-low, and high, into the right places
		 * * negate the BigInteger if signum is positive
		 * 
		 * The problem with this approach is small, negative numbers.
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
	
	public static Int128 valueOf(long l) {
		if (l<0) {
			//sign-extend
			return new Int128(NEGATIVE_SIGN_EXTEND, l);
		} else {
			return new Int128(0, l);
		}
	}
	
	public String toString() {
		return toBigInteger().toString();
	}
}
