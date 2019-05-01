# Appassembler Maven Plugin

This project contains the [Appassembler Maven Plugin project](http://www.mojohaus.org/appassembler/).

[![The MIT License](https://img.shields.io/github/license/mojohaus/appassembler.svg?label=License)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/appassembler-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.codehaus.mojo%22%20AND%20a%3A%22appassembler-maven-plugin%22)
[![Build Status](https://travis-ci.org/mojohaus/appassembler.svg?branch=master)](https://travis-ci.org/mojohaus/appassembler)
[![Build Status (AppVeyor)](https://ci.appveyor.com/api/projects/status/github/mojohaus/appassembler?branch=master&svg=true)](https://ci.appveyor.com/project/khmarbaise/appassembler)

## Overview

The Application Assembler Plugin is a Maven plugin for generating scripts for
starting java applications. All dependencies and the artifact of the project
itself are placed in a generated Maven repository in a defined assemble
directory. All artifacts (dependencies + the artifact from the project) are
added to the classpath in the generated bin scripts.

Supported platforms:

 * Unix-variants
 * Windows NT (Windows 9x is NOT supported)
 * Java Service Wrapper (JSW)

## Contributing

The first step is to create an appropriate [issue][issues]. Describe the
problem/idea you have and create an appropriate pull request.

Furthermore please get your commit message look like this:

```
Fixed #IssueNumber - Head line of the issue
 o Optional explanations if needed
```

Squash your pull request into a single commit to make
the later history easier to read. Apart from that
please check that all tests are working.


[issues]: https://github.com/mojohaus/appassembler/issues
