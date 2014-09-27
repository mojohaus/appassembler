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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Daemon
{
    private String id;

    private String mainClass;

    private String wrapperMainClass = "org.tanukisoftware.wrapper.WrapperSimpleApp";

    private String descriptor;

    private List<String> platforms;

    private List<String> commandLineArguments;

    private String configurationDirectory;

    private JvmSettings jvmSettings;

    private List<GeneratorConfiguration> generatorConfigurations;

    private String licenseHeaderFile;

    private String repositoryName;

    private boolean showConsoleWindow = true;

    private String environmentSetupFileName;

    private String endorsedDir;

    private String preWrapperConf;
    
    private String wrapperLogFile;

    /**
     * The daemon id which must be unique.
     *
     * @return The name of the id.
     */
    public String getId()
    {
        return id;
    }

    /**
     * The FQN of the main class.
     *
     * @return The name of the main class.
     */
    public String getMainClass()
    {
        return mainClass;
    }

    /**
     * Wrapper main class
     *
     * @return classname of wrapper main class
     */
    public String getWrapperMainClass()
    {
        return wrapperMainClass;
    }

    /**
     * The descriptor.
     *
     * @return The descriptor string.
     */
    public String getDescriptor()
    {
        return descriptor;
    }

    /**
     * The list of platforms.
     *
     * @return The list of platforms or an empty list if non have been defined before.
     */
    public List<String> getPlatforms()
    {
        if ( platforms == null )
        {
            platforms = new ArrayList<String>();
        }

        return platforms;
    }

    /**
     * Get the list of command line arguments.
     *
     * @return The list of command line arguments.
     */
    public List<String> getCommandLineArguments()
    {
        return commandLineArguments;
    }

    /**
     * Return the configuration directory.
     *
     * @return The configuration directory.
     */
    public String getConfigurationDirectory()
    {
        return configurationDirectory;
    }

    /**
     * Get the current JVM settings.
     *
     * @return The instance with the current JVM settings back.
     */
    public JvmSettings getJvmSettings()
    {
        return jvmSettings;
    }

    /**
     * Return the generator configurations.
     *
     * @return The list of generator configurations.
     */
    public List<GeneratorConfiguration> getGeneratorConfigurations()
    {
        return generatorConfigurations;
    }

    /**
     * Return the name of the license header file.
     *
     * @return The name of the license header file.
     */
    public String getLicenseHeaderFile()
    {
        return licenseHeaderFile;
    }

    /**
     * Return the repository name.
     *
     * @return The repository name.
     */
    public String getRepositoryName()
    {
        return repositoryName;
    }

    /**
     * Return the state of the {@link #showConsoleWindow} flag.
     *
     * @return true if ShowConsoleWindow is active false otherwise.
     */
    public boolean isShowConsoleWindow()
    {
        return showConsoleWindow;
    }

    /**
     * The file name as string.
     *
     * @return The environment setup file name.
     */
    public String getEnvironmentSetupFileName()
    {
        return environmentSetupFileName;
    }

    /**
     * Define the environment setup file name.
     *
     * @param environmentSetupFileName The filename as string.
     */
    public void setEnvironmentSetupFileName( String environmentSetupFileName )
    {
        this.environmentSetupFileName = environmentSetupFileName;
    }

    /**
     * The directory where endorsed libraries can be found.
     *
     * @return The directory where endorsed libraries can be found.
     */
    public String getEndorsedDir()
    {
        return endorsedDir;
    }

    /**
     * Define the endorsed directory where optional jars will be loaded.
     *
     * @param endorsedDir The name of the endorsed directory.
     */
    public void setEndorsedDir( String endorsedDir )
    {
        this.endorsedDir = endorsedDir;
    }

    public String getPreWrapperConf()
    {
        return preWrapperConf;
    }

    public void setPreWrapperConf( String preWrapperConf )
    {
        this.preWrapperConf = preWrapperConf;
    }

    public String getWrapperLogFile()
    {
        return wrapperLogFile;
    }

    public void setWrapperLogFile( String wrapperLogFile )
    {
        this.wrapperLogFile = wrapperLogFile;
    }


}
