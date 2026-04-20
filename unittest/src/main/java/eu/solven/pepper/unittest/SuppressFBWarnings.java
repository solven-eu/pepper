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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to suppress SpotBug warnings.
 *
 * @author Lee Rhodes
 */
@Retention(RetentionPolicy.CLASS)
public @interface SuppressFBWarnings {

	/**
	 * A list of comma-separated, quoted SpotBugs warnings that are to be suppressed in the associated annotated
	 * element. The value can be a bug category, kind or pattern.
	 * 
	 * @return list of relevant bug descriptors
	 */
	String[] value() default {};

	/**
	 * Optional explanation for the suppression.
	 * 
	 * @return explanation
	 */
	String justification() default "";
}
