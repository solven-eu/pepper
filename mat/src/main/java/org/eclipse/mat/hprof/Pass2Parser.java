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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.hprof.IHprofParserHandler.HeapObject;
import org.eclipse.mat.hprof.ui.HprofPreferences;
import org.eclipse.mat.hprof.ui.HprofPreferences.HprofStrictness;
import org.eclipse.mat.parser.io.PositionInputStream;
import org.eclipse.mat.parser.model.ClassImpl;
import org.eclipse.mat.snapshot.model.FieldDescriptor;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.IProgressListener.Severity;
import org.eclipse.mat.util.MessageUtil;
import org.eclipse.mat.util.SimpleMonitor;

/**
 * Parser used to read the hprof formatted heap dump
 */

public class Pass2Parser extends AbstractParser {
	private IHprofParserHandler handler;
	private SimpleMonitor.Listener monitor;

	public Pass2Parser(IHprofParserHandler handler,
			SimpleMonitor.Listener monitor,
			HprofPreferences.HprofStrictness strictnessPreference) {
		super(strictnessPreference);
		this.handler = handler;
		this.monitor = monitor;
	}

	public void read(File file, String dumpNrToRead) throws SnapshotException, IOException {
		in = new PositionInputStream(new BufferedInputStream(new FileInputStream(file)));

		int currentDumpNr = 0;

		try {
			version = readVersion(in);
			idSize = in.readInt();
			if (idSize != 4 && idSize != 8)
				throw new SnapshotException(Messages.Pass1Parser_Error_SupportedDumps);
			in.skipBytes(8); // creation date

			long fileSize = file.length();
			long curPos = in.position();

			while (curPos < fileSize) {
				if (monitor.isProbablyCanceled())
					throw new IProgressListener.OperationCanceledException();
				monitor.totalWorkDone(curPos / 1000);

				int record = in.readUnsignedByte();

				in.skipBytes(4); // time stamp

				long length = readUnsignedInt();
				if (length < 0)
					throw new SnapshotException(MessageUtil
							.format(Messages.Pass1Parser_Error_IllegalRecordLength, length, in.position(), record));

				length = updateLengthIfNecessary(fileSize, curPos, record, length, monitor);

				switch (record) {
				case Constants.Record.HEAP_DUMP:
				case Constants.Record.HEAP_DUMP_SEGMENT:
					if (dumpMatches(currentDumpNr, dumpNrToRead))
						readDumpSegments(length);
					else
						in.skipBytes(length);

					if (record == Constants.Record.HEAP_DUMP)
						currentDumpNr++;

					break;
				case Constants.Record.HEAP_DUMP_END:
					currentDumpNr++;
					in.skipBytes(length);
					break;
				default:
					in.skipBytes(length);
					break;
				}

				curPos = in.position();
			}
		} finally {
			try {
				in.close();
			} catch (IOException ignore) {
			}
		}
	}

	private void readDumpSegments(long length) throws SnapshotException, IOException {
		long segmentStartPos = in.position();
		long segmentsEndPos = segmentStartPos + length;

		while (segmentStartPos < segmentsEndPos) {
			long workDone = segmentStartPos / 1000;
			if (this.monitor.getWorkDone() < workDone) {
				if (this.monitor.isProbablyCanceled())
					throw new IProgressListener.OperationCanceledException();
				this.monitor.totalWorkDone(workDone);
			}

			int segmentType = in.readUnsignedByte();
			switch (segmentType) {
			case Constants.DumpSegment.ROOT_UNKNOWN:
			case Constants.DumpSegment.ROOT_STICKY_CLASS:
			case Constants.DumpSegment.ROOT_MONITOR_USED:
				in.skipBytes(idSize);
				break;
			case Constants.DumpSegment.ROOT_JNI_GLOBAL:
				in.skipBytes(idSize * 2);
				break;
			case Constants.DumpSegment.ROOT_NATIVE_STACK:
			case Constants.DumpSegment.ROOT_THREAD_BLOCK:
				in.skipBytes(idSize + 4);
				break;
			case Constants.DumpSegment.ROOT_THREAD_OBJECT:
			case Constants.DumpSegment.ROOT_JNI_LOCAL:
			case Constants.DumpSegment.ROOT_JAVA_FRAME:
				in.skipBytes(idSize + 8);
				break;
			case Constants.DumpSegment.CLASS_DUMP:
				skipClassDump();
				break;
			case Constants.DumpSegment.INSTANCE_DUMP:
				readInstanceDump(segmentStartPos);
				break;
			case Constants.DumpSegment.OBJECT_ARRAY_DUMP:
				readObjectArrayDump(segmentStartPos);
				break;
			case Constants.DumpSegment.PRIMITIVE_ARRAY_DUMP:
				readPrimitiveArrayDump(segmentStartPos);
				break;
			default:
				throw new SnapshotException(MessageUtil
						.format(Messages.Pass1Parser_Error_InvalidHeapDumpFile, segmentType, segmentStartPos));
			}
			segmentStartPos = in.position();
		}
	}

	private void skipClassDump() throws IOException {
		in.skipBytes(7 * idSize + 8);

		int constantPoolSize = in.readUnsignedShort();
		for (int ii = 0; ii < constantPoolSize; ii++) {
			in.skipBytes(2);
			skipValue();
		}

		int numStaticFields = in.readUnsignedShort();
		for (int i = 0; i < numStaticFields; i++) {
			in.skipBytes(idSize);
			skipValue();
		}

		int numInstanceFields = in.readUnsignedShort();
		in.skipBytes((idSize + 1) * numInstanceFields);
	}

	private void readInstanceDump(long segmentStartPos) throws IOException {
		long id = readID();
		in.skipBytes(4);
		long classID = readID();
		int bytesFollowing = in.readInt();
		long endPos = in.position() + bytesFollowing;

		List<IClass> hierarchy = handler.resolveClassHierarchy(classID);

		ClassImpl thisClazz = (ClassImpl) hierarchy.get(0);
		HeapObject heapObject =
				new HeapObject(handler.mapAddressToId(id), id, thisClazz, thisClazz.getHeapSizePerInstance());

		heapObject.references.add(thisClazz.getObjectAddress());

		// extract outgoing references
		for (IClass clazz : hierarchy) {
			for (FieldDescriptor field : clazz.getFieldDescriptors()) {
				int type = field.getType();
				if (type == IObject.Type.OBJECT) {
					long refId = readID();
					if (refId != 0)
						heapObject.references.add(refId);
				} else {
					skipValue(type);
				}
			}
		}

		if (endPos != in.position()) {
			boolean unknown = false;
			for (IClass clazz : hierarchy) {
				if (clazz.getName().startsWith("unknown-class")) {
					unknown = true;
				}
			}

			if (endPos >= in.position() && unknown
					&& (strictnessPreference == HprofStrictness.STRICTNESS_WARNING
							|| strictnessPreference == HprofStrictness.STRICTNESS_PERMISSIVE)) {
				monitor.sendUserMessage(Severity.WARNING,
						MessageUtil.format(Messages.Pass2Parser_Error_InsufficientBytesRead,
								segmentStartPos,
								endPos,
								in.position()),
						null);
				in.skipBytes(endPos - in.position());
			} else {
				throw new IOException(MessageUtil.format(Messages.Pass2Parser_Error_InsufficientBytesRead,
						segmentStartPos,
						endPos,
						in.position()));
			}
		}

		handler.addObject(heapObject, segmentStartPos);
	}

	private void readObjectArrayDump(long segmentStartPos) throws IOException {
		long id = readID();

		in.skipBytes(4);
		int size = in.readInt();
		long arrayClassObjectID = readID();

		ClassImpl arrayType = (ClassImpl) handler.lookupClass(arrayClassObjectID);
		if (arrayType == null)
			throw new RuntimeException(
					MessageUtil.format(Messages.Pass2Parser_Error_HandlerMustCreateFakeClassForAddress,
							Long.toHexString(arrayClassObjectID)));

		long usedHeapSize = handler.getObjectArrayHeapSize(arrayType, size);
		HeapObject heapObject = new HeapObject(handler.mapAddressToId(id), id, arrayType, usedHeapSize);
		heapObject.references.add(arrayType.getObjectAddress());
		heapObject.isArray = true;

		for (int ii = 0; ii < size; ii++) {
			long refId = readID();
			if (refId != 0)
				heapObject.references.add(refId);
		}

		handler.addObject(heapObject, segmentStartPos);
	}

	private void readPrimitiveArrayDump(long segmentStartPost) throws SnapshotException, IOException {
		long id = readID();

		in.skipBytes(4);
		int size = in.readInt();
		byte elementType = in.readByte();

		if ((elementType < IPrimitiveArray.Type.BOOLEAN) || (elementType > IPrimitiveArray.Type.LONG))
			throw new SnapshotException(Messages.Pass1Parser_Error_IllegalType);

		String name = IPrimitiveArray.TYPE[elementType];
		ClassImpl clazz = (ClassImpl) handler.lookupClassByName(name, true);
		if (clazz == null)
			throw new RuntimeException(
					MessageUtil.format(Messages.Pass2Parser_Error_HandleMustCreateFakeClassForName, name));

		long usedHeapSize = handler.getPrimitiveArrayHeapSize(elementType, size);
		HeapObject heapObject = new HeapObject(handler.mapAddressToId(id), id, clazz, usedHeapSize);
		heapObject.references.add(clazz.getObjectAddress());
		heapObject.isArray = true;

		handler.addObject(heapObject, segmentStartPost);

		int elementSize = IPrimitiveArray.ELEMENT_SIZE[elementType];
		in.skipBytes((long) elementSize * size);
	}

}
