package org.codehaus.mojo.appassembler.daemon;

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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractDaemonGeneratorTest
    extends PlexusTestCase
{
    private static String appassemblerVersion;

    public void runTest( String generatorId, String pom, String descriptor, String outputPath )
        throws Exception
    {
        File outputDir = getTestFile( outputPath );

        DaemonGenerator generator = (DaemonGenerator) lookup( DaemonGenerator.ROLE, generatorId );

        // -----------------------------------------------------------------------
        // Build the MavenProject instance
        // -----------------------------------------------------------------------

        MavenProjectBuilder projectBuilder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        MavenSettingsBuilder settingsBuilder = (MavenSettingsBuilder) lookup( MavenSettingsBuilder.ROLE );
        Settings settings = settingsBuilder.buildSettings();
        
        ArtifactRepositoryFactory artifactRepositoryFactory =
            (ArtifactRepositoryFactory) lookup( ArtifactRepositoryFactory.ROLE );

        String localRepoUrl = new File( settings.getLocalRepository() ).toURL().toExternalForm();

        ArtifactRepository localRepository =
            artifactRepositoryFactory.createDeploymentArtifactRepository( "local", localRepoUrl,
                                                                          new DefaultRepositoryLayout(), false );

        ProfileManager profileManager = new DefaultProfileManager( getContainer() );

        File tempPom = createFilteredFile( pom );

        MavenProject project = projectBuilder.buildWithDependencies( tempPom, localRepository, profileManager );

        // -----------------------------------------------------------------------
        // Clean the output directory
        // -----------------------------------------------------------------------

        FileUtils.deleteDirectory( outputDir );
        FileUtils.forceMkdir( outputDir );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        DaemonGeneratorService daemonGeneratorService = (DaemonGeneratorService) lookup( DaemonGeneratorService.ROLE );

        Daemon model = daemonGeneratorService.loadModel( getTestFile( descriptor ) );

        generator.generate( new DaemonGenerationRequest( model, project, localRepository, outputDir ) );
    }

    protected File createFilteredFile( String file )
        throws IOException, FileNotFoundException, DaemonGeneratorException, XmlPullParserException
    {
        String version = getAppAssemblerBooterVersion();
        Properties context = new Properties();
        context.setProperty( "appassembler.version", version );

        File tempPom = File.createTempFile( "appassembler", "tmp" );
        tempPom.deleteOnExit();

        InterpolationFilterReader reader =
            new InterpolationFilterReader( new FileReader( getTestFile( file ) ), context, "@", "@" );
        FileWriter out = null;

        try
        {
            out = new FileWriter( tempPom );

            IOUtil.copy( reader, out );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error writing output file: " + tempPom.getAbsolutePath(), e );
        }
        finally
        {
            IOUtil.close( reader );
            IOUtil.close( out );
        }
        return tempPom;
    }

    private static String getAppAssemblerBooterVersion()
        throws IOException, XmlPullParserException
    {
        if ( appassemblerVersion == null )
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            FileReader fileReader = new FileReader( getTestFile( "pom.xml" ) );
            try
            {
                appassemblerVersion = reader.read( fileReader ).getParent().getVersion();
            }
            finally
            {
                IOUtil.close( fileReader );
            }
        }
        return appassemblerVersion;
    }

    protected static String normalizeLineTerminators( String input )
    {
        return ( input != null ) ? input.replaceAll( "(\r\n)|(\r)", "\n" ) : null;
    }
}
