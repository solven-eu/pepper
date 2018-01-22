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
package cormoran.pepper.io;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

import cormoran.pepper.jmx.SetStaticMBean;

/**
 * Various utility method related to Serialization, as conversion from/to String to/from Collections and Map
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperSerializationHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperSerializationHelper.class);

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

	public static final char FORCE_SEPARATOR = '#';

	private static final int HEX_FILTER = 0xFF;

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

	private static final Function<Object, CharSequence> OBJECT_TO_QUOTED_STRING = input -> {
		if (input == null) {
			// An empty String is a nice reflect of null
			return "";
		} else {
			String asString = input.toString();

			if (input instanceof CharSequence && ((CharSequence) input).length() >= 2
					&& ((CharSequence) input).charAt(0) == '"'
					&& ((CharSequence) input).charAt(((CharSequence) input).length() - 1) == '"') {
				// Already quoted
				return asString;
			} else {
				String replaceDoubleQuotes = asString.replace("\"", "\"\"");

				// Wrap between quotes
				return "\"" + replaceDoubleQuotes + "\"";
			}
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
		return ImmutableMap.copyOf(Maps.transformValues(mapStringString, input -> convertStringToObject(input)));
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
		return Maps.transformValues(mapStringString, value -> convertToListString(value));
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
		return ImmutableList.copyOf(Lists.transform(stringList, (input) -> {
			return convertStringToObject(input);
		}));
	}

	public static List<String> convertToListString(CharSequence asString, char separator) {
		return Splitter.on(separator).trimResults().splitToList(asString);
	}

	public static String convertToString(Map<?, ?> asMap) {
		return Joiner.on(MAP_ENTRY_SEPARATOR).withKeyValueSeparator(Character.toString(MAP_KEY_VALUE_SEPARATOR)).join(
				Maps.transformValues(asMap, input -> {
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
					return asObject.map(o -> (Object) o).orElse(string);
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

	@Beta
	// TODO
	public static Object toDoubleLowDigits(Object value) {
		// if (value instanceof Float || value instanceof Double) {
		// //
		// http://stackoverflow.com/questions/703396/how-to-nicely-format-floating-numbers-to-string-without-unnecessary-decimal-0
		//
		//
		// double asDouble = ((Number) value).doubleValue();
		//
		// if (asDouble >= 1) {
		// // Get ride of decimals
		// return (double) ((long) asDouble);
		// } else {
		// String asString = String.format("%f", asDouble);
		//
		// int indexOfDot = asString.indexOf('.');
		// if (indexOfDot == -1) {
		// return asDouble;
		// } else {
		// int notZeroOrDot = 0;
		// for (int i = 0 ; i < )
		//
		// if (asString.length() > indexOfDot + 4)
		// }
		// if (asString.)
		//
		// String subString = asString.substring(0, + 4);
		//
		// return Double.parseDouble(Double.toString(asDouble));
		// }
		// } else {
		// return super.cleanValue(value);
		// }
		// TODO Auto-generated method stub
		return value;
	}

	/**
	 * Easy way to append a single CSV row in a file
	 * 
	 * @param file
	 * @param row
	 * @throws IOException
	 */
	// synchronized to prevent interlaced rows
	// TODO: one lock per actual file
	@Beta
	public static synchronized void appendLineInCSVFile(Path file, Iterable<?> row) throws IOException {
		appendLineInCSVFile(new FileWriter(file.toFile(), true), row);
	}

	@Beta
	public static void appendLineInCSVFile(Writer writer, Iterable<?> row) throws IOException {
		// Ensure the writer is buffered
		try (BufferedWriter bufferedWriter = new BufferedWriter(writer) {
			@Override
			public void close() throws IOException {
				// Skip closing as we received a Writer from somewhere else
				super.flush();
			};
		}) {
			// By default, we wrap in quotes
			rawAppendLineInCSVFile(bufferedWriter, row, true, MAX_CHARS_PER_COLUMN);
			// Prepare the next line
			bufferedWriter.newLine();
		}
	}

	@Beta
	public static void rawAppendLineInCSVFile(Writer writer,
			Iterable<?> row,
			final boolean wrapInQuotes,
			final int maxLength) throws IOException {
		// Get ride of null references
		Iterable<CharSequence> asString = Iterables.transform(row, OBJECT_TO_QUOTED_STRING);

		asString = Iterables.transform(asString, input -> {

			if (input == null || maxLength < 0) {
				// No transformation
				return input;
			} else if (!wrapInQuotes && input.length() > maxLength) {
				// simple SubSequence
				return input.subSequence(0, maxLength);
			} else {
				// We do '-2' to prevent an overflow if maxLength == Integer.MAX_VALUE
				if (wrapInQuotes && input.length() - 2 > maxLength) {
					// SubSequence between quotes
					return "\"" + input.subSequence(1, maxLength + 1) + '\"';
				} else {
					return input;
				}
			}

		});

		// Append the row
		Joiner.on(';').appendTo(writer, asString);
	}

	@Beta
	public static void appendLineInCSVFile(FileOutputStream outputFileIS, Iterable<?> row) throws IOException {
		// Use a filelock to prevent several process having their rows being interlaced
		java.nio.channels.FileLock lock = outputFileIS.getChannel().lock();
		try {
			appendLineInCSVFile(new OutputStreamWriter(outputFileIS, Charsets.UTF_8), row);
		} finally {
			lock.release();
		}
	}

	public static List<String> parseList(String asString) {
		return Splitter.on(',').trimResults().splitToList(
				asString.substring(asString.indexOf('[') + 1, asString.lastIndexOf(']')));
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
	public static String toString(InputStream inputStream, Charset charset) throws IOException {
		// We picked the faster implementation proposed in
		// https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(o);
		}
		return baos.toByteArray();
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
		for (int i = 0; i < digest.length; i++) {
			String hex = Integer.toHexString(HEX_FILTER & digest[i]);
			if (hex.length() == 1) {
				// could use a for loop, but we're only dealing with a
				// single byte
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

}
