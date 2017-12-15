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
package cormoran.pepper.thread;

/**
 * Producing a Throwable is the fastest way to retrieve current thread stack.
 * 
 * 'new Exception().getStackTrace()' is much faster than 'Thread.currentThread().getStackTrace()'
 * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6375302
 * 
 * We use a name which does not refer to Exception as this is NOT an exception. it should be used to produce logs
 * referring to current thread stack.
 * 
 * We extends {@link Throwable} as we do no expect to use this for rethrowing
 * 
 * @author Benoit Lacelle
 *
 */
public class CurrentThreadStack extends Throwable {
	private static final long serialVersionUID = -4426770891772850366L;

	public static CurrentThreadStack snapshot() {
		return new CurrentThreadStack();
	}

	public static StackTraceElement[] snapshotStackTrace() {
		return snapshot().getStackTrace();
	}

}
