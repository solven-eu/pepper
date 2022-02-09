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
package eu.solven.pepper.thread;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Some abstraction for data-structures doing asynchronous processes. Useful to collect them all and wait for the end of
 * all asynchronous processes.
 * 
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings("PMD.BooleanGetMethodName")
@ManagedResource
public interface IHasAsyncTasks {

	@Deprecated
	default boolean getHasPending() {
		return isHasPending();
	}

	/**
	 * 
	 * @return true if we have some pending tasks, maybe not already started
	 */
	@ManagedAttribute
	boolean isHasPending();

	@Deprecated
	default boolean getIsActive() {
		return isActive();
	}

	/**
	 * 
	 * @return true if this bean is active, synchronously or asynchronously
	 */
	@ManagedAttribute
	boolean isActive();
}
