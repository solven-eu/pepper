/**
 * The MIT License
 * Copyright (c) 2024 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.mappath;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables doing `.remove` operations referring paths a-la-MapPath.
 * 
 * @author Benoit Lacelle
 *
 */
public class MapPathRemove {
	private static final Logger LOGGER = LoggerFactory.getLogger(MapPath.class);

	protected MapPathRemove() {
		// hidden
	}

	/**
	 *
	 * @param <T>
	 * @param <S>
	 * @param currentMap
	 * @param mainKey
	 * @param moreKeys
	 * @return a modified view of the Map. It may be the original Map, or a different one if it was immutable.
	 */
	public static <T, S> Map<T, S> remove(Map<T, S> currentMap, Object mainKey, Object... moreKeys) {
		if (moreKeys.length == 0) {
			if (!currentMap.containsKey(mainKey)) {
				return currentMap;
			} else {
				try {
					currentMap.remove(mainKey);
					return currentMap;
				} catch (RuntimeException e) {
					LOGGER.debug("The Map seems not writable", e);
					// Convert to a writable map
					Map<T, S> mutableMap = MapPathPut.convertToMutableMap(currentMap);
					mutableMap.remove(mainKey);
					return mutableMap;
				}
			}
		} else {
			var moreKeysWithoutLast = Arrays.copyOf(moreKeys, moreKeys.length - 1);
			Optional<?> optParentObject = MapPathGet.getOptionalAs(currentMap, mainKey, moreKeysWithoutLast);

			if (optParentObject.isPresent() && optParentObject.get() instanceof Map<?, ?>) {
				Map<?, ?> removed = remove((Map<?, ?>) optParentObject.get(), moreKeys[moreKeys.length - 1]);

				try {
					MapPathPut.rawPutEntry(true, currentMap, removed, MapPathGet.asList(mainKey, moreKeysWithoutLast));
					return currentMap;
				} catch (RuntimeException e) {
					LOGGER.debug("The Map seems not writable", e);
					// Convert to a writable map
					Map<T, S> mutableMap = MapPathPut.convertToMutableMap(currentMap);
					MapPathPut.rawPutEntry(true, mutableMap, removed, MapPathGet.asList(mainKey, moreKeysWithoutLast));
					return mutableMap;
				}

			} else {
				return currentMap;
			}
		}
	}
}
