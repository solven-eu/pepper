https://wiki.eclipse.org/MemoryAnalyzer/FAQ#Enable_Debug_Output

vim MemoryAnalyzer.ini
-debug -consoleLog

./ParseHeapDump.sh ../heapdump_22385.hprof
./ParseHeapDump.sh -keep_unreachable_objects ../heapdump_22385.hprof