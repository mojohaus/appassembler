package org.codehaus.mojo.appassembler.util;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.mapping.MappingUtils;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * A factory that creates Dependency objects.
 *
 * @author Dennis Lundberg
 */
public class DependencyFactory
{
    public static Dependency create( Artifact artifact, ArtifactRepositoryLayout layout, String outputFileNameMapping )
    {
        Dependency dependency = new Dependency();
        dependency.setGroupId( artifact.getGroupId() );
        dependency.setArtifactId( artifact.getArtifactId() );
        dependency.setVersion( artifact.getVersion() );
        dependency.setClassifier( artifact.getClassifier() );

        String path = layout.pathOf( artifact );
        if( StringUtils.isNotEmpty( outputFileNameMapping ) )
        {
            // Replace the file name part of the path with one that has been mapped
            File directory = new File( path ).getParentFile();

            try
            {
                String fileName = MappingUtils.evaluateFileNameMapping( outputFileNameMapping, artifact );
                File file = new File( directory, fileName );
                // Always use forward slash as path separator, because that's what layout.pathOf( artifact ) uses
                path = file.getPath().replace( '\\', '/' );
            }
            catch(InterpolationException e)
            {
                // TODO Handle exceptions!
//                throw new MojoExecutionException("Unable to map file name.", e);
            }
        }
        dependency.setRelativePath( path );

        return dependency;
    }

    public static Dependency create( Artifact artifact, ArtifactRepositoryLayout layout,
                                     boolean useTimestampInSnapshotFileName, String outputFileNameMapping )
    {
        Dependency dependency = create( artifact, layout, outputFileNameMapping );

        if ( artifact.isSnapshot() && !useTimestampInSnapshotFileName )
        {
            dependency.setRelativePath( ArtifactUtils.pathBaseVersionOf( layout, artifact ) );
        }

        return dependency;
    }

    public static Dependency create( MavenProject project, String id,
                                     ArtifactRepositoryLayout layout,
                                     String outputFileNameMapping )
        throws DaemonGeneratorException
    {
        Artifact artifact = (Artifact) project.getArtifactMap().get( id );

        if ( artifact == null )
        {
            throw new DaemonGeneratorException( "The project has to have a dependency on '" + id + "'." );
        }

        return create( artifact, layout, outputFileNameMapping );
    }
}
