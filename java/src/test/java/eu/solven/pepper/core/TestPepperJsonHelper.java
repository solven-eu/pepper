/**
 * The MIT License
 * Copyright (c) 2023 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestPepperJsonHelper {

	final ObjectMapper objectMapper = new ObjectMapper();

	public static class SomeBean {
		private final String a;
		private final String b;

		@JsonCreator
		public SomeBean(@JsonProperty("a") String a, @JsonProperty("b") String b) {
			this.a = a;
			this.b = b;
		}

		public String getA() {
			return a;
		}

		public String getB() {
			return b;
		}

	}

	@Test
	public void testMerge_mergeNulls() {
		SomeBean output = PepperJsonHelper
				.mergeNonNull(new SomeBean("someA", null), new SomeBean(null, "someB"), objectMapper, SomeBean.class);

		Assertions.assertEquals("someA", output.getA());
		Assertions.assertEquals("someB", output.getB());
	}

	@Test
	public void testMerge_partialOverrideFull() {
		SomeBean output = PepperJsonHelper.mergeNonNull(new SomeBean("someA", "someB"),
				new SomeBean(null, "otherB"),
				objectMapper,
				SomeBean.class);

		Assertions.assertEquals("someA", output.getA());
		Assertions.assertEquals("otherB", output.getB());
	}

	@Test
	public void testMerge_fullOverridePartial() {
		SomeBean output = PepperJsonHelper.mergeNonNull(new SomeBean(null, "someB"),
				new SomeBean("someA", "otherB"),
				objectMapper,
				SomeBean.class);

		Assertions.assertEquals("someA", output.getA());
		Assertions.assertEquals("otherB", output.getB());
	}
}