/*******************************************************************************
 * Copyright (c) 2008, 2013 SAP AG and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *     IBM Corporation - updates including multiple snapshots
 *******************************************************************************/
package org.eclipse.mat.internal;

public class Messages {
	public static final String ArgumentParser_ErrorMsg_ParsingError = "Error parsing ''{0}'': {1}";
	public static final String ArgumentParser_ErrorMsg_Unparsed = "Remaining unparsed line: {0}";
	public static final String ArrayFillRatioQuery_ColumnFillRatio = "Fill Ratio";
	public static final String ArrayFillRatioQuery_ColumnNumObjects = "\\# Objects";
	public static final String ArrayFillRatioQuery_ExtractingFillRatios = "Extracting fill ratios...";
	public static final String ArraysBySizeQuery_ColumnLength = "Length";
	public static final String ArraysBySizeQuery_ColumnNumObjects = "\\# Objects";
	public static final String ArraysBySizeQuery_ExtractingArraySizes = "Extracting array sizes...";
	public static final String BigDropsQuery_AccumulationPoint = "Accumulation Point";
	public static final String BigDropsQuery_Column_AccPtSize = "Acc.Pt. Size";
	public static final String BigDropsQuery_Column_AccumulationPoint = "Accumulation Point";
	public static final String BigDropsQuery_Column_Dominator = "Dominator";
	public static final String BigDropsQuery_Column_DomRetainedSize = "Dom. Retained Size";
	public static final String BigDropsQuery_Column_NumChildren = "\\# Children";
	public static final String BigDropsQuery_Dominator = "Dominator";
	public static final String BigDropsQuery_Root = "<ROOT>";
	public static final String BundleReaderFactory_ErrorMsg_EquinoxNotFound = "Could not find Equinox OSGi Framework.";
	public static final String BundleRegistryQuery_Bundles = "Bundles";
	public static final String BundleRegistryQuery_BundleState = "Bundle State";
	public static final String BundleRegistryQuery_BundlesUsing = "Bundles Using";
	public static final String BundleRegistryQuery_ContributedBy = "contributed by: {0}";
	public static final String BundleRegistryQuery_Dependencies = "Dependencies";
	public static final String BundleRegistryQuery_Dependents = "Dependents";
	public static final String BundleRegistryQuery_ErrorMsg_FailedReadingModel = "Failed reading bundle from OSGiModel";
	public static final String BundleRegistryQuery_ExtensionPoints = "Extension Points";
	public static final String BundleRegistryQuery_Extensions = "Extensions";
	public static final String BundleRegistryQuery_Fragments = "Fragments";
	public static final String BundleRegistryQuery_HostedBy = "hosted by: {0}";
	public static final String BundleRegistryQuery_Properties = "Properties";
	public static final String BundleRegistryQuery_RegisteredBy = "registered by: {0}";
	public static final String BundleRegistryQuery_RegisteredServices = "Registered Services";
	public static final String BundleRegistryQuery_Services = "Services";
	public static final String BundleRegistryQuery_UserServices = "Used Services";
	public static final String ClassLoaderExplorerQuery_Class = "Class";
	public static final String ClassLoaderExplorerQuery_ClassLoader = "Class Loader";
	public static final String ClassLoaderExplorerQuery_Column_DefinedClasses = "Defined Classes";
	public static final String ClassLoaderExplorerQuery_Column_NoInstances = "No. of Instances";
	public static final String ClassLoaderExplorerQuery_DefinedClasses = "Defined Classes";
	public static final String ClassLoaderExplorerQuery_Instances = "Instances";
	public static final String ClassSpecificNameResolverRegistry_Error_CreateResolver =
			"Error while creating name resolver ''{0}''";
	public static final String ClassSpecificNameResolverRegistry_Error_MissingObject =
			"No object to resolve class specific name for.";
	public static final String ClassSpecificNameResolverRegistry_Error_MissingSubjects =
			"Resolver without subjects: ''{0}''";
	public static final String ClassSpecificNameResolverRegistry_Error_Resolving = "Error resolving name of {0}";
	public static final String ClassSpecificNameResolverRegistry_ErrorMsg_DuringResolving =
			"Error resolving name of {0}";
	public static final String ClassSpecificNameResolverRegistry_ErrorMsg_MissingSubject =
			"Resolver without subjects: ''{0}''";
	public static final String ClassSpecificNameResolverRegistry_ErrorMsg_WhileCreatingResolver =
			"Error while creating name resolver ''{0}''";
	public static final String CollectionFillRatioQuery_ClassNotFound = "Class ''{0}'' not found in heap dump.";
	public static final String CollectionFillRatioQuery_Column_FillRatio = "Fill Ratio";
	public static final String CollectionFillRatioQuery_ColumnNumObjects = "\\# Objects";
	public static final String CollectionFillRatioQuery_ErrorMsg_AllArgumentsMustBeSet =
			"If the collection argument is set to a custom (e.g. non-JDK) collection class, the size_attribute and array_attribute argument must be set. Otherwise, the query cannot calculate the fill ratio.";
	public static final String CollectionFillRatioQuery_ExtractingFillRatios = "Extracting collection fill ratios...";
	public static final String CollectionFillRatioQuery_IgnoringCollection = "Ignoring collection {0}";
	public static final String CollectionUtil_BadBackingArray =
			"Field ''{0}'' of {1} contains {2} instead of an array of objects as a backing array";
	public static final String CollectionsBySizeQuery_ClassNotFound = "Class ''{0}'' not found in heap dump.";
	public static final String CollectionsBySizeQuery_CollectingSizes = "Collecting collection sizes...";
	public static final String CollectionsBySizeQuery_Column_Length = "Length";
	public static final String CollectionsBySizeQuery_Column_NumObjects = "\\# Objects";
	public static final String CollectionsBySizeQuery_ErrorMsg_ArgumentMissing =
			"If the collection argument is set to a custom (e.g. non-JDK) collection class, the size_attribute must be set. Otherwise, the query cannot determine the size of the collection.";
	public static final String CollectionsBySizeQuery_IgnoringCollection = "Ignoring collection {0}";
	public static final String Column_ClassLoaderName = "Class Loader Name";
	public static final String Column_ClassName = "Class Name";
	public static final String Column_Heap = "Heap";
	public static final String Column_Objects = "Objects";
	public static final String Column_Percentage = "Percentage";
	public static final String Column_RetainedHeap = "Retained Heap";
	public static final String Column_ShallowHeap = "Shallow Heap";
	public static final String CompareTablesQuery_ColumnAbsolute = "{0} \\#{1}";
	public static final String CompareTablesQuery_ColumnDifference = "{0} \\#{1}-\\#{2}";
	public static final String CompareTablesQuery_ColumnPercentDifference = "{0} \\#{1}-\\#{2} %";
	public static final String CompareTablesQuery_DifferenceFirst = "Table {0} without Table {1}";
	public static final String CompareTablesQuery_DifferenceLast = "{0} and Table {1}";
	public static final String CompareTablesQuery_DifferenceMiddle = "{0}, Table {1}";
	public static final String CompareTablesQuery_DifferenceOf2 = "Table {0} without Table {1}";
	public static final String CompareTablesQuery_IntersectionFirst = "Intersection of Table {0}, Table {1}";
	public static final String CompareTablesQuery_IntersectionLast = "{0} and Table {1}";
	public static final String CompareTablesQuery_IntersectionMiddle = "{0}, Table {1}";
	public static final String CompareTablesQuery_IntersectionOf2 = "Intersection of Table {0} and Table {1}";
	public static final String CompareTablesQuery_Table = "Table {0}";
	public static final String CompareTablesQuery_SymmetricDifferenceFirst =
			"Symmetric difference of Table {0}, Table {1}";
	public static final String CompareTablesQuery_SymmetricDifferenceLast = "{0} and Table {1}";
	public static final String CompareTablesQuery_SymmetricDifferenceMiddle = "{0}, Table {1}";
	public static final String CompareTablesQuery_SymmetricDifferenceOf2 =
			"Symmetric difference of Table {0} and Table {1}";
	public static final String CompareTablesQuery_UnionFirst = "Union of Table {0}, Table {1}";
	public static final String CompareTablesQuery_UnionLast = "{0} and Table {1}";
	public static final String CompareTablesQuery_UnionMiddle = "{0}, Table {1}";
	public static final String CompareTablesQuery_UnionOf2 = "Union of Table {0} and Table {1}";
	public static final String ComponentReportQuery_Classes = "Classes:";
	public static final String ComponentReportQuery_ClassLoader = "Class Loader:";
	public static final String ComponentReportQuery_CollectionFillRatios = "Collection Fill Ratios";
	public static final String ComponentReportQuery_Comment = "Comment";
	public static final String ComponentReportQuery_ComponentReport = "Component Report {0}";
	public static final String ComponentReportQuery_Details = "Details";
	public static final String ComponentReportQuery_DetectedEmptyCollections =
			"Detected the following empty collections:";
	public static final String ComponentReportQuery_Distribution = "Distribution";
	public static final String ComponentReportQuery_DuplicateStrings = "Duplicate Strings";
	public static final String ComponentReportQuery_EmptyCollections = "Empty Collections";
	public static final String ComponentReportQuery_PathsToReferents = "Paths to referents";
	public static final String ComponentReportQuery_FinalizerStatistics = "Finalizer Statistics";
	public static final String ComponentReportQuery_Histogram = "Histogram";
	public static final String ComponentReportQuery_HistogramFinalizeMethod =
			"Histogram of Objects with Finalize Method";
	public static final String ComponentReportQuery_HistogramOfSoftReferences = "Histogram of Soft References";
	public static final String ComponentReportQuery_HistogramOfWeakReferences = "Histogram of Weak References";
	public static final String ComponentReportQuery_Label_Bytes = "({0} bytes)";
	public static final String ComponentReportQuery_MapCollisionRatios = "Map Collision Ratios";
	public static final String ComponentReportQuery_Miscellaneous = "Miscellaneous";
	public static final String ComponentReportQuery_Msg_DetectedCollectionFillRatios =
			"Detected the following collections with fill ratios below 20%:";
	public static final String ComponentReportQuery_Msg_DetectedCollisionRatios =
			"Detected the following maps with collision ratios above 80%:";
	public static final String ComponentReportQuery_Msg_FoundOccurrences =
			"Found {0,number} occurrences of char[] with at least 10 instances having identical content. Total size is {1} bytes.";
	public static final String ComponentReportQuery_Msg_InstancesRetainBytes =
			"{0,number} instances of <strong>{1}</strong> retain <strong>{2}</strong> bytes.";
	public static final String ComponentReportQuery_Msg_NoAliveSoftReferences =
			"Component does not keep Soft References alive.";
	public static final String ComponentReportQuery_Msg_NoAliveWeakReferences =
			"Component does not keep Weak References alive.";
	public static final String ComponentReportQuery_Msg_NoCollisionRatiosFound =
			"No maps found with collision ratios greater than 80%.";
	public static final String ComponentReportQuery_Msg_NoExcessiveEmptyCollectionsFound =
			"No excessive usage of empty collections found.";
	public static final String ComponentReportQuery_Msg_NoFinalizerFound =
			"Component does not keep object with Finalizer methods alive.";
	public static final String ComponentReportQuery_Msg_NoFinalizerObjects =
			"Heap dump contains no java.lang.ref.Finalizer objects.<br/>IBM VMs implement Finalizer differently and are currently not supported by this report.";
	public static final String ComponentReportQuery_Msg_NoLowFillRatiosFound =
			"No serious amount of collections with low fill ratios found.";
	public static final String ComponentReportQuery_Msg_NoSoftReferencesFound =
			"Heap dump contains no soft references.";
	public static final String ComponentReportQuery_Msg_NoWeakReferencesFound =
			"Heap dump contains no weak references.";
	public static final String ComponentReportQuery_Msg_SoftReferencesFound =
			"A total of {0} java.lang.ref.SoftReference object{0,choice,0\\#s|1\\#|2\\#s} have been found, which softly reference {1,choice,0\\#no objects|1\\#one object|2\\#{1,number} objects}.";
	public static final String ComponentReportQuery_Msg_SoftReferencesRetained =
			"{0,choice,0\\#No objects|1\\#one object|2\\#{0,number} objects} totalling {1} are retained (kept alive) only via soft references.";
	public static final String ComponentReportQuery_Msg_SoftReferencesStronglyRetained =
			"{0,choice,0\\#No objects|1\\#One object|2\\#{0,number} objects} totalling {1} are softly referenced and also strongly retained (kept alive) via soft references.";
	public static final String ComponentReportQuery_Msg_TotalFinalizerMethods =
			"A total of {0} object{0,choice,0\\#s|1\\#|2\\#s} implement the finalize method.";
	public static final String ComponentReportQuery_Msg_WeakReferencesFound =
			"A total of {0} java.lang.ref.WeakReference object{0,choice,0\\#s|1\\#|2\\#s} have been found, which weakly reference {1,choice,0\\#no objects|1\\#one object|2\\#{1,number} objects}.";
	public static final String ComponentReportQuery_Msg_WeakReferencesRetained =
			"{0,choice,0\\#No objects|1\\#One object|2\\#{0,number} objects} totalling {1} are retained (kept alive) only via weak references.";
	public static final String ComponentReportQuery_Msg_WeakReferencesStronglyRetained =
			"{0,choice,0\\#No objects|1\\#One object|2\\#{0,number} objects} totalling {1} are weakly referenced and also strongly retained (kept alive) via weak references.";
	public static final String ComponentReportQuery_Objects = "Objects:";
	public static final String ComponentReportQuery_Overview = "Overview";
	public static final String ComponentReportQuery_PossibleMemoryLeak = "Possible Memory Leak";
	public static final String ComponentReportQuery_PossibleMemoryWaste = "Possible Memory Waste";
	public static final String ComponentReportQuery_RetainedSet = "Retained Set";
	public static final String ComponentReportQuery_Size = "Size:";
	public static final String ComponentReportQuery_SoftReferenceStatistics = "Soft Reference Statistics";
	public static final String ComponentReportQuery_TopConsumers = "Top Consumers";
	public static final String ComponentReportQuery_TopElementsInclude = "Top elements include:";
	public static final String ComponentReportQuery_WeakReferenceStatistics = "Weak Reference Statistics";
	public static final String CustomizedRetainedSetQuery_RetainedBy = "Retained by ''{0}''";
	public static final String DominatorQuery_Group_ByClass = "Group by class";
	public static final String DominatorQuery_Group_ByClassLoader = "Group by class loader";
	public static final String DominatorQuery_Group_ByPackage = "Group by package";
	public static final String DominatorQuery_Group_None = "No Grouping (objects)";
	public static final String DominatorQuery_LabelAll = "<all>";
	public static final String DominatorQuery_Msg_Grouping = "Grouping by package";
	public static final String DuplicatedClassesQuery_Checking = "Checking for duplicate Classes";
	public static final String DuplicatedClassesQuery_ClassLoaderNotFound = "ClassLoader of 0x{0} not found";
	public static final String DuplicatedClassesQuery_Column_Count = "Count";
	public static final String DuplicatedClassesQuery_Column_DefinedClasses = "Defined Classes";
	public static final String DuplicatedClassesQuery_Column_NoInstances = "No. of Instances";
	public static final String EclipseNameResolver_EquinoxStartupClassLoader = "Equinox Startup Class Loader";
	public static final String EclipseNameResolver_Point = "({0},{1})";
	public static final String EclipseNameResolver_Rectangle = "({0},{1},{2},{3})";
	public static final String EclipseNameResolver_RGB = "({0,number,000},{1,number,000},{2,number,000})";
	public static final String EquinoxBundleReader_CannotFindContributorID =
			"Could not parse contributorId. Expected a number but got \"{0}\"";
	public static final String EquinoxBundleReader_ErrorMsg_BundleNotFound = "Bundle host not found: 0x{0}";
	public static final String EquinoxBundleReader_ErrorMsg_DuplicateConfigurationElement =
			"Duplicate configuration element: {0}, first object address: 0x{1}, second object address: 0x{2}";
	public static final String EquinoxBundleReader_ErrorMsg_DuplicateExtension =
			"Duplicate extension: {0}, first object address: 0x{1}, second object address: 0x{2}";
	public static final String EquinoxBundleReader_ErrorMsg_DuplicateExtensionPoint =
			"Duplicate extension point: {0}, first object address: 0x{1}, second object address: 0x{2}";
	public static final String EquinoxBundleReader_ErrorMsg_ExpectedArrayType = "Expected array type: 0x{0}";
	public static final String EquinoxBundleReader_ErrorMsg_ExpectedFieldExtraInformation =
			"Expected ''extraInformation'': 0x{0}";
	public static final String EquinoxBundleReader_ErrorMsg_ExpectedFieldObjectId =
			"Expected field ''objectId'': 0x{0}";
	public static final String EquinoxBundleReader_ErrorMsg_ExpectedFieldParent = "Expected field ''parentId'': 0x{0}";
	public static final String EquinoxBundleReader_ErrorMsg_ExpectedStringArray =
			"Expected ''propertiesAndValues'' in String[] format: 0x{0}";
	public static final String EquinoxBundleReader_ErrorMsg_ReadingProperty =
			"Error reading property of ConfigurationElement: 0x{0}";
	public static final String EquinoxBundleReader_ErrorMsg_ServiceName = "Error reading service''s name: 0x{0}";
	public static final String EquinoxBundleReader_ErrorMsg_ServiceProperty = "Error reading Service property: 0x{0}";
	public static final String EquinoxBundleReader_ErrorMsg_SoftReferencesNotHandled =
			"Extension 0x{0} has properties in the form of SoftReference. Not handled.";
	public static final String EquinoxBundleReader_ErrorMsg_UnknownElementType = "Unknown element 0x{0} of type {1}";
	public static final String EquinoxBundleReader_ExpectedFieldContributorId =
			"Expected field ''contributorId'': 0x{0}";
	public static final String EquinoxBundleReader_ExpectedFieldPropertiesAndValues =
			"Expected field ''propertiesAndValues'': 0x{0}";
	public static final String EquinoxBundleReader_NotApplicable = "N/A";
	public static final String EquinoxBundleReader_ProcessListenerBundles = "Bundles";
	public static final String EquinoxBundleReader_ReadingBundles = "Reading bundles";
	public static final String EquinoxBundleReader_ReadingExtensions = "Reading extensions";
	public static final String EquinoxBundleReader_ReadingServices = "Reading services";
	public static final String EquinoxBundleReader_ReadingDependencies = "Reading dependencies";
	public static final String EquinoxBundleReader_State_Active = "active";
	public static final String EquinoxBundleReader_State_Installed = "installed";
	public static final String EquinoxBundleReader_State_Resolved = "resolved";
	public static final String EquinoxBundleReader_State_Starting = "starting";
	public static final String EquinoxBundleReader_State_LazyStarting = "lazy starting";
	public static final String EquinoxBundleReader_State_Stopping = "stopping";
	public static final String EquinoxBundleReader_State_Uninstalled = "uninstalled";
	public static final String ExtractListValuesQuery_CollectingElements = "Collecting {0} element(s) of {1}";
	public static final String ExtractListValuesQuery_NotAWellKnownList = "Not a (well-known) list: {0}";
	public static final String FinalizerQuery_Finalizers = "Finalizers";
	public static final String FinalizerQuery_FinalizerThread = "Finalizer Thread";
	public static final String FinalizerQuery_FinalizerThreadLocals = "Finalizer Thread Locals";
	public static final String FinalizerQuery_InProcessing = "In processing by Finalizer Thread";
	public static final String FinalizerQuery_ReadyForFinalizer = "Ready for Finalizer Thread";
	public static final String FinalizerQueueQuery_ErrorMsg_MultipleFinalizerClasses =
			"Error: Snapshot contains multiple java.lang.ref.Finalizer classes.";
	public static final String FinalizerQueueQuery_Msg_ExtractingObjects =
			"Extracting objects ready for finalization from queue...";
	public static final String FinalizerQueueQuery_ReadyForFinalizerThread = "Ready for Finalizer Thread";
	public static final String FinalizerQueueQuery_ReadyForFinalizerThread_Histogram =
			"Ready for Finalizer Thread - Histogram";
	public static final String FinalizerQueueQuery_ReadyForFinalizerThread_List =
			"Ready for Finalizer Thread - Object List";
	public static final String FinalizerReferenceStatQuery_Label_Referenced =
			"Histogram of Objects Referenced by Finalizer";
	public static final String FinalizerReferenceStatQuery_Label_Retained = "Only Retained through Finalizer";
	public static final String FinalizerReferenceStatQuery_Label_StronglyRetainedReferents =
			"Referents strongly retained by finalizer references";
	public static final String FinalizerThreadQuery_ErrorMsg_FinalizerThreadInstanceNotFound =
			"Instance of class java.lang.ref.Finalizer$FinalizerThread not found in heap dump.";
	public static final String FinalizerThreadQuery_ErrorMsg_FinalizerThreadNotFound =
			"Class java.lang.ref.Finalizer$FinalizerThread not found in heap dump.";
	public static final String FinalizerThreadQuery_ErrorMsg_MultipleFinalizerThreadClasses =
			"Error: Snapshot contains multiple instances of java.lang.ref.Finalizer$FinalizerThread class.";
	public static final String FinalizerThreadQuery_ErrorMsg_MultipleThreadClassesFound =
			"Error: Snapshot contains multiple java.lang.Thread classes.";
	public static final String FinalizerThreadQuery_ErrorMsg_MultipleThreads =
			"Error: Snapshot contains multiple java.lang.ref.Finalizer$FinalizerThread classes.";
	public static final String FinalizerThreadQuery_ErrorMsg_ThreadClassNotFound =
			"Class java.lang.Thread not found in heap dump.";
	public static final String FinalizerThreadQuery_ErrorMsg_ThreadInstanceNotFound =
			"Instance of class java.lang.Thread not found in heap dump.";
	public static final String FinalizerThreadQuery_FinalizerThread = "Finalizer thread";
	public static final String FinalizerThreadQuery_SecondaryFinalizer = "Secondary finalizer";
	public static final String FindLeaksQuery_AccumulationPoint = "Accumulation Point";
	public static final String FindLeaksQuery_Column_AccPointPercent = "Acc. Point %";
	public static final String FindLeaksQuery_Column_AccPointRetainedHeap = "Acc. Point Retained Heap";
	public static final String FindLeaksQuery_Column_AccumulationPoint = "Accumulation Point";
	public static final String FindLeaksQuery_Column_NumObjects = "\\# Objects";
	public static final String FindLeaksQuery_Column_SuspectPercent = "Suspect's %";
	public static final String FindLeaksQuery_Column_SuspectRetainedHeap = "Suspect's Retained Heap";
	public static final String FindLeaksQuery_ColumnLeakSuspect = "Leak Suspect";
	public static final String FindLeaksQuery_LeakSuspect = "Leak Suspect";
	public static final String FindLeaksQuery_NotFound = "<not found>";
	public static final String FindLeaksQuery_PathNotFound = "Couldn''t find paths for {0} of the {1} objects";
	public static final String FindLeaksQuery_SearchingGroupsOfObjects = "Searching suspicious groups of objects ...";
	public static final String FindLeaksQuery_SearchingSingleObjects = "Searching suspicious single objects ...";
	public static final String FindLeaksQuery_TooManySuspects =
			"Too many suspect instances ({0}). Will use {1} random from them to search for common path.";
	public static final String FindStringsQuery_SearchingStrings = "Searching Strings...";
	public static final String GCRootInfo_BusyMonitor = "Busy Monitor";
	public static final String GCRootInfo_Finalizable = "Finalizable";
	public static final String GCRootInfo_JavaLocal = "Java Local";
	public static final String GCRootInfo_JavaStackFrame = "Java Stack Frame";
	public static final String GCRootInfo_JNIGlobal = "JNI Global";
	public static final String GCRootInfo_JNILocal = "JNI Local";
	public static final String GCRootInfo_NativeStack = "Native Stack";
	public static final String GCRootInfo_SystemClass = "System Class";
	public static final String GCRootInfo_Thread = "Thread";
	public static final String GCRootInfo_ThreadBlock = "Thread Block";
	public static final String GCRootInfo_Unfinalized = "Unfinalized";
	public static final String GCRootInfo_Unreachable = "Unreachable";
	public static final String GCRootInfo_Unkown = "Unknown";
	public static final String GroupByValueQuery_Column_AvgRetainedSize = "Avg. Retained Size";
	public static final String GroupByValueQuery_Column_Objects = "Objects";
	public static final String GroupByValueQuery_Column_StringValue = "String Value";
	public static final String GroupByValueQuery_GroupingObjects = "Grouping objects ...";
	public static final String HashEntriesQuery_Column_Collection = "Collection";
	public static final String HashEntriesQuery_Column_Key = "Key";
	public static final String HashEntriesQuery_Column_Value = "Value";
	public static final String HashEntriesQuery_ErrorMsg_ClassNotFound = "Class ''{0}'' not found in heap dump.";
	public static final String HashEntriesQuery_ErrorMsg_MissingArguments =
			"If the map argument is set to a custom (e.g. non-JDK) collection class, the array_attribute, key_attribute and value_attribute arguments must be set. Otherwise, the query cannot determine the contents of the map.";
	public static final String HashEntriesQuery_Msg_Extracting = "Extracting Key Value Pairs...";
	public static final String HashSetValuesQuery_ErrorMsg_MissingArgument =
			"If the collection argument is set to a custom (e.g. non-JDK) collection class, the array_attribute and key_attribute arguments must be set. Otherwise, the query cannot determine the contents of the hash set.";
	public static final String HashSetValuesQuery_ErrorMsg_NotAHashSet = "Not a hash set: {0}";
	public static final String HeapDumpInfoQuery_32bit = "32-bit";
	public static final String HeapDumpInfoQuery_64bit = "64-bit";
	public static final String HeapDumpInfoQuery_Column_HeapFormat = "Format";
	public static final String HeapDumpInfoQuery_Column_JVMVersion = "JVM version";
	public static final String HeapDumpInfoQuery_Column_Time = "Time";
	public static final String HeapDumpInfoQuery_TimeFormat = "{0,time,long}";
	public static final String HeapDumpInfoQuery_Column_Date = "Date";
	public static final String HeapDumpInfoQuery_DateFormat = "{0,date}";
	public static final String HeapDumpInfoQuery_Column_FilePath = "File path";
	public static final String HeapDumpInfoQuery_Column_FileLength = "File length";
	public static final String HeapDumpInfoQuery_Column_IdentifierSize = "Identifier size";
	public static final String HeapDumpInfoQuery_Column_UseCompressedOops = "Compressed object pointers";
	public static final String HeapDumpInfoQuery_Column_NumClasses = "Number of classes";
	public static final String HeapDumpInfoQuery_Column_NumClassLoaders = "Number of class loaders";
	public static final String HeapDumpInfoQuery_Column_NumGCRoots = "Number of GC roots";
	public static final String HeapDumpInfoQuery_Column_NumObjects = "Number of objects";
	public static final String HeapDumpInfoQuery_NumClassesFormat = "{0}";
	public static final String HeapDumpInfoQuery_NumClassLoadersFormat = "{0}";
	public static final String HeapDumpInfoQuery_NumGCRootsFormat = "{0}";
	public static final String HeapDumpInfoQuery_NumObjectsFormat = "{0}";
	public static final String HeapDumpInfoQuery_FileLengthFormat = "{0}";
	public static final String HeapDumpInfoQuery_Column_UsedHeapDump = "Used heap dump";
	public static final String HeapDumpInfoQuery_MultipleSnapshotIdentifier = "Multiple snapshot identifier";
	public static final String HeapDumpInfoQuery_PropertyName = "Property Name";
	public static final String HeapDumpInfoQuery_ProperyValue = "Property Value";
	public static final String HeapDumpProviderRegistry_ErrorGettingArgumentErrorMsg =
			"Error get argument ''{0}'' of class ''{1}''";
	public static final String HeapDumpProviderRegistry_NameAlreadyBouneErrorMsg =
			"Heap dump adapter name ''{0}'' is already bound to {1}\\!";
	public static final String HeapDumpProviderRegistry_UnableToAccessArgumentErrorMsg =
			"Unable to access argument ''{0}'' of class ''{1}''. Make sure the attribute is PUBLIC.";
	public static final String HeapDumpProviderRegistry_WrongTypeErrorMsg =
			"Field {0} of {1} has advice {2} but is not of type {3}.";
	public static final String HeapObjectArgumentFactory_ErrorMsg_MultipleObjects =
			"Argument ''{0}'' expects one object not {1} objects";
	public static final String HeapObjectArgumentFactory_ErrorMsg_SettingField =
			"{0}: Error setting heap objects to field ''{1}''";
	public static final String HeapObjectArgumentFactory_ErrorMsg_TypeNotSupported =
			"Type ''{0}'' of argument ''{1}'' not supported.";
	public static final String HeapObjectArgumentFactory_Label_Objects = "[objects]";
	public static final String HeapObjectArgumentFactory_Objects = "Objects";
	public static final String HeapObjectContextArgument_Label_Context = "[context]";
	public static final String HeapObjectParamArgument_ErrorMsg_NorResult = "OQL Query does not yield a result: {0}";
	public static final String HeapObjectParamArgument_ErrorMsg_NotAClass =
			"Not a class: 0x{0} ({2}). If specifying ''{3}'', the selected objects all must be classes.";
	public static final String HeapObjectParamArgument_ErrorMsg_NotAClassLoader =
			"Not a class loader: 0x{0} ({2}). If specifying ''{3}'', the selected objects all must be class loaders.";
	public static final String HeapObjectParamArgument_ErrorMsg_NotAListOfObjects =
			"OQL query does not return a list of objects: {0}";
	public static final String HeapObjectParamArgument_ErrorMsg_UnknownArgument = "Unknown argument type: {0}";
	public static final String HeapObjectParamArgument_Msg_AddedInstances = "Added class {0} and {1} instances of it";
	public static final String HeapObjectParamArgument_Msg_MatchingPattern =
			"{0} classes ({1} instances) are matching the pattern";
	public static final String HeapObjectParamArgument_Msg_ObjectFound = "Object 0x{0} not found";
	public static final String HeapObjectParamArgument_Msg_SearchingByPattern =
			"Looking up objects for class name pattern ''{0}''";
	public static final String Histogram_ClassLoaderStatistics = "CLASSLOADER STATISTICS";
	public static final String Histogram_ClassStatistics = "CLASS STATISTICS";
	public static final String Histogram_Column_ClassLoaderPerClass = "Class Loader / Class";
	public static final String Histogram_Column_PackagePerClass = "Package / Class";
	public static final String Histogram_Column_SuperclassPerClass = "Superclass / Class";
	public static final String Histogram_Description =
			"Histogram {0} with {1} class loaders, {2} classes, {3} objects, {4} used heap bytes:";
	public static final String Histogram_Difference = "Histogram difference between {0} and {1}";
	public static final String Histogram_Intersection = "Histogram intersection of {0} and {1}";
	public static final String HistogramQuery_GroupByClass = "Group by class";
	public static final String HistogramQuery_GroupByClassLoader = "Group by class loader";
	public static final String HistogramQuery_GroupBySuperclass = "Group by superclass";
	public static final String HistogramQuery_GroupByPackage = "Group by package";
	public static final String HistogramQuery_HistogramOf = "Histogram of {0}";
	public static final String HistogramQuery_IllegalArgument = "Illegal groupBy argument: {0}";
	public static final String HistogramRecordBeanInfo_Label = "Label";
	public static final String HistogramRecordBeanInfo_NumberOfObjects = "Number of Objects";
	public static final String HistogramRecordBeanInfo_RetainedHeapSize = "Retained Heap Size";
	public static final String HistogramRecordBeanInfo_UsedHeapSize = "Used Heap Size";
	public static final String ImmediateDominatorsQuery_Column_DominatedShallowHeap = "Dom. Shallow Heap";
	public static final String ImmediateDominatorsQuery_ColumnDominatedObjects = "Dom. Objects";
	public static final String ImmediateDominatorsQuery_DominatedObjects = "Dominated Objects";
	public static final String ImmediateDominatorsQuery_Objects = "Objects";
	public static final String InspectionAssert_NotSupported =
			"Dump format ''{0}'' does not support inspection ''{1}''.";
	public static final String JettyNameResolvers_JSPofWebApp = "JSPs of {0}";
	public static final String JettyRequestResolver_Collection = "Collection";
	public static final String JettyRequestResolver_Msg_ThreadExecutesHTTPRequest =
			"The thread is executing an HTTP Request to <strong>{0}</strong>.";
	public static final String JettyRequestResolver_Parameters = "Parameters";
	public static final String JettyRequestResolver_Summary = "Summary";
	public static final String JettyRequestResolver_URI = "URI";
	public static final String LeakHunterQuery_AccumulatedObjects = "Accumulated Objects in Dominator Tree";
	public static final String LeakHunterQuery_AccumulatedObjectsByClass =
			"Accumulated Objects by Class in Dominator Tree";
	public static final String LeakHunterQuery_AllAccumulatedObjectsByClass = "All Accumulated Objects by Class";
	public static final String LeakHunterQuery_BiggestInstances = "Biggest instances:";
	public static final String LeakHunterQuery_CommonPath = "Common Path To the Accumulation Point";
	public static final String LeakHunterQuery_Description = "Description";
	public static final String LeakHunterQuery_ErrorRetrievingRequestDetails = "Error retrieving request details";
	public static final String LeakHunterQuery_ErrorShortestPaths =
			"Error creating shortest paths to accumulation point.";
	public static final String LeakHunterQuery_FindingProblemSuspects = "Finding problem suspects";
	public static final String LeakHunterQuery_Hint = "Hint {0}";
	public static final String LeakHunterQuery_Keywords = "Keywords";
	public static final String LeakHunterQuery_LeakHunter = "Leak Hunter";
	public static final String LeakHunterQuery_Msg_AccumulatedBy =
			"The memory is accumulated in classloader/component <b>&quot;{0}&quot;</b>.";
	public static final String LeakHunterQuery_Msg_AccumulatedByInstance =
			"The memory is accumulated in one instance of <b>&quot;{0}&quot;</b> loaded by <b>&quot;{1}&quot;</b>.";
	public static final String LeakHunterQuery_Msg_AccumulatedByLoadedBy =
			"The memory is accumulated in class <b>&quot;{0}&quot;</b>, loaded by <b>&quot;{1}&quot;</b>.";
	public static final String LeakHunterQuery_Msg_Bytes = "{0} bytes. ";
	public static final String LeakHunterQuery_Msg_Class =
			"The class <b>&quot;{0}&quot;</b>, loaded by <b>&quot;{1}&quot;</b>, occupies <b>{2}</b> bytes. ";
	public static final String LeakHunterQuery_Msg_ClassLoader =
			"The classloader/component <b>&quot;{0}&quot;</b> occupies <b>{1}</b> bytes. ";
	public static final String LeakHunterQuery_Msg_Instance =
			"One instance of <b>&quot;{0}&quot;</b> loaded by <b>&quot;{1}&quot;</b> occupies <b>{2}</b> bytes. ";
	public static final String LeakHunterQuery_Msg_InstancesOccupy =
			"{0} instances of <b>&quot;{1}&quot;</b>, loaded by <b>&quot;{2}&quot;</b> occupy <b>{3}</b> bytes. ";
	public static final String LeakHunterQuery_Msg_ReferencedBy =
			"The instance is referenced by classloader/component. <b>&quot;{0}&quot;</b>. ";
	public static final String LeakHunterQuery_Msg_ReferencedByClass =
			"The instance is referenced by class <b>&quot;{0}&quot;</b>, loaded by <b>&quot;{1}&quot;</b>. ";
	public static final String LeakHunterQuery_Msg_ReferencedByInstance =
			"The instance is referenced by <b>{0}</b>&nbsp;, loaded by <b>&quot;{1}&quot;</b>. ";
	public static final String LeakHunterQuery_Msg_ReferencedFromClass =
			"These instances are referenced from the class <b>&quot;{0}&quot;</b>, loaded by <b>&quot;{1}&quot;</b>";
	public static final String LeakHunterQuery_Msg_ReferencedFromClassLoader =
			"These instances are referenced from classloader/component <b>&quot;{0}&quot;</b>";
	public static final String LeakHunterQuery_Msg_ReferencedFromInstance =
			"These instances are referenced from one instance of <b>&quot;{0}&quot;</b>, loaded by <b>&quot;{1}&quot;</b>";
	public static final String LeakHunterQuery_Msg_SuspectsRelated =
			"The problem suspects {0} and {1} may be related, because the reference chains to them have a common beginning.";
	public static final String LeakHunterQuery_Msg_Thread =
			"The thread <b>{0}</b> keeps local variables with total size <b>{1}</b> bytes.";
	public static final String LeakHunterQuery_NothingFound = "No leak suspect was found";
	public static final String LeakHunterQuery_Overview = "Overview";
	public static final String LeakHunterQuery_PreparingResults = "Preparing results";
	public static final String LeakHunterQuery_ProblemSuspect = "Problem Suspect {0}";
	public static final String LeakHunterQuery_ReferencePattern = "Reference Pattern";
	public static final String LeakHunterQuery_RequestDetails = "Request Details";
	public static final String LeakHunterQuery_SeeStackstrace = "See stacktrace";
	public static final String LeakHunterQuery_ShortestPaths = "Shortest Paths To the Accumulation Point";
	public static final String LeakHunterQuery_StackTraceAvailable = "The stacktrace of this Thread is available.";
	public static final String LeakHunterQuery_SystemClassLoader = "&lt;system class loader&gt;";
	public static final String LeakHunterQuery_ThreadDetails = "Thread Details";
	public static final String LeakHunterQuery_ThreadStack = "Thread Stack";
	public static final String LeakHunterQuery_TicketForSuspect = "{0} for &quot;{1}&quot;";
	public static final String LeakingPlugins_NoLeakingPlugInsDetected = "No leaking plug-ins detected.";
	public static final String MapCollisionRatioQuery_CalculatingCollisionRatios =
			"Calculating Map Collision Ratios...";
	public static final String MapCollisionRatioQuery_Column_CollisionRatio = "Collision Ratio";
	public static final String MapCollisionRatioQuery_Column_NumObjects = "\\# Objects";
	public static final String MapCollisionRatioQuery_ErrorMsg_ClassNotFound = "Class ''{0}'' not found in heap dump.";
	public static final String MapCollisionRatioQuery_ErrorMsg_MissingArgument =
			"If the collection argument is set to a custom (e.g. non-JDK) collection class, the size_attribute and array_attribute argument must be set. Otherwise, the query cannot calculate the collision ratio.";
	public static final String MapCollisionRatioQuery_IgnoringCollection = "Ignoring collection {0}";
	public static final String MATPlugin_InternalError = "Internal Error";
	public static final String MultiplePath2GCRootsQuery_Column_RefObjects = "Ref. Objects";
	public static final String MultiplePath2GCRootsQuery_Column_RefShallowHeap = "Ref. Shallow Heap";
	public static final String MultiplePath2GCRootsQuery_Group_FromGCRoots = "Merge Paths from GC Roots";
	public static final String MultiplePath2GCRootsQuery_Group_FromGCRootsOnClass =
			"Merge Paths from GC Roots on Class";
	public static final String MultiplePath2GCRootsQuery_Group_ToGCRoots = "Merge to GC Roots on Class";
	public static final String ObjectTreeFactory_Column_Percentage = "Percentage";
	public static final String ObjectTreeFactory_ErrorMsg_addChild =
			"\\#addChild must be called after a root object has been added.";
	public static final String ObjectTreeFactory_ErrorMsg_addChildren =
			"\\#addChildren must be called after a root object has been added.";
	public static final String OQLQuery_ExecutedQuery = "Executed Query:";
	public static final String OQLQuery_NoResult = "Your Query did not yield any result.";
	public static final String OQLQuery_ProblemReported = "Problem reported: ";
	public static final String ParseSnapshotApp_ErrorMsg_FileNotFound = "File not found: {0}";
	public static final String ParseSnapshotApp_ErrorMsg_ReportNotFound = "Report not found: {0}";
	public static final String ParseSnapshotApp_Usage = "Usage: [options] <snapshot> [(<report id>)*]";
	public static final String ParseSnapshotApp_MultipleSnapshotsDetail = "Snapshot identifier: {0} Information: {1}";
	public static final String ParseSnapshotApp_MultipleSnapshotsDetected =
			"Multiple heap dump snapshots have been detected in the file being analyzed. Use the 'runtime_identifier' option to select which snapshot to process.";
	public static final String PhantomReferenceStatQuery_Label_Referenced = "Histogram of Phantomly Referenced";
	public static final String PhantomReferenceStatQuery_Label_Retained = "Only Phantomly Retained";
	public static final String PhantomReferenceStatQuery_Label_StronglyRetainedReferents =
			"Referents strongly retained by phantom references";
	public static final String PieFactory_ErrorMsg_NoSnapshotAvailable =
			"No snapshot available. Use new PieFactory(snapshot) instead.";
	public static final String PieFactory_Label_Remainder = "Remainder";
	public static final String PieFactory_Label_RetainedSize = "Retained Size:";
	public static final String PieFactory_Label_ShallowSize = "Shallow Size:";
	public static final String PrimitiveArraysWithAConstantValueQuery_SearchingArrayValues =
			"Searching array values...";
	public static final String ReferenceQuery_ErrorMsg_NoMatchingClassesFound = "No classes matching pattern {0}";
	public static final String ReferenceQuery_Msg_ComputingReferentSet =
			"Computing Referent Set (objects referenced by the Reference objects)...";
	public static final String ReferenceQuery_Msg_ComputingRetainedSet =
			"Computing retained set of reference set (assuming only the referents are no longer referenced by the Reference objects)...";
	public static final String ReferenceQuery_Msg_ComputingStronglyRetainedSet =
			"Computing strongly retained set of reference set...";
	public static final String ReferenceQuery_HistogramOfReferentObjects = "Histogram of referent objects";
	public static final String ReferenceQuery_OnlyRetainedByReferents = "Only retained via referents";
	public static final String ReferenceQuery_StronglyRetainedByReferences =
			"Referents strongly retained by references";
	public static final String RetainedSetQuery_RetainedBy = "Retained by ''{0}''";
	public static final String RetainedSizeDerivedData_Error_IllegalContext =
			"Context provider ''{0}'' returned an illegal context object set for ''{1}'' with content ''{2}''. Return null instead.";
	public static final String RetainedSizeDerivedData_Error_IllegalObjectId =
			"Context provider ''{0}'' returned an context object with an illegeal object id for ''{1}''. Return null instead.";
	public static final String RetainedSizeDerivedData_ErrorMsg_IllegalContextObject =
			"Context provider ''{0}'' returned an illegal context object set for ''{1}}'' with content ''{{2}}'''. Return null instead.";
	public static final String RetainedSizeDerivedData_ErrorMsg_IllegalObjectId =
			"Context provider ''{0}'' returned an context object with an illegeal object id for ''{1}}''. Return null instead.";
	public static final String RetainedSizeDerivedData_Label_Approximate =
			"Calculate Minimum Retained Size (quick approx.)";
	public static final String RetainedSizeDerivedData_Label_Precise = "Calculate Precise Retained Size";
	public static final String Service_ErrorMsg_MismatchKeysServices =
			"Number of keys does not correspond to the number of values for the service: 0x{0}";
	public static final String SnapshotFactory_Error = "Error during creation of snapshot factory.";
	public static final String SnapshotFactory_ErrorMsg_FactoryCreation = "Error during creation of snapshot factory.";
	public static final String SnapshotQuery_ErrorMsg_NoResult = "Query {0} did not produce a result.";
	public static final String SnapshotQuery_ErrorMsg_QueryNotAvailable = "Query not available: {0}";
	public static final String SnapshotQuery_ErrorMsg_UnkownArgument = "Unknown argument: {0} for query {1}";
	public static final String SnapshotQuery_ErrorMsg_UnsuitableSubjects =
			"Query {0} has unsuitable Subjects {1} for the snapshot";
	public static final String SnapshotQuery_ErrorMsg_UnsupportedTyp =
			"Unsupported type for argument {0}: {1}\n(Use: IObject, Integer, int[], ArrayInt, IHeapObjectArgument)";
	public static final String SoftReferenceStatQuery_Label_Referenced = "Histogram of Softly Referenced";
	public static final String SoftReferenceStatQuery_Label_Retained = "Only Softly Retained";
	public static final String SoftReferenceStatQuery_Label_StronglyRetainedReferents =
			"Referents strongly retained by soft reference";
	public static final String SubjectRegistry_Error_MissingAnnotation =
			"Missing or empty @Subject(s) annotation: ''{0}''";
	public static final String SubjectRegistry_ErrorMsg_MissingSubjectAnnotation =
			"Missing or empty @Subject(s) annotation: ''{0}''";
	public static final String TaskInfo_Column_Id = "Id";
	public static final String TaskInfo_Column_Name = "Name";
	public static final String TaskInfo_Column_Number = "\\#";
	public static final String TaskInfo_Column_State = "State";
	public static final String TaskInfo_State_Idle = "idle";
	public static final String TaskInfo_State_NotApplicable = "N/A";
	public static final String TaskInfo_State_Processing = "processing";
	public static final String TaskInfo_State_Waiting = "waiting for a task";
	public static final String TaskInfo_State_WaitingSyncIO = "waiting for sync. I/O";
	public static final String ThreadInfoImpl_Column_ContextClassLoader = "Context Class Loader";
	public static final String ThreadInfoImpl_Column_IsDaemon = "Is Daemon";
	public static final String ThreadInfoImpl_Column_Instance = "Instance";
	public static final String ThreadInfoImpl_Column_Name = "Name";
	public static final String ThreadInfoQuery_Requests = "Requests";
	public static final String ThreadInfoQuery_ThreadDetails = "Thread Details";
	public static final String ThreadInfoQuery_ThreadLabel = "Thread {0}";
	public static final String ThreadInfoQuery_ThreadProperties = "Thread Properties";
	public static final String ThreadInfoQuery_ThreadStack = "Thread Stack";
	public static final String ThreadOverviewQuery_SearchingThreads = "Searching Threads...";
	public static final String ThreadOverviewQuery_ThreadDetails = "Thread Details";
	public static final String ThreadStackQuery_Column_ObjectStackFrame = "Object / Stack Frame";
	public static final String ThreadStackQuery_Label_Local = "<local>";
	public static final String ThreadStackQuery_Label_Local_Blocked_On = "<local, blocked on>";
	public static final String TopComponentsReportQuery_TopComponentReports = "Top Component Reports";
	public static final String TopConsumers2Query_BiggestClasses = "Biggest Top-Level Dominator Classes";
	public static final String TopConsumers2Query_BiggestClassesOverview =
			"Biggest Top-Level Dominator Classes (Overview)";
	public static final String TopConsumers2Query_BiggestClassLoaders = "Biggest Top-Level Dominator Class Loaders";
	public static final String TopConsumers2Query_BiggestClassLoadersOverview =
			"Biggest Top-Level Dominator Class Loaders (Overview)";
	public static final String TopConsumers2Query_BiggestObjects = "Biggest Objects";
	public static final String TopConsumers2Query_BiggestObjectsOverview = "Biggest Objects (Overview)";
	public static final String TopConsumers2Query_BiggestPackages = "Biggest Top-Level Dominator Packages";
	public static final String TopConsumers2Query_Column_Package = "Package";
	public static final String TopConsumers2Query_Column_RetainedHeapPercent = "Retained Heap, %";
	public static final String TopConsumers2Query_Column_TopDominators = "\\# Top Dominators";
	public static final String TopConsumers2Query_CreatingHistogram = "Creating histogram";
	public static final String TopConsumers2Query_GroupingByPackage = "Grouping by package";
	public static final String TopConsumers2Query_Label_all = "<all>";
	public static final String TopConsumers2Query_MsgNoObjects = "There are no objects matching the specified criteria";
	public static final String TopConsumers2Query_NoClassesBiggerThan = "No classes bigger than {0}%.";
	public static final String TopConsumers2Query_NoClassLoaderBiggerThan = "No class loader bigger than {0}%.";
	public static final String TopConsumers2Query_NoObjectsBiggerThan = "No objects bigger than {0}%.";
	public static final String TopConsumers2Query_TopConsumers = "Top Consumers";
	public static final String TopConsumersQuery_ColumnLabels =
			"package,  retained%,  retained bytes, \\#top-dominators";
	public static final String TQuantize_Label_GroupByClassLoader = "Group by class loader";
	public static final String TQuantize_Label_GroupByPackage = "Group by package";
	public static final String TQuantize_Label_GroupedByClassLoader = "Grouped ''{0}'' by class loader";
	public static final String TQuantize_Label_GroupedByPackage = "Grouped ''{0}'' by package";
	public static final String TQuantize_None = "<none>";
	public static final String VmInfoDescriptor_ErrorGettingArgumentErrorMsg =
			"Error get argument ''{0}'' of class ''{1}''";
	public static final String VmInfoDescriptor_UnableToAccessArgumentErrorMsg =
			"Unable to access argument ''{0}'' of class ''{1}''. Make sure the attribute is PUBLIC.";
	public static final String VmInfoDescriptor_WrongTypeErrorMsg =
			"Field {0} of {1} has advice {2} but is not of type {3}.";
	public static final String WasteInCharArraysQuery_CheckingCharArrays = "Checking char[]...";
	public static final String WeakReferenceStatQuery_Label_Referenced = "Histogram of Weakly Referenced";
	public static final String WeakReferenceStatQuery_Label_Retained = "Only Weakly Retained";
	public static final String WeakReferenceStatQuery_Label_StronglyRetainedReferents =
			"Referents strongly retained by weak references";

	private Messages() {
	}
}
