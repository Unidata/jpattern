# JPattern - Snobol4-Style Pattern Matching Primitives for Java

## Project Description

* Last Updated: January 29, 2014
* Latest Version: Jpattern 2.1
* Minimum JDK Level: JDK 1.5

The goal of the **jpattern** project is to provide
a reference implementation, in Java, of the Snobol4
primitives as implemented by Robert Dewar in Ada.

## Introduction

Most current programming languages provide some form of
pattern matching. As a rule, the pattern matching is
based on regular expressions. The Comit/Snobol3/Snobol4/Spitbol
programming languages have provided pattern matching but
based on a pattern matching paradigm that is strictly more
powerful than standard regular expressions.

The goal of this project is to make Snobol4-style pattern matching
available as a package for the Java programming language.
Rather than build such a package from scratch,
Rober Dewar's existing 
[Ada-based Gnat Spitbol Patterns code]
(http://gcc.gnu.org/onlinedocs/gcc-3.3.6/gnat_rm/The-GNAT-Library.html#The-GNAT-Library")
"Ada-based Gnat Spitbol Patterns code")
was converted to Java (Note this link may be out-of-date).
The result is generally consistent with that Ada package, although
some changes were made to conform to the capabilities and
limitations of the Java language. In light of the derived nature of this code,
the original GNAT license is assumed to cover this code.
The compiler, however is licensed under a BSD license.
See the <a target="_self" href="#License">License Section</a>
for more details.

## Status
This software should be considered to be in early Beta
stage.  Testing has been spotty at best.  While the author
is not currently using this software, he is supporting it,
and bug fixes and extensions are welcome.

## Download
This software is hosted on github and me be obtained
using the git command:
    git clone https://github.com/Unidata/jpattern.git

In addition, earlier versions may also be available here.
    http://www.unidata.ucar.edu/staff/dmh

Note that the <code>jpattern-x.x.jar</code> file is not
itself executable by java. It must be downloaded and
unpacked. An executable jar file (also named
<i>jpattern.jar</i>) is included in the set of extracted
files.

### Dependencies
* Java -- This interpreter requires Java 1.5 or later (it has been tested
through Java 1.7).

## Installation



The distribution contains an ant build.xml file
with the following major tasks defined.

* all: Compile the source and construct the jar file called
`jpattern.jar`
It is assumed that the JDK 1.5 (or later) bin directory
is in the PATH environment variable.

* clean: Delete all generated files.

* test: Perform testing in `src/test/java/jpattern/test`

The final product is the jar file called `jpattern.jar`.
For convenience, a version of that jar file is included.
It will have been compiled with jdk1.7.

## Testing
A set of tests is provided in the directory named
    src/test/java/jpatter/tests.
An ant build.xml file is provided in that directory.

The output of the tests is captured and compared to the expected output.
Any discrepancies are reported. 

The set of tests is admittedly sketchy; it is derived from various examples
from online, from the snobol4 book, and from bug reports. If anyone knows
of a more comprehensive set of pattern tests, please contact the author.

For more information see the files `doc/jpattern.html`
and `doc/refman.html`.

## The JPattern Pseudo-Compiler
The package jpattern.compiler supports the compilation of
string expressions into equivalent Pattern objects.  It is
capable of converting a string representation of a pattern
to equivalent Java code (via a command line interface) or to
a Pattern object at runtime (via an API).  Refer to the
reference manual for more details.

## Change Log

### Changes Incorporated into Version 2

Minor version levels are indicated in parentheses.

* (1) Fixed the breakx code and added new test cases.
* (1) Change jpattern.util.Error to jpattern.Failure
      to avoid conflict with java.lang.Error.
* (1) Refactored src file to jpattern/util and jpattern/compiler
      directories.
* (1) Added direct support for OUTPUT and INPUT variables
      (see the reference manual for details).

* (0) Fixed a number of foolish errors, some introduced in version 1.2
      (Thanks to Arjan Loeffen of Valid/Vision.). These include:
	+ The pattern `fence(arb &amp; "b") &amp; "c"`
		matched against subject "b" throws
		java.lang.ArrayIndexOutOfBoundsException: -2147483647 , while
		`fence(arb &amp; "b")` doesn't. 
	+ The pattern `len(1)` fails on subject "a".
	+ The pattern `"a" ** MATCH` on subject "a"`
	     throws java.lang.ArrayIndexOutOfBoundsException: -2147483647.
	+ The pattern `bal("()")` doesn't seem to work, while bal does.
* (0) Modified the test set files to all extend Test.java to handle
         the common code.
* (0) Did some significant refactoring.
* (0) Fixed errors in ParseArgs.java that did not handle missing
         arguments correctly.
* (0) Added backquote strings to pass through Java expressions
         for use during pattern construction.
* (0) Replaced the Function mechanism with a somewhat more general
         ExternalVariable mechanism.
* (0) Added an ExternalPattern mechanism to support user defined
         pattern objects in matching.
* (0) Divided doc/jpattern.html into jpattern.html and refman.html.
* (0) The structure of the classes and their use has been
         modified to look more like the java.util.regex structure.
* (0) The test cases have been moved from package <i>jpattern.test</i>
         to <i>test</i> so that they do not have implicit access
         to the jpattern package, thus being able to expose access errors.

### Changes Incorporated into Version 1

Minor version levels are indicated in parentheses.
* (0) This is the initial release

* (1) Modified the licensing to conform to Robert Dewar's request.
* (1) Changed the -java flag to be -tag.
* (1) Added  the -xmltag flag.
* (2) Added a manifest to jpattern.jar so it can be used in compiler mode.
* (2) Modified the distribution to include a pre-compiled jpattern.jar.

## Point of Contact
* Author: Dennis Heimbigner
* E-mail: dmh at unidata dot ucar dot edu

## License
The code is divided into two parts. The source code which is derived
directly from the Ada source code is licensed under that source's
license, which is essentially the LGPL. The compiler source code is
licensed under the BSD license.  See the file license.txt for more more
details.
