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
package eu.solven.pepper.unittest;

import org.assertj.core.api.Assertions;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

/**
 * Helpers for Jackson (de)serialization Unit-tests
 *
 * @author Benoit Lacelle
 *
 */
@Slf4j
@UtilityClass
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class PepperJackson3TestHelper {

	public static ObjectMapper makeObjectMapper() {
		return new ObjectMapper().rebuild()

				// https://stackoverflow.com/questions/17617370/pretty-printing-json-from-jackson-2-2s-objectmapper
				.enable(SerializationFeature.INDENT_OUTPUT)
				.build();
	}

	public static <T> String verifyJackson(T object) {
		ObjectMapper objectMapper = makeObjectMapper();

		Class<? extends T> clazz = (Class<? extends T>) object.getClass();
		return verifyJackson(objectMapper, clazz, object);
	}

	public static <T> String verifyJackson(Class<? extends T> clazz, T object) {
		ObjectMapper objectMapper = makeObjectMapper();

		return verifyJackson(objectMapper, clazz, object);
	}

	public static <T> String verifyJackson(ObjectMapper om, Class<? extends T> clazz, T object) {
		String asString = om.writeValueAsString(object);
		Object fromString = om.readValue(asString, clazz);

		Assertions.assertThat(fromString).as(() -> "notEquals given " + asString).isEqualTo(object);

		return asString;
	}

	public static <T> String asString(T object) {
		return asString(object.getClass(), object);
	}

	public static <T> String asString(Class<? extends T> clazz, T object) {
		return asString(makeObjectMapper(), clazz, object);
	}

	/**
	 * To be used by classes which are not deserializable.
	 */
	public static <T> String asString(ObjectMapper om, Class<? extends T> clazz, T object) {
		return om.writeValueAsString(object);
	}
}
