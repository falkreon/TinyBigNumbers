/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2021, Falkreon (Isaac Ellingson) falkreon@gmail.com
 */

package blue.endless.tinybignumbers;

import java.math.BigInteger;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestInt128 {
	
	@Test
	public void testBackAndForth() {
		BigInteger prime = BigInteger.probablePrime(127, new Random()); //At 128 bits we could get some sign problems
		Int128 prime128 = Int128.valueOf(prime);
		BigInteger test = prime128.toBigInteger();
		
		Assertions.assertEquals(prime, test);
	}
	
	
	@Test
	public void testTwosComplement() {
		Random rnd = new Random();
		for(int i=0; i<100; i++) {
			long foo = Math.abs(rnd.nextLong());
			long bar = Int128.valueOf(foo).negate().longValue();
			
			Assertions.assertEquals(-foo, bar);
		}
	}
	
	@Test
	public void testMultiply() {
		Random rnd = new Random();
		for(int i=0; i<100; i++) {
			long bar = rnd.nextInt(1000)+1;
			long foo = rnd.nextLong()/bar;
			
			
			BigInteger productInt128 = Int128.valueOf(foo).multiply(Int128.valueOf(bar)).toBigInteger();
			BigInteger productBigInt = BigInteger.valueOf(foo).multiply(BigInteger.valueOf(bar));
			
			Assertions.assertEquals(productBigInt, productInt128);
		}
	}
}
