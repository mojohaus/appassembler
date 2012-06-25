/**
 *
 * The MIT License
 *
 * Copyright 2006-2011 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import java.io.*
import java.util.*


t = new IntegrationBase();

//Check the existence of the generated repository.
def repoFolder1 = new File( basedir, "target/exec-1/repo");
def jarFile1 = new File( repoFolder1, "dbupgrade-core-1.0-beta-3-SNAPSHOT.jar" );

if ( jarFile1.canRead() ) {
    throw new RuntimeException( "Unexpected generated jar found! " + jarFile1 );
}

def repoFolder2 = new File( basedir, "target/exec-2/repo");
def jarFile2 = new File( repoFolder2, "dbupgrade-core-1.0-beta-3-SNAPSHOT.jar" );

if ( !jarFile2.canRead() ) {
    throw new FileNotFoundException( "Generated jar found! " + jarFile1 );
}

///////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////

def repoFolder3 = new File( basedir, "target/exec-3/repo");
def jarFile3 = new File( repoFolder3, "dbupgrade-core-1.0-beta-3-SNAPSHOT.jar" );

if ( !jarFile3.canRead() ) {
    throw new FileNotFoundException( "Generated jar found! " + jarFile1 );
}

//The bin folder where to find the generated scripts.
def fileBinFolder3 = new File( basedir, "target/exec-3/bin");

// Check the existence of the generated unix script
def unixScriptFile3 = new File( fileBinFolder3, "useTimestampInSnapshotFileNameTest" );

if ( !unixScriptFile3.canRead() ) {
    throw new FileNotFoundException( "Could not find: " + unixScriptFile3 );
}

// Check the existence of the generated windows script
File windowsBatchFile3 = new File( fileBinFolder3, "useTimestampInSnapshotFileNameTest.bat" );
if ( !windowsBatchFile3.canRead() ) {
    throw new FileNotFoundException( "Could not find: " + windowsBatchFile3 );
}

def list_of_files_which_must_be_in_classpath3 = [
    'dbupgrade-core-1.0-beta-3-SNAPSHOT.jar'
];


def line_unix_script_with_classpath3 = "";
unixScriptFile3.eachLine {
    line_content, line_number -> if (line_content.startsWith('CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc')) {
        line_unix_script_with_classpath3 = line_content;
    }
}

list_of_files_which_must_be_in_classpath3.each {
    lib_file -> if (!line_unix_script_with_classpath3.contains(lib_file)) {
        throw new FileNotFoundException("We couldn't found " + lib_file + " in classpath (unix script).");
    }
}

def line_windows_script_with_classpath3 = "";
windowsBatchFile3.eachLine {
    line_content, line_number -> if (line_content.startsWith(/set CLASSPATH="%BASEDIR%"\etc;/)) {
        line_windows_script_with_classpath3 = line_content;
    }
}

list_of_files_which_must_be_in_classpath3.each {
    lib_file -> if (!line_windows_script_with_classpath3.contains(lib_file)) {
        throw new FileNotFoundException("We couldn't found " + lib_file + " in classpath (windows script).");
    }
}

///////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////

def repoFolder4 = new File( basedir, "target/exec-4/jsw/useTimestampInSnapshotFileNameTest/lib");
def jarFile4 = new File( repoFolder4, "dbupgrade-core-1.0-beta-3-SNAPSHOT.jar" );

if ( !jarFile4.canRead() ) {
    throw new FileNotFoundException( "Generated jar found! " + jarFile4 );
}

//The bin folder where to find the generated scripts.
def fileConfFolder4 = new File( basedir, "target/exec-4/jsw/useTimestampInSnapshotFileNameTest/conf");

// Check the existence of the generated wrapper.conf script
def wrapperConfFile4 = new File( fileConfFolder4, "wrapper.conf" );

if ( !wrapperConfFile4.canRead() ) {
    throw new FileNotFoundException( "Could not find: " + wrapperConf4 );
}

wrapperConfFile4.eachLine {
    file_content -> if (file_content.startsWith('wrapper.java.classpath.3')) {
        def found = false;
        list_of_files_which_must_be_in_classpath3.each {
            lib_file -> if (file_content.contains(lib_file)) {
                found = true;
            }
            System.out.println( file_content );
        }
        if (!found) {
            //TODO: Make it better!
            throw new FileNotFoundException("We couldn't find an element!");
        }
    }
}

return true;