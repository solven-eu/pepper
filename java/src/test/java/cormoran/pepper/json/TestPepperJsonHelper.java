package cormoran.pepper.json;

import org.junit.Assert;
import org.junit.Test;

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

		Assert.assertEquals("someA", output.getA());
		Assert.assertEquals("someB", output.getB());
	}

	@Test
	public void testMerge_partialOverrideFull() {
		SomeBean output = PepperJsonHelper.mergeNonNull(new SomeBean("someA", "someB"),
				new SomeBean(null, "otherB"),
				objectMapper,
				SomeBean.class);

		Assert.assertEquals("someA", output.getA());
		Assert.assertEquals("otherB", output.getB());
	}

	@Test
	public void testMerge_fullOverridePartial() {
		SomeBean output = PepperJsonHelper.mergeNonNull(new SomeBean(null, "someB"),
				new SomeBean("someA", "otherB"),
				objectMapper,
				SomeBean.class);

		Assert.assertEquals("someA", output.getA());
		Assert.assertEquals("otherB", output.getB());
	}
}