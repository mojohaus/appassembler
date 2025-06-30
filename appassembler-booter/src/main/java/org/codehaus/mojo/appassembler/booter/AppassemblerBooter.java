package org.codehaus.mojo.appassembler.booter;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.codehaus.mojo.appassembler.model.ClasspathElement;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.codehaus.mojo.appassembler.model.io.stax.AppassemblerModelStaxReader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.StringUtils;

/**
 * Reads the appassembler manifest file from the repo, and executes the specified main class.
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * TODO: get rid of all the statics
 */
public class AppassemblerBooter
{
    private AppassemblerBooter() 
    {
        
    }
    
    private static boolean debug;

    private static Daemon config;

    private static String mainClassName;

    public static void main( String[] args )
        throws Exception
    {
        URLClassLoader classLoader = setup();

        executeMain( classLoader, args );
    }

    public static URLClassLoader setup()
        throws IOException, XMLStreamException
    {
        // -----------------------------------------------------------------------
        // Load and validate environmental settings
        // -----------------------------------------------------------------------

        String appName = System.getProperty( "app.name" );

        if ( appName == null )
        {
            throw new RuntimeException( "Missing required system property 'app.name'." );
        }

        debug = Boolean.getBoolean( "app.booter.debug" );

        String b = System.getProperty( "basedir" );

        if ( b == null )
        {
            throw new RuntimeException( "Missing required system property 'basedir'." );
        }

        // TODO: shouldn't this be in the descriptor too?
        String r = System.getProperty( "app.repo", b );

        File repoDir = new File( r );

        // -----------------------------------------------------------------------
        // Load and validate the configuration
        // -----------------------------------------------------------------------

        config = loadConfig( appName );

        mainClassName = config.getMainClass();

        if ( isEmpty( mainClassName ) )
        {
            throw new RuntimeException( "Missing required property from configuration: 'mainClass'." );
        }

        setSystemProperties();

        return createClassLoader( repoDir );
    }

    public static URLClassLoader createClassLoader( File repoDir )
        throws MalformedURLException
    {
        List<URL> classpathUrls = new ArrayList<URL>();

        StringBuilder appClassPath = new StringBuilder();
        boolean firstAppClassPathElement = true;

        for ( ClasspathElement element : config.getAllClasspathElements() )
        {
            File artifact = new File( repoDir, element.getRelativePath() );

            if ( debug )
            {
                System.err.println( "Adding file to classpath: " + artifact.getAbsolutePath() );
            }

            classpathUrls.add( artifact.toURL() );

            if (firstAppClassPathElement) {
                firstAppClassPathElement = false;
            } else {
                appClassPath.append(File.pathSeparator);
            }
            appClassPath.append(artifact.getAbsolutePath());
        }

        System.setProperty("app.class.path", appClassPath.toString());

        URL[] urls = (URL[]) classpathUrls.toArray( new URL[classpathUrls.size()] );

        return new URLClassLoader( urls, ClassLoader.getSystemClassLoader() );
    }

    /**
     * Pass any given system properties to the java system properties.
     */
    public static void setSystemProperties()
    {
        JvmSettings jvmSettings = config.getJvmSettings();
        if ( jvmSettings != null && jvmSettings.getSystemProperties() != null )
        {
            for ( String line : jvmSettings.getSystemProperties() )
            {
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
                }
                catch ( Throwable e )
                {
                    if ( debug )
                    {
                        System.err.println( "Error Setting system property with value '" + line + "'." );
                    }
                }
            }
        }
    }

    private static Daemon loadConfig( String appName )
        throws IOException, XMLStreamException
    {
        String resourceName = "/" + appName + ".xml";

        InputStream resource = AppassemblerBooter.class.getResourceAsStream( resourceName );

        if ( debug )
        {
            System.err.println( "Loading configuration file from: " + resourceName );
        }

        if ( resource == null )
        {
            throw new RuntimeException( "Could not load configuration resource: '" + resourceName + "'." );
        }

        try
        {
            AppassemblerModelStaxReader reader = new AppassemblerModelStaxReader();
            return reader.read( new InputStreamReader( resource, "UTF-8" ) );
        }
        finally
        {
            resource.close();
        }
    }

    public static void executeMain( URLClassLoader classLoader, String[] args )
        throws Exception
    {
        List<String> arguments = config.getCommandLineArguments();

        // -----------------------------------------------------------------------
        // Load the class and main() method
        // -----------------------------------------------------------------------

        // This will always return an instance or throw an exception
        Class mainClass = classLoader.loadClass( mainClassName );

        Method main = mainClass.getMethod( "main", new Class[] { String[].class } );

        if ( arguments == null )
        {
            arguments = Arrays.asList( args );
        }
        else
        {
            arguments.addAll( Arrays.asList( args ) );
        }

        String[] commandLineArgs = interpolateArguments((String[])arguments.toArray( new String[arguments.size()] ));

        // -----------------------------------------------------------------------
        // Setup environment
        // -----------------------------------------------------------------------

        Thread.currentThread().setContextClassLoader( classLoader );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        main.invoke( null, new Object[] { commandLineArgs } );
    }

    private static String[] interpolateArguments(String[] arguments) {
        if (arguments == null) {
            return null;
        }

        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = interpolateBaseDirAndRepo(arguments[i]);
        }
        return arguments;
    }

    private static String interpolateBaseDirAndRepo( String content )
    {
        StringReader sr = new StringReader( content );
        StringWriter result = new StringWriter();

        Map<Object, Object> context = new HashMap<Object, Object>();

        final String baseDir = System.getProperty( "basedir", System.getProperty( "app.home" ) );
        if ( baseDir != null && baseDir.length() > 0) {
            context.put( "BASEDIR", baseDir );
        }

        final String repo = System.getProperty( "app.repo" );
        if ( repo != null && repo.length() > 0 ) {
            context.put("REPO", repo);
        }

        InterpolationFilterReader interpolationFilterReader = new InterpolationFilterReader( sr, context, "@", "@" );
        try
        {
            IOUtil.copy( interpolationFilterReader, result );
        }
        catch ( IOException e )
        {
            // shouldn't happen...
        }

        return result.toString();
    }

    // -----------------------------------------------------------------------
    // Utils
    // -----------------------------------------------------------------------

    private static boolean isEmpty( String mainClass )
    {
        return mainClass == null || mainClass.trim().length() == 0;
    }
}
