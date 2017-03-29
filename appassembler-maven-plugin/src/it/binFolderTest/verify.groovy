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
def fileBinFolder = new File( basedir, "target/appassembler/xbin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "basic-test" );

t.checkExistenceAndContentOfAFile(unixScriptFile, [
    '[ -f "$BASEDIR"/xbin/setup-env ] && . "$BASEDIR"/xbin/setup-env',
    'CLASSPATH="$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/envrionmentSetup-test/1.0-SNAPSHOT/envrionmentSetup-test-1.0-SNAPSHOT.jar',
    'exec "$JAVACMD" -Xms16m $JAVA_OPTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="basic-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dapp.home="$BASEDIR" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  arg1 arg2 "$@"',
    
])

File windowsBatchFile = new File( fileBinFolder, "basic-test.bat" );

t.checkExistenceAndContentOfAFile(windowsBatchFile, [
    /if exist "%BASEDIR%\xbin\setup-env.bat" call "%BASEDIR%\xbin\setup-env.bat"/,
    /set CLASSPATH="%BASEDIR%"\etc;"%REPO%"\org\codehaus\mojo\appassembler-maven-plugin\it\envrionmentSetup-test\1.0-SNAPSHOT\envrionmentSetup-test-1.0-SNAPSHOT.jar/,
    /%JAVACMD% -Xms16m %JAVA_OPTS% -classpath %CLASSPATH% -Dapp.name="basic-test" -Dapp.repo="%REPO%" -Dapp.home="%BASEDIR%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld arg1 arg2 %CMD_LINE_ARGS%/,
])

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def jarFileRepoFolder = new File( fileRepoFolder, "org/codehaus/mojo/appassembler-maven-plugin/it/envrionmentSetup-test/1.0-SNAPSHOT/envrionmentSetup-test-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}

return true;
