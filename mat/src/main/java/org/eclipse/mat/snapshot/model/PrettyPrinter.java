/*******************************************************************************
 * Copyright (c) 2008, 2023 SAP AG, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - additional debug information
 *    Netflix (Jason Koch) - refactors for increased performance and concurrency
 *******************************************************************************/
package org.eclipse.mat.snapshot.model;

import java.util.Collection;

import org.eclipse.mat.SnapshotException;

/**
 * Utility class to extract String representations of heap dump objects.
 */
public final class PrettyPrinter {
	/**
	 * Convert a <code>java.lang.String</code> object into a String.
	 *
	 * @param stringObject
	 *            the String object in the dump
	 * @param limit
	 *            maximum number of characters to return
	 * @return the value of the string from the dump, as a String
	 * @throws SnapshotException
	 */
	public static String objectAsString(IObject stringObject, int limit) throws SnapshotException {
		Object valueObj = stringObject.resolveValue("value");
		if (!(valueObj instanceof IPrimitiveArray))
			return null;
		IPrimitiveArray charArray = (IPrimitiveArray) valueObj;

		Object countObj = stringObject.resolveValue("count");
		// count and offset fields were removed with JDK7u6
		if (countObj == null) {
			if (charArray.getType() == IObject.Type.BYTE) {
				// Java 9 compact strings
				Object coder = stringObject.resolveValue("coder"); //$NON-NLS-1$
				if (coder instanceof Byte) {
					if (((Byte) coder).byteValue() == 0) {
						return compactByteArrayAsString(charArray, 0, charArray.getLength(), limit);
					}
				}
				Collection<IClass> clss = stringObject.getSnapshot().getClassesByName("java.lang.StringUTF16", false);//$NON-NLS-1$
				int bigEndian;
				if (!clss.isEmpty()) {
					Object o = clss.iterator().next().resolveValue("HI_BYTE_SHIFT"); //$NON-NLS-1$
					bigEndian = o instanceof Integer && (Integer) o == 8 ? 1 : 0;
				} else {
					bigEndian = 0;
				}
				return byteArrayAsString(charArray, 0, charArray.getLength() / 2, limit, bigEndian);
			} else {
				return arrayAsString(charArray, 0, charArray.getLength(), limit);
			}
		} else {
			if (!(countObj instanceof Integer))
				return null;
			Integer count = (Integer) countObj;
			if (count.intValue() == 0)
				return "";

			// IBM java.lang.String implementation may have count but not offset
			Integer offset = 0;
			Object offsetObj = stringObject.resolveValue("offset");
			if (offsetObj instanceof Integer) {
				offset = (Integer) offsetObj;
			}

			if (charArray.getType() == IObject.Type.BYTE) {
				// Java 9 compact strings
				Object coder = stringObject.resolveValue("coder"); //$NON-NLS-1$
				if (coder instanceof Byte) {
					if (((Byte) coder).byteValue() == 0) {
						return compactByteArrayAsString(charArray, offset, count, limit);
					}
				}
				Object o = stringObject.getSnapshot()
						.getClassesByName("java.lang.String", false) //$NON-NLS-1$
						.iterator()
						.next()
						.resolveValue("HI_BYTE_SHIFT"); //$NON-NLS-1$
				int bigEndian;
				if (o instanceof Integer && (Integer) o == 8) {
					bigEndian = 1;
				} else {
					bigEndian = 0;
				}
				return byteArrayAsString(charArray, offset, count, limit, bigEndian);
			} else {
				return arrayAsString(charArray, offset, count, limit);
			}
		}
	}

	/**
	 * Convert a <code>char[]</code> object into a String. Unprintable characters are returned as \\unnnn values
	 *
	 * @param charArray
	 * @param offset
	 *            where to start
	 * @param count
	 *            how many characters to read
	 * @param limit
	 *            the maximum number of characters to read
	 * @return the characters as a string
	 */
	public static String arrayAsString(IPrimitiveArray charArray, int offset, int count, int limit) {
		if (charArray.getType() != IObject.Type.CHAR)
			return null;

		int length = charArray.getLength();
		int contentToRead;

		if (count <= limit) {
			contentToRead = count;
		} else {
			contentToRead = limit;
		}
		if (contentToRead > length - offset)
			contentToRead = length - offset;

		char[] value;
		if (offset == 0 && length == contentToRead)
			value = (char[]) charArray.getValueArray();
		else
			value = (char[]) charArray.getValueArray(offset, contentToRead);

		if (value == null)
			return null;

		StringBuilder result = new StringBuilder(value.length);
		for (int ii = 0; ii < value.length; ii++) {
			char val = value[ii];
			if (val >= 32 && val < 127)
				result.append(val);
			else
				result.append("\\u").append(String.format("%04x", 0xFFFF & val));
		}
		if (limit < count)
			result.append("...");
		return result.toString();
	}

	/**
	 * Convert a <code>byte[]</code> object into a String. Values are ISO8859_1 Unprintable characters are returned as
	 * \\unnnn values
	 *
	 * @param byteArray
	 * @param offset
	 *            where to start
	 * @param count
	 *            how many characters to read
	 * @param limit
	 *            the maximum number of characters to read
	 * @return the characters as a string
	 */
	static String compactByteArrayAsString(IPrimitiveArray byteArray, int offset, int count, int limit) {
		if (byteArray.getType() != IObject.Type.BYTE)
			return null;

		int length = byteArray.getLength();
		int contentToRead;

		if (count <= limit) {
			contentToRead = count;
		} else {
			contentToRead = limit;
		}
		if (contentToRead > length - offset)
			contentToRead = length - offset;

		byte[] value;
		if (offset == 0 && length == contentToRead)
			value = (byte[]) byteArray.getValueArray();
		else
			value = (byte[]) byteArray.getValueArray(offset, contentToRead);

		if (value == null)
			return null;

		StringBuilder result = new StringBuilder(value.length);
		for (int ii = 0; ii < value.length; ii++) {
			byte val = value[ii];
			if (val >= 32 && val < 127)
				result.append((char) val);
			else
				result.append("\\u").append(String.format("%04x", 0xFFFF & val)); //$NON-NLS-1$//$NON-NLS-2$
		}
		if (limit < count)
			result.append("..."); //$NON-NLS-1$
		return result.toString();
	}

	/**
	 * Convert a <code>byte[]</code> object into a String. Values are UTF-16, low byte, high byte. Unprintable
	 * characters are returned as \\unnnn values
	 *
	 * @param byteArray
	 * @param offset
	 *            where to start
	 * @param count
	 *            how many characters to read
	 * @param limit
	 *            the maximum number of characters to read
	 * @return the characters as a string
	 */
	static String byteArrayAsString(IPrimitiveArray byteArray, int offset, int count, int limit, int bigEndian) {
		if (byteArray.getType() != IObject.Type.BYTE)
			return null;

		int length = byteArray.getLength();
		int contentToRead;

		if (count <= limit) {
			contentToRead = count * 2;
		} else {
			contentToRead = limit * 2;
		}
		if (contentToRead > length - offset * 2)
			contentToRead = length - offset * 2;

		byte[] value;
		if (offset == 0 && length == contentToRead)
			value = (byte[]) byteArray.getValueArray();
		else
			value = (byte[]) byteArray.getValueArray(offset, contentToRead);

		if (value == null)
			return null;

		// Round up for possible last byte
		StringBuilder result = new StringBuilder(value.length / 2);
		for (int ii = 0; ii < value.length; ii += 2) {
			byte val = value[ii + bigEndian];
			// Last high byte might not be there
			byte val2 = value[ii + 1 - bigEndian];
			if (val >= 32 && val < 127 && val2 == 0)
				result.append((char) val);
			else
				result.append("\\u").append(String.format("%04x", 0xFF & val | (0xFF & val2) << 8)); //$NON-NLS-1$//$NON-NLS-2$
		}
		if (limit < count)
			result.append("..."); //$NON-NLS-1$
		return result.toString();
	}

	private PrettyPrinter() {
	}
}
