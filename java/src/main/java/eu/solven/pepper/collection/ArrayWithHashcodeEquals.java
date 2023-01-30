/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package eu.solven.pepper.collection;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * This class is useful when one want to consider a generic List of Object as key in a hashed structure.
 *
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.AvoidProtectedFieldInFinalClass" })
public final class ArrayWithHashcodeEquals {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArrayWithHashcodeEquals.class);

	public static final int COLLISION_COUNT_LOG = 1_000_000;

	// precompute the hashcode for performance consideration
	private final int arrayHash;

	@VisibleForTesting
	protected final Object[] array;

	private static final AtomicLong COLLISION_COUNTER = new AtomicLong();

	public ArrayWithHashcodeEquals(Object... array) {
		// We do not copy the array, but this originating array should not be mutated
		this.array = array;

		// Accept null array
		arrayHash = Objects.hash(array);
	}

	@Override
	public int hashCode() {
		return arrayHash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ArrayWithHashcodeEquals other = (ArrayWithHashcodeEquals) obj;
		if (arrayHash != other.arrayHash) {
			return false;
		}
		if (!Arrays.equals(array, other.array)) {
			incrementCollision();
			return false;
		}
		return true;
	}

	/**
	 * It may be useful to log if we have many collisions on this data-structure
	 */
	private static void incrementCollision() {
		if (0 == COLLISION_COUNTER.incrementAndGet() % COLLISION_COUNT_LOG) {
			LOGGER.warn("{} collisions on {}", COLLISION_COUNTER, ArrayWithHashcodeEquals.class);
		}
	}

}
