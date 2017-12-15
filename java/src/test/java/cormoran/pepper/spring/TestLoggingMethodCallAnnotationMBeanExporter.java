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
package cormoran.pepper.spring;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.modelmbean.ModelMBean;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLoggingMethodCallAnnotationMBeanExporter {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TestLoggingMethodCallAnnotationMBeanExporter.class);

	@Test
	public void doLog() throws MBeanException, ReflectionException {
		LoggingMethodCallAnnotationMBeanExporter exporter = new LoggingMethodCallAnnotationMBeanExporter();

		ModelMBean mbean = exporter.createModelMBean();

		try {
			mbean.invoke("actionName", new Object[0], new String[0]);
		} catch (MBeanException e) {
			LOGGER.trace("Expected", e);
		}

		Assert.assertEquals(1, exporter.getNbErrors());
	}

	@Test
	public void doLog_classLoaderNotExposed() throws MBeanException, ReflectionException {
		LoggingMethodCallAnnotationMBeanExporter exporter = new LoggingMethodCallAnnotationMBeanExporter();

		// We instanciate a different kind of bean in this boolean is false
		exporter.setExposeManagedResourceClassLoader(false);

		ModelMBean mbean = exporter.createModelMBean();

		try {
			mbean.invoke("actionName", new Object[0], new String[0]);
		} catch (MBeanException e) {
			LOGGER.trace("Expected", e);
		}

		Assert.assertEquals(1, exporter.getNbErrors());
	}
}
