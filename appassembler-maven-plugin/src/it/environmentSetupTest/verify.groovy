import java.io.*
import java.util.*


t = new IntegrationBase();

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "basic-test" );

t.checkExistenceAndContentOfAFile(unixScriptFile, [
    '[ -f "$BASEDIR"/bin/setup-env ] && . "$BASEDIR"/bin/setup-env',
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/envrionmentSetup-test/1.0-SNAPSHOT/envrionmentSetup-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS="-Xms16m"',
    'exec "$JAVACMD" $JAVA_OPTS \\',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="basic-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  arg1 arg2 "$@"',
    
])

File windowsBatchFile = new File( fileBinFolder, "basic-test.bat" );

t.checkExistenceAndContentOfAFile(windowsBatchFile, [
    'if exist "%BASEDIR%\\bin\\setup-env.bat" call "%BASEDIR%\\bin\\setup-env.bat"',
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\envrionmentSetup-test\\1.0-SNAPSHOT\\envrionmentSetup-test-1.0-SNAPSHOT.jar',
    'set EXTRA_JVM_ARGUMENTS=-Xms16m',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="basic-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld arg1 arg2 %CMD_LINE_ARGS%',
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
