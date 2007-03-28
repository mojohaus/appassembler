package org.codehaus.mojo.appassembler.daemon.script;

import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.ClasspathElement;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Directory;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.plexus.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
* @version $Id$
*/
public class Platform
{
    public final static String UNIX_NAME = "unix";

    public final static String WINDOWS_NAME = "windows";

    private final static Map ALL_PLATFORMS;

    private String name;

    private boolean isWindows;

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    static
    {
        ALL_PLATFORMS = new HashMap();
        addPlatform( new Platform( UNIX_NAME, false ) );
        addPlatform( new Platform( WINDOWS_NAME, true ) );
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

            throw new DaemonGeneratorException( "The special platform 'all' can only be used if it is the only element in the platform list." );
        }

        Set platformSet = new HashSet();

        for ( Iterator it = platformList.iterator(); it.hasNext(); )
        {
            String platformName = (String) it.next();

            if ( platformName.equals( "all" ) )
            {
                throw new DaemonGeneratorException( "The special platform 'all' can only be used if it is the only element in a platform list." );
            }

            platformSet.add( getInstance( platformName ) );
        }

        return platformSet;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private Platform( String name, boolean isWindows )
    {
        this.name = name;

        this.isWindows = isWindows;
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
        return isWindows ? ".bat" : "";
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
        List classpath = daemon.getClasspath();

        if ( classpath == null || classpath.size() == 0 )
        {
            return "";
        }

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

            classpathBuffer.append( ((ClasspathElement) object).getRelativePath() );
        }

        return classpathBuffer.toString();
    }

    public String getExtraJvmArguments( JvmSettings extraJvmArguments )
    {
        if ( extraJvmArguments == null )
        {
            return "";
        }

        String vmArgs = "";

        vmArgs = addJvmSetting( "-Xms", extraJvmArguments.getInitialMemorySize(), vmArgs );
        vmArgs = addJvmSetting( "-Xmx", extraJvmArguments.getMaxMemorySize(), vmArgs );
        vmArgs = addJvmSetting( "-Xss", extraJvmArguments.getMaxStackSize(), vmArgs );

        List systemProperties = extraJvmArguments.getSystemProperties();
        if ( systemProperties != null )
        {
            Iterator it = systemProperties.iterator();

            while ( it.hasNext() )
            {
                vmArgs += " \"-D" + it.next() + "\"";
            }
        }

        return vmArgs.trim();
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
}
