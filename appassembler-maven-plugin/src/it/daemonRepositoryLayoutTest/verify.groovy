import java.io.*
import java.util.*


t = new IntegrationBase();

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/generated-resources/appassembler/jsw/daemon-1/bin/");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "daemon-1" );

t.checkExistenceAndContentOfAFile(unixScriptFile, [
    'APP_LONG_NAME="appassembler-maven-plugin daemonRepositoryLayout-test"',
    '# description: appassembler-maven-plugin daemonRepositoryLayout-test',
])

//ATTENTION:
//The contents of the windows daemon-1.bat file does not contain special information about
//the current application which will change for every application so we don't check the contents.

//conf folder.
def fileConfFolder = new File( basedir, "target/generated-resources/appassembler/jsw/daemon-1/conf/");


// Check the existence of the generated wrapper.conf file.
def wrapperConfFile = new File( fileConfFolder, "wrapper.conf" );

t.checkExistenceAndContentOfAFile(wrapperConfFile, [
    'wrapper.java.classpath.2=etc',
    'wrapper.java.classpath.3=%REPO_DIR%/daemonRepositoryLayout-test-1.0-SNAPSHOT.jar',
    'wrapper.app.parameter.1=org.codehaus.mojo.appassembler.example.helloworld.HelloWorld',
    'wrapper.ntservice.displayname=appassembler-maven-plugin daemonRepositoryLayout-test',
    'wrapper.ntservice.description=appassembler-maven-plugin daemonRepositoryLayout-test',
    
])

return true;
