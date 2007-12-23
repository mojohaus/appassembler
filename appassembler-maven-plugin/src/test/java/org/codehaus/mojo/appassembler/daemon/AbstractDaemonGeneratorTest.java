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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractDaemonGeneratorTest
    extends PlexusTestCase
{
    public void runTest( String generatorId, String pom, String descriptor, String outputPath )
        throws Exception
    {
        File outputDir = getTestFile( outputPath );

        DaemonGenerator generator = (DaemonGenerator) lookup( DaemonGenerator.ROLE, generatorId );

        // -----------------------------------------------------------------------
        // Build the MavenProject instance
        // -----------------------------------------------------------------------

        MavenProjectBuilder projectBuilder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        ArtifactRepositoryFactory artifactRepositoryFactory =
            (ArtifactRepositoryFactory) lookup( ArtifactRepositoryFactory.ROLE );

        ArtifactRepositoryPolicy policy = new ArtifactRepositoryPolicy( true, "never", "never" );

        String localRepoUrl = "file://" + System.getProperty( "user.home" ) + "/.m2/repository";

        ArtifactRepository localRepository = artifactRepositoryFactory.createArtifactRepository( "local", localRepoUrl,
                                                                                                 new DefaultRepositoryLayout(),
                                                                                                 policy, policy );

        ProfileManager profileManager = new DefaultProfileManager( getContainer() );

        MavenProject project =
            projectBuilder.buildWithDependencies( getTestFile( pom ), localRepository, profileManager );

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
}
