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


t = new IntegrationBase()

//The bin folder where to find the generated scripts.
def fileBinFolder = new File( basedir, "target/generated-resources/appassembler/jsw/test-jsw/bin");

// Check the existence of the generated unix script
def unixScriptFile = new File( fileBinFolder, "test-jsw" );

t.checkExistenceAndContentOfAFile(unixScriptFile, [
    'APP_NAME="test-jsw"',
    'APP_LONG_NAME="appassembler-maven-plugin BasicTest"',
])

//Check the existence of the generated repository.
def fileRepoFolder = new File( basedir, "target/generated-resources/appassembler/jsw/test-jsw/lib");

if ( !fileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated repository. " + fileRepoFolder );
}

def jarFileRepoFolder = new File( fileRepoFolder, "/org/codehaus/mojo/appassembler-maven-plugin/it/preAssembleDirectoryCopyDaemon-test/1.0-SNAPSHOT/preAssembleDirectoryCopyDaemon-test-1.0-SNAPSHOT.jar");
if ( !jarFileRepoFolder.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated jar. " + jarFileRepoFolder );
}

//The staging folder where to find the copied src/main/runtime folder.
def stagingFolder = new File( basedir, "target/generated-resources/appassembler/jsw/test-jsw");


if (!stagingFolder.canRead()) {
	throw new FileNotFoundException( "Could not find the generated etc folder." + stagingFolder );
}

def fileSubDirFolder = new File (stagingFolder, "subdir");

if (!fileSubDirFolder.canRead()) {
	throw new FileNotFoundException( "Could not find the generated subdir folder." + fileSubDirFolder);
}

def fileSubDirFolderTestTxt = new File(fileSubDirFolder, "test.txt");
if (!fileSubDirFolderTestTxt.canRead()) {
	throw new FileNotFoundException( "Could not find the copied subdir/test.txt file." );
}

def fileSubDir1Folder = new File (stagingFolder, "subdir1");

if (!fileSubDir1Folder.canRead()) {
	throw new FileNotFoundException( "Could not find the generated subdir1 folder." + fileSubDir1Folder);
}

return true;
