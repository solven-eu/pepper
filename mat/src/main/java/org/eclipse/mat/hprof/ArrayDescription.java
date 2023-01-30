/**
 * The MIT License
 * Copyright (c) 2008-2010 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.hprof;

import java.lang.ref.SoftReference;

/* package */class ArrayDescription {
	/* package */static class Offline extends ArrayDescription {
		boolean isPrimitive;
		long position;
		int arraySize;
		int elementSize;

		SoftReference<Object> lazyReadContent = new SoftReference<Object>(null);

		public Offline(boolean isPrimitive, long position, int elementSize, int arraySize) {
			this.isPrimitive = isPrimitive;
			this.position = position;
			this.elementSize = elementSize;
			this.arraySize = arraySize;
		}

		public boolean isPrimitive() {
			return isPrimitive;
		}

		public long getPosition() {
			return position;
		}

		public int getArraySize() {
			return arraySize;
		}

		public int getElementSize() {
			return elementSize;
		}

		public Object getLazyReadContent() {
			return lazyReadContent.get();
		}

		public void setLazyReadContent(Object content) {
			this.lazyReadContent = new SoftReference<Object>(content);
		}

	}

	/* package */static class Raw extends ArrayDescription {
		byte[] content;

		public Raw(byte[] content) {
			this.content = content;
		}

		public byte[] getContent() {
			return content;
		}
	}
}
