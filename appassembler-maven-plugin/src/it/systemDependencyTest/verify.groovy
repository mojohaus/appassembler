/**
 *
 * The MIT License
 *
 * Copyright 2006-2011 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import java.io.*
import java.util.*

t = new IntegrationBase();


//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

def scriptfile_program_01_test = new File( fileBinFolder, 'program-01-test');

t.checkExistenceAndContentOfAFile(scriptfile_program_01_test, [
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/test/artifactId/1.0.0/artifactId-1.0.0.jar:"$REPO"/commons-logging/commons-logging-api/1.1/commons-logging-api-1.1.jar:"$REPO"/log4j/log4j/1.2.14/log4j-1.2.14.jar:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/systemDependency-test/1.0-SNAPSHOT/systemDependency-test-1.0-SNAPSHOT.jar',
    'exec "$JAVACMD" $JAVA_OPTS \\',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="program-01-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
]);

def scriptfile_program_01_test_bat = new File( fileBinFolder, 'program-01-test.bat');

t.checkExistenceAndContentOfAFile(scriptfile_program_01_test_bat, [
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\test\\artifactId\\1.0.0\\artifactId-1.0.0.jar;"%REPO%"\\commons-logging\\commons-logging-api\\1.1\\commons-logging-api-1.1.jar;"%REPO%"\\log4j\\log4j\\1.2.14\\log4j-1.2.14.jar;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\systemDependency-test\\1.0-SNAPSHOT\\systemDependency-test-1.0-SNAPSHOT.jar',
]);

def artifacts_in_repository = [
	'/test/artifactId/1.0.0/artifactId-1.0.0.jar',
	'/commons-logging/commons-logging-api/1.1/commons-logging-api-1.1.jar',
	'/log4j/log4j/1.2.14/log4j-1.2.14.jar',
	'/org/codehaus/mojo/appassembler-maven-plugin/it/systemDependency-test/1.0-SNAPSHOT/systemDependency-test-1.0-SNAPSHOT.jar',
];

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

artifacts_in_repository.each { artifact -> jarFileRepoFolder = new File( fileRepoFolder, artifact);
	if ( !jarFileRepoFolder.canRead() ) {
		throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
	}
}

return true;
