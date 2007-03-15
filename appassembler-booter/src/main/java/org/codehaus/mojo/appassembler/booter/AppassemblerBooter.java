package org.codehaus.mojo.appassembler.booter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
 * 
 */
public class AppassemblerBooter
{
    public static void main( String[] args )
    {
        Daemon config = loadConfig();
        setSystemProperties( config );
        setClassPath( config );
        executeMain( config );

    }

    private static void executeMain( Daemon config )
    {
        String mainClass = config.getMainClass();
        List arguments = config.getCommandLineArguments();

        try
        {
            Method main =
                Thread.currentThread().getContextClassLoader().loadClass( mainClass ).getMethod(
                                                                                                 "main",
                                                                                                 new Class[] { String[].class } );
            if ( !main.isAccessible() )
            {
                main.setAccessible( true );
            }
            String[] args = (String[]) ( arguments == null ? new String[0] : arguments.toArray( new String[0] ) );
            main.invoke( main, new Object[] { args } );
        }
        catch ( Exception e )
        {
            System.out.println( "Could not execute Main method" );
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    private static void setClassPath( Daemon config )
    {
        try
        {
            List classpathUrls = new ArrayList();
            List classPathElements = config.getClasspath();
            Iterator iter = classPathElements.iterator();
            String repoLocation = getRepoLocation();
            while ( iter.hasNext() )
            {
                ClasspathElement element = (ClasspathElement) iter.next();
                File artifact = new File(repoLocation + "/" + element.getRelativePath());
                classpathUrls.add( artifact.toURL() );
            }

            Thread.currentThread().setContextClassLoader(
                                                          new URLClassLoader(
                                                                              (URL[]) classpathUrls.toArray( new URL[classpathUrls.size()] ) ) );
        }
        catch ( MalformedURLException e )
        {
            System.out.println( "Could not set classpath" );
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    private static Daemon loadConfig()
    {
        Daemon config = null;
        try
        {
            config =
                DaemonModelUtil.loadModel( new File(
                                                     AppassemblerBooter.class.getResource( "/" + getAppName() + ".xml" ).toURI() ) );
        }
        catch ( IOException e )
        {
            System.out.println( "Unable to launch app" );
            e.printStackTrace();
            System.exit( -1 );
        }
        catch ( URISyntaxException e )
        {
            System.out.println( "Could not load app descriptor" );
            e.printStackTrace();
            System.exit( -1 );
        }
        return config;
    }

    /**
     * Pass any given system properties to the java system properties.
     */
    protected static void setSystemProperties( Daemon config )
    {
        if ( null == config.getJvmSettings() || null == config.getJvmSettings().getSystemProperties() )
        {
            return;
        }
        JvmSettings jvmSettings = config.getJvmSettings();
        List systemProperties = jvmSettings.getSystemProperties();
        Iterator iter = systemProperties.iterator();
        while ( iter.hasNext() )
        {
            String line = (String) iter.next();
            String key = line.split( "=" )[0];
            String value = line.split( "=" )[1];
            System.setProperty( key, value );
        }
    }

    private static String getAppName()
    {
        return System.getProperty( "app.name" );
    }

    private static String getRepoLocation()
    {
        return System.getProperty( "app.repo" );
    }

}
