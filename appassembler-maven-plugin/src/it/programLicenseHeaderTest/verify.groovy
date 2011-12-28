import java.io.*
import java.util.*


//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "programLicenseHeader-test" );

if ( !unixScriptFile.canRead() ) {
    throw new FileNotFoundException( "Could not find the basic-test file.: " + unixScriptFile );
}

// Check the existence of the generated windows script
File windowsBatchFile = new File( fileBinFolder, "programLicenseHeader-test.bat" );
if ( !windowsBatchFile.canRead() ) {
    throw new FileNotFoundException( "Could not find the basic-test.bat file.: " + windowsBatchFile );
}

def lines_to_check_in_unix_script = [
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
    '@REM ******************************************',
    '@REM --- This is my own license header file ---',
    '@REM ******************************************',
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\programLicenseHeader-test\\1.0-SNAPSHOT\\programLicenseHeader-test-1.0-SNAPSHOT.jar',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="programLicenseHeader-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld arg1 arg2 %CMD_LINE_ARGS%',
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

def jarFileRepoFolder = new File( fileRepoFolder, "org/codehaus/mojo/appassembler-maven-plugin/it/programLicenseHeader-test/1.0-SNAPSHOT/programLicenseHeader-test-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}

return true;
