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
package cormoran.pepper.core.jmx;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TestSetStatic {
	public static String STRING_STATIC = "-";
	public static LocalDate LOCALDATE_STATIC = LocalDate.now();
	private static final double DOUBLE_STATIC = 0D;

	@Test
	public void testSetStatic() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		// Modify from current value
		String newValue = STRING_STATIC + "-";

		// Do the modification
		setSTatic.setStatic(TestSetStatic.class.getName(), "STRING_STATIC", newValue);

		Assert.assertEquals(newValue, STRING_STATIC);
	}

	@Test
	public void testSetStaticLocalDate() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		LocalDate initialDate = LOCALDATE_STATIC;

		// Modify from current value
		String newValue = initialDate.minusDays(1).toString();

		// Do the modification
		setSTatic.setStatic(TestSetStatic.class.getName(), "LOCALDATE_STATIC", newValue);

		Assert.assertEquals(initialDate.minusDays(1), LOCALDATE_STATIC);
	}

	@Test
	public void testGetStaticLocalDate() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		LocalDate initialDate = LOCALDATE_STATIC;

		String className = TestSetStatic.class.getName();
		Assert.assertEquals(initialDate, setSTatic.getStatic(className, "LOCALDATE_STATIC"));
		Assert.assertEquals(initialDate.toString(), setSTatic.getStaticAsString(className, "LOCALDATE_STATIC"));
	}

	@Ignore
	@Test
	public void testSetStaticPrivateFinalDouble()
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		double initialDouble = DOUBLE_STATIC;

		// Modify from current value
		String newValue = initialDouble + 1D + "";

		// Do the modification
		setSTatic.setStatic(TestSetStatic.class.getName(), "DOUBLE_STATIC", newValue);

		Assert.assertEquals(initialDouble + 1D, DOUBLE_STATIC, 0.0001D);
	}

	@Test
	public void testGetResourcesFor()
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IOException {
		SetStaticMBean setSTatic = new SetStaticMBean();

		List<String> pathes = setSTatic.getResourcesFor(this.getClass().getName());

		Assert.assertEquals(1, pathes.size());
		Assertions.assertThat(pathes.get(0)).startsWith("file:/").endsWith(".class");

		List<String> pathesWithSlahes =
				setSTatic.getResourcesFor(this.getClass().getName().toString().replace('.', '/'));
		Assert.assertEquals(1, pathesWithSlahes.size());
		Assertions.assertThat(pathesWithSlahes.get(0)).startsWith("file:/").endsWith(".class");

		List<String> pathesWithSlahesAndDotClassSuffix =
				setSTatic.getResourcesFor(this.getClass().getName().toString().replace('.', '/') + ".class");
		Assert.assertEquals(1, pathesWithSlahesAndDotClassSuffix.size());
		Assertions.assertThat(pathesWithSlahesAndDotClassSuffix.get(0)).startsWith("file:/").endsWith(".class");
	}
}
