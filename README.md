# pepper

Various Java utilities

[![Build Status](https://travis-ci.org/solven-eu/pepper.svg?branch=master)](https://travis-ci.org/solven-eu/pepper)
[![Coverage Status](https://coveralls.io/repos/github/solven-eu/pepper/badge.svg?branch=master)](https://coveralls.io/github/solven-eu/pepper?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.solven-eu.pepper/pepper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.solven-eu.pepper/pepper/badge.svg)
[![Quality Gate](https://sonarqube.com/api/badges/gate?key=com.github.solven-eu.pepper:pepper)](https://sonarqube.com/dashboard/index/com.github.solven-eu.pepper:pepper)
[![Technical debt ratio](https://sonarqube.com/api/badges/measure?key=com.github.solven-eu.pepper:pepper&metric=sqale_debt_ratio)](https://sonarqube.com/dashboard/index/com.github.solven-eu.pepper:pepper)
[![javadoc.io](https://javadoc-emblem.rhcloud.com/doc/com.github.solven-eu.pepper/pepper/badge.svg)](http://www.javadoc.io/doc/com.github.solven-eu.pepper/pepper)
[![Issues](https://img.shields.io/github/issues/solven-eu/pepper.svg)](https://github.com/revelc/pepper/issues)
[![Forks](https://img.shields.io/github/forks/solven-eu/pepper.svg)](https://github.com/solven-eu/pepper/network)
[![Stars](https://img.shields.io/github/stars/solven-eu/pepper.svg)](https://github.com/solven-eu/pepper/stargazers)
[![MIT License](http://img.shields.io/badge/license-ASL-blue.svg)](https://github.com/solven-eu/pepper/blob/master/LICENSE)

# Maven

Add pepper as dependency to your project through:

        <dependency>
             <groupId>io.github.solven-eu.pepper</groupId>
             <artifactId>pepper</artifactId>
             <version>${pepper.version}</version>
        </dependency>

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

It were useful to investigate memory issues in [Heroku](https://devcenter.heroku.com/articles/getting-started-with-java).

# MapPath

This module inspires itself from [xpath](https://en.wikipedia.org/wiki/XPath) and [jsonPath](https://github.com/json-path/JsonPath) to manipulate standard `java.util.Map`.

See [map-path](./map-path)

```xml
<dependency>
	<groupId>io.github.solven-eu.pepper</groupId>
	<artifactId>map-path</artifactId>
	<version>4.4</version>
</dependency>
```

# Upgrade from 2.X to 3.X

Change groupId from `<groupId>com.github.cormoran-io.pepper</groupId>` to `<groupId>io.github.solven-eu.pepper</groupId>`
Change imported package from `import cormoran.pepper.X` to `import eu.solven.pepper.X`

# Upgrade from 4.X to 5.X

Pepper requires a JDK17 from 5.0

# Deprecated

## MAT

We use to maintain a fork of [MAT](https://github.com/eclipse-mat/mat), dedicated to huge Heap-Analysis. This is still available in [4.x MAT](https://github.com/solven-eu/pepper/tree/4.x/mat)

## Spark

We use to maintain a set of projects demonstrating how to prepare a Spark-based job, as a SpringBoot application, workarounding various issues like excluding some libraries already provided by the Hadoop ecosystem. This is still available in [4.x Spark](https://github.com/solven-eu/pepper/blob/4.x/hadoop/spark/spark-springboot/README.MD)
