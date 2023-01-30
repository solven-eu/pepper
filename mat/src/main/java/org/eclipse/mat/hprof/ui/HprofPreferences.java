/**
 * The MIT License
 * Copyright (c) 2011 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.hprof.ui;

/**
 * Constant definitions for plug-in preferences
 */
public class HprofPreferences {
	/** Strictness of the HPROF parser */
	public static final String STRICTNESS_PREF = "hprofStrictness"; //$NON-NLS-1$

	/** Default strictness for preferences and value parsing */
	public static final HprofStrictness DEFAULT_STRICTNESS = HprofStrictness.STRICTNESS_STOP;

	/**
	 * Return the currently selected preference for strictness. This first checks the preference store, and then checks
	 * for any -D$(STRICTNESS)=true command line arguments.
	 *
	 * @return Current strictness preference or reflection of command line setting.
	 */
	public static HprofStrictness getCurrentStrictness() {
		// HprofPreferences.STRICTNESS_PREF
		HprofPreferences.HprofStrictness strictnessPreference = HprofPreferences.HprofStrictness
				.parse(System.getProperty("mat.strictness", HprofPreferences.DEFAULT_STRICTNESS.toString()));

		// Check if the user overrides on the command line
		for (HprofStrictness strictness : HprofStrictness.values()) {
			if (Boolean.getBoolean(strictness.toString())) {
				strictnessPreference = strictness;
				break;
			}
		}

		return strictnessPreference;
	}

	/**
	 * Enumeration for the parser strictness.
	 */
	public enum HprofStrictness {
		/**
		 * Throw an error and stop processing the dump.
		 */
		STRICTNESS_STOP("hprofStrictnessStop"), //$NON-NLS-1$

		/**
		 * Raise a warning and continue.
		 */
		STRICTNESS_WARNING("hprofStrictnessWarning"), //$NON-NLS-1$

		/**
		 * Raise a warning and try to "fix" the dump.
		 */
		STRICTNESS_PERMISSIVE("hprofStrictnessPermissive"); //$NON-NLS-1$

		private final String name;

		/**
		 * Enumeration value with a preference key.
		 *
		 * @param name
		 *            The preference key.
		 */
		HprofStrictness(String name) {
			this.name = name;
		}

		/**
		 * Return the preference key.
		 */
		@Override
		public String toString() {
			return name;
		}

		/**
		 * Given a stored preference value, return the enumeration value, or otherwise the default strictness.
		 *
		 * @param value
		 *            The preference value.
		 * @return Given a stored preference value, return the enumeration value, or otherwise the default strictness.
		 */
		public static HprofStrictness parse(String value) {
			if (value != null && value.length() > 0) {
				for (HprofStrictness strictness : values()) {
					if (strictness.toString().equals(value)) {
						return strictness;
					}
				}
			}
			return DEFAULT_STRICTNESS;
		}
	}
}
