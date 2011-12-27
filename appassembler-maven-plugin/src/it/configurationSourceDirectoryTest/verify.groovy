import java.io.*
import java.util.*


//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "basic-test" );

if ( !unixScriptFile.canRead() ) {
    throw new FileNotFoundException( "Could not find the basic-test file.: " + unixScriptFile );
}

// Check the existence of the generated windows script
File windowsBatchFile = new File( fileBinFolder, "basic-test.bat" );
if ( !windowsBatchFile.canRead() ) {
    throw new FileNotFoundException( "Could not find the basic-test.bat file.: " + windowsBatchFile );
}

def lines_to_check_in_unix_script = [
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/configurationSourceDirectory-test/1.0-SNAPSHOT/configurationSourceDirectory-test-1.0-SNAPSHOT.jar',
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
    
];

def lines_to_check_in_unix_script_marker = [
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
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


def lines_to_check_in_windows_script = [
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\configurationSourceDirectory-test\\1.0-SNAPSHOT\\configurationSourceDirectory-test-1.0-SNAPSHOT.jar',
    'set EXTRA_JVM_ARGUMENTS=-Xms16m',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="basic-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld arg1 arg2 %CMD_LINE_ARGS%',
];

def lines_to_check_in_windows_script_marker = [
    false,
    false,
    false,
];

windowsBatchFile.eachLine {
    line_content, line_number -> lines_to_check_in_windows_script.eachWithIndex {
        script_content, index -> if (line_content.equals(script_content)) {
            lines_to_check_in_windows_script_marker[index] = true;
        }
    }
}

lines_to_check_in_windows_script_marker.eachWithIndex {
    value, index -> if (!value) {
        throw new Exception("The expected content in " + windowsBatchFile + " couldn't be found." + lines_to_check_in_windows_script[index]);
    }
}

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def jarFileRepoFolder = new File( fileRepoFolder, "org/codehaus/mojo/appassembler-maven-plugin/it/configurationSourceDirectory-test/1.0-SNAPSHOT/configurationSourceDirectory-test-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}

def fileEtcFolder = new File( basedir, "target/appassembler/etc");

//Check for existence of the etc folder.
if ( !fileEtcFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated etc. " + fileEtcFolder );
}

def supplementalConfigurationFileInEtcFolder = new File( fileEtcFolder, "supplemental-config.properties");
if ( !supplementalConfigurationFileInEtcFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the supplemental-config.properties file. " + supplementalConfigurationFileInEtcFolder );
}


return true;
