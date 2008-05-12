package org.codehaus.mojo.appassembler;

/*
 * The MIT License
 *
 * Copyright 2005-2007 The Codehaus.
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

import java.util.Set;

/**
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 * @deprecated Use generate-daemons instead
 */
public class Program
{
    private String name;

    private String mainClass;

    private Set platforms;
    
    public Program()
    {
    }

    public Program( String name, String mainClass )
    {
        this.name = name;
        this.mainClass = mainClass;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getMainClass()
    {
        return mainClass;
    }

    public void setMainClass( String mainClass )
    {
        this.mainClass = mainClass;
    }

    public Set getPlatforms()
    {
        return platforms;
    }

    public void setPlatforms( Set platforms )
    {
        this.platforms = platforms;
    }
    
}
