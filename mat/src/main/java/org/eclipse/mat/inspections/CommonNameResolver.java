/**
 * The MIT License
 * Copyright (c) 2008-2017 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.inspections;

import java.lang.reflect.Modifier;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.extension.IClassSpecificNameResolver;
import org.eclipse.mat.snapshot.extension.Subject;
import org.eclipse.mat.snapshot.extension.Subjects;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.snapshot.model.PrettyPrinter;
import org.eclipse.mat.snapshot.registry.ClassSpecificNameResolverRegistry;

public class CommonNameResolver {
	@Subject("java.lang.String")
	public static class StringResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject obj) throws SnapshotException {
			return PrettyPrinter.objectAsString(obj, 1_024);
		}
	}

	@Subjects({ "java.lang.StringBuffer", //
			"java.lang.StringBuilder" })
	public static class StringBufferResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject obj) throws SnapshotException {
			Integer count = (Integer) obj.resolveValue("count");
			if (count == null)
				return null;
			if (count == 0)
				return "";

			IPrimitiveArray charArray = (IPrimitiveArray) obj.resolveValue("value");
			if (charArray == null)
				return null;

			if (charArray.getType() == IObject.Type.BYTE) {
				// Java 9 compact strings
				return PrettyPrinter.objectAsString(obj, 1_024);
			} else {
				return PrettyPrinter.arrayAsString(charArray, 0, count, 1_024);
			}
		}
	}

	@Subject("java.lang.Thread")
	public static class ThreadResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject obj) throws SnapshotException {
			IObject name = (IObject) obj.resolveValue("name");
			if (name != null) {
				return name.getClassSpecificName();
			} else {
				return null;
			}
		}
	}

	@Subject("java.lang.ThreadGroup")
	public static class ThreadGroupResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject object) throws SnapshotException {
			IObject nameString = (IObject) object.resolveValue("name");
			if (nameString == null)
				return null;
			return nameString.getClassSpecificName();
		}
	}

	@Subjects({ "java.lang.Byte", //
			"java.lang.Character", //
			"java.lang.Short", //
			"java.lang.Integer", //
			"java.lang.Long", //
			"java.lang.Float", //
			"java.lang.Double", //
			"java.lang.Boolean", //
			"java.util.concurrent.atomic.AtomicInteger", //
			"java.util.concurrent.atomic.AtomicLong", //
	})
	public static class ValueResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject heapObject) throws SnapshotException {
			Object value = heapObject.resolveValue("value");
			if (value != null) {
				return String.valueOf(value);
			} else {
				return null;
			}
		}
	}

	@Subjects("java.util.concurrent.atomic.AtomicBoolean")
	public static class AtomicBooleanResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject heapObject) throws SnapshotException {
			Integer value = (Integer) heapObject.resolveValue("value");
			if (value != null) {
				return Boolean.toString(value != 0);
			} else {
				return null;
			}
		}
	}

	@Subjects("java.util.concurrent.atomic.AtomicReference")
	public static class AtomicReferenceValueResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject heapObject) throws SnapshotException {
			IObject value = (IObject) heapObject.resolveValue("value");
			if (value != null) {
				return ClassSpecificNameResolverRegistry.resolve(value);
			} else {
				return null;
			}
		}
	}

	@Subjects("java.util.concurrent.atomic.AtomicStampedReference")
	public static class AtomicStampedReferenceValueResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject heapObject) throws SnapshotException {
			IObject value = (IObject) heapObject.resolveValue("pair.reference");
			if (value != null) {
				return ClassSpecificNameResolverRegistry.resolve(value);
			} else {
				return null;
			}
		}
	}

	@Subject("char[]")
	public static class CharArrayResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject heapObject) throws SnapshotException {
			IPrimitiveArray charArray = (IPrimitiveArray) heapObject;
			return PrettyPrinter.arrayAsString(charArray, 0, charArray.getLength(), 1_024);
		}
	}

	@Subject("byte[]")
	public static class ByteArrayResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject heapObject) throws SnapshotException {
			IPrimitiveArray arr = (IPrimitiveArray) heapObject;
			byte[] value = (byte[]) arr.getValueArray(0, Math.min(arr.getLength(), 1_024));
			if (value == null)
				return null;

			// must not modify the original byte array
			StringBuilder r = new StringBuilder(value.length);
			for (int i = 0; i < value.length; i++) {
				// ASCII/Unicode 127 is not printable
				if (value[i] < 32 || value[i] > 126)
					r.append('.');
				else
					r.append((char) value[i]);
			}
			return r.toString();
		}
	}

	/*
	 * Contributed in bug 273915
	 */
	@Subject("java.net.URL")
	public static class URLResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject obj) throws SnapshotException {
			StringBuilder builder = new StringBuilder();
			IObject protocol = (IObject) obj.resolveValue("protocol");
			if (protocol != null) {
				builder.append(protocol.getClassSpecificName());
				builder.append(":");
			}
			IObject authority = (IObject) obj.resolveValue("authority");
			if (authority != null) {
				builder.append("//");
				builder.append(authority.getClassSpecificName());
			}
			IObject path = (IObject) obj.resolveValue("path");
			if (path != null)
				builder.append(path.getClassSpecificName());
			IObject query = (IObject) obj.resolveValue("query");
			if (query != null) {
				builder.append("?");
				builder.append(query.getClassSpecificName());
			}
			IObject ref = (IObject) obj.resolveValue("ref");
			if (ref != null) {
				builder.append("#");
				builder.append(ref.getClassSpecificName());
			}
			if (builder.length() > 0) {
				return builder.toString();
			} else {
				return null;
			}
		}
	}

	@Subject("java.lang.reflect.AccessibleObject")
	public static class AccessibleObjectResolver implements IClassSpecificNameResolver {
		@Override
		public String resolve(IObject obj) throws SnapshotException {
			// Important fields
			// modifiers - not actually present, but present in all superclasses
			// clazz - not actually present, but present in all superclasses
			StringBuilder r = new StringBuilder();
			ISnapshot snapshot = obj.getSnapshot();
			IObject ref;
			Object val = obj.resolveValue("modifiers");
			if (val instanceof Integer) {
				r.append(Modifier.toString((Integer) val));
				if (r.length() > 0)
					r.append(' ');
			}
			ref = (IObject) obj.resolveValue("clazz");
			if (ref != null) {
				addClassName(snapshot, ref.getObjectAddress(), r);
			} else {
				return null;
			}
			return r.toString();
		}

		protected void addClassName(ISnapshot snapshot, long addr, StringBuilder r) throws SnapshotException {
			int id = snapshot.mapAddressToId(addr);
			IObject ox = snapshot.getObject(id);
			if (ox instanceof IClass) {
				IClass cls = (IClass) ox;
				r.append(cls.getName());
			}
		}
	}

	@Subject("java.lang.reflect.Field")
	public static class FieldResolver extends AccessibleObjectResolver {
		@Override
		public String resolve(IObject obj) throws SnapshotException {
			// Important fields
			// modifiers
			// clazz
			// name
			// type
			StringBuilder r = new StringBuilder();
			ISnapshot snapshot = obj.getSnapshot();
			IObject ref;
			Object val = obj.resolveValue("modifiers");
			if (val instanceof Integer) {
				r.append(Modifier.toString((Integer) val));
				if (r.length() > 0)
					r.append(' ');
			}
			ref = (IObject) obj.resolveValue("type");
			if (ref != null) {
				addClassName(snapshot, ref.getObjectAddress(), r);
				r.append(' ');
			}
			ref = (IObject) obj.resolveValue("clazz");
			if (ref != null) {
				addClassName(snapshot, ref.getObjectAddress(), r);
				r.append('.');
			}
			ref = (IObject) obj.resolveValue("name");
			if (ref != null) {
				r.append(ref.getClassSpecificName());
			} else {
				// No method name so give up
				return null;
			}
			return r.toString();
		}
	}

	@Subject("java.lang.reflect.Method")
	public static class MethodResolver extends AccessibleObjectResolver {
		@Override
		public String resolve(IObject obj) throws SnapshotException {
			// Important fields
			// modifiers
			// clazz
			// name
			// parameterTypes[]
			// exceptionTypes[]
			// returnType
			StringBuilder r = new StringBuilder();
			ISnapshot snapshot = obj.getSnapshot();
			IObject ref;
			Object val = obj.resolveValue("modifiers");
			if (val instanceof Integer) {
				r.append(Modifier.toString((Integer) val));
				if (r.length() > 0)
					r.append(' ');
			}
			ref = (IObject) obj.resolveValue("returnType");
			if (ref != null) {
				addClassName(snapshot, ref.getObjectAddress(), r);
				r.append(' ');
			}
			ref = (IObject) obj.resolveValue("clazz");
			if (ref != null) {
				addClassName(snapshot, ref.getObjectAddress(), r);
				r.append('.');
			}
			ref = (IObject) obj.resolveValue("name");
			if (ref != null) {
				r.append(ref.getClassSpecificName());
			} else {
				// No method name so give up
				return null;
			}
			r.append('(');
			ref = (IObject) obj.resolveValue("parameterTypes");
			if (ref instanceof IObjectArray) {
				IObjectArray orefa = (IObjectArray) ref;
				long refs[] = orefa.getReferenceArray();
				for (int i = 0; i < orefa.getLength(); ++i) {
					if (i > 0)
						r.append(',');
					long addr = refs[i];
					addClassName(snapshot, addr, r);
				}
			}
			r.append(')');
			return r.toString();
		}
	}

	@Subject("java.lang.reflect.Constructor")
	public static class ConstructorResolver extends AccessibleObjectResolver {
		@Override
		public String resolve(IObject obj) throws SnapshotException {
			// Important fields
			// modifiers
			// clazz
			// parameterTypes[]
			// exceptionTypes[]
			StringBuilder r = new StringBuilder();
			ISnapshot snapshot = obj.getSnapshot();
			IObject ref;
			Object val = obj.resolveValue("modifiers");
			if (val instanceof Integer) {
				r.append(Modifier.toString((Integer) val));
				if (r.length() > 0)
					r.append(' ');
			}
			ref = (IObject) obj.resolveValue("clazz");
			if (ref != null) {
				addClassName(snapshot, ref.getObjectAddress(), r);
			} else {
				// No class name so give up
				return null;
			}
			r.append('(');
			ref = (IObject) obj.resolveValue("parameterTypes");
			if (ref instanceof IObjectArray) {
				IObjectArray orefa = (IObjectArray) ref;
				long refs[] = orefa.getReferenceArray();
				for (int i = 0; i < orefa.getLength(); ++i) {
					if (i > 0)
						r.append(',');
					long addr = refs[i];
					addClassName(snapshot, addr, r);
				}
			}
			r.append(')');
			return r.toString();
		}
	}
}
