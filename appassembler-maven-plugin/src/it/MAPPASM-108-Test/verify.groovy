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


//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/appassembler/bin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "mappasm-108-test" );

if ( !unixScriptFile.canRead() ) {
    throw new FileNotFoundException( "Could not find the mappasm-108-test file.: " + unixScriptFile );
}

// Check the existence of the generated windows script
File windowsBatchFile = new File( fileBinFolder, "mappasm-108-test.bat" );
if ( !windowsBatchFile.canRead() ) {
    throw new FileNotFoundException( "Could not find the mappasm-108-test.bat file.: " + windowsBatchFile );
}

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/appassembler/repo");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def list_of_files_which_must_be_in_classpath = [
    'MAPPASM-108-test-1.0-SNAPSHOT.jar',
    'asm-3.1.jar',
    'bcmail-jdk15-1.45.jar',
    'bcprov-jdk15-1.45.jar',
    'commons-compress-1.0.jar',
    'commons-logging-1.1.1.jar',
    'dom4j-1.6.1.jar',
    'fontbox-1.1.0.jar',
    'geronimo-stax-api_1.0_spec-1.0.1.jar',
    'jempbox-1.1.0.jar',
    'log4j-1.2.14.jar',
    'metadata-extractor-2.4.0-beta-1.jar',
    'pdfbox-1.1.0.jar',
    'poi-3.6.jar',
    'poi-ooxml-3.6.jar',
    'poi-ooxml-schemas-3.6.jar',
    'poi-scratchpad-3.6.jar',
    'tagsoup-1.2.jar',
    'tika-core-0.7.jar',
    'tika-parsers-0.7.jar',
    'xml-apis-1.0.b2.jar',
    'xmlbeans-2.3.0.jar',
];


def line_unix_script_with_classpath = "";
unixScriptFile.eachLine {
    line_content, line_number -> if (line_content.startsWith('CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc')) {
        line_unix_script_with_classpath = line_content;
    }
}

list_of_files_which_must_be_in_classpath.each {
    lib_file -> if (!line_unix_script_with_classpath.contains(lib_file)) {
        throw new FileNotFoundException("We couldn't found " + lib_file + " in classpath (unix script).");
    }
}

def line_windows_script_with_classpath = "";
windowsBatchFile.eachLine {
    line_content, line_number -> if (line_content.startsWith('set CLASSPATH="%BASEDIR%"\\etc;')) {
        line_windows_script_with_classpath = line_content;
    }
}

list_of_files_which_must_be_in_classpath.each {
    lib_file -> if (!line_windows_script_with_classpath.contains(lib_file)) {
        throw new FileNotFoundException("We couldn't found " + lib_file + " in classpath (windows script).");
    }
}

def list_of_files_which_must_be_existing_in_repository = [
    'MAPPASM-108-test-1.0-SNAPSHOT.jar',
    'asm-3.1.jar',
    'bcmail-jdk15-1.45.jar',
    'bcprov-jdk15-1.45.jar',
    'commons-compress-1.0.jar',
    'commons-logging-1.1.1.jar',
    'dom4j-1.6.1.jar',
    'fontbox-1.1.0.jar',
    'geronimo-stax-api_1.0_spec-1.0.1.jar',
    'jempbox-1.1.0.jar',
    'log4j-1.2.14.jar',
    'maven-metadata-appassembler.xml',
    'metadata-extractor-2.4.0-beta-1.jar',
    'pdfbox-1.1.0.jar',
    'poi-3.6.jar',
    'poi-ooxml-3.6.jar',
    'poi-ooxml-schemas-3.6.jar',
    'poi-scratchpad-3.6.jar',
    'tagsoup-1.2.jar',
    'tika-core-0.7.jar',
    'tika-parsers-0.7.jar',
    'xml-apis-1.0.b2.jar',
    'xmlbeans-2.3.0.jar',
];

list_of_files_which_must_be_existing_in_repository.each {
    lib_file -> file = new File(fileRepoFolder, lib_file);
    if (!file.canRead()) {
        throw new FileNotFoundException("Could not find " + lib_file + " in generated repository.");
    }
}

return true;
