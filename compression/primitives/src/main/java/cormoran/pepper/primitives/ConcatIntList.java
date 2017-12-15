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
package cormoran.pepper.primitives;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Wraps multiple {@link IntList} in a single concatenated {@link IntList}
 * 
 * @author Benoit Lacelle
 *
 */
public class ConcatIntList extends AbstractIntList {

	protected final List<IntList> intLists;

	public ConcatIntList(IntList... intLists) {
		this(Arrays.asList(intLists));
	}

	public ConcatIntList(List<IntList> intLists) {
		this.intLists = intLists;
	}

	@Override
	public int getInt(final int index) {
		Iterator<IntList> it = intLists.iterator();

		int previousSize = 0;

		while (it.hasNext()) {
			IntList next = it.next();
			int size = next.size();

			if (index >= previousSize + size) {
				previousSize += size;
				continue;
			} else {
				return next.getInt(index - previousSize);
			}
		}

		throw new ArrayIndexOutOfBoundsException("index=" + index + " while size=" + size());
	}

	@Override
	public int size() {
		return intLists.stream().mapToInt(IntList::size).sum();
	}

}
