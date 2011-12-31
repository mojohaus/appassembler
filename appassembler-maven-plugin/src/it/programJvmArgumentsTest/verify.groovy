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
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/programJvmArguments-test/1.0-SNAPSHOT/programJvmArguments-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS=""--port 1" "--option 2""',
    'exec "$JAVACMD" $JAVA_OPTS \\',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="program-01-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  arg1 arg2 "$@"',
]);

def scriptfile_program_01_test_bat = new File( fileBinFolder, 'program-01-test.bat');

t.checkExistenceAndContentOfAFile(scriptfile_program_01_test_bat, [
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\programJvmArguments-test\\1.0-SNAPSHOT\\programJvmArguments-test-1.0-SNAPSHOT.jar',
    'set EXTRA_JVM_ARGUMENTS="--port 1" "--option 2"',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="program-01-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld arg1 arg2 %CMD_LINE_ARGS%',
]);

def scriptfile_program_02_test = new File( fileBinFolder, 'program-02-test');

t.checkExistenceAndContentOfAFile(scriptfile_program_02_test, [
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/programJvmArguments-test/1.0-SNAPSHOT/programJvmArguments-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS="-Xms20M -Xmx256G"',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="program-02-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  test-environment "$@"',
]);


def scriptfile_program_02_test_bat = new File( fileBinFolder, 'program-02-test.bat');

t.checkExistenceAndContentOfAFile(scriptfile_program_02_test_bat, [
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\programJvmArguments-test\\1.0-SNAPSHOT\\programJvmArguments-test-1.0-SNAPSHOT.jar',
    'set EXTRA_JVM_ARGUMENTS=-Xms20M -Xmx256G',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="program-02-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld test-environment %CMD_LINE_ARGS%',
]);



def scriptfile_program_03_test = new File( fileBinFolder, 'program-03-test');

t.checkExistenceAndContentOfAFile(scriptfile_program_03_test, [
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/programJvmArguments-test/1.0-SNAPSHOT/programJvmArguments-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS="-Xms20m -Xmx256m -Xss128m -Dtest=false -Dlog4j.properties=false -Danton=2345 -Dberta="xaz""',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="program-03-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  test-environment "$@"',
]);


def scriptfile_program_03_test_bat = new File( fileBinFolder, 'program-03-test.bat');

t.checkExistenceAndContentOfAFile(scriptfile_program_03_test_bat, [
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\programJvmArguments-test\\1.0-SNAPSHOT\\programJvmArguments-test-1.0-SNAPSHOT.jar',
    'set EXTRA_JVM_ARGUMENTS=-Xms20m -Xmx256m -Xss128m -Dtest=false -Dlog4j.properties=false -Danton=2345 -Dberta="xaz"',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="program-03-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld test-environment %CMD_LINE_ARGS%',
]);


//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def jarFileRepoFolder = new File( fileRepoFolder, "org/codehaus/mojo/appassembler-maven-plugin/it/programJvmArguments-test/1.0-SNAPSHOT/programJvmArguments-test-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}

return true;
