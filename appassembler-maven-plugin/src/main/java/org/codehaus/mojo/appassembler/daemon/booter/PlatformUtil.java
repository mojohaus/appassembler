package org.codehaus.mojo.appassembler.daemon.booter;

import java.util.Iterator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:kristian.nordal@gmail.com">Kristian Nordal</a>
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class PlatformUtil
{

    public static String getClassPath( boolean useBooter, boolean includeConfigurationDirectoryInClasspath,
                                       boolean isWindows, MavenProject project,
                                       ArtifactRepository artifactRepositoryLayout )
    {
        String classPath = "";

        // include the project's own artifact in the classpath
        if ( includeConfigurationDirectoryInClasspath )
        {
            if ( isWindows )
            {
                classPath += "\"%BASEDIR%\"\\etc;";
            }
            else
            {
                classPath += "\"$BASEDIR\"/etc:";
            }
        }

        Artifact appassemblerArtifact =
            (Artifact) project.getPluginArtifactMap().get( "org.codehaus.mojo:appassembler-maven-plugin" );
        if ( useBooter )
        {
            String versionNumber = null == appassemblerArtifact ? "1.0-SNAPSHOT" : appassemblerArtifact.getVersion();
            if ( isWindows )
            {
                classPath +=
                    "\"%REPO%/org/codehaus/mojo/appassembler-booter/" + versionNumber + "/appassembler-booter-"
                                    + versionNumber + ".jar\";";
                classPath +=
                    "\"%REPO%/org/codehaus/mojo/appassembler-model/" + versionNumber + "/appassembler-model-"
                                    + versionNumber + ".jar\"";
            }
            else
            {
                classPath +=
                    "\"$REPO/org/codehaus/mojo/appassembler-booter/" + versionNumber + "/appassembler-booter-"
                                    + versionNumber + ".jar\":";
                classPath +=
                    "\"$REPO/org/codehaus/mojo/appassembler-model/" + versionNumber + "/appassembler-model-"
                                    + versionNumber + ".jar\"";
            }
        }
        else
        {
            for ( Iterator it = project.getArtifacts().iterator(); it.hasNext(); )
            {
                Artifact artifact = (Artifact) it.next();

                if ( isWindows )
                {
                    String path = artifactRepositoryLayout.pathOf( artifact );

                    path = path.replace( '/', '\\' );
                    classPath += "%REPO%\\" + path + ";";
                }
                else
                {
                    classPath += "$REPO/" + artifactRepositoryLayout.pathOf( artifact ) + ":";
                }
            }
        }

        return classPath;
    }

    public static String getBinTemplate( boolean isWindows )
    {
        if ( isWindows )
        {
            return "/windowsBinTemplate";
        }
        else
        {
            return "/unixBinTemplate";
        }
    }

    public static String getInterpolationToken( boolean isWindows )
    {
        if ( isWindows )
        {
            return "#";
        }
        else
        {
            return "@";
        }
    }

    public static String getBinFileExtension( boolean isWindows )
    {
        if ( isWindows )
        {
            return ".bat";
        }
        else
        {
            return "";
        }
    }

    public static String getExtraJvmArgumentsForCli( JvmSettings extraJvmArguments )
    {
        if ( null == extraJvmArguments )
        {
            return "";
        }
        String vmArgs = "";
        vmArgs = addJvmSetting( "-Xms" + extraJvmArguments.getInitialMemorySize(), vmArgs );
        vmArgs = addJvmSetting( "-Xmx" + extraJvmArguments.getMaxMemorySize(), vmArgs );
        vmArgs = addJvmSetting( "-Xss" + extraJvmArguments.getMaxStackSize(), vmArgs );
        if ( null != extraJvmArguments.getSystemProperties() )
        {
            for ( Iterator it = extraJvmArguments.getSystemProperties().iterator(); it.hasNext(); )
            {
                String systemProperty = (String) it.next();
                vmArgs = addSystemProperty( systemProperty, vmArgs );
            }
        }
        return vmArgs;
    }

    public static String getAppArguments( Daemon descriptor )
    {
        String appArguments = "";
        if ( null != descriptor.getCommandLineArguments() )
        {
            for ( Iterator it = descriptor.getCommandLineArguments().iterator(); it.hasNext(); )
            {
                String commmandLineArgument = (String) it.next();
                appArguments = addAppArgument( commmandLineArgument, appArguments );
            }
        }
        return appArguments;
    }

    private static String addAppArgument( String commmandLineArgument, String appArguments )
    {
        if ( !"".equals( appArguments.trim() ) )
        {
            appArguments += " ";
        }

        if ( !StringUtils.isEmpty( commmandLineArgument ) )
        {
            appArguments += commmandLineArgument;
        }

        return appArguments;

    }

    private static String addSystemProperty( String systemProperty, String vmArgs )
    {
        if ( !"".equals( vmArgs.trim() ) )
        {
            vmArgs += " ";
        }

        if ( !StringUtils.isEmpty( systemProperty ) )
        {
            vmArgs += "-D" + systemProperty;
        }
        return vmArgs;
    }

    private static String addJvmSetting( String extraJvmArgument, String vmArgs )
    {
        if ( !"".equals( vmArgs.trim() ) )
        {
            vmArgs += " ";
        }

        if ( extraJvmArgument.length() > 3 )
        {
            vmArgs += extraJvmArgument;
        }
        return vmArgs;
    }

}