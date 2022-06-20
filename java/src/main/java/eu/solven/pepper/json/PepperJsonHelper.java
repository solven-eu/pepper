package eu.solven.pepper.json;

import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Helps working with JSON
 *
 * @author Benoit Lacelle
 *
 */
public class PepperJsonHelper {
	private static final ObjectMapper HUMAN_OBJECT_MAPPER = new ObjectMapper();

	static {
		// https://stackoverflow.com/questions/17617370/pretty-printing-json-from-jackson-2-2s-objectmapper
		HUMAN_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
	}

	protected PepperJsonHelper() {
		// hidden
	}

	/**
	 * Merge fields of foreign object into base object if not null
	 *
	 * @param baseObject
	 *            base object to merge fields to
	 * @param foreignObject
	 *            foreign object to merge fields from
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <O> O mergeNonNull(Object baseObject,
			Object foreignObject,
			ObjectMapper objectMapper,
			Class<O> outputClass) {
		Map<String, ?> mapBaseFields = objectMapper.convertValue(baseObject, Map.class);
		Map<String, ?> mapForeignFields = objectMapper.convertValue(foreignObject, Map.class);
		Map<String, Object> mapMergedFields = recursiveMergeMapNonNull(mapBaseFields, mapForeignFields);
		O mergedObject = objectMapper.convertValue(mapMergedFields, outputClass);
		return mergedObject;
	}

	private static Map<String, Object> recursiveMergeMapNonNull(Map<String, ?> mapBaseFields,
			Map<String, ?> mapForeignFields) {
		Map<String, Object> mapMergedFields = new LinkedHashMap<>();
		mapMergedFields.putAll(mapBaseFields);

		for (Map.Entry<String, ?> foreignField : mapForeignFields.entrySet()) {
			String key = foreignField.getKey();
			Object baseFieldValue = mapBaseFields.get(foreignField.getKey());
			Object foreignFieldValue = foreignField.getValue();

			if (foreignField.getValue() == null) {
				// Do not merge field if input field is null
				continue;
			}

			if (baseFieldValue instanceof Map<?, ?> && foreignFieldValue instanceof Map<?, ?>) {
				// If base and foreign are maps, recursive merge
				@SuppressWarnings("unchecked")
				Map<String, ?> mapBaseFieldValue = (Map<String, ?>) baseFieldValue;
				@SuppressWarnings("unchecked")
				Map<String, ?> mapForeignFieldValue = (Map<String, ?>) foreignFieldValue;
				Map<String, Object> mapMergedFieldValue =
						recursiveMergeMapNonNull(mapBaseFieldValue, mapForeignFieldValue);
				mapMergedFields.put(key, mapMergedFieldValue);
			} else {
				// In other cases, just put/update using foreign field
				mapMergedFields.put(key, foreignFieldValue);
			}
		}

		return mapMergedFields;
	}

	/**
	 *
	 * @param objectMapper
	 * @param o
	 * @return the natural JSON representation, without explicit exceptions
	 */
	public static String toString(ObjectMapper objectMapper, Object o) {
		String keysAsString;
		try {
			keysAsString = objectMapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
		return keysAsString;
	}

	/**
	 * We consider that in most cases, we manipulate a String representing a POJO/an object, but not an Array/a
	 * primitive String/number.
	 *
	 * @param objectMapper
	 * @param asString
	 * @return the input String parsed as a Map
	 */
	public static Map<String, ?> fromString(ObjectMapper objectMapper, String asString) {
		try {
			return objectMapper.readValue(asString, Map.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <T> T fromString(ObjectMapper objectMapper, String json, Class<? extends T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Invalid json: " + json, e);
		}
	}

	/**
	 *
	 * @param o
	 * @return a safe toString for logging purposes
	 */
	public static String safeHumanString(Object o) {
		try {
			return toString(HUMAN_OBJECT_MAPPER, o);
		} catch (RuntimeException e) {
			return "{\"error\":\"" + e.getMessage() + "\"}";
		}
	}
}