/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle
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
package cormoran.pepper.memory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cormoran.pepper.io.PepperURLHelper;
import cormoran.pepper.logging.PepperLogHelper;

public class TestPepperReferenceHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestPepperReferenceHelper.class);

	PepperReferenceInternalizer pepperReferenceInternalizer = new PepperReferenceInternalizer();

	@Before
	public void clear() {
		PepperReferenceHelper.clear();
	}

	@Test
	public void testNullRef() {
		PepperReferenceHelper.internalizeFields(null);
		PepperReferenceHelper.internalizeArray(null);
		PepperReferenceHelper.dictionarizeIterable(null);
	}

	@Test
	public void testDictionarisationOnFinal() {
		FinalField left = new FinalField("Youpi");
		FinalField right = new FinalField(new String("Youpi"));

		// Not same ref
		Assert.assertNotSame(left.oneString, right.oneString);

		PepperReferenceHelper.internalizeFields(left);
		PepperReferenceHelper.internalizeFields(right);

		Assert.assertSame(left.oneString, right.oneString);
	}

	@Test
	public void testDictionarisationOnNotFinal() {
		NotFinalField left = new NotFinalField("Youpi");
		NotFinalField right = new NotFinalField(new String("Youpi"));

		// Not same ref
		Assert.assertNotSame(left.oneString, right.oneString);

		PepperReferenceHelper.internalizeFields(left);
		PepperReferenceHelper.internalizeFields(right);

		Assert.assertSame(left.oneString, right.oneString);
	}

	@Test
	public void testDictionarisationOnNotFinal_high_cardinality() {
		for (int i = 0; i < 10000; i++) {
			NotFinalField left = new NotFinalField("Youpi" + i);

			PepperReferenceHelper.internalize(left);
		}

		// We need to ensure a high cardinality Field does not lead to a huge dictionary
		Assert.assertEquals(775,
				PepperReferenceHelper.DICTIONARY_FIELDS.get(NotFinalField.class.getDeclaredFields()[0]).size());
	}

	@Test
	public void testDictionarisationOnNotFinal_veryhigh_cardinality() {
		for (int i = 0; i < 100000; i++) {
			NotFinalField left = new NotFinalField("Youpi" + i);

			PepperReferenceHelper.internalize(left);
		}

		// We need to ensure a very-high cardinality Field leads to a removed dictionary
		Assert.assertNull(PepperReferenceHelper.DICTIONARY_FIELDS.get(NotFinalField.class.getDeclaredFields()[0]));
	}

	@Test
	public void testDictionarisationOnDerived() {
		DerivedClass left = new DerivedClass("Youpi");
		DerivedClass right = new DerivedClass(new String("Youpi"));

		// Not same ref
		Assert.assertNotSame(left.oneString, right.oneString);

		PepperReferenceHelper.internalizeFields(left);
		PepperReferenceHelper.internalizeFields(right);

		Assert.assertSame(left.oneString, right.oneString);
	}

	@Test
	public void testDictionarizeArray() {
		Object[] array = new Object[] { "Youpi", 123L, "_Youpi".substring(1) };

		Assert.assertNotSame(array[0], array[2]);

		PepperReferenceHelper.internalizeArray(array);

		Assert.assertSame(array[0], array[2]);
	}

	@Test
	public void testDictionarizeArray_high_cardinality() {
		for (int i = 0; i < 100000; i++) {
			Object[] array = new Object[] { "Youpi" + i };

			PepperReferenceHelper.internalizeArray(array);
		}

		// We need to ensure a very-high cardinality Class does not lead to a huge dictionary
		Assert.assertEquals(575, PepperReferenceHelper.DICTIONARY_ARRAY.get(String.class).size());
	}

	static class NotFinalField {
		public String oneString;

		public NotFinalField(String oneString) {
			this.oneString = oneString;
		}

	}

	static class FinalField {
		public String oneString;

		public FinalField(String oneString) {
			this.oneString = oneString;
		}

	}

	static class DerivedClass extends FinalField {

		public DerivedClass(String oneString) {
			super(oneString);
		}

	}

	@Test
	public void testShareReference_List() {
		List<String> beforeIntern =
				IntStream.range(0, 1000).map(i -> i % 3).mapToObj(i -> "String_" + i).collect(Collectors.toList());

		long sizeBefore = PepperMemoryHelper.deepSize(beforeIntern);

		pepperReferenceInternalizer.internalize(beforeIntern);

		long sizeAfter = PepperMemoryHelper.deepSize(beforeIntern);

		LOGGER.info("Size Before: {}, Size after: {}",
				PepperLogHelper.humanBytes(sizeBefore),
				PepperLogHelper.humanBytes(sizeAfter));
		Assert.assertTrue(sizeBefore > sizeAfter * 2.5D);
	}

	@Test
	public void testShareReference_URL() {
		URL firstUrl = PepperURLHelper.toHttpURL("https://youpi.com/go?arg#ici");
		URL secondUrl = PepperURLHelper.toHttpURL("https://youpi.com/go?arg#ici");

		List<URL> asList = Arrays.asList(firstUrl, secondUrl);
		long sizeBefore = PepperMemoryHelper.deepSize(asList);

		pepperReferenceInternalizer.internalize(asList);

		long sizeAfter = PepperMemoryHelper.deepSize(asList);

		LOGGER.info("Size Before: {}, Size after: {}",
				PepperLogHelper.humanBytes(sizeBefore),
				PepperLogHelper.humanBytes(sizeAfter));

		// Nearly divided by 2
		Assertions.assertThat(sizeAfter).isLessThanOrEqualTo((long) (sizeBefore / 1.8D));
	}
}
