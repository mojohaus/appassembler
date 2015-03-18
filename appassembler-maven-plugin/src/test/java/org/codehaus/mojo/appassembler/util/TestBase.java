/**
 * The MIT License
 *
 * Copyright 2006-2012 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.codehaus.mojo.appassembler.util;

import java.io.File;

/**
 * This class exposes helper methods to do Unit testing in a Maven/Eclipse environment.
 *
 * @author <a href="mailto:info@soebes.de">Karl-Heinz Marbaise</a>
 */
public class TestBase
{
    /**
     * Return the base directory of the project.
     *
     * @return The base folder.
     */
    public static String getMavenBaseDir()
    {
        // basedir is defined by Maven
        // but the above will not work under Eclipse.
        // So there I'M using user.dir
        return System.getProperty( "basedir", System.getProperty( "user.dir", "." ) );
    }

    /**
     * Return the <code>target</code> directory of the current project.
     *
     * @return The target folder.
     */
    public static String getTargetDir()
    {
        return getMavenBaseDir() + File.separatorChar + "target" + File.separator;
    }

    /**
     * This will give you the <code>src</code> folder.
     *
     * @return The string
     */
    public static String getSrcDirectory()
    {
        return getMavenBaseDir() + File.separator + "src";
    }

    /**
     * This will give you the <code>src/test</code> folder.
     *
     * @return String representing the folder.
     */

    public static String getTestDirectory()
    {
        return getSrcDirectory() + File.separator + "test";
    }

    /**
     * This will give you the <code>src/test/resources</code> folder.
     *
     * @return The string representing the folder.
     */
    public static String getTestResourcesDirectory()
    {
        return getTestDirectory() + File.separator + "resources" + File.separator;
    }

}
