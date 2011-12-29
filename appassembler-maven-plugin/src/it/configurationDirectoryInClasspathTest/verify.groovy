import java.io.*
import java.util.*


t = new IntegrationBase()

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "basic-test" );

t.checkExistenceAndContentOfAFile(unixScriptFile, [
    'CLASSPATH=$CLASSPATH_PREFIX:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/configurationDirectoryInClasspath-test/1.0-SNAPSHOT/configurationDirectoryInClasspath-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS="-Xms16m"',
])

File windowsBatchFile = new File( fileBinFolder, "basic-test.bat" );

t.checkExistenceAndContentOfAFile(windowsBatchFile, [
    'set CLASSPATH="%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\configurationDirectoryInClasspath-test\\1.0-SNAPSHOT\\configurationDirectoryInClasspath-test-1.0-SNAPSHOT.jar',
    'set EXTRA_JVM_ARGUMENTS=-Xms16m',
])

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def jarFileRepoFolder = new File( fileRepoFolder, "/org/codehaus/mojo/appassembler-maven-plugin/it/configurationDirectoryInClasspath-test/1.0-SNAPSHOT/configurationDirectoryInClasspath-test-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}




return true;
