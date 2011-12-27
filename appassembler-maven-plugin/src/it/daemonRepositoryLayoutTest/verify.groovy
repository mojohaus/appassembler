import java.io.*
import java.util.*


//The bin folder where to find the generated scripts.

def fileBinFolder = new File( basedir, "target/generated-resources/appassembler/jsw/daemon-1/bin/");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "daemon-1" );

if ( !unixScriptFile.canRead() ) {
    throw new FileNotFoundException( "Could not find the daemon-1 file.: " + unixScriptFile );
}

def lines_to_check_in_unix_script = [
    'APP_LONG_NAME="appassembler-maven-plugin daemonRepositoryLayout-test"',
    '# description: appassembler-maven-plugin daemonRepositoryLayout-test',
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


//ATTENTION:
//The contents of the windows daemon-1.bat file does not contain special information about
//the current application which will change for every application so we don't check the contents.

//conf folder.
def fileConfFolder = new File( basedir, "target/generated-resources/appassembler/jsw/daemon-1/conf/");


// Check the existence of the generated wrapper.conf file.
def wrapperConfFile = new File( fileConfFolder, "wrapper.conf" );


if ( !wrapperConfFile.canRead() ) {
    throw new FileNotFoundException( "Could not find the wrapper.conf file.: " + wrapperConfFile );
}

def lines_to_check_in_wrapper_script = [
    'wrapper.java.classpath.2=etc',
    'wrapper.java.classpath.3=%REPO_DIR%/daemonRepositoryLayout-test-1.0-SNAPSHOT.jar',
    'wrapper.app.parameter.1=org.codehaus.mojo.appassembler.example.helloworld.HelloWorld',
    'wrapper.ntservice.displayname=appassembler-maven-plugin daemonRepositoryLayout-test',
    'wrapper.ntservice.description=appassembler-maven-plugin daemonRepositoryLayout-test',
    
];

def lines_to_check_in_wrapper_script_marker = [
    false,
    false,
    false,
    false,
    false,
];

wrapperConfFile.eachLine {
    line_content, line_number -> lines_to_check_in_wrapper_script.eachWithIndex {
        script_content, index -> if (line_content.equals(script_content)) {
            lines_to_check_in_wrapper_script_marker[index] = true;
        }
    }
}

lines_to_check_in_wrapper_script_marker.eachWithIndex {
    value, index -> if (!value) {
        throw new Exception("The expected content in " + wrapperConfFile+ " couldn't be found." + lines_to_check_in_wrapper_script[index]);
    }
}

return true;
