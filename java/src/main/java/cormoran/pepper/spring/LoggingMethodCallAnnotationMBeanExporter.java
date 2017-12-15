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

import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.RequiredModelMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.SpringModelMBean;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Extends {@link MBeanExporter} to add logging when a remote call failed. Else, the exception is forwarded to the
 * client, which may not render the exception correctly (e.g. the JConsole does not show the whole stacktrace)
 * 
 * @author Benoit Lacelle
 */
// http://docs.spring.io/autorepo/docs/spring/3.2.x/spring-framework-reference/html/jmx.html
@ManagedResource
public class LoggingMethodCallAnnotationMBeanExporter extends AnnotationMBeanExporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingMethodCallAnnotationMBeanExporter.class);

	protected final AtomicLong nbErrors = new AtomicLong();

	@ManagedAttribute
	public long getNbErrors() {
		return nbErrors.get();
	}

	// http://stackoverflow.com/questions/5767747/pmd-cpd-ignore-bits-of-code-using-comments
	@SuppressWarnings("CPD-START")
	@Override
	protected ModelMBean createModelMBean() throws MBeanException {
		ModelMBean superModelMBean = super.createModelMBean();

		if (superModelMBean instanceof SpringModelMBean) {
			return new SpringModelMBean() {
				@Override
				public Object invoke(String opName, Object[] opArgs, String[] sig)
						throws MBeanException, ReflectionException {
					try {
						return super.invoke(opName, opArgs, sig);
					} catch (MBeanException | ReflectionException | RuntimeException | Error e) {
						nbErrors.incrementAndGet();
						onErrorInRemoteCall(e);
						throw e;
					}
				}
			};
		} else {
			return new RequiredModelMBean() {
				@Override
				public Object invoke(String opName, Object[] opArgs, String[] sig)
						throws MBeanException, ReflectionException {
					try {
						return super.invoke(opName, opArgs, sig);
					} catch (MBeanException | ReflectionException | RuntimeException | Error e) {
						nbErrors.incrementAndGet();
						onErrorInRemoteCall(e);
						throw e;
					}
				}
			};
		}
	}

	protected void onErrorInRemoteCall(Throwable e) {
		LOGGER.warn("Issue on a remote call", e);
	}
}
