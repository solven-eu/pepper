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
package cormoran.pepper.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.math.LongMath;

/**
 * Helpers related to cartesian products. It is typically used to compute covering cartesian products
 * 
 * @author Benoit Lacelle
 *
 */
@Beta
public class CartesianProductHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CartesianProductHelper.class);

	/**
	 * Used to flag the intersection failed
	 */
	public static final Object GROUP_NOT_EXPRESSED = new Object();

	private static final int CARTESIAN_CARDINALITY_LOG = 1000000;

	protected CartesianProductHelper() {
		// hidden
	}

	@Beta
	public static <T, V> Set<? extends Map<T, ? extends Set<V>>> groupByKeyAndInValues(
			Iterable<? extends Map<? extends T, ? extends V>> input) {
		return groupByKeyAndInValues(input, any -> LOGGER.trace("Working"));
	}

	public static <T, V> Set<? extends Map<T, ? extends Set<V>>> groupByKeyAndInValues(
			Iterable<? extends Map<? extends T, ? extends V>> input,
			Consumer<Object> checkRegularly) {
		if (input instanceof Collection<?>) {
			Collection<?> asCollection = (Collection<?>) input;
			if (asCollection.size() >= 2) {
				LOGGER.debug("Compute covering cartesian products over {} entries", asCollection.size());
			} else {
				LOGGER.trace("Compute covering cartesian products over {} entries", asCollection.size());
			}
		} else {
			LOGGER.debug("Compute covering cartesian products over an Iterable");
		}

		Map<Set<T>, SetMultimap<T, V>> keysToKeyToEncountered = new LinkedHashMap<>();
		Map<Set<T>, Set<Map<T, V>>> keysToFlatMap = new LinkedHashMap<>();

		flattenInput(input, keysToKeyToEncountered, keysToFlatMap);

		if (LOGGER.isTraceEnabled()) {
			for (Entry<Set<T>, Set<Map<T, V>>> entry : keysToFlatMap.entrySet()) {
				LOGGER.trace("We flatten to {} entries for {}", entry.getValue().size(), entry.getKey());
			}
		}

		{
			Set<Set<T>> impactedKeySets = removeRedundantConditions(keysToKeyToEncountered, keysToFlatMap);

			// We have removed some flatMaps: we need to recompute keysToKeyToEncountered for these keySets
			for (Set<T> impactedKeySet : impactedKeySets) {
				// We want to rebuild the keysToKeyEncountered
				keysToKeyToEncountered.remove(impactedKeySet);

				// So we extracted the flattenMaps
				Set<Map<T, V>> impactedKeySetFlatMaps = keysToFlatMap.remove(impactedKeySet);
				assert impactedKeySetFlatMaps != null;

				// And resubmit the flatten maps for keysToKeyEncountered reconstruction
				flattenInput(impactedKeySetFlatMaps, keysToKeyToEncountered, keysToFlatMap);
			}
		}

		// Here, we have grouped by keySet. For each keyset, we have computed the Set of simple Map requested

		Set<Map<T, ? extends Set<V>>> coveringMaps = new LinkedHashSet<>();

		for (Entry<Set<T>, SetMultimap<T, V>> entry : keysToKeyToEncountered.entrySet()) {
			Set<T> keyset = entry.getKey();
			coveringMaps.addAll(computeCoveringMaps(keyset, entry.getValue(), keysToFlatMap.get(keyset)));
		}

		return coveringMaps;
	}

	protected static <T, V> Set<Set<T>> removeRedundantConditions(Map<Set<T>, SetMultimap<T, V>> keysToKeyToEncountered,
			Map<Set<T>, Set<Map<T, V>>> keysToFlatMap) {
		Set<Set<T>> impactedKeySet = new HashSet<>();

		Sets.cartesianProduct(Arrays.asList(keysToFlatMap.keySet(), keysToFlatMap.keySet()))
				.stream()
				.filter(list -> list.get(0).size() > list.get(1).size())
				.filter(list -> list.get(0).containsAll(list.get(1)))
				.forEach(list -> {
					Set<T> biggerKeySet = list.get(0);
					Set<T> smallerKeySet = list.get(1);

					// We have some flatMaps which expresses a smaller number of keys (e.g. a condition like
					// Country=France and another like Country=France&Ccy=EUR): it can be replaced with Country=France
					for (Map<T, V> smallestCondition : keysToFlatMap.get(smallerKeySet)) {
						Iterator<Map<T, V>> biggestConditionIt = keysToFlatMap.get(biggerKeySet).iterator();
						while (biggestConditionIt.hasNext()) {
							Map<T, V> biggestCondition = biggestConditionIt.next();
							if (biggestCondition.entrySet().containsAll(smallestCondition.entrySet())) {
								// The smallest condition is a softer constrain than the biggest condition: we can keep
								// only the softer condition as we do an OR between Maps/Conditions
								biggestConditionIt.remove();

								impactedKeySet.add(biggerKeySet);
							}
						}
					}
				});

		return impactedKeySet;
	}

	protected static <T, V> void flattenInput(Iterable<? extends Map<? extends T, ? extends V>> input,
			Map<Set<T>, SetMultimap<T, V>> keysToKeyToEncountered,
			Map<Set<T>, Set<Map<T, V>>> keysToFlatMap) {

		// For each input Map
		nextMap: for (Map<? extends T, ? extends V> map : input) {
			// Detect the keySet
			SetMultimap<T, V> keyToInValues = keysToKeyToEncountered.get(map.keySet());
			Set<Map<T, V>> flatMaps = keysToFlatMap.get(map.keySet());

			if (keyToInValues == null) {
				assert flatMaps == null;

				// This is our first encounter with the set of keys
				keyToInValues = SetMultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
				flatMaps = new LinkedHashSet<>();

				// Do not reference the original map
				Set<T> cleanKeySet = new LinkedHashSet<>(map.keySet());
				keysToKeyToEncountered.put(cleanKeySet, keyToInValues);
				keysToFlatMap.put(new LinkedHashSet<>(map.keySet()), flatMaps);
			} else {
				assert flatMaps != null;
			}

			List<T> keyWithCollection = new ArrayList<>();
			List<Set<V>> collectionValues = new ArrayList<>();

			// Extract the values for each key, being potentially wildcard or Collection
			for (Entry<? extends T, ? extends V> entry : map.entrySet()) {
				V value = entry.getValue();
				if (value == null) {
					LOGGER.debug("We skip {} as it holds a null value", map);
					// This Map is kind of empty
					continue nextMap;
				} else if (value instanceof Collection<?>) {
					@SuppressWarnings("unchecked")
					Collection<V> collectionValue = (Collection<V>) value;

					Set<V> valueAsSet = new LinkedHashSet<>(collectionValue);

					if (valueAsSet.size() == 0) {
						LOGGER.debug("We skip {} as it holds an empty Collection value", map);
						// This Map is kind of empty
						continue nextMap;
					} else {
						keyWithCollection.add(entry.getKey());
						collectionValues.add(valueAsSet);
						keyToInValues.putAll(entry.getKey(), valueAsSet);
					}
				} else {
					keyWithCollection.add(entry.getKey());
					collectionValues.add(ImmutableSet.of(value));
					keyToInValues.put(entry.getKey(), value);
					// singleCoordinate.put(entry.getKey(), value);
				}
			}

			// Flatten the cartesian product
			Set<List<V>> allCombinations = Sets.cartesianProduct(collectionValues);

			for (List<V> oneCombination : allCombinations) {
				Map<T, V> duplicate = new LinkedHashMap<>();

				for (int i = 0; i < oneCombination.size(); i++) {
					duplicate.put(keyWithCollection.get(i), oneCombination.get(i));
				}

				// Register each combination as a single Map
				flatMaps.add(duplicate);
			}
		}
	}

	protected static <T, V> Collection<? extends Map<T, ? extends Set<V>>> computeCoveringMaps(Set<T> keySet,
			final SetMultimap<T, V> keyToEncountered,
			Set<Map<T, V>> flatMaps) {
		if (keyToEncountered.isEmpty()) {
			if (flatMaps.isEmpty()) {
				LOGGER.debug("There is not a single entry to match for keyset={}", keySet);
				return Collections.emptySet();
			} else if (flatMaps.size() == 1 && flatMaps.iterator().next().isEmpty()) {
				return Collections.singleton(Collections.emptyMap());
			} else {
				throw new IllegalStateException("keyToEncountered is empty while flatMaps holds " + flatMaps);
			}

		}

		int inputToCoverSize = flatMaps.size();
		long cartesianProductSize = computeCartesianSize(keyToEncountered);

		// We should have less entries than the cartesian product size. Else it means we missed some possible entry
		// candidate. If we have less entries, it typically means we have not a single cartesian product
		assert inputToCoverSize <= cartesianProductSize;

		if (inputToCoverSize == cartesianProductSize) {
			if (inputToCoverSize >= 2) {
				LOGGER.debug("We have a perfect cartesian product for keyset={} with size {}",
						keySet,
						cartesianProductSize);
			} else {
				LOGGER.trace("We have a perfect cartesian product for keyset={} with size {}",
						keySet,
						cartesianProductSize);
			}
			// This is a full cartesian product
			return Arrays.asList(Multimaps.asMap(keyToEncountered));
		} else {
			// The cartesian product would add elements which have not been requested: compute smaller cartesian
			// products
			List<T> keysOrderedByInSize = computeKeysOrder(keyToEncountered);

			// Search for the next key to consider break in smaller cartesian product
			// int nextKeyToBreak = 0;

			Iterator<T> keysToConsider = keysOrderedByInSize.iterator();

			List<T> subCartesianKeys = new ArrayList<>();

			while (keysToConsider.hasNext()) {
				T currentSplitKey = keysToConsider.next();
				Set<V> splitValues = keyToEncountered.get(currentSplitKey);

				if (splitValues.size() == 1) {
					// This is a single coordinate common to the whole cartesian product: it is common to all Map: we
					// can search for another key which will not be common to all entries
					continue;
				} else {
					subCartesianKeys.add(currentSplitKey);
					break;
				}
				// nextKeyToBreak++;
			}

			SetMultimap<Map<T, ? extends Set<V>>, Map<T, V>> reverseCovering =
					MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();

			fillReverseCovering(flatMaps, subCartesianKeys, keySet, keysToConsider, reverseCovering);

			Set<Map<T, ? extends Set<V>>> coveringMaps = new LinkedHashSet<>();

			for (Map<T, ? extends Set<V>> sharedCounterCovering : reverseCovering.keySet()) {
				Set<Map<T, V>> subCartesian = reverseCovering.get(sharedCounterCovering);

				// We have A1 associated to B1 and A2 associated to B1: we can retry to group A1 and A2 togethers
				Set<? extends Map<T, ? extends Set<V>>> grouped = groupByKeyAndInValues(subCartesian);

				for (Map<T, ? extends Set<V>> oneGroup : grouped) {
					Map<T, Set<V>> coveringMap = new LinkedHashMap<>(keySet.size());

					for (T key : keySet) {
						if (oneGroup.containsKey(key)) {
							coveringMap.put(key, oneGroup.get(key));
						} else if (sharedCounterCovering.containsKey(key)) {
							coveringMap.put(key, sharedCounterCovering.get(key));
						} else {
							LOGGER.warn("We expected to find {} in {} or {}", key, oneGroup, sharedCounterCovering);
						}
					}

					coveringMaps.add(coveringMap);
				}
			}

			return coveringMaps;
		}
	}

	private static <T, V> void fillReverseCovering(Set<Map<T, V>> flatMaps,
			List<T> subCartesianKeys,
			Set<T> keySet,
			Iterator<T> keysToConsider,
			SetMultimap<Map<T, ? extends Set<V>>, Map<T, V>> reverseCovering) {
		// Process until all flatMaps have been coverered
		while (!flatMaps.isEmpty()) {
			if (subCartesianKeys.size() + 1 == keySet.size()) {
				LOGGER.debug("We fallback on guaranteed sub-cartesian keyset={} for keyset={}",
						subCartesianKeys,
						keySet);
			} else {
				LOGGER.debug("We search for sub-cartesian keyset={} for keyset={}", subCartesianKeys, keySet);
			}

			// Consider one smaller cartesian product by splitKey
			Map<Map<T, V>, SetMultimap<T, V>> splitKeyToSmallCartesian = new LinkedHashMap<>();
			SetMultimap<Map<T, V>, Map<T, V>> splitKeyToFlatMap =
					SetMultimapBuilder.linkedHashKeys().linkedHashSetValues().build();

			// For each Map, associate it to the proper sub-cartesian
			for (Map<T, V> flatMap : flatMaps) {
				// Compute the key mapping to the sub-cartesian
				Map<T, V> subCartesianMap = new LinkedHashMap<>();
				for (T subCartesianKey : subCartesianKeys) {
					subCartesianMap.put(subCartesianKey, flatMap.get(subCartesianKey));
				}

				// Register
				splitKeyToFlatMap.put(subCartesianMap, flatMap);

				SetMultimap<T, V> subCartesian = splitKeyToSmallCartesian.get(subCartesianMap);

				if (subCartesian == null) {
					// This is our first encounter for this sub-cartesian
					subCartesian = SetMultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
					splitKeyToSmallCartesian.put(subCartesianMap, subCartesian);
				}

				// Ventilate current map to this sub-cartesian
				for (Entry<T, V> entry : flatMap.entrySet()) {
					subCartesian.put(entry.getKey(), entry.getValue());
				}
			}

			Set<Map<T, V>> matchingSubCartesianKeys = new LinkedHashSet<>();

			// For each splitValue, check if it is a perfect covering
			for (Map<T, V> subCartesianMap : splitKeyToFlatMap.keySet()) {
				long nbFlatMap = splitKeyToFlatMap.get(subCartesianMap).size();
				SetMultimap<T, V> cartesian = splitKeyToSmallCartesian.get(subCartesianMap);

				long cartesianSize = computeCartesianSize(cartesian);

				if (cartesianSize == nbFlatMap) {
					// The cartesian product associate to this value is a perfect match: register it and remove
					// associated flatMaps
					Map<T, Set<V>> cartesianAsMap = Multimaps.asMap(cartesian);
					// coveringMaps.add(cartesianAsMap);

					Map<T, Set<V>> difference = new LinkedHashMap<>(cartesianAsMap);
					difference.keySet().removeAll(subCartesianMap.keySet());
					reverseCovering.put(difference, subCartesianMap);

					matchingSubCartesianKeys.add(subCartesianMap);
				}
			}

			// Remove the elected covering cartesian
			for (Map<T, V> subCartesianMap : matchingSubCartesianKeys) {
				splitKeyToFlatMap.removeAll(subCartesianMap);
			}

			// Compile all Maps left to cover
			flatMaps = ImmutableSet.copyOf(splitKeyToFlatMap.values());

			if (keysToConsider.hasNext()) {
				// We will consider an additional for for sub-cartesian
				// We pick next give on a List sorted by relevancy
				subCartesianKeys.add(keysToConsider.next());
			} else {
				// All keys have been considered
				throw new IllegalStateException("We have depleted keys without covering all maps");
			}
		}
	}

	protected static <T, V> List<T> computeKeysOrder(final SetMultimap<T, V> keyToEncountered) {
		List<T> keysOrderedByInSize = new ArrayList<>(keyToEncountered.keySet());

		Collections.sort(keysOrderedByInSize, (o1, o2) -> {

			// Keep the keys with a low cardinality first. As high-cardinality keys have low probablity from
			// matching a lot of pattern (e.g. we have A*123456789 and B*12345 and C*12345: we prefer to keep
			// ABC giving ABC*12345 than 123456789 which matched only 123456789*A)
			int compareBySize = keyToEncountered.get(o1).size() - keyToEncountered.get(o2).size();

			if (compareBySize != 0) {
				return compareBySize;
			} else {
				// Compare by String (works for String, ILevelInfo,...)
				// TODO: should we rather keep the original order?
				return o1.toString().compareTo(o2.toString());
			}
		});

		return keysOrderedByInSize;
	}

	public static <T> long computeCartesianSize(SetMultimap<T, ?> keyToEncountered) {
		if (keyToEncountered.isEmpty()) {
			return 0;
		} else {

			long size = 1;

			for (T key : keyToEncountered.keySet()) {
				size *= keyToEncountered.get(key).size();
			}

			return size;
		}
	}

	/**
	 * 
	 * @param templates
	 *            the List of templates which has to be multiplied together (e.g. the first set would express (FR, USA)
	 *            and the second Set (Today, Yesterday). We want all combinations, leading at the end to
	 *            (FR,USA)*(Today, Yesterday))
	 * @return a Set of covering Map, each map having keys associated to either a single value, or a Collection of
	 *         coordinates
	 */
	public static <T> Set<? extends Map<T, ?>> mergeCartesianProducts(
			Iterable<? extends Set<? extends Map<? extends T, ?>>> templates) {
		return mergeCartesianProducts(templates, o -> LOGGER.trace("Working..."));

		// As this computation may be very long
		// to compute, we check occasionally the
		// timeout
		// ApexDatastoreHelper.checkInterruption(ApexCartesianProductHelper.class.getName());
	}

	public static <T> Set<? extends Map<T, ?>> mergeCartesianProducts(
			Iterable<? extends Set<? extends Map<? extends T, ?>>> templates,
			Consumer<Object> checkRegularly) {
		if (!templates.iterator().hasNext()) {
			return Collections.emptySet();
		}

		Set<Set<? extends Map<T, ? extends Set<?>>>> simplerTemplates = new LinkedHashSet<>();

		// Simplify each component of the cartesian product. Then the iteration over cartesian product will be faster
		for (Set<? extends Map<? extends T, ?>> oneGroupTemplates : templates) {
			if (oneGroupTemplates.isEmpty()) {
				// it typically happens when one bucket returned no groups
				return Collections.emptySet();
			}

			Set<? extends Map<T, ? extends Set<?>>> simplerTemplate = groupByKeyAndInValues(oneGroupTemplates);

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Simplified {} to {}", oneGroupTemplates, simplerTemplate);
			} else {
				LOGGER.debug("Simplified {} templates to {} templates",
						oneGroupTemplates.size(),
						simplerTemplate.size());
			}

			simplerTemplates.add(simplerTemplate);
		}

		Set<List<Map<T, ? extends Set<?>>>> cartesianProduct =
				Sets.cartesianProduct(Lists.newArrayList(simplerTemplates));

		final int cartesianProductSize = cartesianProduct.size();

		// We do the cartesian product with a merge operation as some group may
		// not be compatible, and then should be rejected.
		// For instance CountryGroup G8=FRANCE+UK and CcyGroup India=INDIA&ROUPI
		final AtomicLong nbChecked = new AtomicLong();
		Iterable<Map<T, ?>> merged = Iterables.transform(cartesianProduct, input -> {
			checkRegularly.accept(null);

			long currentCheck = nbChecked.incrementAndGet();
			if (currentCheck % CARTESIAN_CARDINALITY_LOG == 0) {
				LOGGER.info("Checking cartesian product: {}/{}", nbChecked, cartesianProductSize);
			}

			Map<T, ?> oneMerged = new LinkedHashMap<>();

			for (Map<T, ? extends Set<?>> oneToMerge : input) {
				Optional<Map<T, Object>> mergedAgainTemplate = mergeTemplate(oneMerged, oneToMerge);

				if (mergedAgainTemplate.isPresent()) {
					oneMerged = mergedAgainTemplate.get();
				} else {
					LOGGER.trace("We rejected combination {} after adding {} to {}", input, oneToMerge, oneMerged);
					return null;
				}
			}

			return oneMerged;
		});

		// Filter out null Map, which are rejected combinations
		Set<Map<T, ?>> output = Sets.newLinkedHashSet(Iterables.filter(merged, Predicates.notNull()));

		LOGGER.trace("Reduced to {} combinations", output.size());

		return output;
	}

	public static <T> Optional<Map<T, Object>> mergeTemplate(Map<? extends T, ?> currentTemplate,
			Map<? extends T, ?> additionalTemplate) {
		Map<T, Object> mergedTemplate = new LinkedHashMap<>();

		mergedTemplate.putAll(currentTemplate);

		for (T key : additionalTemplate.keySet()) {
			if (mergedTemplate.containsKey(key)) {
				// One group needs FR+US and another group needs US+JP: we need
				// FR+US+JP

				// If in addition the targetRangeLocation requires FR, the final
				// cell would be empty as at least one group is empty

				// If instead the targetRangeLocation requires US, we could
				// request one US
				Object baseCoordinate = currentTemplate.get(key);
				Object additionalCoordinate = additionalTemplate.get(key);

				Object intersection = intersectCoordinates(baseCoordinate, additionalCoordinate);

				if (intersection == GROUP_NOT_EXPRESSED) {
					return Optional.empty();
				} else {
					mergedTemplate.put(key, intersection);
				}
			} else {
				// Simple add
				mergedTemplate.put(key, additionalTemplate.get(key));
			}
		}

		return Optional.of(mergedTemplate);
	}

	/**
	 * @return true if the intersection is not empty
	 */
	public static <T> boolean intersectCoordinates(Map<T, Object> columnNameToConstraint,
			T columnName,
			Object rangeCoordinate) {
		Object existingConstrain = columnNameToConstraint.get(columnName);

		Object intersection = intersectCoordinates(existingConstrain, rangeCoordinate);

		if (intersection == null) {
			// No need to write wildcard constrain
			return true;
		} else if (intersection == GROUP_NOT_EXPRESSED) {
			columnNameToConstraint.put(columnName, GROUP_NOT_EXPRESSED);
			return false;
		} else {
			columnNameToConstraint.put(columnName, intersection);
			return true;
		}
	}

	public static <T> boolean intersectMapCoordinates(Map<T, Object> target, Map<? extends T, ?> source) {
		boolean result = true;

		for (Entry<? extends T, ?> sourceEntry : source.entrySet()) {
			// Mutate the target to intersect it with the source
			boolean entryResult = intersectCoordinates(target, sourceEntry.getKey(), sourceEntry.getValue());

			if (!entryResult) {
				// At least one intersection failed
				result = false;
			}
		}

		return result;
	}

	// TODO: We can not use Optional as null is a valid output
	public static Object intersectCoordinates(Object baseCoordinate, Object additionalCoordinate) {
		if (baseCoordinate == null) {
			return additionalCoordinate;
		} else if (additionalCoordinate == null) {
			return baseCoordinate;
		} else {
			// None is null
			// TODO: Use LocationUtil.doesCoordinateBelongToScope
			if (baseCoordinate instanceof Collection<?>) {
				if (additionalCoordinate instanceof Collection<?>) {
					// Both are collections
					// Sets.intersection(set1, set2) does not accept input Collections
					Set<Object> intersection = new LinkedHashSet<>((Collection<?>) baseCoordinate);
					intersection.retainAll((Collection<?>) additionalCoordinate);

					if (intersection.isEmpty()) {
						return GROUP_NOT_EXPRESSED;
					} else if (intersection.size() == 1) {
						return intersection.iterator().next();
					} else {
						return intersection;
					}
				} else {
					// only base in Collection
					if (((Collection<?>) baseCoordinate).contains(additionalCoordinate)) {
						return additionalCoordinate;
					} else {
						return GROUP_NOT_EXPRESSED;
					}
				}
			} else if (additionalCoordinate instanceof Collection<?>) {
				// only additional in Collection
				if (((Collection<?>) additionalCoordinate).contains(baseCoordinate)) {
					return baseCoordinate;
				} else {
					return GROUP_NOT_EXPRESSED;
				}
			} else {
				if (baseCoordinate.equals(additionalCoordinate)) {
					return baseCoordinate;
				} else {
					return GROUP_NOT_EXPRESSED;
				}
			}
		}
	}

	public static long cartesianProductSize(List<? extends Set<?>> listOfSets) {
		if (listOfSets.isEmpty()) {
			return 0L;
		} else {
			return listOfSets.stream().mapToLong(s -> s.size()).reduce(1, (l, r) -> LongMath.checkedMultiply(l, r));
		}
	}

}
