/**
 * The MIT License
 * Copyright (c) 2008-2016 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.snapshot.extension;

import java.util.EnumSet;

/**
 * Enumeration of known JDK collection versions
 *
 * @since 1.6
 */
public enum JdkVersion {
	SUN, IBM14, IBM15, IBM16, // Harmony based collections
	IBM17, // Oracle based collections
	IBM18, JAVA18;

	// helpers
	public static EnumSet<JdkVersion> ALL = EnumSet.allOf(JdkVersion.class);

	public static EnumSet<JdkVersion> of(JdkVersion first, JdkVersion... rest) {
		return EnumSet.of(first, rest);
	}

	public static EnumSet<JdkVersion> except(JdkVersion first, JdkVersion... rest) {
		return EnumSet.complementOf(EnumSet.of(first, rest));
	}
}