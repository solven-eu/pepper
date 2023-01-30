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
package org.eclipse.mat.snapshot.model;

/**
 * Interface for primitive arrays in the heap dump.
 *
 * @noimplement
 */
public interface IPrimitiveArray extends IArray {
	/**
	 * Primitive signatures. Indexes match the values of {@link IObject.Type}
	 *
	 * @see IObject.Type
	 */
	byte[] SIGNATURES = { -1,
			-1,
			-1,
			-1,
			(byte) 'Z',
			(byte) 'C',
			(byte) 'F',
			(byte) 'D',
			(byte) 'B',
			(byte) 'S',
			(byte) 'I',
			(byte) 'J' };

	/**
	 * Element sizes inside the array. Indexes match the values of {@link IObject.Type}
	 *
	 * @see IObject.Type
	 */
	int[] ELEMENT_SIZE = { -1, -1, -1, -1, 1, 2, 4, 8, 1, 2, 4, 8 };

	/**
	 * Display string of the type. Indexes match the values of {@link IObject.Type}
	 *
	 * @see IObject.Type
	 */
	String[] TYPE = { null,
			null,
			null,
			null,
			"boolean[]",
			"char[]",
			"float[]",
			"double[]",
			"byte[]",
			"short[]",
			"int[]",
			"long[]" };

	/**
	 * Java component type of the primitive array. Indexes match the values of {link IObject.Type}
	 *
	 * @see IObject.Type
	 */
	Class<?>[] COMPONENT_TYPE = { null,
			null,
			null,
			null,
			boolean.class,
			char.class,
			float.class,
			double.class,
			byte.class,
			short.class,
			int.class,
			long.class };

	/**
	 * Returns the {@link IObject.Type} of the primitive array.
	 */
	int getType();

	/**
	 * Returns the component type of the array.
	 */
	Class<?> getComponentType();

	/**
	 * Returns the value of the array at the specified index
	 *
	 * @param index
	 *            from 0 to length-1
	 * @return Byte - for a byte array Short - for a short array Integer - for an int array Long - for a long array
	 *         Boolean - for a boolean array Char - for a char array Float - for a float array Double - for a double
	 *         array
	 */
	Object getValueAt(int index);

	/**
	 * Get the primitive Java array. The return value can be cast into the correct component type, e.g.
	 *
	 * <pre>
	 * if (char.class == array.getComponentType()) {
	 * 	char[] content = (char[]) array.getValueArray();
	 * 	System.out.println(content.length);
	 * }
	 * </pre>
	 *
	 * The return value must not be modified because it is cached by the heap dump adapter. This method does not return
	 * a copy of the array for performance reasons.
	 */
	Object getValueArray();

	/**
	 * Get the primitive Java array, beginning at <code>offset</code> and <code>length</code> number of elements.
	 * <p>
	 * The return value must not be modified because it is cached by the heap dump adapter. This method does not return
	 * a copy of the array for performance reasons.
	 */
	Object getValueArray(int offset, int length);
}
