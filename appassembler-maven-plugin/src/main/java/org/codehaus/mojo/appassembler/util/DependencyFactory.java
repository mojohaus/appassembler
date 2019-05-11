package org.codehaus.mojo.appassembler.util;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.mapping.MappingUtils;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.util.StringUtils;

/**
 * A factory that creates Dependency objects.
 *
 * @author Dennis Lundberg
 */
public class DependencyFactory
{
    private DependencyFactory() 
    {
        
    }
    
    /**
     * Used by GenericDaemonGenerator.
     * @param artifact {@link Artifact}
     * @param layout {@link ArtifactRepositoryLayout}
     * @param outputFileNameMapping The name mapping.
     * @return the dependency.
     */
    public static Dependency create( Artifact artifact, ArtifactRepositoryLayout layout, String outputFileNameMapping )
    {
        Dependency dependency = new Dependency();
        dependency.setGroupId( artifact.getGroupId() );
        dependency.setArtifactId( artifact.getArtifactId() );
        dependency.setVersion( artifact.getVersion() );
        dependency.setClassifier( artifact.getClassifier() );

        String path = layout.pathOf( artifact );
        if ( StringUtils.isNotEmpty( outputFileNameMapping ) )
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
            catch ( InterpolationException e )
            {
                // TODO Handle exceptions!
                // throw new MojoExecutionException("Unable to map file name.", e);
            }
        }
        dependency.setRelativePath( path );

        return dependency;
    }

    /**
     * Used by AssembleMojo and JavaServiceWrapperDaemonGenerator.
     * @param artifact {@link Artifact}
     * @param layout {@link ArtifactRepositoryLayout}
     * @param useTimestampInSnapshotFileName timestamp or not.
     * @param outputFileNameMapping The name mapping.
     * @return the dependency.
     */
    public static Dependency create(Artifact artifact, ArtifactRepositoryLayout layout,
                                    boolean useTimestampInSnapshotFileName, String outputFileNameMapping)
    {
        Dependency dependency = create( artifact, layout, outputFileNameMapping );

        if ( artifact.isSnapshot() && !useTimestampInSnapshotFileName )
        {
            dependency.setRelativePath( ArtifactUtils.pathBaseVersionOf( layout, artifact ) );
        }

        return dependency;
    }

    /**
     * Used by AbstractBooterDaemonGenerator.
     * @param project {@link MavenProject}
     * @param id The id.
     * @param layout {@link ArtifactRepositoryLayout}
     * @param outputFileNameMapping The name mapping.
     * @return the dependency.
     */
    public static Dependency create( MavenProject project, String id, ArtifactRepositoryLayout layout,
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
