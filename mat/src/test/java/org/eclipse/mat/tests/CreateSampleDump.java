/**
 * The MIT License
 * Copyright (c) 2008 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.tests;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CreateSampleDump {

	public static void main(String[] args) throws Exception {
		DominatorTestData dominatorTestData = new DominatorTestData();

		ReferenceTestData referenceTestData = new ReferenceTestData();

		System.out.println("Acquire Heap Dump NOW (then press any key to terminate program)");
		int c = System.in.read();
		// Control-break causes read to return early, so try again for another key to wait
		// for the dump to complete
		if (c == -1)
			c = System.in.read();

		System.out.println(dominatorTestData);
		System.out.println(referenceTestData);
	}

	// //////////////////////////////////////////////////////////////
	// dominator tree
	// //////////////////////////////////////////////////////////////

	static class DominatorTestData {
		R r;

		DominatorTestData() {
			// Create Lengauer & Tarjan Paper Tree
			A a = new A();
			B b = new B();
			C c = new C();
			D d = new D();
			E e = new E();
			F f = new F();
			G g = new G();
			H h = new H();
			I i = new I();
			J j = new J();
			K k = new K();
			L l = new L();
			r = new R();
			a.d = d;
			b.a = a;
			b.d = d;
			b.e = e;
			c.f = f;
			c.g = g;
			d.l = l;
			e.h = h;
			f.i = i;
			g.i = i;
			g.j = j;
			h.e = e;
			h.k = k;
			i.k = k;
			j.i = i;
			k.i = i;
			k.r = r;
			l.h = h;
			r.a = a;
			r.b = b;
			r.c = c;
		}

		static class A {
			D d;
		}

		static class B {
			A a;
			D d;
			E e;
		}

		static class C {
			F f;
			G g;
		}

		static class D {
			L l;
		}

		static class E {
			H h;
		}

		static class F {
			I i;
		}

		static class G {
			I i;
			J j;
		}

		static class H {
			E e;
			K k;
		}

		static class I {
			K k;
		}

		static class J {
			I i;
		}

		static class K {
			I i;
			R r;
		}

		static class L {
			H h;
		}

		static class R {
			A a;
			B b;
			C c;
		}
	}

	// //////////////////////////////////////////////////////////////
	// paths tests
	// //////////////////////////////////////////////////////////////

	static class ReferenceTestData {
		SoftReference<?> clearedSoftReference;
		SoftReference<?> availableSoftReference;
		WeakReference<?> clearedWeakReference;
		WeakReference<?> availableWeakReference;
		String keptWeakReference;

		ReferenceTestData() {
			// Soft reference
			int size = (int) Runtime.getRuntime().totalMemory();
			byte[] lostSoftReference = null;
			while (lostSoftReference == null) {
				size -= (1 << 20);
				try {
					lostSoftReference = new byte[size];
				} catch (OutOfMemoryError ignore) {
				}
			}
			clearedSoftReference = new SoftReference<byte[]>(lostSoftReference);

			lostSoftReference = null;
			ArrayList<byte[]> garbage = new ArrayList<byte[]>();
			while (clearedSoftReference.get() != null) {
				try {
					garbage.add(new byte[size - (1 << 10)]);
				} catch (OutOfMemoryError ignore) {
					// $JL-EXC$
				}
			}

			availableSoftReference = new SoftReference<String>(new String("availableSoftReference"));

			// Weak reference
			String lostWeakReference = new String("clearedWeakReference");

			clearedWeakReference = new WeakReference<String>(lostWeakReference);

			keptWeakReference = new String("keptWeakReference");
			availableWeakReference = new WeakReference<String>(keptWeakReference);
		}
	}
}
