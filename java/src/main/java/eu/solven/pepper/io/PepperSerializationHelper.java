/**
 * The MIT License
 * Copyright (c) 2014-2024 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SerializationUtils;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

import eu.solven.pepper.core.PepperLogHelper;
import eu.solven.pepper.jmx.SetStaticMBean;

/**
 * Various utility method related to Serialization, as conversion from/to String to/from Collections and Map
 *
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings("PMD.GodClass")
public class PepperSerializationHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperSerializationHelper.class);

	private static final int KB = 1024;

	// Excel accepts only 32,767 chars per cell: we accept up to 4 MDX in a row
	// https://support.office.com/en-us/article/Excel-specifications-and-limits-16c69c74-3d6a-4aaf-ba35-e6eb276e8eaa
	public static final int MAX_CHARS_PER_COLUMN = 8192;

	public static final char MAP_KEY_VALUE_SEPARATOR = '=';

	// This should be the same as IPostProcessor.SEPARATOR
	public static final char MAP_ENTRY_SEPARATOR = ',';

	// Human often use ';' as entry separator
	public static final char MAP_ENTRY_SEPARATOR_SEMICOLUMN = ';';

	// Handle 'a=b,c=d'
	public static final MapSplitter MAP_TO_STRING_SPLITTER =
			Splitter.on(MAP_ENTRY_SEPARATOR).trimResults().withKeyValueSeparator(MAP_KEY_VALUE_SEPARATOR);

	// Handle 'a=b;c=d'
	public static final MapSplitter MAP_TO_STRING_SPLITTER_SEMICOLUMN =
			Splitter.on(MAP_ENTRY_SEPARATOR_SEMICOLUMN).trimResults().withKeyValueSeparator(MAP_KEY_VALUE_SEPARATOR);

	public static final char PIPE = '|';

	// TODO
	// This should be the same as IPostProcessor.SEPARATOR
	public static final char COLLECTION_SEPARATOR = PIPE;

	@Deprecated
	public static final char FORCE_SEPARATOR = '#';

	private static final int HEX_FILTER = 0xFF;
	private static final int HEX_SHIFT = 0x100;
	private static final int RADIX_16 = 16;

	// TODO: Not useful at all?
	@Deprecated
	protected static final Function<Object, CharSequence> OBJECT_TO_STRING = input -> {

		if (input == null) {
			// An empty String is a nice reflect of null
			return "";
		} else {
			return input.toString();
		}
	};

	protected PepperSerializationHelper() {
		// hidden
	}

	/**
	 *
	 * @param asString
	 *            a String with the form key1=value1,key2=value2
	 * @return a {@link Map}
	 */
	public static Map<String, Object> convertToMap(CharSequence asString) {
		Map<String, String> mapStringString = convertToMapStringString(asString);
		return ImmutableMap
				.copyOf(Maps.transformValues(mapStringString, PepperSerializationHelper::convertStringToObject));
	}

	public static Map<String, String> convertToMapStringString(CharSequence asString) {
		if (asString == null || asString.length() == 0) {
			return Collections.emptyMap();
		} else {
			Map<String, String> notFullyTrimmed;
			try {
				notFullyTrimmed = MAP_TO_STRING_SPLITTER.split(asString);
			} catch (IllegalArgumentException e) {
				// Try to parse as "a=b;c=d"
				LOGGER.trace("Can not parse " + asString + " with " + MAP_TO_STRING_SPLITTER);
				notFullyTrimmed = MAP_TO_STRING_SPLITTER_SEMICOLUMN.split(asString);
			}

			// Linked to maintain the order of the String
			Map<String, String> fullyTrimmed = new LinkedHashMap<>();

			// .trimResults does not work from Maps
			for (Entry<String, String> notTrimmed : notFullyTrimmed.entrySet()) {
				String valueAsObject = notTrimmed.getValue().trim();
				fullyTrimmed.put(notTrimmed.getKey().trim(), valueAsObject);
			}

			return fullyTrimmed;
		}
	}

	public static Map<String, List<String>> convertToMapStringListString(String asString) {
		// Separate keys from values
		Map<String, String> mapStringString = convertToMapStringString(asString);

		// Convert value from String to List of String
		return Maps.transformValues(mapStringString, PepperSerializationHelper::convertToListString);
	}

	public static Set<?> convertToSet(CharSequence asString) {
		// Linked to maintain order, typically to match secondary indexes
		return new LinkedHashSet<>(convertToList(asString));
	}

	public static Set<? extends String> convertToSetString(CharSequence asString) {
		// Linked to maintain order, typically to match secondary indexes
		return new LinkedHashSet<>(convertToListString(asString));
	}

	public static List<Object> convertToList(CharSequence asString) {
		if (CharMatcher.is(COLLECTION_SEPARATOR).indexIn(asString) >= 0) {
			return convertToList(asString, COLLECTION_SEPARATOR);
		} else {
			return convertToList(asString, MAP_ENTRY_SEPARATOR);
		}
	}

	public static List<String> convertToListString(CharSequence asString) {
		if (CharMatcher.is(COLLECTION_SEPARATOR).indexIn(asString) >= 0) {
			return convertToListString(asString, COLLECTION_SEPARATOR);
		} else {
			return convertToListString(asString, MAP_ENTRY_SEPARATOR);
		}
	}

	public static List<Object> convertToList(CharSequence asString, char separator) {
		List<String> stringList = convertToListString(asString, separator);
		return ImmutableList.copyOf(Lists.transform(stringList, PepperSerializationHelper::convertStringToObject));
	}

	public static List<String> convertToListString(CharSequence asString, char separator) {
		return Splitter.on(separator).trimResults().splitToList(asString);
	}

	public static String convertToString(Map<?, ?> asMap) {
		return Joiner.on(MAP_ENTRY_SEPARATOR)
				.withKeyValueSeparator(Character.toString(MAP_KEY_VALUE_SEPARATOR))
				.join(Maps.transformValues(asMap, input -> {
					if (input == null) {
						return "";
					} else if (input instanceof Iterable<?>) {
						// convertToString would use MAP_ENTRY_SEPARATOR
						// which is already used by Map
						return convertToString2((Iterable<?>) input);
					} else if (input instanceof CharSequence) {
						return input.toString();
					} else {
						return convertObjectToString(input);
					}
				}));
	}

	public static String convertToString(Iterable<?> asList) {
		return Joiner.on(MAP_ENTRY_SEPARATOR).join(asList);
	}

	public static String convertToString2(Iterable<?> asList) {
		return Joiner.on(COLLECTION_SEPARATOR).join(asList);
	}

	/**
	 *
	 * @param object
	 * @return the String, or the className followed by the object .toString
	 * @deprecated As it seems not useful to call the .toString. You may rely on
	 *             {@link PepperLogHelper#getObjectAndClass(Object)}
	 */
	@Deprecated
	public static String convertObjectToString(Object object) {
		if (object == null) {
			return "";
		} else if (object instanceof CharSequence) {
			return object.toString();
		} else {
			return object.getClass().getName() + FORCE_SEPARATOR + object;
		}
	}

	/**
	 *
	 * @param charSequence
	 * @return an Object is transcoding succeeded, else the original String
	 */
	@Deprecated
	public static Object convertStringToObject(CharSequence charSequence) {
		if (charSequence == null || charSequence.length() == 0) {
			return "";
		} else {
			String string = charSequence.toString();
			final int indexofForceSep = string.indexOf(FORCE_SEPARATOR);
			if (indexofForceSep >= 0) {
				String className = string.substring(0, indexofForceSep);
				try {
					Class<?> clazz = Class.forName(className);
					String subString = string.substring(indexofForceSep + 1);

					Optional<?> asObject = safeToObject(clazz, subString);

					// Fallback on String
					return asObject.map(Object.class::cast).orElse(string);
				} catch (ClassNotFoundException e) {
					LOGGER.trace("No class for {}", className);

					// Return as String
					return string;
				}
			} else {
				return string;
			}
		}
	}

	/**
	 *
	 * @param clazz
	 *            this the {@link Class} of the object instantiated by this method
	 * @param asString
	 *            some string describing the object to instantiate
	 * @return an object of given class build over the input String
	 */
	@Beta
	public static <T> Optional<T> safeToObject(Class<? extends T> clazz, String asString) {
		T asObject = SetStaticMBean.safeTrySingleArgConstructor(clazz, asString);
		if (asObject != null) {
			// Success
			return Optional.of(asObject);
		}

		asObject = SetStaticMBean.safeTryParseArgument(clazz, asString);
		if (asObject != null) {
			// Success
			return Optional.of(asObject);
		}

		return Optional.empty();
	}

	public static List<String> parseList(String asString) {
		return Splitter.on(',')
				.trimResults()
				.splitToList(asString.substring(asString.indexOf('[') + 1, asString.lastIndexOf(']')));
	}

	/**
	 * Read the object from Base64 string.
	 */
	// http://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
	public static <T extends Serializable> T fromString(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);

		return fromBytes(data);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T fromBytes(byte[] data) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
			return (T) ois.readObject();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T fromBytes(ByteBuffer data) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data.array()))) {
			return (T) ois.readObject();
		}
	}

	/**
	 *
	 * @param inputStream
	 *            the inputStream which which bytes have to be read. It will NOT be closed
	 * @param charset
	 * @return the String associated to given bytes
	 * @throws IOException
	 */
	@SuppressWarnings("PMD.AssignmentInOperand")
	public static String toString(InputStream inputStream, Charset charset) throws IOException {
		// We picked the faster implementation proposed in
		// https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[KB];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString(charset.name());
	}

	/**
	 * Write the object to a Base64 string.
	 */
	// http://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
	public static String toString(Serializable o) throws IOException {
		return Base64.getEncoder().encodeToString(toBytes(o));
	}

	public static byte[] toBytes(Serializable o) throws IOException {
		return SerializationUtils.serialize(o);
	}

	public static String toMD5(String input) {
		final MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		return toMD5(input, Charset.defaultCharset(), md);
	}

	public static String toMD5(String input, Charset charset, MessageDigest md) {
		try (InputStream baos = new DigestInputStream(new ByteArrayInputStream(input.getBytes(charset)), md)) {
			ByteStreams.exhaust(baos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		byte[] digest = md.digest();

		StringBuffer hexString = new StringBuffer();
		for (byte someByte : digest) {
			String hex = Integer.toHexString(HEX_FILTER & someByte);
			if (hex.length() == 1) {
				// could use a for loop, but we're only dealing with a
				// single byte
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static String toSha512(String input, String salt) {
		final MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		return toSha512(input, salt, StandardCharsets.UTF_8, md);
	}

	// https://stackoverflow.com/questions/33085493/hash-a-password-with-sha-512-in-java
	public static String toSha512(String input, String salt, Charset charset, MessageDigest md) {
		md.update(salt.getBytes(charset));

		byte[] bytes = md.digest(input.getBytes(charset));
		StringBuilder sb = new StringBuilder();
		for (byte someByte : bytes) {
			int asInt = (someByte & HEX_FILTER) + HEX_SHIFT;
			sb.append(Integer.toString(asInt, RADIX_16).substring(1));
		}
		return sb.toString();
	}

	public static String generateSha512() {
		return toSha512(UUID.randomUUID().toString(), UUID.randomUUID().toString());
	}

}
