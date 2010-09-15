package org.codehaus.mojo.appassembler.daemon.jsw;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.*;
import org.codehaus.mojo.appassembler.util.FormattedProperties;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringOutputStream;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role-hint="jsw"
 */
public class JavaServiceWrapperDaemonGenerator
    extends AbstractLogEnabled
    implements DaemonGenerator
{
    private static final Map jswPlatformsMap = new HashMap() { 
        {   
            put( "linux-x86-32-lib", "lib/libwrapper-linux-x86-32.so" );
            put( "linux-x86-32-exec", "bin/wrapper-linux-x86-32" );
            put( "linux-x86-64-lib", "lib/libwrapper-linux-x86-64.so" );
            put( "linux-x86-64-exec", "bin/wrapper-linux-x86-64" );
            put( "linux-ppc-64-lib", "lib/libwrapper-linux-ppc-64.so" );
            put( "linux-ppc-64-exec", "bin/wrapper-linux-ppc-64" );
            put( "macosx-ppc-32-lib", "lib/libwrapper-macosx-ppc-32.jnilib" );
            put( "macosx-ppc-32-exec", "bin/wrapper-macosx-ppc-32" );
            put( "macosx-x86-universal-32-lib", "lib/libwrapper-macosx-universal-32.jnilib" );
            put( "macosx-x86-universal-32-exec", "bin/wrapper-macosx-universal-32" );
            put( "solaris-sparc-32-lib", "lib/libwrapper-solaris-sparc-32.so" );
            put( "solaris-sparc-32-exec", "bin/wrapper-solaris-sparc-32" );
            put( "solaris-sparc-64-lib", "lib/libwrapper-solaris-sparc-64.so" );
            put( "solaris-sparc-64-exec", "bin/wrapper-solaris-sparc-64" );
            put( "solaris-x86-32-lib", "lib/libwrapper-solaris-x86-32.so" );
            put( "solaris-x86-32-exec", "bin/wrapper-solaris-x86-32" );
            put( "windows-x86-32-lib", "lib/wrapper-windows-x86-32.dll" );
            put( "windows-x86-32-exec", "bin/wrapper-windows-x86-32.exe" );
        }
    };
    
    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------
    
    public void generate( DaemonGenerationRequest request )
        throws DaemonGeneratorException
    {
        Daemon daemon = request.getDaemon();

        File outputDirectory = new File( request.getOutputDirectory(), daemon.getId() );

        Properties configuration = createConfiguration( daemon );

        // Don't want these in the wrapper.conf file
        String appBaseEnvVar = configuration.getProperty( "app.base.envvar", "APP_BASE" );
        configuration.remove( "app.base.envvar" );
        String runAsUserEnvVar = configuration.getProperty( "run.as.user.envvar", "" );
        if ( !runAsUserEnvVar.equals( "" ) )
        {
            runAsUserEnvVar = "RUN_AS_USER=" + runAsUserEnvVar;
            configuration.remove( "run.as.user.envvar" );
        }

        Properties context = createContext( request, daemon );
        context.setProperty( "app.base.envvar", appBaseEnvVar );
        context.setProperty( "run.as.user.envvar", runAsUserEnvVar );

        writeWrapperConfFile( request, daemon, outputDirectory, context, configuration );

        writeScriptFiles( request, daemon, outputDirectory, context );
                
        List jswPlatformIncludes = getJswPlatformIncludes( daemon );        
        
        writeLibraryFiles( outputDirectory, jswPlatformIncludes );

        writeExecutableFiles( outputDirectory, jswPlatformIncludes );
    }

    private void writeWrapperConfFile( DaemonGenerationRequest request, Daemon daemon, File outputDirectory,
                                       Properties context, Properties configuration )
        throws DaemonGeneratorException
    {
        InputStream in = this.getClass().getResourceAsStream( "conf/wrapper.conf.in" );

        if ( in == null )
        {
            throw new DaemonGeneratorException( "Could not load template." );
        }

        FormattedProperties confFile = new FormattedProperties();

        try
        {
            confFile.read( in );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error reading template: " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( in );
        }

        // TODO: configurable?
        confFile.setPropertyAfter( "wrapper.working.dir", "..", "wrapper.java.command" );
        confFile.setProperty( "wrapper.java.library.path.1", "lib" );

        confFile.setPropertyAfter( "set.default.REPO_DIR", "repo", "wrapper.java.mainclass" );
        confFile.setPropertyAfter( "set.default." + context.getProperty( "app.base.envvar" ), ".",
                                   "wrapper.java.mainclass" );

        if ( daemon.getJvmSettings() != null && !StringUtils.isEmpty( daemon.getJvmSettings().getInitialMemorySize() ) )
        {
            confFile.setProperty( "wrapper.java.initmemory", daemon.getJvmSettings().getInitialMemorySize() );
        }

        if ( daemon.getJvmSettings() != null && !StringUtils.isEmpty( daemon.getJvmSettings().getMaxMemorySize() ) )
        {
            confFile.setProperty( "wrapper.java.maxmemory", daemon.getJvmSettings().getMaxMemorySize() );
        }

        confFile.setProperty( "wrapper.app.parameter.1", daemon.getMainClass() );        

        createClasspath( request, confFile, configuration );
        createAdditional( daemon, confFile );
        createParameters( daemon, confFile );

        for ( Iterator i = configuration.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) i.next();

            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if ( value.length() > 0 )
            {
                confFile.setProperty( key, value );
            }
            else
            {
                confFile.removeProperty( key );
            }
        }

        StringOutputStream string = new StringOutputStream();
        confFile.save( string );

        Reader reader = new InputStreamReader( new StringInputStream( string.toString() ) );

        writeFilteredFile( request, daemon, reader, new File( outputDirectory, "conf/wrapper.conf" ), context );
    }

    private Properties createConfiguration( Daemon daemon )
    {
        Properties configuration = new Properties();
        
        for ( Iterator i = daemon.getGeneratorConfigurations().iterator(); i.hasNext(); )
        {
            GeneratorConfiguration generatorConfiguration = (GeneratorConfiguration) i.next();

            if ( generatorConfiguration.getGenerator().equals( "jsw" ) )
            {
                configuration.putAll( generatorConfiguration.getConfiguration() );
            }
        }
        return configuration;
    }

    private static void writeFilteredFile( DaemonGenerationRequest request, Daemon daemon, Reader reader,
                                           File outputFile, Map context )
        throws DaemonGeneratorException
    {
        InterpolationFilterReader interpolationFilterReader =
            new InterpolationFilterReader( reader, context, "@", "@" );

        writeFile( outputFile, interpolationFilterReader );
    }

    private static Properties createContext( DaemonGenerationRequest request, Daemon daemon )
    {
        Properties context = new Properties();
        context.setProperty( "app.long.name", request.getMavenProject().getName() );
        context.setProperty( "app.name", daemon.getId() );
        String description = request.getMavenProject().getDescription();
        if ( description == null )
        {
            description = request.getMavenProject().getName();
        }
        context.setProperty( "app.description", description );
        return context;
    }

    private static void writeFile( File outputFile, Reader reader )
        throws DaemonGeneratorException
    {
        FileWriter out = null;

        try
        {
            outputFile.getParentFile().mkdirs();
            out = new FileWriter( outputFile );

            IOUtil.copy( reader, out );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error writing output file: " + outputFile.getAbsolutePath(), e );
        }
        finally
        {
            IOUtil.close( reader );
            IOUtil.close( out );
        }
    }

    private static void writeFile( File outputFile, InputStream inputStream )
        throws DaemonGeneratorException
    {
        FileOutputStream out = null;

        try
        {
            outputFile.getParentFile().mkdirs();
            out = new FileOutputStream( outputFile );

            IOUtil.copy( inputStream, out );
        }
        catch ( IOException e )
        {            
            throw new DaemonGeneratorException( "Error writing output file: " + outputFile.getAbsolutePath(), e );
        }
        finally
        {
            IOUtil.close( inputStream );
            IOUtil.close( out );
        }
    }

    private static void createClasspath( DaemonGenerationRequest request, FormattedProperties confFile,
                                         Properties configuration )
    {
        final String wrapperClassPathPrefix = "wrapper.java.classpath.";

        int counter = 1;
        confFile.setProperty( wrapperClassPathPrefix + counter++, "lib/wrapper.jar" );

        String configurationDirFirst = configuration.getProperty( "configuration.directory.in.classpath.first" );
        if ( configurationDirFirst != null )
        {
            confFile.setProperty( wrapperClassPathPrefix + counter++, configurationDirFirst );
        }

        MavenProject project = request.getMavenProject();
        ArtifactRepositoryLayout layout = request.getRepositoryLayout();

        confFile.setProperty( wrapperClassPathPrefix + counter++, "%REPO_DIR%/"
            + createDependency( layout, project.getArtifact() ).getRelativePath() );

        Iterator j = project.getRuntimeArtifacts().iterator();
        while ( j.hasNext() )
        {
            Artifact artifact = (Artifact) j.next();

            confFile.setProperty( wrapperClassPathPrefix + counter, "%REPO_DIR%/"
                + createDependency( layout, artifact ).getRelativePath() );
            counter++;
        }

        String configurationDirLast = configuration.getProperty( "configuration.directory.in.classpath.last" );
        if ( configurationDirLast != null )
        {
            confFile.setProperty( wrapperClassPathPrefix + counter++, configurationDirLast );
        }
    }

    private static Dependency createDependency( ArtifactRepositoryLayout layout, Artifact artifact )
    {
        Dependency dependency = new Dependency();
        dependency.setArtifactId( artifact.getArtifactId() );
        dependency.setGroupId( artifact.getGroupId() );
        dependency.setVersion( artifact.getVersion() );
        dependency.setRelativePath( layout.pathOf( artifact ) );
        return dependency;
    }

    private static void createAdditional( Daemon daemon, FormattedProperties confFile )
    {
        if ( daemon.getJvmSettings() != null )
        {
            int count = 1;
            for ( Iterator i = daemon.getJvmSettings().getSystemProperties().iterator(); i.hasNext(); count++ )
            {
                String systemProperty = (String) i.next();
                confFile.setProperty( "wrapper.java.additional." + count, "-D" + systemProperty );
            }
            for ( Iterator i = daemon.getJvmSettings().getExtraArguments().iterator(); i.hasNext(); count++ )
            {
                String extraArgument = (String) i.next();
                confFile.setProperty( "wrapper.java.additional." + count, extraArgument );
            }
        }
    }

    private static void createParameters( Daemon daemon, FormattedProperties confFile )
    {
        int count = 2;
        for ( Iterator i = daemon.getCommandLineArguments().iterator(); i.hasNext(); count++ )
        {
            String argument = (String) i.next();

            confFile.setProperty( "wrapper.app.parameter." + count, argument );
        }
    }

    private void writeScriptFiles( DaemonGenerationRequest request, Daemon daemon, File outputDirectory,
                                   Properties context )
        throws DaemonGeneratorException
    {
        // TODO: selectively depending on selected platforms instead of always doing both
        InputStream shellScriptInputStream = this.getClass().getResourceAsStream( "bin/sh.script.in" );

        if ( shellScriptInputStream == null )
        {
            throw new DaemonGeneratorException( "Could not load template." );
        }

        Reader reader = new InputStreamReader( shellScriptInputStream );

        writeFilteredFile( request, daemon, reader, new File( outputDirectory, "bin/" + daemon.getId() ), context );

        // AppCommand.bat is not filtered
        InputStream batchFileInputStream = this.getClass().getResourceAsStream( "bin/AppCommand.bat.in" );

        if ( batchFileInputStream == null )
        {
            throw new DaemonGeneratorException( "Could not load template." );
        }

        writeFile( new File( outputDirectory, "bin/" + daemon.getId() + ".bat" ),
                   batchFileInputStream );
    }

    private void writeLibraryFiles( File outputDirectory, List jswPlatformIncludes )
        throws DaemonGeneratorException
    {
        copyResourceFile( outputDirectory, "lib/wrapper.jar" );

        for ( Iterator iter = jswPlatformIncludes.iterator(); iter.hasNext(); )
        {
            String platform = (String) iter.next();
            String libFile = (String) jswPlatformsMap.get( platform + "-lib" );
            if ( libFile != null )
            {
                copyResourceFile( outputDirectory, libFile );
            }
            else
            {
                getLogger().warn( "Lib file for " + platform + " not found in map." );
            }
        }
    }

    private void writeExecutableFiles( File outputDirectory, List jswPlatformIncludes )
        throws DaemonGeneratorException
    {
        for ( Iterator iter = jswPlatformIncludes.iterator(); iter.hasNext(); )
        {
            String platform = (String) iter.next();
            String execFile = (String) jswPlatformsMap.get( platform + "-exec" );
            if ( execFile != null )
            {
                copyResourceFile( outputDirectory, execFile );
            }
            else
            {
                getLogger().warn( "Exec file for " + platform + " not found in map." );
            }
        }
    }

    private void copyResourceFile( File outputDirectory, String fileName )
        throws DaemonGeneratorException
    {
        InputStream batchFileInputStream = this.getClass().getResourceAsStream( fileName );

        if ( batchFileInputStream == null )
        {
            throw new DaemonGeneratorException( "Could not load library file: " + fileName );
        }

        writeFile( new File( outputDirectory, fileName ), batchFileInputStream );
    }
    
    private List getJswPlatformIncludes( Daemon daemon )
    {
        List jswPlatformIncludes = null;
        for ( Iterator i = daemon.getGeneratorConfigurations().iterator(); i.hasNext(); )
        {
            GeneratorConfiguration generatorConfiguration = (GeneratorConfiguration) i.next();

            if ( generatorConfiguration.getGenerator().equals( "jsw" ) )
            {
                jswPlatformIncludes = generatorConfiguration.getIncludes();
            }
        }

        // set default if none is specified
        if ( jswPlatformIncludes == null || jswPlatformIncludes.isEmpty() )
        {
            jswPlatformIncludes = new ArrayList();
            jswPlatformIncludes.add( "linux-x86-32" );
            jswPlatformIncludes.add( "macosx-x86-universal-32" );
            jswPlatformIncludes.add( "solaris-x86-32" );
            jswPlatformIncludes.add( "windows-x86-32" );
        }

        return jswPlatformIncludes;
    }
}
