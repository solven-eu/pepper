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
package eu.solven.pepper.toto;

import static com.google.common.base.Strings.lenientFormat;
import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import ch.qos.logback.classic.joran.action.LoggerAction;
import junit.extensions.TestSetup;

public class TestVirtualMachineWithoutToolsJar {
	@Test
	public void testFindVirtualMachineClass() throws ClassNotFoundException, MalformedURLException {
		Charsets.US_ASCII.toString();
		Strings.isNullOrEmpty("");
		lenientFormat("");

		assertEquals("titi", "toto");

		new TestSetup(null);
		new LoggerAction();
	}
}
