package org.codehaus.mojo.appassembler.daemon.script;

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

import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.ClasspathElement;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.Directory;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.plexus.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Platform
{
    public static final String UNIX_NAME = "unix";

    public static final String WINDOWS_NAME = "windows";

    private static final Map ALL_PLATFORMS;

    private static final String DEFAULT_UNIX_BIN_FILE_EXTENSION = "";

    private static final String DEFAULT_WINDOWS_BIN_FILE_EXTENSION = ".bat";

    private String binFileExtension;

    private String name;

    private boolean isWindows;

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    static
    {
        ALL_PLATFORMS = new HashMap();
        addPlatform( new Platform( UNIX_NAME, false, DEFAULT_UNIX_BIN_FILE_EXTENSION ) );
        addPlatform( new Platform( WINDOWS_NAME, true, DEFAULT_WINDOWS_BIN_FILE_EXTENSION ) );
    }

    private static Platform addPlatform( Platform platform )
    {
        ALL_PLATFORMS.put( platform.name, platform );

        return platform;
    }

    public static Platform getInstance( String platformName )
        throws DaemonGeneratorException
    {
        Platform platform = (Platform) ALL_PLATFORMS.get( platformName );

        if ( platform == null )
        {
            throw new DaemonGeneratorException( "Unknown platform name '" + platformName + "'" );
        }

        return platform;
    }

    public static Set getAllPlatformNames()
    {
        return ALL_PLATFORMS.keySet();
    }

    public static Set getAllPlatforms()
    {
        return new HashSet( ALL_PLATFORMS.values() );
    }

    public static Set getPlatformSet( List platformList )
        throws DaemonGeneratorException
    {
        return getPlatformSet( platformList, new HashSet( ALL_PLATFORMS.values() ) );
    }

    public static Set getPlatformSet( List platformList, Set allSet )
        throws DaemonGeneratorException
    {
        if ( platformList == null )
        {
            return allSet;
        }

        if ( platformList.size() == 1 )
        {
            Object first = platformList.get( 0 );

            if ( "all".equals( first ) )
            {
                return allSet;
            }

            throw new DaemonGeneratorException(
                "The special platform 'all' can only be used if it is the only element in the platform list." );
        }

        Set platformSet = new HashSet();

        for ( Iterator it = platformList.iterator(); it.hasNext(); )
        {
            String platformName = (String) it.next();

            if ( platformName.equals( "all" ) )
            {
                throw new DaemonGeneratorException(
                    "The special platform 'all' can only be used if it is the only element in a platform list." );
            }

            platformSet.add( getInstance( platformName ) );
        }

        return platformSet;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private Platform( String name, boolean isWindows, String binFileExtension )
    {
        this.name = name;

        this.isWindows = isWindows;

        this.binFileExtension = binFileExtension;
    }

    // -----------------------------------------------------------------------
    // The platform-specific bits
    // -----------------------------------------------------------------------

    public String getInterpolationToken()
    {
        return isWindows ? "#" : "@";
    }

    public String getBinFileExtension()
    {
        return binFileExtension;
    }

    public String getBasedir()
    {
        return isWindows ? "\"%BASEDIR%\"" : "\"$BASEDIR\"";
    }

    public String getRepo()
    {
        return isWindows ? "\"%REPO%\"" : "\"$REPO\"";
    }

    public String getSeparator()
    {
        return isWindows ? "\\" : "/";
    }

    public String getPathSeparator()
    {
        return isWindows ? ";" : ":";
    }

    // -----------------------------------------------------------------------
    // This part depend on the platform-specific parts
    // -----------------------------------------------------------------------

    public String getClassPath( Daemon daemon )
        throws DaemonGeneratorException
    {
        List classpath = daemon.getAllClasspathElements();

        StringBuffer classpathBuffer = new StringBuffer();

        for ( Iterator it = classpath.iterator(); it.hasNext(); )
        {
            if ( classpathBuffer.length() > 0 )
            {
                classpathBuffer.append( getPathSeparator() );
            }

            // -----------------------------------------------------------------------
            //
            // -----------------------------------------------------------------------

            Object object = it.next();

            if ( object instanceof Directory )
            {
                Directory directory = (Directory) object;

                if ( directory.getRelativePath().charAt( 0 ) != '/' )
                {
                    classpathBuffer.append( getBasedir() ).append( getSeparator() );
                }
            }
            else if ( object instanceof Dependency )
            {
                classpathBuffer.append( getRepo() ).append( getSeparator() );
            }
            else
            {
                throw new DaemonGeneratorException( "Unknown classpath element type: " + object.getClass().getName() );
            }

            classpathBuffer.append( StringUtils.replace( ( (ClasspathElement) object ).getRelativePath(),
                                                         "/", getSeparator() ) );
        }

        return classpathBuffer.toString();
    }

    public String getExtraJvmArguments( JvmSettings jvmSettings )
    {
        if ( jvmSettings == null )
        {
            return "";
        }

        String vmArgs = "";

        vmArgs = addJvmSetting( "-Xms", jvmSettings.getInitialMemorySize(), vmArgs );
        vmArgs = addJvmSetting( "-Xmx", jvmSettings.getMaxMemorySize(), vmArgs );
        vmArgs = addJvmSetting( "-Xss", jvmSettings.getMaxStackSize(), vmArgs );

        vmArgs += arrayToString( jvmSettings.getExtraArguments(), "" );
        vmArgs += arrayToString( jvmSettings.getSystemProperties(), "-D" );

        return vmArgs.trim();
    }

    private String arrayToString( List strings, String separator )
    {
        String string = "";

        if ( strings != null )
        {
            Iterator it = strings.iterator();

            while ( it.hasNext() )
            {
                String s = (String) it.next();

                if ( s.indexOf( ' ' ) == -1 )
                {
                    string += " " + separator + s;
                }
                else
                {
                    string += " \"" + separator + s + "\"";
                }
            }
        }

        return string;
    }

    public String getAppArguments( Daemon descriptor )
    {
        List commandLineArguments = descriptor.getCommandLineArguments();

        if ( commandLineArguments == null || commandLineArguments.size() == 0 )
        {
            return null;
        }

        if ( commandLineArguments.size() == 1 )
        {
            return (String) commandLineArguments.get( 0 );
        }

        Iterator it = commandLineArguments.iterator();

        String appArguments = (String) it.next();

        while ( it.hasNext() )
        {
            appArguments += " " + it.next();
        }

        return appArguments;
    }

    private String addJvmSetting( String argType, String extraJvmArgument, String vmArgs )
    {
        if ( StringUtils.isEmpty( extraJvmArgument ) )
        {
            return vmArgs;
        }

        return vmArgs + " " + argType + extraJvmArgument;
    }

    public String getEnvSetup( Daemon daemon )
    {
        String envSetup = "";

        String envSetupFileName = daemon.getEnvironmentSetupFileName();

        if ( envSetupFileName != null )
        {
            if ( isWindows )
            {
                String envScriptPath = "%BASEDIR%\\bin\\" + envSetupFileName + ".bat";

                envSetup = "if exist " + envScriptPath + " call " + envScriptPath;
            }
            else
            {
                String envScriptPath = "\"$BASEDIR\"/bin/" + envSetupFileName;
                envSetup = "[ -f " + envScriptPath + " ] && . " + envScriptPath;
            }
        }

        return envSetup;
    }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Platform platform = (Platform) o;

        return name.equals( platform.name );
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    public String getName()
    {
        return name;
    }

    public boolean isShowConsoleWindow( Daemon daemon )
    {
        return daemon.isShowConsoleWindow() && isWindows;
    }

    // -----------------------------------------------------------------------
    // Setters for the platform-specific bits
    // -----------------------------------------------------------------------

    public void setBinFileExtension( String binFileExtension )
    {
        // We can't have a null extension
        if ( binFileExtension == null )
        {
            this.binFileExtension = "";
        }
        else
        {
            this.binFileExtension = binFileExtension;
        }
    }
}
