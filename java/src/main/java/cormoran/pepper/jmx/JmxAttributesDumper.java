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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Connects remotely to a JMX server and dumps all the available information
 * 
 * @author Benoit Lacelle
 * 
 */
public class JmxAttributesDumper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(JmxAttributesDumper.class);

	public static final int HOST_INDEX = 0;
	public static final int PORT_INDEX = 1;
	public static final int USERNAME_INDEX = 2;
	public static final int PASSWORD_INDEX = 3;

	private static final String DEFAULT_USERNAME = "username";
	public static final int DEFAULT_JMX_PORT = 8050;

	public static final String QFS_REFRESH_OPERATION_NAME = "Refresh underlying MBeans";

	public static void main(String[] args) throws IOException {
		JmxAttributesDumper dumper = new JmxAttributesDumper();

		try (JMXConnector connector = dumper.openConnector(prepareConnection(Arrays.asList(args)))) {
			MBeanServerConnection connection = connector.getMBeanServerConnection();

			Map<ObjectName, Map<String, Object>> output;
			try {
				output = new JmxAttributesDumper().dump(connection, null, null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			for (Entry<ObjectName, Map<String, Object>> entry : output.entrySet()) {
				LOGGER.info("{} - {}", entry.getKey(), entry.getValue());
			}
		}
	}

	// http://stackoverflow.com/questions/5552960/how-to-connect-to-a-java-program-on-localhost-jvm-using-jmx
	public static PepperBasicConnectionDTO prepareConnection(List<? extends String> args) {
		if (args == null) {
			// DUMMY parameters
			return new PepperBasicConnectionDTO("HOST", DEFAULT_JMX_PORT, DEFAULT_USERNAME, DEFAULT_USERNAME);
		} else {
			String host;
			if (args.size() > HOST_INDEX) {
				host = args.get(HOST_INDEX);
			} else {
				host = "HOST";
			}
			int port;
			if (args.size() > PORT_INDEX) {
				port = Integer.parseInt(args.get(PORT_INDEX));
			} else {
				port = DEFAULT_JMX_PORT;
			}
			String userName;
			if (args.size() > USERNAME_INDEX) {
				userName = args.get(USERNAME_INDEX);
			} else {
				userName = DEFAULT_USERNAME;
			}
			String password;
			if (args.size() > PASSWORD_INDEX) {
				password = args.get(PASSWORD_INDEX);
			} else {
				// By default, we use the username as password
				password = userName;
			}

			return new PepperBasicConnectionDTO(host, port, userName, password);
		}
	}

	public JMXConnector openConnector(PepperBasicConnectionDTO connectionDetails) throws IOException {
		Map<String, String[]> env = new HashMap<String, String[]>();

		String userName = connectionDetails.userName;
		if (!Strings.isNullOrEmpty(userName)) {
			env.put(JMXConnector.CREDENTIALS, new String[] { userName, connectionDetails.password });
		}
		String url = buildJmxUrl(connectionDetails.host, connectionDetails.port);
		JMXServiceURL address = new JMXServiceURL(url);
		try {
			return JMXConnectorFactory.connect(address, env);
		} catch (RuntimeException | IOException e) {
			throw new RuntimeException("Issue while connecting as " + userName + " to " + url, e);
		}
	}

	public static String buildJmxUrl(String host, int port) {
		return "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
	}

	public Map<ObjectName, Map<String, Object>> dump(MBeanServerConnection mbs, ObjectName template, QueryExp query)
			throws IOException {
		Set<ObjectInstance> beans = getAllBeans(mbs, template, query);

		Map<ObjectName, Map<String, Object>> output = new HashMap<>();

		for (ObjectInstance instance : beans) {
			ObjectName name = instance.getObjectName();
			MBeanInfo info;
			try {
				info = mbs.getMBeanInfo(name);
			} catch (InstanceNotFoundException | IntrospectionException | ReflectionException | IOException e) {
				LOGGER.warn("Issue when calling MBeanInfo for " + name, e);
				continue;
			}

			List<String> attributes = new ArrayList<>();
			for (MBeanAttributeInfo attribute : info.getAttributes()) {
				attributes.add(attribute.getName());
			}

			List<Object> attributeValues;
			try {
				attributeValues = mbs.getAttributes(name, attributes.toArray(new String[0]));
			} catch (RuntimeException | InstanceNotFoundException | ReflectionException | IOException e) {
				attributeValues = Collections.emptyList();
			}

			Map<String, Object> attributeToValue = new HashMap<>();
			for (int i = 0; i < attributeValues.size(); i++) {
				attributeToValue.put(attributes.get(i), attributeValues.get(i));
			}

			output.put(name, attributeToValue);
		}

		return output;
	}

	protected Set<ObjectInstance> getAllBeans(MBeanServerConnection mbs, ObjectName template, QueryExp query)
			throws IOException {
		// Make sure we refresh underlying beans only once per bean
		Set<ObjectName> objectNameExecuted = new HashSet<>();

		boolean hasPotentiallyMoreUndelryingBeans = true;

		Set<ObjectInstance> beans = Collections.emptySet();
		while (hasPotentiallyMoreUndelryingBeans) {
			beans = mbs.queryMBeans(template, query);

			hasPotentiallyMoreUndelryingBeans = false;
			for (ObjectInstance instance : beans) {
				try {
					MBeanInfo info = mbs.getMBeanInfo(instance.getObjectName());

					if (objectNameExecuted.add(instance.getObjectName())) {
						for (MBeanOperationInfo operation : info.getOperations()) {
							if (QFS_REFRESH_OPERATION_NAME.equals(operation.getName())) {
								try {
									mbs.invoke(instance.getObjectName(), QFS_REFRESH_OPERATION_NAME, null, null);
									hasPotentiallyMoreUndelryingBeans = true;
								} catch (MBeanException e) {
									LOGGER.warn("Issue on " + instance, e);
								}
							}
						}
					}
				} catch (InstanceNotFoundException | IntrospectionException | ReflectionException e) {
					LOGGER.warn("Issue on " + instance, e);
				}
			}
		}

		return beans;
	}
}
