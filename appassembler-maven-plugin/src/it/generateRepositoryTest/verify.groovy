import java.io.*
import java.util.*


//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "basic-test" );

if ( !unixScriptFile.canRead() ) {
    throw new FileNotFoundException( "Could not find the basic-test file.: " + unixScriptFile );
}

def lines_to_check_in_unix_script = [
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/repositoryLayout-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS="-Xms16m"',
];

def lines_to_check_in_unix_script_marker = [
    false,
    false,
];

unixScriptFile.eachLine {
    line_content, line_number -> lines_to_check_in_unix_script.eachWithIndex {
        script_content, index -> if (line_content.equals(script_content)) {
            lines_to_check_in_unix_script_marker[index] = true;
        }
    }
}

lines_to_check_in_unix_script_marker.eachWithIndex {
    value, index -> if (!value) {
        throw new Exception("The expected content in " + unixScriptFile + " couldn't be found." + lines_to_check_in_unix_script[index]);
    }
}

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Generated repository exists! " + fileRepoFolder );
}

def jarFileRepoFolder = new File( fileRepoFolder, "repositoryLayout-test-1.0-SNAPSHOT.jar");
if ( jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Generated jar found! " + jarFileRepoFolder );
}

return true;
