package org.codehaus.mojo.appassembler.util;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Dependency;

/**
 * A factory that creates Dependency objects.
 *
 * @author Dennis Lundberg
 */
public class DependencyFactory
{
    public static Dependency create( Artifact artifact, ArtifactRepositoryLayout layout )
    {
        Dependency dependency = new Dependency();
        dependency.setGroupId( artifact.getGroupId() );
        dependency.setArtifactId( artifact.getArtifactId() );
        dependency.setVersion( artifact.getVersion() );
        dependency.setClassifier( artifact.getClassifier() );
        dependency.setRelativePath( layout.pathOf( artifact ) );
        return dependency;
    }

    public static Dependency create( Artifact artifact, ArtifactRepositoryLayout layout,
                                     boolean useTimestampInSnapshotFileName )
    {
        Dependency dependency = create( artifact, layout );

        if ( artifact.isSnapshot() && !useTimestampInSnapshotFileName )
        {
            dependency.setRelativePath( ArtifactUtils.pathBaseVersionOf( layout, artifact ) );
        }

        return dependency;
    }

    public static Dependency create( MavenProject project, String id,
                                     ArtifactRepositoryLayout layout )
        throws DaemonGeneratorException
    {
        Artifact artifact = (Artifact) project.getArtifactMap().get( id );

        if ( artifact == null )
        {
            throw new DaemonGeneratorException( "The project has to have a dependency on '" + id + "'." );
        }

        return create( artifact, layout );
    }
}
