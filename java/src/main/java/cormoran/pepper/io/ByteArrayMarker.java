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
package cormoran.pepper.io;

import java.io.Serializable;

/**
 * Enable to mark an ObjectOutputStream than what is following is a byte[]. This way, it would be streamed, instead of
 * materialize in memory
 * 
 * @author Benoit Lacelle
 *
 */
public class ByteArrayMarker implements Serializable {
	private static final long serialVersionUID = -3032117473402808084L;

	protected final long nbBytes;
	protected final boolean isfinished;

	public ByteArrayMarker(long nbBytes, boolean isfinished) {
		this.nbBytes = nbBytes;
		this.isfinished = isfinished;
	}

	public long getNbBytes() {
		return nbBytes;
	}

	public boolean getIsFinished() {
		return isfinished;
	}
}
