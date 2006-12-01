package org.codehaus.mojo.appassembler.daemon;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.profiles.DefaultProfileManager;

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

        ArtifactRepository localRepository = artifactRepositoryFactory.createArtifactRepository( "local", localRepoUrl, new DefaultRepositoryLayout(), policy, policy );

        ProfileManager profileManager = new DefaultProfileManager( getContainer() );

        MavenProject project = projectBuilder.buildWithDependencies( getTestFile( pom ),
                                                                     localRepository, profileManager );

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

        generator.generate( model, project, localRepository, outputDir );
    }
}
