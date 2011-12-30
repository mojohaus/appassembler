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
class IntegrationBase {
    
    void checkExistenceAndContentOfAFile(file, contents) {
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
    
    
}