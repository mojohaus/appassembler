/**
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
package org.codehaus.mojo.appassembler;

import java.io.File;
import java.util.List;
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

    /**
     * Extra arguments which will be given the Main Class as arguments verbatim.
     * 
     * @parameter
     */
    private List commandLineArguments;

    /**
     * The License header which can be used instead of the default header.
     * 
     * @parameter
     * @since 1.2
     */
    private File licenseHeaderFile;

    /**
     * Define the name of binary folder.
     * 
     * @parameter default-value="bin"
     * @since 1.2
     */
    private File binFolder;

    /**
     * JvmSettings for every program.
     * 
     * @parameter
     * @since 1.2
     */
    private org.codehaus.mojo.appassembler.model.JvmSettings jvmSettings;

    /**
     * The platforms the plugin will generate bin files for.
     * Configure with string values - "all"(default/empty) | "windows" | "unix".
     * 
     * @parameter
     */
    private Set platforms;

    public Program ( )
    {
    }

    public Program ( String name, String mainClass )
    {
        this.name = name;
        this.mainClass = mainClass;
    }

    public String getName ()
    {
        return name;
    }

    public void setName ( String name )
    {
        this.name = name;
    }

    public String getMainClass ()
    {
        return mainClass;
    }

    public void setMainClass ( String mainClass )
    {
        this.mainClass = mainClass;
    }

    public Set getPlatforms ()
    {
        return platforms;
    }

    public void setPlatforms ( Set platforms )
    {
        this.platforms = platforms;
    }

    public List getCommandLineArguments ()
    {
        return this.commandLineArguments;
    }

    public void setCommandLineArguments ( List arguments )
    {
        this.commandLineArguments = arguments;
    }

    public void addCommandLineArgument ( String argument )
    {
        this.commandLineArguments.add ( argument );
    }

    public org.codehaus.mojo.appassembler.model.JvmSettings getJvmSettings ()
    {
        return jvmSettings;
    }

    public void setJvmSettings ( org.codehaus.mojo.appassembler.model.JvmSettings jvmSettings )
    {
        this.jvmSettings = jvmSettings;
    }

    public File getLicenseHeaderFile ()
    {
        return licenseHeaderFile;
    }

    public void setLicenseHeaderFile ( File licenseHeaderFile )
    {
        this.licenseHeaderFile = licenseHeaderFile;
    }

    public File getBinFolder ()
    {
        return binFolder;
    }

    public void setBinFolder ( File binFolder )
    {
        this.binFolder = binFolder;
    }

}
