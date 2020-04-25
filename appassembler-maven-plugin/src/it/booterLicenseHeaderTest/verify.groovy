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
def fileBinFolder = new File( basedir, "target/generated-resources/appassembler");

// booterLicenseHeaderTest
t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "booter-unix/bin/booterLicenseHeaderTest" ), [
    '# ******************************************',
    '# --- This is my own license header file ---',
    '# ******************************************',
    'exec "$JAVACMD" $JAVA_OPTS  \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="booterLicenseHeaderTest" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dapp.home="$BASEDIR" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.booter.AppassemblerBooter \\',
    '  "$@"',
])

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "booter-unix/etc/booterLicenseHeaderTest.xml" ), [
    '        <groupId>org.codehaus.mojo.appassembler-maven-plugin.it</groupId>',
    '        <artifactId>booterLicenseHeaderTest</artifactId>',
    '        <version>1.0-SNAPSHOT</version>',
    '        <relativePath>org/codehaus/mojo/appassembler-maven-plugin/it/booterLicenseHeaderTest/1.0-SNAPSHOT/booterLicenseHeaderTest-1.0-SNAPSHOT.jar</relativePath>',
])

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "booter-windows/bin/booterLicenseHeaderTest.bat" ), [
    '@REM ******************************************',
    '@REM --- This is my own license header file ---',
    '@REM ******************************************',
    '%JAVACMD% %JAVA_OPTS%  -classpath %CLASSPATH% -Dapp.name="booterLicenseHeaderTest" -Dapp.repo="%REPO%" -Dapp.home="%BASEDIR%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.booter.AppassemblerBooter %CMD_LINE_ARGS%',
])

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "booter-windows/etc/booterLicenseHeaderTest.xml" ), [
    '        <groupId>org.codehaus.mojo.appassembler-maven-plugin.it</groupId>',
    '        <artifactId>booterLicenseHeaderTest</artifactId>',
    '        <version>1.0-SNAPSHOT</version>',
    '        <relativePath>org/codehaus/mojo/appassembler-maven-plugin/it/booterLicenseHeaderTest/1.0-SNAPSHOT/booterLicenseHeaderTest-1.0-SNAPSHOT.jar</relativePath>',
    ])

// booterLicenseHeaderTest-01
t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "booter-unix/bin/booterLicenseHeaderTest-01"), [
    '# ******************************************',
    '# --- This is LicenseHeader File 01      ---',
    '# ******************************************',
    'exec "$JAVACMD" $JAVA_OPTS  \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="booterLicenseHeaderTest-01" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dapp.home="$BASEDIR" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.booter.AppassemblerBooter \\',
    '  "$@"',
])

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "booter-unix/etc/booterLicenseHeaderTest-01.xml" ), [
    '        <groupId>org.codehaus.mojo.appassembler-maven-plugin.it</groupId>',
    '        <artifactId>booterLicenseHeaderTest</artifactId>',
    '        <version>1.0-SNAPSHOT</version>',
    '        <relativePath>org/codehaus/mojo/appassembler-maven-plugin/it/booterLicenseHeaderTest/1.0-SNAPSHOT/booterLicenseHeaderTest-1.0-SNAPSHOT.jar</relativePath>',
    ])

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "booter-windows/bin/booterLicenseHeaderTest-01.bat"), [
    '@REM ******************************************',
    '@REM --- This is LicenseHeader File 01      ---',
    '@REM ******************************************',
    '%JAVACMD% %JAVA_OPTS%  -classpath %CLASSPATH% -Dapp.name="booterLicenseHeaderTest-01" -Dapp.repo="%REPO%" -Dapp.home="%BASEDIR%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.booter.AppassemblerBooter %CMD_LINE_ARGS%',
])

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "booter-windows/etc/booterLicenseHeaderTest-01.xml" ), [
    '        <groupId>org.codehaus.mojo.appassembler-maven-plugin.it</groupId>',
    '        <artifactId>booterLicenseHeaderTest</artifactId>',
    '        <version>1.0-SNAPSHOT</version>',
    '        <relativePath>org/codehaus/mojo/appassembler-maven-plugin/it/booterLicenseHeaderTest/1.0-SNAPSHOT/booterLicenseHeaderTest-1.0-SNAPSHOT.jar</relativePath>',
    ])

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def jarFileRepoFolder = new File( fileRepoFolder, "org/codehaus/mojo/appassembler-maven-plugin/it/booterLicenseHeaderTest/1.0-SNAPSHOT/booterLicenseHeaderTest-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}

return true;
