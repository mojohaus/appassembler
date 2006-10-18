package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.mojo.appassembler.model.generic.Daemon;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GenericDaemonGeneratorTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        GenericDaemonGenerator generator = (GenericDaemonGenerator) lookup( DaemonGenerator.ROLE, "generic" );

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

        MavenProject project = projectBuilder.buildWithDependencies(
            getTestFile( "src/test/resources/project-1/pom.xml" ), localRepository, profileManager );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        DaemonGeneratorService daemonGeneratorService = (DaemonGeneratorService) lookup( DaemonGeneratorService.ROLE );

        Daemon model = daemonGeneratorService.loadModel( getTestFile( "src/test/resources/project-1/descriptor.xml" ) );

        generator.generate( model, project, localRepository, getTestFile( "target/output-1" ) );
    }
}
