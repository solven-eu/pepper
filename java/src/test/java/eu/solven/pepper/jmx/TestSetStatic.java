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
package eu.solven.pepper.jmx;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestSetStatic {
	public static String STRING_STATIC = "-";
	public static LocalDate LOCALDATE_STATIC = LocalDate.now();
	private static final double DOUBLE_STATIC_FINAL = 0D;
	private static TimeUnit ENUM_STATIC_FINAL = TimeUnit.MINUTES;
	private static ClassWithCharSequenceContructor CTOR_STATIC_FINAL = new ClassWithCharSequenceContructor("0");

	public static class ClassWithCharSequenceContructor {
		protected final String string;

		public ClassWithCharSequenceContructor(String string) {
			this.string = string;
		}

		@Override
		public String toString() {
			return string;
		}
	}

	@Test
	public void testSetStatic() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		// Modify from current value
		String newValue = STRING_STATIC + "-";

		// Do the modification
		setSTatic.setStatic(TestSetStatic.class.getName(), "STRING_STATIC", newValue);

		Assertions.assertEquals(newValue, STRING_STATIC);
	}

	@Test
	public void testSetStatic_ParseMethod()
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		LocalDate initialDate = LOCALDATE_STATIC;

		// Modify from current value
		String newValue = initialDate.minusDays(1).toString();

		// Do the modification
		setSTatic.setStatic(TestSetStatic.class.getName(), "LOCALDATE_STATIC", newValue);

		Assertions.assertEquals(initialDate.minusDays(1), LOCALDATE_STATIC);
	}

	@Test
	public void testGetStaticLocalDate() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		LocalDate initialDate = LOCALDATE_STATIC;

		String className = TestSetStatic.class.getName();
		Assertions.assertEquals(initialDate, setSTatic.getStatic(className, "LOCALDATE_STATIC"));
		Assertions.assertEquals(initialDate.toString(), setSTatic.getStaticAsString(className, "LOCALDATE_STATIC"));
	}

	@Test
	public void testSetStatic_ConstructorOverString()
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		ClassWithCharSequenceContructor initial = CTOR_STATIC_FINAL;

		// Do the modification
		setSTatic.setStatic(TestSetStatic.class.getName(), "CTOR_STATIC_FINAL", "123");

		// Check the test is not trivial
		Assertions.assertNotSame(initial, CTOR_STATIC_FINAL);
		Assertions.assertNotEquals(initial.toString(), CTOR_STATIC_FINAL.toString());

		// Check the field have been modified
		Assertions.assertEquals("123", CTOR_STATIC_FINAL.toString());
	}

	// TODO
	@Disabled("TODO")
	@Test
	public void testSetStaticEnum() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		TimeUnit initial = ENUM_STATIC_FINAL;

		// Do the modification
		setSTatic.setStatic(TestSetStatic.class.getName(), "ENUM_STATIC_FINAL", TimeUnit.MINUTES.toString());

		// Check the test is not trivial
		Assertions.assertNotSame(initial, ENUM_STATIC_FINAL);
		// Check the field have been modified
		Assertions.assertEquals(TimeUnit.MINUTES, ENUM_STATIC_FINAL);
	}

	@Disabled
	@Test
	public void testSetStaticPrivateFinalDouble()
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		double initialDouble = DOUBLE_STATIC_FINAL;

		// Modify from current value
		String newValue = initialDouble + 1D + "";

		// Do the modification
		setSTatic.setStatic(TestSetStatic.class.getName(), "DOUBLE_STATIC", newValue);

		Assertions.assertEquals(initialDouble + 1D, DOUBLE_STATIC_FINAL, 0.0001D);
	}

	@Test
	public void testGetResourcesFor()
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IOException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		List<String> pathes = setSTatic.getResourcesFor(this.getClass().getName());

		Assertions.assertEquals(1, pathes.size());
		org.assertj.core.api.Assertions.assertThat(pathes.get(0)).startsWith("file:/").endsWith(".class");

		List<String> pathesWithSlahes = setSTatic.getResourcesFor(this.getClass().getName().replace('.', '/'));
		Assertions.assertEquals(1, pathesWithSlahes.size());
		org.assertj.core.api.Assertions.assertThat(pathesWithSlahes.get(0)).startsWith("file:/").endsWith(".class");

		List<String> pathesWithSlahesAndDotClassSuffix =
				setSTatic.getResourcesFor(this.getClass().getName().replace('.', '/') + ".class");
		Assertions.assertEquals(1, pathesWithSlahesAndDotClassSuffix.size());
		org.assertj.core.api.Assertions.assertThat(pathesWithSlahesAndDotClassSuffix.get(0)).startsWith("file:/").endsWith(".class");
	}
}
