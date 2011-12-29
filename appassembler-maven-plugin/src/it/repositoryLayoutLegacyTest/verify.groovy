import java.io.*
import java.util.*


//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "basic-test" );

t = new IntegrationBase();

t.checkExistenceAndContentOfAFile(unixScriptFile, [
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org.codehaus.mojo.appassembler-maven-plugin.it/jars/repositoryLayoutLegacy-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS="-Xms16m"',
])

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def jarFolder = new File (fileRepoFolder, "org.codehaus.mojo.appassembler-maven-plugin.it/jars/");

def jarFileRepoFolder = new File( jarFolder, "repositoryLayoutLegacy-test-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}



return true;
