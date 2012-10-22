/**
 * The MIT License
 * 
 * Copyright 2006-2012 The Codehaus.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.codehaus.mojo.appassembler;

/*
 * 
 * 
 * Copyright 2006-2012 The Codehaus.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

/**
 * This is intended to summarize all generic parts of the Mojos into a single class.
 * First step of refactoring code.
 * 
 * @author <a href="mailto:khmarbaise@soebes.de">Karl Heinz Marbaise</a>
 * 
 */
public abstract class AbstractAppAssemblerMojo
    extends AbstractMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * The layout of the generated Maven repository. Supported types - "default"
     * (Maven2) | "legacy" (Maven1) | "flat" (flat <code>lib/</code> style). The
     * style "legacy" is only supported if you are running under Maven 2.2.1 and
     * before.
     *
     * @parameter default-value="default"
     */
    protected String repositoryLayout;

    /**
     * For those snapshots downloaded from a remote repo, replace the timestamp
     * part with "SNAPSHOT" instead.
     *
     * @parameter default-value="true"
     * @since 1.2.3 (create-repository), 1.3 (assemble and generate-daemons)
     */
    protected boolean useTimestampInSnapshotFileName;

    // -----------------------------------------------------------------------
    // Methods
    // -----------------------------------------------------------------------

    /**
     * Copy artifact to another repository, with an option not to use timestamp in the snapshot filename.
     * @param artifact
     * @param artifactRepository
     * @param useTimestampInSnapshotFileName
     * @throws MojoExecutionException
     */
    protected void installArtifact( Artifact artifact, ArtifactRepository artifactRepository,
                                    boolean useTimestampInSnapshotFileName )
        throws MojoExecutionException
    {
        if ( artifact.getFile() != null )
        {
            try
            {
                // Necessary for the artifact's baseVersion to be set correctly
                // See: http://mail-archives.apache.org/mod_mbox/maven-dev/200511.mbox/%3c437288F4.4080003@apache.org%3e
                artifact.isSnapshot();

                File source = artifact.getFile();

                String localPath = artifactRepository.pathOf( artifact );

                File destination = new File( artifactRepository.getBasedir(), localPath );
                if ( !destination.getParentFile().exists() )
                {
                    destination.getParentFile().mkdirs();
                }

                if ( artifact.isSnapshot() )
                {
                    if ( !useTimestampInSnapshotFileName )
                    {
                        // Don't want timestamp in the snapshot file during copy
                        destination = new File( destination.getParentFile(), source.getName() );
                    }
                }

                if ( ! source.isDirectory() ) {
                    // Sometimes target/classes is in the artifact list and copyFile() would fail.
                    // Need to ignore this condition
                    FileUtils.copyFile( source, destination );
                }

                getLog().info( "Installing artifact " + source.getPath() + " to " + destination );

            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to copy artifact.", e );
            }
        }
    }

    /**
     * Copy artifact to another repository.
     * @param artifact
     * @param artifactRepository
     * @throws MojoExecutionException
     */
    protected void installArtifact( Artifact artifact, ArtifactRepository artifactRepository )
        throws MojoExecutionException
    {
        installArtifact( artifact, artifactRepository, true );
    }

}
