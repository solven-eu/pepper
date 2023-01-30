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
package org.eclipse.mat.parser.internal.snapshot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.mat.collect.HashMapIntObject;

abstract public class ObjectCache<E> {
	static class Entry<E> {
		E object;
		int key;
		int numUsages;
	}

	private int maxSize;
	private final HashMapIntObject<Entry<E>> map;
	private final List<LinkedList<Entry<E>>> lfus;
	private int maxLfuBuckets = 0;
	private int lowestNonEmptyLfu = 0;

	public ObjectCache(int maxSize) {
		this.maxSize = maxSize;
		this.map = new HashMapIntObject<Entry<E>>(maxSize);
		this.lfus = new ArrayList<LinkedList<Entry<E>>>(5);
		this.maxLfuBuckets = maxSize / 3;
	}

	public synchronized E get(int objectId) {
		Entry<E> e = map.get(objectId);
		if (e != null) {
			revalueEntry(e);
		} else {
			e = new Entry<E>();
			e.object = load(objectId);
			e.key = objectId;

			doInsert(e);

			while (map.size() > maxSize)
				removeLeastValuableNode();
		}

		return e.object;
	}

	public synchronized void clear() {
		this.map.clear();
		this.lfus.clear();
	}

	protected abstract E load(int key);

	protected synchronized void doInsert(final Entry<E> e) {
		lfu(e.numUsages).addFirst(e);
		Entry<?> p = map.put(e.key, e);
		lowestNonEmptyLfu = 0;

		if (p != null)
			lfu(p.numUsages).remove(p);
	}

	protected final LinkedList<Entry<E>> lfu(int numUsageIndex) {
		int lfuIndex = Math.min(maxLfuBuckets, numUsageIndex);

		if (lfuIndex >= lfus.size()) {
			LinkedList<Entry<E>> lfu = new LinkedList<Entry<E>>();
			lfus.add(lfuIndex, lfu);
			return lfu;
		} else {
			return lfus.get(lfuIndex);
		}
	}

	protected void revalueEntry(Entry<E> entry) {
		LinkedList<Entry<E>> currBucket = lfu(entry.numUsages);
		LinkedList<Entry<E>> nextBucket = lfu(++entry.numUsages);

		currBucket.remove(entry);
		nextBucket.addFirst(entry);
	}

	protected LinkedList<Entry<E>> getLowestNonEmptyLfu() {
		LinkedList<Entry<E>> lfu = null;
		for (int i = lowestNonEmptyLfu; i < lfus.size(); i++) {
			lfu = lfu(i);

			if (lfu.size() != 0) {
				lowestNonEmptyLfu = i;
				return lfu;
			}
		}
		return lfu;
	}

	protected void removeLeastValuableNode() {
		LinkedList<Entry<E>> lfu = getLowestNonEmptyLfu();
		Entry<?> lln = lfu.remove(lfu.size() - 1);
		map.remove(lln.key);
	}

}
