/**
 * The MIT License
 * Copyright (c) 2025 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.unittest;

import java.time.LocalDate;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.fasterxml.jackson.core.JsonProcessingException;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.InvalidDefinitionException;
import tools.jackson.databind.exc.MismatchedInputException;

public class TestPepperJackson3TestHelper {
	@Disabled("JavaTimeModule is included by default in Jackson3")
	@Test
	public void testVerifyJackson() throws JsonProcessingException {
		LocalDate now = LocalDate.now();

		// Check with native objectMapper
		Assertions.assertThatThrownBy(() -> {
			PepperJackson3TestHelper.verifyJackson(now);
		})
				.hasRootCauseExactlyInstanceOf(InvalidDefinitionException.class)
				.hasStackTraceContaining("Java 8 date/time type");

		// Check with custom module
		ObjectMapper objectMapper = new ObjectMapper().rebuild()
				// .addModule(new JavaTimeModule())
				.build();
		String asString = PepperJackson3TestHelper.verifyJackson(objectMapper, LocalDate.class, now);

		Assertions.assertThat(asString)
				.isEqualTo("[%s,%s,%s]".formatted(now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
	}

	@Test
	public void testVerifyIndentation() throws JsonProcessingException {
		String asString = PepperJackson3TestHelper.verifyJackson(Map.class, Map.of("k", "v"));

		Assertions.assertThat(asString).isEqualTo("""
				{
				  "k" : "v"
				}""");
	}

	@Test
	public void testAbstractClass() throws JsonProcessingException {
		String asString = PepperJackson3TestHelper.verifyJackson(Number.class, Double.valueOf(12.34));

		Assertions.assertThat(asString).isEqualTo("12.34");
	}

	@Test
	public void testBrokenClass_missingConstructor() throws JsonProcessingException {
		Assertions
				.assertThatThrownBy(
						() -> PepperJackson3TestHelper.verifyJackson(new InvalidJackson_noEmptyCtor("someString")))
				.isInstanceOf(MismatchedInputException.class)
				.hasStackTraceContaining("Cannot construct instance of");
	}

	@Test
	public void testBrokenClass_unequals() throws JsonProcessingException {
		Assertions
				.assertThatThrownBy(
						() -> PepperJackson3TestHelper.verifyJackson(new InvalidJackson_weirdSetter("someString")))
				.isInstanceOf(AssertionFailedError.class)
				.hasNoCause()
				.hasStackTraceContaining("notEquals");
	}
}
