import java.io.*
import java.util.*

def checkExistenceAndContentOfAFile(file, contents) {
    if (!file.canRead()) {
        throw new FileNotFoundException( "Could not find the " + file);
    }

    def lines_to_check_in_unix_script_marker = [:];
    (1..contents.size()).each {
        index -> lines_to_check_in_unix_script_marker[index] = false
    }
    
    file.eachLine {
        file_content, file_line -> contents.eachWithIndex {
            contents_expected, index -> if (file_content.equals(contents_expected)) {
                lines_to_check_in_unix_script_marker[index] = true;
            }
        }
    }

    contents.eachWithIndex {
        value, index -> if ( lines_to_check_in_unix_script_marker[index] == false ) {
            throw new Exception("The expected content in " + file + " couldn't be found." + contents[index]);
        }
    }
}

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

def scriptfile_program_01_test = new File( fileBinFolder, 'program-01-test');

def scriptfile_program_01_test_content = [
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/programJvmArguments-test/1.0-SNAPSHOT/programJvmArguments-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS=""--port 1" "--option 2""',
    'exec "$JAVACMD" $JAVA_OPTS \\',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="program-01-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  arg1 arg2 "$@"',
    
];

checkExistenceAndContentOfAFile(scriptfile_program_01_test, scriptfile_program_01_test_content);

def scriptfile_program_01_test_bat = new File( fileBinFolder, 'program-01-test.bat');

def scriptfile_program_01_test_bat_content = [
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\programJvmArguments-test\\1.0-SNAPSHOT\\programJvmArguments-test-1.0-SNAPSHOT.jar',
    'set EXTRA_JVM_ARGUMENTS="--port 1" "--option 2"',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="program-01-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld arg1 arg2 %CMD_LINE_ARGS%',
];

checkExistenceAndContentOfAFile(scriptfile_program_01_test_bat, scriptfile_program_01_test_bat_content);


def scriptfile_program_02_test = new File( fileBinFolder, 'program-02-test');

def scriptfile_program_02_test_content = [
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/programJvmArguments-test/1.0-SNAPSHOT/programJvmArguments-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS="-Xms20M -Xmx256G"',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="program-02-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  test-environment "$@"',
];

checkExistenceAndContentOfAFile(scriptfile_program_02_test, scriptfile_program_02_test_content);


def scriptfile_program_02_test_bat = new File( fileBinFolder, 'program-02-test.bat');

def scriptfile_program_02_test_bat_content = [
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\programJvmArguments-test\\1.0-SNAPSHOT\\programJvmArguments-test-1.0-SNAPSHOT.jar',
    'set EXTRA_JVM_ARGUMENTS=-Xms20M -Xmx256G',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="program-02-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld test-environment %CMD_LINE_ARGS%',
];

checkExistenceAndContentOfAFile(scriptfile_program_02_test_bat, scriptfile_program_02_test_bat_content);



def scriptfile_program_03_test = new File( fileBinFolder, 'program-03-test');

def scriptfile_program_03_test_content = [
    'CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/codehaus/mojo/appassembler-maven-plugin/it/programJvmArguments-test/1.0-SNAPSHOT/programJvmArguments-test-1.0-SNAPSHOT.jar',
    'EXTRA_JVM_ARGUMENTS="-Xms16m"',
    '  $EXTRA_JVM_ARGUMENTS \\',
    '  -classpath "$CLASSPATH" \\',
    '  -Dapp.name="program-03-test" \\',
    '  -Dapp.pid="$$" \\',
    '  -Dapp.repo="$REPO" \\',
    '  -Dbasedir="$BASEDIR" \\',
    '  org.codehaus.mojo.appassembler.example.helloworld.HelloWorld \\',
    '  test-environment "$@"',
];

checkExistenceAndContentOfAFile(scriptfile_program_03_test, scriptfile_program_03_test_content);


def scriptfile_program_03_test_bat = new File( fileBinFolder, 'program-03-test.bat');

def scriptfile_program_03_test_bat_content = [
    'set CLASSPATH="%BASEDIR%"\\etc;"%REPO%"\\org\\codehaus\\mojo\\appassembler-maven-plugin\\it\\programJvmArguments-test\\1.0-SNAPSHOT\\programJvmArguments-test-1.0-SNAPSHOT.jar',
    'set EXTRA_JVM_ARGUMENTS=-Xms16m',
    '%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="program-03-test" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.codehaus.mojo.appassembler.example.helloworld.HelloWorld test-environment %CMD_LINE_ARGS%',
];

checkExistenceAndContentOfAFile(scriptfile_program_03_test_bat, scriptfile_program_03_test_bat_content);


//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def jarFileRepoFolder = new File( fileRepoFolder, "org/codehaus/mojo/appassembler-maven-plugin/it/programJvmArguments-test/1.0-SNAPSHOT/programJvmArguments-test-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}

return true;
