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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.GeneratorConfiguration;
import org.codehaus.mojo.appassembler.util.FormattedProperties;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringOutputStream;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role-hint="jsw"
 */
public class JavaServiceWrapperDaemonGenerator
    extends AbstractLogEnabled
    implements DaemonGenerator
{
    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------

    public void generate( DaemonGenerationRequest request )
        throws DaemonGeneratorException
    {
        Daemon daemon = request.getDaemon();

        File outputDirectory = new File( request.getOutputDirectory(), daemon.getId() );

        writeWrapperConfFile( request, daemon, outputDirectory );

        writeScriptFiles( request, daemon, outputDirectory );

        writeLibraryFiles( outputDirectory );

        writeExecutableFiles( outputDirectory );
    }

    private void writeWrapperConfFile( DaemonGenerationRequest request, Daemon daemon, File outputDirectory )
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

        // TODO: write this variable into the start scripts
        confFile.setPropertyAfter( "set.default.REPO_DIR", "repo", "wrapper.java.mainclass" );

        if ( daemon.getJvmSettings() != null && !StringUtils.isEmpty( daemon.getJvmSettings().getInitialMemorySize() ) )
        {
            confFile.setProperty( "wrapper.java.initmemory", daemon.getJvmSettings().getInitialMemorySize() );
        }

        if ( daemon.getJvmSettings() != null && !StringUtils.isEmpty( daemon.getJvmSettings().getMaxMemorySize() ) )
        {
            confFile.setProperty( "wrapper.java.maxmemory", daemon.getJvmSettings().getMaxMemorySize() );
        }

        confFile.setProperty( "wrapper.app.parameter.1", daemon.getMainClass() );

        createClasspath( request, confFile );
        createAdditional( daemon, confFile );
        createParameters( daemon, confFile );

        for ( Iterator i = daemon.getGeneratorConfigurations().iterator(); i.hasNext(); )
        {
            GeneratorConfiguration generatorConfiguration = (GeneratorConfiguration) i.next();

            if ( generatorConfiguration.getGenerator().equals( "jsw" ) )
            {
                for ( Iterator j = generatorConfiguration.getConfiguration().entrySet().iterator(); j.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) j.next();

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
            }
        }

        StringOutputStream string = new StringOutputStream();
        confFile.save( string );

        Reader reader = new InputStreamReader( new StringInputStream( string.toString() ) );

        writeFilteredFile( request, daemon, reader, new File( outputDirectory, "conf/wrapper.conf" ) );
    }

    private static void writeFilteredFile( DaemonGenerationRequest request, Daemon daemon, Reader reader,
                                           File outputFile )
        throws DaemonGeneratorException
    {
        Map context = new HashMap();
        context.put( "app.long.name", request.getMavenProject().getName() );
        context.put( "app.name", daemon.getId() );
        context.put( "app.description", request.getMavenProject().getDescription() );

        InterpolationFilterReader interpolationFilterReader =
            new InterpolationFilterReader( reader, context, "@", "@" );

        writeFile( outputFile, interpolationFilterReader );
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

    private static void createClasspath( DaemonGenerationRequest request, FormattedProperties confFile )
    {
        confFile.setProperty( "wrapper.java.classpath.1", "lib/wrapper.jar" );

        MavenProject project = request.getMavenProject();
        ArtifactRepositoryLayout layout = request.getRepositoryLayout();
        confFile.setProperty( "wrapper.java.classpath.2",
                              "%REPO_DIR%/" + createDependency( layout, project.getArtifact() ).getRelativePath() );
        int counter = 3;
        for ( Iterator i = project.getRuntimeArtifacts().iterator(); i.hasNext(); counter++ )
        {
            Artifact artifact = (Artifact) i.next();

            confFile.setProperty( "wrapper.java.classpath." + counter,
                                  "%REPO_DIR%/" + createDependency( layout, artifact ).getRelativePath() );
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

    private void writeScriptFiles( DaemonGenerationRequest request, Daemon daemon, File outputDirectory )
        throws DaemonGeneratorException
    {
        // TODO: selectively depending on selected platforms instead of always doing both
        InputStream shellScriptInputStream = this.getClass().getResourceAsStream( "bin/sh.script.in" );

        if ( shellScriptInputStream == null )
        {
            throw new DaemonGeneratorException( "Could not load template." );
        }

        Reader reader = new InputStreamReader( shellScriptInputStream );

        writeFilteredFile( request, daemon, reader, new File( outputDirectory, "bin/" + daemon.getId() ) );

        // App.bat is not filtered
        InputStream batchFileInputStream = this.getClass().getResourceAsStream( "bin/App.bat.in" );

        if ( batchFileInputStream == null )
        {
            throw new DaemonGeneratorException( "Could not load template." );
        }

        writeFile( new File( outputDirectory, "bin/" + daemon.getId() + ".bat" ),
                   new InputStreamReader( batchFileInputStream ) );
    }

    private void writeLibraryFiles( File outputDirectory )
        throws DaemonGeneratorException
    {
        copyResourceFile( outputDirectory, "lib/wrapper.jar" );

        // TODO: selectively depending on selected platforms instead of always doing both
        copyResourceFile( outputDirectory, "lib/libwrapper-macosx-universal-32.jnilib" );
        copyResourceFile( outputDirectory, "lib/libwrapper-linux-x86-32.so" );
        copyResourceFile( outputDirectory, "lib/libwrapper-solaris-x86-32.so" );
        copyResourceFile( outputDirectory, "lib/wrapper-windows-x86-32.dll" );
    }

    private void writeExecutableFiles( File outputDirectory )
        throws DaemonGeneratorException
    {
        // TODO: selectively depending on selected platforms instead of always doing both
        copyResourceFile( outputDirectory, "bin/wrapper-macosx-universal-32" );
        copyResourceFile( outputDirectory, "bin/wrapper-linux-x86-32" );
        copyResourceFile( outputDirectory, "bin/wrapper-solaris-x86-32" );
        copyResourceFile( outputDirectory, "bin/wrapper-windows-x86-32.exe" );
    }

    private void copyResourceFile( File outputDirectory, String fileName )
        throws DaemonGeneratorException
    {
        InputStream batchFileInputStream = this.getClass().getResourceAsStream( fileName );

        if ( batchFileInputStream == null )
        {
            throw new DaemonGeneratorException( "Could not load library file: " + fileName );
        }

        writeFile( new File( outputDirectory, fileName ), new InputStreamReader( batchFileInputStream ) );
    }
}
