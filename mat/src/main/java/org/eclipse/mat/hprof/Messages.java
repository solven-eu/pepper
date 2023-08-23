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

public class Messages {
	public static final String AbstractParser_Error_IllegalType = "Illegal Type:  {0}";
	public static final String AbstractParser_Error_InvalidHPROFHeader = "Invalid HPROF file header.";
	public static final String AbstractParser_Error_NotHeapDump = "Not a HPROF heap dump";
	public static final String AbstractParser_Error_UnknownHPROFVersion = "Unknown HPROF Version ({0})";
	public static final String AbstractParser_Error_UnsupportedHPROFVersion = "Unsupported HPROF Version {0}";
	public static final String EnhancerRegistry_ErrorCreatingParser = "Error creating parser for {0}";
	public static final String EnhancerRegistry_ErrorCreatingRuntime = "Error creating runtime for {0}";
	public static final String HprofIndexBuilder_ExtractingObjects = "Extracting objects from {0}";
	public static final String HprofIndexBuilder_Parsing = "Parsing {0}";
	public static final String HprofIndexBuilder_Scanning = "Scanning {0}";
	public static final String HprofIndexBuilder_Writing = "Writing {0}";
	public static final String HprofParserHandlerImpl_Error_ExpectedClassSegment =
			"Error: Found instance segment but expected class segment (see FAQ): 0x{0}";
	public static final String HprofParserHandlerImpl_Error_MultipleClassInstancesExist =
			"multiple class instances exist for {0}";
	public static final String HprofParserHandlerImpl_HeapContainsObjects = "Heap {0} contains {1,number} objects";
	public static final String HprofRandomAccessParser_Error_DumpIncomplete =
			"need to create dummy class. dump incomplete";
	public static final String HprofRandomAccessParser_Error_DuplicateClass = "Duplicate class: {0}";
	public static final String HprofRandomAccessParser_Error_IllegalDumpSegment = "Illegal dump segment {0}";
	public static final String HprofRandomAccessParser_Error_MissingClass = "missing fake class {0}";
	public static final String HprofRandomAccessParser_Error_MissingFakeClass = "missing fake class";
	public static final String JMapHeapDumpProvider_ErrorCreatingDump = "Error creating heap dump. jmap exit code  = ";
	public static final String JMapHeapDumpProvider_HeapDumpNotCreated =
			"Heap dump file was not created. jmap exit code  = ";
	public static final String JMapHeapDumpProvider_WaitForHeapDump =
			"Waiting while the heap dump is written to the disk";
	public static final String LocalJavaProcessesUtils_ErrorGettingProcesses = "Error getting list of processes";
	public static final String LocalJavaProcessesUtils_ErrorGettingProcessListJPS =
			"Error getting Java processes list with 'jps'. Try to configure a JDK for the HPROF jmap provider";
	public static final String Pass1Parser_DetectedCompressedReferences =
			"Detected compressed references, because with uncompressed 64-bit references the array at 0x{0} would overlap the array at 0x{1}";
	public static final String Pass1Parser_Error_IllegalRecordLength =
			"Illegal record length {0} at byte {1} for record type {2}";
	public static final String Pass1Parser_Error_IllegalType = "Illegal primitive object array type";
	public static final String Pass1Parser_Error_InvalidHeapDumpFile =
			"Error: Invalid heap dump file.\n Unsupported segment type {0} at position {1}";
	public static final String Pass1Parser_Error_invalidHPROFFile =
			"(Possibly) Invalid HPROF file: Expected to read another {0,number} bytes, but only {1,number} bytes are available.";
	public static final String Pass1Parser_Error_SupportedDumps = "Only 32bit and 64bit dumps are supported.";
	public static final String Pass1Parser_Error_UnresolvedName = "Unresolved Name 0x";
	public static final String Pass1Parser_Error_WritingThreadsInformation = "Error writing threads information";
	public static final String Pass1Parser_Info_WroteThreadsTo = "Wrote threads call stacks to {0}";
	public static final String Pass1Parser_Error_NoHeapDumpIndexFound =
			"Parser found {0} HPROF dumps in file {1}. No heap dump index {2} found. See FAQ.";
	public static final String Pass1Parser_Info_UsingDumpIndex =
			"Parser found {0} HPROF dumps in file {1}. Using dump index {2}. See FAQ.";
	public static final String Pass1Parser_UnexpectedEndPosition =
			"Heap dump segment at 0x{0} size {1} ends at 0x{2} instead of 0x{3}";
	public static final String Pass1Parser_UnexpectedRecord =
			"Heap dump record 0x{0} size {1} is not a supported record type.";
	public static final String Pass1Parser_GuessingLengthOverflow =
			"Guessing that heap dump record 0x{0} at 0x{1} with length {2} is probably overflowed, updating to length {3}. See bug 404679.";
	public static final String Pass1Parser_HeapDumpCreated = "Heap dump created at {0,time,long} {0,date,long}";
	public static final String Pass1Parser_HeapDumpsFound = "{0} heap dumps found";
	public static final String Pass2Parser_Error_HandleMustCreateFakeClassForName =
			"handler must create fake class for {0}";
	public static final String Pass2Parser_Error_HandlerMustCreateFakeClassForAddress =
			"handler must create fake class for 0x{0}";
	public static final String Pass2Parser_Error_InsufficientBytesRead =
			"Insufficient bytes read for instance at {0}, expected {1} read {2}";
	public static final String HPROFPreferences_Description = "Optional configuration of the HPROF heapdump parser.";
	public static final String HPROFPreferences_Strictness =
			"Parser Strictness: What to do when the dump doesn't match the specification";
	public static final String HPROFPreferences_Strictness_Stop =
			"Strict: Do not finish loading the dump and throw an error. For one exception to this strictness, see bug 404679.";
	public static final String HPROFPreferences_Strictness_Warning =
			"Warning: Continue parsing and loading the dump and raise a warning to the Error Log.";
	public static final String HPROFPreferences_Strictness_Permissive =
			"Permissive: Raise a warning and try to fix the potential problem.";
	public static final String HPROFStrictness_Unhandled_Preference =
			"The parser does not know how to handle the current strictness preference in some situations.";
	public static final String HPROFStrictness_Stopped =
			"The HPROF parser encountered a violation of the HPROF specification that it could not safely handle. This could be due to file truncation or a bug in the JVM. Please consider filing a bug at eclipse.org. To continue parsing the dump anyway, you can use -DhprofStrictnessWarning=true or set the strictness mode under Preferences > HPROF Parser > Parser Strictness. See the inner exception for details.";

	private Messages() {
	}
}
