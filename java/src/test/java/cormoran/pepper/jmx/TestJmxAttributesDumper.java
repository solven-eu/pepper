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
package cormoran.pepper.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.jqno.equalsverifier.EqualsVerifier;

public class TestJmxAttributesDumper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TestJmxAttributesDumper.class);

	@Test
	public void dumpJmx() throws InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException,
			IOException, MalformedObjectNameException {
		JmxAttributesDumper dumper = new JmxAttributesDumper();

		Map<ObjectName, Map<String, Object>> output =
				dumper.dump(ManagementFactory.getPlatformMBeanServer(), null, null);

		Assert.assertFalse(output.isEmpty());

		// Check some key supposed to be present on any JVM
		Map<String, Object> threadMBean = output.get(new ObjectName("java.lang:type=Threading"));
		Assert.assertNotNull(threadMBean);

		Assert.assertNotNull("", threadMBean.get("ThreadCount"));
	}

	@Test
	public void testMain() throws IOException {
		try {
			JmxAttributesDumper.main(new String[0]);
		} catch (Exception e) {
			LOGGER.trace("Exception exception as no host", e);
		}
	}

	@Test
	public void testPrepareConnectionDetails() {
		PepperBasicConnectionDTO details =
				JmxAttributesDumper.prepareConnection(Arrays.asList("host", "123", "user", "pw"));

		Assert.assertEquals("host", details.host);
		Assert.assertEquals(123, details.port);
		Assert.assertEquals("user", details.userName);
		Assert.assertEquals("pw", details.password);
	}

	@Test
	public void testApexBasicConnectionDTOEqualsContract() {
		EqualsVerifier.forClass(PepperBasicConnectionDTO.class).verify();
	}

}
