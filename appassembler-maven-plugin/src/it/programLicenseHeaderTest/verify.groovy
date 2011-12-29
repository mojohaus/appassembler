import java.io.*
import java.util.*


t = new IntegrationBase();

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "programLicenseHeader-test" ), [
    '# ******************************************',
    '# --- This is my own license header file ---',
    '# ******************************************',
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/programLicenseHeader-test/1.0-SNAPSHOT/programLicenseHeader-test-1.0-SNAPSHOT.jar',
    'exec "$JAVACMD" $JAVA_OPTS \\',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="programLicenseHeader-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  arg1 arg2 "$@"',
])

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "programLicenseHeader-test.bat" ), [
    '@REM ******************************************',
    '@REM --- This is my own license header file ---',
    '@REM ******************************************',
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\programLicenseHeader-test\\1.0-SNAPSHOT\\programLicenseHeader-test-1.0-SNAPSHOT.jar',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="programLicenseHeader-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld arg1 arg2 %CMD_LINE_ARGS%',
])

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "programLicenseHeader-01-test"), [
    '# ******************************************',
    '# --- This is LicenseHeader File 01      ---',
    '# ******************************************',
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/programLicenseHeader-test/1.0-SNAPSHOT/programLicenseHeader-test-1.0-SNAPSHOT.jar',
    'exec "$JAVACMD" $JAVA_OPTS \\',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="programLicenseHeader-01-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  "$@"',
])

t.checkExistenceAndContentOfAFile(new File( fileBinFolder, "programLicenseHeader-01-test.bat"), [
    '@REM ******************************************',
    '@REM --- This is LicenseHeader File 01      ---',
    '@REM ******************************************',
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\programLicenseHeader-test\\1.0-SNAPSHOT\\programLicenseHeader-test-1.0-SNAPSHOT.jar',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="programLicenseHeader-01-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld %CMD_LINE_ARGS%',
])

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def jarFileRepoFolder = new File( fileRepoFolder, "org/codehaus/mojo/appassembler-maven-plugin/it/programLicenseHeader-test/1.0-SNAPSHOT/programLicenseHeader-test-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}

return true;
