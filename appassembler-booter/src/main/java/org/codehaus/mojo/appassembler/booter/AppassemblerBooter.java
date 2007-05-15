package org.codehaus.mojo.appassembler.booter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.codehaus.mojo.appassembler.model.ClasspathElement;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.mojo.appassembler.model.io.DaemonModelUtil;

/**
 * Reads the appassembler manifest file from the repo, and executes the specified main class.
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class AppassemblerBooter
{
    private static String appName;

    private static boolean debug;

    private static File repoDir;
    
    private static File basedir;

    private static Daemon config;

    private static String mainClassName;

    public static void main( String[] args )
        throws Exception
    {
        URLClassLoader classLoader = setup();

        executeMain( classLoader, args );
    }

    public static URLClassLoader setup()
        throws Exception
    {
        // -----------------------------------------------------------------------
        // Load and validate environmental settings
        // -----------------------------------------------------------------------

        appName = System.getProperty( "app.name" );

        if ( appName == null )
        {
            throw new InternalErrorException( "Missing required system property 'app.name'." );
        }

        debug = Boolean.getBoolean( "app.booter.debug" );

        String b = System.getProperty( "basedir" );

        if ( b == null )
        {
            throw new InternalErrorException( "Missing required system property 'basedir'." );
        }

        basedir = new File( b );
        
        String r = System.getProperty( "app.repo", b );
        
        repoDir = new File ( r );

        // -----------------------------------------------------------------------
        // Load and validate the configuration
        // -----------------------------------------------------------------------

        config = loadConfig();

        mainClassName = config.getMainClass();

        if ( isEmpty( mainClassName ) )
        {
            throw new InternalErrorException( "Missing required property from configuration: 'mainClass'." );
        }

        setSystemProperties();

        return createClassLoader();
    }

    public static URLClassLoader createClassLoader()
        throws Exception
    {
        List classpathUrls = new ArrayList();
        List classPathElements = config.getClasspath();
        Iterator iter = classPathElements.iterator();

        while ( iter.hasNext() )
        {
            ClasspathElement element = (ClasspathElement) iter.next();
            File artifact = new File( repoDir, element.getRelativePath() );

            if ( debug )
            {
                System.err.println( "Adding file to classpath: " + artifact.getAbsolutePath() );
            }

            classpathUrls.add( artifact.toURL() );
        }

        URL[] urls = (URL[]) classpathUrls.toArray( new URL[classpathUrls.size()] );

        return new URLClassLoader( urls, ClassLoader.getSystemClassLoader() );
    }

    /**
     * Pass any given system properties to the java system properties.
     */
    public static void setSystemProperties()
    {
        if ( config.getJvmSettings() == null || config.getJvmSettings().getSystemProperties() == null )
        {
            return;
        }

        JvmSettings jvmSettings = config.getJvmSettings();
        List systemProperties = jvmSettings.getSystemProperties();
        Iterator iter = systemProperties.iterator();
        while ( iter.hasNext() )
        {
            String line = (String) iter.next();
            try
            {
                String[] strings = line.split( "=" );
                String key = strings[0];
                String value = strings[1];
    
                if ( debug )
                {
                    System.err.println( "Setting system property '" + key + "' to '" + value + "'." );
                }
    
                System.setProperty( key, value );
            } catch (Throwable e) {
                if ( debug )
                {
                    System.err.println( "Error Setting system property with value '" + line + "'." );
                }
            }
        }
    }

    private static Daemon loadConfig()
        throws Exception
    {
        String resourceName = "/" + appName + ".xml";

        URL resource = AppassemblerBooter.class.getResource( resourceName );

        if ( debug )
        {
            System.err.println( "Loading configuration file from: " + resource.toExternalForm() );
        }

        if ( resource == null )
        {
            throw new InternalErrorException( "Could not load configuration resource: '" + resourceName + "'." );
        }

        return DaemonModelUtil.loadModel( resource.openStream() );
    }

    public static void executeMain( URLClassLoader classLoader, String[] args )
        throws Exception
    {
        List arguments = config.getCommandLineArguments();

        // -----------------------------------------------------------------------
        // Load the class and main() method
        // -----------------------------------------------------------------------

        // This will always return an instance or throw an exception
        Class mainClass = classLoader.loadClass( mainClassName );

        Method main = mainClass.getMethod( "main", new Class[]{String[].class} );

        if ( arguments == null )
        {
            arguments = Arrays.asList( args );
        }
        else
        {
            arguments.addAll( Arrays.asList( args ) );
        }

        String[] commandLineArgs = (String[]) arguments.toArray( new String[0] );

        // -----------------------------------------------------------------------
        // Setup environment
        // -----------------------------------------------------------------------

        Thread.currentThread().setContextClassLoader( classLoader );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        main.invoke( null, new Object[]{commandLineArgs} );
    }

    // -----------------------------------------------------------------------
    // Utils
    // -----------------------------------------------------------------------

    private static boolean isEmpty( String mainClass )
    {
        return mainClass == null || mainClass.trim().length() == 0;
    }
}
