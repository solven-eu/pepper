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
package org.eclipse.mat.hprof;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.hprof.extension.IRuntimeEnhancer;
import org.eclipse.mat.hprof.ui.HprofPreferences;
import org.eclipse.mat.parser.IObjectReader;
import org.eclipse.mat.parser.index.IIndexReader;
import org.eclipse.mat.parser.index.IndexReader;
import org.eclipse.mat.parser.model.AbstractArrayImpl;
import org.eclipse.mat.parser.model.ObjectArrayImpl;
import org.eclipse.mat.parser.model.PrimitiveArrayImpl;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HprofHeapObjectReader implements IObjectReader {

	protected static final Logger LOGGER = LoggerFactory.getLogger(HprofHeapObjectReader.class);

	public static final String VERSION_PROPERTY = "hprof.version";

	private ISnapshot snapshot;
	private HprofRandomAccessParser hprofDump;
	private IIndexReader.IOne2LongIndex o2hprof;
	private List<IRuntimeEnhancer> enhancers;

	@Override
	public void open(ISnapshot snapshot) throws IOException {
		this.snapshot = snapshot;

		AbstractParser.Version version =
				AbstractParser.Version.valueOf((String) snapshot.getSnapshotInfo().getProperty(VERSION_PROPERTY));

		HprofPreferences.HprofStrictness strictnessPreference = HprofPreferences.getCurrentStrictness();

		this.hprofDump = new HprofRandomAccessParser(new File(snapshot.getSnapshotInfo().getPath()), //
				version, //
				snapshot.getSnapshotInfo().getIdentifierSize(),
				strictnessPreference);
		this.o2hprof =
				new IndexReader.LongIndexReader(new File(snapshot.getSnapshotInfo().getPrefix() + "o2hprof.index"));

		this.enhancers = new ArrayList<IRuntimeEnhancer>();
		// for (EnhancerRegistry.Enhancer enhancer : EnhancerRegistry.instance().delegates())
		// {
		// IRuntimeEnhancer runtime = enhancer.runtime();
		// if (runtime != null)
		// this.enhancers.add(runtime);
		// }
	}

	@Override
	public long[] readObjectArrayContent(ObjectArrayImpl array, int offset, int length)
			throws IOException, SnapshotException {
		Object info = array.getInfo();

		if (info instanceof ArrayDescription.Offline) {
			ArrayDescription.Offline description = (ArrayDescription.Offline) info;

			long[] answer = (long[]) description.getLazyReadContent();
			if (answer == null) {
				answer = hprofDump.readObjectArray(description, offset, length);

				// save content if fully read...
				if (offset == 0 && length == array.getLength())
					description.setLazyReadContent(answer);

				return answer;
			} else {
				return (long[]) fragment(array, answer, offset, length);
			}
		} else if (info instanceof long[]) {
			return (long[]) fragment(array, info, offset, length);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Object readPrimitiveArrayContent(PrimitiveArrayImpl array, int offset, int length)
			throws IOException, SnapshotException {
		Object info = array.getInfo();

		if (info instanceof ArrayDescription.Offline) {
			ArrayDescription.Offline description = (ArrayDescription.Offline) info;

			Object content = description.getLazyReadContent();
			if (content == null) {
				content = convert(array, hprofDump.readPrimitiveArray(description, offset, length));

				// save content if fully read...
				if (offset == 0 && length == array.getLength())
					description.setLazyReadContent(content);

				return content;
			} else {
				return fragment(array, content, offset, length);
			}
		} else if (info instanceof ArrayDescription.Raw) {
			ArrayDescription.Raw description = (ArrayDescription.Raw) info;
			Object content = convert(array, description.getContent());
			array.setInfo(content);

			return fragment(array, content, offset, length);
		} else {
			return fragment(array, info, offset, length);
		}
	}

	private Object convert(PrimitiveArrayImpl array, byte[] content) {
		if (array.getType() == IObject.Type.BYTE)
			return content;

		int elementSize = IPrimitiveArray.ELEMENT_SIZE[array.getType()];
		int length = content.length / elementSize;

		Object answer = Array.newInstance(IPrimitiveArray.COMPONENT_TYPE[array.getType()], length);

		int index = 0;
		for (int ii = 0; ii < content.length; ii += elementSize) {
			switch (array.getType()) {
			case IObject.Type.BOOLEAN:
				Array.set(answer, index, content[ii] != 0);
				break;
			case IObject.Type.CHAR:
				Array.set(answer, index, readChar(content, ii));
				break;
			case IObject.Type.FLOAT:
				Array.set(answer, index, readFloat(content, ii));
				break;
			case IObject.Type.DOUBLE:
				Array.set(answer, index, readDouble(content, ii));
				break;
			case IObject.Type.SHORT:
				Array.set(answer, index, readShort(content, ii));
				break;
			case IObject.Type.INT:
				Array.set(answer, index, readInt(content, ii));
				break;
			case IObject.Type.LONG:
				Array.set(answer, index, readLong(content, ii));
				break;
			}

			index++;
		}

		return answer;
	}

	private Object fragment(AbstractArrayImpl array, Object content, int offset, int length) {
		if (offset == 0 && length == array.getLength())
			return content;

		Object answer = Array.newInstance(content.getClass().getComponentType(), length);
		System.arraycopy(content, offset, answer, 0, length);
		return answer;
	}

	@Override
	public IObject read(int objectId, ISnapshot snapshot) throws SnapshotException, IOException {
		try {
			long filePosition = o2hprof.get(objectId);
			return hprofDump.read(objectId, filePosition, snapshot);
		} catch (SnapshotException e) {
			throw new SnapshotException(
					"Issue on objectId=" + objectId
							+ " snapshot="
							+ snapshot.getSnapshotInfo().getPath()
							+ " o2hprof="
							+ o2hprof
							+ " o2hprof.size()="
							+ o2hprof.size()
							+ " allIndexes="
							+ Arrays.toString(o2hprof.getNext(0, o2hprof.size())),
					e);
		}
	}

	/**
	 * Returns extra data to be provided by {@link ISnapshot#getSnapshotAddons(Class addon)}. Also can be returned via
	 * {@link org.eclipse.mat.query.annotations.Argument}.
	 *
	 * @see org.eclipse.mat.parser.IObjectReader#getAddon(Class)
	 * @param addon
	 *            the type of the extra data required from the dump. HprofHeapObjectReader can be extended using an
	 *            {@link IRuntimeEnhancer} extension to return extra data.
	 * @return the extra data
	 */
	@Override
	public <A> A getAddon(Class<A> addon) throws SnapshotException {
		for (IRuntimeEnhancer enhancer : enhancers) {
			A answer = enhancer.getAddon(snapshot, addon);
			if (answer != null)
				return answer;
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		try {
			hprofDump.close();
		} catch (IOException ignore) {
			LOGGER.trace("Ouch", ignore);
		}

		try {
			o2hprof.close();
		} catch (IOException ignore) {
			LOGGER.trace("Ouch", ignore);
		}
	}

	// //////////////////////////////////////////////////////////////
	// conversion routines
	// //////////////////////////////////////////////////////////////

	private short readShort(byte[] data, int offset) {
		int b1 = data[offset] & 0xff;
		int b2 = data[offset + 1] & 0xff;
		return (short) ((b1 << 8) + b2);
	}

	private char readChar(byte[] data, int offset) {
		int b1 = data[offset] & 0xff;
		int b2 = data[offset + 1] & 0xff;
		return (char) ((b1 << 8) + b2);
	}

	private int readInt(byte[] data, int offset) {
		int ch1 = data[offset] & 0xff;
		int ch2 = data[offset + 1] & 0xff;
		int ch3 = data[offset + 2] & 0xff;
		int ch4 = data[offset + 3] & 0xff;
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	private float readFloat(byte[] data, int offset) {
		return Float.intBitsToFloat(readInt(data, offset));
	}

	private long readLong(byte[] data, int offset) {
		return ((((long) data[offset] & 0xff) << 56) + //
				((long) (data[offset + 1] & 0xff) << 48) + //
				((long) (data[offset + 2] & 0xff) << 40) + //
				((long) (data[offset + 3] & 0xff) << 32) + //
				((long) (data[offset + 4] & 0xff) << 24) + //
				((data[offset + 5] & 0xff) << 16) + //
				((data[offset + 6] & 0xff) << 8) + //
				((data[offset + 7] & 0xff) << 0));
	}

	private double readDouble(byte[] data, int offset) {
		return Double.longBitsToDouble(readLong(data, offset));
	}

}
