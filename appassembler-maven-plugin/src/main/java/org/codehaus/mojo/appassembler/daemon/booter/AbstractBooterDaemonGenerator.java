package org.codehaus.mojo.appassembler.daemon.booter;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.script.AbstactScriptDaemonGenerator;
import org.codehaus.mojo.appassembler.model.Classpath;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.Directory;
import org.codehaus.mojo.appassembler.model.JvmSettings;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractBooterDaemonGenerator
    extends AbstactScriptDaemonGenerator
{
    /**
     * @plexus.requirement role-hint="generic"
     */
    private DaemonGenerator genericDaemonGenerator;

    protected AbstractBooterDaemonGenerator( String platformName )
    {
        super( platformName );
    }

    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------

    public void generate( DaemonGenerationRequest request )
        throws DaemonGeneratorException
    {
        Daemon daemon = request.getDaemon();
        JvmSettings jvmSettings = daemon.getJvmSettings();

        File outputDirectory = request.getOutputDirectory();

        // -----------------------------------------------------------------------
        // Generate the generic XML file
        // -----------------------------------------------------------------------

        request.setOutputDirectory( new File( outputDirectory, "etc" ) );

        // TODO: we're assuming state for things that don't really appear stateful
        /*
         * The JVM settings are written to the script, and do not need to go into
         * the manifest.
         */
        daemon.setJvmSettings( null );

        genericDaemonGenerator.generate( request );

        // set back
        daemon.setJvmSettings( jvmSettings );

        // -----------------------------------------------------------------------
        // Generate the shell script
        // -----------------------------------------------------------------------

        Daemon booterDaemon = new Daemon();
        booterDaemon.setId( daemon.getId() );
        booterDaemon.setEnvironmentSetupFileName( daemon.getEnvironmentSetupFileName() );
        booterDaemon.setModelEncoding( daemon.getModelEncoding() );
        // TODO: replace with org.codehaus.mojo.appassembler.booter.AppassemblerBooter.class.getName() and test - trygve
        booterDaemon.setMainClass( "org.codehaus.mojo.appassembler.booter.AppassemblerBooter" );
        booterDaemon.setShowConsoleWindow( daemon.isShowConsoleWindow() );

        booterDaemon.setJvmSettings( jvmSettings );

        MavenProject project = request.getMavenProject();

        Classpath classpath = new Classpath();
        booterDaemon.setClasspath( classpath );
        classpath.addDirectory( createDirectory( "etc" ) );
        classpath.addDependency( createDependency( project, "org.codehaus.mojo.appassembler:appassembler-booter",
                                                   request.getRepositoryLayout() ) );

        // TODO: Transitively resolve the dependencies of the booter - for now we're just hardcoding them in
        classpath.addDependency( createDependency( project, "org.codehaus.mojo.appassembler:appassembler-model",
                                                   request.getRepositoryLayout() ) );
        classpath.addDependency( createDependency( project, "org.codehaus.plexus:plexus-utils",
                                                   request.getRepositoryLayout() ) );
        classpath.addDependency( createDependency( project, "stax:stax-api",
                                                   request.getRepositoryLayout() ) );
        classpath.addDependency( createDependency( project, "stax:stax",
                                                   request.getRepositoryLayout() ) );
        
        scriptGenerator.createBinScript( getPlatformName(), booterDaemon, outputDirectory );
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private static Dependency createDependency( MavenProject project, String id,
                                                ArtifactRepositoryLayout artifactRepositoryLayout )
        throws DaemonGeneratorException
    {
        Artifact artifact = (Artifact) project.getArtifactMap().get( id );

        if ( artifact == null )
        {
            throw new DaemonGeneratorException( "The project has to have a dependency on '" + id + "'." );
        }

        Dependency dependency = new Dependency();

        dependency.setRelativePath( artifactRepositoryLayout.pathOf( artifact ) );
        return dependency;
    }

    private static Directory createDirectory( String relativePath )
    {
        Directory directory = new Directory();
        directory.setRelativePath( relativePath );
        return directory;
    }
}
