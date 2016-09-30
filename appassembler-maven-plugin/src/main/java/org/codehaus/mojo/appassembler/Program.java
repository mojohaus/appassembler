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

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @version $Id$
 * @deprecated Use generate-daemons instead
 */
public class Program
{
    private String name;

    private String id;

    private String mainClass;

    /**
     * Extra arguments which will be given the Main Class as arguments verbatim.
     */
    @Parameter
    private List<String> commandLineArguments;

    /**
     * The License header which can be used instead of the default header.
     *
     * @since 1.2
     */
    @Parameter
    private File licenseHeaderFile;

    /**
     * Define the name of binary folder.
     *
     * @since 1.2
     */
    @Parameter( defaultValue = "bin" )
    private File binFolder;

    /**
     * JvmSettings for every program.
     *
     * @since 1.2
     */
    @Parameter
    private org.codehaus.mojo.appassembler.model.JvmSettings jvmSettings;

    /**
     * The platforms the plugin will generate bin files for. Configure with string values - "all"(default/empty) |
     * "windows" | "unix".
     */
    @Parameter
    private Set<String> platforms;

    /**
     * Show console window when execute this application. When false, the generated java command runs in background.
     * This works best for Swing application where the command line invocation is not blocked.
     */
    @Parameter
    private Boolean showConsoleWindow;

    /**
     * The default constructor.
     */
    public Program()
    {
    }

    /**
     * The constructor.
     *
     * @param name The name of the program.
     * @param mainClass The main class of the program.
     */
    public Program( String name, String mainClass )
    {
        this.name = name;
        this.mainClass = mainClass;
    }

    /**
     * The name.
     *
     * @return The name of the program.
     * @deprecated Please use @{link {@link #getId()} instead.
     */
    public String getName()
    {
        return name;
    }

    /**
     * The id.
     *
     * @return The id of the program.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Set the name.
     *
     * @param name The name of the program.
     * @deprecated Use {@link #setId(String)} instead.
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Set the id.
     *
     * @param id
     */
    public void setId( String id )
    {
        this.id = id;
    }

    /**
     * Get the main class.
     *
     * @return The name of the main class.
     */
    public String getMainClass()
    {
        return mainClass;
    }

    /**
     * Set the main class.
     *
     * @param mainClass The name of the main class.
     */
    public void setMainClass( String mainClass )
    {
        this.mainClass = mainClass;
    }

    /**
     * The platforms.
     *
     * @return The set of platforms.
     */
    public Set<String> getPlatforms()
    {
        return platforms;
    }

    /**
     * The platforms.
     *
     * @param platforms The set with the platforms.
     */
    public void setPlatforms( Set<String> platforms )
    {
        this.platforms = platforms;
    }

    /**
     * Get the command line arguments.
     *
     * @return The list of command line arguments.
     */
    public List<String> getCommandLineArguments()
    {
        return this.commandLineArguments;
    }

    /**
     * Set the argument list.
     *
     * @param arguments The list of command line arguments.
     */
    public void setCommandLineArguments( List<String> arguments )
    {
        this.commandLineArguments = arguments;
    }

    /**
     * Add an command line arguments.
     *
     * @param argument The argument which will be aded to list of arguments.
     */
    public void addCommandLineArgument( String argument )
    {
        this.commandLineArguments.add( argument );
    }

    /**
     * Get the JVM settings.
     *
     * @return An instance of the JVM settings.
     * @see JvmSettings
     */
    public org.codehaus.mojo.appassembler.model.JvmSettings getJvmSettings()
    {
        return jvmSettings;
    }

    /**
     * Set the JVM settings.
     *
     * @param jvmSettings The instance of the JVM settings which will be used.
     */
    public void setJvmSettings( org.codehaus.mojo.appassembler.model.JvmSettings jvmSettings )
    {
        this.jvmSettings = jvmSettings;
    }

    /**
     * Get the current license header file which is used.
     *
     * @return The file instance of the header file.
     */
    public File getLicenseHeaderFile()
    {
        return licenseHeaderFile;
    }

    /**
     * Set the license header file.
     *
     * @param licenseHeaderFile The File instance.
     */
    public void setLicenseHeaderFile( File licenseHeaderFile )
    {
        this.licenseHeaderFile = licenseHeaderFile;
    }

    /**
     * The bin folder.
     *
     * @return The bin folder.
     */
    public File getBinFolder()
    {
        return binFolder;
    }

    /**
     * Set the bin folder.
     *
     * @param binFolder The new bin folder name.
     */
    public void setBinFolder( File binFolder )
    {
        this.binFolder = binFolder;
    }

    /**
     * Should show console window.
     *
     * @return If console window should be shown.
     */
    public Boolean getShowConsoleWindow() {
      return showConsoleWindow;
    }

    /**
     * Set show console window.
     *
     * @param showConsoleWindow Console window shown.
     */
    public void setShowConsoleWindow(Boolean showConsoleWindow) {
      this.showConsoleWindow = showConsoleWindow;
    }

}
