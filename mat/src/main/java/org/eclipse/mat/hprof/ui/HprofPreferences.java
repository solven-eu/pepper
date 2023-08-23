/*******************************************************************************
 * Copyright (c) 2008, 2023 SAP AG, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - additional debug information
 *    Netflix (Jason Koch) - refactors for increased performance and concurrency
 *******************************************************************************/
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
