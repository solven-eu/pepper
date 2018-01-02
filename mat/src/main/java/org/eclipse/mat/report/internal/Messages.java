/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.report.internal;

public final class Messages {
	public static final String ArgumentSet_Error_IllegalArgument =
			"Illegal argument: {0} of type {1} cannot be set to field {2} of type {3}";
	public static final String ArgumentSet_Error_Inaccessible = "Unable to access field {0} of type {1}";
	public static final String ArgumentSet_Error_Instantiation = "Unable to instantiate command {0}";
	public static final String ArgumentSet_Error_MissingMandatoryArgument = "Missing required parameter: {0}";
	public static final String ArgumentSet_Error_NoSuchArgument = "Query ''{0}'' has no argument named ''{1}''";
	public static final String ArgumentSet_Error_SetField = "Unable to set field of {0}";
	public static final String ArgumentSet_Msg_NullValue = "Setting null value for: {0}";
	public static final String ArrayInt_Error_LengthExceeded = "Requested length of new int[{0}] exceeds limit of {1}";
	public static final String ArrayLong_Error_LengthExceeded =
			"Requested length of new long[{0}] exceeds limit of {1}";
	public static final String CategoryDescriptor_Label_NoCategory = "<uncategorized>";
	public static final String CommandLine_Error_AssignmentFailed =
			"''{0}'' cannot be assigned. Argument ''{1}'' is already set.";
	public static final String CommandLine_Error_InvalidCommand = "Invalid command line: {0}";
	public static final String CommandLine_Error_MissingArgument = "Query ''{0}'' has no argument ''{1}''";
	public static final String CommandLine_Error_MissingValue = "Missing value for argument ''{0}'' {1}";
	public static final String CommandLine_Error_NotFound = "Command {0} not found.";
	public static final String CommandLine_Error_NoUnflaggedArguments =
			"No unflagged parameters available for argument ''{0}''";
	public static final String ConsoleProgressListener_Label_Subtask = "Subtask:";
	public static final String ConsoleProgressListener_Label_Task = "Task:";
	public static final String ContextDerivedData_Error_OperationNotFound =
			"Mismatch: Operation ''{0}'' not found in ''{1}''";
	public static final String Converters_Error_InvalidEnumValue = "Must be one of {0}";
	public static final String DisplayFileResult_Label_NoFile = "<no file>";
	public static final String Filter_Error_IllegalCharacters = "Illegal characters: {0}";
	public static final String Filter_Error_InvalidRegex = "Invalid regular expression:";
	public static final String Filter_Error_Parsing =
			"Error parsing the filter expression.\n\nUse one of the following:\nIntervals: 1000..10000  1%..10%\nUpper Boundary: < = \"10000 <1%\nLower Boundary: >1000 > = \"5%\n\n";
	public static final String Filter_Label_Numeric = "<Numeric>";
	public static final String Filter_Label_Regex = "<Regex>";
	public static final String HtmlOutputter_Error_MovingFile = "Error moving file {0} to {1}";
	public static final String HtmlOutputter_Label_Details = "Details";
	public static final String HtmlOutputter_Label_NotApplicable = "n/a";
	public static final String HtmlOutputter_Msg_TreeIsLimited = "Depth of the tree is limited to 100";
	public static final String HtmlOutputter_Label_AllObjects = "All objects";
	public static final String HtmlOutputter_Label_FirstObjects = "First {0} of {1} objects";
	public static final String HtmlOutputter_Label_AllNObjects = "All {0} objects";
	public static final String PageSnippets_Label_HideUnhide = "hide / unhide";
	public static final String PageSnippets_Label_CreatedBy =
			"Created by <a href = \"http://www.eclipse.org/mat/\" target = \"_blank\">Eclipse Memory Analyzer</a>";
	public static final String PageSnippets_Label_OpenInMemoryAnalyzer = "Open in Memory Analyzer:";
	public static final String PageSnippets_Label_StartPage = "Start Page";
	public static final String PageSnippets_Label_TableOfContents = "Table Of Contents";
	public static final String PartsFactory_Error_Construction = "Unable to construct part for type {0}";
	public static final String PropertyResult_Column_Name = "Name";
	public static final String PropertyResult_Column_Value = "Value";
	public static final String Quantize_Error_MismatchArgumentsColumns =
			"Mismatch between number of arguments and number of columns";
	public static final String Queries_Error_NotAvialable = "Query not available: {0}";
	public static final String Queries_Error_UnknownArgument = "Unknown argument: {0} for query {1}";
	public static final String QueryContextImpl_ImpossibleToConvert =
			"Impossible to convert any string to {0}, query is not suitable for this snapshot";
	public static final String QueryDescriptor_Error_IgnoringQuery = "Ignoring query {0} due to argument {1}";
	public static final String QueryDescriptor_Error_NotSupported = "Not supported: {0}";
	public static final String QueryPart_Error_ColumnNotFound = "Column not found: {0}";
	public static final String QueryPart_Error_Filter = "Error in filter: {0}";
	public static final String QueryPart_Error_IgnoringResult = "Ignoring result of ''{0}'' due to {1}";
	public static final String QueryPart_Error_InvalidProvider =
			"Invalid configuration parameter {0}: expected <provider> = \"<operation>: {1}";
	public static final String QueryPart_Error_InvalidProviderOperation =
			"Invalid configuration parameter {0}: unknown operation: {1}";
	public static final String QueryPart_Error_MissingEqualsSign = "Missing '' = \"'' sign in filter ''{0}''";
	public static final String QueryPart_Error_NoCommand = "No command specified for test ''{0}'' of section ''{1}''";
	public static final String QueryPart_Error_RetainedSizeColumnNotFound =
			"Error added retained size column for ''{0}'' - no context provider found.";
	public static final String QueryPart_Error_SortColumnNotFound = "Sort column not found: {0}";
	public static final String QueryPart_Label_ReportRoot = "Report Root";
	public static final String QueryPart_Msg_TestProgress = "Test ''{0}'' of section ''{1}''";
	public static final String QueryRegistry_Error_Advice = "Field {0} of {1} has advice {2} but is not of type {3}.";
	public static final String QueryRegistry_Error_Argument = "Error getting argument ''{0}'' of class ''{1}''";
	public static final String QueryRegistry_Error_Inaccessible =
			"Unable to access argument ''{0}'' of class ''{1}''. Make sure the attribute is PUBLIC.";
	public static final String QueryRegistry_Error_NameBound = "Query name ''{0}'' is already bound to {1}\\!";
	public static final String QueryRegistry_Error_Registering = "Error registering query: {0}";
	public static final String QueryRegistry_MissingLabel = "Missing label: {0} for menu entry number {1}.";
	public static final String QueryRegistry_Msg_QueryRegistered = "Query registered: {0}";
	public static final String QuerySpec_Error_IncompatibleTypes = "Incompatible types: {0} and {1}";
	public static final String QueueInt_Error_LengthExceeded = "Requested length of new int[{0}] exceeds limit of {1}";
	public static final String QueueInt_ZeroSizeQueue = "QueueInt called on a zero-size queue";
	public static final String RefinedResultBuilder_Error_ColumnsSorting =
			"Same number of columns and sort directions must be provided.";
	public static final String RefinedResultBuilder_Error_UnsupportedType = "Unsupported type: {0}";
	public static final String RefinedStructuredResult_Calculating = "Calculating";
	public static final String RegistryReader_Error_Registry = "Error while creating: ''{0}''";
	public static final String RendererRegistry_Error_MissingAnnotation = "Class must be annotated as Renderer: {0}";
	public static final String ReportPlugin_InternalError = "Internal Error";
	public static final String ResultRenderer_Error_OutputterNotFound =
			"No outputter found for format ''{0}'' and type ''{1}''";
	public static final String ResultRenderer_Label_Details = "Details \u00BB";
	public static final String ResultRenderer_Label_TableOfContents = "Table Of Contents";
	public static final String RunRegisterdReport_Error_UnknownReport = "Unknown report: {0}";
	public static final String SpecFactory_Error_MissingTemplate = "Template not found: {0}";
	public static final String TextResult_Label_Links = "Links";
	public static final String TotalsRow_Label_Filtered = "({0,number,integer} filtered)";
	public static final String TotalsRow_Label_Total =
			"Total: {0,number,integer} {0,choice,0\\#entries|1\\#entry|1<entries}";
	public static final String TotalsRow_Label_TotalVisible =
			"Total: {0,number,integer} of {1,number,integer} {1,choice,0\\#entries|1\\#entry|1<entries}; {2,number,integer} more";

	public static final String ConsoleProgressListener_ERROR = "[ERROR] ";
	public static final String ConsoleProgressListener_INFO = "[INFO] ";
	public static final String ConsoleProgressListener_UNKNOWN = "[UNKNOWN] ";
	public static final String ConsoleProgressListener_WARNING = "[WARNING]";

	private Messages() {
	}
}