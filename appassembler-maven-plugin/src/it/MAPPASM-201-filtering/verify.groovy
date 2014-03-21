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

t = new IntegrationBase()

// The folder where we expect to find content copied from src/main/config
def configurationDirectory = new File( basedir, "target/appassembler/etc" );

if ( !configurationDirectory.canRead() ) {
    throw new FileNotFoundException( "Could not find the generated etc folder '" + configurationDirectory + "'." );
}

def emptyDirectory = new File( configurationDirectory, "emptyDirectory" );

if ( !emptyDirectory.canRead() ) {
    throw new FileNotFoundException( "Could not find the empty directory '" + emptyDirectory + "'." );
}

def configurationFile = new File( configurationDirectory, "config.txt" );

// Verify that the content is unfiltered and that encoding is working
t.checkExistenceAndContentOfAFile( configurationFile, [
        "This contains filtered values like 1.0-SNAPSHOT and customValue",
        "Here are some characters that require a correct encoding to work åäö",
] )

return true;
