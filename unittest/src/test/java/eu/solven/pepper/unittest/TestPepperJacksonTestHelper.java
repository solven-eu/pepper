package eu.solven.pepper.unittest;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TestPepperJacksonTestHelper {
	@Test
	public void testVerifyJackson() throws JsonProcessingException {
		LocalDate now = LocalDate.now();

		// Check with native objectMapper
		Assertions.assertThatThrownBy(() -> {
			PepperJacksonTestHelper.verifyJackson(now);
		})
				.hasRootCauseExactlyInstanceOf(InvalidDefinitionException.class)
				.hasStackTraceContaining("Java 8 date/time type");

		// Check with custom module
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		String asString = PepperJacksonTestHelper.verifyJackson(objectMapper, LocalDate.class, now);

		Assertions.assertThat(asString)
				.isEqualTo("[%s,%s,%s]".formatted(now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
	}

	@Test
	public void testVerifyIndentation() throws JsonProcessingException {
		String asString = PepperJacksonTestHelper.verifyJackson(Map.class, Map.of("k", "v"));

		Assertions.assertThat(asString).isEqualTo("""
				{
				  "k" : "v"
				}""");
	}

	@Test
	public void testAbstractClass() throws JsonProcessingException {
		String asString = PepperJacksonTestHelper.verifyJackson(Number.class, Double.valueOf(12.34));

		Assertions.assertThat(asString).isEqualTo("12.34");
	}

	@Test
	public void testBrokenClass_missingConstructor() throws JsonProcessingException {
		Assertions
				.assertThatThrownBy(
						() -> PepperJacksonTestHelper.verifyJackson(new InvalidJackson_noEmptyCtor("someString")))
				.isInstanceOf(UncheckedIOException.class)
				.hasRootCauseInstanceOf(MismatchedInputException.class)
				.hasStackTraceContaining("Cannot construct instance of");
	}

	@Test
	public void testBrokenClass_unequals() throws JsonProcessingException {
		Assertions
				.assertThatThrownBy(
						() -> PepperJacksonTestHelper.verifyJackson(new InvalidJackson_weirdSetter("someString")))
				.isInstanceOf(AssertionFailedError.class)
				.hasNoCause()
				.hasStackTraceContaining("notEquals");
	}
}
