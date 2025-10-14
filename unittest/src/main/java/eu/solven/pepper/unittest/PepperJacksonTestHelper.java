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

import java.io.UncheckedIOException;

import org.assertj.core.api.Assertions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Helpers for Jackson (de)serialization Unit-tests
 *
 * @author Benoit Lacelle
 *
 */
@Slf4j
@UtilityClass
public class PepperJacksonTestHelper {

	public static ObjectMapper makeObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		// https://stackoverflow.com/questions/17617370/pretty-printing-json-from-jackson-2-2s-objectmapper
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		return objectMapper;
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
		String asString;
		Object fromString;
		try {
			asString = om.writeValueAsString(object);
			fromString = om.readValue(asString, clazz);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}

		Assertions.assertThat(fromString).as(() -> "notEquals given " + asString).isEqualTo(object);

		return asString;
	}

	public static <T> String asString(Class<? extends T> clazz, T object) {
		return asString(makeObjectMapper(), clazz, object);
	}

	/**
	 * To be used by classes which are not deserializable.
	 */
	public static <T> String asString(ObjectMapper om, Class<? extends T> clazz, T object) {
		try {
			return om.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}
}
