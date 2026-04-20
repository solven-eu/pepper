/**
 * The MIT License
 * Copyright (c) 2026 Benoit Lacelle - SOLVEN
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicTest;

import lombok.experimental.UtilityClass;

/**
 * Runtime contract verifier for custom {@link Map} implementations — same spirit as {@code EqualsVerifier}: a fluent
 * builder and a single {@code verify()} call.
 *
 * Two entry points, matching the two flavours of "map under test":
 * <ul>
 * <li>{@link #forSupplier(Supplier)} — full lifecycle on a mutable map. Runs ~20 fixed scenarios (put/remove/clear,
 * null handling, equals/hashCode/iteration) plus a randomized torture sequence against a {@link LinkedHashMap}
 * reference.</li>
 * <li>{@link #forInstance(Map)} — contract checks on a populated, typically-immutable map. Verifies
 * equals/hashCode/toString, keySet/entrySet/values, {@code get}/{@code containsKey}/{@code containsValue} for every
 * entry, and wrong-type key handling — all against a {@link LinkedHashMap} snapshot of the instance's own entries.</li>
 * </ul>
 *
 * Usage (supplier):
 *
 * <pre>{@code
 * MapVerifier.forSupplier(MyMap<String, Integer>::new)
 * 		.withSampleKeys("a", "b", "c")
 * 		.withSampleValues(1, 2, 3)
 * 		.preservesInsertionOrder()
 * 		.verify();
 * }</pre>
 *
 * Usage (instance):
 *
 * <pre>{@code
 * MapVerifier.forInstance(populatedImmutableMap).verify();
 * }</pre>
 *
 * Flags that are not declared explicitly are probed on a throwaway instance: a {@link NullPointerException} from
 * {@code put(k, null)} infers {@code rejectsNullValues()}; an {@link UnsupportedOperationException} from {@code put}
 * infers {@code immutable()}.
 *
 * @author Benoit Lacelle
 */
@UtilityClass
@SuppressWarnings({ "checkstyle:HideUtilityClassConstructor",
		"checkstyle:MagicNumber",
		"checkstyle:LeftCurly",
		"checkstyle:RightCurly" })
public final class MapVerifier {

	/**
	 * Supplier-based entry point — full lifecycle verification on a mutable map.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param supplier
	 *            produces a fresh empty map instance on every call
	 * @return a builder to declare optional flags and finally {@link SupplierBuilder#verify()}
	 */
	public static <K, V> SupplierBuilder<K, V> forSupplier(Supplier<Map<K, V>> supplier) {
		return new SupplierBuilder<>(supplier);
	}

	/**
	 * Instance-based entry point — contract checks on a populated map. No sample keys/values required; the instance's
	 * own entries act as the "present" cases, and a wrong-type {@link Object} acts as the "absent" case.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param instance
	 *            the populated map to verify
	 * @return a builder to declare optional flags and finally {@link InstanceBuilder#verify()}
	 */
	public static <K, V> InstanceBuilder<K, V> forInstance(Map<K, V> instance) {
		return new InstanceBuilder<>(instance);
	}

	// =============================================================================================================
	// Supplier-based builder — exercises the full Map lifecycle.
	// =============================================================================================================

	/**
	 * Fluent configuration then terminal {@link #verify()} or {@link #buildTests()} for a supplier-backed verifier.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 */
	@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
	public static final class SupplierBuilder<K, V> {

		private final Supplier<Map<K, V>> supplier;
		private List<K> sampleKeys = List.of();
		private List<V> sampleValues = List.of();

		// Boxed tri-state: null = probe, TRUE/FALSE = explicit override.
		private Boolean immutable;
		private Boolean rejectsNullKeys;
		private Boolean rejectsNullValues;
		private boolean preservesInsertionOrder;
		private long seed = System.nanoTime();
		private int randomOps = 200;
		private int randomRepetitions = 10;

		private SupplierBuilder(Supplier<Map<K, V>> supplier) {
			this.supplier = supplier;
		}

		/**
		 * At least three distinct sample keys (two act as present keys, one as the "probe absent" key).
		 */
		@SafeVarargs
		public final SupplierBuilder<K, V> withSampleKeys(K... keys) {
			if (keys.length < 3) {
				throw new IllegalArgumentException("withSampleKeys requires >= 3 distinct keys, got " + keys.length);
			}
			this.sampleKeys = List.of(keys);
			return this;
		}

		/**
		 * At least two distinct sample values.
		 */
		@SafeVarargs
		public final SupplierBuilder<K, V> withSampleValues(V... values) {
			if (values.length < 2) {
				throw new IllegalArgumentException(
						"withSampleValues requires >= 2 distinct values, got " + values.length);
			}
			this.sampleValues = List.of(values);
			return this;
		}

		/** Asserts the map rejects {@code null} keys. Overrides the probe. */
		public SupplierBuilder<K, V> rejectsNullKeys() {
			this.rejectsNullKeys = Boolean.TRUE;
			return this;
		}

		/** Asserts the map rejects {@code null} values. Overrides the probe. */
		public SupplierBuilder<K, V> rejectsNullValues() {
			this.rejectsNullValues = Boolean.TRUE;
			return this;
		}

		/** Asserts the map is immutable — every mutating operation must throw UOE. Overrides the probe. */
		public SupplierBuilder<K, V> immutable() {
			this.immutable = Boolean.TRUE;
			return this;
		}

		/** Asserts iteration order equals insertion order. */
		public SupplierBuilder<K, V> preservesInsertionOrder() {
			this.preservesInsertionOrder = true;
			return this;
		}

		/** Fixes the random seed for the torture sequence. */
		public SupplierBuilder<K, V> randomSeed(long seed) {
			this.seed = seed;
			return this;
		}

		/** Number of random operations per torture repetition (default 200). */
		public SupplierBuilder<K, V> randomOps(int count) {
			this.randomOps = count;
			return this;
		}

		/**
		 * Number of torture repetitions — only honoured by {@link #buildTests()}; {@link #verify()} runs exactly one.
		 */
		public SupplierBuilder<K, V> randomRepetitions(int count) {
			this.randomRepetitions = count;
			return this;
		}

		/**
		 * Runs every scenario and throws an {@link AssertionError} aggregating all failures.
		 */
		public void verify() {
			ResolvedFlags flags = probe();
			List<Scenario> scenarios = buildScenarios(flags);
			scenarios.add(new Scenario("randomizedOps", () -> runRandomized(flags, seed)));
			org.junit.jupiter.api.Assertions.assertAll(scenarios.stream()
					.map(s -> (org.junit.jupiter.api.function.Executable) s.body::execute)
					.toArray(org.junit.jupiter.api.function.Executable[]::new));
		}

		/**
		 * Returns one {@link DynamicTest} per scenario — use under {@code @TestFactory} for per-scenario reporting.
		 */
		public Stream<DynamicTest> buildTests() {
			ResolvedFlags flags = probe();
			List<Scenario> scenarios = buildScenarios(flags);
			for (int i = 0; i < randomRepetitions; i++) {
				long repSeed = seed + i;
				scenarios.add(new Scenario("randomizedOps[seed=" + repSeed + "]", () -> runRandomized(flags, repSeed)));
			}
			return scenarios.stream().map(s -> DynamicTest.dynamicTest(s.name, s.body::execute));
		}

		// Snapshot of the resolved probe flags — computed once so scenarios see a stable view.
		private static final class ResolvedFlags {
			final boolean immutable;
			final boolean rejectsNullKeys;
			final boolean rejectsNullValues;

			ResolvedFlags(boolean immutable, boolean rejectsNullKeys, boolean rejectsNullValues) {
				this.immutable = immutable;
				this.rejectsNullKeys = rejectsNullKeys;
				this.rejectsNullValues = rejectsNullValues;
			}
		}

		private ResolvedFlags probe() {
			if (sampleKeys.isEmpty() || sampleValues.isEmpty()) {
				throw new IllegalStateException("withSampleKeys(...) and withSampleValues(...) are required");
			}
			boolean resolvedImmutable;
			if (immutable != null) {
				resolvedImmutable = immutable;
			} else {
				resolvedImmutable = probeImmutable();
			}
			boolean resolvedRejectsNullKeys;
			if (rejectsNullKeys != null) {
				resolvedRejectsNullKeys = rejectsNullKeys;
			} else if (resolvedImmutable) {
				resolvedRejectsNullKeys = false;
			} else {
				resolvedRejectsNullKeys = probeRejectsNullKeys();
			}
			boolean resolvedRejectsNullValues;
			if (rejectsNullValues != null) {
				resolvedRejectsNullValues = rejectsNullValues;
			} else if (resolvedImmutable) {
				resolvedRejectsNullValues = false;
			} else {
				resolvedRejectsNullValues = probeRejectsNullValues();
			}
			return new ResolvedFlags(resolvedImmutable, resolvedRejectsNullKeys, resolvedRejectsNullValues);
		}

		private boolean probeImmutable() {
			try {
				supplier.get().put(sampleKeys.get(0), sampleValues.get(0));
				return false;
			} catch (UnsupportedOperationException e) {
				return true;
			} catch (RuntimeException e) {
				return false;
			}
		}

		@SuppressFBWarnings("DCN_NULLPOINTER_EXCEPTION")
		private boolean probeRejectsNullKeys() {
			try {
				supplier.get().put(null, sampleValues.get(0));
				return false;
			} catch (NullPointerException e) {
				return true;
			} catch (RuntimeException e) {
				return false;
			}
		}

		@SuppressFBWarnings("DCN_NULLPOINTER_EXCEPTION")
		private boolean probeRejectsNullValues() {
			try {
				supplier.get().put(sampleKeys.get(0), null);
				return false;
			} catch (NullPointerException e) {
				return true;
			} catch (RuntimeException e) {
				return false;
			}
		}

		private List<Scenario> buildScenarios(ResolvedFlags flags) {
			List<Scenario> out = new ArrayList<>();
			out.add(new Scenario("empty", this::checkEmpty));
			out.add(new Scenario("get_absent_returnsNull", this::checkGetAbsent));
			out.add(new Scenario("getOrDefault_absent_returnsDefault", this::checkGetOrDefaultAbsent));
			out.add(new Scenario("containsKey_absent_false", this::checkContainsKeyAbsent));
			out.add(new Scenario("containsKey_wrongType_false", this::checkContainsKeyWrongType));

			if (flags.immutable) {
				out.add(new Scenario("put_throwsUOE",
						() -> checkMutationThrows(m -> m.put(sampleKeys.get(0), sampleValues.get(0)))));
				out.add(new Scenario("remove_throwsUOE", () -> checkMutationThrows(m -> m.remove(sampleKeys.get(0)))));
				out.add(new Scenario("clear_throwsUOE", () -> checkMutationThrows(Map::clear)));
				out.add(new Scenario("putAll_throwsUOE",
						() -> checkMutationThrows(m -> m.putAll(Map.of(sampleKeys.get(0), sampleValues.get(0))))));
			} else {
				out.add(new Scenario("put_newKey_returnsNull", this::checkPutNewKey));
				out.add(new Scenario("put_existingKey_returnsOldValue", this::checkPutExistingKey));
				out.add(new Scenario("remove_absent_returnsNull", this::checkRemoveAbsent));
				out.add(new Scenario("remove_present_returnsOldValue", this::checkRemovePresent));
				out.add(new Scenario("clear_empties", this::checkClear));
				out.add(new Scenario("putAll_matchesReference", this::checkPutAll));
				out.add(new Scenario("containsValue", this::checkContainsValue));
				out.add(new Scenario("equalsHashCode_matchReference", this::checkEqualsHashCode));
				out.add(new Scenario("entrySet_matchesReference", this::checkEntrySet));
				out.add(new Scenario("keySet_matchesReference", this::checkKeySet));
				out.add(new Scenario("values_matchesReference", this::checkValues));
				out.add(new Scenario("forEach_visitsAll", this::checkForEach));
				if (flags.rejectsNullValues) {
					out.add(new Scenario("put_nullValue_throwsNPE", this::checkPutNullValueThrows));
				}
				if (flags.rejectsNullKeys) {
					out.add(new Scenario("put_nullKey_throwsNPE", this::checkPutNullKeyThrows));
				}
			}
			return out;
		}

		// --- scenario bodies -----------------------------------------------------------------------------------

		private void checkEmpty() {
			Map<K, V> m = supplier.get();
			Assertions.assertThat(m).as("empty").isEmpty();
			Assertions.assertThat(m.size()).as("size").isZero();
		}

		private void checkGetAbsent() {
			Assertions.assertThat(supplier.get().get(sampleKeys.get(0))).as("get on empty").isNull();
		}

		private void checkGetOrDefaultAbsent() {
			Assertions.assertThat(supplier.get().getOrDefault(sampleKeys.get(0), sampleValues.get(0)))
					.as("getOrDefault on empty")
					.isEqualTo(sampleValues.get(0));
		}

		private void checkContainsKeyAbsent() {
			Assertions.assertThat(supplier.get().containsKey(sampleKeys.get(0))).as("containsKey on empty").isFalse();
		}

		private void checkContainsKeyWrongType() {
			Assertions.assertThat(supplier.get().containsKey(new Object())).as("wrong-type containsKey").isFalse();
		}

		private void checkPutNewKey() {
			Map<K, V> m = supplier.get();
			Assertions.assertThat(m.put(sampleKeys.get(0), sampleValues.get(0))).as("put new returns null").isNull();
			Assertions.assertThat(m).as("after put").containsEntry(sampleKeys.get(0), sampleValues.get(0)).hasSize(1);
		}

		private void checkPutExistingKey() {
			Map<K, V> m = supplier.get();
			m.put(sampleKeys.get(0), sampleValues.get(0));
			Assertions.assertThat(m.put(sampleKeys.get(0), sampleValues.get(1)))
					.as("put existing returns old")
					.isEqualTo(sampleValues.get(0));
			Assertions.assertThat(m).hasSize(1).containsEntry(sampleKeys.get(0), sampleValues.get(1));
		}

		private void checkPutNullValueThrows() {
			Map<K, V> m = supplier.get();
			Assertions.assertThatThrownBy(() -> m.put(sampleKeys.get(0), null))
					.isInstanceOf(NullPointerException.class);
		}

		private void checkPutNullKeyThrows() {
			Map<K, V> m = supplier.get();
			Assertions.assertThatThrownBy(() -> m.put(null, sampleValues.get(0)))
					.isInstanceOf(NullPointerException.class);
		}

		private void checkRemoveAbsent() {
			Assertions.assertThat(supplier.get().remove(sampleKeys.get(0))).as("remove absent").isNull();
		}

		private void checkRemovePresent() {
			Map<K, V> m = supplier.get();
			m.put(sampleKeys.get(0), sampleValues.get(0));
			m.put(sampleKeys.get(1), sampleValues.get(1));
			Assertions.assertThat(m.remove(sampleKeys.get(0))).as("remove present").isEqualTo(sampleValues.get(0));
			Assertions.assertThat(m)
					.hasSize(1)
					.doesNotContainKey(sampleKeys.get(0))
					.containsEntry(sampleKeys.get(1), sampleValues.get(1));
		}

		private void checkClear() {
			Map<K, V> m = supplier.get();
			m.put(sampleKeys.get(0), sampleValues.get(0));
			m.put(sampleKeys.get(1), sampleValues.get(1));
			m.clear();
			Assertions.assertThat(m).as("after clear").isEmpty();
			m.put(sampleKeys.get(2), sampleValues.get(0));
			Assertions.assertThat(m.get(sampleKeys.get(2))).isEqualTo(sampleValues.get(0));
		}

		private void checkPutAll() {
			Map<K, V> m = supplier.get();
			Map<K, V> src = new LinkedHashMap<>();
			src.put(sampleKeys.get(0), sampleValues.get(0));
			src.put(sampleKeys.get(1), sampleValues.get(1));
			m.putAll(src);
			Assertions.assertThat(m).containsExactlyInAnyOrderEntriesOf(src);
		}

		private void checkContainsValue() {
			Map<K, V> m = supplier.get();
			m.put(sampleKeys.get(0), sampleValues.get(0));
			Assertions.assertThat(m.containsValue(sampleValues.get(0))).isTrue();
			Assertions.assertThat(m.containsValue(sampleValues.get(1))).isFalse();
		}

		private void checkEqualsHashCode() {
			Map<K, V> m = supplier.get();
			Map<K, V> ref = new LinkedHashMap<>();
			m.put(sampleKeys.get(0), sampleValues.get(0));
			m.put(sampleKeys.get(1), sampleValues.get(1));
			ref.put(sampleKeys.get(0), sampleValues.get(0));
			ref.put(sampleKeys.get(1), sampleValues.get(1));
			Assertions.assertThat(m).as("equals").isEqualTo(ref);
			Assertions.assertThat(m.hashCode()).as("hashCode").isEqualTo(ref.hashCode());
		}

		private void checkEntrySet() {
			Map<K, V> m = supplier.get();
			Map<K, V> ref = populateBoth(m);
			Assertions.assertThat(m.entrySet()).containsExactlyInAnyOrderElementsOf(ref.entrySet());
			if (preservesInsertionOrder) {
				Assertions.assertThat(m.entrySet()).containsExactlyElementsOf(ref.entrySet());
			}
		}

		private void checkKeySet() {
			Map<K, V> m = supplier.get();
			Map<K, V> ref = populateBoth(m);
			Assertions.assertThat(m.keySet()).containsExactlyInAnyOrderElementsOf(ref.keySet());
			if (preservesInsertionOrder) {
				Assertions.assertThat(m.keySet()).containsExactlyElementsOf(ref.keySet());
			}
		}

		private void checkValues() {
			Map<K, V> m = supplier.get();
			Map<K, V> ref = populateBoth(m);
			Assertions.assertThat(m.values()).containsExactlyInAnyOrderElementsOf(ref.values());
			if (preservesInsertionOrder) {
				Assertions.assertThat(m.values()).containsExactlyElementsOf(ref.values());
			}
		}

		private void checkForEach() {
			Map<K, V> m = supplier.get();
			Map<K, V> ref = populateBoth(m);
			Map<K, V> collected = new LinkedHashMap<>();
			m.forEach(collected::put);
			Assertions.assertThat(collected).containsExactlyInAnyOrderEntriesOf(ref);
		}

		private void checkMutationThrows(MutationOp<K, V> op) {
			Map<K, V> m = supplier.get();
			Assertions.assertThatThrownBy(() -> op.apply(m)).isInstanceOf(UnsupportedOperationException.class);
		}

		private Map<K, V> populateBoth(Map<K, V> m) {
			Map<K, V> ref = new LinkedHashMap<>();
			int count = Math.min(sampleKeys.size(), sampleValues.size());
			for (int i = 0; i < count; i++) {
				m.put(sampleKeys.get(i), sampleValues.get(i % sampleValues.size()));
				ref.put(sampleKeys.get(i), sampleValues.get(i % sampleValues.size()));
			}
			return ref;
		}

		// --- randomized torture --------------------------------------------------------------------------------

		private void runRandomized(ResolvedFlags flags, long repSeed) {
			if (flags.immutable) {
				return;
			}
			Random rnd = new Random(repSeed);
			Map<K, V> m = supplier.get();
			Map<K, V> ref = new LinkedHashMap<>();
			List<String> trace = new ArrayList<>();
			try {
				for (int i = 0; i < randomOps; i++) {
					applyRandomOp(rnd, m, ref, trace);
				}
				Assertions.assertThat(m)
						.as("post-sequence equality (seed=" + repSeed + ")")
						.containsExactlyInAnyOrderEntriesOf(ref);
			} catch (Throwable t) {
				throw new AssertionError(
						"Randomized torture failed. seed=" + repSeed + "\nops=\n  " + String.join("\n  ", trace),
						t);
			}
		}

		@SuppressWarnings("checkstyle:LineLength")
		private void applyRandomOp(Random r, Map<K, V> m, Map<K, V> ref, List<String> trace) {
			K k = sampleKeys.get(r.nextInt(sampleKeys.size()));
			V v = sampleValues.get(r.nextInt(sampleValues.size()));
			switch (r.nextInt(6)) {
			case 0 -> {
				trace.add("put(" + k + ", " + v + ")");
				Assertions.assertThat(m.put(k, v)).isEqualTo(ref.put(k, v));
			}
			case 1 -> {
				trace.add("remove(" + k + ")");
				Assertions.assertThat(m.remove(k)).isEqualTo(ref.remove(k));
			}
			case 2 -> {
				trace.add("get(" + k + ")");
				Assertions.assertThat(m.get(k)).isEqualTo(ref.get(k));
			}
			case 3 -> {
				trace.add("containsKey(" + k + ")");
				Assertions.assertThat(m.containsKey(k)).isEqualTo(ref.containsKey(k));
			}
			case 4 -> {
				trace.add("size()");
				Assertions.assertThat(m.size()).isEqualTo(ref.size());
			}
			case 5 -> {
				if (r.nextInt(10) == 0) {
					trace.add("clear()");
					m.clear();
					ref.clear();
				}
			}
			default -> throw new IllegalStateException();
			}
		}
	}

	// =============================================================================================================
	// Instance-based builder — contract checks on a populated map. No sample keys/values required.
	// =============================================================================================================

	/**
	 * Fluent configuration then terminal {@link #verify()} for an instance-backed verifier.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 */
	public static final class InstanceBuilder<K, V> {

		@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
		private final Map<K, V> instance;
		@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
		private boolean preservesInsertionOrder;

		private InstanceBuilder(Map<K, V> instance) {
			if (instance == null) {
				throw new IllegalArgumentException("instance is required");
			}
			this.instance = instance;
		}

		/**
		 * Asserts iteration order of {@link Map#keySet()}, {@link Map#entrySet()}, {@link Map#values()} matches the
		 * order of the reference snapshot built via {@code new LinkedHashMap<>(instance)}.
		 */
		public InstanceBuilder<K, V> preservesInsertionOrder() {
			this.preservesInsertionOrder = true;
			return this;
		}

		/**
		 * Runs every scenario and throws an {@link AssertionError} aggregating all failures.
		 */
		@SuppressWarnings("CPD-START")
		public void verify() {
			Map<K, V> ref = new LinkedHashMap<>(instance);

			List<Scenario> scenarios = new ArrayList<>();
			scenarios.add(new Scenario("size_matchesReference", () -> checkSize(ref)));
			scenarios.add(new Scenario("isEmpty_matchesReference", () -> checkIsEmpty(ref)));
			scenarios.add(new Scenario("equals_matchesReference", () -> checkEquals(ref)));
			scenarios.add(new Scenario("hashCode_matchesReference", () -> checkHashCode(ref)));
			scenarios.add(new Scenario("toString_matchesReference", () -> checkToString(ref)));
			scenarios.add(new Scenario("keySet_matchesReference", () -> checkKeySet(ref)));
			scenarios.add(new Scenario("entrySet_matchesReference", () -> checkEntrySet(ref)));
			scenarios.add(new Scenario("values_matchesReference", () -> checkValues(ref)));
			scenarios.add(new Scenario("getAndContainsForEveryEntry", () -> checkGetAndContains(ref)));
			scenarios.add(new Scenario("containsKey_wrongType_false", this::checkContainsKeyWrongType));
			scenarios.add(new Scenario("forEach_visitsAll", () -> checkForEach(ref)));

			org.junit.jupiter.api.Assertions.assertAll(scenarios.stream()
					.map(s -> (org.junit.jupiter.api.function.Executable) s.body::execute)
					.toArray(org.junit.jupiter.api.function.Executable[]::new));
		}

		// --- scenario bodies -----------------------------------------------------------------------------------

		private void checkSize(Map<K, V> ref) {
			Assertions.assertThat(instance.size()).as("size").isEqualTo(ref.size());
		}

		private void checkIsEmpty(Map<K, V> ref) {
			Assertions.assertThat(instance.isEmpty()).as("isEmpty").isEqualTo(ref.isEmpty());
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void checkEquals(Map<K, V> ref) {
			Assertions.assertThat((Map) instance).as("equals").isEqualTo(ref);
			Assertions.assertThat(ref).as("equals symmetry").isEqualTo(instance);
		}

		private void checkHashCode(Map<K, V> ref) {
			Assertions.assertThat(instance.hashCode()).as("hashCode").isEqualTo(ref.hashCode());
		}

		private void checkToString(Map<K, V> ref) {
			Assertions.assertThat(instance.toString()).as("toString").isEqualTo(ref.toString());
		}

		private void checkKeySet(Map<K, V> ref) {
			Assertions.assertThat(instance.keySet())
					.as("keySet equals")
					.isEqualTo(ref.keySet())
					.hasSameHashCodeAs(ref.keySet())
					.hasToString(ref.keySet().toString());
			if (preservesInsertionOrder) {
				Assertions.assertThat(instance.keySet()).containsExactlyElementsOf(ref.keySet());
			}
		}

		private void checkEntrySet(Map<K, V> ref) {
			Assertions.assertThat(instance.entrySet())
					.as("entrySet equals")
					.isEqualTo(ref.entrySet())
					.hasSameHashCodeAs(ref.entrySet())
					.hasToString(ref.entrySet().toString());
			if (preservesInsertionOrder) {
				Assertions.assertThat(instance.entrySet()).containsExactlyElementsOf(ref.entrySet());
			}
		}

		private void checkValues(Map<K, V> ref) {
			Assertions.assertThat(instance.values())
					.as("values (any order)")
					.containsExactlyInAnyOrderElementsOf(ref.values());
			if (preservesInsertionOrder) {
				Assertions.assertThat(instance.values()).containsExactlyElementsOf(ref.values());
			}
		}

		private void checkGetAndContains(Map<K, V> ref) {
			for (Map.Entry<K, V> e : ref.entrySet()) {
				Assertions.assertThat(instance.get(e.getKey())).as("get(" + e.getKey() + ")").isEqualTo(e.getValue());
				Assertions.assertThat(instance.containsKey(e.getKey())).as("containsKey(" + e.getKey() + ")").isTrue();
				Assertions.assertThat(instance.containsValue(e.getValue()))
						.as("containsValue(" + e.getValue() + ")")
						.isTrue();
			}
		}

		private void checkContainsKeyWrongType() {
			Assertions.assertThat(instance.containsKey(new Object())).as("wrong-type containsKey").isFalse();
		}

		private void checkForEach(Map<K, V> ref) {
			Map<K, V> collected = new LinkedHashMap<>();
			instance.forEach(collected::put);
			Assertions.assertThat(collected).containsExactlyInAnyOrderEntriesOf(ref);
		}
	}

	// =============================================================================================================
	// Shared scaffolding
	// =============================================================================================================

	@FunctionalInterface
	private interface ThrowingRunnable {
		void execute() throws Throwable;
	}

	@FunctionalInterface
	private interface MutationOp<K, V> {
		void apply(Map<K, V> m);
	}

	private static final class Scenario {
		final String name;
		final ThrowingRunnable body;

		Scenario(String name, ThrowingRunnable body) {
			this.name = name;
			this.body = body;
		}
	}

}
