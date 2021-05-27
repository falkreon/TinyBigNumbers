/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2021, Falkreon (Isaac Ellingson) falkreon@gmail.com
 */

package blue.endless.tinybignumbers;

/**
 * Fixed-Point decimal number, with 56 bits dedicated to whole numbers, and 8 bits dedicated to fractional numbers.
 * This has the effect of 1/256th resolution across the entire addressible space of 2^56-1..
 */
public record FixedPoint56Q8(long rawBits) {
	public static final FixedPoint56Q8 MAX_VALUE = new FixedPoint56Q8(0x7FFFFFFFFFFFFF_FFL);
	public static final FixedPoint56Q8 MIN_VALUE = new FixedPoint56Q8(0x80000000000000_01L);
	
	private static final long FRACTION_MASK = 0x00000000000000_FFL;
	private static final long WHOLE_MASK    = 0xFFFFFFFFFFFFFF_00L;
	
	public FixedPoint56Q8 add(FixedPoint56Q8 other) {
		return new FixedPoint56Q8(this.rawBits+other.rawBits);
	}
	
	public FixedPoint56Q8 add(long val) {
		return new FixedPoint56Q8(this.rawBits + (val<<8));
	}
	
	public FixedPoint56Q8 subtract(FixedPoint56Q8 other) {
		return new FixedPoint56Q8(this.rawBits-other.rawBits);
	}
	
	public FixedPoint56Q8 subtract(long val) {
		return new FixedPoint56Q8(this.rawBits - (val<<8));
	}
	
	public FixedPoint56Q8 multiply(FixedPoint56Q8 other) {
		return new FixedPoint56Q8(this.rawBits*other.rawBits);
	}
	
	public FixedPoint56Q8 multiply(long val) {
		return new FixedPoint56Q8(this.rawBits * (val<<8));
	}
	
	public FixedPoint56Q8 divide(FixedPoint56Q8 other) {
		return new FixedPoint56Q8(this.rawBits / other.rawBits);
	}
	
	public FixedPoint56Q8 divide(long val) {
		return new FixedPoint56Q8(this.rawBits / (val<<8));
	}
	
	public FixedPoint56Q8 shiftLeft(int numBits) {
		return new FixedPoint56Q8(this.rawBits << numBits);
	}
	
	public FixedPoint56Q8 shiftRight(int numBits) {
		return new FixedPoint56Q8(this.rawBits >> numBits);
	}
	
	public FixedPoint56Q8 and(FixedPoint56Q8 other) {
		return new FixedPoint56Q8(this.rawBits & other.rawBits);
	}
	
	public FixedPoint56Q8 or(FixedPoint56Q8 other) {
		return new FixedPoint56Q8(this.rawBits | other.rawBits); 
	}
	
	public FixedPoint56Q8 xor(FixedPoint56Q8 other) {
		return new FixedPoint56Q8(this.rawBits ^ other.rawBits);
	}
	
	public FixedPoint56Q8 pow(FixedPoint56Q8 other) {
		return new FixedPoint56Q8((long)Math.pow(this.rawBits, other.rawBits)); //TODO: Verify that this is correct
	}
	
	public int compare(FixedPoint56Q8 other) {
		return Long.compare(this.rawBits, other.rawBits);
	}
	
	public long getWholePart() {
		return rawBits >> 8;
	}
	
	public double getFractionalPart() {
		if (rawBits<0) {
			return (double) (rawBits | WHOLE_MASK) / 256.0D;
		} else {
			return (rawBits & FRACTION_MASK) / 256.0D;
		}
	}
	
	@Override
	public String toString() {
		return Double.toString(((double) rawBits) / 256.0D);
	}
	
	public static FixedPoint56Q8 valueOf(long l) {
		return new FixedPoint56Q8(l<<8);
	}
	
	public static FixedPoint56Q8 valueOf(double d) {
		return new FixedPoint56Q8((long) (d*256.0D));
	}
}
