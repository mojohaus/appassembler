package org.codehaus.mojo.appassembler;

/*
 * The MIT License
 *
 * Copyright (c) 2006-2012, The Codehaus
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

/**
 * The class which is used to contain the JVM settings.
 * 
 * @author <a href="mailto:codehaus@soebes.de">Karl Heinz Marbaise</a>
 */
public class JvmSettings
{
    private String initialMemorySize;

    private String maxMemorySize;

    private String maxStackSize;

    private String[] systemProperties;

    private String[] extraArguments;

    /**
     * The initial memory size.
     * 
     * @return value as string.
     */
    public String getInitialMemorySize()
    {
        return initialMemorySize;
    }

    /**
     * The maximum memory size.
     * 
     * @return The max memory size.
     */
    public String getMaxMemorySize()
    {
        return maxMemorySize;
    }

    /**
     * Max Stack Size.
     * 
     * @return The max stack size.
     */
    public String getMaxStackSize()
    {
        return maxStackSize;
    }

    /**
     * The system properties.
     * 
     * @return The array with with the system properties.
     */
    public String[] getSystemProperties()
    {
        return systemProperties;
    }

    /**
     * Extra arguments.
     * 
     * @return The array with the extra arguments.
     */
    public String[] getExtraArguments()
    {
        return extraArguments;
    }
}
