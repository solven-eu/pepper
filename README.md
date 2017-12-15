# pepper
Various Java utilities

[![Build Status](https://travis-ci.org/cormoran-io/pepper.svg?branch=master)](https://travis-ci.org/cormoran-io/pepper)
[![Coverage Status](https://coveralls.io/repos/github/cormoran-io/pepper/badge.svg?branch=master)](https://coveralls.io/github/cormoran-io/pepper?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.cormoran-io.pepper/pepper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.cormoran-io.pepper/pepper)
[![Quality Gate](https://sonarqube.com/api/badges/gate?key=com.github.cormoran-io.pepper:pepper)](https://sonarqube.com/dashboard/index/com.github.cormoran-io.pepper:pepper)
[![Technical debt ratio](https://sonarqube.com/api/badges/measure?key=com.github.cormoran-io.pepper:pepper&metric=sqale_debt_ratio)](https://sonarqube.com/dashboard/index/com.github.cormoran-io.pepper:pepper)
[![javadoc.io](https://javadoc-emblem.rhcloud.com/doc/com.github.cormoran-io.pepper/pepper/badge.svg)](http://www.javadoc.io/doc/com.github.cormoran-io.pepper/pepper)
[![Issues](https://img.shields.io/github/issues/cormoran-io/pepper.svg)](https://github.com/revelc/pepper/issues)
[![Forks](https://img.shields.io/github/forks/cormoran-io/pepper.svg)](https://github.com/cormoran-io/pepper/network)
[![Stars](https://img.shields.io/github/stars/cormoran-io/pepper.svg)](https://github.com/cormoran-io/pepper/stargazers)
[![MIT License](http://img.shields.io/badge/license-ASL-blue.svg)](https://github.com/cormoran-io/pepper/blob/master/LICENSE)

# pepper-java

Various utilities helping operating in Java on a daily basis.

## Standard helpers
GCInspector is drop-in class providing standard logs related to GC activity
```
  @Bean
	public IPepperThreadDumper pepperThreadDumper() {
		return new PepperThreadDump(ManagementFactory.getThreadMXBean());
	}

	@Bean
	public GCInspector gcInspector(IpepperThreadDumper pepperThreadDumper) {
		return new GCInspector(pepperThreadDumper);
	}
```
PepperLogHelper helps publishing relevant logs regarding memory and timings
```
  Assert.assertEquals("0.09%", pepperLogHelper.getNicePercentage(123, 123456).toString());
  
  
  Assert.assertEquals("9sec 600ms", pepperLogHelper.getNiceTime(9600).toString());
  Assert.assertEquals("2min 11sec", pepperLogHelper.getNiceTime(131, TimeUnit.SECONDS).toString());
  
  
  Assert.assertEquals("789B", pepperLogHelper.getNiceMemory(789L).toString());
  Assert.assertEquals("607KB", pepperLogHelper.getNiceMemory(789L * 789).toString());
  Assert.assertEquals("468MB", pepperLogHelper.getNiceMemory(789L * 789 * 789).toString());
  Assert.assertEquals("360GB", pepperLogHelper.getNiceMemory(789L * 789 * 789 * 789).toString());
  Assert.assertEquals("278TB", pepperLogHelper.getNiceMemory(789L * 789 * 789 * 789 * 789).toString());
  Assert.assertEquals("214PB", pepperLogHelper.getNiceMemory(789L * 789 * 789 * 789 * 789 * 789).toString());
```

## Fancy helpers
ObjectInputHandlingInputStream enables transmitting a raw InputStream through an ObjectInput

CartesianProductHelper helps computing covering cartesian products over sets defined by Collections and Maps.

PepperProcessHelper enables tracking the memory consumption of a process (would it be current JVM, a forked Process or any other process).
```
  PepperProcessHelper.getProcessResidentMemory(processPID)
```

# Pepper-MAT
A fork from Eclipse MAT for HeapDump analysis. It improves original MAT by lowering the heap required to prepare MAT index files, while keeping the produced indexes compatible with the original MAT.

Original work:
https://git.eclipse.org/c/mat/org.eclipse.mat.git

